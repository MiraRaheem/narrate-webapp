
package com.example.ontology;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
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
        JsonObject json = new JsonObject();

        try {
            IndividualData data = gson.fromJson(request.getReader(), IndividualData.class);

            if (data == null || data.getIndividualName() == null || data.getClassName() == null) {
                json.addProperty("status", "error");
                json.addProperty("message", "Invalid input.");
                response.getWriter().print(json);
                return;
            }

            synchronized (AddIndividualServlet2.class) {

                OntologyReader.reloadModel();
                OntModel model = OntologyReader.getModel();

                System.out.println("📂 Using ontology path: " + ONTOLOGY_PATH);

                // Duplicate check
                if (model.getIndividual(NAMESPACE + data.getIndividualName()) != null) {
                    json.addProperty("status", "error");
                    json.addProperty("message", "Individual already exists.");
                    response.getWriter().print(json);
                    return;
                }

                // Class check
                OntClass ontClass = model.getOntClass(NAMESPACE + data.getClassName());
                if (ontClass == null) {
                    json.addProperty("status", "error");
                    json.addProperty("message", "Class not found.");
                    response.getWriter().print(json);
                    return;
                }

                // Create individual
                Individual individual = model.createIndividual(
                        NAMESPACE + data.getIndividualName(),
                        ontClass
                );
                individual.addRDFType(OWL.NamedIndividual);

                List<DataPropertyEntry> dataProps =
                        data.getDataProperties() != null ? data.getDataProperties() : Collections.emptyList();

                List<ObjectPropertyEntry> objectProps =
                        data.getObjectProperties() != null ? data.getObjectProperties() : Collections.emptyList();

                // Data properties
                for (DataPropertyEntry dp : dataProps) {

                    DatatypeProperty property = model.getDatatypeProperty(NAMESPACE + dp.getProperty());
                    if (property == null) {
                        System.err.println("❌ Missing property: " + dp.getProperty());
                        continue;
                    }

                    String raw = dp.getValue();
                    String range = property.getRange() != null ? property.getRange().getURI() : null;

                    try {
                        if (range != null) {

                            if (range.equals(XSD.integer.getURI())) {
                                individual.addProperty(property, model.createTypedLiteral(Integer.parseInt(raw)));

                            } else if (range.equals(XSD.decimal.getURI())) {
                                individual.addProperty(property, model.createTypedLiteral(new BigDecimal(raw)));

                            } else if (range.equals(XSD.xfloat.getURI())) {
                                individual.addProperty(property, model.createTypedLiteral(Float.parseFloat(raw)));

                            } else if (range.equals(XSD.xdouble.getURI())) {
                                individual.addProperty(property, model.createTypedLiteral(Double.parseDouble(raw)));

                            } else if (range.equals(XSD.xboolean.getURI())) {
                                individual.addProperty(property, model.createTypedLiteral(Boolean.parseBoolean(raw)));

                            } else if (range.equals(XSD.date.getURI())) {
                                individual.addProperty(property, model.createTypedLiteral(raw, XSD.date.getURI()));

                            } else if (range.equals(XSD.dateTime.getURI())) {
                                if (!raw.contains("T")) raw += "T00:00:00";
                                individual.addProperty(property, model.createTypedLiteral(raw, XSD.dateTime.getURI()));

                            } else if (range.equals(XSD_DATETIMESTAMP)) {
                                if (!raw.contains("T")) raw += "T00:00:00Z";
                                else if (!raw.endsWith("Z") && !raw.matches(".*[+-]\\d{2}:\\d{2}$")) raw += "Z";

                                individual.addProperty(property,
                                        model.createTypedLiteral(raw, XSD_DATETIMESTAMP));

                            } else if (range.equals(XSD.anyURI.getURI())) {
                                individual.addProperty(property,
                                        model.createTypedLiteral(raw, XSD.anyURI.getURI()));

                            } else {
                                individual.addProperty(property, guessTypedLiteral(model, raw));
                            }

                        } else {
                            individual.addProperty(property,
                                    model.createTypedLiteral(raw, XSD.xstring.getURI()));
                        }

                    } catch (Exception e) {
                        individual.addProperty(property, model.createTypedLiteral(raw));
                    }
                }

                // Object properties + inverse handling
                for (ObjectPropertyEntry op : objectProps) {

                    ObjectProperty property = model.getObjectProperty(NAMESPACE + op.getProperty());
                    Individual related = model.getIndividual(NAMESPACE + op.getValue());

                    if (property == null || related == null) {
                        System.err.println("❌ Invalid object property: " + op.getProperty());
                        continue;
                    }

                    individual.addProperty(property, related);
                    addInverseProperties(model, property, individual, related);
                }

                // Safe write
                File temp = new File(ONTOLOGY_PATH + ".tmp");
                try (FileOutputStream out = new FileOutputStream(temp)) {
                    model.write(out, "RDF/XML");
                }

                File finalFile = new File(ONTOLOGY_PATH);
                if (!temp.renameTo(finalFile)) {
                    throw new IOException("Failed to replace ontology file.");
                }

                OntologyReader.reloadModel();

                json.addProperty("status", "success");
                json.addProperty("uri", NAMESPACE + data.getIndividualName());
                response.getWriter().print(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
            json.addProperty("status", "error");
            json.addProperty("message", e.getMessage());
            response.getWriter().print(json);
        }
    }

    private void addInverseProperties(OntModel model,
                                      ObjectProperty property,
                                      Individual subject,
                                      Individual object) {

        StmtIterator direct = model.listStatements(property, OWL.inverseOf, (RDFNode) null);
        while (direct.hasNext()) {
            RDFNode node = direct.nextStatement().getObject();
            if (node.isResource()) {
                ObjectProperty inv = model.getObjectProperty(node.asResource().getURI());
                if (inv != null && !object.hasProperty(inv, subject)) {
                    object.addProperty(inv, subject);
                }
            }
        }

        StmtIterator reverse = model.listStatements(null, OWL.inverseOf, property);
        while (reverse.hasNext()) {
            if (reverse.nextStatement().getSubject().canAs(ObjectProperty.class)) {
                ObjectProperty inv = reverse.nextStatement().getSubject().as(ObjectProperty.class);
                if (inv != null && !object.hasProperty(inv, subject)) {
                    object.addProperty(inv, subject);
                }
            }
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

