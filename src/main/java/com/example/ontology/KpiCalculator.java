/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ontology;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author amal.elgammal
 */
/**
 * KpiCalculator centralizes all sustainability KPI computations from the OWL
 * knowledge base. This version includes only computeMaterialIntensity. Add more
 * static methods as needed.
 */
public class KpiCalculator {

    /**
     * Computes Material Intensity per material for a given product.
     *
     * Concept: - Each product has a hasProductWeight data property. - Each
     * product has a 1:1 object property to a BillOfMaterials (BoM) individual
     * via hasBoM. - Each BoM includes one or more materials via
     * includesMaterial. - Each material has a hasMaterialWeight data property.
     *
     * Formula: Material Intensity = hasMaterialWeight / hasProductWeight
     *
     * @param productName The product individual name (e.g., "product_1")
     * @param ontologyReader The ontology reader instance for querying RDF data
     * @return A JsonObject with product name and calculated material
     * intensities
     */
    public static JsonObject computeMaterialIntensity(String productName, OntologyReader reader) {
        JsonObject result = new JsonObject();
        result.addProperty("product", productName);
        JsonArray array = new JsonArray();

        try {
            double productWeight = getProductWeight(productName, reader);
            String bom = getBomForProduct(productName, reader);
            if (bom == null) {
                result.addProperty("error", "No BillOfMaterials found.");
                return result;
            }

            for (String material : getMaterialsForBom(bom, reader)) {
                double matWeight = getMaterialWeight(material, reader);
                double intensity = productWeight != 0 ? matWeight / productWeight : 0.0;

                JsonObject entry = new JsonObject();
                entry.addProperty("material", material);
                entry.addProperty("intensity", intensity);
                array.add(entry);
            }

            result.add("materialIntensities", array);

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static JsonObject computeRenewableContent(String productName, OntologyReader ontologyReader) {
        JsonObject result = new JsonObject();
        result.addProperty("product", productName);
        JsonArray array = new JsonArray();

        try {
            // Step 1: Get BoM
            List<String> boms = ontologyReader.getObjectPropertyValues(productName, "hasBOM");
            if (boms.isEmpty()) {
                result.addProperty("error", "No BOM found for product: " + productName);
                return result;
            }
            String bom = boms.get(0);

            // Step 2: Get materials
            List<String> materials = ontologyReader.getObjectPropertyValues(bom, "includesMaterial");

            // Step 3: Sum all material weights
            double totalWeight = 0.0;
            Map<String, Double> weightMap = new HashMap<>();
            for (String material : materials) {
                Map<String, String> matProps = ontologyReader.getDataPropertiesForIndividual(material);
                double weight = matProps.containsKey("hasMaterialWeight")
                        ? Double.parseDouble(matProps.get("hasMaterialWeight")) : 0.0;
                weightMap.put(material, weight);
                totalWeight += weight;
            }

// ⬅️ Add total product weight to result object
            result.addProperty("productWeight", totalWeight);

            // Step 4: Compute renewable content %
            for (String material : materials) {
                double matWeight = weightMap.getOrDefault(material, 0.0);

                double renewableFraction = 0.0;
                List<String> sustNodes = ontologyReader.getObjectPropertyValues(material, "hasMaterialSustainability");
                if (!sustNodes.isEmpty()) {
                    Map<String, String> sustProps = ontologyReader.getDataPropertiesForIndividual(sustNodes.get(0));
                    if (sustProps.containsKey("hasMaterialRenewableContent")) {
                        renewableFraction = Double.parseDouble(sustProps.get("hasMaterialRenewableContent"));
                    }
                }

                double renewablePercent = (matWeight != 0)
                        ? (totalWeight * renewableFraction) / matWeight
                        : 0.0;

                JsonObject entry = new JsonObject();
                entry.addProperty("material", material);
                entry.addProperty("weight", matWeight);
                entry.addProperty("renewableFraction", renewableFraction);
                entry.addProperty("renewablePercentage", renewablePercent);

                array.add(entry);
            }

            result.add("renewableContents", array);

        } catch (Exception e) {
            result.addProperty("error", "Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private static double getProductWeight(String productName, OntologyReader reader) {
        Map<String, String> props = reader.getDataPropertiesForIndividual(productName);
        return props.containsKey("hasProductWeight") ? Double.parseDouble(props.get("hasProductWeight")) : 1.0;
    }

    private static String getBomForProduct(String productName, OntologyReader reader) {
        List<String> boms = reader.getObjectPropertyValues(productName, "hasBOM");
        return boms.isEmpty() ? null : boms.get(0);
    }

    private static List<String> getMaterialsForBom(String bomName, OntologyReader reader) {
        return reader.getObjectPropertyValues(bomName, "includesMaterial");
    }

    private static double getMaterialWeight(String materialName, OntologyReader reader) {
        Map<String, String> props = reader.getDataPropertiesForIndividual(materialName);
        return props.containsKey("hasMaterialWeight") ? Double.parseDouble(props.get("hasMaterialWeight")) : 0.0;
    }

    private static String getSustainabilityNodeForMaterial(String material, OntologyReader reader) {
        List<String> sust = reader.getObjectPropertyValues(material, "hasMaterialSustainability");
        return sust.isEmpty() ? null : sust.get(0);
    }

    private static double getSustainabilityFraction(String node, String property, OntologyReader reader) {
        if (node == null) {
            return 0.0;
        }
        Map<String, String> props = reader.getDataPropertiesForIndividual(node);
        return props.containsKey(property) ? Double.parseDouble(props.get(property)) : 0.0;
    }

}
