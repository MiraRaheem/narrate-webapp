
package com.example.ontology;

import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.jena.vocabulary.OWL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.ontology.Individual;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import java.math.BigDecimal;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
/**
 *
 * @author amal.elgammal
 */

public class OntologyReader {

private <T> T withReadLock(java.util.function.Supplier<T> action) {
    MODEL_LOCK.readLock().lock();
    try {
        return action.get();
    } finally {
        MODEL_LOCK.readLock().unlock();
    }
}

private void withWriteLock(Runnable action) {
    MODEL_LOCK.writeLock().lock();
    try {
        action.run();
    } finally {
        MODEL_LOCK.writeLock().unlock();
    }
}

    // ✅ Docker-safe path (ONLY real change)
    private static final String ONTOLOGY_PATH =
        System.getenv().getOrDefault(
            "ONTOLOGY_PATH",
            "/data/NARRATE-blueprints-rdf-xml.rdf"
        );

    // ✅ Namespace (kept EXACT default, just env-flexible)
    private static final String NS =
        System.getenv().getOrDefault(
            "ONTOLOGY_NS",
            "http://www.semanticweb.org/amal.elgammal/ontologies/2025/3/untitled-ontology-31#"
        );

    private static volatile OntModel model;
    private static volatile OntologyReader instance;
    static final ReentrantReadWriteLock MODEL_LOCK =
        new ReentrantReadWriteLock();
    

    private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    private static final Property OWL_MIN_QUALIFIED_CARDINALITY = ResourceFactory.createProperty(OWL_NS + "minQualifiedCardinality");
    private static final Property OWL_MAX_QUALIFIED_CARDINALITY = ResourceFactory.createProperty(OWL_NS + "maxQualifiedCardinality");
    private static final Property OWL_QUALIFIED_CARDINALITY = ResourceFactory.createProperty(OWL_NS + "qualifiedCardinality");
    
    
    public static String getNS() {
    return NS;
}
    private OntologyReader() {
        loadOntologyModel();
    }

    public static OntologyReader getInstance() {
        if (instance == null) {
            synchronized (OntologyReader.class) {
                if (instance == null) {
                    instance = new OntologyReader();
                }
            }
        }
        return instance;
    }

    // ✅ Load model (UNCHANGED logic)
    private static void loadOntologyModel() {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try (InputStream in = FileManager.get().open(ONTOLOGY_PATH)) {
            if (in == null) {
                throw new IllegalArgumentException("❌ Ontology file not found: " + ONTOLOGY_PATH);
            }
            model.read(in, null);
            System.out.println("✅ Ontology loaded successfully from: " + ONTOLOGY_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

public static void reloadModel() {
    System.out.println("♻️ Reloading Ontology Model...");

    MODEL_LOCK.writeLock().lock();

    try {
        loadOntologyModel();

        System.out.println("✅ Ontology model reloaded successfully.");
        System.out.println("📌 Individuals After Reload:");

        for (OntClass cls : model.listNamedClasses().toList()) {
            for (ExtendedIterator<? extends OntResource> i =
                    cls.listInstances(); i.hasNext();) {

                Individual ind = (Individual) i.next();
                System.out.println("🔹 " + ind.getLocalName());
            }
        }

    } finally {
        MODEL_LOCK.writeLock().unlock();
    }
}
    
    public static OntModel getModel() {
        return model;
    }
    

    public OntModel getReasonedModel() {
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
    }

   // Method to get all classes
    public Set<String> getOntologyClasses() {
    return withReadLock(() -> {

        Set<String> classSet = new HashSet<>();

        ExtendedIterator<OntClass> classIter =
                model.listClasses();

        while (classIter.hasNext()) {
            OntClass ontClass = classIter.next();

            if (ontClass.getLocalName() != null) {
                classSet.add(
                        ontClass.getLocalName()
                );
            }
        }

        return classSet;
    });
}
    public List<Map<String, Object>> getDataPropertiesWithMeta(String className) {
        return withReadLock(() -> {
        List<Map<String, Object>> result = new ArrayList<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            for (Iterator<DatatypeProperty> it = model.listDatatypeProperties(); it.hasNext();) {
                DatatypeProperty dp = it.next();
                for (ExtendedIterator<? extends Resource> itDomain = dp.listDomain(); itDomain.hasNext();) {
                    Resource domain = itDomain.next();
                    if (domain.canAs(OntClass.class)) {
                        OntClass domainClass = domain.as(OntClass.class);
                        if (domainClass.equals(ontClass) || ontClass.hasSuperClass(domainClass, true)) {

                            Map<String, Object> propMeta = new HashMap<>();
                            propMeta.put("name", dp.getLocalName());

                            // Get comment (if any)
                            String comment = "";
                            if (dp.hasProperty(RDFS.comment)) {
                                RDFNode obj = dp.getProperty(RDFS.comment).getObject();
                                if (obj.isLiteral()) {
                                    comment = obj.asLiteral().getString();
                                }
                            }
                            propMeta.put("comment", comment);

                            // Get displayOrder (default to Integer.MAX_VALUE)
                            int order = Integer.MAX_VALUE;
                            for (StmtIterator annots = dp.listProperties(); annots.hasNext();) {
                                Statement stmt = annots.nextStatement();
                                if (stmt.getPredicate().getLocalName().equals("displayOrder") && stmt.getObject().isLiteral()) {
                                    try {
                                        order = stmt.getObject().asLiteral().getInt();
                                        break;
                                    } catch (Exception e) {
                                        System.err.println("⚠️ Invalid displayOrder for property: " + dp.getLocalName());
                                    }
                                }
                            }
                            propMeta.put("order", order);

                            result.add(propMeta);
                            break; // found a matching domain, don't check others
                        }
                    }
                }
            }
        }

        // Sort by displayOrder
        result.sort(Comparator.comparingInt(p -> (int) p.get("order")));
        return result;
    });
}

    // New method to return Data Property names + comments
    public Map<String, String> getDataPropertiesWithComments(String className) {
        return withReadLock(() -> {
        Map<String, String> propertiesWithComments = new HashMap<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            for (Iterator<DatatypeProperty> it = model.listDatatypeProperties(); it.hasNext();) {
                DatatypeProperty dp = it.next();
                for (ExtendedIterator<? extends Resource> itDomain = dp.listDomain(); itDomain.hasNext();) {
                    Resource domain = itDomain.next();
                    if (domain.canAs(OntClass.class)) {
                        OntClass domainClass = domain.as(OntClass.class);
                        if (domainClass.equals(ontClass) || ontClass.hasSuperClass(domainClass, true)) {
                            String comment = "";
                            if (dp.hasProperty(RDFS.comment)) {
                                comment = dp.getProperty(RDFS.comment).getObject().asLiteral().getString();
                            }
                            propertiesWithComments.put(dp.getLocalName(), comment);
                        }
                    }
                }
            }
        }
        return propertiesWithComments;
    });
}

    public Set<String> getDataProperties(String className) {
        return withReadLock(() -> {
        Set<String> properties = new HashSet<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            // Fetch data properties including inherited ones
            for (Iterator<DatatypeProperty> it = model.listDatatypeProperties(); it.hasNext();) {
                DatatypeProperty dp = it.next();

                // Get the domain classes of the property
                for (ExtendedIterator<? extends Resource> itDomain = dp.listDomain(); itDomain.hasNext();) {
                    Resource domain = itDomain.next();

                    if (domain.canAs(OntClass.class)) {
                        OntClass domainClass = domain.as(OntClass.class);

                        // Check if the domain matches or is a superclass of the target class
                        if (domainClass.equals(ontClass) || ontClass.hasSuperClass(domainClass, true)) {
                            properties.add(dp.getLocalName());
                        }
                    }
                }
            }
        }
        return properties;
    });
}

