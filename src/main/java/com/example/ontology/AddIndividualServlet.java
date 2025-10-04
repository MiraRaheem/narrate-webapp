/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.example.ontology;

import java.io.*;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

/**
 *
 * @author amal.elgammal
 */
@WebServlet(name = "AddIndividualServlet", urlPatterns = {"/AddIndividualServlet"})
public class AddIndividualServlet extends HttpServlet {

    //private static final String ONTOLOGY_PATH = "C:/Programs/university-rdf-xml.owl"; // Change this to your actual path
    //private static final String ONTOLOGY_PATH = "C:/Programs/university-rdf-xml.owl"; // Change this to your actual path
    //private static final String ONTOLOGY_URI = "http://www.semanticweb.org/amal.elgammal/ontologies/2025/2/untitled-ontology-3#";
    // Use env var with a safe default inside the container
    private static final String ONTOLOGY_PATH =System.getenv().getOrDefault("ONTOLOGY_PATH", "/data/NARRATE-blueprints-rdf-xml.rdf");
    // Use the dynamic namespace from OntologyReader (inferred from the RDF or taken from ONTOLOGY_NS)
private static String ONTOLOGY_URI() { return OntologyReader.getNS(); }

    //private static final String ONTOLOGY_URI = "http://www.semanticweb.org/amal.elgammal/ontologies/2025/3/untitled-ontology-31#";

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

        String className = request.getParameter("className");
        String individualName = request.getParameter("individualName");

        if (className == null || individualName == null || className.trim().isEmpty() || individualName.trim().isEmpty()) {
            response.getWriter().println("Invalid input!");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AddIndividualServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AddIndividualServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");

            if (addIndividualToOntology(className, individualName)) {
                out.println("<h2>Individual added successfully!</h2>");
            } else {
                out.println("<h2>Error: Class not found in ontology.</h2>");
            }
        }

    }

    private boolean addIndividualToOntology(String className, String individualName) {
        // Load the ontology
        try {
            OntModel model = ModelFactory.createOntologyModel();
            InputStream in = FileManager.get().open(ONTOLOGY_PATH);
            if (in == null) {
                System.out.println("Error: Ontology file not found!");
                return false;
            }
            model.read(in, null);
            in.close();

            // Check if class exists
            OntClass ontClass = model.getOntClass(ONTOLOGY_URI() + className);
            System.out.println("ontClass.getURI" + ontClass.getURI());

            if (ontClass == null) {
                System.out.println("Class not found in ontology: " + className);
                return false;
            }

            // Create new individual
            Individual newIndividual = model.createIndividual(ONTOLOGY_URI() + individualName, ontClass);
            System.out.println("Added individual: " + individualName + " of class " + className);

            // Write back to OWL file
            try (FileOutputStream out = new FileOutputStream(ONTOLOGY_PATH)) {
                model.write(out, "RDF/XML");
                System.out.println("Ontology updated successfully!");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

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
