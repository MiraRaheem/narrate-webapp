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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "AddIndividualServlet2", urlPatterns = {"/AddIndividualServlet2"})
public class AddIndividualServlet2 extends HttpServlet {

    // =========================================================
    // ONTOLOGY CONFIGURATION
    // =========================================================

    // Docker-safe path
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
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        try {

            // =====================================================
            // STEP 1: READ REQUEST
            // =====================================================

            BufferedReader reader = request.getReader();

            IndividualData data =
                    gson.fromJson(reader, IndividualData.class);

            // =====================================================
            // STEP 2: VALIDATE INPUT
            // =====================================================

            if (data == null
                    || data.getIndividualName() == null
                    || data.getClassName() == null
                    || data.getIndividualName().trim().isEmpty()
                    || data.getClassName().trim().isEmpty()) {

                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty(
                        "message",
                        "Invalid input data."
                );

                response.getWriter().print(jsonResponse);
                return;
            }


            // =====================================================
            // STEP 3: ACQUIRE ONTOLOGY WRITE LOCK
            // =====================================================
            //
            // IMPORTANT:
            // Every operation that modifies the shared ontology
            // must use the SAME write lock.
            //
            // This prevents:
            //
            // GET/READ
            //        ↓
            // WRITE
            //        ↓
            // ANOTHER WRITE
            //
            // from modifying the model/file simultaneously.
            //
            OntologyReader.MODEL_LOCK.writeLock().lock();

            try {

                // =================================================
                // STEP 4: RELOAD BEFORE MODIFYING
                // =================================================
                //
                // This ensures that if another successful operation
                // modified the file before this request obtained
                // the lock, we work from the latest ontology.
                //
                OntologyReader.reloadModel();

                OntModel model =
                        OntologyReader.getModel();


                // =================================================
                // STEP 5: CHECK DUPLICATE
                // =================================================

                String individualURI =
                        NAMESPACE + data.getIndividualName();

                if (model.getIndividual(individualURI) != null) {

                    jsonResponse.addProperty(
                            "status",
                            "error"
                    );

                    jsonResponse.addProperty(
                            "message",
                            "Individual already exists."
                    );

                    response.getWriter().print(jsonResponse);
                    return;
                }


                // =================================================
                // STEP 6: FIND CLASS
                // =================================================

                String classURI =
                        NAMESPACE + data.getClassName();

                OntClass ontClass =
                        model.getOntClass(classURI);

                if (ontClass == null) {

                    jsonResponse.addProperty(
                            "status",
                            "error"
                    );

                    jsonResponse.addProperty(
                            "message",
                            "Class not found."
                    );

                    response.getWriter().print(jsonResponse);
                    return;
                }


                // =================================================
                // STEP 7: CREATE INDIVIDUAL
                // =================================================

                Individual individual =
                        model.createIndividual(
                                individualURI,
                                ontClass
                        );

                individual.addRDFType(
                        OWL.NamedIndividual
                );


                // =================================================
                // STEP 8: SAFE PROPERTY LISTS
                // =================================================

                List<DataPropertyEntry> dataProps =
                        data.getDataProperties() != null
                                ? data.getDataProperties()
                                : Collections.emptyList();

                List<ObjectPropertyEntry> objectProps =
                        data.getObjectProperties() != null
                                ? data.getObjectProperties()
                                : Collections.emptyList();


                // =================================================
                // STEP 9: DATA PROPERTIES
                // =================================================

                for (DataPropertyEntry dp : dataProps) {

                    if (dp == null
                            || dp.getProperty() == null
                            || dp.getValue() == null) {
                        continue;
                    }

                    DatatypeProperty property =
                            model.getDatatypeProperty(
                                    NAMESPACE + dp.getProperty()
                            );

                    if (property == null) {
                        continue;
                    }

                    String rawValue =
                            dp.getValue();

                    String rangeURI =
                            property.getRange() != null
                                    ? property.getRange().getURI()
                                    : null;

                    try {

                        if (rangeURI != null) {

                            // -------------------------------------
                            // INTEGER
                            // -------------------------------------

                            if (rangeURI.equals(
                                    XSD.integer.getURI())) {

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                Integer.parseInt(rawValue)
                                        )
                                );

                            // -------------------------------------
                            // DECIMAL
                            // -------------------------------------

                            } else if (rangeURI.equals(
                                    XSD.decimal.getURI())) {

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                new BigDecimal(rawValue)
                                        )
                                );

                            // -------------------------------------
                            // FLOAT
                            // -------------------------------------

                            } else if (rangeURI.equals(
                                    XSD.xfloat.getURI())) {

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                Float.parseFloat(rawValue)
                                        )
                                );