    /* public Set<String> getObjectProperties(String className) {
        Set<String> properties = new HashSet<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            // Fetch object properties including inherited ones
            for (Iterator<ObjectProperty> it = model.listObjectProperties(); it.hasNext();) {
                ObjectProperty op = it.next();

                // Get the domain classes of the property
                for (ExtendedIterator<? extends Resource> itDomain = op.listDomain(); itDomain.hasNext();) {
                    Resource domain = itDomain.next();

                    if (domain.canAs(OntClass.class)) {
                        OntClass domainClass = domain.as(OntClass.class);

                        // Check if the domain matches or is a superclass of the target class
                        if (domainClass.equals(ontClass) || ontClass.hasSuperClass(domainClass, true)) {
                            properties.add(op.getLocalName());
                        }
                    }
                }
            }
        }
        return properties;
    }*/
    public Set<String> getObjectProperties(String className, boolean excludeInverses) {
        return withReadLock(() -> {
        System.out.println("Entering getObjectProperties method");
        //OntologyReader.reloadModel();
        Set<String> properties = new HashSet<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            for (Iterator<ObjectProperty> it = model.listObjectProperties(); it.hasNext();) {
                ObjectProperty op = it.next();

                if (excludeInverses) {
                    // ✅ Check if *this* property is defined as inverseOf another
                    StmtIterator invCheck = model.listStatements(op, OWL.inverseOf, (RDFNode) null);

                    if (invCheck.hasNext()) {
                        System.out.println("⛔ Skipping inverse-only property: " + op.getLocalName());
                        continue;
                    }
                }

                for (ExtendedIterator<? extends Resource> itDomain = op.listDomain(); itDomain.hasNext();) {
                    Resource domain = itDomain.next();

                    if (domain.canAs(OntClass.class)) {
                        OntClass domainClass = domain.as(OntClass.class);

                        if (domainClass.equals(ontClass) || ontClass.hasSuperClass(domainClass, true)) {
                            properties.add(op.getLocalName());
                        }
                    }
                }
            }
        }

        return properties;
    });
}

    public Set<String> getObjectProperties(String className) {
        return getObjectProperties(className, true); // Default: exclude inverses
    }

    public Map<String, String> getObjectPropertiesWithComments(
        String className,
        boolean excludeInverses) {

    return withReadLock(() -> {

        Map<String, String> propertiesWithComments = new HashMap<>();

        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {

            for (Iterator<ObjectProperty> it = model.listObjectProperties();
                    it.hasNext();) {

                ObjectProperty op = it.next();

                if (excludeInverses) {
    StmtIterator invCheck =
            model.listStatements(
                    op,
                    OWL.inverseOf,
                    (RDFNode) null
            );

    if (invCheck.hasNext()) {
        continue;
    }
}

                for (ExtendedIterator<? extends Resource> itDomain =
                        op.listDomain(); itDomain.hasNext();) {

                    Resource domain = itDomain.next();

                    if (domain.canAs(OntClass.class)) {

                        OntClass domainClass =
                                domain.as(OntClass.class);

                        if (domainClass.equals(ontClass)
                                || ontClass.hasSuperClass(domainClass, true)) {

                            String comment = "";

                            if (op.hasProperty(RDFS.comment)) {
                                comment = op.getProperty(RDFS.comment)
                                        .getObject()
                                        .asLiteral()
                                        .getString();
                            }

                            propertiesWithComments.put(
                                    op.getLocalName(),
                                    comment
                            );
                        }
                    }
                }
            }
        }

        return propertiesWithComments;
    });
}
    public Map<String, String> getDataPropertiesForIndividual(String individualName) {
        return withReadLock(() -> {
        Map<String, String> dataProperties = new HashMap<>();
        Individual individual = model.getIndividual(NS + individualName);
        if (individual != null) {
            StmtIterator it = individual.listProperties();
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();
                if (stmt.getObject().isLiteral()) {
                    dataProperties.put(stmt.getPredicate().getLocalName(), stmt.getObject().asLiteral().getString());
                }
            }
        }
        return dataProperties;
    });
}

    //not used across application
    public Map<String, String> getObjectPropertiesForIndividual(String individualName) {
        return withReadLock(() -> {

            Map<String, String> objectProperties = new HashMap<>();
        Individual individual = model.getIndividual(NS + individualName);
        if (individual != null) {
            StmtIterator it = individual.listProperties();
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();
                if (stmt.getObject().isResource()) {
                    objectProperties.put(stmt.getPredicate().getLocalName(), stmt.getObject().asResource().getLocalName());
                }
            }
        }
        return objectProperties;
    });
    }

    public Set<String> getInstancesOfClass(String className) {
        return withReadLock(() -> {
        Set<String> instances = new HashSet<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            // Get individuals from class listInstances()
            for (ExtendedIterator<? extends OntResource> i = ontClass.listInstances(); i.hasNext();) {
                Individual ind = (Individual) i.next();
                if (ind.getLocalName() != null) {
                    instances.add(ind.getLocalName());
                }
            }

            // Also check manually via rdf:type (safety net)
            for (ExtendedIterator<Individual> i = model.listIndividuals(); i.hasNext();) {
                Individual ind = i.next();
                if (ind.hasRDFType(ontClass) && ind.getLocalName() != null) {
                    instances.add(ind.getLocalName());
                }
            }
        }
        return instances;
    });
}

    /**
     * Retrieves the range class of an object property.
     *
     * @param objectProperty The name of the object property.
     * @return The name of the range class, or null if not found.
     */
    public String getRangeClass(String objectProperty) {
    return withReadLock(() -> {

        ObjectProperty prop =
                model.getObjectProperty(NS + objectProperty);

        if (prop == null) {
            System.out.println(
                    "Object Property not found: "
                            + objectProperty
            );
            return null;
        }

        StmtIterator it =
                model.listStatements(
                        prop,
                        RDFS.range,
                        (RDFNode) null
                );

        while (it.hasNext()) {
            Statement stmt = it.nextStatement();

            return stmt.getObject()
                    .asResource()
                    .getLocalName();
        }

        return null;
    });
}

private String getFullURI(String className) {
    for (OntClass cls : model.listNamedClasses().toList()) {
        if (cls.getLocalName() != null
                && cls.getLocalName().equals(className)) {

            return cls.getURI();
        }
    }

    return null;
}
    //not used across application
    public Map<String, String> getIndividualDetails(String individualName) {
        return withReadLock(() -> {
        Map<String, String> details = new HashMap<>();
        Individual individual = model.getIndividual(NS + individualName);
        if (individual != null) {
            StmtIterator it = individual.listProperties();
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();
                String property = stmt.getPredicate().getLocalName();
                System.out.print("getIndividualDetails property Name:" + property);
                String value = stmt.getObject().toString();
                System.out.print("getIndividualDetails property value:" + value);
                details.put(property, value);
            }
        }
        return details;
    });
    }

    public boolean deleteIndividual(String individualName) {

    MODEL_LOCK.writeLock().lock();

    try {

        System.out.println(
                "Deleting individual: " + individualName
        );

        // Always reload the latest file before modifying it
        loadOntologyModel();

        Individual individual =
                model.getIndividual(NS + individualName);

        if (individual == null) {

            System.err.println(
                    "Error: Individual "
                            + individualName
                            + " not found."
            );

            return false;
        }

        // Log triples before deletion
        StmtIterator it =
                model.listStatements(
                        individual,
                        null,
                        (RDFNode) null
                );

        while (it.hasNext()) {
            System.out.println(
                    "Triple found: "
                            + it.nextStatement()
            );
        }

        // Remove triples where individual is subject
        model.removeAll(
                individual,
                null,
                null
        );

        // Remove triples where individual is object
        model.removeAll(
                null,
                null,
                individual
        );

        System.out.println(
                "Successfully deleted: "
                        + individualName
        );

        // Write to temporary file first
        File tempFile =
                new File(
                        ONTOLOGY_PATH + ".tmp"
                );

        try (FileOutputStream out =
                     new FileOutputStream(tempFile)) {

            model.write(
                    out,
                    "RDF/XML-ABBREV"
            );
        }

        // Atomically replace ontology
        Files.move(
                tempFile.toPath(),
                new File(ONTOLOGY_PATH).toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );

        System.out.println(
                "📂 Loaded ontology from: "
                        + ONTOLOGY_PATH
        );

        // Reload latest version
        loadOntologyModel();

        return true;

    } catch (IOException e) {

        e.printStackTrace();
        return false;

    } finally {

        MODEL_LOCK.writeLock().unlock();
    }
}
    
    public List<String> getIndividualTriples(String individualName) {
        return withReadLock(() -> {
        List<String> triples = new ArrayList<>();
        Individual individual = model.getIndividual(NS + individualName);

        if (individual == null) {
            System.err.println("Error: Individual " + individualName + " not found.");
            return triples;
        }

        // Get triples where the individual is the subject
        StmtIterator subjectTriples = model.listStatements(individual, null, (RDFNode) null);
        while (subjectTriples.hasNext()) {
            Statement stmt = subjectTriples.nextStatement();
            triples.add(stmt.getSubject().getLocalName() + " " + stmt.getPredicate().getLocalName() + " " + stmt.getObject().toString());
        }

        // Get triples where the individual is the object
        StmtIterator objectTriples = model.listStatements(null, null, individual);
        while (objectTriples.hasNext()) {
            Statement stmt = objectTriples.nextStatement();
            triples.add(stmt.getSubject().getLocalName() + " " + stmt.getPredicate().getLocalName() + " " + stmt.getObject().toString());
        }

        return triples;
    });
    }

