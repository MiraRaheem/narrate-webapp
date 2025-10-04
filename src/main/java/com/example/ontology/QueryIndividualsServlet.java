/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.example.ontology;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import static jakarta.ws.rs.client.Entity.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 *
 * @author amal.elgammal
 */
@WebServlet(name = "QueryIndividualsServlet", urlPatterns = {"/QueryIndividualsServlet"})
public class QueryIndividualsServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonRequest = JsonParser.parseReader(reader).getAsJsonObject();

            String className = jsonRequest.get("className").getAsString();

            Map<String, List<String>> dataProperties = new HashMap<>();
            Map<String, List<String>> objectProperties = new HashMap<>();

            boolean includeFuzzy = jsonRequest.has("includeFuzzy") && jsonRequest.get("includeFuzzy").getAsBoolean();
            int fuzzyThreshold = jsonRequest.has("fuzzyThreshold") ? jsonRequest.get("fuzzyThreshold").getAsInt() : 0;
            System.out.println("🔍 Fuzzy Matching Enabled: " + includeFuzzy + " | Threshold: " + fuzzyThreshold);

            // ✅ Parse data properties
            if (jsonRequest.has("dataProperties")) {
                JsonObject dpJson = jsonRequest.getAsJsonObject("dataProperties");
                for (Map.Entry<String, JsonElement> entry : dpJson.entrySet()) {
                    List<String> values = new ArrayList<>();
                    if (entry.getValue().isJsonArray()) {
                        for (JsonElement val : entry.getValue().getAsJsonArray()) {
                            values.add(val.getAsString());
                        }
                    } else {
                        values.add(entry.getValue().getAsString());
                    }
                    dataProperties.put(entry.getKey(), values);
                }
            }

            // ✅ Parse object properties
            if (jsonRequest.has("objectProperties")) {
                JsonObject opJson = jsonRequest.getAsJsonObject("objectProperties");
                for (Map.Entry<String, JsonElement> entry : opJson.entrySet()) {
                    List<String> values = new ArrayList<>();
                    if (entry.getValue().isJsonArray()) {
                        for (JsonElement val : entry.getValue().getAsJsonArray()) {
                            values.add(val.getAsString());
                        }
                    } else {
                        values.add(entry.getValue().getAsString());
                    }
                    objectProperties.put(entry.getKey(), values);
                }
            }

            // 🔍 Query individuals using OntologyReader
            OntologyReader readerInstance = OntologyReader.getInstance();
            //Set<String> matchedIndividuals = readerInstance.queryIndividuals(className, dataProperties, objectProperties);

            Map<String, Object> queryResult = readerInstance.queryIndividuals(
                    className, dataProperties, objectProperties,
                    jsonRequest.has("includeFuzzy") && jsonRequest.get("includeFuzzy").getAsBoolean(),
                    jsonRequest.has("fuzzyThreshold") ? jsonRequest.get("fuzzyThreshold").getAsDouble() : 0.7
            );

            // ✅ Send response
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", "success");
            responseMap.put("matchedIndividuals", queryResult.get("matchedIndividuals"));
            responseMap.put("similarityNotes", queryResult.get("similarityNotes"));

            out.print(gson.toJson(responseMap));

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "❌ Failed to process query: " + e.getMessage());
            out.print(gson.toJson(error));
        } finally {
            out.flush();
        }

        //response.setContentType("text/html;charset=UTF-8");
        /*try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet QueryIndividualsServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet QueryIndividualsServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }*/
    }

    private double computeSimilarity(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = org.apache.commons.text.similarity.LevenshteinDistance.getDefaultInstance().apply(s1, s2);
        return 1.0 - ((double) distance / maxLength);
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