                            // -------------------------------------
                            // DOUBLE
                            // -------------------------------------

                            } else if (rangeURI.equals(
                                    XSD.xdouble.getURI())) {

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                Double.parseDouble(rawValue)
                                        )
                                );

                            // -------------------------------------
                            // BOOLEAN
                            // -------------------------------------

                            } else if (rangeURI.equals(
                                    XSD.xboolean.getURI())) {

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                Boolean.parseBoolean(rawValue)
                                        )
                                );

                            // -------------------------------------
                            // DATE
                            // -------------------------------------

                            } else if (rangeURI.equals(
                                    XSD.date.getURI())) {

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                rawValue,
                                                XSD.date.getURI()
                                        )
                                );

                            // -------------------------------------
                            // DATETIME
                            // -------------------------------------

                            } else if (rangeURI.equals(
                                    XSD.dateTime.getURI())) {

                                if (!rawValue.contains("T")) {
                                    rawValue += "T00:00:00";
                                }

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                rawValue,
                                                XSD.dateTime.getURI()
                                        )
                                );

                            // -------------------------------------
                            // DATETIME STAMP
                            // -------------------------------------

                            } else if (rangeURI.equals(
                                    XSD_DATETIMESTAMP)) {

                                if (!rawValue.contains("T")) {

                                    rawValue +=
                                            "T00:00:00Z";

                                } else if (
                                        !rawValue.endsWith("Z")
                                                && !rawValue.matches(
                                                ".*[+-]\\d{2}:\\d{2}$"
                                        )) {

                                    rawValue += "Z";
                                }

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                rawValue,
                                                XSD_DATETIMESTAMP
                                        )
                                );

                            // -------------------------------------
                            // URI
                            // -------------------------------------

                            } else if (rangeURI.equals(
                                    XSD.anyURI.getURI())) {

                                individual.addProperty(
                                        property,
                                        model.createTypedLiteral(
                                                rawValue,
                                                XSD.anyURI.getURI()
                                        )
                                );

                            // -------------------------------------
                            // OTHER TYPES
                            // -------------------------------------

                            } else {

                                individual.addProperty(
                                        property,
                                        guessTypedLiteral(
                                                model,
                                                rawValue
                                        )
                                );
                            }

                        } else {

                            // No range → treat as string

                            individual.addProperty(
                                    property,
                                    model.createTypedLiteral(
                                            rawValue,
                                            XSD.xstring.getURI()
                                    )
                            );
                        }

                    } catch (Exception e) {

                        // Fallback to normal literal

                        individual.addProperty(
                                property,
                                model.createTypedLiteral(
                                        rawValue
                                )
                        );
                    }
                }


                // =================================================
                // STEP 10: OBJECT PROPERTIES
                // =================================================

                for (ObjectPropertyEntry op : objectProps) {

                    if (op == null
                            || op.getProperty() == null
                            || op.getValue() == null) {
                        continue;
                    }

                    ObjectProperty property =
                            model.getObjectProperty(
                                    NAMESPACE + op.getProperty()
                            );

                    Individual related =
                            model.getIndividual(
                                    NAMESPACE + op.getValue()
                            );

                    if (property != null
                            && related != null) {

                        individual.addProperty(
                                property,
                                related
                        );
                    }
                }


                // =================================================
                // STEP 11: WRITE TO TEMP FILE
                // =================================================
                //
                // DO NOT write directly to the real ontology file.
                //
                // First create:
                //
                // /data/NARRATE-blueprints-rdf-xml.rdf.tmp
                //
                // Then replace the real file atomically.
                //

                File tempFile =
                        new File(
                                ONTOLOGY_PATH + ".tmp"
                        );

                try (FileOutputStream out =
                             new FileOutputStream(tempFile)) {

                    model.write(
                            out,
                            "RDF/XML"
                    );
                }


                // =================================================
                // STEP 12: REPLACE REAL ONTOLOGY FILE
                // =================================================

                File finalFile =
                        new File(ONTOLOGY_PATH);

                System.out.println(
                        "=== DEBUG ONTOLOGY WRITE ==="
                );

                System.out.println(
                        "Path: "
                                + finalFile.getAbsolutePath()
                );

                System.out.println(
                        "Exists: "
                                + finalFile.exists()
                );

                System.out.println(
                        "Writable: "
                                + finalFile.canWrite()
                );

                System.out.println(
                        "Size BEFORE: "
                                + finalFile.length()
                );


                Files.move(
                        tempFile.toPath(),
                        finalFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );


                System.out.println(
                        "Size AFTER: "
                                + finalFile.length()
                );

                System.out.println(
                        "==== END DEBUG ===="
                );


                // =================================================
                // STEP 13: RELOAD SHARED MODEL
                // =================================================
                //
                // This happens while the WRITE LOCK is still held.
                //
                // Therefore no other writer can modify the ontology
                // while this model is being replaced/reloaded.
                //
                OntologyReader.reloadModel();


                // =================================================
                // STEP 14: SUCCESS RESPONSE
                // =================================================

                jsonResponse.addProperty(
                        "status",
                        "success"
                );

                jsonResponse.addProperty(
                        "uri",
                        individualURI
                );

                response.getWriter().print(
                        jsonResponse
                );


            } finally {

                // =================================================
                // STEP 15: ALWAYS RELEASE WRITE LOCK
                // =================================================
                //
                // This is extremely important.
                //
                // Even if:
                //
                // - ontology writing fails
                // - reload fails
                // - duplicate is found
                // - class is missing
                // - unexpected exception occurs
                //
                // the lock MUST be released.
                //

                OntologyReader.MODEL_LOCK
                        .writeLock()
                        .unlock();
            }


        } catch (Exception e) {

            e.printStackTrace();

            jsonResponse.addProperty(
                    "status",
                    "error"
            );

            jsonResponse.addProperty(
                    "message",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Unknown error"
            );

            response.getWriter().print(
                    jsonResponse
            );
        }
    }


    // =============================================================
    // TYPED LITERAL HELPER
    // =============================================================

    private Literal guessTypedLiteral(
            OntModel model,
            String value) {

        try {

            if (value.matches("^-?\\d+$")) {

                return model.createTypedLiteral(
                        Integer.parseInt(value)
                );

            } else if (value.matches(
                    "^-?\\d*\\.\\d+$")) {

                return model.createTypedLiteral(
                        new BigDecimal(value)
                );

            } else if (
                    value.equalsIgnoreCase("true")
                            || value.equalsIgnoreCase("false")) {

                return model.createTypedLiteral(
                        Boolean.parseBoolean(value)
                );
            }

        } catch (Exception ignored) {
        }

        return model.createTypedLiteral(value);
    }


    // =============================================================
    // DTO CLASSES
    // =============================================================

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
