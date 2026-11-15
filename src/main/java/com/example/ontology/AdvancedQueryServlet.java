package com.example.ontology;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Advanced, additive query API. Existing API routes and response formats are
 * intentionally left unchanged.
 */
@WebServlet("/api/v2/query/*")
public class AdvancedQueryServlet extends HttpServlet {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String className = requiredString(body, "className");
            String mode = stringValue(body, "mode", "records").toLowerCase(Locale.ROOT);
            if (!Set.of("records", "count", "aggregate").contains(mode)) {
                throw new BadRequest("mode must be records, count, or aggregate.");
            }

            OntologyReader reader = OntologyReader.getInstance();
            Set<String> classes = reader.getOntologyClasses();
            if (!classes.contains(className)) {
                throw new BadRequest("Unknown class: " + className);
            }

            Set<String> dataProperties = reader.getDataProperties(className);
            Set<String> objectProperties = reader.getObjectProperties(className, false);
            Set<String> allProperties = new HashSet<>(dataProperties);
            allProperties.addAll(objectProperties);

            JsonObject filters = body.has("filters") && body.get("filters").isJsonObject()
                    ? body.getAsJsonObject("filters") : body;
            Map<String, List<String>> dataFilters = parseFilters(
                    filters, "dataProperties", dataProperties);
            Map<String, List<String>> objectFilters = parseFilters(
                    filters, "objectProperties", objectProperties);

            Set<String> selected = parseSelection(body, allProperties);
            SortSpec sort = parseSort(body, allProperties);
            List<AggregateSpec> aggregateSpecs = parseAggregates(body, dataProperties);

            int limit = parseInt(body, "limit", DEFAULT_LIMIT);
            int offset = parseInt(body, "offset", 0);
            if (limit < 1 || limit > MAX_LIMIT) {
                throw new BadRequest("limit must be between 1 and " + MAX_LIMIT + ".");
            }
            if (offset < 0) {
                throw new BadRequest("offset must be zero or greater.");
            }

            boolean includeFuzzy = booleanValue(body, "includeFuzzy", false);
            double fuzzyThreshold = doubleValue(body, "fuzzyThreshold", 0.7);
            if (fuzzyThreshold < 0 || fuzzyThreshold > 1) {
                throw new BadRequest("fuzzyThreshold must be between 0 and 1.");
            }

            Map<String, Object> query = reader.queryIndividuals(
                    className, dataFilters, objectFilters, includeFuzzy, fuzzyThreshold);
            Set<String> matches = toStringSet(query.get("matchedIndividuals"));
            List<String> orderedIds = new ArrayList<>(matches);

            if (mode.equals("count")) {
                Map<String, Object> result = baseResponse(className, mode, matches.size());
                result.put("count", matches.size());
                writeJson(response, result);
                return;
            }

            Map<String, Map<String, List<String>>> detailsById = new HashMap<>();
            for (String id : orderedIds) {
                detailsById.put(id, reader.getIndividualDetailsMulti(id));
            }

            if (mode.equals("aggregate")) {
                if (aggregateSpecs.isEmpty()) {
                    throw new BadRequest("mode aggregate requires a non-empty aggregates array.");
                }
                Map<String, Object> result = baseResponse(className, mode, matches.size());
                Map<String, Object> aggregates = new LinkedHashMap<>();
                for (AggregateSpec spec : aggregateSpecs) {
                    aggregates.put(spec.alias, calculateAggregate(spec, orderedIds, detailsById));
                }
                result.put("aggregates", aggregates);
                result.put("aggregationScope", "all matching individuals; limit and offset are not applied");
                writeJson(response, result);
                return;
            }

            if (sort != null) {
                Comparator<String> comparator = (left, right) -> compareValues(
                        first(detailsById.get(left).get(sort.property)),
                        first(detailsById.get(right).get(sort.property)));
                if (sort.descending) {
                    comparator = comparator.reversed();
                }
                // Stable URI-local-name tie-break makes page boundaries deterministic.
                comparator = comparator.thenComparing(Comparator.naturalOrder());
                orderedIds.sort(comparator);
            } else {
                orderedIds.sort(Comparator.naturalOrder());
            }

            int from = Math.min(offset, orderedIds.size());
            int to = (int) Math.min((long) from + limit, orderedIds.size());
            List<Map<String, Object>> records = new ArrayList<>();
            for (String id : orderedIds.subList(from, to)) {
                Map<String, List<String>> allDetails = detailsById.get(id);
                Map<String, List<String>> data = new LinkedHashMap<>();
                if (selected.isEmpty()) {
                    data.putAll(allDetails);
                } else {
                    for (String property : new TreeSet<>(selected)) {
                        if (allDetails.containsKey(property)) {
                            data.put(property, allDetails.get(property));
                        }
                    }
                }
                Map<String, Object> record = new LinkedHashMap<>();
                record.put("id", id);
                record.put("data", data);
                records.add(record);
            }

