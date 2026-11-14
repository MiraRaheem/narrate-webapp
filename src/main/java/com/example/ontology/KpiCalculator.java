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
    public static JsonObject computeMaterialIntensity(
            String manufacturedProductName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();
        JsonArray materialIntensities = new JsonArray();

        result.addProperty("product", manufacturedProductName);
        result.addProperty("manufacturedProduct", manufacturedProductName);

        try {
            // Step 1: Read the manufactured product's data properties.
            Map<String, String> productProps
                    = reader.getDataPropertiesForIndividual(
                            manufacturedProductName);

            double manufacturedQuantity = requirePositiveNumber(
                    productProps,
                    "hasManufacturedQuantity",
                    manufacturedProductName);

            double manufacturedProductWeight = requirePositiveNumber(
                    productProps,
                    "hasManufacturedProductWeight",
                    manufacturedProductName);

            // Step 2: ManufacturedProduct -> ProductType.
            String productType = requireSingleRelatedIndividual(
                    reader,
                    manufacturedProductName,
                    "isInstanceOfProductType");

            // Step 3: ProductType -> engineering BOM.
            String bom = requireSingleRelatedIndividual(
                    reader,
                    productType,
                    "hasBOM");

            // Step 4: Engineering BOM -> manufacturing MBOM.
            String mbom = requireSingleRelatedIndividual(
                    reader,
                    bom,
                    "isSpecializedAs");

            // Step 5: Retrieve ALL materials from the MBOM.
            List<String> materials
                    = reader.getObjectPropertyValues(
                            mbom,
                            "MBOMIncludedMaterial");

            if (materials == null || materials.isEmpty()) {
                throw new IllegalStateException(
                        "No materials found in MBOM: " + mbom);
            }

            // Prevent duplicate rows if the reasoned model
            // returns the same material more than once.
            java.util.Set<String> uniqueMaterials
                    = new java.util.TreeSet<>(materials);

            double totalMaterialInputWeight = 0.0;

            // Step 6: Calculate intensity for each material.
            for (String material : uniqueMaterials) {

                Map<String, String> materialProps
                        = reader.getDataPropertiesForIndividual(material);

                double materialWeightPerUnit = requireNonNegativeNumber(
                        materialProps,
                        "hasMaterialWeight",
                        material);

                double totalMaterialWeight
                        = materialWeightPerUnit * manufacturedQuantity;

                if (!Double.isFinite(totalMaterialWeight)) {
                    throw new IllegalStateException(
                            "Material input exceeds numeric range: "
                            + material);
                }

                double intensity
                        = totalMaterialWeight / manufacturedProductWeight;

                if (!Double.isFinite(intensity)) {
                    throw new IllegalStateException(
                            "Invalid Material Intensity for: " + material);
                }

                JsonObject entry = new JsonObject();
                entry.addProperty("material", material);
                entry.addProperty("weightPerUnit", materialWeightPerUnit);
                entry.addProperty("totalMaterialWeight", totalMaterialWeight);
                entry.addProperty("intensity", intensity);

                materialIntensities.add(entry);
                totalMaterialInputWeight += totalMaterialWeight;
            }

            if (!Double.isFinite(totalMaterialInputWeight)) {
                throw new IllegalStateException(
                        "Total material input exceeds numeric range.");
            }

            // Step 7: Return the calculation and its context.
            result.addProperty("productType", productType);
            result.addProperty("bom", bom);
            result.addProperty("mbom", mbom);
            result.addProperty("manufacturedQuantity", manufacturedQuantity);
            result.addProperty("productWeight", manufacturedProductWeight);
            result.addProperty("totalMaterialInputWeight",
                    totalMaterialInputWeight);
            result.addProperty("calculationBasis",
                    "PLANNED_EQUALS_CONSUMED");
            result.add("materialIntensities", materialIntensities);

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static JsonObject computeProductMaterialEfficiency(
            String manufacturedProductName,
            OntologyReader reader) {

        JsonObject result = computeMaterialIntensity(
                manufacturedProductName, reader);

        // Preserve errors returned by the existing calculation.
        if (result.has("error")) {
            return result;
        }

        try {
            double totalMaterialInputWeight
                    = result.get("totalMaterialInputWeight").getAsDouble();

            double productWeight
                    = result.get("productWeight").getAsDouble();

            if (!Double.isFinite(totalMaterialInputWeight)
                    || !Double.isFinite(productWeight)
                    || productWeight <= 0.0) {
                throw new IllegalStateException(
                        "Invalid weights for Product Material Efficiency.");
            }

            double materialEfficiency
                    = totalMaterialInputWeight / productWeight;

            result.addProperty(
                    "materialEfficiency", materialEfficiency);

            result.addProperty("unit", "kg/kg");

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
        }

        return result;
    }

    public static JsonObject computeRenewableContent(
            String manufacturedProductName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();
        JsonArray renewableContents = new JsonArray();

        result.addProperty("product", manufacturedProductName);
        result.addProperty("manufacturedProduct", manufacturedProductName);

        try {
            // 1. Get manufactured quantity and finished product weight.
            Map<String, String> productProps
                    = reader.getDataPropertiesForIndividual(
                            manufacturedProductName);

            double quantity = requirePositiveNumber(
                    productProps,
                    "hasManufacturedQuantity",
                    manufacturedProductName);

            double productWeight = requirePositiveNumber(
                    productProps,
                    "hasManufacturedProductWeight",
                    manufacturedProductName);

            // 2. ManufacturedProduct -> ProductType -> BOM -> MBOM.
            String productType = requireSingleRelatedIndividual(
                    reader,
                    manufacturedProductName,
                    "isInstanceOfProductType");

            String bom = requireSingleRelatedIndividual(
                    reader,
                    productType,
                    "hasBOM");

            String mbom = requireSingleRelatedIndividual(
                    reader,
                    bom,
                    "isSpecializedAs");

            // 3. Retrieve materials from the MBOM.
            List<String> materials = reader.getObjectPropertyValues(
                    mbom,
                    "MBOMIncludedMaterial");

            if (materials == null || materials.isEmpty()) {
                throw new IllegalStateException(
                        "No materials found in MBOM: " + mbom);
            }

            java.util.Set<String> uniqueMaterials
                    = new java.util.TreeSet<>(materials);

            // 4. Retrieve MaterialSustainability records.
            // Relationship direction:
            // MaterialSustainability -> materialSustainabilityOf -> Material
            List<String> sustainabilityRecords
                    = reader.getIndividualsByClass("MaterialSustainability");

            Map<String, String> sustainabilityByMaterial
                    = new HashMap<>();

            for (String sustainability : sustainabilityRecords) {

                List<String> linkedMaterials
                        = reader.getObjectPropertyValues(
                                sustainability,
                                "materialSustainabilityOf");

                for (String material : linkedMaterials) {
                    if (sustainabilityByMaterial.putIfAbsent(
                            material, sustainability) != null) {
                        throw new IllegalStateException(
                                "Multiple MaterialSustainability records for: "
                                + material);
                    }
                }
            }

            double totalMaterialInputWeight = 0.0;
            double totalRenewableWeight = 0.0;

            // 5. Calculate renewable mass for each material.
            for (String material : uniqueMaterials) {

                Map<String, String> materialProps
                        = reader.getDataPropertiesForIndividual(material);

                double weightPerUnit = requireNonNegativeNumber(
                        materialProps,
                        "hasMaterialWeight",
                        material);

                double totalMaterialWeight = weightPerUnit * quantity;

                String sustainability
                        = sustainabilityByMaterial.get(material);

                if (sustainability == null) {
                    throw new IllegalStateException(
                            "No MaterialSustainability record for: "
                            + material);
                }

                Map<String, String> sustainabilityProps
                        = reader.getDataPropertiesForIndividual(
                                sustainability);

                double renewableFraction = requireNonNegativeNumber(
                        sustainabilityProps,
                        "hasMaterialRenewableContent",
                        sustainability);

                if (renewableFraction > 1.0) {
                    throw new IllegalStateException(
                            "hasMaterialRenewableContent must be between "
                            + "0 and 1 for " + sustainability);
                }

                double renewableWeight
                        = totalMaterialWeight * renewableFraction;

                double renewablePercentage
                        = (renewableWeight / productWeight) * 100.0;

                JsonObject entry = new JsonObject();
                entry.addProperty("material", material);
                entry.addProperty("materialSustainability", sustainability);
                entry.addProperty("weightPerUnit", weightPerUnit);
                entry.addProperty("weight", totalMaterialWeight);
                entry.addProperty("totalMaterialWeight", totalMaterialWeight);
                entry.addProperty("renewableFraction", renewableFraction);
                entry.addProperty("renewableWeight", renewableWeight);
                entry.addProperty("renewablePercentage", renewablePercentage);

                renewableContents.add(entry);

                totalMaterialInputWeight += totalMaterialWeight;
                totalRenewableWeight += renewableWeight;
            }

            
            
            // 6. Overall renewable content of manufactured product.
            double overallRenewablePercentage
                    = (totalRenewableWeight / productWeight) * 100.0;

            result.addProperty("productType", productType);
            result.addProperty("bom", bom);
            result.addProperty("mbom", mbom);
            result.addProperty("manufacturedQuantity", quantity);
            result.addProperty("productWeight", productWeight);
            result.addProperty("totalMaterialInputWeight",
                    totalMaterialInputWeight);
            result.addProperty("totalRenewableWeight",
                    totalRenewableWeight);
            result.addProperty("overallRenewablePercentage",
                    overallRenewablePercentage);
            result.addProperty("calculationBasis",
                    "PLANNED_EQUALS_CONSUMED");
            result.add("renewableContents", renewableContents);

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static JsonObject computeProductRenewableMaterial(
            String manufacturedProductName,
            OntologyReader reader) {

        JsonObject result = computeRenewableContent(
                manufacturedProductName, reader);

        if (result.has("error")) {
            return result;
        }

        try {
            double totalMaterialInputWeight = 0.0;
            double totalRenewableWeight = 0.0;

            JsonArray materials
                    = result.getAsJsonArray("renewableContents");

            if (materials == null || materials.size() == 0) {
                throw new IllegalStateException(
                        "No renewable material data found.");
            }

            for (int i = 0; i < materials.size(); i++) {
                JsonObject material = materials.get(i).getAsJsonObject();

                totalMaterialInputWeight
                        += material.get("totalMaterialWeight").getAsDouble();

                totalRenewableWeight
                        += material.get("renewableWeight").getAsDouble();
            }

            if (!Double.isFinite(totalMaterialInputWeight)
                    || !Double.isFinite(totalRenewableWeight)
                    || totalMaterialInputWeight <= 0.0) {
                throw new IllegalStateException(
                        "Invalid material weights for Product Renewable Material.");
            }

            double renewablePercentage
                    = (totalRenewableWeight / totalMaterialInputWeight) * 100.0;

            result.addProperty(
                    "totalMaterialInputWeight", totalMaterialInputWeight);

            result.addProperty(
                    "productRenewablePercentage", renewablePercentage);

            result.addProperty("unit", "%");
            result.addProperty(
                    "calculationBasis", "TOTAL_MATERIAL_INPUT");

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
        }

        return result;
    }

    public static JsonObject computeRecycledContent(
            String manufacturedProductName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();
        JsonArray recycledContents = new JsonArray();

        result.addProperty("product", manufacturedProductName);
        result.addProperty("manufacturedProduct", manufacturedProductName);

        try {
            // 1. Read manufactured quantity and finished product weight.
            Map<String, String> productProps
                    = reader.getDataPropertiesForIndividual(
                            manufacturedProductName);

            double quantity = requirePositiveNumber(
                    productProps,
                    "hasManufacturedQuantity",
                    manufacturedProductName);

            double productWeight = requirePositiveNumber(
                    productProps,
                    "hasManufacturedProductWeight",
                    manufacturedProductName);

            // 2. ManufacturedProduct -> ProductType -> BOM -> MBOM.
            String productType = requireSingleRelatedIndividual(
                    reader,
                    manufacturedProductName,
                    "isInstanceOfProductType");

            String bom = requireSingleRelatedIndividual(
                    reader,
                    productType,
                    "hasBOM");

            String mbom = requireSingleRelatedIndividual(
                    reader,
                    bom,
                    "isSpecializedAs");

            // 3. Retrieve all materials included in the MBOM.
            List<String> materials = reader.getObjectPropertyValues(
                    mbom,
                    "MBOMIncludedMaterial");

            if (materials == null || materials.isEmpty()) {
                throw new IllegalStateException(
                        "No materials found in MBOM: " + mbom);
            }

            java.util.Set<String> uniqueMaterials
                    = new java.util.TreeSet<>(materials);

            // 4. Find each material's sustainability record.
            // MaterialSustainability -> materialSustainabilityOf -> Material.
            List<String> sustainabilityRecords
                    = reader.getIndividualsByClass("MaterialSustainability");

            Map<String, String> sustainabilityByMaterial
                    = new HashMap<>();

            if (sustainabilityRecords == null) {
                throw new IllegalStateException(
                        "Unable to retrieve MaterialSustainability records.");
            }

            for (String sustainability : sustainabilityRecords) {

                List<String> linkedMaterials
                        = reader.getObjectPropertyValues(
                                sustainability,
                                "materialSustainabilityOf");

                if (linkedMaterials == null) {
                    continue;
                }

                for (String material : linkedMaterials) {
                    if (sustainabilityByMaterial.putIfAbsent(
                            material, sustainability) != null) {
                        throw new IllegalStateException(
                                "Multiple MaterialSustainability records for: "
                                + material);
                    }
                }
            }

            double totalMaterialInputWeight = 0.0;
            double totalRecycledWeight = 0.0;

            // 5. Calculate recycled mass for each material.
            for (String material : uniqueMaterials) {

                Map<String, String> materialProps
                        = reader.getDataPropertiesForIndividual(material);

                double weightPerUnit = requireNonNegativeNumber(
                        materialProps,
                        "hasMaterialWeight",
                        material);

                double totalMaterialWeight = weightPerUnit * quantity;

                if (!Double.isFinite(totalMaterialWeight)) {
                    throw new IllegalStateException(
                            "Invalid total material weight for: " + material);
                }

                String sustainability
                        = sustainabilityByMaterial.get(material);

                if (sustainability == null) {
                    throw new IllegalStateException(
                            "No MaterialSustainability record for: "
                            + material);
                }

                Map<String, String> sustainabilityProps
                        = reader.getDataPropertiesForIndividual(
                                sustainability);

                double recycledFraction = requireNonNegativeNumber(
                        sustainabilityProps,
                        "hasMaterialRecycledContent",
                        sustainability);

                if (recycledFraction > 1.0) {
                    throw new IllegalStateException(
                            "hasMaterialRecycledContent must be between "
                            + "0 and 1 for " + sustainability);
                }

                double recycledWeight
                        = totalMaterialWeight * recycledFraction;

                double recycledPercentage
                        = (recycledWeight / productWeight) * 100.0;

                if (!Double.isFinite(recycledWeight)
                        || !Double.isFinite(recycledPercentage)) {
                    throw new IllegalStateException(
                            "Invalid recycled content for: " + material);
                }

                JsonObject entry = new JsonObject();
                entry.addProperty("material", material);
                entry.addProperty("materialSustainability", sustainability);
                entry.addProperty("weightPerUnit", weightPerUnit);
                entry.addProperty("weight", totalMaterialWeight);
                entry.addProperty("totalMaterialWeight", totalMaterialWeight);
                entry.addProperty("recycledFraction", recycledFraction);
                entry.addProperty("recycledWeight", recycledWeight);
                entry.addProperty("recycledPercentage", recycledPercentage);

                recycledContents.add(entry);

                totalMaterialInputWeight += totalMaterialWeight;
                totalRecycledWeight += recycledWeight;
            }

            if (!Double.isFinite(totalMaterialInputWeight)
                    || !Double.isFinite(totalRecycledWeight)) {
                throw new IllegalStateException(
                        "Total material weight exceeds numeric range.");
            }

            // 6. Calculate overall recycled content.
            double overallRecycledPercentage
                    = (totalRecycledWeight / productWeight) * 100.0;

            if (!Double.isFinite(overallRecycledPercentage)) {
                throw new IllegalStateException(
                        "Invalid overall recycled content.");
            }

            // 7. Return the result and calculation context.
            result.addProperty("productType", productType);
            result.addProperty("bom", bom);
            result.addProperty("mbom", mbom);
            result.addProperty("manufacturedQuantity", quantity);
            result.addProperty("productWeight", productWeight);
            result.addProperty("totalMaterialInputWeight",
                    totalMaterialInputWeight);
            result.addProperty("totalRecycledWeight",
                    totalRecycledWeight);
            result.addProperty("overallRecycledPercentage",
                    overallRecycledPercentage);
            result.addProperty("calculationBasis",
                    "PLANNED_EQUALS_CONSUMED");
            result.add("recycledContents", recycledContents);

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static JsonObject computeProductRecycledMaterial(
            String manufacturedProductName,
            OntologyReader reader) {

        JsonObject result = computeRecycledContent(
                manufacturedProductName, reader);

        if (result.has("error")) {
            return result;
        }

        try {
            double totalMaterialInputWeight = 0.0;
            double totalRecycledWeight = 0.0;

            JsonArray materials
                    = result.getAsJsonArray("recycledContents");

            if (materials == null || materials.size() == 0) {
                throw new IllegalStateException(
                        "No recycled material data found.");
            }

            for (int i = 0; i < materials.size(); i++) {
                JsonObject material = materials.get(i).getAsJsonObject();

                totalMaterialInputWeight
                        += material.get("totalMaterialWeight").getAsDouble();

                totalRecycledWeight
                        += material.get("recycledWeight").getAsDouble();
            }

            if (!Double.isFinite(totalMaterialInputWeight)
                    || !Double.isFinite(totalRecycledWeight)
                    || totalMaterialInputWeight <= 0.0
                    || totalRecycledWeight < 0.0
                    || totalRecycledWeight > totalMaterialInputWeight) {
                throw new IllegalStateException(
                        "Invalid material weights for Product Recycled Material.");
            }

            double recycledPercentage
                    = (totalRecycledWeight / totalMaterialInputWeight) * 100.0;

            result.addProperty(
                    "totalMaterialInputWeight", totalMaterialInputWeight);

            result.addProperty(
                    "productRecycledPercentage", recycledPercentage);

            result.addProperty("unit", "%");

            result.addProperty(
                    "calculationBasis", "TOTAL_MATERIAL_INPUT");

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
        }

        return result;
    }

    public static JsonObject computeSupplyTransportCarbonFootprint(
            String manufacturedProductName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();
        JsonArray materialFootprints = new JsonArray();

        result.addProperty("product", manufacturedProductName);
        result.addProperty("manufacturedProduct", manufacturedProductName);

        try {
            // 1. Identify the product and its MBOM.
            Map<String, String> productProps
                    = reader.getDataPropertiesForIndividual(
                            manufacturedProductName);

            double quantity = requirePositiveNumber(
                    productProps,
                    "hasManufacturedQuantity",
                    manufacturedProductName);

            String productType = requireSingleRelatedIndividual(
                    reader,
                    manufacturedProductName,
                    "isInstanceOfProductType");

            String bom = requireSingleRelatedIndividual(
                    reader,
                    productType,
                    "hasBOM");

            String mbom = requireSingleRelatedIndividual(
                    reader,
                    bom,
                    "isSpecializedAs");

            List<String> materials = reader.getObjectPropertyValues(
                    mbom,
                    "MBOMIncludedMaterial");

            if (materials == null || materials.isEmpty()) {
                throw new IllegalStateException(
                        "No materials found in MBOM: " + mbom);
            }

            java.util.Set<String> uniqueMaterials
                    = new java.util.TreeSet<>(materials);

            // 2. Index shipments by the materials they transport.
            Map<String, java.util.List<String>> shipmentsByMaterial
                    = new HashMap<>();

            List<String> allShipments
                    = reader.getIndividualsByClass("Shipment");

            if (allShipments == null) {
                throw new IllegalStateException(
                        "Unable to retrieve Shipment individuals.");
            }

            for (String shipment : new java.util.TreeSet<>(allShipments)) {

                List<String> linkedMaterials
                        = reader.getObjectPropertyValues(
                                shipment,
                                "transportsMaterial");

                if (linkedMaterials == null) {
                    continue;
                }

                for (String material
                        : new java.util.LinkedHashSet<>(linkedMaterials)) {

                    shipmentsByMaterial
                            .computeIfAbsent(
                                    material,
                                    k -> new java.util.ArrayList<>())
                            .add(shipment);
                }
            }

            // 3. Index sustainability records by shipment.
            Map<String, String> sustainabilityByShipment
                    = new HashMap<>();

            List<String> sustainabilityRecords
                    = reader.getIndividualsByClass(
                            "ShipmentSustainability");

            if (sustainabilityRecords == null) {
                throw new IllegalStateException(
                        "Unable to retrieve ShipmentSustainability individuals.");
            }

            for (String sustainability
                    : new java.util.TreeSet<>(sustainabilityRecords)) {

                List<String> linkedShipments
                        = reader.getObjectPropertyValues(
                                sustainability,
                                "sustainabilityOfShipment");

                if (linkedShipments == null) {
                    continue;
                }

                for (String shipment
                        : new java.util.LinkedHashSet<>(linkedShipments)) {

                    if (sustainabilityByShipment.putIfAbsent(
                            shipment, sustainability) != null) {

                        throw new IllegalStateException(
                                "Multiple ShipmentSustainability records for: "
                                + shipment);
                    }
                }
            }

            double totalRoadCarbonKg = 0.0;
            double totalSeaCarbonKg = 0.0;
            double totalCarbonKg = 0.0;

            // 4. Calculate the footprint of each MBOM material.
            for (String material : uniqueMaterials) {

                java.util.List<String> materialShipments
                        = shipmentsByMaterial.get(material);

                if (materialShipments == null
                        || materialShipments.isEmpty()) {

                    throw new IllegalStateException(
                            "No shipments found for material: " + material);
                }

                JsonArray shipmentDetails = new JsonArray();

                double materialRoadCarbonKg = 0.0;
                double materialSeaCarbonKg = 0.0;
                double materialCarbonKg = 0.0;

                java.util.Set<String> modesFound
                        = new java.util.HashSet<>();

                for (String shipment : materialShipments) {

                    Map<String, String> shipmentProps
                            = reader.getDataPropertiesForIndividual(
                                    shipment);

                    double shipmentWeightKg = requirePositiveNumber(
                            shipmentProps,
                            "hasShipmentWeightKG",
                            shipment);

                    double distanceKm = requireNonNegativeNumber(
                            shipmentProps,
                            "hasTransportDistanceKM",
                            shipment);

                    String mode = shipmentProps.get("hasTransportMode");

                    if (mode == null || mode.trim().isEmpty()) {
                        throw new IllegalStateException(
                                "Missing hasTransportMode for: "
                                + shipment);
                    }

                    mode = mode.trim();

                    if (!"Road".equals(mode) && !"Sea".equals(mode)) {
                        throw new IllegalStateException(
                                "Unsupported transport mode for "
                                + shipment + ": " + mode
                                + ". This KPI currently supports Road and Sea.");
                    }

                    if (!modesFound.add(mode)) {
                        throw new IllegalStateException(
                                "Multiple " + mode
                                + " shipments found for material "
                                + material
                                + ". Review whether these are separate "
                                + "deliveries or transport legs.");
                    }

                    String sustainability
                            = sustainabilityByShipment.get(shipment);

                    if (sustainability == null) {
                        throw new IllegalStateException(
                                "No ShipmentSustainability record for: "
                                + shipment);
                    }

                    Map<String, String> sustainabilityProps
                            = reader.getDataPropertiesForIndividual(
                                    sustainability);

                    double emissionFactor = requireNonNegativeNumber(
                            sustainabilityProps,
                            "hasTransportEmissionFactor",
                            sustainability);

                    // kg -> tonnes, then tonne-km -> kg CO2eq.
                    double tonneKilometres
                            = (shipmentWeightKg / 1000.0) * distanceKm;

                    double carbonKg
                            = tonneKilometres * emissionFactor;

                    if (!Double.isFinite(tonneKilometres)
                            || !Double.isFinite(carbonKg)) {
                        throw new IllegalStateException(
                                "Invalid transport footprint for: "
                                + shipment);
                    }

                    JsonObject shipmentEntry = new JsonObject();
                    shipmentEntry.addProperty("shipment", shipment);
                    shipmentEntry.addProperty(
                            "shipmentSustainability", sustainability);
                    shipmentEntry.addProperty("transportMode", mode);
                    shipmentEntry.addProperty(
                            "shipmentWeightKg", shipmentWeightKg);
                    shipmentEntry.addProperty("distanceKm", distanceKm);
                    shipmentEntry.addProperty(
                            "tonneKilometres", tonneKilometres);
                    shipmentEntry.addProperty(
                            "emissionFactor", emissionFactor);
                    shipmentEntry.addProperty("carbonKg", carbonKg);

                    shipmentDetails.add(shipmentEntry);

                    materialCarbonKg += carbonKg;

                    if ("Road".equals(mode)) {
                        materialRoadCarbonKg += carbonKg;
                    } else {
                        materialSeaCarbonKg += carbonKg;
                    }
                }

                // Our initial test scenario contains both transport legs.
                if (!modesFound.contains("Road")
                        || !modesFound.contains("Sea")) {
                    throw new IllegalStateException(
                            "Expected Road and Sea shipments for material: "
                            + material);
                }

                if (!Double.isFinite(materialCarbonKg)) {
                    throw new IllegalStateException(
                            "Invalid total carbon footprint for: "
                            + material);
                }

                JsonObject materialEntry = new JsonObject();
                materialEntry.addProperty("material", material);
                materialEntry.addProperty(
                        "roadCarbonKg", materialRoadCarbonKg);
                materialEntry.addProperty(
                        "seaCarbonKg", materialSeaCarbonKg);
                materialEntry.addProperty(
                        "totalCarbonKg", materialCarbonKg);
                materialEntry.add("shipments", shipmentDetails);

                materialFootprints.add(materialEntry);

                totalRoadCarbonKg += materialRoadCarbonKg;
                totalSeaCarbonKg += materialSeaCarbonKg;
                totalCarbonKg += materialCarbonKg;
            }

            if (!Double.isFinite(totalRoadCarbonKg)
                    || !Double.isFinite(totalSeaCarbonKg)
                    || !Double.isFinite(totalCarbonKg)) {
                throw new IllegalStateException(
                        "Total transport footprint exceeds numeric range.");
            }

            // 5. Return the product-level and material-level results.
            result.addProperty("productType", productType);
            result.addProperty("bom", bom);
            result.addProperty("mbom", mbom);
            result.addProperty("manufacturedQuantity", quantity);
            result.addProperty("totalRoadCarbonKg", totalRoadCarbonKg);
            result.addProperty("totalSeaCarbonKg", totalSeaCarbonKg);
            result.addProperty("totalTransportCarbonKg", totalCarbonKg);
            result.addProperty("unit", "kg CO2eq");
            result.addProperty(
                    "calculationBasis",
                    "SHIPMENT_WEIGHT_ROAD_AND_SEA");
            result.add("materialFootprints", materialFootprints);

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static JsonObject computeCorporateEnergyCarbonFootprint(
            String processSustainabilityName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();
        JsonArray energySources = new JsonArray();

        result.addProperty(
                "processSustainability", processSustainabilityName);

        try {
            // 1. Read the total production output.
            Map<String, String> processProps
                    = reader.getDataPropertiesForIndividual(
                            processSustainabilityName);

            double productionOutputKg = requirePositiveNumber(
                    processProps,
                    "hasProductionOutputKG",
                    processSustainabilityName);

            // 2. Follow the process-to-energy-record relationship.
            List<String> records = reader.getObjectPropertyValues(
                    processSustainabilityName,
                    "hasEnergySourceRecord");

            if (records == null || records.isEmpty()) {
                throw new IllegalStateException(
                        "No EnergySourceRecord individuals linked to "
                        + processSustainabilityName);
            }

            java.util.Set<String> uniqueRecords
                    = new java.util.TreeSet<>(records);

            java.util.Set<String> sourceTypes
                    = new java.util.HashSet<>();

            double totalCarbonKg = 0.0;
            double totalElectricityKWh = 0.0;
            double renewableElectricityKWh = 0.0;

            // 3. Calculate emissions for each energy source.
            for (String record : uniqueRecords) {

                Map<String, String> props
                        = reader.getDataPropertiesForIndividual(record);

                String sourceType = props.get("hasEnergySourceType");
                String unit = props.get("hasEnergyConsumptionUnit");

                if (sourceType == null
                        || sourceType.trim().isEmpty()) {
                    throw new IllegalStateException(
                            "Missing hasEnergySourceType for " + record);
                }

                if (unit == null || unit.trim().isEmpty()) {
                    throw new IllegalStateException(
                            "Missing hasEnergyConsumptionUnit for " + record);
                }

                sourceType = sourceType.trim();
                unit = unit.trim();

                if (!sourceTypes.add(sourceType)) {
                    throw new IllegalStateException(
                            "Duplicate energy source type: " + sourceType);
                }

                // Validate the source-specific unit.
                switch (sourceType) {
                    case "Electricity":
                        if (!"kWh".equals(unit)) {
                            throw new IllegalStateException(
                                    "Electricity must use kWh: " + record);
                        }
                        break;

                    case "Gasoil":
                        if (!"L".equals(unit)) {
                            throw new IllegalStateException(
                                    "Gasoil must use L: " + record);
                        }
                        break;

                    case "NaturalGas":
                        if (!"m3".equals(unit)
                                && !"m³".equals(unit)) {
                            throw new IllegalStateException(
                                    "NaturalGas must use m3: " + record);
                        }
                        break;

                    case "Other":
                        // Other sources may use source-specific units.
                        break;

                    default:
                        throw new IllegalStateException(
                                "Unsupported energy source type: "
                                + sourceType);
                }

                double consumption = requireNonNegativeNumber(
                        props,
                        "hasEnergyConsumption",
                        record);

                double emissionFactor = requireNonNegativeNumber(
                        props,
                        "hasEnergyEmissionFactor",
                        record);

                double renewableFraction = requireNonNegativeNumber(
                        props,
                        "hasEnergyRenewableFraction",
                        record);

                if (renewableFraction > 1.0) {
                    throw new IllegalStateException(
                            "hasEnergyRenewableFraction must be "
                            + "between 0 and 1 for " + record);
                }

                // 4. Calculate absolute and normalized emissions.
                double carbonKg = consumption * emissionFactor;
                double carbonPerKg = carbonKg / productionOutputKg;

                if (!Double.isFinite(carbonKg)
                        || !Double.isFinite(carbonPerKg)) {
                    throw new IllegalStateException(
                            "Invalid energy emissions for " + record);
                }

                // Track renewable electricity separately.
                if ("Electricity".equals(sourceType)) {
                    totalElectricityKWh += consumption;
                    renewableElectricityKWh
                            += consumption * renewableFraction;
                }

                JsonObject entry = new JsonObject();

                entry.addProperty("energyRecord", record);
                entry.addProperty("sourceType", sourceType);
                entry.addProperty("consumption", consumption);
                entry.addProperty("consumptionUnit", unit);
                entry.addProperty("emissionFactor", emissionFactor);
                entry.addProperty(
                        "renewableFraction", renewableFraction);
                entry.addProperty("carbonKg", carbonKg);
                entry.addProperty("carbonPerKg", carbonPerKg);

                energySources.add(entry);
                totalCarbonKg += carbonKg;
            }

            if (!Double.isFinite(totalCarbonKg)) {
                throw new IllegalStateException(
                        "Total energy emissions exceed numeric range.");
            }

            // 5. Calculate the corporate energy indicators.
            double totalCarbonPerKg
                    = totalCarbonKg / productionOutputKg;

            double renewableElectricityPercentage
                    = totalElectricityKWh > 0.0
                            ? (renewableElectricityKWh
                            / totalElectricityKWh) * 100.0
                            : 0.0;

            result.addProperty(
                    "productionOutputKg", productionOutputKg);

            result.addProperty(
                    "totalEnergyCarbonKg", totalCarbonKg);

            result.addProperty(
                    "totalEnergyCarbonPerKg", totalCarbonPerKg);

            result.addProperty(
                    "totalElectricityKWh", totalElectricityKWh);

            result.addProperty(
                    "renewableElectricityKWh", renewableElectricityKWh);

            result.addProperty(
                    "renewableElectricityPercentage",
                    renewableElectricityPercentage);

            result.addProperty("unit", "kg CO2eq/kg");

            result.addProperty(
                    "calculationBasis",
                    "PRODUCTION_OUTPUT_MASS");

            result.add("energySources", energySources);

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static JsonObject computeProductEnergyCarbonFootprint(
            String manufacturedProductName,
            String processSustainabilityName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();
        JsonArray productEnergySources = new JsonArray();

        result.addProperty("product", manufacturedProductName);
        result.addProperty("manufacturedProduct", manufacturedProductName);
        result.addProperty(
                "processSustainability", processSustainabilityName);

        try {
            // 1. Read the manufactured product's actual weight.
            Map<String, String> productProps
                    = reader.getDataPropertiesForIndividual(
                            manufacturedProductName);

            double productWeightKg = requirePositiveNumber(
                    productProps,
                    "hasManufacturedProductWeight",
                    manufacturedProductName);

            // 2. Reuse the verified Corporate Energy calculation.
            JsonObject corporate
                    = computeCorporateEnergyCarbonFootprint(
                            processSustainabilityName, reader);

            if (corporate.has("error")) {
                throw new IllegalStateException(
                        "Corporate Energy calculation failed: "
                        + corporate.get("error").getAsString());
            }

            double productionOutputKg
                    = corporate.get("productionOutputKg").getAsDouble();

            double corporateTotalCarbonKg
                    = corporate.get("totalEnergyCarbonKg").getAsDouble();

            if (!Double.isFinite(productionOutputKg)
                    || productionOutputKg <= 0.0
                    || !Double.isFinite(corporateTotalCarbonKg)
                    || corporateTotalCarbonKg < 0.0) {
                throw new IllegalStateException(
                        "Invalid Corporate Energy calculation values.");
            }

            // 3. Calculate the mass-based allocation fraction.
            if (productWeightKg > productionOutputKg) {
                throw new IllegalStateException(
                        "Product weight exceeds production output: "
                        + manufacturedProductName);
            }

            double allocationFraction
                    = productWeightKg / productionOutputKg;

            // 4. Allocate each energy source to the product.
            JsonArray corporateSources
                    = corporate.getAsJsonArray("energySources");

            if (corporateSources == null
                    || corporateSources.size() == 0) {
                throw new IllegalStateException(
                        "No Corporate Energy sources available.");
            }

            double totalAllocatedCarbonKg = 0.0;
            double totalProductCarbonPerKg = 0.0;

            for (int i = 0; i < corporateSources.size(); i++) {

                JsonObject source
                        = corporateSources.get(i).getAsJsonObject();

                String sourceType
                        = source.get("sourceType").getAsString();

                double corporateCarbonKg
                        = source.get("carbonKg").getAsDouble();

                double allocatedCarbonKg
                        = corporateCarbonKg * allocationFraction;

                double carbonPerKg
                        = allocatedCarbonKg / productWeightKg;

                if (!Double.isFinite(allocatedCarbonKg)
                        || !Double.isFinite(carbonPerKg)) {
                    throw new IllegalStateException(
                            "Invalid allocated energy emissions for "
                            + sourceType);
                }

                JsonObject entry = new JsonObject();

                entry.addProperty(
                        "energyRecord",
                        source.get("energyRecord").getAsString());

                entry.addProperty("sourceType", sourceType);
                entry.addProperty(
                        "corporateCarbonKg", corporateCarbonKg);

                entry.addProperty(
                        "allocatedCarbonKg", allocatedCarbonKg);

                entry.addProperty(
                        "carbonPerKg", carbonPerKg);

                productEnergySources.add(entry);

                totalAllocatedCarbonKg += allocatedCarbonKg;
                totalProductCarbonPerKg += carbonPerKg;
            }

            // 5. Verify allocation totals.
            double expectedAllocatedCarbonKg
                    = corporateTotalCarbonKg * allocationFraction;

            if (Math.abs(totalAllocatedCarbonKg
                    - expectedAllocatedCarbonKg) > 1e-8) {
                throw new IllegalStateException(
                        "Allocated energy totals do not reconcile.");
            }

            if (!Double.isFinite(totalAllocatedCarbonKg)
                    || !Double.isFinite(totalProductCarbonPerKg)) {
                throw new IllegalStateException(
                        "Invalid total Product Energy Carbon Footprint.");
            }

            // 6. Return product-level results.
            result.addProperty(
                    "productWeightKg", productWeightKg);

            result.addProperty(
                    "productionOutputKg", productionOutputKg);

            result.addProperty(
                    "allocationFraction", allocationFraction);

            result.addProperty(
                    "corporateTotalCarbonKg", corporateTotalCarbonKg);

            result.addProperty(
                    "totalAllocatedCarbonKg", totalAllocatedCarbonKg);

            result.addProperty(
                    "totalProductCarbonPerKg", totalProductCarbonPerKg);

            result.addProperty(
                    "absoluteUnit", "kg CO2eq");

            result.addProperty(
                    "intensityUnit", "kg CO2eq/kg product");

            result.addProperty(
                    "calculationBasis", "MASS_ALLOCATED_PROTOTYPE");

            result.add("energySources", productEnergySources);

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static JsonObject computeProductEmbodiedCarbon(
            String manufacturedProductName,
            String processSustainabilityName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();

        result.addProperty("product", manufacturedProductName);
        result.addProperty("manufacturedProduct", manufacturedProductName);
        result.addProperty(
                "processSustainability", processSustainabilityName);

        try {
            // 1. Reuse the verified Product Energy calculation.
            JsonObject energy
                    = computeProductEnergyCarbonFootprint(
                            manufacturedProductName,
                            processSustainabilityName,
                            reader);

            if (energy.has("error")) {
                throw new IllegalStateException(
                        "Product Energy calculation failed: "
                        + energy.get("error").getAsString());
            }

            // 2. Retrieve product weight and allocated emissions.
            double productWeightKg
                    = energy.get("productWeightKg").getAsDouble();

            double totalAllocatedCarbonKg
                    = energy.get("totalAllocatedCarbonKg").getAsDouble();

            double energyCarbonPerKg
                    = energy.get("totalProductCarbonPerKg").getAsDouble();

            if (!Double.isFinite(productWeightKg)
                    || productWeightKg <= 0.0
                    || !Double.isFinite(totalAllocatedCarbonKg)
                    || totalAllocatedCarbonKg < 0.0
                    || !Double.isFinite(energyCarbonPerKg)
                    || energyCarbonPerKg < 0.0) {

                throw new IllegalStateException(
                        "Invalid Product Energy values for "
                        + "Embodied Carbon calculation.");
            }

            // 3. Verify the source-level carbon contributions.
            JsonArray sources
                    = energy.getAsJsonArray("energySources");

            if (sources == null || sources.size() == 0) {
                throw new IllegalStateException(
                        "No Product Energy source breakdown found.");
            }

            double sourceTotalKg = 0.0;
            double sourceTotalPerKg = 0.0;

            for (int i = 0; i < sources.size(); i++) {

                JsonObject source
                        = sources.get(i).getAsJsonObject();

                double allocatedKg
                        = source.get("allocatedCarbonKg").getAsDouble();

                double intensity
                        = source.get("carbonPerKg").getAsDouble();

                if (!Double.isFinite(allocatedKg)
                        || allocatedKg < 0.0
                        || !Double.isFinite(intensity)
                        || intensity < 0.0) {

                    throw new IllegalStateException(
                            "Invalid embodied carbon contribution.");
                }

                sourceTotalKg += allocatedKg;
                sourceTotalPerKg += intensity;
            }

            // 4. Reconcile the breakdown with the product totals.
            if (Math.abs(sourceTotalKg - totalAllocatedCarbonKg)
                    > 1e-8
                    || Math.abs(sourceTotalPerKg - energyCarbonPerKg)
                    > 1e-8) {

                throw new IllegalStateException(
                        "Embodied Carbon source totals do not reconcile.");
            }

            // 5. Return the energy-related embodied carbon.
            result.addProperty(
                    "productWeightKg", productWeightKg);

            result.addProperty(
                    "productionOutputKg",
                    energy.get("productionOutputKg").getAsDouble());

            result.addProperty(
                    "allocationFraction",
                    energy.get("allocationFraction").getAsDouble());

            result.addProperty(
                    "embodiedCarbonKg", totalAllocatedCarbonKg);

            result.addProperty(
                    "embodiedCarbonPerKg", energyCarbonPerKg);

            result.addProperty(
                    "absoluteUnit", "kg CO2eq");

            result.addProperty(
                    "intensityUnit", "kg CO2eq/kg product");

            result.addProperty(
                    "calculationBoundary",
                    "PRODUCTION_ENERGY_ONLY");

            result.addProperty(
                    "calculationBasis",
                    "MASS_ALLOCATED_PROTOTYPE");

            result.add(
                    "energySources",
                    sources.deepCopy());

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Computes corporate water KPIs for a ProcessSustainability individual.
     *
     * Input properties: - hasProductionOutputKG: kg - hasWaterUsage: m3 -
     * hasWaterWaste: m3
     *
     * Derived properties (optional consistency checks): - hasWaterUsePerKg:
     * m3/kg - hasWastewaterPerKg: m3/kg
     *
     * The three input measurements must refer to the same production period or
     * batch. No product-level allocation is performed here.
     *
     * @param processSustainabilityName ProcessSustainability individual
     * @param reader Ontology reader
     * @return Corporate water KPI values and calculation metadata
     */
    public static JsonObject computeCorporateWaterKpis(
            String processSustainabilityName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();

        result.addProperty(
                "processSustainability", processSustainabilityName);

        try {
            // 1. Retrieve the process data.
            Map<String, String> props
                    = reader.getDataPropertiesForIndividual(
                            processSustainabilityName);

            if (props == null || props.isEmpty()) {
                throw new IllegalStateException(
                        "No data properties found for "
                        + processSustainabilityName);
            }

            // 2. Read and validate the raw measurements.
            double productionOutputKg = requirePositiveNumber(
                    props,
                    "hasProductionOutputKG",
                    processSustainabilityName);

            double totalWaterUsageM3 = requireNonNegativeNumber(
                    props,
                    "hasWaterUsage",
                    processSustainabilityName);

            double totalWastewaterM3 = requireNonNegativeNumber(
                    props,
                    "hasWaterWaste",
                    processSustainabilityName);

            // 3. Calculate normalized water KPIs.
            double waterUsePerKg
                    = totalWaterUsageM3 / productionOutputKg;

            double wastewaterPerKg
                    = totalWastewaterM3 / productionOutputKg;

            if (!Double.isFinite(waterUsePerKg)
                    || !Double.isFinite(wastewaterPerKg)) {
                throw new IllegalStateException(
                        "Invalid Corporate Water KPI calculation for "
                        + processSustainabilityName);
            }

            // 4. Check stored derived values when available.
            // The calculated values remain the source of truth.
            boolean storedIntensitiesVerified = true;

            if (props.containsKey("hasWaterUsePerKg")) {
                double storedWaterIntensity
                        = requireNonNegativeNumber(
                                props,
                                "hasWaterUsePerKg",
                                processSustainabilityName);

                if (Math.abs(storedWaterIntensity - waterUsePerKg)
                        > 1e-6 * Math.max(1.0, Math.abs(waterUsePerKg))) {
                    storedIntensitiesVerified = false;
                }
            }

            if (props.containsKey("hasWastewaterPerKg")) {
                double storedWastewaterIntensity
                        = requireNonNegativeNumber(
                                props,
                                "hasWastewaterPerKg",
                                processSustainabilityName);

                if (Math.abs(storedWastewaterIntensity - wastewaterPerKg)
                        > 1e-6 * Math.max(1.0, Math.abs(wastewaterPerKg))) {
                    storedIntensitiesVerified = false;
                }
            }

            // 5. Return total water volumes.
            result.addProperty(
                    "productionOutputKg", productionOutputKg);

            result.addProperty(
                    "totalWaterUsageM3", totalWaterUsageM3);

            result.addProperty(
                    "totalWastewaterM3", totalWastewaterM3);

            // 6. Return normalized water intensities.
            result.addProperty(
                    "waterUsePerKg", waterUsePerKg);

            result.addProperty(
                    "wastewaterPerKg", wastewaterPerKg);

            // Convenience conversion: 1 m3 = 1000 L.
            result.addProperty(
                    "waterUseLitersPerKg", waterUsePerKg * 1000.0);

            result.addProperty(
                    "wastewaterLitersPerKg", wastewaterPerKg * 1000.0);

            // 7. Return metadata.
            result.addProperty("absoluteUnit", "m3");
            result.addProperty("intensityUnit", "m3/kg product");

            result.addProperty(
                    "calculationBasis", "PRODUCTION_OUTPUT_MASS");

            result.addProperty(
                    "calculationBoundary", "PROCESS_WATER_AND_WASTEWATER");

            result.addProperty(
                    "storedIntensitiesVerified",
                    storedIntensitiesVerified);

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Computes product water KPIs using mass allocation from a
     * ProcessSustainability water prototype.
     *
     * Product weight: ManufacturedProduct.hasManufacturedProductWeight (kg)
     *
     * Corporate inputs: ProcessSustainability.hasWaterUsage (m3)
     * ProcessSustainability.hasWaterWaste (m3)
     * ProcessSustainability.hasProductionOutputKG (kg)
     *
     * This is a prototype allocation, not a verified product-to-process
     * relationship.
     */
    public static JsonObject computeProductWaterKpis(
            String manufacturedProductName,
            String processSustainabilityName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();

        result.addProperty("product", manufacturedProductName);
        result.addProperty("manufacturedProduct", manufacturedProductName);
        result.addProperty(
                "processSustainability", processSustainabilityName);

        try {
            // 1. Read the actual manufactured product weight.
            Map<String, String> productProps
                    = reader.getDataPropertiesForIndividual(
                            manufacturedProductName);

            if (productProps == null || productProps.isEmpty()) {
                throw new IllegalStateException(
                        "No data properties found for product: "
                        + manufacturedProductName);
            }

            double productWeightKg = requirePositiveNumber(
                    productProps,
                    "hasManufacturedProductWeight",
                    manufacturedProductName);

            // 2. Reuse the verified Corporate Water calculation.
            JsonObject corporate = computeCorporateWaterKpis(
                    processSustainabilityName,
                    reader);

            if (corporate.has("error")) {
                throw new IllegalStateException(
                        "Corporate Water calculation failed: "
                        + corporate.get("error").getAsString());
            }

            double productionOutputKg
                    = corporate.get("productionOutputKg").getAsDouble();

            double corporateWaterUsageM3
                    = corporate.get("totalWaterUsageM3").getAsDouble();

            double corporateWastewaterM3
                    = corporate.get("totalWastewaterM3").getAsDouble();

            // 3. Validate the production allocation boundary.
            if (!Double.isFinite(productionOutputKg)
                    || productionOutputKg <= 0.0
                    || !Double.isFinite(corporateWaterUsageM3)
                    || corporateWaterUsageM3 < 0.0
                    || !Double.isFinite(corporateWastewaterM3)
                    || corporateWastewaterM3 < 0.0) {

                throw new IllegalStateException(
                        "Invalid Corporate Water values for allocation.");
            }

            if (productWeightKg > productionOutputKg) {
                throw new IllegalStateException(
                        "Product weight exceeds production output: "
                        + manufacturedProductName);
            }

            // 4. Calculate the mass allocation fraction.
            double allocationFraction
                    = productWeightKg / productionOutputKg;

            // 5. Allocate process water and wastewater to the product.
            double allocatedWaterUsageM3
                    = corporateWaterUsageM3 * allocationFraction;

            double allocatedWastewaterM3
                    = corporateWastewaterM3 * allocationFraction;

            // 6. Calculate product-level water intensities.
            double productWaterUsePerKg
                    = allocatedWaterUsageM3 / productWeightKg;

            double productWastewaterPerKg
                    = allocatedWastewaterM3 / productWeightKg;

            if (!Double.isFinite(allocationFraction)
                    || !Double.isFinite(allocatedWaterUsageM3)
                    || !Double.isFinite(allocatedWastewaterM3)
                    || !Double.isFinite(productWaterUsePerKg)
                    || !Double.isFinite(productWastewaterPerKg)) {

                throw new IllegalStateException(
                        "Invalid Product Water KPI calculation.");
            }

            // 7. Verify that allocation preserves corporate intensities.
            double corporateWaterUsePerKg
                    = corporate.get("waterUsePerKg").getAsDouble();

            double corporateWastewaterPerKg
                    = corporate.get("wastewaterPerKg").getAsDouble();

            double tolerance = 1e-9;

            if (Math.abs(productWaterUsePerKg - corporateWaterUsePerKg)
                    > tolerance
                    || Math.abs(productWastewaterPerKg
                            - corporateWastewaterPerKg) > tolerance) {

                throw new IllegalStateException(
                        "Product Water KPI intensities do not reconcile.");
            }

            // 8. Return product allocation results.
            result.addProperty("productWeightKg", productWeightKg);
            result.addProperty("productionOutputKg", productionOutputKg);
            result.addProperty("allocationFraction", allocationFraction);

            result.addProperty(
                    "corporateWaterUsageM3", corporateWaterUsageM3);

            result.addProperty(
                    "corporateWastewaterM3", corporateWastewaterM3);

            result.addProperty(
                    "allocatedWaterUsageM3", allocatedWaterUsageM3);

            result.addProperty(
                    "allocatedWastewaterM3", allocatedWastewaterM3);

            result.addProperty(
                    "productWaterUsePerKg", productWaterUsePerKg);

            result.addProperty(
                    "productWastewaterPerKg", productWastewaterPerKg);

            result.addProperty(
                    "allocatedWaterUsageLiters",
                    allocatedWaterUsageM3 * 1000.0);

            result.addProperty(
                    "allocatedWastewaterLiters",
                    allocatedWastewaterM3 * 1000.0);

            result.addProperty("absoluteUnit", "m3/product");
            result.addProperty("intensityUnit", "m3/kg product");

            result.addProperty(
                    "calculationBasis", "MASS_ALLOCATED_PROTOTYPE");

            result.addProperty(
                    "calculationBoundary", "PROCESS_WATER_AND_WASTEWATER");

            result.addProperty(
                    "storedCorporateIntensitiesVerified",
                    corporate.get("storedIntensitiesVerified").getAsBoolean());

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Computes corporate hazardous and non-hazardous Waste KPIs.
     *
     * Waste generation intensities are calculated from measured waste masses
     * and production output.
     *
     * Treatment percentages are read from the ontology and validated, not
     * calculated from treatment masses.
     */
    public static JsonObject computeCorporateWasteKpis(
            String processSustainabilityName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();

        result.addProperty(
                "processSustainability", processSustainabilityName);

        try {
            // 1. Retrieve the ProcessSustainability data.
            Map<String, String> props
                    = reader.getDataPropertiesForIndividual(
                            processSustainabilityName);

            if (props == null || props.isEmpty()) {
                throw new IllegalStateException(
                        "No data properties found for "
                        + processSustainabilityName);
            }

            // 2. Read the measured production and waste masses.
            double productionOutputKg = requirePositiveNumber(
                    props,
                    "hasProductionOutputKG",
                    processSustainabilityName);

            double hazardousWasteKg = requireNonNegativeNumber(
                    props,
                    "hasHazardousWasteGenerated",
                    processSustainabilityName);

            double nonHazardousWasteKg = requireNonNegativeNumber(
                    props,
                    "hasNonHazardousWasteGenerated",
                    processSustainabilityName);

            // 3. Calculate waste generation intensities.
            double hazardousWastePerKg
                    = hazardousWasteKg / productionOutputKg;

            double nonHazardousWastePerKg
                    = nonHazardousWasteKg / productionOutputKg;

            if (!Double.isFinite(hazardousWastePerKg)
                    || !Double.isFinite(nonHazardousWastePerKg)) {
                throw new IllegalStateException(
                        "Invalid Corporate Waste intensity calculation.");
            }

            // 4. Verify stored intensities when present.
            // Calculated intensities remain the source of truth.
            boolean storedIntensitiesVerified = true;

            if (props.containsKey("hasHazardousWastePerUnit")) {

                double stored = requireNonNegativeNumber(
                        props,
                        "hasHazardousWastePerUnit",
                        processSustainabilityName);

                if (Math.abs(stored - hazardousWastePerKg)
                        > 1e-6 * Math.max(
                                1.0, Math.abs(hazardousWastePerKg))) {
                    storedIntensitiesVerified = false;
                }
            }

            if (props.containsKey("hasNonHazardousWastePerUnit")) {

                double stored = requireNonNegativeNumber(
                        props,
                        "hasNonHazardousWastePerUnit",
                        processSustainabilityName);

                if (Math.abs(stored - nonHazardousWastePerKg)
                        > 1e-6 * Math.max(
                                1.0, Math.abs(nonHazardousWastePerKg))) {
                    storedIntensitiesVerified = false;
                }
            }

            // 5. Read hazardous waste treatment percentages.
            double hwInternal = requireNonNegativeNumber(
                    props,
                    "hasHazardousWasteInternallyRecycled",
                    processSustainabilityName);

            double hwLandfill = requireNonNegativeNumber(
                    props,
                    "hasHazardousWasteLandfillIncineration",
                    processSustainabilityName);

            double hwExternal = requireNonNegativeNumber(
                    props,
                    "hasHazardousWasteExternalRecycling",
                    processSustainabilityName);

            // 6. Read non-hazardous waste treatment percentages.
            double nhwInternal = requireNonNegativeNumber(
                    props,
                    "hasNonHazardousWasteInternallyRecycled",
                    processSustainabilityName);

            double nhwLandfill = requireNonNegativeNumber(
                    props,
                    "hasNonHazardousWasteLandfillIncineration",
                    processSustainabilityName);

            double nhwExternal = requireNonNegativeNumber(
                    props,
                    "hasNonHazardousWasteExternalRecycling",
                    processSustainabilityName);

            // 7. Validate percentage ranges.
            double[] percentages = {
                hwInternal, hwLandfill, hwExternal,
                nhwInternal, nhwLandfill, nhwExternal
            };

            for (double percentage : percentages) {
                if (!Double.isFinite(percentage)
                        || percentage > 100.0) {
                    throw new IllegalStateException(
                            "Waste treatment percentages must "
                            + "be between 0 and 100.");
                }
            }

            // 8. Check treatment-route completeness.
            // The prototype assumes three mutually exclusive,
            // exhaustive treatment routes per waste category.
            double hazardousTreatmentTotal
                    = hwInternal + hwLandfill + hwExternal;

            double nonHazardousTreatmentTotal
                    = nhwInternal + nhwLandfill + nhwExternal;

            boolean hazardousTreatmentComplete
                    = Math.abs(hazardousTreatmentTotal - 100.0) <= 1e-6;

            boolean nonHazardousTreatmentComplete
                    = Math.abs(nonHazardousTreatmentTotal - 100.0) <= 1e-6;

            // 9. Return absolute waste generation masses.
            result.addProperty(
                    "productionOutputKg", productionOutputKg);

            result.addProperty(
                    "hazardousWasteKg", hazardousWasteKg);

            result.addProperty(
                    "nonHazardousWasteKg", nonHazardousWasteKg);

            result.addProperty(
                    "totalWasteKg",
                    hazardousWasteKg + nonHazardousWasteKg);

            // 10. Return the two calculated waste intensities.
            result.addProperty(
                    "hazardousWastePerKg", hazardousWastePerKg);

            result.addProperty(
                    "nonHazardousWastePerKg", nonHazardousWastePerKg);

            // 11. Return hazardous waste treatment percentages.
            result.addProperty(
                    "hazardousInternalRecyclingPercent", hwInternal);

            result.addProperty(
                    "hazardousLandfillIncinerationPercent", hwLandfill);

            result.addProperty(
                    "hazardousExternalRecyclingPercent", hwExternal);

            // 12. Return non-hazardous waste treatment percentages.
            result.addProperty(
                    "nonHazardousInternalRecyclingPercent", nhwInternal);

            result.addProperty(
                    "nonHazardousLandfillIncinerationPercent", nhwLandfill);

            result.addProperty(
                    "nonHazardousExternalRecyclingPercent", nhwExternal);

            // 13. Return validation results.
            result.addProperty(
                    "storedIntensitiesVerified",
                    storedIntensitiesVerified);

            result.addProperty(
                    "hazardousTreatmentTotalPercent",
                    hazardousTreatmentTotal);

            result.addProperty(
                    "nonHazardousTreatmentTotalPercent",
                    nonHazardousTreatmentTotal);

            result.addProperty(
                    "hazardousTreatmentComplete",
                    hazardousTreatmentComplete);

            result.addProperty(
                    "nonHazardousTreatmentComplete",
                    nonHazardousTreatmentComplete);

            // 14. Return units and calculation metadata.
            result.addProperty("absoluteUnit", "kg");
            result.addProperty("intensityUnit", "kg/kg product");
            result.addProperty("treatmentUnit", "%");

            result.addProperty(
                    "calculationBasis", "PRODUCTION_OUTPUT_MASS");

            result.addProperty(
                    "calculationBoundary",
                    "PROCESS_HAZARDOUS_AND_NON_HAZARDOUS_WASTE");

            result.addProperty(
                    "treatmentDataBasis",
                    "ONTOLOGY_STORED_PERCENTAGES");

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Computes Product Waste KPIs using mass allocation from a
     * ProcessSustainability waste prototype.
     *
     * Product weight: ManufacturedProduct.hasManufacturedProductWeight (kg)
     *
     * Corporate inputs: ProcessSustainability.hasProductionOutputKG (kg)
     * ProcessSustainability.hasHazardousWasteGenerated (kg)
     * ProcessSustainability.hasNonHazardousWasteGenerated (kg)
     *
     * The six treatment percentages are read from the corporate calculation.
     * Under uniform mass allocation, the percentages remain unchanged.
     *
     * This is a prototype allocation, not an independently measured
     * product-specific waste assessment.
     */
    public static JsonObject computeProductWasteKpis(
            String manufacturedProductName,
            String processSustainabilityName,
            OntologyReader reader) {

        JsonObject result = new JsonObject();

        result.addProperty("product", manufacturedProductName);
        result.addProperty("manufacturedProduct", manufacturedProductName);
        result.addProperty(
                "processSustainability", processSustainabilityName);

        try {
            // 1. Read the manufactured product's actual weight.
            Map<String, String> productProps
                    = reader.getDataPropertiesForIndividual(
                            manufacturedProductName);

            if (productProps == null || productProps.isEmpty()) {
                throw new IllegalStateException(
                        "No data properties found for product: "
                        + manufacturedProductName);
            }

            double productWeightKg = requirePositiveNumber(
                    productProps,
                    "hasManufacturedProductWeight",
                    manufacturedProductName);

            // 2. Reuse the verified Corporate Waste calculation.
            JsonObject corporate = computeCorporateWasteKpis(
                    processSustainabilityName,
                    reader);

            if (corporate.has("error")) {
                throw new IllegalStateException(
                        "Corporate Waste calculation failed: "
                        + corporate.get("error").getAsString());
            }

            // 3. Read corporate production and waste totals.
            double productionOutputKg
                    = corporate.get("productionOutputKg").getAsDouble();

            double corporateHazardousWasteKg
                    = corporate.get("hazardousWasteKg").getAsDouble();

            double corporateNonHazardousWasteKg
                    = corporate.get("nonHazardousWasteKg").getAsDouble();

            if (!Double.isFinite(productionOutputKg)
                    || productionOutputKg <= 0.0
                    || !Double.isFinite(corporateHazardousWasteKg)
                    || corporateHazardousWasteKg < 0.0
                    || !Double.isFinite(corporateNonHazardousWasteKg)
                    || corporateNonHazardousWasteKg < 0.0) {

                throw new IllegalStateException(
                        "Invalid Corporate Waste values for allocation.");
            }

            // 4. Validate the mass-allocation boundary.
            if (productWeightKg > productionOutputKg) {
                throw new IllegalStateException(
                        "Product weight exceeds production output: "
                        + manufacturedProductName);
            }

            double allocationFraction
                    = productWeightKg / productionOutputKg;

            // 5. Allocate hazardous and non-hazardous waste masses.
            double allocatedHazardousWasteKg
                    = corporateHazardousWasteKg * allocationFraction;

            double allocatedNonHazardousWasteKg
                    = corporateNonHazardousWasteKg * allocationFraction;

            double totalAllocatedWasteKg
                    = allocatedHazardousWasteKg
                    + allocatedNonHazardousWasteKg;

            // 6. Calculate product-level waste intensities.
            double productHazardousWastePerKg
                    = allocatedHazardousWasteKg / productWeightKg;

            double productNonHazardousWastePerKg
                    = allocatedNonHazardousWasteKg / productWeightKg;

            if (!Double.isFinite(allocationFraction)
                    || !Double.isFinite(allocatedHazardousWasteKg)
                    || !Double.isFinite(allocatedNonHazardousWasteKg)
                    || !Double.isFinite(totalAllocatedWasteKg)
                    || !Double.isFinite(productHazardousWastePerKg)
                    || !Double.isFinite(productNonHazardousWastePerKg)) {

                throw new IllegalStateException(
                        "Invalid Product Waste KPI calculation.");
            }

            // 7. Verify that allocation preserves corporate intensities.
            double corporateHazardousWastePerKg
                    = corporate.get("hazardousWastePerKg").getAsDouble();

            double corporateNonHazardousWastePerKg
                    = corporate.get("nonHazardousWastePerKg").getAsDouble();

            double tolerance = 1e-9;

            if (Math.abs(productHazardousWastePerKg
                    - corporateHazardousWastePerKg) > tolerance
                    || Math.abs(productNonHazardousWastePerKg
                            - corporateNonHazardousWastePerKg) > tolerance) {

                throw new IllegalStateException(
                        "Product Waste KPI intensities do not reconcile.");
            }

            // 8. Read the six stored treatment percentages.
            // These are not calculated from treatment masses.
            double hwInternal
                    = corporate.get(
                            "hazardousInternalRecyclingPercent").getAsDouble();

            double hwLandfill
                    = corporate.get(
                            "hazardousLandfillIncinerationPercent").getAsDouble();

            double hwExternal
                    = corporate.get(
                            "hazardousExternalRecyclingPercent").getAsDouble();

            double nhwInternal
                    = corporate.get(
                            "nonHazardousInternalRecyclingPercent").getAsDouble();

            double nhwLandfill
                    = corporate.get(
                            "nonHazardousLandfillIncinerationPercent").getAsDouble();

            double nhwExternal
                    = corporate.get(
                            "nonHazardousExternalRecyclingPercent").getAsDouble();

            // 9. Return product allocation information.
            result.addProperty(
                    "productWeightKg", productWeightKg);

            result.addProperty(
                    "productionOutputKg", productionOutputKg);

            result.addProperty(
                    "allocationFraction", allocationFraction);

            result.addProperty(
                    "corporateHazardousWasteKg",
                    corporateHazardousWasteKg);

            result.addProperty(
                    "corporateNonHazardousWasteKg",
                    corporateNonHazardousWasteKg);

            result.addProperty(
                    "allocatedHazardousWasteKg",
                    allocatedHazardousWasteKg);

            result.addProperty(
                    "allocatedNonHazardousWasteKg",
                    allocatedNonHazardousWasteKg);

            result.addProperty(
                    "totalAllocatedWasteKg",
                    totalAllocatedWasteKg);

            // 10. Return calculated product waste intensities.
            result.addProperty(
                    "productHazardousWastePerKg",
                    productHazardousWastePerKg);

            result.addProperty(
                    "productNonHazardousWastePerKg",
                    productNonHazardousWastePerKg);

            // 11. Return treatment percentages.
            // Uniform mass allocation preserves these percentages.
            result.addProperty(
                    "hazardousInternalRecyclingPercent", hwInternal);

            result.addProperty(
                    "hazardousLandfillIncinerationPercent", hwLandfill);

            result.addProperty(
                    "hazardousExternalRecyclingPercent", hwExternal);

            result.addProperty(
                    "nonHazardousInternalRecyclingPercent", nhwInternal);

            result.addProperty(
                    "nonHazardousLandfillIncinerationPercent", nhwLandfill);

            result.addProperty(
                    "nonHazardousExternalRecyclingPercent", nhwExternal);

            // 12. Include corporate data-validation results.
            result.addProperty(
                    "storedCorporateIntensitiesVerified",
                    corporate.get(
                            "storedIntensitiesVerified").getAsBoolean());

            result.addProperty(
                    "hazardousTreatmentComplete",
                    corporate.get(
                            "hazardousTreatmentComplete").getAsBoolean());

            result.addProperty(
                    "nonHazardousTreatmentComplete",
                    corporate.get(
                            "nonHazardousTreatmentComplete").getAsBoolean());

            // 13. Return units and calculation metadata.
            result.addProperty("absoluteUnit", "kg");
            result.addProperty("intensityUnit", "kg/kg product");
            result.addProperty("treatmentUnit", "%");

            result.addProperty(
                    "calculationBasis", "MASS_ALLOCATED_PROTOTYPE");

            result.addProperty(
                    "calculationBoundary",
                    "PROCESS_HAZARDOUS_AND_NON_HAZARDOUS_WASTE");

            result.addProperty(
                    "treatmentDataBasis",
                    "CORPORATE_ONTOLOGY_STORED_PERCENTAGES");

        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
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

    private static String requireSingleRelatedIndividual(
            OntologyReader reader,
            String individualName,
            String propertyName) {

        List<String> values
                = reader.getObjectPropertyValues(
                        individualName, propertyName);

        java.util.Set<String> uniqueValues
                = new java.util.LinkedHashSet<>(values);

        if (uniqueValues.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one " + propertyName
                    + " relationship for " + individualName
                    + ", but found " + uniqueValues.size());
        }

        return uniqueValues.iterator().next();
    }

    private static double requirePositiveNumber(
            Map<String, String> properties,
            String propertyName,
            String individualName) {

        double value = requireNonNegativeNumber(
                properties, propertyName, individualName);

        if (value <= 0.0) {
            throw new IllegalStateException(
                    propertyName + " must be greater than zero for "
                    + individualName);
        }

        return value;
    }

    private static double requireNonNegativeNumber(
            Map<String, String> properties,
            String propertyName,
            String individualName) {

        String rawValue = properties.get(propertyName);

        if (rawValue == null || rawValue.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing " + propertyName + " for "
                    + individualName);
        }

        try {
            double value = Double.parseDouble(rawValue.trim());

            if (!Double.isFinite(value) || value < 0.0) {
                throw new IllegalStateException(
                        "Invalid " + propertyName + " for "
                        + individualName + ": " + rawValue);
            }

            return value;

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Non-numeric " + propertyName + " for "
                    + individualName + ": " + rawValue, e);
        }

    }
}
