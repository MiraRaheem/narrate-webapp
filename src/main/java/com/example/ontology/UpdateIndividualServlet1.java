/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.example.ontology;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.XSD;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

/**
 *
 * @author amal.elgammal
 */
@WebServlet(name = "UpdateIndividualServlet1", urlPatterns = {"/UpdateIndividualServlet1"})
public class UpdateIndividualServlet1 extends HttpServlet {

    //private static final long serialVersionUID = 1L;
    //private static final String ONTOLOGY_FILE_PATH = "C:/Programs/university-rdf-xml.owl"; // Change this to your actual path
    //private static final String ONTOLOGY_URI = "http://www.semanticweb.org/amal.elgammal/ontologies/2025/2/untitled-ontology-3#";
    private static final String ONTOLOGY_FILE_PATH = "C:/Programs/NARRATE-blueprints-rdf-xml.rdf";
    private static String ONTOLOGY_URI() { return OntologyReader.getNS(); }
//private static final String ONTOLOGY_URI = "http://www.semanticweb.org/amal.elgammal/ontologies/2025/3/untitled-ontology-31#";

    private OntologyReader ontologyReader;

    @Override
    public void init() throws ServletException {
        super.init();
        ontologyReader = OntologyReader.getInstance();
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

        try {
            // ✅ Parse JSON request
            JsonObject jsonRequest = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            String className = jsonRequest.has("className") ? jsonRequest.get("className").getAsString() : null;
            String individualName = jsonRequest.has("individualName") ? jsonRequest.get("individualName").getAsString() : null;

            if (className == null || individualName == null) {
                sendErrorResponse(out, gson, "❌ Missing className or individualName.");
                return;
            }

            // ✅ Load ontology model
            OntologyReader.reloadModel();  // reload singleton safely
            OntModel model = OntologyReader.getModel();  // get the updated shared model

            Individual individual = model.getIndividual(ONTOLOGY_URI() + individualName);

            if (individual == null) {
                sendErrorResponse(out, gson, "❌ Individual not found.");
                return;
            }

            // ✅ Read updated properties from JSON (support multiple values)
            Map<String, List<String>> dataProperties = new HashMap<>();
            Map<String, List<String>> objectProperties = new HashMap<>();

            if (jsonRequest.has("dataProperties")) {
                JsonObject dataPropsJson = jsonRequest.getAsJsonObject("dataProperties");
                for (Map.Entry<String, JsonElement> entry : dataPropsJson.entrySet()) {
                    List<String> values = new ArrayList<>();
                    if (entry.getValue().isJsonArray()) {
                        for (JsonElement v : entry.getValue().getAsJsonArray()) {
                            values.add(v.getAsString());
                        }
                    } else {
                        values.add(entry.getValue().getAsString());
                    }
                    dataProperties.put(entry.getKey(), values);
                }
            }

            if (jsonRequest.has("objectProperties")) {
                JsonObject objectPropsJson = jsonRequest.getAsJsonObject("objectProperties");
                for (Map.Entry<String, JsonElement> entry : objectPropsJson.entrySet()) {
                    List<String> values = new ArrayList<>();
                    if (entry.getValue().isJsonArray()) {
                        for (JsonElement v : entry.getValue().getAsJsonArray()) {
                            values.add(v.getAsString());
                        }
                    } else {
                        values.add(entry.getValue().getAsString());
                    }
                    objectProperties.put(entry.getKey(), values);
                }
            }

            // ✅ Update properties in ontology
            updateIndividualProperties(model, individual, className, dataProperties, objectProperties);

            // ✅ Save changes to ontology
            saveOntologyModel(model);

            // ✅ Send success response
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("status", "success");
            jsonResponse.put("message", "✅ Individual updated successfully.");
            jsonResponse.put("updatedDataProperties", dataProperties);
            jsonResponse.put("updatedObjectProperties", objectProperties);

            out.print(gson.toJson(jsonResponse));
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, gson, "❌ Error processing update request: " + e.getMessage());
        }
    }
