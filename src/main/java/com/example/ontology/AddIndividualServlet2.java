/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.example.ontology;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import com.example.ontology.OntologyReader;
import com.google.gson.JsonObject;
import org.apache.jena.rdf.model.Literal;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Collections;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;

/**
 *
 * @author amal.elgammal
 */
@WebServlet(name = "AddIndividualServlet2", urlPatterns = {"/AddIndividualServlet2"})
public class AddIndividualServlet2 extends HttpServlet {

    //private static final String ONTOLOGY_PATH = "C:/Programs/university-rdf-xml.owl"; // Change this to your actual path
    //private static final String NAMESPACE = "http://www.semanticweb.org/amal.elgammal/ontologies/2025/2/untitled-ontology-3#";
    //private static final Resource NAMED_INDIVIDUAL = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#NamedIndividual");
    private static final String ONTOLOGY_PATH =System.getenv().getOrDefault("ONTOLOGY_PATH", "/data/NARRATE-blueprints-rdf-xml.rdf");
    private static final String NAMESPACE = "http://www.semanticweb.org/amal.elgammal/ontologies/2025/3/untitled-ontology-31#";

    private OntologyReader ontologyReader;

    @Override
    public void init() throws ServletException {
        super.init();
        //ontologyReader = OntologyReader.getInstance();
        //OntologyReader.reloadModel();
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
        //processRequest(request, response);

        // Read JSON data from request
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        IndividualData data = gson.fromJson(reader, IndividualData.class);

        // Load ontology model
        OntologyReader.reloadModel();              // Load the shared model
        OntModel model = OntologyReader.getModel(); // Use the already-loaded one

        // ✅ Uniqueness check — must come AFTER loading the model and parsing JSON
        Individual existing = model.getIndividual(NAMESPACE + data.getIndividualName());
        if (existing != null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("status", "error");
            errorJson.addProperty("message", "❌ Individual already exists. Please choose a different name.");
            response.getWriter().print(errorJson.toString());
            return;
        }

        // Get the selected class
        OntClass ontClass = model.getOntClass(NAMESPACE + data.getClassName());
        if (ontClass == null) {
            response.getWriter().write("Class not found in ontology.");
            return;
        }

        // Create the new individual
        Individual individual = model.createIndividual(NAMESPACE + data.getIndividualName(), ontClass);
        individual.addRDFType(OWL.NamedIndividual);

// Process Data Properties
        /*for (DataPropertyEntry dp : data.getDataProperties()) {
            DatatypeProperty property = model.getDatatypeProperty(NAMESPACE + dp.getProperty());
            if (property != null) {
                individual.addProperty(property, model.createTypedLiteral(dp.getValue()));
            }
        }*/
        // Process Data Properties
        // Process Data Properties
        for (DataPropertyEntry dp : data.getDataProperties()) {
            DatatypeProperty property = model.getDatatypeProperty(NAMESPACE + dp.getProperty());
            if (property != null) {
                String rawValue = dp.getValue();
                String rangeURI = property.getRange() != null ? property.getRange().getURI() : null;

                System.out.println("🔍 Processing Data Property: " + dp.getProperty());
                System.out.println("   ↪ Raw Value: " + rawValue);
                System.out.println("   ↪ Range URI: " + rangeURI);

                try {
                    if (rangeURI != null) {
                        if (rangeURI.equals(XSD.integer.getURI())) {
                            int intValue = Integer.parseInt(rawValue);
                            System.out.println("   ✅ Parsed Integer: " + intValue);
                            individual.addProperty(property, model.createTypedLiteral(intValue));
                        } else if (rangeURI.equals(XSD.decimal.getURI())) {
                            BigDecimal decimalValue = new BigDecimal(rawValue);
                            System.out.println("   ✅ Parsed Decimal: " + decimalValue);
                            individual.addProperty(property, model.createTypedLiteral(decimalValue));
                        } else if (rangeURI.equals(XSD.xfloat.getURI())) {
                            float floatValue = Float.parseFloat(rawValue);
                            System.out.println("   ✅ Parsed Float: " + floatValue);
                            individual.addProperty(property, model.createTypedLiteral(floatValue));
                        } else if (rangeURI.equals(XSD.xdouble.getURI())) {
                            double doubleValue = Double.parseDouble(rawValue);
                            System.out.println("   ✅ Parsed Double: " + doubleValue);
                            individual.addProperty(property, model.createTypedLiteral(doubleValue));
                        } else if (rangeURI.equals(XSD.xboolean.getURI())) {
                            boolean boolValue = Boolean.parseBoolean(rawValue);
                            System.out.println("   ✅ Parsed Boolean: " + boolValue);
                            individual.addProperty(property, model.createTypedLiteral(boolValue));
                        } else if (rangeURI.equals(XSD.date.getURI())) {
                            System.out.println("   📅 Handling Date (xsd:date): " + rawValue);
                            individual.addProperty(property, model.createTypedLiteral(rawValue, XSD.date.getURI()));
                        } else if (rangeURI.equals(XSD.dateTime.getURI())) {
                            // Ensure proper xsd:dateTime format
                            if (!rawValue.contains("T")) {
                                rawValue += "T00:00:00";
                            }
                            System.out.println("   ⏰ Handling DateTime (fixed format): " + rawValue);
                            individual.addProperty(property, model.createTypedLiteral(rawValue, XSD.dateTime.getURI()));
                        } else if (rangeURI.equals(XSD.anyURI.getURI())) {
                            System.out.println("   🌐 Handling URI: " + rawValue);
                            individual.addProperty(property, model.createTypedLiteral(rawValue, XSD.anyURI.getURI()));
                        } else if (rangeURI.equals(XSD.xstring.getURI())) {
                            System.out.println("   ✏️ Handling xsd:string explicitly: " + rawValue);
                            individual.addProperty(property, model.createTypedLiteral(rawValue, XSD.xstring.getURI()));
                        } else {
                            System.out.println("   ⚠ Unknown range, trying to infer type...");
                            individual.addProperty(property, guessTypedLiteral(model, rawValue));
                        }
                    } else {
                        // 🚫 Don't guess — treat raw value as plain xsd:string
                        System.out.println("   ✏️ Treating as xsd:string explicitly");
                        individual.addProperty(property, model.createTypedLiteral(rawValue, XSD.xstring.getURI()));
                    }

                } catch (Exception e) {
                    System.err.println("   ❌ Error parsing value '" + rawValue + "'. Using string fallback.");
                    individual.addProperty(property, model.createTypedLiteral(rawValue));
                }
            } else {
                System.err.println("   ❌ Property not found in ontology: " + dp.getProperty());
            }
        }

        // Process Object Properties
        for (ObjectPropertyEntry op : data.getObjectProperties()) {
            ObjectProperty property = model.getObjectProperty(NAMESPACE + op.getProperty());
            Individual relatedIndividual = model.getIndividual(NAMESPACE + op.getValue());
            if (property != null && relatedIndividual != null) {
                individual.addProperty(property, relatedIndividual);
            }
        }

        // Save the updated ontology
        try (FileOutputStream outputStream = new FileOutputStream(ONTOLOGY_PATH)) {
            model.write(outputStream, "RDF/XML");
            OntologyReader.reloadModel();  // ✅ <-- Add this line here
        } catch (Exception e) {
            response.getWriter().write("Error saving ontology: " + e.getMessage());
            return;
        }

        response.getWriter().write("Individual added successfully!");
    }

