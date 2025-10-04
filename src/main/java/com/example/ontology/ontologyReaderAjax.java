package com.example.ontology;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import com.google.gson.Gson;
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
                        Set<String> instances = ontologyReader.getInstancesOfClass(actualClass);
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
            } else if ("uriProperties".equals(type) && className != null) {
                Set<String> uriProperties = ontologyReader.getURIDataProperties(className);
                out.print(gson.toJson(uriProperties));
            } else if ("enumeratedProperties".equals(type) && className != null) {
                Map<String, List<String>> enums = ontologyReader.getEnumeratedDataProperties(className);
                out.print(gson.toJson(enums));
            } else if ("inverseObjectProperties".equals(type) && individualName != null) {
                Map<String, List<String>> inverseProps = ontologyReader.getInverseObjectProperties(individualName);
                out.print(gson.toJson(inverseProps));
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
        }

        out.flush();
    }
}