public boolean updateIndividual(
        String className,
        String individualName,
        Map<String, Object> dataProps,
        Map<String, Object> objectProps) {

    System.out.println("🚨 updateIndividual() CALLED!");
    System.out.println("👉 className: " + className);
    System.out.println("👉 individualName: " + individualName);
    System.out.println("👉 dataProps: " + dataProps);
    System.out.println("👉 objectProps: " + objectProps);

    // =========================================================
    // ACQUIRE WRITE LOCK
    // =========================================================
    //
    // The entire operation is protected.
    //
    // Therefore:
    //
    // PUT A ────────────────┐
    //                       │
    // PUT B ── waits ───────┤
    //                       │
    // GET   ── waits ───────┤
    //                       │
    //                       ▼
    //                 ontology update
    //
    MODEL_LOCK.writeLock().lock();

    try {

        // =====================================================
        // LOAD LATEST ONTOLOGY
        // =====================================================
        //
        // Do this AFTER obtaining the write lock.
        //
        // This is important because another writer may have
        // modified the file before this request obtained the lock.
        //

        System.out.println(
                "♻️ Loading latest ontology before update..."
        );

        loadOntologyModel();

        OntModel currentModel = model;


        // =====================================================
        // FIND INDIVIDUAL
        // =====================================================

        Individual individual =
                currentModel.getIndividual(
                        NS + individualName
                );

        if (individual == null) {

            System.out.println(
                    "❌ Individual not found: "
                            + individualName
            );

            return false;
        }


        // =====================================================
        // REMOVE EXISTING PROPERTIES
        // =====================================================

        individual.removeAll(null);


        // =====================================================
        // RESTORE SELECTED CLASS
        // =====================================================

        OntClass selectedClass =
                currentModel.getOntClass(
                        NS + className
                );

        if (selectedClass != null) {

            individual.addRDFType(
                    selectedClass
            );

        } else {

            System.out.println(
                    "⚠️ Class not found: "
                            + className
            );
        }


        // =====================================================
        // RESTORE OWL:NamedIndividual
        // =====================================================

        Resource namedIndividualType =
                currentModel.getResource(
                        OWL.NS + "NamedIndividual"
                );

        individual.addProperty(
                RDF.type,
                namedIndividualType
        );


        // =====================================================
        // DATA PROPERTY RANGES
        // =====================================================

        Map<String, String> dataPropertyRanges =
                getDataPropertyRanges(className);

        System.out.println(
                "📋 Data property ranges: "
                        + dataPropertyRanges
        );


        // =====================================================
        // DATA PROPERTIES
        // =====================================================

        if (dataProps != null) {

            for (Map.Entry<String, Object> entry
                    : dataProps.entrySet()) {

                String propName =
                        entry.getKey();

                Object rawValues =
                        entry.getValue();

                if (propName == null
                        || rawValues == null) {
                    continue;
                }


                // -------------------------------------------------
                // Convert value to List
                // -------------------------------------------------

                List<Object> values;

                if (rawValues instanceof List) {

                    values =
                            (List<Object>) rawValues;

                } else {

                    values =
                            Collections.singletonList(
                                    rawValues
                            );
                }


                // -------------------------------------------------
                // Find datatype property
                // -------------------------------------------------

                DatatypeProperty dataProp =
                        currentModel.getDatatypeProperty(
                                NS + propName
                        );

                if (dataProp == null) {

                    System.out.println(
                            "⚠️ Datatype property not found: "
                                    + propName
                    );

                    continue;
                }


                // -------------------------------------------------
                // Remove any existing values
                // -------------------------------------------------

                Property genericProp =
                        currentModel.getProperty(
                                NS + propName
                        );

                currentModel.removeAll(
                        individual,
                        genericProp,
                        null
                );


                // -------------------------------------------------
                // Determine range
                // -------------------------------------------------

                String rangeURI =
                        dataPropertyRanges.get(
                                propName
                        );


                // -------------------------------------------------
                // Add each value
                // -------------------------------------------------

                for (Object rawValue : values) {

                    if (rawValue == null) {
                        continue;
                    }

                    String val =
                            rawValue.toString();


                    try {

                        if (rangeURI != null) {

                            // =====================================
                            // INTEGER
                            // =====================================

                            if (rangeURI.equals(
                                    XSD.integer.getURI())) {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                Integer.parseInt(val)
                                        )
                                );


                            // =====================================
                            // DECIMAL
                            // =====================================

                            } else if (rangeURI.equals(
                                    XSD.decimal.getURI())) {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                new BigDecimal(val)
                                        )
                                );


                            // =====================================
                            // FLOAT
                            // =====================================

                            } else if (rangeURI.equals(
                                    XSD.xfloat.getURI())) {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                Float.parseFloat(val)
                                        )
                                );


                            // =====================================
                            // DOUBLE
                            // =====================================

                            } else if (rangeURI.equals(
                                    XSD.xdouble.getURI())) {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                Double.parseDouble(val)
                                        )
                                );


                            // =====================================
                            // BOOLEAN
                            // =====================================

                            } else if (rangeURI.equals(
                                    XSD.xboolean.getURI())) {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                Boolean.parseBoolean(val)
                                        )
                                );


                            // =====================================
                            // DATE
                            // =====================================

                            } else if (rangeURI.equals(
                                    XSD.date.getURI())) {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                val,
                                                XSD.date.getURI()
                                        )
                                );


                            // =====================================
                            // DATETIME
                            // =====================================

                            } else if (rangeURI.equals(
                                    XSD.dateTime.getURI())) {

                                if (!val.contains("T")) {
                                    val += "T00:00:00";
                                }

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                val,
                                                XSD.dateTime.getURI()
                                        )
                                );


                            // =====================================
                            // DATETIME STAMP
                            // =====================================

                            } else if (rangeURI.equals(
                                    "http://www.w3.org/2001/XMLSchema#dateTimeStamp")) {

                                if (!val.contains("T")) {

                                    val +=
                                            "T00:00:00Z";

                                } else {

                                    // Preserve original behavior:
                                    // HH:mm → HH:mm:00

                                    if (val.length() == 16) {
                                        val += ":00";
                                    }

                                    if (!val.endsWith("Z")
                                            && !val.matches(
                                            ".*[+-]\\d{2}:\\d{2}$"
                                    )) {

                                        val += "Z";
                                    }
                                }

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                val,
                                                "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
                                        )
                                );


                            // =====================================
                            // ANY URI
                            // =====================================

                            } else if (rangeURI.equals(
                                    XSD.anyURI.getURI())) {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                val,
                                                XSD.anyURI.getURI()
                                        )
                                );


                            // =====================================
                            // STRING
                            // =====================================

                            } else if (rangeURI.equals(
                                    XSD.xstring.getURI())) {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                val,
                                                XSD.xstring.getURI()
                                        )
                                );


                            // =====================================
                            // OTHER
                            // =====================================

                            } else {

                                individual.addLiteral(
                                        dataProp,
                                        currentModel.createTypedLiteral(
                                                val,
                                                XSD.xstring.getURI()
                                        )
                                );
                            }

                        } else {

                            // No range → string

                            individual.addLiteral(
                                    dataProp,
                                    currentModel.createTypedLiteral(
                                            val,
                                            XSD.xstring.getURI()
                                    )
                            );
                        }

                    } catch (Exception e) {

                        System.err.println(
                                "⚠️ Could not parse value '"
                                        + val
                                        + "' for property '"
                                        + propName
                                        + "'. Falling back to string."
                        );

                        individual.addLiteral(
                                dataProp,
                                currentModel.createTypedLiteral(
                                        val,
                                        XSD.xstring.getURI()
                                )
                        );
                    }
                }
            }
        }


        // =========================================================
        // SPECIAL "type" OBJECT PROPERTY
        // =========================================================
        //
        // THIS WAS PRESENT IN YOUR ORIGINAL
        // UpdateIndividualServlet1.java.
        //
        // We preserve it here.
        //

        if (objectProps != null
                && objectProps.containsKey("type")) {

            Object rawTypes =
                    objectProps.get("type");

            List<Object> types;

            if (rawTypes instanceof List) {

                types =
                        (List<Object>) rawTypes;

            } else {

                types =
                        Collections.singletonList(
                                rawTypes
                        );
            }

            for (Object rawType : types) {

                if (rawType == null) {
                    continue;
                }

                String typeName =
                        rawType.toString();

                if (!typeName.isEmpty()) {

                    OntClass classType =
                            currentModel.getOntClass(
                                    NS + typeName
                            );

                    if (classType != null) {

                        individual.addRDFType(
                                classType
                        );

                        System.out.println(
                                "✅ Added RDF type: "
                                        + typeName
                        );

                    } else {

                        System.out.println(
                                "⚠️ RDF type class not found: "
                                        + typeName
                        );
                    }
                }
            }

            // Do NOT treat "type" as an ordinary object property
            objectProps.remove("type");
        }


        // =========================================================
        // OBJECT PROPERTIES
        // =========================================================

        if (objectProps != null) {

            for (Map.Entry<String, Object> entry
                    : objectProps.entrySet()) {

                String propName =
                        entry.getKey();

                Object rawValues =
                        entry.getValue();

                if (propName == null
                        || rawValues == null) {
                    continue;
                }


                // -------------------------------------------------
                // Convert to List
                // -------------------------------------------------

                List<Object> values;

                if (rawValues instanceof List) {

                    values =
                            (List<Object>) rawValues;

                } else {

                    values =
                            Collections.singletonList(
                                    rawValues
                            );
                }


                // -------------------------------------------------
                // Find property
                // -------------------------------------------------

                Property property =
                        currentModel.getProperty(
                                NS + propName
                        );

                if (property == null) {

                    System.out.println(
                            "⚠️ Object property not found: "
                                    + propName
                    );

                    continue;
                }


                // -------------------------------------------------
                // Add each object
                // -------------------------------------------------

                for (Object rawObjectValue : values) {

                    if (rawObjectValue == null) {
                        continue;
                    }

                    String objectValue =
                            rawObjectValue.toString();

                    if (objectValue.isEmpty()) {
                        continue;
                    }


                    // ---------------------------------------------
                    // Preserve original URI handling
                    // ---------------------------------------------

                    if (!objectValue.startsWith("http://")
        && !objectValue.startsWith("https://")) {
                        objectValue =
                                NS + objectValue;
                    }


                    try {

                        new java.net.URI(
                                objectValue
                        );

                        Resource objResource =
                                currentModel.getResource(
                                        objectValue
                                );

                        if (objResource != null) {

                            individual.addProperty(
                                    property,
                                    objResource
                            );

                            System.out.println(
                                    "🔗 Added object property: "
                                            + propName
                                            + " → "
                                            + objectValue
                            );
                        }

                    } catch (
                            java.net.URISyntaxException e) {

                        System.err.println(
                                "⚠️ Invalid URI for object property: "
                                        + objectValue
                        );
                    }
                }
            }
        }


        // =========================================================
        // SAFE ATOMIC FILE WRITE
        // =========================================================

        File finalFile =
                new File(ONTOLOGY_PATH);

        File tempFile =
                new File(
                        ONTOLOGY_PATH + ".tmp"
                );


        System.out.println(
                "=== DEBUG ONTOLOGY UPDATE ==="
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


        try {

            // -----------------------------------------------------
            // Write complete model to temporary file
            // -----------------------------------------------------

            try (FileOutputStream out =
                         new FileOutputStream(tempFile)) {

                currentModel.write(
                        out,
                        "RDF/XML-ABBREV"
                );
            }


            System.out.println(
                    "✅ Temporary ontology written."
            );


            // -----------------------------------------------------
            // Replace original atomically
            // -----------------------------------------------------

            Files.move(
                    tempFile.toPath(),
                    finalFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );


            System.out.println(
                    "✅ Ontology file replaced successfully."
            );

            System.out.println(
                    "Size AFTER: "
                            + finalFile.length()
            );

            System.out.println(
                    "==== END DEBUG ===="
            );


            // -----------------------------------------------------
            // Reload model
            // -----------------------------------------------------
            //
            // We already hold the WRITE LOCK, so use
            // loadOntologyModel() directly rather than calling
            // reloadModel(), which would acquire the same lock again.
            //

            loadOntologyModel();

            System.out.println(
                    "📂 Ontology model reloaded from: "
                            + ONTOLOGY_PATH
            );


            System.out.println(
                    "✅ updateIndividual() completed successfully."
            );

            return true;


        } catch (Exception e) {

            System.err.println(
                    "❌ Error while writing updated ontology."
            );

            e.printStackTrace();


            // -----------------------------------------------------
            // Clean up temporary file
            // -----------------------------------------------------

            try {

                if (tempFile.exists()) {

                    Files.deleteIfExists(
                            tempFile.toPath()
                    );

                    System.out.println(
                            "🧹 Temporary ontology file deleted."
                    );
                }

            } catch (IOException cleanupException) {

                System.err.println(
                        "⚠️ Could not delete temporary ontology file."
                );

                cleanupException.printStackTrace();
            }

            return false;
        }


    } finally {

        // =========================================================
        // ALWAYS RELEASE WRITE LOCK
        // =========================================================

        MODEL_LOCK.writeLock().unlock();

        System.out.println(
                "🔓 updateIndividual(): write lock released."
        );
    }
}    
    public Map<String, Map<String, Integer>> getPropertyCardinalities(String className) {
        return withReadLock(() -> {
        Map<String, Map<String, Integer>> cardinalityMap = new HashMap<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass == null) {
            return cardinalityMap;
        }

        // Loop through restrictions
        for (ExtendedIterator<OntClass> it = ontClass.listSuperClasses(); it.hasNext();) {
            OntClass superCls = it.next();

            if (superCls.isRestriction()) {
                Restriction restriction = superCls.asRestriction();
                String propName = restriction.getOnProperty().getLocalName();

                Map<String, Integer> card = cardinalityMap.getOrDefault(propName, new HashMap<>());

                if (restriction.isMinCardinalityRestriction()) {
                    card.put("min", restriction.asMinCardinalityRestriction().getMinCardinality());
                } else if (restriction.isMaxCardinalityRestriction()) {
                    card.put("max", restriction.asMaxCardinalityRestriction().getMaxCardinality());
                } else if (restriction.isCardinalityRestriction()) {
                    int c = restriction.asCardinalityRestriction().getCardinality();
                    card.put("min", c);
                    card.put("max", c);
                }

                cardinalityMap.put(propName, card);
            }
        }

        return cardinalityMap;
    });
    }

    public Map<String, Map<String, Integer>> getCardinalities(String className) {
        return withReadLock(() -> {
        System.out.println("📌 Fetching cardinalities for class: " + className);

        Map<String, Map<String, Integer>> cardinalityMap = new HashMap<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass == null) {
            System.err.println("❌ OntClass not found for: " + className);
            return cardinalityMap;
        }

        Set<OntClass> allClassesToCheck = new HashSet<>();
        Queue<OntClass> queue = new LinkedList<>();
        queue.add(ontClass);

        // 🔁 Collect all superclasses transitively (including the class itself)
        while (!queue.isEmpty()) {
            OntClass current = queue.poll();
            if (allClassesToCheck.add(current)) {
                ExtendedIterator<OntClass> superIt = current.listSuperClasses(true);
                while (superIt.hasNext()) {
                    queue.add(superIt.next());
                }
            }
        }

        // ✅ Check restrictions on all collected classes
        for (OntClass cls : allClassesToCheck) {
            System.out.println("🔍 Checking class (or restriction): " + cls);

            if (!cls.isRestriction()) {
                continue;
            }

            Restriction restriction = cls.asRestriction();
            Property onProperty = restriction.getOnProperty();

            if (onProperty == null) {
                System.out.println("⚠️ Restriction has null onProperty — skipping.");
                continue;
            }

            String propName = onProperty.getLocalName();
            Map<String, Integer> card = cardinalityMap.getOrDefault(propName, new HashMap<>());

            System.out.println("🔎 Found restriction on property: " + propName);
            System.out.println("🔎 Restriction type: " + restriction.getClass().getName());

            // Skip someValuesFrom
            if (restriction.canAs(SomeValuesFromRestriction.class)) {
                System.out.println("👀 SomeValuesFrom restriction detected — skipping cardinality.");
                continue;
            }

            // Manual cardinality checks (OWL-qualified-cardinality)
            StmtIterator stmtIter = restriction.listProperties();
            while (stmtIter.hasNext()) {
                Statement stmt = stmtIter.nextStatement();
                Property predicate = stmt.getPredicate();
                RDFNode object = stmt.getObject();

                if (!object.isLiteral()) {
                    continue;
                }

                int value = object.asLiteral().getInt();
                if (predicate.equals(OWL_MIN_QUALIFIED_CARDINALITY)) {
                    card.put("min", value);
                    System.out.println("✅ Found minQualifiedCardinality = " + value);
                } else if (predicate.equals(OWL_MAX_QUALIFIED_CARDINALITY)) {
                    card.put("max", value);
                    System.out.println("✅ Found maxQualifiedCardinality = " + value);
                } else if (predicate.equals(OWL_QUALIFIED_CARDINALITY)) {
                    card.put("min", value);
                    card.put("max", value);
                    System.out.println("✅ Found qualifiedCardinality = " + value);
                }
            }

            if (!card.isEmpty()) {
                cardinalityMap.put(propName, card);
            } else {
                System.out.println("❓ No cardinality values found for property: " + propName);
            }
        }

        System.out.println("📦 Final cardinalities map: " + cardinalityMap);
        return cardinalityMap;
    });
    }

    public Map<String, List<String>> getAllDataPropertiesForIndividual(String individualName) {
        return withReadLock(() -> {
        Map<String, List<String>> dataProperties = new HashMap<>();
        Individual individual = model.getIndividual(NS + individualName);

        if (individual != null) {
            StmtIterator it = individual.listProperties();
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();
                if (stmt.getObject().isLiteral()) {
                    String propName = stmt.getPredicate().getLocalName();
                    RDFNode object = stmt.getObject();

                    if (object.isLiteral()) {
                        Literal literal = object.asLiteral();

                        String value;
                        if (literal.getDatatypeURI() != null) {
                            value = literal.getLexicalForm(); // 📅 Get the correct lexical form including date, datetime, URI, number
                        } else {
                            value = literal.getString();
                        }

                        dataProperties.computeIfAbsent(propName, k -> new ArrayList<>()).add(value);
                    }
                }
            }
        }

        return dataProperties;
    });
    }

    //not used across application
    public Map<String, List<String>> getAllObjectPropertiesForIndividual(String individualName) {
        return withReadLock(() -> {
        Map<String, List<String>> objectProperties = new HashMap<>();
        Individual individual = model.getIndividual(NS + individualName);

        if (individual != null) {
            StmtIterator it = individual.listProperties();
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();
                if (stmt.getObject().isResource()) {
                    String propName = stmt.getPredicate().getLocalName();
                    String value = stmt.getObject().asResource().getLocalName();

                    objectProperties.computeIfAbsent(propName, k -> new ArrayList<>()).add(value);
                }
            }
        }

        return objectProperties;
    });
    }

    public Map<String, List<String>> getAllObjectPropertiesWithReasoner(String individualName) {
        return withReadLock(() -> {
        OntModel reasonedModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Map<String, List<String>> objectProperties = new HashMap<>();
        Individual individual = reasonedModel.getIndividual(NS + individualName);

        if (individual != null) {
            StmtIterator it = individual.listProperties();
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();
                if (stmt.getObject().isResource()) {
                    String propName = stmt.getPredicate().getLocalName();
                    String value = stmt.getObject().asResource().getLocalName();

                    objectProperties.computeIfAbsent(propName, k -> new ArrayList<>()).add(value);
                }
            }
        }

        return objectProperties;
    });
    }

