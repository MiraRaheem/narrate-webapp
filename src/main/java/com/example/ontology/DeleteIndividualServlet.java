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
import com.example.ontology.OntologyReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amal.elgammal
 */
@WebServlet(name = "DeleteIndividualServlet", urlPatterns = {"/DeleteIndividualServlet"})
public class DeleteIndividualServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private OntologyReader ontologyReader;
    private static final Logger LOGGER = Logger.getLogger(DeleteIndividualServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        ontologyReader = OntologyReader.getInstance(); // ✅ Assign to class-level variable
        //OntologyReader.reloadModel();
    }

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
        ResponseMessage message;

        String className = request.getParameter("className");
        String individualName = request.getParameter("individualName");

        if (className == null || individualName == null || className.isEmpty() || individualName.isEmpty()) {
            LOGGER.log(Level.WARNING, "Invalid request parameters: className={0}, individualName={1}", new Object[]{className, individualName});
            out.write(gson.toJson(new ResponseMessage("error", "Invalid request parameters.")));
            return;
        }

        try {
            boolean deleted = ontologyReader.deleteIndividual(individualName);

            if (deleted) {
                LOGGER.log(Level.INFO, "Successfully deleted individual: {0}", individualName);

                // ✅ Reload the ontology model after deletion
                ontologyReader.reloadModel();

                out.write(gson.toJson(new ResponseMessage("success", "Individual '" + individualName + "' deleted successfully.")));
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete individual: {0}", individualName);
                out.write(gson.toJson(new ResponseMessage("error", "Failed to delete individual '" + individualName + "'.")));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting individual: " + individualName, e);
            out.write(gson.toJson(new ResponseMessage("error", "An error occurred while deleting the individual.")));
        }

    }

    // Inner class for response messages
    private class ResponseMessage {

        String status;
        String message;

        public ResponseMessage(String status, String message) {
            this.status = status;
            this.message = message;
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
        return "Handles deletion of individuals from OWL ontology";
    }// </editor-fold>

}
