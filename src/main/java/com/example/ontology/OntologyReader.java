/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ontology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.jena.ontology.SomeValuesFromRestriction;

public class OntologyReader {

    // ---- Preferred (Render/Prod): environment variables ----
    private static final String DEFAULT_LINUX_PATH = "/data/NARRATE-blueprints-rdf-xml.rdf";
    private static final String ONTOLOGY_PATH = System.getenv().getOrDefault("ONTOLOGY_PATH", DEFAULT_LINUX_PATH);

    // Namespace: use env if provided; otherwise we'll infer after loading
    private static String NS = System.getenv().getOrDefault("ONTOLOGY_NS", null);
    public static String getNS() { return NS; }

    // ---- Local dev fallback (for colleague Windows setups) ----
    private static final String COLLEAGUE_WINDOWS_PATH = "C:/Programs/NARRATE-blueprints-rdf-xml.rdf";
    private static final String COLLEAGUE_NS_DEFAULT =
        "http://www.semanticweb.org/amal.elgammal/ontologies/2025/3/untitled-ontology-31#";

    // Model / Singleton
    private static volatile OntModel model;
    private static volatile OntologyReader instance;

    // OWL qualified-cardinality helpers
    private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    private static final Property OWL_MIN_QUALIFIED_CARDINALITY =
            ResourceFactory.createProperty(OWL_NS + "minQualifiedCardinality");
    private static final Property OWL_MAX_QUALIFIED_CARDINALITY =
            ResourceFactory.createProperty(OWL_NS + "maxQualifiedCardinality");
    private static final Property OWL_QUALIFIED_CARDINALITY =
            ResourceFactory.createProperty(OWL_NS + "qualifiedCardinality");

    // ✅ Private constructor to prevent instantiation
    private OntologyReader() {
        loadOntologyModel();
    }

    // ✅ Singleton Method to get the instance
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

// ✅ Load the Ontology Model
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

    // ✅ Reload Ontology Model (Ensures real-time updates)
    public static void reloadModel() {
        System.out.println("♻️ Reloading Ontology Model...");
        synchronized (OntologyReader.class) { // Ensure thread safety
            loadOntologyModel();
            System.out.println("✅ Ontology model reloaded successfully.");

            // 🔍 Debug: Print all individuals after reloading
            System.out.println("📌 Individuals After Reload:");
            for (OntClass cls : model.listNamedClasses().toList()) {
                for (ExtendedIterator<? extends OntResource> i = cls.listInstances(); i.hasNext();) {
                    Individual ind = (Individual) i.next();
                    System.out.println("🔹 " + ind.getLocalName());
                }
            }
        }
    }

    // ✅ Get OntModel instance
    public static OntModel getModel() {
        return model;
    }

    public OntModel getReasonedModel() {
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
    }

    // Method to get all classes
    public Set<String> getOntologyClasses() {
        Set<String> classSet = new HashSet<>();
        ExtendedIterator<OntClass> classIter = model.listClasses();

        while (classIter.hasNext()) {
            OntClass ontClass = classIter.next();
            if (ontClass.getLocalName() != null) {
                classSet.add(ontClass.getLocalName());
            }
        }

        return classSet;
    }

    public List<Map<String, Object>> getDataPropertiesWithMeta(String className) {
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
    }

    // New method to return Data Property names + comments
    public Map<String, String> getDataPropertiesWithComments(String className) {
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
    }

    public Set<String> getDataProperties(String className) {
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
    }

    public Set<String> getObjectProperties(String className) {
        return getObjectProperties(className, true); // Default: exclude inverses
    }

