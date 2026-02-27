/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.example.ontology;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

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

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Class name required.\"}");
            return;
        }

        String[] parts = pathInfo.substring(1).split("/");
        String className = parts[0];

        System.out.println("ClassName: " + className);

        try {
            OntologyReader ontologyReader = OntologyReader.getInstance();

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
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
