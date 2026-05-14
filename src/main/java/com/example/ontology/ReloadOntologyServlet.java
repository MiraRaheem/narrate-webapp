package com.example.ontology;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/admin/reloadOntology")
public class ReloadOntologyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        try (PrintWriter out = response.getWriter()) {

            System.out.println("♻️ Manual ontology reload triggered...");

            OntologyReader.reloadModel();

            out.write("""
                {
                  "status": "success",
                  "message": "Ontology reloaded successfully"
                }
            """);

            System.out.println("✅ Ontology reload completed.");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            try (PrintWriter out = response.getWriter()) {
                out.write("""
                    {
                      "status": "error",
                      "message": "Failed to reload ontology"
                    }
                """);
            }
        }
    }
}
