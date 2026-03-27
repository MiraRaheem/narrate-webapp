
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

@WebServlet(name = "UpdateIndividualServlet1", urlPatterns = {"/UpdateIndividualServlet1"})
public class UpdateIndividualServlet1 extends HttpServlet {

    // ✅ Docker-safe path ONLY change
    private static final String ONTOLOGY_FILE_PATH =
        System.getenv().getOrDefault(
            "ONTOLOGY_PATH",
            "/data/NARRATE-blueprints-rdf-xml.rdf"
        );

    private static final String ONTOLOGY_URI = "http://www.semanticweb.org/amal.elgammal/ontologies/2025/3/untitled-ontology-31#";
    private static final String XSD_DATETIMESTAMP = "http://www.w3.org/2001/XMLSchema#dateTimeStamp";

    private OntologyReader ontologyReader;

    @Override
    public void init() throws ServletException {
        super.init();
        ontologyReader = OntologyReader.getInstance();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try {
            JsonObject jsonRequest = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            String className = jsonRequest.has("className") ? jsonRequest.get("className").getAsString() : null;
            String individualName = jsonRequest.has("individualName") ? jsonRequest.get("individualName").getAsString() : null;

            if (className == null || individualName == null) {
                sendErrorResponse(out, gson, "❌ Missing className or individualName.");
                return;
            }

            OntologyReader.reloadModel();
            OntModel model = OntologyReader.getModel();

            Individual individual = model.getIndividual(ONTOLOGY_URI + individualName);

            if (individual == null) {
                sendErrorResponse(out, gson, "❌ Individual not found.");
                return;
            }

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

            updateIndividualProperties(model, individual, className, dataProperties, objectProperties);

            saveOntologyModel(model);

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

    private void updateIndividualProperties(OntModel model, Individual individual,
            String className,
            Map<String, List<String>> dataProps,
            Map<String, List<String>> objectProps) {

        individual.removeAll(null);

        OntClass selectedClass = model.getOntClass(ONTOLOGY_URI + className);
        if (selectedClass != null) {
            individual.addRDFType(selectedClass);
        }

        Resource namedIndividualType = model.getResource(OWL.NS + "NamedIndividual");
        individual.addProperty(RDF.type, namedIndividualType);

        OntologyReader reader = OntologyReader.getInstance();
        Map<String, String> dataPropertyRanges = reader.getDataPropertyRanges(className);

        for (Map.Entry<String, List<String>> entry : dataProps.entrySet()) {
            String propName = entry.getKey();
            List<String> values = entry.getValue();

            DatatypeProperty dataProp = model.getDatatypeProperty(ONTOLOGY_URI + propName);
            if (dataProp != null) {
                Property genericProp = model.getProperty(ONTOLOGY_URI + propName);
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
                            } else if (rangeURI.equals(XSD_DATETIMESTAMP)) {

                                if (!val.contains("T")) {
                                    val += "T00:00:00Z";
                                } else {
                                    if (val.length() == 16) {
                                        val += ":00";
                                    }
                                    if (!val.endsWith("Z") && !val.matches(".*[+-]\\d{2}:\\d{2}$")) {
                                        val += "Z";
                                    }
                                }

                                individual.addLiteral(dataProp,
                                        model.createTypedLiteral(val, XSD_DATETIMESTAMP));

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
                        individual.addLiteral(dataProp, model.createTypedLiteral(val, XSD.xstring.getURI()));
                    }
                }
            }
        }

        if (objectProps.containsKey("type")) {
            List<String> types = objectProps.get("type");
            for (String typeName : types) {
                if (typeName != null && !typeName.isEmpty()) {
                    OntClass classType = model.getOntClass(ONTOLOGY_URI + typeName);
                    if (classType != null) {
                        individual.addRDFType(classType);
                    }
                }
            }
            objectProps.remove("type");
        }

        for (Map.Entry<String, List<String>> entry : objectProps.entrySet()) {
            Property property = model.getProperty(ONTOLOGY_URI + entry.getKey());
            if (property != null) {
                for (String objectValue : entry.getValue()) {
                    if (objectValue != null && !objectValue.isEmpty()) {
                        if (!objectValue.startsWith("http://") && !objectValue.startsWith("https://")) {
                            objectValue = ONTOLOGY_URI + objectValue;
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

    private void saveOntologyModel(OntModel model) {
        try (OutputStream out = new FileOutputStream(ONTOLOGY_FILE_PATH)) {
            model.write(out, "RDF/XML-ABBREV");
            ontologyReader.reloadModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendErrorResponse(PrintWriter out, Gson gson, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        out.print(gson.toJson(errorResponse));
        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
