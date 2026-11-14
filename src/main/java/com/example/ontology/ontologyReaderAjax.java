package com.example.ontology;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "ontologyReaderAjax", urlPatterns = {"/ontologyReaderAjax"})
public class ontologyReaderAjax extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Prevent caching to always fetch fresh ontology data
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        OntologyReader ontologyReader = OntologyReader.getInstance();  // ✅ Use fresh instance per request

        String type = request.getParameter("type");
        String className = request.getParameter("class");
        String individualName = request.getParameter("individual");

        try {
            if ("dataProperties".equals(type)) {
                Set<String> dataProperties = ontologyReader.getDataProperties(className);
                out.print(gson.toJson(dataProperties));

            } else if ("dataPropertiesWithComments".equals(type)) {
                List<Map<String, Object>> dataProperties = ontologyReader.getDataPropertiesWithMeta(className);
                out.print(gson.toJson(dataProperties));
                /*Map<String, String> dataProperties = ontologyReader.getDataPropertiesWithComments(className);
                out.print(gson.toJson(dataProperties));*/
            } else if ("objectProperties".equals(type)) {
                boolean excludeInverses = true;

                // Check if the optional flag was passed (e.g., &excludeInverses=true)
                if (request.getParameter("excludeInverses") != null) {
                    excludeInverses = Boolean.parseBoolean(request.getParameter("excludeInverses"));
                }

                Set<String> objectProperties = ontologyReader.getObjectProperties(className, excludeInverses);
                out.print(gson.toJson(objectProperties));
            } else if ("objectPropertiesWithComments".equals(type)) {
                boolean excludeInverses = true;
                if (request.getParameter("excludeInverses") != null) {
                    excludeInverses = Boolean.parseBoolean(request.getParameter("excludeInverses"));
                }
                Map<String, String> objectProperties = ontologyReader.getObjectPropertiesWithComments(className, excludeInverses);
                out.print(gson.toJson(objectProperties));
            } else if ("instances".equals(type)) {
                String relatedClass = request.getParameter("relatedClass");

                if (relatedClass != null) {
                    String actualClass = ontologyReader.getRangeClass(relatedClass);

                    if (actualClass != null) {
                        //Set<String> instances = ontologyReader.getInstancesOfClass(actualClass);
                        //out.print(gson.toJson(instances));
                        List<Map<String, String>> instances = ontologyReader.getInstancesWithTypes(actualClass);
                        out.print(gson.toJson(instances));
                    } else {
                        out.print("[]");
                    }
                } else {
                    out.print("[]");
                }

            } else if ("directInstances".equals(type)) {
                String targetClass = request.getParameter("className");

                if (targetClass != null) {
                    Set<String> instances = ontologyReader.getInstancesOfClass(targetClass);
                    out.write(gson.toJson(instances));
                } else {
                    out.print("[]");
                }

            } else if ("individualDetails".equals(type) && individualName != null) {
                Map<String, String> individualDetails = ontologyReader.getIndividualDetails(individualName);
                out.print(gson.toJson(individualDetails));

            } else if ("individualTriples".equals(type)) {
                if (individualName != null && !individualName.isEmpty()) {
                    List<String> triples = ontologyReader.getIndividualTriples(individualName);
                    out.write(gson.toJson(triples));
                } else {
                    out.write(gson.toJson(Collections.singletonMap("error", "Invalid individual name")));
                }

            } else if ("cardinalities".equals(type) && className != null) {
                Map<String, Map<String, Integer>> cardMap = ontologyReader.getCardinalities(className);
                out.print(gson.toJson(cardMap));
            } else if ("dataPropertyValues".equals(type) && individualName != null) {
                Map<String, List<String>> dataProps = ontologyReader.getAllDataPropertiesForIndividual(individualName);
                out.print(gson.toJson(dataProps));

            } else if ("objectPropertyValues".equals(type) && individualName != null) {
                Map<String, List<String>> objectProps = ontologyReader.getAllObjectPropertiesWithReasoner(individualName);
                out.print(gson.toJson(objectProps));

            } else if ("numericProperties".equals(type) && className != null) {
                Set<String> numeric = ontologyReader.getNumericDataProperties(className);
                out.print(gson.toJson(numeric));
            } else if ("dateProperties".equals(type) && className != null) {
                Set<String> dateProperties = ontologyReader.getDateDataProperties(className);
                out.print(gson.toJson(dateProperties));
            } else if ("dateTimeStampProperties".equals(type) && className != null) {
                Set<String> props = ontologyReader.getDateTimeStampProperties(className);
                out.print(gson.toJson(props));
            } else if ("uriProperties".equals(type) && className != null) {
                Set<String> uriProperties = ontologyReader.getURIDataProperties(className);
                out.print(gson.toJson(uriProperties));
            } else if ("enumeratedProperties".equals(type) && className != null) {
                Map<String, List<String>> enums = ontologyReader.getEnumeratedDataProperties(className);
                out.print(gson.toJson(enums));
            } else if ("inverseObjectProperties".equals(type) && individualName != null) {
                Map<String, List<String>> inverseProps = ontologyReader.getInverseObjectProperties(individualName);
                out.print(gson.toJson(inverseProps));
            } else if ("classAnnotation".equals(type)) {
                String annotationName = request.getParameter("annotation");
                if (annotationName == null || annotationName.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Missing 'annotation' parameter.\"}");
                    return;
                }

                Map<String, List<String>> grouped = ontologyReader.getClassGroupingsByAnnotation(annotationName.trim());
                out.print(gson.toJson(grouped));
            } else if ("materialIntensity".equals(type)) {

                String productName = request.getParameter("individual");

                JsonObject json
                        = KpiCalculator.computeMaterialIntensity(
                                productName, ontologyReader);

                if (!json.has("error")) {

                    // Overall intensity is shown separately for context.
                    double totalMaterialInputWeight
                            = json.get("totalMaterialInputWeight")
                                    .getAsDouble();

                    double productWeight
                            = json.get("productWeight")
                                    .getAsDouble();

                    if (productWeight <= 0.0
                            || !Double.isFinite(
                                    totalMaterialInputWeight / productWeight)) {

                        throw new IllegalStateException(
                                "Invalid Material Intensity denominator.");
                    }

                    double overallMaterialIntensity
                            = totalMaterialInputWeight / productWeight;

                    json.addProperty(
                            "overallMaterialIntensity",
                            overallMaterialIntensity);

                    // Overall benchmark comparison.
                    JsonObject overallBenchmarkAssessment
                            = KpiBenchmarkService.evaluateMaterialIntensity(
                                    overallMaterialIntensity,
                                    ontologyReader);

                    json.add(
                            "overallBenchmarkAssessment",
                            overallBenchmarkAssessment);

                    // Benchmark each raw material.
                    JsonArray materialIntensities
                            = json.getAsJsonArray("materialIntensities");

                    if (materialIntensities != null) {

                        for (com.google.gson.JsonElement element
                                : materialIntensities) {

                            JsonObject materialResult
                                    = element.getAsJsonObject();

                            double intensity
                                    = materialResult.get("intensity")
                                            .getAsDouble();

                            JsonObject benchmarkAssessment
                                    = KpiBenchmarkService.evaluateMaterialIntensity(
                                            intensity,
                                            ontologyReader);

                            materialResult.add(
                                    "benchmarkAssessment",
                                    benchmarkAssessment);
                        }
                    }
                }

                out.print(json.toString());
            } else if ("productMaterialEfficiency".equals(type)) {

                String productName = request.getParameter("individual");

                JsonObject json
                        = KpiCalculator.computeProductMaterialEfficiency(
                                productName, ontologyReader);

                if (!json.has("error")) {

                    double actualValue
                            = json.get("materialEfficiency").getAsDouble();

                    JsonObject benchmarkAssessment
                            = KpiBenchmarkService.evaluateProductMaterialEfficiency(
                                    actualValue,
                                    ontologyReader);

                    json.add(
                            "benchmarkAssessment",
                            benchmarkAssessment);
                }

                out.print(json.toString());
            } else if ("individualsByClass".equals(type)) {
                String owlClassName = request.getParameter("className");  // Renamed to avoid conflict
                List<String> individuals = ontologyReader.getIndividualsByClass(owlClassName);

                JsonObject jsonResponse = new JsonObject();
                JsonArray jsonIndividuals = new JsonArray();

                for (String individual : individuals) {
                    jsonIndividuals.add(individual);
                }

                jsonResponse.add("individuals", jsonIndividuals);
                out.print(jsonResponse.toString());

            } else if ("renewableContent".equals(type)) {

                String productName = request.getParameter("individual");

                JsonObject json = KpiCalculator.computeRenewableContent(
                        productName, ontologyReader);

                if (!json.has("error")) {

                    JsonArray renewableContents
                            = json.getAsJsonArray("renewableContents");

                    if (renewableContents != null) {

                        for (com.google.gson.JsonElement element : renewableContents) {

                            JsonObject materialResult = element.getAsJsonObject();

                            double renewableFraction
                                    = materialResult.get("renewableFraction").getAsDouble();

                            double renewablePercentage
                                    = renewableFraction * 100.0;

                            JsonObject benchmarkAssessment
                                    = KpiBenchmarkService.evaluateRenewableContent(
                                            renewablePercentage,
                                            ontologyReader);

                            materialResult.add(
                                    "benchmarkAssessment",
                                    benchmarkAssessment);
                        }
                    }
                }

                out.print(json.toString());

            } else if ("recycledContent".equals(type)) {

                String productName = request.getParameter("individual");

                JsonObject json
                        = KpiCalculator.computeRecycledContent(
                                productName, ontologyReader);

                if (!json.has("error")) {

                    JsonArray recycledContents
                            = json.getAsJsonArray("recycledContents");

                    if (recycledContents != null) {

                        for (com.google.gson.JsonElement element
                                : recycledContents) {

                            JsonObject materialResult
                                    = element.getAsJsonObject();

                            double recycledFraction
                                    = materialResult.get("recycledFraction")
                                            .getAsDouble();

                            double recycledPercentage
                                    = recycledFraction * 100.0;

                            JsonObject benchmarkAssessment
                                    = KpiBenchmarkService.evaluateRecycledContent(
                                            recycledPercentage,
                                            ontologyReader);

                            materialResult.add(
                                    "benchmarkAssessment",
                                    benchmarkAssessment);
                        }
                    }
                }

                out.print(json.toString());

            } else if ("supplyTransportCarbonFootprint".equals(type)) {

                String productName = request.getParameter("individual");

                JsonObject json
                        = KpiCalculator.computeSupplyTransportCarbonFootprint(
                                productName,
                                ontologyReader);

                if (!json.has("error")) {

                    // ==========================================
                    // NEW: PRODUCT-LEVEL TRANSPORT BENCHMARK
                    // ==========================================
                    double productTotalCarbonKg
                            = json.get("totalTransportCarbonKg").getAsDouble();

                    JsonObject productBenchmarkAssessment
                            = KpiBenchmarkService.evaluateProductTransportCarbon(
                                    productTotalCarbonKg,
                                    ontologyReader);

                    json.add(
                            "productBenchmarkAssessment",
                            productBenchmarkAssessment);

                    // ==========================================
                    // EXISTING: RAW-MATERIAL TRANSPORT BENCHMARKS
                    // KEEP THIS FUNCTIONALITY
                    // ==========================================
                    JsonArray materialFootprints
                            = json.getAsJsonArray("materialFootprints");

                    if (materialFootprints != null) {

                        for (com.google.gson.JsonElement element
                                : materialFootprints) {

                            JsonObject materialResult
                                    = element.getAsJsonObject();

                            double actualCarbonKg
                                    = materialResult.get("totalCarbonKg")
                                            .getAsDouble();

                            JsonObject benchmarkAssessment
                                    = KpiBenchmarkService
                                            .evaluateSupplyTransportCarbonFootprint(
                                                    actualCarbonKg,
                                                    ontologyReader);

                            materialResult.add(
                                    "benchmarkAssessment",
                                    benchmarkAssessment);
                        }
                    }
                }

                out.print(json.toString());

            } else if ("productRenewableMaterial".equals(type)) {

                // ==========================================
                // 1. GET SELECTED MANUFACTURED PRODUCT
                // ==========================================
                String productName
                        = request.getParameter("individual");

                // ==========================================
                // 2. REUSE EXISTING PRODUCT KPI CALCULATION
                // ==========================================
                JsonObject json
                        = KpiCalculator.computeProductRenewableMaterial(
                                productName,
                                ontologyReader);

                if (!json.has("error")) {

                    // ==========================================
                    // 3. NEW: PRODUCT RENEWABLE BENCHMARK
                    // ==========================================
                    double actualPercentage
                            = json.get("productRenewablePercentage")
                                    .getAsDouble();

                    JsonObject benchmarkAssessment
                            = KpiBenchmarkService.evaluateProductRenewableMaterial(
                                    actualPercentage,
                                    ontologyReader);

                    json.add(
                            "benchmarkAssessment",
                            benchmarkAssessment);
                }

                // ==========================================
                // 4. RETURN CALCULATION + BENCHMARK
                // ==========================================
                out.print(json.toString());
            } else if ("productRecycledMaterial".equals(type)) {

                // ==========================================
                // 1. GET SELECTED MANUFACTURED PRODUCT
                // ==========================================
                String productName
                        = request.getParameter("individual");

                // ==========================================
                // 2. REUSE EXISTING PRODUCT KPI CALCULATION
                // ==========================================
                JsonObject json
                        = KpiCalculator.computeProductRecycledMaterial(
                                productName,
                                ontologyReader);

                if (!json.has("error")) {

                    // ==========================================
                    // 3. NEW: PRODUCT-LEVEL RECYCLED BENCHMARK
                    // ==========================================
                    double actualPercentage
                            = json.get("productRecycledPercentage")
                                    .getAsDouble();

                    JsonObject benchmarkAssessment
                            = KpiBenchmarkService.evaluateProductRecycledMaterial(
                                    actualPercentage,
                                    ontologyReader);

                    json.add(
                            "benchmarkAssessment",
                            benchmarkAssessment);
                }

                // ==========================================
                // 4. RETURN CALCULATION + BENCHMARK
                // ==========================================
                out.print(json.toString());

            } else if ("corporateEnergyCarbonFootprint".equals(type)) {
                String processName = request.getParameter("individual");

                JsonObject json
                        = KpiCalculator.computeCorporateEnergyCarbonFootprint(
                                processName, ontologyReader);

                if (!json.has("error")) {
                    json.add("carbonIntensityBenchmarkAssessment",
                            KpiBenchmarkService.evaluateCorporateEnergyCarbonIntensity(
                                    json.get("totalEnergyCarbonPerKg").getAsDouble(), ontologyReader));
                    json.add("renewableElectricityBenchmarkAssessment",
                            KpiBenchmarkService.evaluateCorporateRenewableElectricity(
                                    json.get("renewableElectricityPercentage").getAsDouble(), ontologyReader));
                }
                out.print(json.toString());
            } else if ("productEnergyCarbonFootprint".equals(type)) {

                String productName
                        = request.getParameter("individual");

                String processName
                        = request.getParameter("processSustainability");

                if (productName == null || productName.trim().isEmpty()
                        || processName == null || processName.trim().isEmpty()) {

                    JsonObject error = new JsonObject();
                    error.addProperty(
                            "error",
                            "Both individual and processSustainability "
                            + "parameters are required.");

                    out.print(error.toString());

                } else {

                    JsonObject json
                            = KpiCalculator.computeProductEnergyCarbonFootprint(
                                    productName,
                                    processName,
                                    ontologyReader);

                    if (!json.has("error")) {
                        json.add("benchmarkAssessment",
                                KpiBenchmarkService.evaluateProductEnergyCarbonFootprint(
                                        json.get("totalAllocatedCarbonKg").getAsDouble(), ontologyReader));
                    }
                    out.print(json.toString());
                }
            } else if ("productEmbodiedCarbon".equals(type)) {

                String productName = request.getParameter("individual");
                String processName = request.getParameter("processSustainability");

                if (productName == null || productName.trim().isEmpty()
                        || processName == null || processName.trim().isEmpty()) {

                    JsonObject error = new JsonObject();

                    error.addProperty(
                            "error",
                            "Both individual and processSustainability parameters are required.");

                    out.print(error.toString());

                } else {

                    JsonObject json = KpiCalculator.computeProductEmbodiedCarbon(
                            productName,
                            processName,
                            ontologyReader);

                    if (!json.has("error")) {
                        json.add("benchmarkAssessment",
                                KpiBenchmarkService.evaluateProductEmbodiedCarbon(
                                        json.get("embodiedCarbonPerKg").getAsDouble(), ontologyReader));
                    }
                    out.print(json.toString());
                }

            } else if ("corporateWaterKpis".equals(type)) {

                String processName = request.getParameter("individual");

                if (processName == null || processName.trim().isEmpty()) {

                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                    JsonObject error = new JsonObject();
                    error.addProperty("error",
                            "Missing required parameter: individual");

                    out.print(gson.toJson(error));

                } else {

                    JsonObject json = KpiCalculator.computeCorporateWaterKpis(
                            processName.trim(), ontologyReader);

                    if (!json.has("error")) {
                        json.add("corporateWaterUseBenchmarkAssessment",
                                KpiBenchmarkService.evaluateCorporateWaterUse(
                                        json.get("waterUsePerKg").getAsDouble(),
                                        ontologyReader));
                        json.add("corporateWastewaterBenchmarkAssessment",
                                KpiBenchmarkService.evaluateCorporateWastewater(
                                        json.get("wastewaterPerKg").getAsDouble(),
                                        ontologyReader));
                    }

                    if (json.has("error")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }

                    out.print(gson.toJson(json));
                }

            } else if ("productWaterKpis".equals(type)) {

                String productName = request.getParameter("individual");
                String processName
                        = request.getParameter("processSustainability");

                if (productName == null || productName.trim().isEmpty()
                        || processName == null || processName.trim().isEmpty()) {

                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                    JsonObject error = new JsonObject();
                    error.addProperty("error",
                            "Both individual and processSustainability "
                            + "parameters are required.");

                    out.print(gson.toJson(error));

                } else {

                    JsonObject json = KpiCalculator.computeProductWaterKpis(
                            productName.trim(),
                            processName.trim(),
                            ontologyReader);

                    if (!json.has("error")) {
                        json.add("productWaterUseBenchmarkAssessment",
                                KpiBenchmarkService.evaluateProductWaterUse(
                                        json.get("allocatedWaterUsageM3").getAsDouble(),
                                        ontologyReader));
                        json.add("productWastewaterBenchmarkAssessment",
                                KpiBenchmarkService.evaluateProductWastewater(
                                        json.get("allocatedWastewaterM3").getAsDouble(),
                                        ontologyReader));
                    }

                    if (json.has("error")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }

                    out.print(gson.toJson(json));
                }

            } else if ("corporateWasteKpis".equals(type)) {

                String processName = request.getParameter("individual");

                if (processName == null || processName.trim().isEmpty()) {

                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                    JsonObject error = new JsonObject();
                    error.addProperty("error",
                            "Missing required parameter: individual");

                    out.print(gson.toJson(error));

                } else {

                    JsonObject json = KpiCalculator.computeCorporateWasteKpis(
                            processName.trim(), ontologyReader);

                    if (!json.has("error")) {
                        json.add("corporateHazardousWasteBenchmarkAssessment",
                                KpiBenchmarkService.evaluateCorporateHazardousWaste(
                                        json.get("hazardousWastePerKg").getAsDouble(),
                                        ontologyReader));
                        json.add("corporateNonHazardousWasteBenchmarkAssessment",
                                KpiBenchmarkService.evaluateCorporateNonHazardousWaste(
                                        json.get("nonHazardousWastePerKg").getAsDouble(),
                                        ontologyReader));
                    }

                    if (json.has("error")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }

                    out.print(gson.toJson(json));
                }

            } else if ("productWasteKpis".equals(type)) {

                String productName
                        = request.getParameter("individual");

                String processName
                        = request.getParameter("processSustainability");

                if (productName == null || productName.trim().isEmpty()
                        || processName == null || processName.trim().isEmpty()) {

                    response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST);

                    JsonObject error = new JsonObject();

                    error.addProperty(
                            "error",
                            "Both individual and processSustainability "
                            + "parameters are required.");

                    out.print(gson.toJson(error));

                } else {

                    JsonObject json
                            = KpiCalculator.computeProductWasteKpis(
                                    productName.trim(),
                                    processName.trim(),
                                    ontologyReader);

                    if (!json.has("error")) {
                        json.add("productHazardousWasteBenchmarkAssessment",
                                KpiBenchmarkService.evaluateProductHazardousWaste(
                                        json.get("allocatedHazardousWasteKg").getAsDouble(),
                                        ontologyReader));
                        json.add("productNonHazardousWasteBenchmarkAssessment",
                                KpiBenchmarkService.evaluateProductNonHazardousWaste(
                                        json.get("allocatedNonHazardousWasteKg").getAsDouble(),
                                        ontologyReader));
                    }

                    if (json.has("error")) {
                        response.setStatus(
                                HttpServletResponse.SC_BAD_REQUEST);
                    }

                    out.print(gson.toJson(json));
                }
            } else {
                Set<String> classes = ontologyReader.getOntologyClasses();
                out.print(gson.toJson(classes));
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print(gson.toJson(Collections.singletonMap("error", "Exception occurred: " + e.getMessage())));
        }

        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        JsonObject json = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        String action = json.get("action").getAsString();

        OntologyReader ontologyReader = OntologyReader.getInstance();  // ✅ Use fresh instance here too

        if ("updateIndividual".equals(action)) {
            String className = json.get("className").getAsString(); // ✅ new line
            String individualName = json.get("individual").getAsString();

            Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> dataProps = gson.fromJson(json.get("dataProperties"), mapType);
            Map<String, Object> objectProps = gson.fromJson(json.get("objectProperties"), mapType);

            // ✅ Updated method call
            boolean success = ontologyReader.updateIndividual(className, individualName, dataProps, objectProps);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("status", success ? "success" : "error");
            responseJson.addProperty("message", success ? "Individual updated successfully!" : "Failed to update individual.");

            out.print(responseJson.toString());
        } else if ("compareIndividuals".equals(action)) {
            String className = json.get("className").getAsString();

            Type listType = new TypeToken<List<String>>() {
            }.getType();
            List<String> individuals = new Gson().fromJson(json.get("individuals"), listType);

            Map<String, Map<String, Map<String, List<String>>>> comparisonData = OntologyReader.getInstance().getIndividualsPropertiesForComparison(className, individuals);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("status", "success");
            responseJson.add("comparisonData", gson.toJsonTree(comparisonData));
            out.print(responseJson.toString());
        } else if ("classNumericStats".equals(action)) {
            String className = json.get("className").getAsString();
            Map<String, List<Double>> allVals = ontologyReader.getAllNumericPropertyValues(className);
            out.print(gson.toJson(allVals));

        } else if ("saveMaterialIntensityEvaluation".equals(action)) {

            JsonArray savedResults = new JsonArray();

            try {

                // 1. Validate the manufactured product.
                if (!json.has("individual")
                        || json.get("individual").isJsonNull()) {

                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                String productName
                        = json.get("individual").getAsString().trim();

                if (productName.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                // 2. Recalculate Material Intensity on the server.
                JsonObject calculation
                        = KpiCalculator.computeMaterialIntensity(
                                productName, ontologyReader);

                if (calculation == null || calculation.has("error")) {

                    throw new IllegalStateException(
                            "Material Intensity calculation failed: "
                            + calculation);
                }

                JsonArray materialIntensities
                        = calculation.getAsJsonArray("materialIntensities");

                if (materialIntensities == null
                        || materialIntensities.size() == 0) {

                    throw new IllegalStateException(
                            "No raw-material Material Intensity results found.");
                }

                // 3. Validate all material results and benchmarks
                // before creating any ontology individuals.
                for (com.google.gson.JsonElement element
                        : materialIntensities) {

                    JsonObject materialResult
                            = element.getAsJsonObject();

                    double intensity
                            = materialResult.get("intensity").getAsDouble();

                    if (!Double.isFinite(intensity)
                            || intensity < 0.0) {

                        throw new IllegalStateException(
                                "Invalid Material Intensity value.");
                    }

                    JsonObject benchmark
                            = KpiBenchmarkService.evaluateMaterialIntensity(
                                    intensity, ontologyReader);

                    if (!benchmark.has("status")
                            || !"success".equals(
                                    benchmark.get("status").getAsString())) {

                        throw new IllegalStateException(
                                "Material Intensity benchmark failed: "
                                + benchmark);
                    }

                    if (!benchmark.has("deviationPercentage")) {

                        throw new IllegalStateException(
                                "Benchmark deviation percentage is missing. "
                                + "Check the benchmark value.");
                    }
                }

                // 4. Persist an evaluation and an assessment
                // for each raw material.
                KpiEvaluationService evaluationService
                        = new KpiEvaluationService();

                for (com.google.gson.JsonElement element
                        : materialIntensities) {

                    JsonObject materialResult
                            = element.getAsJsonObject();

                    String materialName
                            = materialResult.get("material").getAsString();

                    double intensity
                            = materialResult.get("intensity").getAsDouble();

                    JsonObject benchmarkResult
                            = KpiBenchmarkService.evaluateMaterialIntensity(
                                    intensity, ontologyReader);

                    String provenance
                            = "KpiCalculator.computeMaterialIntensity"
                            + "; calculationBasis="
                            + calculation.get("calculationBasis")
                                    .getAsString();

                    // Save KPIEvaluation.
                    JsonObject savedEvaluation
                            = evaluationService.saveMaterialIntensity(
                                    materialName,
                                    productName,
                                    intensity,
                                    provenance);

                    if (savedEvaluation == null
                            || !savedEvaluation.has("status")
                            || !"success".equals(
                                    savedEvaluation.get("status")
                                            .getAsString())) {

                        throw new IllegalStateException(
                                "Could not save Material Intensity evaluation "
                                + "for " + materialName);
                    }

                    String evaluationName
                            = savedEvaluation.get("evaluationIndividual")
                                    .getAsString();

                    if (evaluationName.trim().isEmpty()) {

                        throw new IllegalStateException(
                                "Missing saved evaluation individual.");
                    }

                    // Save BenchmarkAssessment linked to KPIEvaluation.
                    String assessmentName
                            = ontologyReader.createBenchmarkAssessment(
                                    evaluationName,
                                    benchmarkResult.get("benchmark")
                                            .getAsString(),
                                    benchmarkResult.get("meetsBenchmark")
                                            .getAsBoolean(),
                                    benchmarkResult.get("absoluteDeviation")
                                            .getAsDouble(),
                                    benchmarkResult.get("deviationPercentage")
                                            .getAsDouble(),
                                    benchmarkResult.get("assessment")
                                            .getAsString(),
                                    "Material Intensity benchmark assessment"
                                    + "; material=" + materialName
                                    + "; product=" + productName
                                    + "; actual=" + intensity + " kg/kg"
                            );

                    // Collect persistence results.
                    JsonObject savedMaterial = new JsonObject();

                    savedMaterial.addProperty(
                            "material", materialName);

                    savedMaterial.addProperty(
                            "actualValue", intensity);

                    savedMaterial.addProperty(
                            "evaluationIndividual", evaluationName);

                    savedMaterial.addProperty(
                            "benchmarkAssessmentIndividual",
                            assessmentName);

                    savedMaterial.add(
                            "evaluation", savedEvaluation);

                    savedMaterial.add(
                            "benchmarkAssessment", benchmarkResult);

                    savedResults.add(savedMaterial);
                }

                // 5. Return all saved evaluations and assessments.
                JsonObject responseJson = new JsonObject();

                responseJson.addProperty("status", "success");
                responseJson.addProperty("product", productName);
                responseJson.addProperty("count", savedResults.size());
                responseJson.add(
                        "materialEvaluations", savedResults);

                responseJson.add("calculation", calculation);

                out.print(responseJson.toString());

            } catch (Exception ex) {

                ex.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                JsonObject error = new JsonObject();

                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());
                error.add("savedResults", savedResults);

                out.print(error.toString());
            }

        } else if ("saveRenewableContentEvaluation".equals(action)) {

            try {
                // 1. Validate product.
                if (!json.has("individual")
                        || json.get("individual").isJsonNull()) {
                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                String productName
                        = json.get("individual").getAsString().trim();

                if (productName.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                // 2. Calculate Renewable Content.
                JsonObject calculation
                        = KpiCalculator.computeRenewableContent(
                                productName, ontologyReader);

                if (calculation == null || calculation.has("error")) {
                    throw new IllegalStateException(
                            "Renewable Content calculation failed: "
                            + (calculation == null
                                    ? "null response"
                                    : calculation.toString()));
                }

                JsonArray renewableContents
                        = calculation.getAsJsonArray("renewableContents");

                if (renewableContents == null
                        || renewableContents.size() == 0) {
                    throw new IllegalStateException(
                            "No raw-material Renewable Content results found.");
                }

                KpiEvaluationService evaluationService
                        = new KpiEvaluationService();

                JsonArray savedResults = new JsonArray();

                // 3. Process every raw material.
                for (com.google.gson.JsonElement element
                        : renewableContents) {

                    JsonObject materialResult
                            = element.getAsJsonObject();

                    String materialName
                            = materialResult.get("material").getAsString();

                    double renewableFraction
                            = materialResult.get("renewableFraction")
                                    .getAsDouble();

                    if (!Double.isFinite(renewableFraction)
                            || renewableFraction < 0.0
                            || renewableFraction > 1.0) {
                        throw new IllegalStateException(
                                "Invalid renewableFraction for "
                                + materialName);
                    }

                    double renewablePercentage
                            = renewableFraction * 100.0;

                    String sustainabilityName
                            = materialResult.has("materialSustainability")
                            && !materialResult.get("materialSustainability")
                                    .isJsonNull()
                                    ? materialResult.get("materialSustainability")
                                            .getAsString()
                                    : "not specified";

                    String provenance
                            = "KpiCalculator.computeRenewableContent"
                            + "; materialSustainability="
                            + sustainabilityName;

                    // 4. Evaluate benchmark before saving.
                    JsonObject benchmarkResult
                            = KpiBenchmarkService.evaluateRenewableContent(
                                    renewablePercentage,
                                    ontologyReader);

                    if (benchmarkResult == null
                            || !benchmarkResult.has("status")
                            || !"success".equals(
                                    benchmarkResult.get("status")
                                            .getAsString())) {
                        throw new IllegalStateException(
                                "Benchmark evaluation failed for "
                                + materialName);
                    }

                    // 5. Save KPIEvaluation.
                    JsonObject savedEvaluation
                            = evaluationService.saveRenewableContent(
                                    materialName,
                                    productName,
                                    renewablePercentage,
                                    provenance);

                    if (savedEvaluation == null
                            || !savedEvaluation.has("status")
                            || !"success".equals(
                                    savedEvaluation.get("status")
                                            .getAsString())) {
                        throw new IllegalStateException(
                                "Could not save KPIEvaluation for "
                                + materialName + ": "
                                + savedEvaluation);
                    }

                    // Read the actual saved individual name.
                    // This key must match the existing service response.
                    String evaluationName = null;

                    for (String key : new String[]{
                        "evaluationIndividual",
                        "evaluationName",
                        "individualName",
                        "individual"
                    }) {
                        if (savedEvaluation.has(key)
                                && !savedEvaluation.get(key).isJsonNull()) {
                            evaluationName
                                    = savedEvaluation.get(key).getAsString();
                            break;
                        }
                    }

                    if (evaluationName == null
                            || evaluationName.trim().isEmpty()) {
                        throw new IllegalStateException(
                                "Saved KPIEvaluation name was not returned "
                                + "for " + materialName
                                + ". Response: " + savedEvaluation);
                    }

                    // 6. Save linked BenchmarkAssessment.
                    String assessmentName
                            = ontologyReader.createBenchmarkAssessment(
                                    evaluationName,
                                    benchmarkResult.get("benchmark")
                                            .getAsString(),
                                    benchmarkResult.get("meetsBenchmark")
                                            .getAsBoolean(),
                                    benchmarkResult.get("absoluteDeviation")
                                            .getAsDouble(),
                                    benchmarkResult.get("deviationPercentage")
                                            .getAsDouble(),
                                    benchmarkResult.get("assessment")
                                            .getAsString(),
                                    "Renewable Content benchmark assessment"
                                    + "; material=" + materialName
                                    + "; product=" + productName
                                    + "; actual="
                                    + renewablePercentage + "%"
                                    + "; benchmark="
                                    + benchmarkResult.get("benchmarkValue")
                                            .getAsDouble() + "%"
                            );

                    // 7. Return both saved records.
                    JsonObject materialSaved = new JsonObject();

                    materialSaved.addProperty(
                            "material", materialName);

                    materialSaved.addProperty(
                            "actualPercentage", renewablePercentage);

                    materialSaved.addProperty(
                            "evaluationIndividual", evaluationName);

                    materialSaved.addProperty(
                            "benchmarkAssessmentIndividual",
                            assessmentName);

                    materialSaved.add(
                            "evaluation", savedEvaluation);

                    materialSaved.add(
                            "benchmarkAssessment", benchmarkResult);

                    savedResults.add(materialSaved);
                }

                // 8. Return completed result.
                JsonObject responseJson = new JsonObject();

                responseJson.addProperty("status", "success");
                responseJson.addProperty("product", productName);
                responseJson.add(
                        "materialEvaluations", savedResults);
                responseJson.addProperty(
                        "count", savedResults.size());

                out.print(responseJson.toString());

            } catch (Exception ex) {

                ex.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                JsonObject error = new JsonObject();
                error.addProperty("status", "error");
                error.addProperty(
                        "message", ex.getMessage());

                out.print(error.toString());
            }
        } else if ("saveRecycledContentEvaluation".equals(action)) {

            JsonArray savedResults = new JsonArray();

            try {
                if (!json.has("individual")
                        || json.get("individual").isJsonNull()) {
                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                String productName
                        = json.get("individual").getAsString().trim();

                if (productName.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                JsonObject calculation
                        = KpiCalculator.computeRecycledContent(
                                productName, ontologyReader);

                if (calculation == null || calculation.has("error")) {
                    throw new IllegalStateException(
                            "Recycled Content calculation failed: "
                            + calculation);
                }

                JsonArray recycledContents
                        = calculation.getAsJsonArray("recycledContents");

                if (recycledContents == null
                        || recycledContents.size() == 0) {
                    throw new IllegalStateException(
                            "No raw-material Recycled Content results found.");
                }

                KpiEvaluationService evaluationService
                        = new KpiEvaluationService();

                // Validate every benchmark before writing any evaluations.
                for (com.google.gson.JsonElement element
                        : recycledContents) {

                    JsonObject materialResult
                            = element.getAsJsonObject();

                    double fraction
                            = materialResult.get("recycledFraction")
                                    .getAsDouble();

                    if (!Double.isFinite(fraction)
                            || fraction < 0.0 || fraction > 1.0) {
                        throw new IllegalStateException(
                                "Invalid recycledFraction.");
                    }

                    JsonObject benchmark
                            = KpiBenchmarkService.evaluateRecycledContent(
                                    fraction * 100.0, ontologyReader);

                    if (!benchmark.has("status")
                            || !"success".equals(
                                    benchmark.get("status").getAsString())) {
                        throw new IllegalStateException(
                                "Benchmark evaluation failed: " + benchmark);
                    }
                }

                for (com.google.gson.JsonElement element
                        : recycledContents) {

                    JsonObject materialResult
                            = element.getAsJsonObject();

                    String materialName
                            = materialResult.get("material").getAsString();

                    double recycledPercentage
                            = materialResult.get("recycledFraction")
                                    .getAsDouble() * 100.0;

                    String sustainabilityName
                            = materialResult.has("materialSustainability")
                            && !materialResult.get("materialSustainability")
                                    .isJsonNull()
                                    ? materialResult.get("materialSustainability")
                                            .getAsString()
                                    : "not specified";

                    String provenance
                            = "KpiCalculator.computeRecycledContent"
                            + "; materialSustainability="
                            + sustainabilityName;

                    JsonObject benchmarkResult
                            = KpiBenchmarkService.evaluateRecycledContent(
                                    recycledPercentage, ontologyReader);

                    JsonObject savedEvaluation
                            = evaluationService.saveRecycledContent(
                                    materialName,
                                    productName,
                                    recycledPercentage,
                                    provenance);

                    if (savedEvaluation == null
                            || !savedEvaluation.has("status")
                            || !"success".equals(
                                    savedEvaluation.get("status")
                                            .getAsString())) {
                        throw new IllegalStateException(
                                "Could not save KPIEvaluation for "
                                + materialName + ": " + savedEvaluation);
                    }

                    String evaluationName
                            = savedEvaluation.get("evaluationIndividual")
                                    .getAsString();

                    if (evaluationName.trim().isEmpty()) {
                        throw new IllegalStateException(
                                "Missing saved evaluation individual name.");
                    }

                    double deviationPercentage = 0.0;

                    if (benchmarkResult.has("deviationPercentage")) {
                        deviationPercentage
                                = benchmarkResult.get("deviationPercentage")
                                        .getAsDouble();
                    }

                    String assessmentName
                            = ontologyReader.createBenchmarkAssessment(
                                    evaluationName,
                                    benchmarkResult.get("benchmark")
                                            .getAsString(),
                                    benchmarkResult.get("meetsBenchmark")
                                            .getAsBoolean(),
                                    benchmarkResult.get("absoluteDeviation")
                                            .getAsDouble(),
                                    deviationPercentage,
                                    benchmarkResult.get("assessment")
                                            .getAsString(),
                                    "Recycled Content benchmark assessment"
                                    + "; material=" + materialName
                                    + "; product=" + productName
                                    + "; actual="
                                    + recycledPercentage + "%"
                                    + "; benchmark="
                                    + benchmarkResult.get("benchmarkValue")
                                            .getAsDouble() + "%"
                            );

                    JsonObject materialSaved = new JsonObject();

                    materialSaved.addProperty(
                            "material", materialName);
                    materialSaved.addProperty(
                            "actualPercentage", recycledPercentage);
                    materialSaved.addProperty(
                            "evaluationIndividual", evaluationName);
                    materialSaved.addProperty(
                            "benchmarkAssessmentIndividual",
                            assessmentName);
                    materialSaved.add(
                            "evaluation", savedEvaluation);
                    materialSaved.add(
                            "benchmarkAssessment", benchmarkResult);

                    savedResults.add(materialSaved);
                }

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("status", "success");
                responseJson.addProperty("product", productName);
                responseJson.add("materialEvaluations", savedResults);
                responseJson.addProperty("count", savedResults.size());

                out.print(responseJson.toString());

            } catch (Exception ex) {

                ex.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                JsonObject error = new JsonObject();
                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());
                error.add("savedResults", savedResults);

                out.print(error.toString());
            }

        } else if ("saveSupplyTransportCarbonFootprintEvaluation"
                .equals(action)) {

            JsonArray savedResults = new JsonArray();

            try {

                // 1. Validate manufactured product.
                if (!json.has("individual")
                        || json.get("individual").isJsonNull()) {

                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                String productName
                        = json.get("individual").getAsString().trim();

                if (productName.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                // 2. Recalculate transport footprints on the server.
                JsonObject calculation
                        = KpiCalculator.computeSupplyTransportCarbonFootprint(
                                productName, ontologyReader);

                if (calculation == null || calculation.has("error")) {

                    throw new IllegalStateException(
                            "Transport Carbon Footprint calculation failed: "
                            + calculation);
                }

                JsonArray materialFootprints
                        = calculation.getAsJsonArray("materialFootprints");

                if (materialFootprints == null
                        || materialFootprints.size() == 0) {

                    throw new IllegalStateException(
                            "No raw-material transport footprints found.");
                }

                // 3. Validate all materials and benchmarks
                // before creating any ontology individuals.
                for (com.google.gson.JsonElement element
                        : materialFootprints) {

                    JsonObject materialResult
                            = element.getAsJsonObject();

                    String materialName
                            = materialResult.get("material").getAsString();

                    double actualCarbonKg
                            = materialResult.get("totalCarbonKg")
                                    .getAsDouble();

                    if (!Double.isFinite(actualCarbonKg)
                            || actualCarbonKg < 0.0) {

                        throw new IllegalStateException(
                                "Invalid transport footprint for "
                                + materialName);
                    }

                    JsonObject benchmark
                            = KpiBenchmarkService
                                    .evaluateSupplyTransportCarbonFootprint(
                                            actualCarbonKg,
                                            ontologyReader);

                    if (!benchmark.has("status")
                            || !"success".equals(
                                    benchmark.get("status").getAsString())) {

                        throw new IllegalStateException(
                                "Transport benchmark failed for "
                                + materialName + ": " + benchmark);
                    }
                }

                // 4. Save each material's evaluation and assessment.
                KpiEvaluationService evaluationService
                        = new KpiEvaluationService();

                for (com.google.gson.JsonElement element
                        : materialFootprints) {

                    JsonObject materialResult
                            = element.getAsJsonObject();

                    String materialName
                            = materialResult.get("material").getAsString();

                    double actualCarbonKg
                            = materialResult.get("totalCarbonKg")
                                    .getAsDouble();

                    double roadCarbonKg
                            = materialResult.get("roadCarbonKg")
                                    .getAsDouble();

                    double seaCarbonKg
                            = materialResult.get("seaCarbonKg")
                                    .getAsDouble();

                    JsonObject benchmarkResult
                            = KpiBenchmarkService
                                    .evaluateSupplyTransportCarbonFootprint(
                                            actualCarbonKg,
                                            ontologyReader);

                    // Preserve the calculation context.
                    String provenance
                            = "KpiCalculator.computeSupplyTransportCarbonFootprint"
                            + "; calculationBasis="
                            + calculation.get("calculationBasis")
                                    .getAsString()
                            + "; roadCarbonKg=" + roadCarbonKg
                            + "; seaCarbonKg=" + seaCarbonKg
                            + "; shipments="
                            + materialResult.getAsJsonArray("shipments")
                                    .toString();

                    // Save KPIEvaluation.
                    JsonObject savedEvaluation
                            = evaluationService
                                    .saveSupplyTransportCarbonFootprint(
                                            materialName,
                                            productName,
                                            actualCarbonKg,
                                            provenance);

                    if (savedEvaluation == null
                            || !savedEvaluation.has("status")
                            || !"success".equals(
                                    savedEvaluation.get("status")
                                            .getAsString())) {

                        throw new IllegalStateException(
                                "Could not save transport KPIEvaluation "
                                + "for " + materialName);
                    }

                    String evaluationName
                            = savedEvaluation.get("evaluationIndividual")
                                    .getAsString();

                    if (evaluationName.trim().isEmpty()) {

                        throw new IllegalStateException(
                                "Missing saved evaluation individual for "
                                + materialName);
                    }

                    // The generic benchmark service omits this field
                    // when the configured benchmark value is zero.
                    double deviationPercentage
                            = benchmarkResult.has("deviationPercentage")
                            ? benchmarkResult.get("deviationPercentage")
                                    .getAsDouble()
                            : 0.0;

                    // Save BenchmarkAssessment linked to KPIEvaluation.
                    String assessmentName
                            = ontologyReader.createBenchmarkAssessment(
                                    evaluationName,
                                    benchmarkResult.get("benchmark")
                                            .getAsString(),
                                    benchmarkResult.get("meetsBenchmark")
                                            .getAsBoolean(),
                                    benchmarkResult.get("absoluteDeviation")
                                            .getAsDouble(),
                                    deviationPercentage,
                                    benchmarkResult.get("assessment")
                                            .getAsString(),
                                    "Supply Transport Carbon Footprint"
                                    + "; material=" + materialName
                                    + "; product=" + productName
                                    + "; actual=" + actualCarbonKg
                                    + " kg CO2eq"
                                    + "; road=" + roadCarbonKg
                                    + "; sea=" + seaCarbonKg);

                    // Collect saved ontology individual names.
                    JsonObject savedMaterial = new JsonObject();

                    savedMaterial.addProperty(
                            "material", materialName);

                    savedMaterial.addProperty(
                            "actualValue", actualCarbonKg);

                    savedMaterial.addProperty(
                            "unit", "kg CO2eq");

                    savedMaterial.addProperty(
                            "evaluationIndividual", evaluationName);

                    savedMaterial.addProperty(
                            "benchmarkAssessmentIndividual",
                            assessmentName);

                    savedMaterial.add(
                            "evaluation", savedEvaluation);

                    savedMaterial.add(
                            "benchmarkAssessment", benchmarkResult);

                    savedResults.add(savedMaterial);
                }

                // 5. Return the persistence results.
                JsonObject responseJson = new JsonObject();

                responseJson.addProperty("status", "success");
                responseJson.addProperty("product", productName);
                responseJson.addProperty("count", savedResults.size());

                responseJson.add(
                        "materialEvaluations", savedResults);

                responseJson.add(
                        "calculation", calculation);

                out.print(responseJson.toString());

            } catch (Exception ex) {

                ex.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                JsonObject error = new JsonObject();

                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());
                error.add("savedResults", savedResults);

                out.print(error.toString());
            }

        } else if ("saveProductMaterialEfficiencyEvaluation".equals(action)) {

            try {
                // 1. Validate the selected product.
                if (!json.has("individual")
                        || json.get("individual").isJsonNull()) {
                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                String productName
                        = json.get("individual").getAsString().trim();

                if (productName.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                // 2. Recalculate on the server.
                JsonObject calculation
                        = KpiCalculator.computeProductMaterialEfficiency(
                                productName, ontologyReader);

                if (calculation == null || calculation.has("error")) {
                    throw new IllegalStateException(
                            "Product Material Efficiency calculation failed: "
                            + calculation);
                }

                double actualValue
                        = calculation.get("materialEfficiency").getAsDouble();

                if (!Double.isFinite(actualValue) || actualValue < 0.0) {
                    throw new IllegalStateException(
                            "Invalid Product Material Efficiency result.");
                }

                // 3. Evaluate the product-level benchmark.
                JsonObject benchmark
                        = KpiBenchmarkService.evaluateProductMaterialEfficiency(
                                actualValue, ontologyReader);

                if (!"success".equals(
                        benchmark.get("status").getAsString())) {
                    throw new IllegalStateException(
                            "Product Material Efficiency benchmark failed: "
                            + benchmark);
                }

                // 4. Record calculation provenance.
                String provenance
                        = "KpiCalculator.computeProductMaterialEfficiency"
                        + "; calculationBasis="
                        + calculation.get("calculationBasis").getAsString()
                        + "; totalMaterialInputWeight="
                        + calculation.get("totalMaterialInputWeight").getAsDouble()
                        + "; productWeight="
                        + calculation.get("productWeight").getAsDouble()
                        + "; manufacturedQuantity="
                        + calculation.get("manufacturedQuantity").getAsDouble();

                // 5. Save KPIEvaluation.
                KpiEvaluationService evaluationService
                        = new KpiEvaluationService();

                JsonObject savedEvaluation
                        = evaluationService.saveProductMaterialEfficiency(
                                productName, actualValue, provenance);

                if (!"success".equals(
                        savedEvaluation.get("status").getAsString())) {
                    throw new IllegalStateException(
                            "Product Material Efficiency evaluation save failed.");
                }

                String evaluationName
                        = savedEvaluation.get("evaluationIndividual").getAsString();

                // 6. Save the linked BenchmarkAssessment.
                double deviationPercentage
                        = benchmark.has("deviationPercentage")
                        && !benchmark.get("deviationPercentage").isJsonNull()
                        ? benchmark.get("deviationPercentage").getAsDouble()
                        : 0.0;

                String assessmentName
                        = ontologyReader.createBenchmarkAssessment(
                                evaluationName,
                                "ProductMaterialEfficiencyBenchmark",
                                benchmark.get("meetsBenchmark").getAsBoolean(),
                                benchmark.get("absoluteDeviation").getAsDouble(),
                                deviationPercentage,
                                benchmark.get("assessment").getAsString(),
                                "Product Material Efficiency compared against "
                                + "the product-level benchmark configured "
                                + "in the ontology.");

                // 7. Return the response expected by JavaScript.
                JsonObject result = new JsonObject();

                result.addProperty("status", "success");
                result.addProperty("product", productName);
                result.addProperty("computedValue", actualValue);
                result.addProperty("unit", "kg/kg");
                result.addProperty("evaluationIndividual", evaluationName);
                result.addProperty(
                        "benchmarkAssessmentIndividual", assessmentName);

                result.add("benchmarkAssessment", benchmark);
                result.add("calculation", calculation);

                out.print(result.toString());

            } catch (Exception ex) {
                ex.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                JsonObject error = new JsonObject();
                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());
                error.addProperty(
                        "warning",
                        "A KPIEvaluation may have been saved before "
                        + "a later operation failed. Check the ontology "
                        + "before retrying.");

                out.print(error.toString());
            }

        } else if ("saveProductTransportCarbonEvaluation".equals(action)) {

            try {

                // ==========================================
                // 1. VALIDATE MANUFACTURED PRODUCT
                // ==========================================
                if (!json.has("individual")
                        || json.get("individual").isJsonNull()) {

                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                String productName
                        = json.get("individual").getAsString().trim();

                if (productName.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                // ==========================================
                // 2. RECALCULATE PRODUCT TRANSPORT CARBON
                // ==========================================
                // Reuse the existing calculation.
                // Never trust a KPI value submitted by the frontend.
                JsonObject calculation
                        = KpiCalculator.computeSupplyTransportCarbonFootprint(
                                productName,
                                ontologyReader);

                if (calculation == null || calculation.has("error")) {

                    throw new IllegalStateException(
                            "Product Total Transport Carbon calculation failed: "
                            + calculation);
                }

                double actualValue
                        = calculation.get("totalTransportCarbonKg")
                                .getAsDouble();

                if (!Double.isFinite(actualValue)
                        || actualValue < 0.0) {

                    throw new IllegalStateException(
                            "Invalid Product Total Transport Carbon result.");
                }

                // ==========================================
                // 3. EVALUATE PRODUCT-LEVEL BENCHMARK
                // ==========================================
                JsonObject benchmark
                        = KpiBenchmarkService.evaluateProductTransportCarbon(
                                actualValue,
                                ontologyReader);

                if (!benchmark.has("status")
                        || !"success".equals(
                                benchmark.get("status").getAsString())) {

                    throw new IllegalStateException(
                            "Product Total Transport Carbon benchmark failed: "
                            + benchmark);
                }

                // ==========================================
                // 4. BUILD CALCULATION PROVENANCE
                // ==========================================
                String provenance
                        = "KpiCalculator.computeSupplyTransportCarbonFootprint"
                        + "; calculationBasis="
                        + calculation.get("calculationBasis").getAsString()
                        + "; totalRoadCarbonKg="
                        + calculation.get("totalRoadCarbonKg").getAsDouble()
                        + "; totalSeaCarbonKg="
                        + calculation.get("totalSeaCarbonKg").getAsDouble()
                        + "; totalTransportCarbonKg="
                        + actualValue
                        + "; manufacturedQuantity="
                        + calculation.get("manufacturedQuantity").getAsDouble();

                // ==========================================
                // 5. SAVE PRODUCT KPI EVALUATION
                // ==========================================
                KpiEvaluationService evaluationService
                        = new KpiEvaluationService();

                JsonObject savedEvaluation
                        = evaluationService.saveProductTransportCarbon(
                                productName,
                                actualValue,
                                provenance);

                if (!"success".equals(
                        savedEvaluation.get("status").getAsString())) {

                    throw new IllegalStateException(
                            "Product Transport Carbon KPIEvaluation "
                            + "could not be saved.");
                }

                String evaluationName
                        = savedEvaluation.get("evaluationIndividual")
                                .getAsString();

                // ==========================================
                // 6. SAVE LINKED BENCHMARK ASSESSMENT
                // ==========================================
                double deviationPercentage
                        = benchmark.has("deviationPercentage")
                        && !benchmark.get("deviationPercentage").isJsonNull()
                        ? benchmark.get("deviationPercentage").getAsDouble()
                        : 0.0;

                String assessmentName
                        = ontologyReader.createBenchmarkAssessment(
                                evaluationName,
                                "ProductTransportCarbonBenchmark",
                                benchmark.get("meetsBenchmark").getAsBoolean(),
                                benchmark.get("absoluteDeviation").getAsDouble(),
                                deviationPercentage,
                                benchmark.get("assessment").getAsString(),
                                "Product Total Transport Carbon compared against "
                                + "the product-level demonstration benchmark "
                                + "configured in the ontology.");

                // ==========================================
                // 7. RETURN SUCCESS RESPONSE
                // ==========================================
                JsonObject result = new JsonObject();

                result.addProperty("status", "success");
                result.addProperty("product", productName);
                result.addProperty("computedValue", actualValue);
                result.addProperty("unit", "kg CO2eq");

                result.addProperty(
                        "evaluationIndividual",
                        evaluationName);

                result.addProperty(
                        "benchmarkAssessmentIndividual",
                        assessmentName);

                result.add("benchmarkAssessment", benchmark);
                result.add("calculation", calculation);

                out.print(result.toString());

            } catch (Exception ex) {

                ex.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                JsonObject error = new JsonObject();

                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());

                error.addProperty(
                        "warning",
                        "If saving the benchmark assessment failed, "
                        + "the KPIEvaluation may already have been persisted. "
                        + "Check the ontology before retrying.");

                out.print(error.toString());
            }

        } else if ("saveProductRenewableMaterialEvaluation".equals(action)) {

            try {

                // ==========================================
                // 1. VALIDATE MANUFACTURED PRODUCT
                // ==========================================
                if (!json.has("individual")
                        || json.get("individual").isJsonNull()) {

                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                String productName
                        = json.get("individual").getAsString().trim();

                if (productName.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                // ==========================================
                // 2. RECALCULATE ON THE SERVER
                // ==========================================
                // Reuse the existing Product Renewable Material calculation.
                // Do not trust a value submitted by the frontend.
                JsonObject calculation
                        = KpiCalculator.computeProductRenewableMaterial(
                                productName,
                                ontologyReader);

                if (calculation == null || calculation.has("error")) {

                    throw new IllegalStateException(
                            "Product Renewable Material calculation failed: "
                            + calculation);
                }

                double actualValue
                        = calculation.get("productRenewablePercentage")
                                .getAsDouble();

                if (!Double.isFinite(actualValue)
                        || actualValue < 0.0
                        || actualValue > 100.0) {

                    throw new IllegalStateException(
                            "Invalid Product Renewable Material percentage.");
                }

                // ==========================================
                // 3. EVALUATE PRODUCT-LEVEL BENCHMARK
                // ==========================================
                JsonObject benchmark
                        = KpiBenchmarkService.evaluateProductRenewableMaterial(
                                actualValue,
                                ontologyReader);

                if (!benchmark.has("status")
                        || !"success".equals(
                                benchmark.get("status").getAsString())) {

                    throw new IllegalStateException(
                            "Product Renewable Material benchmark failed: "
                            + benchmark);
                }

                // ==========================================
                // 4. BUILD CALCULATION PROVENANCE
                // ==========================================
                String provenance
                        = "KpiCalculator.computeProductRenewableMaterial"
                        + "; calculationBasis="
                        + calculation.get("calculationBasis").getAsString()
                        + "; totalRenewableWeight="
                        + calculation.get("totalRenewableWeight").getAsDouble()
                        + "; totalMaterialInputWeight="
                        + calculation.get("totalMaterialInputWeight").getAsDouble()
                        + "; productRenewablePercentage="
                        + actualValue
                        + "; manufacturedQuantity="
                        + calculation.get("manufacturedQuantity").getAsDouble();

                // ==========================================
                // 5. SAVE PRODUCT KPI EVALUATION
                // ==========================================
                KpiEvaluationService evaluationService
                        = new KpiEvaluationService();

                JsonObject savedEvaluation
                        = evaluationService.saveProductRenewableMaterial(
                                productName,
                                actualValue,
                                provenance);

                if (!"success".equals(
                        savedEvaluation.get("status").getAsString())) {

                    throw new IllegalStateException(
                            "Product Renewable Material KPIEvaluation "
                            + "could not be saved.");
                }

                String evaluationName
                        = savedEvaluation.get("evaluationIndividual")
                                .getAsString();

                // ==========================================
                // 6. SAVE LINKED BENCHMARK ASSESSMENT
                // ==========================================
                double deviationPercentage
                        = benchmark.has("deviationPercentage")
                        && !benchmark.get("deviationPercentage").isJsonNull()
                        ? benchmark.get("deviationPercentage").getAsDouble()
                        : 0.0;

                String assessmentName
                        = ontologyReader.createBenchmarkAssessment(
                                evaluationName,
                                "ProductRenewableMaterialBenchmark",
                                benchmark.get("meetsBenchmark").getAsBoolean(),
                                benchmark.get("absoluteDeviation").getAsDouble(),
                                deviationPercentage,
                                benchmark.get("assessment").getAsString(),
                                "Product Renewable Material percentage "
                                + "compared against the product-level "
                                + "demonstration benchmark configured "
                                + "in the ontology.");

                // ==========================================
                // 7. RETURN SUCCESS RESPONSE
                // ==========================================
                JsonObject result = new JsonObject();

                result.addProperty("status", "success");
                result.addProperty("product", productName);
                result.addProperty("computedValue", actualValue);
                result.addProperty("unit", "%");

                result.addProperty(
                        "evaluationIndividual",
                        evaluationName);

                result.addProperty(
                        "benchmarkAssessmentIndividual",
                        assessmentName);

                result.add(
                        "benchmarkAssessment",
                        benchmark);

                result.add(
                        "calculation",
                        calculation);

                out.print(result.toString());

            } catch (Exception ex) {

                ex.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                JsonObject error = new JsonObject();

                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());

                error.addProperty(
                        "warning",
                        "If saving the benchmark assessment failed, "
                        + "the KPIEvaluation may already have been persisted. "
                        + "Check the ontology before retrying.");

                out.print(error.toString());
            }
        } else if ("saveProductRecycledMaterialEvaluation".equals(action)) {

            try {

                // ==========================================
                // 1. VALIDATE MANUFACTURED PRODUCT
                // ==========================================
                if (!json.has("individual")
                        || json.get("individual").isJsonNull()) {

                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                String productName
                        = json.get("individual").getAsString().trim();

                if (productName.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Manufactured product individual is required.");
                }

                // ==========================================
                // 2. RECALCULATE ON THE SERVER
                // ==========================================
                // Reuse the existing Product Recycled Material calculation.
                // Never trust a value supplied by the frontend.
                JsonObject calculation
                        = KpiCalculator.computeProductRecycledMaterial(
                                productName,
                                ontologyReader);

                if (calculation == null || calculation.has("error")) {

                    throw new IllegalStateException(
                            "Product Recycled Material calculation failed: "
                            + calculation);
                }

                double actualValue
                        = calculation.get("productRecycledPercentage")
                                .getAsDouble();

                if (!Double.isFinite(actualValue)
                        || actualValue < 0.0
                        || actualValue > 100.0) {

                    throw new IllegalStateException(
                            "Invalid Product Recycled Material percentage.");
                }

                // ==========================================
                // 3. EVALUATE PRODUCT-LEVEL BENCHMARK
                // ==========================================
                JsonObject benchmark
                        = KpiBenchmarkService.evaluateProductRecycledMaterial(
                                actualValue,
                                ontologyReader);

                if (!benchmark.has("status")
                        || !"success".equals(
                                benchmark.get("status").getAsString())) {

                    throw new IllegalStateException(
                            "Product Recycled Material benchmark failed: "
                            + benchmark);
                }

                // ==========================================
                // 4. BUILD CALCULATION PROVENANCE
                // ==========================================
                String provenance
                        = "KpiCalculator.computeProductRecycledMaterial"
                        + "; calculationBasis="
                        + calculation.get("calculationBasis").getAsString()
                        + "; totalRecycledWeight="
                        + calculation.get("totalRecycledWeight").getAsDouble()
                        + "; totalMaterialInputWeight="
                        + calculation.get("totalMaterialInputWeight").getAsDouble()
                        + "; productRecycledPercentage="
                        + actualValue
                        + "; manufacturedQuantity="
                        + calculation.get("manufacturedQuantity").getAsDouble();

                // ==========================================
                // 5. SAVE PRODUCT KPI EVALUATION
                // ==========================================
                KpiEvaluationService evaluationService
                        = new KpiEvaluationService();

                JsonObject savedEvaluation
                        = evaluationService.saveProductRecycledMaterial(
                                productName,
                                actualValue,
                                provenance);

                if (!"success".equals(
                        savedEvaluation.get("status").getAsString())) {

                    throw new IllegalStateException(
                            "Product Recycled Material KPIEvaluation "
                            + "could not be saved.");
                }

                String evaluationName
                        = savedEvaluation.get("evaluationIndividual")
                                .getAsString();

                // ==========================================
                // 6. SAVE LINKED BENCHMARK ASSESSMENT
                // ==========================================
                double deviationPercentage
                        = benchmark.has("deviationPercentage")
                        && !benchmark.get("deviationPercentage").isJsonNull()
                        ? benchmark.get("deviationPercentage").getAsDouble()
                        : 0.0;

                String assessmentName
                        = ontologyReader.createBenchmarkAssessment(
                                evaluationName,
                                "ProductRecycledMaterialBenchmark",
                                benchmark.get("meetsBenchmark").getAsBoolean(),
                                benchmark.get("absoluteDeviation").getAsDouble(),
                                deviationPercentage,
                                benchmark.get("assessment").getAsString(),
                                "Product Recycled Material percentage "
                                + "compared against the product-level "
                                + "demonstration benchmark configured "
                                + "in the ontology.");

                // ==========================================
                // 7. RETURN SUCCESS RESPONSE
                // ==========================================
                JsonObject result = new JsonObject();

                result.addProperty("status", "success");
                result.addProperty("product", productName);
                result.addProperty("computedValue", actualValue);
                result.addProperty("unit", "%");

                result.addProperty(
                        "evaluationIndividual",
                        evaluationName);

                result.addProperty(
                        "benchmarkAssessmentIndividual",
                        assessmentName);

                result.add(
                        "benchmarkAssessment",
                        benchmark);

                result.add(
                        "calculation",
                        calculation);

                out.print(result.toString());

            } catch (Exception ex) {

                ex.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                JsonObject error = new JsonObject();

                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());

                error.addProperty(
                        "warning",
                        "If saving the benchmark assessment failed, "
                        + "the KPIEvaluation may already have been persisted. "
                        + "Check the ontology before retrying.");

                out.print(error.toString());
            }
        } else if ("saveCorporateWaterUseEvaluation".equals(action)
                || "saveCorporateWastewaterEvaluation".equals(action)
                || "saveProductWaterUseEvaluation".equals(action)
                || "saveProductWastewaterEvaluation".equals(action)
                || "saveCorporateHazardousWasteEvaluation".equals(action)
                || "saveCorporateNonHazardousWasteEvaluation".equals(action)
                || "saveProductHazardousWasteEvaluation".equals(action)
                || "saveProductNonHazardousWasteEvaluation".equals(action)) {

            try {
                out.print(saveWaterWasteEvaluation(
                        action, json, ontologyReader).toString());
            } catch (IllegalArgumentException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject error = new JsonObject();
                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());
                out.print(error.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject error = new JsonObject();
                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());
                error.addProperty("warning",
                        "KPIEvaluation may have been saved before the "
                        + "BenchmarkAssessment failed. Inspect ontology before retrying.");
                out.print(error.toString());
            }

        } else if ("saveCorporateEnergyCarbonIntensityEvaluation".equals(action)
                || "saveCorporateRenewableElectricityEvaluation".equals(action)
                || "saveProductEnergyCarbonFootprintEvaluation".equals(action)
                || "saveProductEmbodiedCarbonEvaluation".equals(action)) {

            try {
                out.print(saveEnergyEvaluation(action, json, ontologyReader).toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject error = new JsonObject();
                error.addProperty("status", "error");
                error.addProperty("message", ex.getMessage());
                error.addProperty("warning",
                        "If saving the benchmark assessment failed, the KPIEvaluation "
                        + "may already have been persisted. Check the ontology before retrying.");
                out.print(error.toString());
            }

        } else {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            JsonObject error = new JsonObject();

            error.addProperty("status", "error");
            error.addProperty(
                    "message", "Unknown POST action: " + action);

            out.print(error.toString());
        }

        out.flush();

    }

    /**
     * Server-side Energy KPI recalculation, ontology benchmark validation,
     * KPIEvaluation persistence and linked BenchmarkAssessment persistence.
     */
    private JsonObject saveEnergyEvaluation(
            String action, JsonObject requestJson, OntologyReader ontologyReader)
            throws IOException {

        if (!requestJson.has("individual")
                || requestJson.get("individual").isJsonNull()
                || requestJson.get("individual").getAsString().trim().isEmpty()) {
            throw new IllegalArgumentException("individual is required.");
        }

        String individual = requestJson.get("individual").getAsString().trim();
        boolean corporate = action.startsWith("saveCorporate");
        String processName = corporate ? individual : null;

        if (!corporate) {
            if (!requestJson.has("processSustainability")
                    || requestJson.get("processSustainability").isJsonNull()
                    || requestJson.get("processSustainability").getAsString().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "processSustainability is required for Product Energy evaluations.");
            }
            processName = requestJson.get("processSustainability").getAsString().trim();
        }

        JsonObject calculation;
        double actualValue;
        JsonObject benchmark;
        String provenance;
        String unit;
        KpiEvaluationService evaluationService = new KpiEvaluationService();
        JsonObject savedEvaluation;

        switch (action) {
            case "saveCorporateEnergyCarbonIntensityEvaluation":
            case "saveCorporateRenewableElectricityEvaluation":
                calculation = KpiCalculator.computeCorporateEnergyCarbonFootprint(
                        processName, ontologyReader);
                break;
            case "saveProductEnergyCarbonFootprintEvaluation":
                calculation = KpiCalculator.computeProductEnergyCarbonFootprint(
                        individual, processName, ontologyReader);
                break;
            case "saveProductEmbodiedCarbonEvaluation":
                calculation = KpiCalculator.computeProductEmbodiedCarbon(
                        individual, processName, ontologyReader);
                break;
            default:
                throw new IllegalArgumentException("Unknown Energy action: " + action);
        }

        if (calculation == null || calculation.has("error")) {
            throw new IllegalStateException(
                    "Energy KPI calculation failed: " + calculation);
        }

        provenance = "Energy KPI server-side calculation"
                + "; action=" + action
                + "; processSustainability=" + processName
                + "; calculation=" + calculation.toString();

        switch (action) {
            case "saveCorporateEnergyCarbonIntensityEvaluation":
                actualValue = calculation.get("totalEnergyCarbonPerKg").getAsDouble();
                benchmark = KpiBenchmarkService.evaluateCorporateEnergyCarbonIntensity(
                        actualValue, ontologyReader);
                unit = "kg CO2eq/kg";
                break;
            case "saveCorporateRenewableElectricityEvaluation":
                actualValue = calculation.get("renewableElectricityPercentage").getAsDouble();
                benchmark = KpiBenchmarkService.evaluateCorporateRenewableElectricity(
                        actualValue, ontologyReader);
                unit = "%";
                break;
            case "saveProductEnergyCarbonFootprintEvaluation":
                actualValue = calculation.get("totalAllocatedCarbonKg").getAsDouble();
                benchmark = KpiBenchmarkService.evaluateProductEnergyCarbonFootprint(
                        actualValue, ontologyReader);
                unit = "kg CO2eq";
                break;
            case "saveProductEmbodiedCarbonEvaluation":
                actualValue = calculation.get("embodiedCarbonPerKg").getAsDouble();
                benchmark = KpiBenchmarkService.evaluateProductEmbodiedCarbon(
                        actualValue, ontologyReader);
                unit = "kg CO2eq/kg product";
                break;
            default:
                throw new IllegalArgumentException("Unknown Energy action: " + action);
        }

        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            throw new IllegalStateException("Invalid calculated Energy KPI value.");
        }
        if (benchmark == null || !benchmark.has("status")
                || !"success".equals(benchmark.get("status").getAsString())) {
            throw new IllegalStateException("Energy KPI benchmark failed: " + benchmark);
        }

        // Validate the benchmark before creating any ontology individuals.
        switch (action) {
            case "saveCorporateEnergyCarbonIntensityEvaluation":
                savedEvaluation = evaluationService.saveCorporateEnergyCarbonIntensity(
                        processName, actualValue, provenance);
                break;
            case "saveCorporateRenewableElectricityEvaluation":
                savedEvaluation = evaluationService.saveCorporateRenewableElectricity(
                        processName, actualValue, provenance);
                break;
            case "saveProductEnergyCarbonFootprintEvaluation":
                savedEvaluation = evaluationService.saveProductEnergyCarbonFootprint(
                        individual, actualValue, provenance);
                break;
            case "saveProductEmbodiedCarbonEvaluation":
                savedEvaluation = evaluationService.saveProductEmbodiedCarbon(
                        individual, actualValue, provenance);
                break;
            default:
                throw new IllegalArgumentException("Unknown Energy action: " + action);
        }

        if (savedEvaluation == null || !savedEvaluation.has("status")
                || !"success".equals(savedEvaluation.get("status").getAsString())) {
            throw new IllegalStateException(
                    "Could not persist Energy KPIEvaluation: " + savedEvaluation);
        }

        String evaluationName
                = savedEvaluation.get("evaluationIndividual").getAsString();

        double deviationPercentage = benchmark.has("deviationPercentage")
                && !benchmark.get("deviationPercentage").isJsonNull()
                ? benchmark.get("deviationPercentage").getAsDouble()
                : 0.0;

        String assessmentName = ontologyReader.createBenchmarkAssessment(
                evaluationName,
                benchmark.get("benchmark").getAsString(),
                benchmark.get("meetsBenchmark").getAsBoolean(),
                benchmark.get("absoluteDeviation").getAsDouble(),
                deviationPercentage,
                benchmark.get("assessment").getAsString(),
                "Energy KPI benchmark assessment"
                + "; action=" + action
                + "; individual=" + individual
                + "; processSustainability=" + processName
                + "; actual=" + actualValue + " " + unit);

        JsonObject result = new JsonObject();
        result.addProperty("status", "success");
        if (corporate) {
            result.addProperty("processSustainability", processName);
        } else {
            result.addProperty("product", individual);
            result.addProperty("processSustainability", processName);
        }
        result.addProperty("computedValue", actualValue);
        result.addProperty("unit", unit);
        result.addProperty("evaluationIndividual", evaluationName);
        result.addProperty("benchmarkAssessmentIndividual", assessmentName);
        result.add("evaluation", savedEvaluation);
        result.add("benchmarkAssessment", benchmark);
        result.add("calculation", calculation);
        return result;
    }

    /**
     * Server-side Water/Waste calculation, ontology benchmark validation,
     * KPIEvaluation and linked BenchmarkAssessment persistence.
     */
    private JsonObject saveWaterWasteEvaluation(
            String action, JsonObject requestJson, OntologyReader ontologyReader)
            throws IOException {

        if (!requestJson.has("individual")
                || requestJson.get("individual").isJsonNull()
                || requestJson.get("individual").getAsString().trim().isEmpty()) {
            throw new IllegalArgumentException("individual is required.");
        }
        String individual = requestJson.get("individual").getAsString().trim();
        boolean corporate = action.startsWith("saveCorporate");
        String processName = corporate ? individual : null;
        if (!corporate) {
            if (!requestJson.has("processSustainability")
                    || requestJson.get("processSustainability").isJsonNull()
                    || requestJson.get("processSustainability").getAsString().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "processSustainability is required for product KPIs.");
            }
            processName = requestJson.get("processSustainability")
                    .getAsString().trim();
        }

        String baseName;
        String resultKey;
        String unit;
        String calculationType;

        switch (action) {
            case "saveCorporateWaterUseEvaluation":
                baseName = "CorporateWaterUse";
                resultKey = "waterUsePerKg";
                unit = "m3/kg";
                calculationType = "corporateWaterKpis";
                break;
            case "saveCorporateWastewaterEvaluation":
                baseName = "CorporateWastewater";
                resultKey = "wastewaterPerKg";
                unit = "m3/kg";
                calculationType = "corporateWaterKpis";
                break;
            case "saveProductWaterUseEvaluation":
                baseName = "ProductWaterUse";
                resultKey = "allocatedWaterUsageM3";
                unit = "m3/product";
                calculationType = "productWaterKpis";
                break;
            case "saveProductWastewaterEvaluation":
                baseName = "ProductWastewater";
                resultKey = "allocatedWastewaterM3";
                unit = "m3/product";
                calculationType = "productWaterKpis";
                break;
            case "saveCorporateHazardousWasteEvaluation":
                baseName = "CorporateHazardousWaste";
                resultKey = "hazardousWastePerKg";
                unit = "kg/kg";
                calculationType = "corporateWasteKpis";
                break;
            case "saveCorporateNonHazardousWasteEvaluation":
                baseName = "CorporateNonHazardousWaste";
                resultKey = "nonHazardousWastePerKg";
                unit = "kg/kg";
                calculationType = "corporateWasteKpis";
                break;
            case "saveProductHazardousWasteEvaluation":
                baseName = "ProductHazardousWaste";
                resultKey = "allocatedHazardousWasteKg";
                unit = "kg/product";
                calculationType = "productWasteKpis";
                break;
            case "saveProductNonHazardousWasteEvaluation":
                baseName = "ProductNonHazardousWaste";
                resultKey = "allocatedNonHazardousWasteKg";
                unit = "kg/product";
                calculationType = "productWasteKpis";
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown Water/Waste save action: " + action);
        }

        JsonObject calculation;
        switch (calculationType) {
            case "corporateWaterKpis":
                calculation = KpiCalculator.computeCorporateWaterKpis(
                        processName, ontologyReader);
                break;
            case "productWaterKpis":
                calculation = KpiCalculator.computeProductWaterKpis(
                        individual, processName, ontologyReader);
                break;
            case "corporateWasteKpis":
                calculation = KpiCalculator.computeCorporateWasteKpis(
                        processName, ontologyReader);
                break;
            case "productWasteKpis":
                calculation = KpiCalculator.computeProductWasteKpis(
                        individual, processName, ontologyReader);
                break;
            default:
                throw new IllegalStateException(
                        "Unknown calculation type: " + calculationType);
        }

        if (calculation == null || calculation.has("error")) {
            throw new IllegalStateException(
                    "Water/Waste calculation failed: " + calculation);
        }
        if (!calculation.has(resultKey)
                || calculation.get(resultKey).isJsonNull()) {
            throw new IllegalStateException(
                    "Missing calculated KPI field: " + resultKey);
        }

        double actualValue = calculation.get(resultKey).getAsDouble();
        JsonObject benchmark;
        switch (baseName) {
            case "CorporateWaterUse":
                benchmark = KpiBenchmarkService.evaluateCorporateWaterUse(
                        actualValue, ontologyReader);
                break;
            case "CorporateWastewater":
                benchmark = KpiBenchmarkService.evaluateCorporateWastewater(
                        actualValue, ontologyReader);
                break;
            case "ProductWaterUse":
                benchmark = KpiBenchmarkService.evaluateProductWaterUse(
                        actualValue, ontologyReader);
                break;
            case "ProductWastewater":
                benchmark = KpiBenchmarkService.evaluateProductWastewater(
                        actualValue, ontologyReader);
                break;
            case "CorporateHazardousWaste":
                benchmark = KpiBenchmarkService.evaluateCorporateHazardousWaste(
                        actualValue, ontologyReader);
                break;
            case "CorporateNonHazardousWaste":
                benchmark = KpiBenchmarkService.evaluateCorporateNonHazardousWaste(
                        actualValue, ontologyReader);
                break;
            case "ProductHazardousWaste":
                benchmark = KpiBenchmarkService.evaluateProductHazardousWaste(
                        actualValue, ontologyReader);
                break;
            case "ProductNonHazardousWaste":
                benchmark = KpiBenchmarkService.evaluateProductNonHazardousWaste(
                        actualValue, ontologyReader);
                break;
            default:
                throw new IllegalStateException("Unknown KPI: " + baseName);
        }

        if (benchmark == null || !benchmark.has("status")
                || !"success".equals(benchmark.get("status").getAsString())) {
            throw new IllegalStateException(
                    "Benchmark evaluation failed; no KPIEvaluation saved: "
                    + benchmark);
        }

        String provenance = "Water/Waste KPI server-side calculation"
                + "; action=" + action
                + "; processSustainability=" + processName
                + "; calculation=" + calculation.toString();

        KpiEvaluationService evaluationService = new KpiEvaluationService();
        JsonObject savedEvaluation = evaluationService.saveWaterWasteEvaluation(
                baseName, individual, processName, actualValue, unit, provenance);

        if (savedEvaluation == null || !savedEvaluation.has("status")
                || !"success".equals(savedEvaluation.get("status").getAsString())) {
            throw new IllegalStateException(
                    "KPIEvaluation persistence failed: " + savedEvaluation);
        }

        String evaluationName = savedEvaluation.get("evaluationIndividual")
                .getAsString();
        double deviationPercentage = benchmark.has("deviationPercentage")
                && !benchmark.get("deviationPercentage").isJsonNull()
                ? benchmark.get("deviationPercentage").getAsDouble()
                : 0.0;

        String assessmentName = ontologyReader.createBenchmarkAssessment(
                evaluationName,
                benchmark.get("benchmark").getAsString(),
                benchmark.get("meetsBenchmark").getAsBoolean(),
                benchmark.get("absoluteDeviation").getAsDouble(),
                deviationPercentage,
                benchmark.get("assessment").getAsString(),
                "Water/Waste benchmark assessment"
                + "; action=" + action
                + "; individual=" + individual
                + "; processSustainability=" + processName
                + "; actual=" + actualValue + " " + unit);

        JsonObject result = new JsonObject();
        result.addProperty("status", "success");
        result.addProperty("individual", individual);
        result.addProperty("processSustainability", processName);
        result.addProperty("computedValue", actualValue);
        result.addProperty("unit", unit);
        result.addProperty("evaluationIndividual", evaluationName);
        result.addProperty("benchmarkAssessmentIndividual", assessmentName);
        result.add("evaluation", savedEvaluation);
        result.add("benchmarkAssessment", benchmark);
        result.add("calculation", calculation);
        return result;
    }

}