// Helper classes to handle data structure for AJAX request
    public static class DataPropertyValue {

        private String property;
        private String value;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class ObjectPropertyValue {

        private String property;
        private String value;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public Map<String, Object> queryIndividuals(String className,
            Map<String, List<String>> dataProps,
            Map<String, List<String>> objectProps,
            boolean includeFuzzy,
            double fuzzyThreshold) {
        return withReadLock(() -> {

        Set<String> matchedIndividuals = new HashSet<>();
        Map<String, Map<String, String>> similarityNotes = new HashMap<>();

        //OntClass ontClass = model.getOntClass(NS + className);
        //considering readoned moded instead
        OntModel reasonedModel = getReasonedModel();
        OntClass ontClass = reasonedModel.getOntClass(NS + className);

        if (ontClass == null) {
            System.out.println("❌ Class not found: " + className);
            Map<String, Object> result = new HashMap<>();
            result.put("matchedIndividuals", new HashSet<String>());
            result.put("similarityNotes", new HashMap<String, String>());
            return result;
        }

        Map<String, Map<String, Integer>> cardinalityMap = getPropertyCardinalities(className);

        ExtendedIterator<? extends OntResource> instances = ontClass.listInstances();
        while (instances.hasNext()) {
            Individual individual = instances.next().as(Individual.class);
            String individualName = individual.getLocalName();
            boolean matchesAll = true;

            System.out.println("🔍 Evaluating individual: " + individualName);

            // 🔍 Data properties with numeric operator support
            for (Map.Entry<String, List<String>> entry : dataProps.entrySet()) {
                String prop = entry.getKey();
                List<String> searchValues = entry.getValue();
                //Property property = model.getProperty(NS + prop);
                Property property = reasonedModel.getProperty(NS + prop);

                if (property != null) {
                    List<String> actualValues = new ArrayList<>();
                    StmtIterator it = individual.listProperties(property);
                    while (it.hasNext()) {
                        Statement stmt = it.nextStatement();
                        if (stmt.getObject().isLiteral()) {
                            actualValues.add(stmt.getObject().asLiteral().getString());
                        }
                    }

                    System.out.println("🧪 Data property: " + prop);
                    System.out.println("   🔹 Expected: " + searchValues);
                    System.out.println("   🔸 Actual:   " + actualValues);

                    for (String expected : searchValues) {
                        boolean matchFound = false;

                        String operator = expected.replaceAll("[0-9.]", "").trim();
                        String numPart = expected.replaceAll("[^0-9.]", "").trim();

                        try {
                            double expectedNum = Double.parseDouble(numPart);

                            for (String actual : actualValues) {
                                double actualNum = Double.parseDouble(actual);

                                switch (operator) {
                                    case "=":
                                        matchFound = actualNum == expectedNum;
                                        break;
                                    case ">":
                                        matchFound = actualNum > expectedNum;
                                        break;
                                    case ">=":
                                        matchFound = actualNum >= expectedNum;
                                        break;
                                    case "<":
                                        matchFound = actualNum < expectedNum;
                                        break;
                                    case "<=":
                                        matchFound = actualNum <= expectedNum;
                                        break;
                                    default:
                                        matchFound = actual.equals(expected);
                                        break;
                                }

                                if (matchFound) {
                                    break;
                                }
                            }
                        } catch (NumberFormatException e) {
                            if (actualValues.contains(expected)) {
                                matchFound = true;
                            } else if (includeFuzzy) {
                                for (String actual : actualValues) {
                                    double sim = computeStringSimilarity(actual, expected);
                                    System.out.println("🔍 Similarity = " + sim);
                                    if (sim >= fuzzyThreshold) {
                                        matchFound = true;

                                        // 🧠 Capture similarity note per individual and property
                                        String note = actual + " ≈ " + expected + " (" + Math.round(sim * 100) + "%)";
                                        similarityNotes
                                                .computeIfAbsent(individualName, k -> new HashMap<>())
                                                .put(prop, note);

                                        System.out.println("🤝 Fuzzy match: " + note);
                                        break;
                                    }
                                }
                            }
                        }

                        if (!matchFound) {
                            System.out.println("   ❌ No match for: " + expected);
                            matchesAll = false;
                            break;
                        }
                    }

                    int actualCount = actualValues.size();
                    int min = cardinalityMap.getOrDefault(prop, Map.of()).getOrDefault("min", 0);
                    int max = cardinalityMap.getOrDefault(prop, Map.of()).getOrDefault("max", Integer.MAX_VALUE);
                    if (actualCount < min || actualCount > max) {
                        System.out.println("   ❌ Fails cardinality: found " + actualCount + ", expected " + min + ".." + max);
                        matchesAll = false;
                    }
                }

                if (!matchesAll) {
                    break;
                }
            }

            // 🔁 Object properties (unchanged)
            if (matchesAll) {
                for (Map.Entry<String, List<String>> entry : objectProps.entrySet()) {
                    String prop = entry.getKey();
                    List<String> searchValues = entry.getValue();
                    //Property property = model.getProperty(NS + prop);
                    Property property = reasonedModel.getProperty(NS + prop);

                    if (property != null) {
                        List<String> actualTargets = new ArrayList<>();
                        StmtIterator it = individual.listProperties(property);
                        while (it.hasNext()) {
                            Statement stmt = it.nextStatement();
                            if (stmt.getObject().isResource()) {
                                actualTargets.add(stmt.getObject().asResource().getLocalName());
                            }
                        }

                        System.out.println("🔁 Object property: " + prop);
                        System.out.println("   🔹 Expected: " + searchValues);
                        System.out.println("   🔸 Actual:   " + actualTargets);

                        for (String val : searchValues) {
                            if (!actualTargets.contains(val)) {
                                System.out.println("   ❌ Mismatch: missing object '" + val + "'");
                                matchesAll = false;
                                break;
                            }
                        }

                        int actualCount = actualTargets.size();
                        int min = cardinalityMap.getOrDefault(prop, Map.of()).getOrDefault("min", 0);
                        int max = cardinalityMap.getOrDefault(prop, Map.of()).getOrDefault("max", Integer.MAX_VALUE);
                        if (actualCount < min || actualCount > max) {
                            System.out.println("   ❌ Fails cardinality: found " + actualCount + ", expected " + min + ".." + max);
                            matchesAll = false;
                        }
                    }

                    if (!matchesAll) {
                        break;
                    }
                }

            }

            if (matchesAll) {
                System.out.println("✅ MATCH: " + individualName);
                matchedIndividuals.add(individualName);
            } else {
                System.out.println("⛔️ Skipped: " + individualName);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("matchedIndividuals", matchedIndividuals);
        result.put("similarityNotes", similarityNotes);
        return result;

        //return similarityNotes;
    });
    }

    private double computeStringSimilarity(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();

        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = org.apache.commons.text.similarity.LevenshteinDistance.getDefaultInstance().apply(a, b);
        return 1.0 - ((double) distance / maxLength);
    }

    public Set<String> getNumericDataProperties(String className) {
        return withReadLock(() -> {
        Set<String> numericProps = new HashSet<>();
        Set<String> dataProps = getDataProperties(className);
        for (String prop : dataProps) {
            Property property = model.getProperty(NS + prop);
            if (property != null) {
                StmtIterator it = property.listProperties(RDFS.range);
                while (it.hasNext()) {
                    RDFNode range = it.nextStatement().getObject();
                    if (range.isResource()) {
                        String rangeURI = range.asResource().getURI();
                        if (rangeURI != null && (rangeURI.endsWith("integer") || rangeURI.endsWith("decimal") || rangeURI.endsWith("float") || rangeURI.endsWith("double"))) {
                            numericProps.add(prop);
                        }
                    }
                }
            }
        }
        return numericProps;
    });
    }

// 🗓️ Get Date Properties
    public Set<String> getDateDataProperties(String className) {
        return withReadLock(() -> {
        Set<String> dateProps = new HashSet<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            for (Iterator<DatatypeProperty> it = model.listDatatypeProperties(); it.hasNext();) {
                DatatypeProperty dp = it.next();
                for (ExtendedIterator<? extends Resource> itDomain = dp.listDomain(); itDomain.hasNext();) {
                    Resource domain = itDomain.next();
                    if (domain.canAs(OntClass.class)) {
                        OntClass domainClass = domain.as(OntClass.class);
                        if (domainClass.equals(ontClass) || ontClass.hasSuperClass(domainClass, true)) {
                            if (dp.getRange() != null) {
                                String rangeUri = dp.getRange().getURI();
                                if (XSD.date.getURI().equals(rangeUri) || XSD.dateTime.getURI().equals(rangeUri)) {
                                    dateProps.add(dp.getLocalName());
                                }
                            }
                        }
                    }
                }
            }
        }
        return dateProps;
    });
    }

    public Set<String> getDateTimeStampProperties(String className) {
        return withReadLock(() -> {
        Set<String> props = new HashSet<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            for (Iterator<DatatypeProperty> it = model.listDatatypeProperties(); it.hasNext();) {
                DatatypeProperty dp = it.next();

                for (ExtendedIterator<? extends Resource> itDomain = dp.listDomain(); itDomain.hasNext();) {
                    Resource domain = itDomain.next();

                    if (domain.canAs(OntClass.class)) {
                        OntClass domainClass = domain.as(OntClass.class);

                        if (domainClass.equals(ontClass) || ontClass.hasSuperClass(domainClass, true)) {

                            if (dp.getRange() != null
                                    && "http://www.w3.org/2001/XMLSchema#dateTimeStamp".equals(dp.getRange().getURI())) {

                                props.add(dp.getLocalName());
                            }
                        }
                    }
                }
            }
        }
        return props;
    });
    }

// 🌐 Get URI Properties
    public Set<String> getURIDataProperties(String className) {
        return withReadLock(() -> {
        Set<String> uriProps = new HashSet<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            for (Iterator<DatatypeProperty> it = model.listDatatypeProperties(); it.hasNext();) {
                DatatypeProperty dp = it.next();
                for (ExtendedIterator<? extends Resource> itDomain = dp.listDomain(); itDomain.hasNext();) {
                    Resource domain = itDomain.next();
                    if (domain.canAs(OntClass.class)) {
                        OntClass domainClass = domain.as(OntClass.class);
                        if (domainClass.equals(ontClass) || ontClass.hasSuperClass(domainClass, true)) {
                            if (dp.getRange() != null && dp.getRange().getURI().equals(XSD.anyURI.getURI())) {
                                uriProps.add(dp.getLocalName());
                            }
                        }
                    }
                }
            }
        }
        return uriProps;
    });
    }

    public Map<String, String> getDataPropertyRanges(String className) {
        return withReadLock(() -> {
        Map<String, String> propertyRanges = new HashMap<>();
        Set<String> dataProps = getDataProperties(className);

        for (String prop : dataProps) {
            Property property = model.getProperty(NS + prop);
            if (property != null) {
                StmtIterator it = property.listProperties(RDFS.range);
                while (it.hasNext()) {
                    RDFNode range = it.nextStatement().getObject();
                    if (range.isResource()) {
                        String rangeURI = range.asResource().getURI();
                        if (rangeURI != null && rangeURI.startsWith("http://www.w3.org/2001/XMLSchema#")) {
                            propertyRanges.put(prop, rangeURI);
                            break; // Take the first valid range
                        }
                    }
                }
            }
        }

        return propertyRanges;
    });
    }

    public Map<String, List<String>> getEnumeratedDataProperties(String className) {
        return withReadLock(() -> {
            Map<String, List<String>> enumMap = new HashMap<>();

        OntClass ontClass = model.getOntClass(NS + className);
        if (ontClass == null) {
            System.err.println("❌ OntClass not found for: " + className);
            return enumMap;
        }

        Set<String> classProperties = new HashSet<>();
        ExtendedIterator<DatatypeProperty> allDataProps = model.listDatatypeProperties();
        while (allDataProps.hasNext()) {
            DatatypeProperty prop = allDataProps.next();

            // Check if this property is declared for the class
            StmtIterator domains = prop.listProperties(RDFS.domain);
            while (domains.hasNext()) {
                RDFNode domain = domains.next().getObject();
                if (domain.isResource()) {
                    Resource domainRes = domain.asResource();
                    if (domainRes.equals(ontClass) || ontClass.hasSuperClass(domainRes)) {
                        classProperties.add(prop.getURI());
                    }
                }
            }
        }

        for (String propURI : classProperties) {
            DatatypeProperty prop = model.getDatatypeProperty(propURI);

            // Check for range definition
            StmtIterator rangeIt = prop.listProperties(RDFS.range);
            while (rangeIt.hasNext()) {
                RDFNode rangeNode = rangeIt.nextStatement().getObject();

                // We are only interested in anonymous (blank node) range definitions
                if (rangeNode != null && rangeNode.isAnon()) {
                    Resource rangeRes = rangeNode.asResource();

                    // Look for owl:oneOf list inside the anonymous range
                    Statement oneOfStmt = rangeRes.getProperty(OWL.oneOf);
                    if (oneOfStmt != null && oneOfStmt.getObject().canAs(RDFList.class)) {
                        RDFList rdfList = oneOfStmt.getObject().as(RDFList.class);

                        List<String> enumValues = rdfList.iterator().toList().stream()
                                .filter(RDFNode::isLiteral)
                                .map(node -> node.asLiteral().getString())
                                .collect(Collectors.toList());

                        if (!enumValues.isEmpty()) {
                            enumMap.put(prop.getLocalName(), enumValues);
                            System.out.println("✅ Enum property detected: " + prop.getLocalName() + " => " + enumValues);
                        }
                    }
                }
            }
        }

        System.out.println("📦 Final enumerated properties for class " + className + ": " + enumMap);
        return enumMap;
    });
    }

    public Map<String, Map<String, Map<String, List<String>>>> getIndividualsPropertiesForComparison(String className, List<String> individuals) {
        return withReadLock(() -> {
            Map<String, Map<String, Map<String, List<String>>>> result = new HashMap<>();

        for (String individualName : individuals) {
            Map<String, List<String>> dataMap = getAllDataPropertiesForIndividual(individualName);
            Map<String, List<String>> objectMap = getAllObjectPropertiesWithReasoner(individualName);

            Map<String, Map<String, List<String>>> individualProps = new HashMap<>();
            individualProps.put("data", dataMap);
            individualProps.put("object", objectMap);

            result.put(individualName, individualProps);
        }

        return result;
    });
    }

    public Map<String, List<Double>> getAllNumericPropertyValues(String className) {
        return withReadLock(() -> {
            Map<String, List<Double>> statsMap = new HashMap<>();
        Set<String> numericProps = getNumericDataProperties(className);
        OntClass cls = model.getOntClass(NS + className);

        if (cls != null) {
            ExtendedIterator<? extends OntResource> it = cls.listInstances();
            while (it.hasNext()) {
                Individual ind = it.next().as(Individual.class);
                for (String prop : numericProps) {
                    Property p = model.getProperty(NS + prop);
                    StmtIterator stmtIt = ind.listProperties(p);
                    while (stmtIt.hasNext()) {
                        RDFNode obj = stmtIt.next().getObject();
                        if (obj.isLiteral()) {
                            try {
                                double val = obj.asLiteral().getDouble();
                                statsMap.computeIfAbsent(prop, k -> new ArrayList<>()).add(val);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        }
        return statsMap;
    });
    }

    //Enforce specific order for showing data and object properties in the UI based on "displayOrder" metadata
    private int getDisplayOrder(OntProperty prop) {
        return withReadLock(() -> {
        for (StmtIterator annots = prop.listProperties(); annots.hasNext();) {
            Statement stmt = annots.nextStatement();
            if (stmt.getPredicate().getLocalName().equals("displayOrder") && stmt.getObject().isLiteral()) {
                try {
                    return stmt.getObject().asLiteral().getInt(); // assumes xsd:integer
                } catch (Exception e) {
                    System.err.println("⚠️ Could not parse displayOrder for property " + prop.getLocalName());
                }
            }
        }
        return Integer.MAX_VALUE; // No order defined → push to bottom
    });
    }

    public Map<String, List<String>> getInverseObjectProperties(String individualName) {
        return withReadLock(() -> {
            System.out.println("🔍 Checking inverse object properties for individual: " + individualName);

        OntModel reasonedModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Map<String, List<String>> inverseProps = new HashMap<>();
        Resource individual = reasonedModel.getResource(NS + individualName);
        System.out.println("🔎 Resource: " + individual);
        if (individual != null) {
            StmtIterator it = reasonedModel.listStatements(null, null, individual);
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();
                System.out.println("📌 Found inverse triple: " + stmt);
                String propName = stmt.getPredicate().getLocalName();
                String subject = stmt.getSubject().getLocalName();

                if (subject != null && propName != null) {
                    inverseProps.computeIfAbsent(propName, k -> new ArrayList<>()).add(subject);
                }
            }
        }

        return inverseProps;
    });
    }

    //returns a map of blueprint classes and a passed annotation
    public Map<String, List<String>> getClassGroupingsByAnnotation(String annotationName) {
        return withReadLock(() -> {

        System.out.println("🔍 Annotation requested: " + annotationName);

        Map<String, List<String>> grouped = new HashMap<>();
        Set<String> allClasses = getOntologyClasses();

        Property annotationProp;

        // Handle built-in annotations like rdfs:comment
        if ("comment".equals(annotationName)) {
            annotationProp = RDFS.comment;
            System.out.println("🧭 Using built-in RDFS.comment as annotation property");
        } else {
            annotationProp = model.getProperty(NS + annotationName);
            System.out.println("🧭 Using custom property: " + annotationName);
            System.out.println("🔗 Resolved URI: " + NS + annotationName);
        }

        if (annotationProp == null) {
            System.out.println("❌ Annotation property not found in model.");
            return grouped;
        }

        System.out.println("📦 Total classes found: " + allClasses.size());

        for (String className : allClasses) {
            OntClass ontClass = model.getOntClass(NS + className);
            if (ontClass == null) {
                System.out.println("⚠️ OntClass not found for: " + className);
                continue;
            }

            System.out.println("🔍 Checking class: " + className);

            StmtIterator annotations = ontClass.listProperties(annotationProp);
            while (annotations.hasNext()) {
                Statement stmt = annotations.nextStatement();
                RDFNode object = stmt.getObject();

                String groupKey = null;
                if (object.isResource()) {
                    groupKey = object.asResource().getLocalName();
                    System.out.println("🔗 Resource annotation for class " + className + ": " + groupKey);
                } else if (object.isLiteral()) {
                    groupKey = object.asLiteral().getString();
                    System.out.println("📝 Literal annotation for class " + className + ": " + groupKey);
                }

                if (groupKey != null && !groupKey.isEmpty()) {
                    grouped.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(className);
                }
            }
        }

        System.out.println("✅ Grouping complete. Groups found: " + grouped.size());
        return grouped;
    });
    }

    /**
     * Retrieves a list of local names of individuals connected to the given
     * subject via a specified object property (supports 0:N relationships).
     *
     * @param subject The local name of the subject individual (e.g., "bom_1").
     * @param propertyName The local name of the object property (e.g.,
     * "includesMaterial").
     * @return A list of local names of target individuals, or an empty list if
     * none found.
     */
    /* public List<String> getObjectPropertyValues(String subject, String propertyName) {
        System.out.println("[DEBUG] Fetching list of object values for '" + propertyName + "' from subject '" + subject + "'");

        List<String> values = new ArrayList<>();
        Individual ind = model.getIndividual(NS + subject);
        if (ind == null) {
            System.out.println("[WARN] Subject individual not found: " + subject);
            return values;
        }

        Property prop = model.getProperty(NS + propertyName);
        if (prop == null) {
            System.out.println("[WARN] Property not found: " + propertyName);
            return values;
        }

        NodeIterator nodes = model.listObjectsOfProperty(ind, prop);
        while (nodes.hasNext()) {
            RDFNode node = nodes.next();
            if (node.isResource()) {
                String localName = node.asResource().getLocalName();
                System.out.println("[DEBUG] Found linked object: " + localName);
                values.add(localName);
            }
        }

        if (values.isEmpty()) {
            System.out.println("[INFO] No objects found for property: " + propertyName);
        }

        return values;
    } */
    public List<String> getObjectPropertyValues(String subject, String propertyName) {
        return withReadLock(() -> {
            System.out.println("[DEBUG] Fetching list of object values for '" + propertyName + "' from subject '" + subject + "'");

        List<String> values = new ArrayList<>();

        OntModel reasonedModel = getReasonedModel(); // 🔍 Use inferred model

        Individual ind = reasonedModel.getIndividual(NS + subject);
        if (ind == null) {
            System.out.println("[WARN] Subject individual not found: " + subject);
            return values;
        }

        Property prop = reasonedModel.getProperty(NS + propertyName);
        if (prop == null) {
            System.out.println("[WARN] Property not found: " + propertyName);
            return values;
        }

        ExtendedIterator<RDFNode> nodes = reasonedModel.listObjectsOfProperty(ind, prop);
        while (nodes.hasNext()) {
            RDFNode node = nodes.next();
            if (node.isResource()) {
                String localName = node.asResource().getLocalName();
                System.out.println("[DEBUG] Found linked object: " + localName);
                values.add(localName);
            }
        }

        if (values.isEmpty()) {
            System.out.println("[INFO] No objects found for property: " + propertyName);
        }

        return values;
    });
    }

    // Retrieves all instances of a given class, including instances of its subclasses using reasoning.
// Each instance is returned with its most specific subclass type for clearer UI representation.
// Useful when object property ranges are abstract superclasses with no direct instances.
    public List<Map<String, String>> getInstancesWithTypes(String className) {
        return withReadLock(() -> {
            List<Map<String, String>> results = new ArrayList<>();

        OntClass parentClass = model.getOntClass(NS + className);
        if (parentClass == null) {
            return results;
        }

        // Use reasoner to include subclasses
        OntModel reasonedModel = getReasonedModel();
        OntClass reasonedClass = reasonedModel.getOntClass(NS + className);

        ExtendedIterator<? extends OntResource> instances = reasonedClass.listInstances();

        while (instances.hasNext()) {
            Individual ind = instances.next().as(Individual.class);

            String name = ind.getLocalName();
            String type = "";

            boolean isDirectInstance = false; 

            for (ExtendedIterator<OntClass> types = ind.listOntClasses(true); types.hasNext();) {
                OntClass cls = types.next();

                if (cls.getLocalName() != null && cls.getLocalName().equals(className)) {
                    isDirectInstance = true;
                }

                if (cls.getLocalName() != null
                        && !cls.getLocalName().equals(className)
                        && !cls.getLocalName().equals("NamedIndividual")) {

                    type = cls.getLocalName();
                }
            }

            // Only show type if NOT direct instance
            if (isDirectInstance) {
                type = "";
            }

            Map<String, String> entry = new HashMap<>();
            entry.put("name", name);
            entry.put("type", type);

            results.add(entry);
        }

        return results;
    });
    }

    // 🔧 Add this method to OntologyReader.java
    public List<String> getIndividualsByClass(String className) {
        return withReadLock(() -> {
            List<String> individuals = new ArrayList<>();

        OntClass ontClass = model.getOntClass(NS + className);
        if (ontClass != null) {
            ExtendedIterator<? extends OntResource> instances = ontClass.listInstances();
            while (instances.hasNext()) {
                Individual individual = instances.next().asIndividual();
                individuals.add(individual.getLocalName());
            }
        }

        return individuals;
    });
    }



    public Map<String, List<String>> getIndividualDetailsMulti(String individualName) {
return withReadLock(() -> {
    Map<String, List<String>> details = new HashMap<>();

    Individual individual = model.getIndividual(NS + individualName);

    if (individual == null) {
        return details;
    }

    StmtIterator stmts = individual.listProperties();

    while (stmts.hasNext()) {

        Statement stmt = stmts.nextStatement();

        String propertyName = stmt.getPredicate().getLocalName();

        String value;

        if (stmt.getObject().isResource()) {
            value = stmt.getObject().asResource().getURI();
        } else {
            value = stmt.getObject().asLiteral().getString();
        }

        details.computeIfAbsent(propertyName, k -> new ArrayList<>())
               .add(value);
    }

    return details;


});
    }
        /**
     * Publishes a successfully persisted ontology model. Used by
     * KpiEvaluationService.
     */
    static void replaceModelAfterSave(OntModel updatedModel) {
        if (updatedModel == null) {
            throw new IllegalArgumentException(
                    "Updated ontology model cannot be null.");
        }
        model = updatedModel;
    }

    /**
     * Preserves the existing Material Intensity evaluation API.
     *
     * Existing callers with five arguments continue to work.
     */
    public String createKpiEvaluation(
            String evaluationName,
            String evaluationId,
            double computedValue,
            String calculationMethod,
            String resultProvenance) throws IOException {

        return createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                calculationMethod,
                resultProvenance,
                "MaterialIntensityKPI",
                "MaterialIntensityFormula");
    }

    /**
     * Creates and persists a KPI evaluation for the specified KPI and formula
     * individuals.
     *
     * Supports Material Intensity and Renewable Content.
     */
    public String createKpiEvaluation(
            String evaluationName,
            String evaluationId,
            double computedValue,
            String calculationMethod,
            String resultProvenance,
            String kpiIndividualName,
            String formulaIndividualName) throws IOException {

        return createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                calculationMethod,
                resultProvenance,
                kpiIndividualName,
                formulaIndividualName,
                null,
                null);
    }

    /**
     * Creates and persists a KPI evaluation with an optional evaluated entity
     * and an optional additional context.
     *
     * For Renewable Content: evaluatedEntityName = raw material individual
     * contextEntityName = manufactured product individual
     *
     * Both context arguments may be null for legacy callers.
     */
    public String createKpiEvaluation(
            String evaluationName,
            String evaluationId,
            double computedValue,
            String calculationMethod,
            String resultProvenance,
            String kpiIndividualName,
            String formulaIndividualName,
            String evaluatedEntityName,
            String contextEntityName) throws IOException {

        if (!Double.isFinite(computedValue)) {
            throw new IllegalArgumentException(
                    "Computed KPI value must be finite.");
        }

        MODEL_LOCK.writeLock().lock();
        try {
        synchronized (OntologyReader.class) {

            OntModel currentModel = model;

            if (currentModel == null) {
                throw new IllegalStateException(
                        "Ontology model is not initialized.");
            }

            if (currentModel.getIndividual(
                    NS + evaluationName) != null) {

                throw new IllegalStateException(
                        "Evaluation already exists: "
                        + evaluationName);
            }

            // Validate the selected KPI and formula.
            if (currentModel.getIndividual(
                    NS + kpiIndividualName) == null
                    || currentModel.getIndividual(
                            NS + formulaIndividualName) == null) {

                throw new IllegalStateException(
                        "KPI or formula not found: "
                        + kpiIndividualName + " / "
                        + formulaIndividualName);
            }

            OntClass evaluationClass
                    = currentModel.getOntClass(
                            NS + "KPIEvaluation");

            if (evaluationClass == null) {
                throw new IllegalStateException(
                        "KPIEvaluation class not found.");
            }

            // Validate the optional evaluated entity.
            if (evaluatedEntityName != null) {

                if (evaluatedEntityName.trim().isEmpty()
                        || currentModel.getIndividual(
                                NS + evaluatedEntityName) == null) {

                    throw new IllegalArgumentException(
                            "Evaluated entity not found: "
                            + evaluatedEntityName);
                }

                if (currentModel.getObjectProperty(
                        NS + "hasEvaluatedEntity") == null) {

                    throw new IllegalStateException(
                            "Object property hasEvaluatedEntity not found.");
                }
            }

            // Validate the optional evaluation context.
            if (contextEntityName != null) {

                if (contextEntityName.trim().isEmpty()
                        || currentModel.getIndividual(
                                NS + contextEntityName) == null) {

                    throw new IllegalArgumentException(
                            "Evaluation context not found: "
                            + contextEntityName);
                }

                if (currentModel.getObjectProperty(
                        NS + "hasEvaluationContext") == null) {

                    throw new IllegalStateException(
                            "Object property hasEvaluationContext not found.");
                }
            }

            // Preserve all existing ontology statements.
            OntModel updatedModel
                    = ModelFactory.createOntologyModel(
                            OntModelSpec.OWL_MEM);

            updatedModel.add(currentModel);

            Individual evaluation
                    = updatedModel.createIndividual(
                            NS + evaluationName,
                            updatedModel.getOntClass(
                                    NS + "KPIEvaluation"));

            evaluation.addProperty(
                    updatedModel.getProperty(
                            NS + "evaluationID"),
                    evaluationId);

            // Read the datatype declared in the ontology.
            Map<String, String> propertyRanges
                    = getDataPropertyRanges("KPIEvaluation");

            String computedValueRange
                    = propertyRanges.get("computedValue");

            if (computedValueRange == null) {
                throw new IllegalStateException(
                        "No datatype range declared "
                        + "for computedValue.");
            }

            // Create the literal using the ontology's
            // declared datatype.
            Literal computedValueLiteral;

            switch (computedValueRange) {

                case "http://www.w3.org/2001/XMLSchema#decimal":

                    computedValueLiteral
                            = updatedModel.createTypedLiteral(
                                    java.math.BigDecimal.valueOf(
                                            computedValue));
                    break;

                case "http://www.w3.org/2001/XMLSchema#double":

                    computedValueLiteral
                            = updatedModel.createTypedLiteral(
                                    computedValue);
                    break;

                case "http://www.w3.org/2001/XMLSchema#float":

                    computedValueLiteral
                            = updatedModel.createTypedLiteral(
                                    (float) computedValue);
                    break;

                case "http://www.w3.org/2001/XMLSchema#integer":

                    if (computedValue != Math.rint(
                            computedValue)) {

                        throw new IllegalArgumentException(
                                "computedValue is not an integer: "
                                + computedValue);
                    }

                    computedValueLiteral
                            = updatedModel.createTypedLiteral(
                                    java.math.BigDecimal.valueOf(
                                            computedValue)
                                            .toBigIntegerExact());
                    break;

                default:

                    throw new IllegalStateException(
                            "Unsupported datatype "
                            + "for computedValue: "
                            + computedValueRange);
            }

            evaluation.addLiteral(
                    updatedModel.getProperty(
                            NS + "computedValue"),
                    computedValueLiteral);

            evaluation.addProperty(
                    updatedModel.getProperty(
                            NS + "evaluationStatus"),
                    "computed");

            // UTC timestamp with whole-second precision.
            // Matches the ontology's xsd:dateTime range.
            String timestamp = Instant.now()
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toString();

            evaluation.addLiteral(
                    updatedModel.getProperty(
                            NS + "evaluationTimestamp"),
                    updatedModel.createTypedLiteral(
                            timestamp,
                            XSDDatatype.XSDdateTime));

            evaluation.addProperty(
                    updatedModel.getProperty(
                            NS + "calculationMethod"),
                    calculationMethod);

            evaluation.addProperty(
                    updatedModel.getProperty(
                            NS + "resultProvenance"),
                    resultProvenance);

            // Link to the selected KPI.
            evaluation.addProperty(
                    updatedModel.getProperty(
                            NS + "evaluatesKPI"),
                    updatedModel.getResource(
                            NS + kpiIndividualName));

            // Link to the selected formula.
            evaluation.addProperty(
                    updatedModel.getProperty(
                            NS + "usesFormula"),
                    updatedModel.getResource(
                            NS + formulaIndividualName));

            // Link to the primary evaluated entity.
            if (evaluatedEntityName != null) {

                evaluation.addProperty(
                        updatedModel.getObjectProperty(
                                NS + "hasEvaluatedEntity"),
                        updatedModel.getIndividual(
                                NS + evaluatedEntityName));
            }

            // Link to the additional evaluation context.
            if (contextEntityName != null) {

                evaluation.addProperty(
                        updatedModel.getObjectProperty(
                                NS + "hasEvaluationContext"),
                        updatedModel.getIndividual(
                                NS + contextEntityName));
            }

            // Save without replacing the original file
            // until serialization has completed.
            Path ontologyFile
                    = Path.of(ONTOLOGY_PATH);

            Path temporary
                    = Files.createTempFile(
                            ontologyFile.getParent(),
                            "narrate-evaluation-",
                            ".rdf");

            try {

                try (java.io.OutputStream output
                        = Files.newOutputStream(temporary)) {

                    updatedModel.write(
                            output,
                            "RDF/XML-ABBREV");
                }

                Files.move(
                        temporary,
                        ontologyFile,
                        StandardCopyOption.REPLACE_EXISTING);

            } finally {

                Files.deleteIfExists(temporary);
            }

            // Update the shared singleton model.
            replaceModelAfterSave(updatedModel);

            System.out.println(
                    "[KPI Evaluation] Saved: "
                    + evaluationName
                    + " | KPI=" + kpiIndividualName
                    + " | Formula=" + formulaIndividualName
                    + " | EvaluatedEntity=" + evaluatedEntityName
                    + " | Context=" + contextEntityName
                    + " | timestamp=" + timestamp);

            return evaluationName;
        }
        } finally {
            MODEL_LOCK.writeLock().unlock();
        }
    }

    /**
     * Creates and persists a BenchmarkAssessment individual linked to an
     * existing KPIEvaluation and an existing benchmark.
     *
     * Uses the datatype ranges declared in the ontology and preserves the
     * current in-memory model if persistence fails.
     *
     * @return the local name of the saved BenchmarkAssessment individual
     */
    public String createBenchmarkAssessment(
            String evaluationName,
            String benchmarkName,
            boolean meetsBenchmark,
            double absoluteDeviation,
            double relativeDeviationPercent,
            String assessmentStatus,
            String assessmentExplanation) throws IOException {

        // 1. Validate input.
        if (evaluationName == null || evaluationName.trim().isEmpty()
                || benchmarkName == null || benchmarkName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Evaluation and benchmark names are required.");
        }

        if (!Double.isFinite(absoluteDeviation)
                || !Double.isFinite(relativeDeviationPercent)) {
            throw new IllegalArgumentException(
                    "Benchmark deviations must be finite.");
        }

        if (assessmentStatus == null || assessmentStatus.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Assessment status is required.");
        }

        MODEL_LOCK.writeLock().lock();
        try {
        synchronized (OntologyReader.class) {

            OntModel currentModel = model;

            if (currentModel == null) {
                throw new IllegalStateException(
                        "Ontology model is not initialized.");
            }

            // 2. Validate ontology classes and referenced individuals.
            OntClass assessmentClass
                    = currentModel.getOntClass(NS + "BenchmarkAssessment");

            if (assessmentClass == null) {
                throw new IllegalStateException(
                        "BenchmarkAssessment class not found.");
            }

            OntClass evaluationClass
                    = currentModel.getOntClass(NS + "KPIEvaluation");

            if (evaluationClass == null) {
                throw new IllegalStateException(
                        "KPIEvaluation class not found.");
            }

            Individual evaluation
                    = currentModel.getIndividual(NS + evaluationName);

            if (evaluation == null
                    || !evaluation.hasRDFType(evaluationClass)) {
                throw new IllegalStateException(
                        "Valid KPIEvaluation not found: " + evaluationName);
            }

            Individual benchmark
                    = currentModel.getIndividual(NS + benchmarkName);

            if (benchmark == null) {
                throw new IllegalStateException(
                        "Benchmark individual not found: " + benchmarkName);
            }

            // 3. Validate required datatype properties.
            String[] dataProperties = {
                "assessmentStatus",
                "meetsBenchmark",
                "absoluteDeviation",
                "relativeDeviationPercent",
                "assessmentTimestamp",
                "assessmentExplanation"
            };

            for (String propertyName : dataProperties) {
                if (currentModel.getDatatypeProperty(
                        NS + propertyName) == null) {
                    throw new IllegalStateException(
                            "Datatype property not found: " + propertyName);
                }
            }

            // 4. Validate required object properties.
            if (currentModel.getObjectProperty(
                    NS + "assessesEvaluation") == null) {
                throw new IllegalStateException(
                        "Object property assessesEvaluation not found.");
            }

            if (currentModel.getObjectProperty(
                    NS + "assessesAgainstBenchmark") == null) {
                throw new IllegalStateException(
                        "Object property assessesAgainstBenchmark not found.");
            }

            // 5. Read datatype ranges from the ontology.
            Map<String, String> ranges
                    = getDataPropertyRanges("BenchmarkAssessment");

            String absoluteRange = ranges.get("absoluteDeviation");
            String relativeRange = ranges.get("relativeDeviationPercent");
            String timestampRange = ranges.get("assessmentTimestamp");
            String statusRange = ranges.get("assessmentStatus");
            String explanationRange = ranges.get("assessmentExplanation");
            String meetsRange = ranges.get("meetsBenchmark");

            final String xsd
                    = "http://www.w3.org/2001/XMLSchema#";

            for (String range
                    : new String[]{absoluteRange, relativeRange}) {

                if (!(xsd + "decimal").equals(range)
                        && !(xsd + "double").equals(range)
                        && !(xsd + "float").equals(range)) {
                    throw new IllegalStateException(
                            "Unsupported or missing deviation datatype: "
                            + range);
                }
            }

            if (!(xsd + "dateTime").equals(timestampRange)
                    && !(xsd + "dateTimeStamp").equals(timestampRange)) {
                throw new IllegalStateException(
                        "Unsupported assessmentTimestamp datatype: "
                        + timestampRange);
            }

            if (!(xsd + "boolean").equals(meetsRange)) {
                throw new IllegalStateException(
                        "meetsBenchmark must have xsd:boolean range.");
            }

            if (!(xsd + "string").equals(statusRange)
                    || !(xsd + "string").equals(explanationRange)) {
                throw new IllegalStateException(
                        "Assessment status and explanation "
                        + "must have xsd:string ranges.");
            }

            // 6. Create a copy of the current ontology.
            OntModel updatedModel
                    = ModelFactory.createOntologyModel(
                            OntModelSpec.OWL_MEM);

            updatedModel.add(currentModel);

            String assessmentName
                    = "BenchmarkAssessment_"
                    + java.util.UUID.randomUUID()
                            .toString().replace("-", "");

            Individual assessment
                    = updatedModel.createIndividual(
                            NS + assessmentName,
                            updatedModel.getOntClass(
                                    NS + "BenchmarkAssessment"));

            // 7. Store assessment status.
            assessment.addProperty(
                    updatedModel.getDatatypeProperty(
                            NS + "assessmentStatus"),
                    assessmentStatus);

            // 8. Store pass/fail result.
            assessment.addLiteral(
                    updatedModel.getDatatypeProperty(
                            NS + "meetsBenchmark"),
                    updatedModel.createTypedLiteral(meetsBenchmark));

            // 9. Store absolute deviation with its declared datatype.
            Literal absoluteLiteral;

            if ((xsd + "decimal").equals(absoluteRange)) {
                absoluteLiteral = updatedModel.createTypedLiteral(
                        java.math.BigDecimal.valueOf(absoluteDeviation));

            } else if ((xsd + "float").equals(absoluteRange)) {
                absoluteLiteral = updatedModel.createTypedLiteral(
                        (float) absoluteDeviation);

            } else {
                absoluteLiteral = updatedModel.createTypedLiteral(
                        absoluteDeviation);
            }

            assessment.addLiteral(
                    updatedModel.getDatatypeProperty(
                            NS + "absoluteDeviation"),
                    absoluteLiteral);

            // 10. Store relative deviation with its declared datatype.
            Literal relativeLiteral;

            if ((xsd + "decimal").equals(relativeRange)) {
                relativeLiteral = updatedModel.createTypedLiteral(
                        java.math.BigDecimal.valueOf(
                                relativeDeviationPercent));

            } else if ((xsd + "float").equals(relativeRange)) {
                relativeLiteral = updatedModel.createTypedLiteral(
                        (float) relativeDeviationPercent);

            } else {
                relativeLiteral = updatedModel.createTypedLiteral(
                        relativeDeviationPercent);
            }

            assessment.addLiteral(
                    updatedModel.getDatatypeProperty(
                            NS + "relativeDeviationPercent"),
                    relativeLiteral);

            // 11. Store timestamp using the ontology's declared datatype.
            String timestamp = Instant.now()
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toString();

            org.apache.jena.datatypes.RDFDatatype timestampDatatype
                    = org.apache.jena.datatypes.TypeMapper
                            .getInstance()
                            .getSafeTypeByName(timestampRange);

            assessment.addLiteral(
                    updatedModel.getDatatypeProperty(
                            NS + "assessmentTimestamp"),
                    updatedModel.createTypedLiteral(
                            timestamp,
                            timestampDatatype));

            // 12. Store explanation.
            assessment.addProperty(
                    updatedModel.getDatatypeProperty(
                            NS + "assessmentExplanation"),
                    assessmentExplanation == null
                            ? ""
                            : assessmentExplanation);

            // 13. Link assessment to the existing KPIEvaluation.
            assessment.addProperty(
                    updatedModel.getObjectProperty(
                            NS + "assessesEvaluation"),
                    updatedModel.getIndividual(
                            NS + evaluationName));

            // 14. Link assessment to the existing benchmark.
            assessment.addProperty(
                    updatedModel.getObjectProperty(
                            NS + "assessesAgainstBenchmark"),
                    updatedModel.getIndividual(
                            NS + benchmarkName));

            // 15. Persist using a temporary file.
            Path ontologyFile = Path.of(ONTOLOGY_PATH);

            Path temporary = Files.createTempFile(
                    ontologyFile.getParent(),
                    "narrate-benchmark-assessment-",
                    ".rdf");

            try {
                try (java.io.OutputStream output
                        = Files.newOutputStream(temporary)) {

                    updatedModel.write(
                            output,
                            "RDF/XML-ABBREV");
                }

                try {
                    Files.move(
                            temporary,
                            ontologyFile,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);

                } catch (java.nio.file.AtomicMoveNotSupportedException ex) {

                    Files.move(
                            temporary,
                            ontologyFile,
                            StandardCopyOption.REPLACE_EXISTING);
                }

            } finally {
                Files.deleteIfExists(temporary);
            }

            // 16. Publish the successfully saved model.
            replaceModelAfterSave(updatedModel);

            System.out.println(
                    "[BenchmarkAssessment] Saved: "
                    + assessmentName
                    + " | Evaluation=" + evaluationName
                    + " | Benchmark=" + benchmarkName
                    + " | Status=" + assessmentStatus
                    + " | Timestamp=" + timestamp);

            return assessmentName;
        }
        } finally {
            MODEL_LOCK.writeLock().unlock();
        }
    }

}

