package com.example.ontology;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(
        name = "UpdateIndividualServlet1",
        urlPatterns = {"/UpdateIndividualServlet1"}
)
public class UpdateIndividualServlet1 extends HttpServlet {

    private static final String ONTOLOGY_URI =
            "http://www.semanticweb.org/amal.elgammal/ontologies/2025/3/untitled-ontology-31#";

    private OntologyReader ontologyReader;

    @Override
    public void init() throws ServletException {
        super.init();
        ontologyReader = OntologyReader.getInstance();
    }

    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try {

            // =====================================================
            // 1. PARSE REQUEST
            // =====================================================

            JsonObject jsonRequest =
                    JsonParser.parseReader(
                            request.getReader()
                    ).getAsJsonObject();

            String className =
                    jsonRequest.has("className")
                            ? jsonRequest.get("className").getAsString()
                            : null;

            String individualName =
                    jsonRequest.has("individualName")
                            ? jsonRequest.get("individualName").getAsString()
                            : null;


            // =====================================================
            // 2. VALIDATE REQUIRED FIELDS
            // =====================================================

            if (className == null
                    || className.trim().isEmpty()
                    || individualName == null
                    || individualName.trim().isEmpty()) {

                sendErrorResponse(
                        out,
                        gson,
                        "❌ Missing className or individualName."
                );

                return;
            }


            // =====================================================
            // 3. READ DATA PROPERTIES
            // =====================================================

            Map<String, Object> dataProperties =
                    new HashMap<>();

            if (jsonRequest.has("dataProperties")
                    && jsonRequest.get("dataProperties").isJsonObject()) {

                JsonObject dataPropsJson =
                        jsonRequest.getAsJsonObject("dataProperties");

                for (Map.Entry<String, JsonElement> entry
                        : dataPropsJson.entrySet()) {

                    JsonElement element =
                            entry.getValue();

                    if (element == null
                            || element.isJsonNull()) {
                        continue;
                    }

                    List<String> values =
                            new ArrayList<>();

                    if (element.isJsonArray()) {

                        for (JsonElement value
                                : element.getAsJsonArray()) {

                            if (value != null
                                    && !value.isJsonNull()) {

                                values.add(
                                        value.getAsString()
                                );
                            }
                        }

                    } else {

                        values.add(
                                element.getAsString()
                        );
                    }

                    dataProperties.put(
                            entry.getKey(),
                            values
                    );
                }
            }


            // =====================================================
            // 4. READ OBJECT PROPERTIES
            // =====================================================

            Map<String, Object> objectProperties =
                    new HashMap<>();

            if (jsonRequest.has("objectProperties")
                    && jsonRequest.get("objectProperties").isJsonObject()) {

                JsonObject objectPropsJson =
                        jsonRequest.getAsJsonObject("objectProperties");

                for (Map.Entry<String, JsonElement> entry
                        : objectPropsJson.entrySet()) {

                    JsonElement element =
                            entry.getValue();

                    if (element == null
                            || element.isJsonNull()) {
                        continue;
                    }

                    List<String> values =
                            new ArrayList<>();

                    if (element.isJsonArray()) {

                        for (JsonElement value
                                : element.getAsJsonArray()) {

                            if (value != null
                                    && !value.isJsonNull()) {

                                values.add(
                                        value.getAsString()
                                );
                            }
                        }

                    } else {

                        values.add(
                                element.getAsString()
                        );
                    }

                    objectProperties.put(
                            entry.getKey(),
                            values
                    );
                }
            }


            // =====================================================
            // 5. PERFORM UPDATE
            // =====================================================
            //
            // IMPORTANT:
            //
            // All ontology modification is now handled by
            // OntologyReader.updateIndividual().
            //
            // That method is responsible for:
            //
            //   - acquiring WRITE LOCK
            //   - loading the latest ontology
            //   - modifying the shared model
            //   - preserving datatype handling
            //   - preserving object properties
            //   - preserving "type"
            //   - writing a temporary file
            //   - atomically replacing the ontology
            //   - reloading the model
            //   - releasing the WRITE LOCK
            //
            boolean updated =
                    ontologyReader.updateIndividual(
                            className,
                            individualName,
                            dataProperties,
                            objectProperties
                    );


            // =====================================================
            // 6. HANDLE FAILURE
            // =====================================================

            if (!updated) {

                sendErrorResponse(
                        out,
                        gson,
                        "❌ Individual not found or update failed."
                );

                return;
            }


            // =====================================================
            // 7. SUCCESS RESPONSE
            // =====================================================

            Map<String, Object> jsonResponse =
                    new HashMap<>();

            jsonResponse.put(
                    "status",
                    "success"
            );

            jsonResponse.put(
                    "message",
                    "✅ Individual updated successfully."
            );

            jsonResponse.put(
                    "updatedDataProperties",
                    dataProperties
            );

            jsonResponse.put(
                    "updatedObjectProperties",
                    objectProperties
            );

            out.print(
                    gson.toJson(jsonResponse)
            );

            out.flush();

        } catch (Exception e) {

            e.printStackTrace();

            sendErrorResponse(
                    out,
                    gson,
                    "❌ Error processing update request: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // ERROR RESPONSE
    // =========================================================

    private void sendErrorResponse(
            PrintWriter out,
            Gson gson,
            String message) {

        Map<String, String> errorResponse =
                new HashMap<>();

        errorResponse.put(
                "status",
                "error"
        );

        errorResponse.put(
                "message",
                message
        );

        out.print(
                gson.toJson(errorResponse)
        );

        out.flush();
    }


    // =========================================================
    // HTTP METHODS
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(
                request,
                response
        );
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(
                request,
                response
        );
    }


    @Override
    public String getServletInfo() {
        return "Updates ontology individuals safely using OntologyReader.";
    }
}
