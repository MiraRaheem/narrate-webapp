/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ontology;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.UUID;

/**
 * Coordinates KPI evaluation persistence and benchmarking.
 *
 * KPI calculations remain the responsibility of KpiCalculator.
 */
public class KpiEvaluationService {

    private final OntologyReader reader;

    public KpiEvaluationService() {
        // Reuse the singleton OntologyReader.
        this.reader = OntologyReader.getInstance();
    }

    /**
     * Save Material Intensity for one raw material, linked to its product.
     */
    public JsonObject saveMaterialIntensity(
            String materialName,
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (materialName == null || materialName.trim().isEmpty()
                || productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Material and product are required.");
        }

        if (!Double.isFinite(computedValue) || computedValue < 0) {
            throw new IllegalArgumentException(
                    "Invalid Material Intensity.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();

        String evaluationId = "MAT-INT-EVAL-" + uniqueId;

        String evaluationName
                = "MaterialIntensityEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Raw-material Material Intensity (kg/kg) = "
                + "material total input weight / manufactured product weight",
                resultProvenance
                + "; material=" + materialName
                + "; product=" + productName,
                "MaterialIntensityKPI",
                "MaterialIntensityFormula",
                materialName,
                productName);

        JsonObject result = new JsonObject();

        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("material", materialName);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "kg/kg");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save a successfully calculated Material Intensity result.
     *
     * @param computedValue Actual calculated intensity in kg/kg
     * @param resultProvenance Description of the calculation inputs
     * @return Evaluation details and benchmark assessment
     */
    public JsonObject saveMaterialIntensity(
            double computedValue,
            String resultProvenance) throws IOException {

        // 1. Validate the calculated result.
        if (!Double.isFinite(computedValue) || computedValue < 0) {
            throw new IllegalArgumentException(
                    "Material Intensity must be finite and non-negative.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance must be provided.");
        }

        // 2. Generate unique evaluation identifiers.
        String uniqueId = UUID.randomUUID().toString();

        String evaluationId = "MAT-INT-EVAL-" + uniqueId;

        String evaluationName
                = "MaterialIntensityEvaluation_"
                + uniqueId.replace("-", "");

        // 3. Persist using OntologyReader.
        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Material Intensity: consumedRawMaterialMassKg / "
                + "manufacturedProductMassKg; "
                + "calculationBasis=PLANNED_EQUALS_CONSUMED",
                resultProvenance);

        // 4. Assess using the existing benchmark service.
        JsonObject benchmark
                = KpiBenchmarkService.evaluateMaterialIntensity(
                        computedValue, reader);

        // 5. Return evaluation and benchmarking results.
        JsonObject response = new JsonObject();

        response.addProperty("status", "success");
        response.addProperty("evaluationID", evaluationId);
        response.addProperty("evaluationIndividual", savedIndividual);
        response.addProperty("computedValue", computedValue);
        response.addProperty("unit", "kg/kg");
        response.add("benchmark", benchmark);

        return response;
    }

