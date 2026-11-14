/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ontology;

/**
 *
 * @author Amal Elgammal
 */
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

/**
 * Retrieves KPI benchmarks from the ontology and compares calculated KPI values
 * against their configured targets.
 *
 * This service is read-only.
 */
public class KpiBenchmarkService {

    private KpiBenchmarkService() {
        // Utility class; no instances required.
    }

    /**
     * TEMPORARY TEST ONLY. Tests the existing benchmark service without
     * modifying the ontology or dashboard.
     */
    public static void main(String[] args) {

        OntologyReader reader = OntologyReader.getInstance();

        JsonObject assessment
                = evaluateRecycledContent(40.0, reader);

        System.out.println(
                "RECYCLED CONTENT BENCHMARK TEST: "
                + assessment.toString());
    }

    /**
     * Evaluate Material Intensity against its ontology benchmark.
     *
     * @param actualValue Calculated overall Material Intensity
     * @param reader Existing OntologyReader instance
     * @return JSON benchmark assessment
     */
    public static JsonObject evaluateMaterialIntensity(
            double actualValue,
            OntologyReader reader) {

        return evaluate(
                "MaterialIntensityKPI",
                "MaterialIntensityBenchmark",
                actualValue,
                "kg/kg",
                reader);
    }

    /**
     * Evaluate the aggregated Product Material Efficiency against its separate
     * product-level ontology benchmark.
     *
     * Unit: kg/kg.
     */
    public static JsonObject evaluateProductMaterialEfficiency(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue) || actualValue < 0.0) {

            JsonObject error = new JsonObject();

            error.addProperty("kpi", "ProductMaterialEfficiencyKPI");
            error.addProperty("benchmark",
                    "ProductMaterialEfficiencyBenchmark");
            error.addProperty("status", "error");
            error.addProperty("error",
                    "Product Material Efficiency must be finite "
                    + "and non-negative.");

            return error;
        }

        return evaluate(
                "ProductMaterialEfficiencyKPI",
                "ProductMaterialEfficiencyBenchmark",
                actualValue,
                "kg/kg",
                reader);
    }

    /**
     * Evaluate the percentage of renewable material in the manufactured
     * product.
     *
     * Uses the separate product-level benchmark. Unit: %.
     */
    public static JsonObject evaluateProductRenewableMaterial(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue)
                || actualValue < 0.0
                || actualValue > 100.0) {

            JsonObject error = new JsonObject();

            error.addProperty("kpi", "ProductRenewableMaterialKPI");
            error.addProperty(
                    "benchmark", "ProductRenewableMaterialBenchmark");

            error.addProperty("status", "error");
            error.addProperty(
                    "error",
                    "Product Renewable Material percentage "
                    + "must be between 0 and 100.");

            return error;
        }

        return evaluate(
                "ProductRenewableMaterialKPI",
                "ProductRenewableMaterialBenchmark",
                actualValue,
                "%",
                reader);
    }

    /**
     * Evaluate Renewable Content against its ontology benchmark.
     *
     * @param actualValue Calculated Renewable Content percentage
     * @param reader Existing OntologyReader instance
     * @return JSON benchmark assessment
     */
    public static JsonObject evaluateRenewableContent(
            double actualValue,
            OntologyReader reader) {

        return evaluate(
                "RenewableContentKPI",
                "RenewableContentBenchmark",
                actualValue,
                "%",
                reader);
    }

    /**
     * Evaluate Recycled Content against its ontology benchmark.
     *
     * @param actualValue Recycled content of a raw material, expressed as a
     * percentage from 0 to 100
     * @param reader Existing OntologyReader instance
     * @return JSON benchmark assessment
     */
    public static JsonObject evaluateRecycledContent(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue)
                || actualValue < 0.0
                || actualValue > 100.0) {

            JsonObject error = new JsonObject();
            error.addProperty("kpi", "RecycledContentKPI");
            error.addProperty("benchmark", "RecycledContentBenchmark");
            error.addProperty("status", "error");
            error.addProperty("error",
                    "Recycled Content percentage must be between 0 and 100.");
            return error;
        }

        return evaluate(
                "RecycledContentKPI",
                "RecycledContentBenchmark",
                actualValue,
                "%",
                reader);
    }

    /**
     * Evaluate Supply Transport Carbon Footprint for one raw material against
     * the shared ontology benchmark.
     *
     * Unit: kg CO2eq.
     */
    public static JsonObject evaluateSupplyTransportCarbonFootprint(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue) || actualValue < 0.0) {

            JsonObject error = new JsonObject();

            error.addProperty(
                    "kpi", "SupplyTransportCarbonFootprintKPI");

            error.addProperty(
                    "benchmark", "SupplyTransportCarbonFootprintBenchmark");

            error.addProperty("status", "error");

            error.addProperty(
                    "error",
                    "Transport carbon footprint must be finite "
                    + "and non-negative.");

            return error;
        }

        return evaluate(
                "SupplyTransportCarbonFootprintKPI",
                "SupplyTransportCarbonFootprintBenchmark",
                actualValue,
                "kg CO2eq",
                reader);
    }

    /**
     * Evaluate Total Transport Carbon for the entire manufactured product.
     *
     * Uses the separate product-level benchmark stored in the ontology. Unit:
     * kg CO2eq.
     */
    public static JsonObject evaluateProductTransportCarbon(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue) || actualValue < 0.0) {

            JsonObject error = new JsonObject();

            error.addProperty("kpi", "ProductTransportCarbonKPI");
            error.addProperty(
                    "benchmark", "ProductTransportCarbonBenchmark");
            error.addProperty("status", "error");
            error.addProperty(
                    "error",
                    "Product Total Transport Carbon must be finite "
                    + "and non-negative.");

            return error;
        }

        return evaluate(
                "ProductTransportCarbonKPI",
                "ProductTransportCarbonBenchmark",
                actualValue,
                "kg CO2eq",
                reader);
    }

    /**
     * Evaluate Corporate Energy Carbon Intensity (kg CO2eq/kg).
     */
    public static JsonObject evaluateCorporateEnergyCarbonIntensity(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("kpi", "CorporateEnergyCarbonKPI");
            error.addProperty("benchmark", "CorporateEnergyCarbonBenchmark");
            error.addProperty("status", "error");
            error.addProperty("error",
                    "Corporate Energy Carbon Intensity must be finite and non-negative.");
            return error;
        }

        return evaluate(
                "CorporateEnergyCarbonKPI",
                "CorporateEnergyCarbonBenchmark",
                actualValue,
                "kg CO2eq/kg",
                reader);
    }

    /**
     * Evaluate Corporate Renewable Electricity Share (percentage).
     */
    public static JsonObject evaluateCorporateRenewableElectricity(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue)
                || actualValue < 0.0
                || actualValue > 100.0) {
            JsonObject error = new JsonObject();
            error.addProperty("kpi", "CorporateRenewableElectricityKPI");
            error.addProperty("benchmark", "CorporateRenewableElectricityBenchmark");
            error.addProperty("status", "error");
            error.addProperty("error",
                    "Corporate Renewable Electricity percentage must be between 0 and 100.");
            return error;
        }

        return evaluate(
                "CorporateRenewableElectricityKPI",
                "CorporateRenewableElectricityBenchmark",
                actualValue,
                "%",
                reader);
    }

    /**
     * Evaluate Product Energy Carbon Footprint (kg CO2eq).
     */
    public static JsonObject evaluateProductEnergyCarbonFootprint(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("kpi", "ProductEnergyCarbonKPI");
            error.addProperty("benchmark", "ProductEnergyCarbonBenchmark");
            error.addProperty("status", "error");
            error.addProperty("error",
                    "Product Energy Carbon Footprint must be finite and non-negative.");
            return error;
        }

        return evaluate(
                "ProductEnergyCarbonKPI",
                "ProductEnergyCarbonBenchmark",
                actualValue,
                "kg CO2eq",
                reader);
    }

    /**
     * Evaluate Product Energy-Related Embodied Carbon Intensity
     * (kg CO2eq/kg product).
     */
    public static JsonObject evaluateProductEmbodiedCarbon(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("kpi", "ProductEmbodiedCarbonKPI");
            error.addProperty("benchmark", "ProductEmbodiedCarbonBenchmark");
            error.addProperty("status", "error");
            error.addProperty("error",
                    "Product Energy-Related Embodied Carbon Intensity must be finite and non-negative.");
            return error;
        }

        return evaluate(
                "ProductEmbodiedCarbonKPI",
                "ProductEmbodiedCarbonBenchmark",
                actualValue,
                "kg CO2eq/kg product",
                reader);
    }

    /**
     * Generic benchmark evaluation for future KPIs.
     */
    public static JsonObject evaluate(
            String kpiName,
            String benchmarkName,
            double actualValue,
            String actualUnit,
            OntologyReader reader) {

        JsonObject result = new JsonObject();

        result.addProperty("kpi", kpiName);
        result.addProperty("benchmark", benchmarkName);
        result.addProperty("actualValue", actualValue);

        try {
            if (reader == null) {
                throw new IllegalArgumentException(
                        "OntologyReader cannot be null.");
            }

            if (!Double.isFinite(actualValue)) {
                throw new IllegalArgumentException(
                        "Actual KPI value must be finite.");
            }

            // 1. Retrieve benchmark properties from the ontology.
            Map<String, String> props
                    = reader.getDataPropertiesForIndividual(benchmarkName);

            if (props == null || props.isEmpty()) {
                throw new IllegalStateException(
                        "Benchmark individual not found or has no data: "
                        + benchmarkName);
            }

            // 2. Verify the benchmark belongs to the requested KPI.
            List<String> linkedKpis
                    = reader.getObjectPropertyValues(
                            benchmarkName, "isBenchmarkOf");

            if (linkedKpis == null || !linkedKpis.contains(kpiName)) {
                throw new IllegalStateException(
                        "Benchmark is not linked to KPI: " + kpiName);
            }

            // 3. Read and validate benchmark metadata.
            String rawValue = required(
                    props, "benchmarkValue", benchmarkName);

            String operator = required(
                    props, "benchmarkComparisonOperator", benchmarkName);

            String benchmarkUnit = required(
                    props, "benchmarkUnit", benchmarkName);

            String source = required(
                    props, "benchmarkSource", benchmarkName);

            if (!benchmarkUnit.equals(actualUnit)) {
                throw new IllegalStateException(
                        "Unit mismatch: actual=" + actualUnit
                        + ", benchmark=" + benchmarkUnit);
            }

            // OntologyReader returns literal lexical values,
            // so a typed xsd:decimal is normally returned as "1.10".
            double benchmarkValue = Double.parseDouble(rawValue);

            if (!Double.isFinite(benchmarkValue)) {
                throw new IllegalStateException(
                        "Benchmark value must be finite.");
            }

            // 4. Apply the comparison operator from the ontology.
            boolean meetsBenchmark;

            switch (operator) {
                case "<=":
                    meetsBenchmark = actualValue <= benchmarkValue;
                    break;
                case "<":
                    meetsBenchmark = actualValue < benchmarkValue;
                    break;
                case ">=":
                    meetsBenchmark = actualValue >= benchmarkValue;
                    break;
                case ">":
                    meetsBenchmark = actualValue > benchmarkValue;
                    break;
                case "=":
                    meetsBenchmark
                            = Double.compare(actualValue, benchmarkValue) == 0;
                    break;
                default:
                    throw new IllegalStateException(
                            "Unsupported benchmark operator: " + operator);
            }

            // 5. Calculate deviation from the benchmark.
            double absoluteDeviation = actualValue - benchmarkValue;

            result.addProperty("benchmarkValue", benchmarkValue);
            result.addProperty("benchmarkUnit", benchmarkUnit);
            result.addProperty("comparisonOperator", operator);
            result.addProperty("benchmarkSource", source);
            result.addProperty("meetsBenchmark", meetsBenchmark);
            result.addProperty("absoluteDeviation", absoluteDeviation);

            if (benchmarkValue != 0.0) {
                double deviationPercentage
                        = (absoluteDeviation / benchmarkValue) * 100.0;

                result.addProperty(
                        "deviationPercentage", deviationPercentage);
            }

            result.addProperty(
                    "assessment",
                    meetsBenchmark ? "TARGET_MET" : "TARGET_NOT_MET");

            result.addProperty("status", "success");

        } catch (Exception e) {
            result.addProperty("status", "error");
            result.addProperty("error", e.getMessage());
        }

        return result;
    }

    /**
     * Require a non-empty ontology data property.
     */
    private static String required(
            Map<String, String> props,
            String property,
            String individual) {

        String value = props.get(property);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing " + property + " for " + individual);
        }

        return value.trim();
    }

    /**
     * Evaluate Product Recycled Material percentage against its product-level
     * ontology benchmark.
     *
     * Higher is better: >= 30% demonstration target.
     */
    public static JsonObject evaluateProductRecycledMaterial(
            double actualValue,
            OntologyReader reader) {

        if (!Double.isFinite(actualValue)
                || actualValue < 0.0
                || actualValue > 100.0) {

            JsonObject error = new JsonObject();

            error.addProperty("status", "error");
            error.addProperty("kpi", "ProductRecycledMaterialKPI");
            error.addProperty(
                    "benchmark", "ProductRecycledMaterialBenchmark");
            error.addProperty(
                    "error",
                    "Product Recycled Material percentage "
                    + "must be between 0 and 100.");

            return error;
        }

        return evaluate(
                "ProductRecycledMaterialKPI",
                "ProductRecycledMaterialBenchmark",
                actualValue,
                "%",
                reader);
    }

    // Water and Waste benchmarks. Units match the persisted ontology definitions.

    public static JsonObject evaluateCorporateWaterUse(
            double actualValue, OntologyReader reader) {
        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("kpi", "CorporateWaterUseKPI");
            error.addProperty("benchmark", "CorporateWaterUseBenchmark");
            error.addProperty("error", "KPI value must be finite and non-negative.");
            return error;
        }
        return evaluate("CorporateWaterUseKPI", "CorporateWaterUseBenchmark",
                actualValue, "m3/kg", reader);
    }

    public static JsonObject evaluateCorporateWastewater(
            double actualValue, OntologyReader reader) {
        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("kpi", "CorporateWastewaterKPI");
            error.addProperty("benchmark", "CorporateWastewaterBenchmark");
            error.addProperty("error", "KPI value must be finite and non-negative.");
            return error;
        }
        return evaluate("CorporateWastewaterKPI", "CorporateWastewaterBenchmark",
                actualValue, "m3/kg", reader);
    }

    public static JsonObject evaluateProductWaterUse(
            double actualValue, OntologyReader reader) {
        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("kpi", "ProductWaterUseKPI");
            error.addProperty("benchmark", "ProductWaterUseBenchmark");
            error.addProperty("error", "KPI value must be finite and non-negative.");
            return error;
        }
        return evaluate("ProductWaterUseKPI", "ProductWaterUseBenchmark",
                actualValue, "m3/product", reader);
    }

    public static JsonObject evaluateProductWastewater(
            double actualValue, OntologyReader reader) {
        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("kpi", "ProductWastewaterKPI");
            error.addProperty("benchmark", "ProductWastewaterBenchmark");
            error.addProperty("error", "KPI value must be finite and non-negative.");
            return error;
        }
        return evaluate("ProductWastewaterKPI", "ProductWastewaterBenchmark",
                actualValue, "m3/product", reader);
    }

    public static JsonObject evaluateCorporateHazardousWaste(
            double actualValue, OntologyReader reader) {
        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("kpi", "CorporateHazardousWasteKPI");
            error.addProperty("benchmark", "CorporateHazardousWasteBenchmark");
            error.addProperty("error", "KPI value must be finite and non-negative.");
            return error;
        }
        return evaluate("CorporateHazardousWasteKPI", "CorporateHazardousWasteBenchmark",
                actualValue, "kg/kg", reader);
    }

    public static JsonObject evaluateCorporateNonHazardousWaste(
            double actualValue, OntologyReader reader) {
        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("kpi", "CorporateNonHazardousWasteKPI");
            error.addProperty("benchmark", "CorporateNonHazardousWasteBenchmark");
            error.addProperty("error", "KPI value must be finite and non-negative.");
            return error;
        }
        return evaluate("CorporateNonHazardousWasteKPI", "CorporateNonHazardousWasteBenchmark",
                actualValue, "kg/kg", reader);
    }

    public static JsonObject evaluateProductHazardousWaste(
            double actualValue, OntologyReader reader) {
        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("kpi", "ProductHazardousWasteKPI");
            error.addProperty("benchmark", "ProductHazardousWasteBenchmark");
            error.addProperty("error", "KPI value must be finite and non-negative.");
            return error;
        }
        return evaluate("ProductHazardousWasteKPI", "ProductHazardousWasteBenchmark",
                actualValue, "kg/product", reader);
    }

    public static JsonObject evaluateProductNonHazardousWaste(
            double actualValue, OntologyReader reader) {
        if (!Double.isFinite(actualValue) || actualValue < 0.0) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("kpi", "ProductNonHazardousWasteKPI");
            error.addProperty("benchmark", "ProductNonHazardousWasteBenchmark");
            error.addProperty("error", "KPI value must be finite and non-negative.");
            return error;
        }
        return evaluate("ProductNonHazardousWasteKPI", "ProductNonHazardousWasteBenchmark",
                actualValue, "kg/product", reader);
    }

}