    public Map<String, String> getObjectPropertiesWithComments(String className, boolean excludeInverses) {
        System.out.println("Entering getObjectPropertiesWithComments method");
        Map<String, String> propertiesWithComments = new HashMap<>();
        OntClass ontClass = model.getOntClass(NS + className);

        if (ontClass != null) {
            for (Iterator<ObjectProperty> it = model.listObjectProperties(); it.hasNext();) {
                ObjectProperty op = it.next();

                if (excludeInverses) {
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
                            String comment = "";
                            if (op.hasProperty(RDFS.comment)) {
                                comment = op.getProperty(RDFS.comment).getObject().asLiteral().getString();
                            }
                            propertiesWithComments.put(op.getLocalName(), comment);
                        }
                    }
                }
            }
        }

        return propertiesWithComments;
    }

    public Map<String, String> getDataPropertiesForIndividual(String individualName) {
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
    }

    //not used across application
    public Map<String, String> getObjectPropertiesForIndividual(String individualName) {
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
    }

    public Set<String> getInstancesOfClass(String className) {
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
    }

    /**
     * Retrieves the range class of an object property.
     *
     * @param objectProperty The name of the object property.
     * @return The name of the range class, or null if not found.
     */
    public String getRangeClass(String objectProperty) {
        ObjectProperty prop = model.getObjectProperty(NS + objectProperty);

        if (prop == null) {
            System.out.println("Object Property not found: " + objectProperty);
            return null;
        }

        StmtIterator it = model.listStatements(prop, RDFS.range, (RDFNode) null);
        while (it.hasNext()) {
            Statement stmt = it.nextStatement();
            return stmt.getObject().asResource().getLocalName(); // Found range class
        }

        return null; // No range class found
    }

    private String getFullURI(String className) {
        for (OntClass cls : model.listNamedClasses().toList()) {
            if (cls.getLocalName() != null && cls.getLocalName().equals(className)) {
                return cls.getURI();
            }
        }
        return null;
    }

    //not used across application
    public Map<String, String> getIndividualDetails(String individualName) {
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
    }

    public boolean deleteIndividual(String individualName) {
        System.out.println("Deleting individual: " + individualName);

        Individual individual = model.getIndividual(NS + individualName);
        if (individual == null) {
            System.err.println("Error: Individual " + individualName + " not found.");
            return false;
        }

        // Log the triples before deletion
        StmtIterator it = model.listStatements(individual, null, (RDFNode) null);
        while (it.hasNext()) {
            System.out.println("Triple found: " + it.nextStatement());
        }

        // Remove all triples where the individual is subject
        model.removeAll(individual, null, null);

        // Remove all triples where the individual is an object
        model.removeAll(null, null, individual);

        System.out.println("Successfully deleted: " + individualName);

        // Persist changes to OWL file
        try (FileOutputStream out = new FileOutputStream(ONTOLOGY_PATH)) {
            model.write(out, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        reloadModel();
        System.out.println("📂 Loaded ontology from: " + ONTOLOGY_PATH);

        return true;
    }

    public List<String> getIndividualTriples(String individualName) {
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
    }

//UpdateIndividual Method by taking into consideration numeric values, and multi-value properties
    public boolean updateIndividual(String className, String individualName,
            Map<String, Object> dataProps,
            Map<String, Object> objectProps) {

        System.out.println("🚨 updateIndividual() CALLED!");
        System.out.println("👉 className: " + className);
        System.out.println("👉 individualName: " + individualName);
        System.out.println("👉 dataProps: " + dataProps);
        System.out.println("👉 objectProps: " + objectProps);

        Individual individual = model.getIndividual(NS + individualName);
        if (individual == null) {
            System.out.println("❌ Individual not found: " + individualName);
            return false;
        }

        // 🔄 Remove all existing properties
        individual.removeAll(null);

        // 🔍 Detect numeric data properties for this class
        Set<String> numericProps = getNumericDataProperties(className);
        System.out.println("🔢 Numeric properties: " + numericProps);

        // ✅ Add updated Data Properties
        for (Map.Entry<String, Object> entry : dataProps.entrySet()) {
            String prop = entry.getKey();
            Object rawValue = entry.getValue();
            Property property = model.getProperty(NS + prop);
            if (property == null) {
                continue;
            }

            List<Object> values = (rawValue instanceof List)
                    ? (List<Object>) rawValue
                    : Collections.singletonList(rawValue);

            for (Object valueObj : values) {
                String valueStr = valueObj.toString();
                Literal typedLiteral;

                if (numericProps.contains(prop)) {
                    if (valueObj instanceof Number) {
                        double num = ((Number) valueObj).doubleValue();
                        if (num == Math.floor(num)) {
                            typedLiteral = model.createTypedLiteral((int) num);
                            System.out.println("🔢 Parsed Integer for " + prop + ": " + num);
                        } else {
                            typedLiteral = model.createTypedLiteral(num);
                            System.out.println("🔢 Parsed Double for " + prop + ": " + num);
                        }
                    } else {
                        try {
                            if (valueStr.contains(".")) {
                                typedLiteral = model.createTypedLiteral(Double.parseDouble(valueStr));
                            } else {
                                typedLiteral = model.createTypedLiteral(Integer.parseInt(valueStr));
                            }
                            System.out.println("🔢 Parsed from String for " + prop + ": " + valueStr);
                        } catch (NumberFormatException e) {
                            System.err.println("⚠️ Failed to parse number for " + prop + ": " + valueStr);
                            typedLiteral = model.createTypedLiteral(valueStr);
                        }
                    }
                } else {
                    typedLiteral = model.createTypedLiteral(valueStr);
                }

                individual.addProperty(property, typedLiteral);
                // ✅ Print what's actually being stored
                System.out.println("✅ Stored Property: " + prop);
                System.out.println("   → Value: " + typedLiteral.getString());
                System.out.println("   → DataType URI: " + typedLiteral.getDatatypeURI());
                System.out.println("   → Jena Type: " + typedLiteral.getDatatype());
            }
        }

        // ✅ Add updated Object Properties
        for (Map.Entry<String, Object> entry : objectProps.entrySet()) {
            String prop = entry.getKey();
            Object rawValue = entry.getValue();
            Property property = model.getProperty(NS + prop);
            if (property == null) {
                continue;
            }

            List<Object> values = (rawValue instanceof List)
                    ? (List<Object>) rawValue
                    : Collections.singletonList(rawValue);

            for (Object valueObj : values) {
                String value = valueObj.toString();
                Resource objResource = model.getResource(NS + value);
                if (objResource != null) {
                    individual.addProperty(property, objResource);
                }
            }
        }

        // 💾 Save the updated model
        try (FileOutputStream out = new FileOutputStream(ONTOLOGY_PATH)) {
            model.write(out, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        reloadModel();
        System.out.println("📂 Loaded ontology from: " + ONTOLOGY_PATH);
        return true;
    }

    public Map<String, Map<String, Integer>> getPropertyCardinalities(String className) {
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
    }

    public Map<String, Map<String, Integer>> getCardinalities(String className) {
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
    }

    public Map<String, List<String>> getAllDataPropertiesForIndividual(String individualName) {
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
    }

    //not used across application
    public Map<String, List<String>> getAllObjectPropertiesForIndividual(String individualName) {
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
    }

    public Map<String, List<String>> getAllObjectPropertiesWithReasoner(String individualName) {
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
    }

// 🗓️ Get Date Properties
    public Set<String> getDateDataProperties(String className) {
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
    }

// 🌐 Get URI Properties
    public Set<String> getURIDataProperties(String className) {
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
    }

    public Map<String, String> getDataPropertyRanges(String className) {
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
    }

    public Map<String, List<String>> getEnumeratedDataProperties(String className) {
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
    }

    public Map<String, Map<String, Map<String, List<String>>>> getIndividualsPropertiesForComparison(String className, List<String> individuals) {
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
    }

    public Map<String, List<Double>> getAllNumericPropertyValues(String className) {
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
    }

    //Enforce specific order for showing data and object properties in the UI based on "displayOrder" metadata
    private int getDisplayOrder(OntProperty prop) {
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
    }

    public Map<String, List<String>> getInverseObjectProperties(String individualName) {
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
    }

    //returns a map of blueprint classes and a passed annotation
    public Map<String, List<String>> getClassGroupingsByAnnotation(String annotationName) {

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
    }

    // 🔧 Add this method to OntologyReader.java
    public List<String> getIndividualsByClass(String className) {
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
    }

}