// 🔹 Method to update data & object properties (supports multiple values)

    private void updateIndividualProperties(OntModel model, Individual individual,
            String className,
            Map<String, List<String>> dataProps,
            Map<String, List<String>> objectProps) {

        individual.removeAll(null);

        // 🔥 Immediately reassign rdf:type
        OntClass selectedClass = model.getOntClass(ONTOLOGY_URI() + className);
        if (selectedClass != null) {
            individual.addRDFType(selectedClass);
        }

        // Re-assign rdf:type owl:NamedIndividual
        Resource namedIndividualType = model.getResource(OWL.NS + "NamedIndividual");
        individual.addProperty(RDF.type, namedIndividualType);

        OntologyReader reader = OntologyReader.getInstance();

        Map<String, String> dataPropertyRanges = reader.getDataPropertyRanges(className);
        System.out.println("🔍 Data Property Ranges: " + dataPropertyRanges);

        for (Map.Entry<String, List<String>> entry : dataProps.entrySet()) {
            String propName = entry.getKey();
            List<String> values = entry.getValue();

            DatatypeProperty dataProp = model.getDatatypeProperty(ONTOLOGY_URI() + propName);
            if (dataProp != null) {
                Property genericProp = model.getProperty(ONTOLOGY_URI() + propName);
                model.removeAll(individual, genericProp, null);

                String rangeURI = dataPropertyRanges.get(propName);

                for (String val : values) {
                    try {
                        if (rangeURI != null) {
                            if (rangeURI.equals(XSD.integer.getURI())) {
                                individual.addLiteral(dataProp, model.createTypedLiteral(Integer.parseInt(val)));
                            } else if (rangeURI.equals(XSD.decimal.getURI())) {
                                individual.addLiteral(dataProp, model.createTypedLiteral(new BigDecimal(val)));
                            } else if (rangeURI.equals(XSD.xfloat.getURI())) {
                                individual.addLiteral(dataProp, model.createTypedLiteral(Float.parseFloat(val)));
                            } else if (rangeURI.equals(XSD.xdouble.getURI())) {
                                individual.addLiteral(dataProp, model.createTypedLiteral(Double.parseDouble(val)));
                            } else if (rangeURI.equals(XSD.xboolean.getURI())) {
                                individual.addLiteral(dataProp, model.createTypedLiteral(Boolean.parseBoolean(val)));
                            } else if (rangeURI.equals(XSD.date.getURI())) {
                                individual.addLiteral(dataProp, model.createTypedLiteral(val, XSD.date.getURI()));
                            } else if (rangeURI.equals(XSD.dateTime.getURI())) {
                                if (!val.contains("T")) {
                                    val += "T00:00:00";
                                }
                                individual.addLiteral(dataProp, model.createTypedLiteral(val, XSD.dateTime.getURI()));
                            } else if (rangeURI.equals(XSD.anyURI.getURI())) {
                                individual.addLiteral(dataProp, model.createTypedLiteral(val, XSD.anyURI.getURI()));
                            } else if (rangeURI.equals(XSD.xstring.getURI())) {
                                individual.addLiteral(dataProp, model.createTypedLiteral(val, XSD.xstring.getURI()));
                            } else {
                                individual.addLiteral(dataProp, model.createTypedLiteral(val, XSD.xstring.getURI()));
                            }
                        } else {
                            individual.addLiteral(dataProp, model.createTypedLiteral(val, XSD.xstring.getURI()));
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error saving data property '" + propName + "' with value '" + val + "'. Falling back to xsd:string.");
                        individual.addLiteral(dataProp, model.createTypedLiteral(val, XSD.xstring.getURI()));
                    }
                }
            }
        }

        if (objectProps.containsKey("type")) {
            List<String> types = objectProps.get("type");
            for (String typeName : types) {
                if (typeName != null && !typeName.isEmpty()) {
                    OntClass classType = model.getOntClass(ONTOLOGY_URI() + typeName);
                    if (classType != null) {
                        individual.addRDFType(classType);
                    }
                }
            }
            objectProps.remove("type");
        }

        for (Map.Entry<String, List<String>> entry : objectProps.entrySet()) {
            Property property = model.getProperty(ONTOLOGY_URI() + entry.getKey());
            if (property != null) {
                for (String objectValue : entry.getValue()) {
                    if (objectValue != null && !objectValue.isEmpty()) {
                        if (!objectValue.startsWith("http://") && !objectValue.startsWith("https://")) {
                            objectValue = ONTOLOGY_URI() + objectValue;
                        }
                        try {
                            new java.net.URI(objectValue);
                            Resource objResource = model.getResource(objectValue);
                            if (objResource != null) {
                                individual.addProperty(property, objResource);
                            }
                        } catch (java.net.URISyntaxException e) {
                            System.err.println("⚠ Invalid URI for object property: " + objectValue);
                        }
                    }
                }
            }
        }
    }

    // 🔹 Load Ontology Model
    private OntModel loadOntologyModel() {
        OntModel model = ModelFactory.createOntologyModel();
        try (InputStream in = FileManager.get().open(ONTOLOGY_FILE_PATH)) {
            if (in == null) {
                throw new IllegalArgumentException("❌ Ontology file not found: " + ONTOLOGY_FILE_PATH);
            }
            model.read(in, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    // 🔹 Save Ontology Model
    private void saveOntologyModel(OntModel model) {
        try (OutputStream out = new FileOutputStream(ONTOLOGY_FILE_PATH)) {
            model.write(out, "RDF/XML-ABBREV");
            ontologyReader.reloadModel(); // ✅ Reload after writing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Send Error Response
    private void sendErrorResponse(PrintWriter out, Gson gson, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        out.print(gson.toJson(errorResponse));
        out.flush();
    }

//response.setContentType("text/html;charset=UTF-8");

    /* try (PrintWriter out = response.getWriter()) {
         
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet UpdateIndividualServlet1</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet UpdateIndividualServlet1 at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }*/
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
