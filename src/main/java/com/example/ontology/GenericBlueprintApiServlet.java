/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.example.ontology;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet("/api/*")
public class GenericBlueprintApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        System.out.println("=== GenericBlueprintApiServlet HIT ===");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        System.out.println("PathInfo: " + pathInfo);

        OntologyReader ontologyReader = OntologyReader.getInstance();

        // =====================================
// CASE 0: GET /api/classes
// =====================================
        if (pathInfo != null && pathInfo.equals("/classes")) {

            Set<String> classes = ontologyReader.getOntologyClasses();
            // Use the correct method you already have
            // If it's named differently, replace accordingly

            out.print(gson.toJson(Map.of(
                    "classes", classes,
                    "count", classes.size()
            )));
            return;
        }

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Class name required.\"}");
            return;
        }

        String[] parts = pathInfo.substring(1).split("/");
        String className = parts[0];

        System.out.println("ClassName: " + className);

        try {
            //OntologyReader ontologyReader = OntologyReader.getInstance();

            // CASE 1: /api/{className}
            if (parts.length == 1) {
                Set<String> instances
                        = ontologyReader.getInstancesOfClass(className);

                out.print(gson.toJson(Map.of(
                        "class", className,
                        "count", instances.size(),
                        "instances", instances
                )));
                return;
            }

            // CASE 2: /api/{className}/{id}
            if (parts.length == 2) {
                String individualId = parts[1];
                System.out.println("IndividualId: " + individualId);

                Map<String, String> details
                        = ontologyReader.getIndividualDetails(individualId);

                if (details == null || details.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Individual not found.\"}");
                    return;
                }

                out.print(gson.toJson(Map.of(
                        "class", className,
                        "instance", individualId,
                        "data", details
                )));
                return;
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid path.\"}");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of(
                    "error", "Server error",
                    "detail", e.getMessage()
            )));
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();   // Example: /MaterialSupplier

        if (path == null || path.equals("/")) {
            response.getWriter().write("{\"error\":\"Missing class name\"}");
            return;
        }

        // Extract class name from path
        String className = path.substring(1);

        // Read the incoming JSON body
        JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

        // Inject the className expected by AddIndividualServlet2
        body.addProperty("className", className);

        // Convert modified JSON back to bytes
        byte[] newBody = body.toString().getBytes(StandardCharsets.UTF_8);

        // Wrap request so the forwarded servlet receives the modified JSON
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {

            @Override
            public ServletInputStream getInputStream() {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(newBody);

                return new ServletInputStream() {

                    @Override
                    public int read() {
                        return byteArrayInputStream.read();
                    }

                    @Override
                    public boolean isFinished() {
                        return byteArrayInputStream.available() == 0;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(ReadListener readListener) {
                    }
                };
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(newBody),
                                StandardCharsets.UTF_8
                        )
                );
            }

            @Override
            public int getContentLength() {
                return newBody.length;
            }

            @Override
            public long getContentLengthLong() {
                return newBody.length;
            }

            @Override
            public String getContentType() {
                return "application/json";
            }
        };

        // Forward to the original servlet without modifying its code
        request.getRequestDispatcher("/AddIndividualServlet2")
                .forward(wrappedRequest, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();

        if (path == null || path.split("/").length < 3) {
            response.getWriter().write("{\"error\":\"Invalid endpoint\"}");
            return;
        }

        // Extract className and individualName
        String[] parts = path.split("/");
        String className = parts[1];
        String individualName = parts[2];

        Gson gson = new Gson();

        // ==========================================
        // STEP 1: FETCH EXISTING DATA (IMPORTANT)
        // ==========================================
        OntologyReader reader = OntologyReader.getInstance();

        Map<String, List<String>> existingData
                = reader.getAllDataPropertiesForIndividual(individualName);

        Map<String, List<String>> existingObject
                = reader.getAllObjectPropertiesWithReasoner(individualName);

        if (existingData == null) {
            existingData = new HashMap<>();
        }
        if (existingObject == null) {
            existingObject = new HashMap<>();
        }

        // ==========================================
        // STEP 2: READ INCOMING REQUEST
        // ==========================================
        JsonObject inputJson = JsonParser.parseReader(request.getReader()).getAsJsonObject();

        Map<String, List<String>> newData = new HashMap<>();
        Map<String, List<String>> newObject = new HashMap<>();

        // Convert incoming dataProperties (array → map)
        if (inputJson.has("dataProperties")) {
            inputJson.getAsJsonArray("dataProperties").forEach(el -> {
                JsonObject obj = el.getAsJsonObject();
                String prop = obj.get("property").getAsString();
                String val = obj.has("value") && !obj.get("value").isJsonNull()
                        ? obj.get("value").getAsString()
                        : null;

                if (val != null && !val.trim().isEmpty()) {
                    newData.computeIfAbsent(prop, k -> new ArrayList<>()).add(val);
                }
            });
        }

        // Convert incoming objectProperties (array → map)
        if (inputJson.has("objectProperties")) {
            inputJson.getAsJsonArray("objectProperties").forEach(el -> {
                JsonObject obj = el.getAsJsonObject();
                String prop = obj.get("property").getAsString();
                String val = obj.has("value") && !obj.get("value").isJsonNull()
                        ? obj.get("value").getAsString()
                        : null;

                if (val != null && !val.trim().isEmpty()) {
                    newObject.computeIfAbsent(prop, k -> new ArrayList<>()).add(val);
                }
            });
        }

        // ==========================================
        // STEP 3: MERGE (CRITICAL FIX)
        // ==========================================
        // Overwrite only updated data properties
        for (String key : newData.keySet()) {
            existingData.put(key, newData.get(key));
        }

        // Overwrite only updated object properties
        for (String key : newObject.keySet()) {
            existingObject.put(key, newObject.get(key));
        }

        // ==========================================
        // STEP 4: BUILD FINAL PAYLOAD
        // ==========================================
        JsonObject payload = new JsonObject();
        payload.addProperty("action", "updateIndividual");
        payload.addProperty("className", className);
        payload.addProperty("individualName", individualName); // ✅ FIX HERE

        existingData = cleanNullValues(existingData);
        existingObject = cleanNullValues(existingObject);

        payload.add("dataProperties", gson.toJsonTree(existingData));
        payload.add("objectProperties", gson.toJsonTree(existingObject));

        byte[] newBody = payload.toString().getBytes(StandardCharsets.UTF_8);

        // ==========================================
        // STEP 5: WRAP REQUEST
        // ==========================================
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {

            @Override
            public String getMethod() {
                return "POST"; // 🔥 FORCE POST
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(newBody),
                                StandardCharsets.UTF_8
                        )
                );
            }

            @Override
            public ServletInputStream getInputStream() {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(newBody);

                return new ServletInputStream() {
                    @Override
                    public int read() {
                        return byteArrayInputStream.read();
                    }

                    @Override
                    public boolean isFinished() {
                        return byteArrayInputStream.available() == 0;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(ReadListener readListener) {
                    }
                };
            }

            @Override
            public int getContentLength() {
                return newBody.length;
            }

            @Override
            public long getContentLengthLong() {
                return newBody.length;
            }

            @Override
            public String getContentType() {
                return "application/json";
            }
        };

        // ==========================================
        // STEP 6: FORWARD TO EXISTING BACKEND
        // ==========================================
        request.getRequestDispatcher("/UpdateIndividualServlet1")
                .forward(wrappedRequest, response);
    }

    private Map<String, List<String>> cleanNullValues(Map<String, List<String>> map) {
        Map<String, List<String>> cleaned = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            if (values == null) {
                continue;
            }

            List<String> filtered = values.stream()
                    .filter(v -> v != null && !v.trim().isEmpty())
                    .collect(Collectors.toList());

            if (!filtered.isEmpty()) {
                cleaned.put(key, filtered);
            }
        }

        return cleaned;
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo(); // /MaterialSupplier/MaterialSupplier_1

        if (pathInfo == null || pathInfo.split("/").length < 3) {
            response.getWriter().write("{\"error\":\"Invalid DELETE path\"}");
            return;
        }

        String[] parts = pathInfo.split("/");
        String className = parts[1];
        String instanceId = parts[2];

        // 🔥 Build parameters exactly like frontend servlet expects
        String queryString = "className=" + className + "&individualName=" + instanceId;

        byte[] newBody = new byte[0]; // no body needed

        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {

            @Override
            public String getMethod() {
                return "POST"; // 🔥 force POST
            }

            @Override
            public String getParameter(String name) {
                if ("className".equals(name)) {
                    return className;
                }
                if ("individualName".equals(name)) {
                    return instanceId;
                }
                return super.getParameter(name);
            }

            @Override
            public String getQueryString() {
                return queryString;
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(newBody),
                                StandardCharsets.UTF_8
                        )
                );
            }

            @Override
            public ServletInputStream getInputStream() {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(newBody);

                return new ServletInputStream() {
                    @Override
                    public int read() {
                        return byteArrayInputStream.read();
                    }

                    @Override
                    public boolean isFinished() {
                        return byteArrayInputStream.available() == 0;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(ReadListener readListener) {
                    }
                };
            }
        };

        // 🔥 Forward to your WORKING servlet
        request.getRequestDispatcher("/DeleteIndividualServlet")
                .forward(wrappedRequest, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
