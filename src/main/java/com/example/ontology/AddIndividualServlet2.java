```java
package com.example.ontology;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "AddIndividualServlet2", urlPatterns = {"/AddIndividualServlet2"})
public class AddIndividualServlet2 extends HttpServlet {

    // ✅ Docker-safe path
    private static final String ONTOLOGY_PATH =
            System.getenv().getOrDefault(
                    "ONTOLOGY_PATH",
                    "/data/NARRATE-blueprints-rdf-xml.rdf"
            );

    private static final String NAMESPACE =
            "http://www.semanticweb.org/amal.elgammal/ontologies/2025/3/untitled-ontology-31#";

    private static final String XSD_DATETIMESTAMP =
            "http://www.w3.org/2001/XMLSchema#dateTimeStamp";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            IndividualData data = gson.fromJson(reader, IndividualData.class);

            if (data == null || data.getIndividualName() == null || data.getClassName() == null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid input data.");
                response.getWriter().print(jsonResponse);
                return;
            }

            synchronized (AddIndividualServlet2.class) {

                // Load model safely
                OntologyReader.reloadModel();
                OntModel model = OntologyReader.getModel();

                System.out.println("📂 Using ontology path: " + ONTOLOGY_PATH);

                // Check duplicate
                if (model.getIndividual(NAMESPACE + data.getIndividualName()) != null) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Individual already exists.");
                    response.getWriter().print(jsonResponse);
                    return;
                }

                // Get class
                OntClass ontClass = model.getOntClass(NAMESPACE + data.getClassName());
                if (ontClass == null) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Class not found.");
                    response.getWriter().print(jsonResponse);
                    return;
                }

                // Create individual
                Individual individual = model.createIndividual(
                        NAMESPACE + data.getIndividualName(),
                        ontClass
                );
                individual.addRDFType(OWL.NamedIndividual);

                // Safe lists
                List<DataPropertyEntry> dataProps =
                        data.getDataProperties() != null ? data.getDataProperties() : Collections.emptyList();

                List<ObjectPropertyEntry> objectProps =
                        data.getObjectProperties() != null ? data.getObjectProperties() : Collections.emptyList();

                // Process Data Properties
                for (DataPropertyEntry dp : dataProps) {

                    DatatypeProperty property =
                            model.getDatatypeProperty(NAMESPACE + dp.getProperty());

                    if (property == null) {
                        System.err.println("❌ Property not found: " + dp.getProperty());
                        continue;
                    }

                    String rawValue = dp.getValue();
                    String rangeURI = property.getRange() != null
                            ? property.getRange().getURI()
                            : null;

                    try {
                        if (rangeURI != null) {

                            if (rangeURI.equals(XSD.integer.getURI())) {
                                individual.addProperty(property,
                                        model.createTypedLiteral(Integer.parseInt(rawValue)));

                            } else if (rangeURI.equals(XSD.decimal.getURI())) {
                                individual.addProperty(property,
                                        model.createTypedLiteral(new BigDecimal(rawValue)));

                            } else if (rangeURI.equals(XSD.xfloat.getURI())) {
                                individual.addProperty(property,
                                        model.createTypedLiteral(Float.parseFloat(rawValue)));

                            } else if (rangeURI.equals(XSD.xdouble.getURI())) {
                                individual.addProperty(property,
                                        model.createTypedLiteral(Double.parseDouble(rawValue)));

                            } else if (rangeURI.equals(XSD.xboolean.getURI())) {
                                individual.addProperty(property,
                                        model.createTypedLiteral(Boolean.parseBoolean(rawValue)));

                            } else if (rangeURI.equals(XSD.date.getURI())) {
                                individual.addProperty(property,
                                        model.createTypedLiteral(rawValue, XSD.date.getURI()));

                            } else if (rangeURI.equals(XSD.dateTime.getURI())) {

                                if (!rawValue.contains("T")) {
                                    rawValue += "T00:00:00";
                                }

                                individual.addProperty(property,
                                        model.createTypedLiteral(rawValue, XSD.dateTime.getURI()));

                            } else if (rangeURI.equals(XSD_DATETIMESTAMP)) {

                                if (!rawValue.contains("T")) {
                                    rawValue += "T00:00:00Z";
                                } else if (!rawValue.endsWith("Z") &&
                                        !rawValue.matches(".*[+-]\\d{2}:\\d{2}$")) {
                                    rawValue += "Z";
                                }

                                individual.addProperty(property,
                                        model.createTypedLiteral(rawValue, XSD_DATETIMESTAMP));

                            } else if (rangeURI.equals(XSD.anyURI.getURI())) {
                                individual.addProperty(property,
                                        model.createTypedLiteral(rawValue, XSD.anyURI.getURI()));

                            } else {
                                individual.addProperty(property,
                                        guessTypedLiteral(model, rawValue));
                            }

                        } else {
                            individual.addProperty(property,
                                    model.createTypedLiteral(rawValue, XSD.xstring.getURI()));
                        }

                    } catch (Exception e) {
                        System.err.println("⚠ Failed parsing: " + rawValue);
                        individual.addProperty(property,
                                model.createTypedLiteral(rawValue));
                    }
                }

                // Process Object Properties
                for (ObjectPropertyEntry op : objectProps) {

                    ObjectProperty property =
                            model.getObjectProperty(NAMESPACE + op.getProperty());

                    Individual related =
                            model.getIndividual(NAMESPACE + op.getValue());

                    if (property == null) {
                        System.err.println("❌ Object property not found: " + op.getProperty());
                        continue;
                    }

                    if (related == null) {
                        System.err.println("❌ Related individual not found: " + op.getValue());
                        continue;
                    }

                    individual.addProperty(property, related);
                }

                // Safe file write
                File tempFile = new File(ONTOLOGY_PATH + ".tmp");

                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    model.write(out, "RDF/XML");
                }

                File finalFile = new File(ONTOLOGY_PATH);
                if (!tempFile.renameTo(finalFile)) {
                    throw new IOException("Failed to replace ontology file.");
                }

                OntologyReader.reloadModel();

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("uri", NAMESPACE + data.getIndividualName());
                response.getWriter().print(jsonResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
            response.getWriter().print(jsonResponse);
        }
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
        } catch (Exception ignored) {}
        return model.createTypedLiteral(value);
    }

    private static class IndividualData {
        private String className;
        private String individualName;
        private List<DataPropertyEntry> dataProperties;
        private List<ObjectPropertyEntry> objectProperties;

        public String getClassName() { return className; }
        public String getIndividualName() { return individualName; }
        public List<DataPropertyEntry> getDataProperties() { return dataProperties; }
        public List<ObjectPropertyEntry> getObjectProperties() { return objectProperties; }
    }

    private static class DataPropertyEntry {
        private String property;
        private String value;

        public String getProperty() { return property; }
        public String getValue() { return value; }
    }

    private static class ObjectPropertyEntry {
        private String property;
        private String value;

        public String getProperty() { return property; }
        public String getValue() { return value; }
    }
}
```