    /**
     * Save the Renewable Content KPI for one raw material.
     *
     * @param materialName Raw material individual name
     * @param computedValue Renewable content percentage (0–100)
     * @param resultProvenance Calculation source and context
     */
    public JsonObject saveRenewableContent(
            String materialName,
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (materialName == null || materialName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Raw material name is required.");
        }

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manufactured product name is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0
                || computedValue > 100.0) {
            throw new IllegalArgumentException(
                    "Renewable Content must be between 0 and 100 percent.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String evaluationId = UUID.randomUUID().toString();

        String evaluationName
                = "RenewableContentEvaluation_"
                + evaluationId.replace("-", "");

        String calculationMethod
                = "Raw-material Renewable Content (%) = "
                + "hasMaterialRenewableContent * 100";

        String provenance
                = resultProvenance
                + "; material=" + materialName
                + "; product=" + productName;

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                calculationMethod,
                provenance,
                "RenewableContentKPI",
                "RenewableContentFormula",
                materialName,
                productName);

        JsonObject result = new JsonObject();

        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("material", materialName);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "%");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save the Recycled Content KPI evaluation for one raw material.
     */
    public JsonObject saveRecycledContent(
            String materialName,
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (materialName == null || materialName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Raw material name is required.");
        }

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manufactured product name is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0
                || computedValue > 100.0) {
            throw new IllegalArgumentException(
                    "Recycled Content must be between 0 and 100 percent.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String evaluationId = UUID.randomUUID().toString();

        String evaluationName
                = "RecycledContentEvaluation_"
                + evaluationId.replace("-", "");

        String calculationMethod
                = "Raw-material Recycled Content (%) = "
                + "hasMaterialRecycledContent * 100";

        String provenance
                = resultProvenance
                + "; material=" + materialName
                + "; product=" + productName;

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                calculationMethod,
                provenance,
                "RecycledContentKPI",
                "RecycledContentFormula",
                materialName,
                productName);

        JsonObject result = new JsonObject();

        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("material", materialName);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "%");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save Supply Transport Carbon Footprint for one raw material.
     *
     * computedValue is the total Road + Sea footprint in kg CO2eq.
     */
    public JsonObject saveSupplyTransportCarbonFootprint(
            String materialName,
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (materialName == null || materialName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Raw material name is required.");
        }

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manufactured product name is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0) {
            throw new IllegalArgumentException(
                    "Transport carbon footprint must be finite "
                    + "and non-negative.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String evaluationId = UUID.randomUUID().toString();

        String evaluationName
                = "SupplyTransportCarbonFootprintEvaluation_"
                + evaluationId.replace("-", "");

        String calculationMethod
                = "Supply Transport Carbon Footprint (kg CO2eq) = "
                + "SUM((shipmentWeightKg / 1000) "
                + "* distanceKm * emissionFactor) "
                + "for Road and Sea shipments";

        String provenance
                = resultProvenance
                + "; material=" + materialName
                + "; product=" + productName;

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                calculationMethod,
                provenance,
                "SupplyTransportCarbonFootprintKPI",
                "SupplyTransportCarbonFootprintFormula",
                materialName,
                productName);

        JsonObject result = new JsonObject();

        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("material", materialName);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "kg CO2eq");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save one Product Material Efficiency KPI evaluation.
     *
     * The evaluated entity is the manufactured product itself. The benchmark
     * assessment is saved separately by the servlet.
     */
    public JsonObject saveProductMaterialEfficiency(
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manufactured product is required.");
        }

        if (!Double.isFinite(computedValue) || computedValue < 0.0) {
            throw new IllegalArgumentException(
                    "Invalid Product Material Efficiency value.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();

        String evaluationId = "PROD-MAT-EFF-EVAL-" + uniqueId;

        String evaluationName
                = "ProductMaterialEfficiencyEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Product Material Efficiency (kg/kg) = "
                + "total material input weight / manufactured product weight",
                resultProvenance + "; product=" + productName,
                "ProductMaterialEfficiencyKPI",
                "ProductMaterialEfficiencyFormula",
                productName,
                null);

        JsonObject result = new JsonObject();

        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "kg/kg");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save one Product Total Transport Carbon KPI evaluation.
     *
     * The evaluated entity is the manufactured product. The BenchmarkAssessment
     * is saved separately by the servlet.
     */
    public JsonObject saveProductTransportCarbon(
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manufactured product is required.");
        }

