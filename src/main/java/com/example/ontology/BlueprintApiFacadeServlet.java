/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.example.ontology;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/query/*")
public class BlueprintApiFacadeServlet extends HttpServlet {

    private final Gson gson = new Gson();

    // ===============================
    // GET → METADATA
    // ===============================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo(); // /metadata/SalesOrder
        OntologyReader reader = OntologyReader.getInstance();

        if (path != null && path.startsWith("/metadata/")) {

            String className = path.substring("/metadata/".length());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("class", className);
            result.put("dataProperties", reader.getDataPropertiesWithMeta(className));
            result.put("objectProperties", reader.getObjectPropertiesWithComments(className, false));
            result.put("numericProperties", reader.getNumericDataProperties(className));
            result.put("enumeratedProperties", reader.getEnumeratedDataProperties(className));
            result.put("cardinalities", reader.getCardinalities(className));

            response.getWriter().write(gson.toJson(result));
            return;
        }

        response.getWriter().write("{\"error\":\"Invalid metadata endpoint\"}");
    }

    // ===============================
    // POST → FORWARD TO EXISTING SERVLET
    // ===============================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        // If calling POST /api/query
        if (path == null || "/".equals(path)) {

            request.getRequestDispatcher("/QueryIndividualsServlet")
                    .forward(request, response);
            return;
        }

        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Invalid query endpoint\"}");
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
