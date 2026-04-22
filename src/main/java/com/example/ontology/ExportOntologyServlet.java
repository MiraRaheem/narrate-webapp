package com.example.ontology;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

@WebServlet("/export-ontology")
public class ExportOntologyServlet extends HttpServlet {

    private static final String ONTOLOGY_PATH =
            System.getenv().getOrDefault(
                    "ONTOLOGY_PATH",
                    "/data/NARRATE-blueprints-rdf-xml.rdf"
            );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        File file = new File(ONTOLOGY_PATH);

        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Ontology file not found.");
            return;
        }

        response.setContentType("application/rdf+xml");
        response.setHeader("Content-Disposition", "attachment; filename=ontology.rdf");

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