        if (!Double.isFinite(computedValue) || computedValue < 0.0) {
            throw new IllegalArgumentException(
                    "Invalid Product Total Transport Carbon value.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();

        String evaluationId
                = "PROD-TRANS-CARB-EVAL-" + uniqueId;

        String evaluationName
                = "ProductTransportCarbonEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Product Total Transport Carbon (kg CO2eq) = "
                + "SUM of road and sea shipment carbon emissions "
                + "across all materials in the manufactured product",
                resultProvenance + "; product=" + productName,
                "ProductTransportCarbonKPI",
                "ProductTransportCarbonFormula",
                productName,
                null);

        JsonObject result = new JsonObject();

        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "kg CO2eq");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save one Product Renewable Material KPI evaluation.
     *
     * The evaluated entity is the manufactured product. The benchmark
     * assessment is saved separately by the servlet.
     */
    public JsonObject saveProductRenewableMaterial(
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manufactured product is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0
                || computedValue > 100.0) {

            throw new IllegalArgumentException(
                    "Product Renewable Material percentage "
                    + "must be between 0 and 100.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();

        String evaluationId
                = "PROD-REN-MAT-EVAL-" + uniqueId;

        String evaluationName
                = "ProductRenewableMaterialEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Product Renewable Material (%) = "
                + "100 * total renewable material weight "
                + "/ total material input weight",
                resultProvenance + "; product=" + productName,
                "ProductRenewableMaterialKPI",
                "ProductRenewableMaterialFormula",
                productName,
                null);

        JsonObject result = new JsonObject();

        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty(
                "evaluationIndividual", savedIndividual);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "%");
        result.addProperty(
                "benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save one Product Recycled Material KPI evaluation.
     *
     * The evaluated entity is the manufactured product. Benchmark assessment
     * persistence is handled by the servlet.
     */
    public JsonObject saveProductRecycledMaterial(
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manufactured product is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0
                || computedValue > 100.0) {

            throw new IllegalArgumentException(
                    "Product Recycled Material percentage "
                    + "must be between 0 and 100.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();

        String evaluationId
                = "PROD-REC-MAT-EVAL-" + uniqueId;

        String evaluationName
                = "ProductRecycledMaterialEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Product Recycled Material (%) = "
                + "100 * total recycled material weight "
                + "/ total material input weight",
                resultProvenance + "; product=" + productName,
                "ProductRecycledMaterialKPI",
                "ProductRecycledMaterialFormula",
                productName,
                null);

        JsonObject result = new JsonObject();

        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty(
                "evaluationIndividual", savedIndividual);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "%");
        result.addProperty(
                "benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save the CorporateEnergyCarbonIntensity KPI evaluation. Benchmark
     * assessment persistence is handled separately by the servlet.
     */
    public JsonObject saveCorporateEnergyCarbonIntensity(
            String processSustainability,
            double computedValue,
            String resultProvenance) throws IOException {

        if (processSustainability == null || processSustainability.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "processSustainability is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0) {
            throw new IllegalArgumentException(
                    "Invalid CorporateEnergyCarbonIntensity value.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();
        String evaluationId = "CORP-ENERGY-CARB-EVAL-" + uniqueId;
        String evaluationName = "CorporateEnergyCarbonIntensityEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Corporate Energy Carbon Intensity (kg CO2eq/kg) = total energy carbon emissions / production output mass",
                resultProvenance + "; processSustainability=" + processSustainability,
                "CorporateEnergyCarbonKPI",
                "CorporateEnergyCarbonFormula",
                processSustainability,
                null);

        JsonObject result = new JsonObject();
        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("processSustainability", processSustainability);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "kg CO2eq/kg");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save the CorporateRenewableElectricity KPI evaluation. Benchmark
     * assessment persistence is handled separately by the servlet.
     */
    public JsonObject saveCorporateRenewableElectricity(
            String processSustainability,
            double computedValue,
            String resultProvenance) throws IOException {

        if (processSustainability == null || processSustainability.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "processSustainability is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0 || computedValue > 100.0) {
            throw new IllegalArgumentException(
                    "Invalid CorporateRenewableElectricity value.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();
        String evaluationId = "CORP-REN-ELEC-EVAL-" + uniqueId;
        String evaluationName = "CorporateRenewableElectricityEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Corporate Renewable Electricity (%) = 100 * renewable electricity consumption / total electricity consumption",
                resultProvenance + "; processSustainability=" + processSustainability,
                "CorporateRenewableElectricityKPI",
                "CorporateRenewableElectricityFormula",
                processSustainability,
                null);

        JsonObject result = new JsonObject();
        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("processSustainability", processSustainability);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "%");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save the ProductEnergyCarbonFootprint KPI evaluation. Benchmark
     * assessment persistence is handled separately by the servlet.
     */
    public JsonObject saveProductEnergyCarbonFootprint(
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "product is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0) {
            throw new IllegalArgumentException(
                    "Invalid ProductEnergyCarbonFootprint value.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();
        String evaluationId = "PROD-ENERGY-CARB-EVAL-" + uniqueId;
        String evaluationName = "ProductEnergyCarbonFootprintEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Product Energy Carbon Footprint (kg CO2eq) = mass-allocated corporate energy carbon emissions",
                resultProvenance + "; product=" + productName,
                "ProductEnergyCarbonKPI",
                "ProductEnergyCarbonFormula",
                productName,
                null);

        JsonObject result = new JsonObject();
        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "kg CO2eq");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }

    /**
     * Save the ProductEmbodiedCarbon KPI evaluation. Benchmark assessment
     * persistence is handled separately by the servlet.
     */
    public JsonObject saveProductEmbodiedCarbon(
            String productName,
            double computedValue,
            String resultProvenance) throws IOException {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "product is required.");
        }

        if (!Double.isFinite(computedValue)
                || computedValue < 0.0) {
            throw new IllegalArgumentException(
                    "Invalid ProductEmbodiedCarbon value.");
        }

        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Result provenance is required.");
        }

        String uniqueId = UUID.randomUUID().toString();
        String evaluationId = "PROD-EMB-CARB-EVAL-" + uniqueId;
        String evaluationName = "ProductEmbodiedCarbonEvaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                "Product Energy-Related Embodied Carbon Intensity (kg CO2eq/kg product) = allocated energy carbon emissions / manufactured product weight",
                resultProvenance + "; product=" + productName,
                "ProductEmbodiedCarbonKPI",
                "ProductEmbodiedCarbonFormula",
                productName,
                null);