            Map<String, Object> result = baseResponse(className, mode, matches.size());
            result.put("records", records);
            Map<String, Object> pagination = new LinkedHashMap<>();
            pagination.put("limit", limit);
            pagination.put("offset", offset);
            pagination.put("returned", records.size());
            pagination.put("totalMatches", matches.size());
            pagination.put("hasMore", to < orderedIds.size());
            pagination.put("nextOffset", to < orderedIds.size() ? to : null);
            result.put("pagination", pagination);
            writeJson(response, result);
        } catch (BadRequest e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, Map.of(
                    "status", "error",
                    "message", "Advanced query failed.",
                    "detail", e.getMessage() == null ? "Unknown error" : e.getMessage()));
        }
    }

    private Map<String, Object> baseResponse(String className, String mode, int total) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("class", className);
        result.put("mode", mode);
        result.put("totalMatches", total);
        return result;
    }

    private Map<String, List<String>> parseFilters(
            JsonObject body, String field, Set<String> allowedProperties) {
        Map<String, List<String>> filters = new LinkedHashMap<>();
        if (!body.has(field)) {
            return filters;
        }
        if (!body.get(field).isJsonObject()) {
            throw new BadRequest(field + " must be a JSON object.");
        }
        JsonObject json = body.getAsJsonObject(field);
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            validateProperty(entry.getKey(), allowedProperties, field);
            List<String> values = new ArrayList<>();
            JsonElement value = entry.getValue();
            if (value.isJsonArray()) {
                for (JsonElement element : value.getAsJsonArray()) {
                    if (element.isJsonNull() || element.isJsonObject() || element.isJsonArray()) {
                        throw new BadRequest("Filter values must be strings or numbers.");
                    }
                    values.add(element.getAsString());
                }
            } else if (!value.isJsonNull() && !value.isJsonObject()) {
                values.add(value.getAsString());
            } else {
                throw new BadRequest("Filter values must be strings or numbers.");
            }
            filters.put(entry.getKey(), values);
        }
        return filters;
    }

    private Set<String> parseSelection(JsonObject body, Set<String> allowedProperties) {
        if (!body.has("select") || body.get("select").isJsonNull()) {
            return Set.of();
        }
        if (!body.get("select").isJsonArray()) {
            throw new BadRequest("select must be an array of property names.");
        }
        Set<String> selected = new HashSet<>();
        for (JsonElement item : body.getAsJsonArray("select")) {
            if (!item.isJsonPrimitive()) {
                throw new BadRequest("select entries must be property names.");
            }
            String property = item.getAsString();
            validateProperty(property, allowedProperties, "select");
            selected.add(property);
        }
        return selected;
    }

    private SortSpec parseSort(JsonObject body, Set<String> allowedProperties) {
        if (!body.has("orderBy") || body.get("orderBy").isJsonNull()) {
            return null;
        }
        JsonElement orderBy = body.get("orderBy");
        String property;
        String direction = "asc";
        if (orderBy.isJsonPrimitive()) {
            property = orderBy.getAsString();
        } else if (orderBy.isJsonObject()) {
            JsonObject object = orderBy.getAsJsonObject();
            property = requiredString(object, "property");
            direction = stringValue(object, "direction", "asc");
        } else {
            throw new BadRequest("orderBy must be a property name or an object.");
        }
        validateProperty(property, allowedProperties, "orderBy");
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new BadRequest("orderBy.direction must be asc or desc.");
        }
        return new SortSpec(property, direction.equalsIgnoreCase("desc"));
    }

    private List<AggregateSpec> parseAggregates(JsonObject body, Set<String> dataProperties) {
        List<AggregateSpec> specs = new ArrayList<>();
        if (!body.has("aggregates")) {
            return specs;
        }
        if (!body.get("aggregates").isJsonArray()) {
            throw new BadRequest("aggregates must be an array.");
        }
        JsonArray array = body.getAsJsonArray("aggregates");
        int index = 0;
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                throw new BadRequest("Each aggregate must be an object.");
            }
            JsonObject aggregate = element.getAsJsonObject();
            String function = requiredString(aggregate, "function").toLowerCase(Locale.ROOT);
            if (!Set.of("count", "sum", "avg", "min", "max").contains(function)) {
                throw new BadRequest("Unsupported aggregate function: " + function);
            }
            String property = aggregate.has("property") && !aggregate.get("property").isJsonNull()
                    ? aggregate.get("property").getAsString() : null;
            if (!function.equals("count") && property == null) {
                throw new BadRequest(function + " requires a numeric data property.");
            }
            if (property != null) {
                validateProperty(property, dataProperties, "aggregates");
                if (!function.equals("count") && !dataProperties.contains(property)) {
                    throw new BadRequest(function + " requires a numeric data property.");
                }
            }
            String alias = stringValue(aggregate, "as",
                    function + (property == null ? "" : "_" + property) + "_" + index);
            specs.add(new AggregateSpec(function, property, alias));
            index++;
        }
        return specs;
    }

    private Object calculateAggregate(
            AggregateSpec spec, List<String> ids,
            Map<String, Map<String, List<String>>> detailsById) {
        if (spec.function.equals("count") && spec.property == null) {
            return ids.size();
        }
        List<BigDecimal> values = new ArrayList<>();
        long propertyValueCount = 0;
        for (String id : ids) {
            List<String> rawValues = detailsById.get(id).get(spec.property);
            if (rawValues == null) {
                continue;
            }
            for (String raw : rawValues) {
                propertyValueCount++;
                if (spec.function.equals("count")) {
                    continue;
                }
                try {
                    values.add(new BigDecimal(raw));
                } catch (NumberFormatException e) {
                    throw new BadRequest("Property " + spec.property
                            + " contains a non-numeric value; cannot apply " + spec.function + ".");
                }
            }
        }
        if (spec.function.equals("count")) {
            return propertyValueCount;
        }
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        switch (spec.function) {
            case "sum":
                return sum;
            case "avg":
                return sum.divide(BigDecimal.valueOf(values.size()), 12, BigDecimal.ROUND_HALF_UP)
                        .stripTrailingZeros();
            case "min":
                return values.stream().min(Comparator.naturalOrder()).orElse(null);
            case "max":
                return values.stream().max(Comparator.naturalOrder()).orElse(null);
            default:
                throw new BadRequest("Unsupported aggregate function: " + spec.function);
        }
    }

    private void validateProperty(String property, Set<String> allowed, String field) {
        if (!allowed.contains(property)) {
            throw new BadRequest("Unknown or inapplicable property in " + field + ": " + property);
        }
    }

    private int compareValues(String left, String right) {
        if (left == null && right == null) return 0;
        if (left == null) return 1;
        if (right == null) return -1;
        try {
            return new BigDecimal(left).compareTo(new BigDecimal(right));
        } catch (NumberFormatException ignored) {
            return left.compareToIgnoreCase(right);
        }
    }

    private String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private Set<String> toStringSet(Object value) {
        Set<String> result = new HashSet<>();
        if (value instanceof Collection<?>) {
            for (Object item : (Collection<?>) value) {
                if (item != null) result.add(item.toString());
            }
        }
        return result;
    }

    private int parseInt(JsonObject object, String key, int fallback) {
        if (!object.has(key) || object.get(key).isJsonNull()) return fallback;
        try {
            return object.get(key).getAsInt();
        } catch (RuntimeException e) {
            throw new BadRequest(key + " must be an integer.");
        }
    }

    private double doubleValue(JsonObject object, String key, double fallback) {
        if (!object.has(key) || object.get(key).isJsonNull()) return fallback;
        try {
            return object.get(key).getAsDouble();
        } catch (RuntimeException e) {
            throw new BadRequest(key + " must be numeric.");
        }
    }

    private boolean booleanValue(JsonObject object, String key, boolean fallback) {
        if (!object.has(key) || object.get(key).isJsonNull()) return fallback;
        try {
            return object.get(key).getAsBoolean();
        } catch (RuntimeException e) {
            throw new BadRequest(key + " must be boolean.");
        }
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()
                || !object.get(key).isJsonPrimitive()) {
            throw new BadRequest(key + " is required and must be a string.");
        }
        String value = object.get(key).getAsString().trim();
        if (value.isEmpty()) throw new BadRequest(key + " cannot be empty.");
        return value;
    }

    private String stringValue(JsonObject object, String key, String fallback) {
        if (!object.has(key) || object.get(key).isJsonNull()) return fallback;
        if (!object.get(key).isJsonPrimitive()) throw new BadRequest(key + " must be a string.");
        return object.get(key).getAsString();
    }

    private void writeJson(HttpServletResponse response, Object value) throws IOException {
        response.getWriter().write(gson.toJson(value));
    }

    private static final class SortSpec {
        private final String property;
        private final boolean descending;
        private SortSpec(String property, boolean descending) {
            this.property = property;
            this.descending = descending;
        }
    }

    private static final class AggregateSpec {
        private final String function;
        private final String property;
        private final String alias;
        private AggregateSpec(String function, String property, String alias) {
            this.function = function;
            this.property = property;
            this.alias = alias;
        }
    }

    private static final class BadRequest extends RuntimeException {
        private BadRequest(String message) { super(message); }
    }
}