    private Literal guessTypedLiteral(OntModel model, String value) {
        try {
            if (value.matches("^-?\\d+$")) {
                return model.createTypedLiteral(Integer.parseInt(value));
            } else if (value.matches("^-?\\d*\\.\\d+$")) {
                return model.createTypedLiteral(new BigDecimal(value));
            } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return model.createTypedLiteral(Boolean.parseBoolean(value));
            }
        } catch (Exception ignored) {
        }
        return model.createTypedLiteral(value); // fallback to string
    }

// Helper classes to handle JSON parsing
    private static class IndividualData {

        private String className;
        private String individualName;
        private List<DataPropertyEntry> dataProperties;
        private List<ObjectPropertyEntry> objectProperties;

        public String getClassName() {
            return className;
        }

        public String getIndividualName() {
            return individualName;
        }

        public List<DataPropertyEntry> getDataProperties() {
            return dataProperties;
        }

        public List<ObjectPropertyEntry> getObjectProperties() {
            return objectProperties;
        }
    }

    private static class DataPropertyEntry {

        private String property;
        private String value;

        public String getProperty() {
            return property;
        }

        public String getValue() {
            return value;
        }
    }

    private static class ObjectPropertyEntry {

        private String property;
        private String value;

        public String getProperty() {
            return property;
        }

        public String getValue() {
            return value;
        }
    }

}