        JsonObject result = new JsonObject();
        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("product", productName);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", "kg CO2eq/kg product");
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");

        return result;
    }


    /**
     * Persist a Water/Waste KPIEvaluation using the existing OntologyReader
     * persistence method. BenchmarkAssessment is saved by the servlet.
     */
    public JsonObject saveWaterWasteEvaluation(
            String kpiBaseName, String individual,
            String processSustainability, double computedValue,
            String unit, String resultProvenance) throws IOException {

        if (individual == null || individual.trim().isEmpty()) {
            throw new IllegalArgumentException("individual is required.");
        }
        if (processSustainability == null
                || processSustainability.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "processSustainability is required.");
        }
        if (!Double.isFinite(computedValue) || computedValue < 0.0) {
            throw new IllegalArgumentException(
                    "Water/Waste KPI must be finite and non-negative.");
        }
        if (resultProvenance == null
                || resultProvenance.trim().isEmpty()) {
            throw new IllegalArgumentException("Provenance is required.");
        }

        // Only the eight supported definitions can be persisted.
        switch (kpiBaseName) {
            case "CorporateWaterUse":
            case "CorporateWastewater":
            case "ProductWaterUse":
            case "ProductWastewater":
            case "CorporateHazardousWaste":
            case "CorporateNonHazardousWaste":
            case "ProductHazardousWaste":
            case "ProductNonHazardousWaste":
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown Water/Waste KPI: " + kpiBaseName);
        }

        String uniqueId = UUID.randomUUID().toString();
        String evaluationId = "WATER-WASTE-EVAL-" + uniqueId;
        String evaluationName = kpiBaseName + "Evaluation_"
                + uniqueId.replace("-", "");

        String savedIndividual = reader.createKpiEvaluation(
                evaluationName,
                evaluationId,
                computedValue,
                kpiBaseName + " (" + unit
                + ") computed by KpiCalculator",
                resultProvenance + "; individual=" + individual
                + "; processSustainability=" + processSustainability,
                kpiBaseName + "KPI",
                kpiBaseName + "Formula",
                individual,
                null);

        JsonObject result = new JsonObject();
        result.addProperty("status", "success");
        result.addProperty("evaluationID", evaluationId);
        result.addProperty("evaluationIndividual", savedIndividual);
        result.addProperty("individual", individual);
        result.addProperty("processSustainability", processSustainability);
        result.addProperty("computedValue", computedValue);
        result.addProperty("unit", unit);
        result.addProperty("benchmarkStatus", "NOT_BENCHMARKED");
        return result;
    }

}
