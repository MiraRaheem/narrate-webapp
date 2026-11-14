/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

document.addEventListener("DOMContentLoaded", function () {
    const productSelect = document.getElementById("productSelect");


    // ==========================================
    // MATERIAL KPI EXPLANATIONS
    // ==========================================

    function renderMaterialKpiExplanation(kpiType, data) {

        const explanation =
                document.getElementById("materialKpiExplanation");

        if (!explanation) {
            return;
        }

        const explanations = {

            materialIntensity: `
                <h6><strong>Understanding Material Intensity</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    The amount of material input required to produce
                    one kilogram of finished product.
                </p>

                <p>
                    <strong>Formula:</strong><br>
                    Material Intensity =
                    Total material input (kg) /
                    Finished product weight (kg).
                </p>

                <p>
                    <strong>Unit:</strong> kg/kg.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    Each bar shows the material input contribution
                    to the product's material intensity.
                    Larger bars indicate greater material consumption
                    per kilogram of finished product.
                </p>

                <p class="mb-0 text-muted small">
                    Material intensity measures input requirements.
                    It should not automatically be interpreted as
                    the mass of waste generated.
                </p>
            `,

            renewableContent: `
                <h6><strong>Understanding Renewable Content</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    The share of material input that comes from
                    renewable sources.
                </p>

                <p>
                    <strong>Formula:</strong><br>
                    Renewable Content (%) =
                    Renewable material mass /
                    Total material input mass × 100.
                </p>

                <p>
                    <strong>Unit:</strong> %.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    Each bar shows the renewable share of an
                    individual material. A value of 100%
                    indicates that the material input is
                    entirely classified as renewable.
                </p>

                <p class="mb-0 text-muted small">
                    Renewable content describes material origin.
                    It does not by itself establish recycled content,
                    biodegradability, or overall environmental impact.
                </p>
            `,

            recycledContent: `
                <h6><strong>Understanding Recycled Content</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    The share of material input that consists
                    of recycled material.
                </p>

                <p>
                    <strong>Formula:</strong><br>
                    Recycled Content (%) =
                    Recycled material mass /
                    Total material input mass × 100.
                </p>

                <p>
                    <strong>Unit:</strong> %.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    Each bar shows the recycled share of a
                    particular material.
                </p>

                <p class="mb-0 text-muted small">
                    Recycled content describes the origin of
                    material inputs. It does not measure how
                    much of the finished product will be
                    recycled at the end of its life.
                </p>
            `,

            supplyTransportCarbonFootprint: `
                <h6><strong>Understanding Supply Transport Carbon Footprint</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    Greenhouse gas emissions associated with
                    the modeled transportation of material inputs.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Transport emissions are calculated using
                    transported quantities, transport distances,
                    and the relevant transport emission factors.
                    Contributions are aggregated across the
                    modeled transport routes.
                </p>

                <p>
                    <strong>Unit:</strong> kg CO₂eq.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    The stacked bars separate road and sea
                    transport emissions for each material.
                    The combined bar represents its modeled
                    transport carbon footprint.
                </p>

                <p class="mb-0 text-muted small">
                    The result represents the transport activities
                    included in the current model, not necessarily
                    the complete product life-cycle carbon footprint.
                </p>
            `
        };

        explanation.innerHTML = explanations[kpiType] || "";
        explanation.style.display =
                explanations[kpiType] ? "block" : "none";
    }


    function clearKpiResults() {

        // Remove the previous chart.
        if (window.materialChartInstance) {
            window.materialChartInstance.destroy();
            window.materialChartInstance = null;
        }

        // Remove the previous KPI tables and explanations.
        [
            "materialIntensityTable",
            "renewableContentTable",
            "recycledContentTable",
            "supplyTransportCarbonFootprintTable",
            "materialIntensityExplanation"
        ].forEach(id => {
            const element = document.getElementById(id);

            if (element) {
                element.remove();
            }
        });

        const intensityPanel =
                document.getElementById("materialIntensityBenchmarkResults");

        if (intensityPanel) {
            intensityPanel.replaceChildren();
            intensityPanel.style.display = "none";
        }

        const recycledPanel =
                document.getElementById("recycledBenchmarkResults");

        if (recycledPanel) {
            recycledPanel.replaceChildren();
            recycledPanel.style.display = "none";
        }

        const renewablePanel =
                document.getElementById("renewableBenchmarkResults");

        if (renewablePanel) {
            renewablePanel.replaceChildren();
            renewablePanel.style.display = "none";
        }

        const transportPanel =
                document.getElementById("supplyTransportBenchmarkResults");

        if (transportPanel) {
            transportPanel.replaceChildren();
            transportPanel.style.display = "none";
        }
    }


    // ==========================================
    // PHASE E2 — PRODUCT KPI EXPLANATIONS
    // ==========================================

    function renderProductKpiExplanation(kpiType, data) {

        const explanation =
                document.getElementById("productKpiExplanation");

        if (!explanation) {
            return;
        }

        const explanations = {

            productMaterialEfficiency: `
    <h6><strong>Understanding Product Material Efficiency</strong></h6>

    <p>
        <strong>What it measures:</strong>
        Total modeled material input mass required per
        kilogram of manufactured product.
    </p>

    <p>
        <strong>Calculation:</strong><br>
        Product Material Efficiency (kg/kg) =
        Total material input weight (kg) /
        Manufactured product weight (kg).
    </p>

    <p>
        <strong>Unit:</strong> kg/kg.
        This is an input-intensity ratio, not a percentage.
        Lower values indicate less material input
        per kilogram of finished product.
    </p>

    <p>
        <strong>Chart interpretation:</strong>
        Each bar shows the contribution of one material
        to the overall product material input intensity.
        The sum of the contributions equals the
        product-level KPI value.
    </p>

    <p>
        <strong>Benchmark:</strong>
        The calculated value is compared with the
        product-level demonstration target stored
        in the ontology.
    </p>

    <p class="mb-0 text-muted small">
        The calculation uses the
        PLANNED_EQUALS_CONSUMED assumption.
        It does not independently measure production
        losses or waste.
    </p>
`,

            productTransportCarbon: `
                <h6><strong>Understanding Product Transport Carbon</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    The modeled greenhouse gas emissions
                    associated with transporting material inputs
                    for the selected product.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Transport emissions are calculated using
                    transported material quantities, distances,
                    and applicable transport emission factors.
                    The contributions are summed across the
                    modeled transport activities.
                </p>

                <p>
                    <strong>Unit:</strong> kg CO₂eq.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    The chart compares transport emissions
                    associated with individual material inputs.
                    Higher contributions indicate materials
                    responsible for more modeled transport emissions.
                </p>

                <p class="mb-0 text-muted small">
                    This KPI covers the modeled supply transport
                    activities. It is not the complete product
                    life-cycle carbon footprint.
                </p>
            `,

            productRenewableMaterial: `
                <h6><strong>Understanding Product Renewable Material</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    The proportion of the selected product's
                    total material input mass classified
                    as renewable.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Renewable Material (%) =
                    Total renewable material input (kg) /
                    Total material input (kg) × 100.
                </p>

                <p>
                    <strong>Unit:</strong> %.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    The chart displays renewable and
                    non-renewable portions of the modeled
                    material inputs.
                </p>

                <p class="mb-0 text-muted small">
                    Renewable origin does not automatically
                    imply that a material is recycled,
                    biodegradable, or impact-free.
                </p>
            `,

            productRecycledMaterial: `
                <h6><strong>Understanding Product Recycled Material</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    The proportion of the selected product's
                    total material input mass classified
                    as recycled.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Recycled Material (%) =
                    Total recycled material input (kg) /
                    Total material input (kg) × 100.
                </p>

                <p>
                    <strong>Unit:</strong> %.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    The chart displays recycled and
                    non-recycled portions of the modeled
                    material inputs.
                </p>

                <p class="mb-0 text-muted small">
                    Recycled content refers to recycled
                    material entering production. It does not
                    measure the recyclability of the finished
                    product at end of life.
                </p>
            `
        };

        explanation.innerHTML = explanations[kpiType] || "";
        explanation.style.display =
                explanations[kpiType] ? "block" : "none";
    }

    // END PHASE E2 — PRODUCT KPI EXPLANATIONS



    function clearProductKpiResults() {

        // Destroy the previous Product KPI chart
        if (window.productChartInstance) {
            window.productChartInstance.destroy();
            window.productChartInstance = null;
        }

        // Clear previous Product KPI results
        const results =
                document.getElementById("productKpiResults");

        if (results) {
            results.innerHTML = "";
        }

        // Clear previous Product KPI explanation
        const explanation =
                document.getElementById("productKpiExplanation");

        if (explanation) {
            explanation.innerHTML = "";
            explanation.style.display = "none";
        }

        const benchmarkPanel = document.getElementById(
                "productMaterialEfficiencyBenchmarkResults");

        if (benchmarkPanel) {
            benchmarkPanel.replaceChildren();
            benchmarkPanel.style.display = "none";
        }


        const transportBenchmarkPanel = document.getElementById(
                "productTransportCarbonBenchmarkResults");

        if (transportBenchmarkPanel) {
            transportBenchmarkPanel.replaceChildren();
            transportBenchmarkPanel.style.display = "none";
        }

        const renewableBenchmarkPanel = document.getElementById(
                "productRenewableMaterialBenchmarkResults");

        if (renewableBenchmarkPanel) {
            renewableBenchmarkPanel.replaceChildren();
            renewableBenchmarkPanel.style.display = "none";
        }

        const recycledBenchmarkPanel = document.getElementById(
                "productRecycledMaterialBenchmarkResults");

        if (recycledBenchmarkPanel) {
            recycledBenchmarkPanel.replaceChildren();
            recycledBenchmarkPanel.style.display = "none";
        }

    }


    // ==========================================
    // WATER & WASTE KPI BENCHMARKING
    // ==========================================

    const waterWasteBenchmarkConfig = {
        corporateWaterKpis: [
            {
                title: "Corporate Water Use",
                field: "waterUsePerKg",
                assessment: "corporateWaterUseBenchmarkAssessment",
                action: "saveCorporateWaterUseEvaluation",
                unit: "m3/kg"
            },
            {
                title: "Corporate Wastewater",
                field: "wastewaterPerKg",
                assessment: "corporateWastewaterBenchmarkAssessment",
                action: "saveCorporateWastewaterEvaluation",
                unit: "m3/kg"
            }
        ],
        productWaterKpis: [
            {
                title: "Product Water Use",
                field: "allocatedWaterUsageM3",
                assessment: "productWaterUseBenchmarkAssessment",
                action: "saveProductWaterUseEvaluation",
                unit: "m3/product"
            },
            {
                title: "Product Wastewater",
                field: "allocatedWastewaterM3",
                assessment: "productWastewaterBenchmarkAssessment",
                action: "saveProductWastewaterEvaluation",
                unit: "m3/product"
            }
        ],
        corporateWasteKpis: [
            {
                title: "Corporate Hazardous Waste",
                field: "hazardousWastePerKg",
                assessment: "corporateHazardousWasteBenchmarkAssessment",
                action: "saveCorporateHazardousWasteEvaluation",
                unit: "kg/kg"
            },
            {
                title: "Corporate Non-Hazardous Waste",
                field: "nonHazardousWastePerKg",
                assessment: "corporateNonHazardousWasteBenchmarkAssessment",
                action: "saveCorporateNonHazardousWasteEvaluation",
                unit: "kg/kg"
            }
        ],
        productWasteKpis: [
            {
                title: "Product Hazardous Waste",
                field: "allocatedHazardousWasteKg",
                assessment: "productHazardousWasteBenchmarkAssessment",
                action: "saveProductHazardousWasteEvaluation",
                unit: "kg/product"
            },
            {
                title: "Product Non-Hazardous Waste",
                field: "allocatedNonHazardousWasteKg",
                assessment: "productNonHazardousWasteBenchmarkAssessment",
                action: "saveProductNonHazardousWasteEvaluation",
                unit: "kg/product"
            }
        ]
    };


    function clearWaterWasteBenchmarks(category) {

        const ids = category === "water"
                ? [
                    "corporateWaterUseBenchmarkResults",
                    "corporateWastewaterBenchmarkResults",
                    "productWaterUseBenchmarkResults",
                    "productWastewaterBenchmarkResults"
                ]
                : [
                    "corporateHazardousWasteBenchmarkResults",
                    "corporateNonHazardousWasteBenchmarkResults",
                    "productHazardousWasteBenchmarkResults",
                    "productNonHazardousWasteBenchmarkResults"
                ];

        ids.forEach(id => {
            const panel = document.getElementById(id);

            if (panel) {
                panel.replaceChildren();
                panel.style.display = "none";
            }
        });
    }


    function renderWaterWasteBenchmarks(kpiType, data, individual,
            processSustainability) {


        const category = kpiType.toLowerCase().includes("water")
                ? "water"
                : "waste";

        clearWaterWasteBenchmarks(category);

        const panelIds = {
            corporateWaterKpis: [
                "corporateWaterUseBenchmarkResults",
                "corporateWastewaterBenchmarkResults"
            ],
            productWaterKpis: [
                "productWaterUseBenchmarkResults",
                "productWastewaterBenchmarkResults"
            ],
            corporateWasteKpis: [
                "corporateHazardousWasteBenchmarkResults",
                "corporateNonHazardousWasteBenchmarkResults"
            ],
            productWasteKpis: [
                "productHazardousWasteBenchmarkResults",
                "productNonHazardousWasteBenchmarkResults"
            ]
        };

        const configurations = waterWasteBenchmarkConfig[kpiType] || [];
        const targetPanels = panelIds[kpiType] || [];

        configurations.forEach((config, index) => {

            const panel = document.getElementById(targetPanels[index]);
            if (!panel) {
                console.error("Benchmark panel missing:", targetPanels[index]);
                return;
            }

            panel.replaceChildren();
            panel.style.display = "block";
            const benchmark = data[config.assessment];

            const card = document.createElement("div");
            card.className = "card border mb-3";
            const body = document.createElement("div");
            body.className = "card-body";
            const heading = document.createElement("h5");
            heading.textContent = config.title + " — Benchmark";
            body.appendChild(heading);
            const actual = Number(data[config.field]);
            const validActual =
                    data[config.field] != null &&
                    Number.isFinite(actual);
            const validBenchmark =
                    benchmark &&
                    benchmark.status === "success" &&
                    typeof benchmark.meetsBenchmark === "boolean" &&
                    Number.isFinite(Number(benchmark.benchmarkValue));
            const table = document.createElement("table");
            table.className = "table table-bordered table-sm mt-3";
            const tbody = document.createElement("tbody");
            function addRow(label, value) {
                const tr = document.createElement("tr");
                const th = document.createElement("th");
                th.textContent = label;
                const td = document.createElement("td");
                td.textContent = String(value);
                tr.appendChild(th);
                tr.appendChild(td);
                tbody.appendChild(tr);
            }

            addRow("Calculated KPI",
                    validActual ? actual.toFixed(4) + " " + config.unit
                    : "Unavailable");
            if (validBenchmark) {
                addRow("KPI", benchmark.kpi);
                addRow("Benchmark", benchmark.benchmark);
                addRow("Target",
                        benchmark.comparisonOperator + " " +
                        Number(benchmark.benchmarkValue).toFixed(4) +
                        " " + config.unit);
                addRow("Assessment", benchmark.assessment);
                if (Number.isFinite(
                        Number(benchmark.deviationPercentage))) {
                    addRow("Deviation",
                            Number(benchmark.deviationPercentage)
                            .toFixed(2) + "%");
                }

                if (benchmark.benchmarkSource) {
                    addRow("Source", benchmark.benchmarkSource);
                }
            } else {
                addRow("Benchmark", "Assessment unavailable");
            }

            table.appendChild(tbody);
            body.appendChild(table);
            const status = document.createElement("div");
            status.className = validBenchmark
                    ? (benchmark.meetsBenchmark
                            ? "alert alert-success"
                            : "alert alert-warning")
                    : "alert alert-danger";
            status.textContent = validBenchmark
                    ? (benchmark.meetsBenchmark
                            ? "TARGET_MET"
                            : "TARGET_NOT_MET")
                    : "Benchmark could not be verified.";
            body.appendChild(status);
            const saveButton = document.createElement("button");
            saveButton.type = "button";
            saveButton.className = "btn btn-primary";
            saveButton.textContent =
                    "Save KPI Evaluation & Benchmark Assessment";
            saveButton.disabled = !validActual || !validBenchmark;
            const saveMessage = document.createElement("div");
            saveMessage.className = "mt-3";
            saveButton.addEventListener("click", async function () {

                saveButton.disabled = true;
                saveButton.textContent = "Saving...";
                saveMessage.textContent = "";
                const payload = {
                    action: config.action,
                    individual: individual
                };
                if (kpiType.startsWith("product")) {
                    payload.processSustainability =
                            processSustainability;
                }

                try {
                    const response = await fetch(
                            "ontologyReaderAjax", {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                body: JSON.stringify(payload)
                            }
                    );
                    const result = await response.json();
                    if (!response.ok || result.status !== "success") {
                        throw new Error(
                                result.error ||
                                result.message ||
                                "Failed to save KPI evaluation."
                                );
                    }

                    if (!result.evaluationIndividual ||
                            !result.benchmarkAssessmentIndividual) {
                        throw new Error(
                                "Save response is missing persisted individual IDs."
                                );
                    }

                    saveMessage.className = "alert alert-success mt-3";
                    saveMessage.textContent =
                            "Saved successfully. Evaluation: " +
                            result.evaluationIndividual +
                            " | Assessment: " +
                            result.benchmarkAssessmentIndividual;
                    saveButton.textContent = "Saved";
                } catch (error) {
                    console.error("KPI save failed:", error);
                    saveMessage.className = "alert alert-danger mt-3";
                    saveMessage.textContent = error.message;
                    saveButton.disabled = false;
                    saveButton.textContent =
                            "Retry Saving KPI Evaluation";
                }
            });
            body.appendChild(saveButton);
            body.appendChild(saveMessage);
            card.appendChild(body);
            panel.appendChild(card);
        });
    }

    // END WATER & WASTE KPI BENCHMARKING


    // ==========================================
    // PHASE C5 — WATER KPI DASHBOARD
    // ==========================================


    function clearWaterKpiResults() {

        [
            "waterChartInstance",
            "waterRatioChartInstance",
            "waterComparisonChartInstance"
        ].forEach(name => {
            if (window[name]) {
                window[name].destroy();
                window[name] = null;
            }
        });

        const results = document.getElementById("waterKpiResults");

        if (results) {
            results.innerHTML = "";
        }

        clearWaterWasteBenchmarks("water");
    }



    async function renderWaterKpiChart(data, isProduct) {

        document.getElementById("waterChartTitle").textContent =
                isProduct
                ? "Product Water Volumes"
                : "Corporate Water Intensity";
        const waterColors = isProduct
                ? {
                    primary: "#7c3aed",
                    secondary: "#a78bfa"
                }
        : {
            primary: "#17a2b8",
            secondary: "#087f8c"
        };
        const water = Number(isProduct
                ? data.allocatedWaterUsageM3
                : data.totalWaterUsageM3);

        const wastewater = Number(isProduct
                ? data.allocatedWastewaterM3
                : data.totalWastewaterM3);

        const ratio = water > 0
                ? (wastewater / water) * 100
                : 0;

        // CHART 1 — Horizontal comparison
        window.waterChartInstance = new Chart(
                document.getElementById("waterKpiChart"),
                {
                    type: "bar",
                    data: {
                        labels: ["Water demand", "Wastewater"],
                        datasets: [{
                                data: isProduct
                                        ? [water * 1000, wastewater * 1000]
                                        : [
                                            Number(data.waterUsePerKg) * 1000,
                                            Number(data.wastewaterPerKg) * 1000
                                        ],
                                backgroundColor: [
                                    waterColors.primary,
                                    waterColors.secondary
                                ]
                            }]
                    },
                    options: {
                        indexAxis: "y",
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {display: false}
                        },
                        scales: {
                            x: {
                                beginAtZero: true,
                                title: {
                                    display: true,
                                    text: isProduct
                                            ? "Liters per product"
                                            : "Liters per kg production"
                                }
                            }
                        }
                    }
                }
        );

        // CHART 2 — Wastewater-to-demand volume ratio
        // This is a volume ratio, not a verified water balance.
        const ratioCanvas = document.getElementById("waterRatioChart");

        if (water > 0 && wastewater <= water) {

            window.waterRatioChartInstance = new Chart(
                    ratioCanvas,
                    {
                        type: "doughnut",
                        data: {
                            labels: [
                                "Wastewater generated",
                                "Difference in volumes"
                            ],
                            datasets: [{
                                    data: [
                                        wastewater,
                                        water - wastewater
                                    ],
                                    backgroundColor: [
                                        waterColors.primary,
                                        "#dee2e6"
                                    ]
                                }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            cutout: "65%",
                            plugins: {
                                legend: {
                                    position: "bottom",
                                    labels: {
                                        boxWidth: 10,
                                        font: {size: 10}
                                    }
                                },
                                tooltip: {
                                    callbacks: {
                                        label: context =>
                                            context.label + ": " +
                                                    Number(context.raw).toFixed(3) + " m³"
                                    }
                                }
                            }
                        }
                    }
            );

        } else {

            // The ratio cannot be represented as a part-to-whole
            // doughnut when demand is zero or wastewater exceeds demand.
            const context = ratioCanvas.getContext("2d");
            context.clearRect(
                    0, 0, ratioCanvas.width, ratioCanvas.height
                    );
        }

        // CHART 3 — Corporate versus allocated product
        // Obtain both scopes from their respective endpoints.
        const processName =
                "ProcessSustainability_WaterPrototype_001";

        const selectedProduct = productSelect.value;

        const corporateUrl =
                "ontologyReaderAjax?type=corporateWaterKpis"
                + "&individual=" + encodeURIComponent(processName);

        const corporateResponse = await fetch(corporateUrl);

        const corporate = await corporateResponse.json();

        if (!corporateResponse.ok || corporate.error) {
            throw new Error(
                    corporate.error || "Cannot load corporate water values."
                    );
        }

        let product = null;

        if (selectedProduct) {

            const productUrl =
                    "ontologyReaderAjax?type=productWaterKpis"
                    + "&individual=" + encodeURIComponent(selectedProduct)
                    + "&processSustainability="
                    + encodeURIComponent(processName);

            const productResponse = await fetch(productUrl);
            product = await productResponse.json();

            if (!productResponse.ok || product.error) {
                throw new Error(
                        product.error || "Cannot load product water values."
                        );
            }
        }

        const corporateValues = [
            Number(corporate.totalWaterUsageM3),
            Number(corporate.totalWastewaterM3)
        ];

        const datasets = [{
                label: "Corporate",
                data: corporateValues,
                backgroundColor: "#17a2b8"
            }];

        if (product) {
            datasets.push({
                label: "Product (allocated)",
                data: [
                    Number(product.allocatedWaterUsageM3),
                    Number(product.allocatedWastewaterM3)
                ],
                backgroundColor: "#7c3aed"
            });
        }

        window.waterComparisonChartInstance = new Chart(
                document.getElementById("waterComparisonChart"),
                {
                    type: "bar",
                    data: {
                        labels: ["Water", "Wastewater"],
                        datasets: datasets
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: "bottom",
                                labels: {
                                    boxWidth: 10,
                                    font: {size: 10}
                                }
                            }
                        },
                        scales: {
                            y: {
                                beginAtZero: true,
                                title: {
                                    display: true,
                                    text: "m³"
                                }
                            }
                        }
                    }
                }
        );
    }



    function renderWaterKpiExplanation(data, isProduct) {

        const explanation =
                document.getElementById("waterKpiExplanation");

        if (!explanation) {
            return;
        }

        const waterM3 = Number(isProduct
                ? data.allocatedWaterUsageM3
                : data.totalWaterUsageM3);

        const wastewaterM3 = Number(isProduct
                ? data.allocatedWastewaterM3
                : data.totalWastewaterM3);

        const ratio = waterM3 > 0
                ? (wastewaterM3 / waterM3) * 100
                : null;

        const ratioText = ratio === null
                ? "Not available because water demand is zero."
                : ratio.toFixed(1) + "%";

        explanation.style.display = "block";

        if (isProduct) {

            explanation.innerHTML = `
            <h6><strong>Understanding Product Water KPIs</strong></h6>

            <p>
                <strong>Chart 1 — Product Water Volumes:</strong>
                Shows the water demand and wastewater generated
                allocated to the selected manufactured product.
                The values are expressed in liters per product.
            </p>

            <p>
                <strong>Calculation:</strong><br>
                Product water demand =
                (Corporate water demand / Production output)
                × Product weight.
            </p>

            <p>
                <strong>Chart 2 — Wastewater / Water Demand:</strong>
                Shows the wastewater-generated-to-water-demand
                volume ratio (${ratioText}).
                This is not a measured treatment or recovery rate.
            </p>

            <p>
                <strong>Chart 3 — Corporate vs Product:</strong>
                Compares total corporate process water volumes
                with the mass-allocated product volumes in m³.
            </p>

            <p class="mb-0 text-muted small">
                These product values are mass-allocated prototype
                estimates, not independently measured product
                water consumption.
            </p>
        `;

        } else {

            explanation.innerHTML = `
            <h6><strong>Understanding Corporate Water KPIs</strong></h6>

            <p>
                <strong>Chart 1 — Corporate Water Intensity:</strong>
                Shows the water demand and wastewater generated
                per kilogram of production output.
                The values are expressed in L/kg.
            </p>

            <p>
                <strong>Calculation:</strong><br>
                Water intensity =
                Total water demand / Total production output.
            </p>

            <p>
                <strong>Chart 2 — Wastewater / Water Demand:</strong>
                Shows the wastewater-generated-to-water-demand
                volume ratio (${ratioText}).
                This is not a measured treatment or recovery rate.
            </p>

            <p>
                <strong>Chart 3 — Corporate vs Product:</strong>
                Compares corporate process totals with
                the selected product's mass-allocated volumes
                in m³.
            </p>

            <p class="mb-0 text-muted small">
                Wastewater represents generated wastewater,
                not necessarily treated wastewater.
            </p>
        `;
        }
    }


    function renderWaterKpiResults(data, isProduct) {
        const container = document.getElementById("waterKpiResults");
        container.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent = isProduct
                ? "Product Water KPIs — Mass-Allocated Prototype"
                : "Corporate Water KPIs";

        container.appendChild(heading);

        const summary = document.createElement("p");

        if (isProduct) {
            summary.textContent =
                    "Product: " + data.product
                    + " | Product weight: "
                    + Number(data.productWeightKg).toFixed(2)
                    + " kg | Allocation: "
                    + (Number(data.allocationFraction) * 100).toFixed(2)
                    + "%";
        } else {
            summary.textContent =
                    "Process: " + data.processSustainability
                    + " | Production output: "
                    + Number(data.productionOutputKg).toFixed(2)
                    + " kg";
        }

        container.appendChild(summary);

        const waterM3 = isProduct
                ? Number(data.allocatedWaterUsageM3)
                : Number(data.totalWaterUsageM3);

        const wastewaterM3 = isProduct
                ? Number(data.allocatedWastewaterM3)
                : Number(data.totalWastewaterM3);

        const waterIntensity = isProduct
                ? Number(data.productWaterUsePerKg)
                : Number(data.waterUsePerKg);

        const wastewaterIntensity = isProduct
                ? Number(data.productWastewaterPerKg)
                : Number(data.wastewaterPerKg);

        const rows = [
            ["Water demand", waterM3.toFixed(3), "m³"],
            ["Wastewater generated", wastewaterM3.toFixed(3), "m³"],
            ["Water use per kg", waterIntensity.toFixed(4), "m³/kg"],
            ["Wastewater per kg", wastewaterIntensity.toFixed(4), "m³/kg"],
            ["Water demand", (waterM3 * 1000).toFixed(2), "liters"],
            ["Wastewater generated", (wastewaterM3 * 1000).toFixed(2), "liters"]
        ];

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        ["KPI", "Value", "Unit"].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        rows.forEach(values => {
            const tr = document.createElement("tr");

            values.forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                tr.appendChild(td);
            });

            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        container.appendChild(table);

        const note = document.createElement("p");
        note.className = "text-muted small mt-2";

        note.textContent = isProduct
                ? "Calculation basis: MASS_ALLOCATED_PROTOTYPE. "
                + "Water and wastewater are allocated using product mass "
                + "relative to production output. A direct ontology "
                + "relationship between this product and the water "
                + "process has not been verified."
                : "Calculation boundary: PROCESS_WATER_AND_WASTEWATER. "
                + "Wastewater represents generated wastewater, "
                + "not necessarily treated wastewater.";

        container.appendChild(note);
    }

    document.querySelectorAll(".water-kpi-btn").forEach(button => {

        button.addEventListener("click", function () {

            const kpiType = this.getAttribute("data-kpi");

            const isProduct = kpiType === "productWaterKpis";

            if (!isProduct && kpiType !== "corporateWaterKpis") {
                return;
            }

            const processName =
                    "ProcessSustainability_WaterPrototype_001";

            const selectedProduct = productSelect.value;

            if (isProduct && !selectedProduct) {
                alert("Please select a manufactured product first.");
                return;
            }

            clearWaterKpiResults();

            let url = "ontologyReaderAjax?type="
                    + encodeURIComponent(kpiType);

            if (isProduct) {
                url += "&individual="
                        + encodeURIComponent(selectedProduct)
                        + "&processSustainability="
                        + encodeURIComponent(processName);
            } else {
                url += "&individual="
                        + encodeURIComponent(processName);
            }

            fetch(url)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error(
                                    "Water KPI request failed: HTTP "
                                    + response.status
                                    );
                        }

                        return response.json();
                    })
                    .then(data => {

                        if (data.error) {
                            throw new Error(data.error);
                        }

                        const fields = isProduct
                                ? [
                                    "productWeightKg",
                                    "productionOutputKg",
                                    "allocationFraction",
                                    "allocatedWaterUsageM3",
                                    "allocatedWastewaterM3",
                                    "productWaterUsePerKg",
                                    "productWastewaterPerKg"
                                ]
                                : [
                                    "productionOutputKg",
                                    "totalWaterUsageM3",
                                    "totalWastewaterM3",
                                    "waterUsePerKg",
                                    "wastewaterPerKg"
                                ];

                        fields.forEach(field => {
                            if (data[field] === null
                                    || data[field] === undefined
                                    || !Number.isFinite(
                                            Number(data[field])
                                            )) {
                                throw new Error(
                                        "Invalid Water KPI field: "
                                        + field
                                        );
                            }
                        });

                        if (!(Number(data.productionOutputKg) > 0)
                                || (isProduct
                                        && !(Number(data.productWeightKg) > 0))) {
                            throw new Error(
                                    "Invalid production or product weight."
                                    );
                        }

                        return renderWaterKpiChart(data, isProduct)
                                .then(() => {
                                    renderWaterKpiExplanation(data, isProduct);
                                    renderWaterKpiResults(data, isProduct);

                                    renderWaterWasteBenchmarks(
                                            kpiType,
                                            data,
                                            isProduct ? selectedProduct : processName,
                                            processName
                                            );
                                });
                    })
                    .catch(error => {
                        console.error(
                                "Error fetching Water KPI:",
                                error
                                );

                        alert(error.message);
                    });
        });
    });

    // ==========================================
    // END PHASE C5 — WATER KPI DASHBOARD
    // ==========================================


    // ==========================================
    // PHASE D6 — WASTE KPI DASHBOARD
    // ==========================================

    const wasteProcessName =
            "ProcessSustainability_WastePrototype_001";

    function clearWasteKpiResults() {

        [
            "wasteChartInstance",
            "hazardousWasteChartInstance",
            "nonHazardousWasteChartInstance"
        ].forEach(name => {
            if (window[name]) {
                window[name].destroy();
                window[name] = null;
            }
        });

        const results = document.getElementById("wasteKpiResults");
        if (results) {
            results.innerHTML = "";
        }

        const explanation =
                document.getElementById("wasteKpiExplanation");

        if (explanation) {
            explanation.innerHTML = "";
            explanation.style.display = "none";
        }

        clearWaterWasteBenchmarks("waste");
    }

    function renderWasteKpiCharts(data, isProduct) {

        const hazardous = Number(isProduct
                ? data.allocatedHazardousWasteKg
                : data.hazardousWastePerKg);

        const nonHazardous = Number(isProduct
                ? data.allocatedNonHazardousWasteKg
                : data.nonHazardousWastePerKg);

        document.getElementById("wasteChartTitle").textContent =
                isProduct
                ? "Product Allocated Waste (kg)"
                : "Corporate Waste Intensity (kg/kg)";

        // CHART 1 — Waste generation
        window.wasteChartInstance = new Chart(
                document.getElementById("wasteKpiChart"),
                {
                    type: "bar",
                    data: {
                        labels: [
                            "Hazardous",
                            "Non-hazardous"
                        ],
                        datasets: [{
                                label: isProduct
                                        ? "Allocated waste (kg/product)"
                                        : "Waste intensity (kg/kg)",
                                data: [hazardous, nonHazardous],
                                backgroundColor: [
                                    "#dc3545",
                                    "#17a2b8"
                                ]
                            }]
                    },
                    options: {
                        indexAxis: "y",
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {display: false}
                        },
                        scales: {
                            x: {
                                beginAtZero: true,
                                title: {
                                    display: true,
                                    text: isProduct
                                            ? "kg per selected product"
                                            : "kg waste / kg production"
                                }
                            }
                        }
                    }
                }
        );

        const treatmentLabels = [
            "Internal recycling",
            "Landfill / incineration",
            "External recycling"
        ];

        const treatmentColors = [
            "#28a745",
            "#fd7e14",
            "#007bff"
        ];

        const treatmentOptions = {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "60%",
            plugins: {
                legend: {
                    position: "bottom",
                    labels: {
                        boxWidth: 10,
                        font: {size: 10}
                    }
                },
                tooltip: {
                    callbacks: {
                        label: context =>
                            context.label + ": "
                                    + Number(context.raw).toFixed(1) + "%"
                    }
                }
            }
        };

        // CHART 2 — Hazardous waste treatment
        window.hazardousWasteChartInstance = new Chart(
                document.getElementById("hazardousWasteChart"),
                {
                    type: "doughnut",
                    data: {
                        labels: treatmentLabels,
                        datasets: [{
                                data: [
                                    Number(data.hazardousInternalRecyclingPercent),
                                    Number(data.hazardousLandfillIncinerationPercent),
                                    Number(data.hazardousExternalRecyclingPercent)
                                ],
                                backgroundColor: treatmentColors
                            }]
                    },
                    options: treatmentOptions
                }
        );

        // CHART 3 — Non-hazardous waste treatment
        window.nonHazardousWasteChartInstance = new Chart(
                document.getElementById("nonHazardousWasteChart"),
                {
                    type: "doughnut",
                    data: {
                        labels: treatmentLabels,
                        datasets: [{
                                data: [
                                    Number(data.nonHazardousInternalRecyclingPercent),
                                    Number(data.nonHazardousLandfillIncinerationPercent),
                                    Number(data.nonHazardousExternalRecyclingPercent)
                                ],
                                backgroundColor: treatmentColors
                            }]
                    },
                    options: treatmentOptions
                }
        );
    }

    function renderWasteKpiExplanation(data, isProduct) {

        const explanation =
                document.getElementById("wasteKpiExplanation");

        if (!explanation) {
            return;
        }

        explanation.style.display = "block";

        if (isProduct) {

            const allocationPercent =
                    (Number(data.allocationFraction) * 100).toFixed(1);

            explanation.innerHTML = `
                <h6><strong>Understanding Product Waste KPIs</strong></h6>

                <p>
                    <strong>Chart 1 — Product Allocated Waste:</strong>
                    Shows hazardous and non-hazardous waste
                    attributed to the selected manufactured product,
                    expressed in kg per selected product.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Allocation fraction =
                    Product weight / Production output.<br>
                    Allocated waste =
                    Corporate waste generated × Allocation fraction.
                </p>

                <p>
                    For this product, the allocation fraction is
                    <strong>${allocationPercent}%</strong>.
                    Hazardous waste intensity is
                    <strong>${Number(data.productHazardousWastePerKg).toFixed(3)}
                    kg/kg</strong>, and non-hazardous waste intensity is
                    <strong>${Number(data.productNonHazardousWastePerKg).toFixed(3)}
                    kg/kg</strong>.
                </p>

                <p>
                    <strong>Chart 2 — Hazardous Waste Treatment:</strong>
                    Shows the process-level percentages of hazardous
                    waste assigned to internal recycling,
                    landfill/incineration, and external recycling.
                </p>

                <p>
                    <strong>Chart 3 — Non-Hazardous Waste Treatment:</strong>
                    Shows the corresponding process-level treatment
                    percentages for non-hazardous waste.
                </p>

                <p class="mb-0 text-muted small">
                    These product quantities are mass-allocated
                    prototype estimates, not independently measured
                    product-specific waste quantities.
                    Treatment percentages are stored process-level
                    observations carried into the product model
                    under uniform mass allocation. They are not
                    calculated from treatment masses.
                </p>
            `;

        } else {

            explanation.innerHTML = `
                <h6><strong>Understanding Corporate Waste KPIs</strong></h6>

                <p>
                    <strong>Chart 1 — Corporate Waste Intensity:</strong>
                    Compares hazardous and non-hazardous waste
                    generated per kilogram of production output.
                    The unit is kg waste/kg product.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Hazardous waste intensity =
                    Hazardous waste generated (kg) /
                    Production output (kg).<br>
                    Non-hazardous waste intensity =
                    Non-hazardous waste generated (kg) /
                    Production output (kg).
                </p>

                <p>
                    <strong>Chart 2 — Hazardous Waste Treatment:</strong>
                    Shows the percentages assigned to internal
                    recycling, landfill/incineration, and external
                    recycling for hazardous waste.
                </p>

                <p>
                    <strong>Chart 3 — Non-Hazardous Waste Treatment:</strong>
                    Shows the same three treatment routes for
                    non-hazardous waste.
                </p>

                <p class="mb-0 text-muted small">
                    Waste intensities are calculated from generated
                    waste masses and production output.
                    The six treatment percentages are directly
                    stored in the ontology; they are not derived
                    from measured treatment-route masses.
                    The prototype assumes the three treatment
                    routes cover 100% of each waste category.
                </p>
            `;
        }
    }

    function renderWasteKpiResults(data, isProduct) {

        const container = document.getElementById("wasteKpiResults");
        container.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent = isProduct
                ? "Product Waste KPIs — Mass-Allocated Prototype"
                : "Corporate Waste KPIs";

        container.appendChild(heading);

        const summary = document.createElement("p");

        summary.textContent = isProduct
                ? "Product: " + data.product
                + " | Weight: " + Number(data.productWeightKg).toFixed(2)
                + " kg | Allocation: "
                + (Number(data.allocationFraction) * 100).toFixed(2)
                + "%"
                : "Process: " + data.processSustainability
                + " | Production output: "
                + Number(data.productionOutputKg).toFixed(2) + " kg";

        container.appendChild(summary);

        const hazardousKg = Number(isProduct
                ? data.allocatedHazardousWasteKg
                : data.hazardousWasteKg);

        const nonHazardousKg = Number(isProduct
                ? data.allocatedNonHazardousWasteKg
                : data.nonHazardousWasteKg);

        const totalKg = hazardousKg + nonHazardousKg;

        const hazardousIntensity = Number(isProduct
                ? data.productHazardousWastePerKg
                : data.hazardousWastePerKg);

        const nonHazardousIntensity = Number(isProduct
                ? data.productNonHazardousWastePerKg
                : data.nonHazardousWastePerKg);

        const rows = [
            ["Hazardous waste generated",
                hazardousKg.toFixed(2), "kg"],
            ["Non-hazardous waste generated",
                nonHazardousKg.toFixed(2), "kg"],
            ["Total waste generated",
                totalKg.toFixed(2), "kg"],
            ["Hazardous waste per kg product",
                hazardousIntensity.toFixed(4), "kg/kg"],
            ["Non-hazardous waste per kg product",
                nonHazardousIntensity.toFixed(4), "kg/kg"],
            ["Hazardous — internally recycled",
                Number(data.hazardousInternalRecyclingPercent).toFixed(2), "%"],
            ["Hazardous — landfill/incineration",
                Number(data.hazardousLandfillIncinerationPercent).toFixed(2), "%"],
            ["Hazardous — externally recycled",
                Number(data.hazardousExternalRecyclingPercent).toFixed(2), "%"],
            ["Non-hazardous — internally recycled",
                Number(data.nonHazardousInternalRecyclingPercent).toFixed(2), "%"],
            ["Non-hazardous — landfill/incineration",
                Number(data.nonHazardousLandfillIncinerationPercent).toFixed(2), "%"],
            ["Non-hazardous — externally recycled",
                Number(data.nonHazardousExternalRecyclingPercent).toFixed(2), "%"]
        ];

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        ["KPI", "Value", "Unit"].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        rows.forEach(values => {
            const tr = document.createElement("tr");

            values.forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                tr.appendChild(td);
            });

            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        container.appendChild(table);

        const note = document.createElement("p");
        note.className = "text-muted small mt-2";

        note.textContent = isProduct
                ? "Calculation basis: MASS_ALLOCATED_PROTOTYPE. "
                + "Product waste masses are estimated by uniform "
                + "mass allocation. The treatment percentages are "
                + "inherited from the selected process prototype."
                : "Calculation basis: PRODUCTION_OUTPUT_MASS. "
                + "Waste intensities are calculated. Treatment "
                + "percentages are stored ontology observations.";

        container.appendChild(note);
    }

    document.querySelectorAll(".waste-kpi-btn").forEach(button => {

        button.addEventListener("click", async function () {

            const kpiType = this.getAttribute("data-kpi");

            const isProduct = kpiType === "productWasteKpis";

            if (!isProduct && kpiType !== "corporateWasteKpis") {
                return;
            }

            const selectedProduct = productSelect.value;

            if (isProduct && !selectedProduct) {
                alert("Please select a manufactured product first.");
                return;
            }

            clearWasteKpiResults();

            let url = "ontologyReaderAjax?type="
                    + encodeURIComponent(kpiType);

            if (isProduct) {
                url += "&individual="
                        + encodeURIComponent(selectedProduct)
                        + "&processSustainability="
                        + encodeURIComponent(wasteProcessName);
            } else {
                url += "&individual="
                        + encodeURIComponent(wasteProcessName);
            }

            try {
                const response = await fetch(url);
                const data = await response.json();

                if (!response.ok || data.error) {
                    throw new Error(
                            data.error || "Waste KPI request failed: HTTP "
                            + response.status);
                }

                const requiredFields = [
                    "productionOutputKg",
                    "hazardousInternalRecyclingPercent",
                    "hazardousLandfillIncinerationPercent",
                    "hazardousExternalRecyclingPercent",
                    "nonHazardousInternalRecyclingPercent",
                    "nonHazardousLandfillIncinerationPercent",
                    "nonHazardousExternalRecyclingPercent"
                ];

                if (isProduct) {
                    requiredFields.push(
                            "productWeightKg",
                            "allocationFraction",
                            "allocatedHazardousWasteKg",
                            "allocatedNonHazardousWasteKg",
                            "productHazardousWastePerKg",
                            "productNonHazardousWastePerKg"
                            );
                } else {
                    requiredFields.push(
                            "hazardousWasteKg",
                            "nonHazardousWasteKg",
                            "hazardousWastePerKg",
                            "nonHazardousWastePerKg"
                            );
                }

                requiredFields.forEach(field => {
                    if (data[field] === null
                            || data[field] === undefined
                            || !Number.isFinite(Number(data[field]))) {
                        throw new Error(
                                "Invalid Waste KPI field: " + field);
                    }
                });

                if (!(Number(data.productionOutputKg) > 0)
                        || (isProduct
                                && !(Number(data.productWeightKg) > 0))) {
                    throw new Error(
                            "Invalid production output or product weight.");
                }

                const percentKeys = [
                    "hazardousInternalRecyclingPercent",
                    "hazardousLandfillIncinerationPercent",
                    "hazardousExternalRecyclingPercent",
                    "nonHazardousInternalRecyclingPercent",
                    "nonHazardousLandfillIncinerationPercent",
                    "nonHazardousExternalRecyclingPercent"
                ];

                percentKeys.forEach(key => {
                    const value = Number(data[key]);
                    if (value < 0 || value > 100) {
                        throw new Error(
                                "Waste treatment percentage out of range: "
                                + key);
                    }
                });

                renderWasteKpiCharts(data, isProduct);
                renderWasteKpiExplanation(data, isProduct);
                renderWasteKpiResults(data, isProduct);

                renderWaterWasteBenchmarks(
                        kpiType,
                        data,
                        isProduct ? selectedProduct : wasteProcessName,
                        wasteProcessName
                        );

            } catch (error) {
                clearWasteKpiResults();
                console.error("Error fetching Waste KPI:", error);
                alert(error.message);
            }
        });
    });

    // ==========================================
    // END PHASE D6 — WASTE KPI DASHBOARD
    // ==========================================



    // ==========================================
    // PHASE E3 — ENERGY KPI EXPLANATIONS
    // ==========================================

    function renderEnergyKpiExplanation(kpiType, data) {

        const explanation =
                document.getElementById("energyKpiExplanation");

        if (!explanation) {
            return;
        }

        const explanations = {

            corporateEnergyCarbonFootprint: `
                <h6><strong>Understanding Corporate Energy Carbon Footprint</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    Greenhouse gas emissions associated with
                    energy sources used by the modeled
                    manufacturing process.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Energy carbon footprint =
                    Sum of energy-source carbon emissions.<br>
                    Energy carbon intensity =
                    Total energy carbon footprint /
                    Production output.
                </p>

                <p>
                    <strong>Units:</strong>
                    kg CO₂eq for total emissions;
                    kg CO₂eq/kg for carbon intensity.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    Each bar represents the carbon intensity
                    of an energy source, such as electricity,
                    gasoil, or natural gas.
                    The contributions can be compared to identify
                    the larger modeled emission sources.
                </p>

                <p class="mb-0 text-muted small">
                    The result covers energy-related emissions
                    included in the process model. It should not
                    be interpreted as the company's complete
                    greenhouse gas inventory.
                </p>
            `,

            corporateRenewableElectricity: `
                <h6><strong>Understanding Corporate Renewable Electricity</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    The percentage of electricity consumption
                    classified as renewable within the modeled
                    manufacturing process.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Renewable Electricity (%) =
                    Renewable electricity (kWh) /
                    Total electricity (kWh) × 100.
                </p>

                <p>
                    <strong>Units:</strong>
                    kWh for electricity consumption and
                    % for the renewable share.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    The doughnut chart compares renewable
                    electricity consumption with the remaining
                    non-renewable electricity consumption.
                </p>

                <p>
                    <strong>Current renewable share:</strong>
                    ${Number(data.renewableElectricityPercentage).toFixed(2)}%.
                </p>

                <p class="mb-0 text-muted small">
                    Renewable electricity share describes the
                    modeled electricity mix. It does not measure
                    the renewable share of all energy carriers,
                    including fuels.
                </p>
            `,

            productEnergyCarbonFootprint: `
                <h6><strong>Understanding Product Energy Carbon Footprint</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    Energy-related carbon emissions attributed
                    to the selected manufactured product.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Product energy carbon intensity =
                    Product-allocated energy carbon emissions /
                    Product weight.
                </p>

                <p>
                    <strong>Units:</strong>
                    kg CO₂eq per selected product and
                    kg CO₂eq/kg product.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    Each bar shows the product's carbon intensity
                    contribution from an individual energy source.
                    Larger bars indicate greater modeled
                    energy-related carbon contributions.
                </p>

                <p class="mb-0 text-muted small">
                    Product emissions are based on the allocation
                    assumptions of the energy prototype.
                    They are not necessarily independently
                    measured for the selected product.
                </p>
            `,

            productEmbodiedCarbon: `
                <h6><strong>Understanding Product Embodied Carbon</strong></h6>

                <p>
                    <strong>What it measures:</strong>
                    The embodied carbon value attributed to
                    the selected product within the boundary
                    represented by the current energy model.
                </p>

                <p>
                    <strong>Calculation:</strong><br>
                    Embodied carbon intensity =
                    Product embodied carbon (kg CO₂eq) /
                    Product weight (kg).
                </p>

                <p>
                    <strong>Units:</strong>
                    kg CO₂eq per selected product and
                    kg CO₂eq/kg product.
                </p>

                <p>
                    <strong>Chart interpretation:</strong>
                    The doughnut chart shows how the modeled
                    embodied carbon is distributed across
                    energy sources.
                </p>

                <p class="mb-0 text-muted small">
                    In this prototype, the chart represents
                    energy-source carbon contributions.
                    Do not interpret this as a verified
                    cradle-to-gate embodied carbon footprint
                    unless upstream material production,
                    transport, and other relevant stages
                    are explicitly included in the calculation.
                </p>
            `
        };

        explanation.innerHTML = explanations[kpiType] || "";
        explanation.style.display =
                explanations[kpiType] ? "block" : "none";
    }

    // END PHASE E3 — ENERGY KPI EXPLANATIONS


    // ==========================================
    // ENERGY KPI BENCHMARKING AND PERSISTENCE
    // ==========================================
    const energyBenchmarkConfig = {
        corporateEnergyCarbonFootprint: {
            panel: "corporateEnergyCarbonBenchmarkResults",
            title: "Corporate Energy Carbon Intensity",
            benchmark: "CorporateEnergyCarbonBenchmark",
            kpi: "CorporateEnergyCarbonKPI",
            field: "totalEnergyCarbonPerKg",
            unit: "kg CO₂eq/kg",
            action: "saveCorporateEnergyCarbonIntensityEvaluation",
            product: false
        },
        corporateRenewableElectricity: {
            panel: "corporateRenewableElectricityBenchmarkResults",
            title: "Corporate Renewable Electricity Share",
            benchmark: "CorporateRenewableElectricityBenchmark",
            kpi: "CorporateRenewableElectricityKPI",
            field: "renewableElectricityPercentage",
            unit: "%",
            action: "saveCorporateRenewableElectricityEvaluation",
            product: false
        },
        productEnergyCarbonFootprint: {
            panel: "productEnergyCarbonBenchmarkResults",
            title: "Product Energy Carbon Footprint",
            benchmark: "ProductEnergyCarbonBenchmark",
            kpi: "ProductEnergyCarbonKPI",
            field: "totalAllocatedCarbonKg",
            unit: "kg CO₂eq/product",
            action: "saveProductEnergyCarbonFootprintEvaluation",
            product: true
        },
        productEmbodiedCarbon: {
            panel: "productEmbodiedCarbonBenchmarkResults",
            title: "Product Energy-Related Embodied Carbon Intensity",
            benchmark: "ProductEmbodiedCarbonBenchmark",
            kpi: "ProductEmbodiedCarbonKPI",
            field: "embodiedCarbonPerKg",
            unit: "kg CO₂eq/kg product",
            action: "saveProductEmbodiedCarbonEvaluation",
            product: true
        }
    };

    function renderEnergyBenchmark(kpiType, data, productName, processName) {
        const config = energyBenchmarkConfig[kpiType];
        if (!config)
            return;

        const panel = document.getElementById(config.panel);
        if (!panel) {
            console.error("Energy benchmark container not found: " + config.panel);
            return;
        }
        panel.replaceChildren();
        panel.style.display = "block";

        const title = document.createElement("h5");
        title.className = "font-weight-bold mt-3";
        title.textContent = config.title + " — Benchmarking";
        panel.appendChild(title);

        const actual = Number(data[config.field]);
        if (data[config.field] == null || !Number.isFinite(actual)) {
            const warning = document.createElement("div");
            warning.className = "alert alert-danger";
            warning.textContent = "Invalid KPI value; saving is disabled.";
            panel.appendChild(warning);
            return;
        }

        // Some GET actions provide benchmarkAssessment; others return only
        // calculation data. The POST endpoint performs authoritative benchmarking.
        const benchmark = data.benchmarkAssessment ||
                data.productBenchmarkAssessment ||
                data.energyBenchmarkAssessment;
        const validBenchmark = benchmark &&
                benchmark.status === "success" &&
                (benchmark.benchmark == null ||
                        benchmark.benchmark === config.benchmark) &&
                Number.isFinite(Number(benchmark.benchmarkValue)) &&
                typeof benchmark.meetsBenchmark === "boolean";

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";
        const tbody = document.createElement("tbody");
        const rows = [
            ["Calculated KPI", actual.toFixed(4) + " " + config.unit],
            ["KPI individual", config.kpi],
            ["Benchmark individual", config.benchmark]
        ];

        if (validBenchmark) {
            rows.push(
                    ["Benchmark target", (benchmark.comparisonOperator || "—") +
                                " " + Number(benchmark.benchmarkValue).toFixed(4) + " " + config.unit],
                    ["Assessment", benchmark.meetsBenchmark ? "TARGET_MET" : "TARGET_NOT_MET"]
                    );
            if (benchmark.absoluteDeviation != null &&
                    Number.isFinite(Number(benchmark.absoluteDeviation))) {
                rows.push(["Absolute deviation",
                    Number(benchmark.absoluteDeviation).toFixed(4) + " " + config.unit]);
            }
        } else {
            rows.push(["Benchmark assessment",
                "Will be confirmed by the server when the evaluation is saved."]);
        }

        rows.forEach(([label, value]) => {
            const tr = document.createElement("tr");
            const th = document.createElement("th");
            th.textContent = label;
            const td = document.createElement("td");
            td.textContent = value;
            tr.append(th, td);
            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        panel.appendChild(table);

        if (validBenchmark) {
            const status = document.createElement("div");
            status.className = benchmark.meetsBenchmark
                    ? "alert alert-success" : "alert alert-danger";
            status.textContent = benchmark.meetsBenchmark
                    ? "Demonstration benchmark target met."
                    : "Demonstration benchmark target not met.";
            panel.appendChild(status);
        }

        const note = document.createElement("p");
        note.className = "text-muted small";
        note.textContent = "Benchmark targets are ontology demonstration values, " +
                "not independently verified industry standards.";
        panel.appendChild(note);

        const save = document.createElement("button");
        save.type = "button";
        save.className = "btn btn-success mt-2";
        save.textContent = "Save KPI Evaluation & Benchmark Assessment";
        panel.appendChild(save);

        const feedback = document.createElement("div");
        feedback.className = "mt-3";
        feedback.setAttribute("role", "status");
        panel.appendChild(feedback);

        save.addEventListener("click", async () => {
            save.disabled = true;
            feedback.className = "alert alert-info mt-3";
            feedback.textContent = "Saving evaluation and assessment to ontology...";

            const payload = {
                action: config.action,
                individual: config.product ? productName : processName
            };

            if (config.product) {
                payload.processSustainability = processName;
            }

            try {
                const response = await fetch("ontologyReaderAjax", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(payload)
                });

                const result = await response.json();

                if (!response.ok || result.status !== "success" ||
                        !result.evaluationIndividual ||
                        !result.benchmarkAssessmentIndividual) {
                    throw new Error(result.message || result.error ||
                            "The server did not confirm both ontology individuals.");
                }

                feedback.className = "alert alert-success mt-3";
                feedback.replaceChildren();

                const heading = document.createElement("strong");
                heading.textContent = "Evaluation and benchmark assessment saved.";
                feedback.appendChild(heading);

                const saved = document.createElement("p");
                saved.className = "mb-1 mt-2";
                saved.textContent = "KPIEvaluation: " + result.evaluationIndividual +
                        " | BenchmarkAssessment: " + result.benchmarkAssessmentIndividual;
                feedback.appendChild(saved);

                const assessment = result.benchmarkAssessment;
                if (assessment && typeof assessment.meetsBenchmark === "boolean") {
                    const status = document.createElement("p");
                    status.className = "mb-0";
                    status.textContent = "Benchmark result: " +
                            (assessment.meetsBenchmark ? "TARGET_MET" : "TARGET_NOT_MET");
                    feedback.appendChild(status);
                }

                save.textContent = "Saved";

            } catch (error) {
                console.error("Energy KPI save failed:", error);
                feedback.className = "alert alert-danger mt-3";
                feedback.textContent = "Save not confirmed: " + error.message +
                        " Check the ontology before retrying; a partial save may have occurred.";
                save.textContent = "Save not confirmed";

                // Deliberately leave disabled to avoid duplicate ontology writes.
            }
        });
    }


    function clearEnergyKpiResults() {

        Object.values(energyBenchmarkConfig).forEach(config => {
            const panel = document.getElementById(config.panel);
            if (panel) {
                panel.replaceChildren();
                panel.style.display = "none";
            }
        });


        if (window.energyChartInstance) {

            window.energyChartInstance.destroy();
            window.energyChartInstance = null;
        }

        const results =
                document.getElementById("energyKpiResults");

        if (results) {
            results.innerHTML = "";
        }

        const explanation =
                document.getElementById("energyKpiExplanation");

        if (explanation) {
            explanation.innerHTML = "";
            explanation.style.display = "none";
        }
    }




    document.querySelectorAll(".energy-kpi-btn").forEach(button => {

        button.addEventListener("click", function () {

            const kpiType = this.getAttribute("data-kpi");
            const processName = "ProcessSustainability_EnergyPrototype_001";

            const corporateTypes = [
                "corporateEnergyCarbonFootprint",
                "corporateRenewableElectricity"
            ];

            const productTypes = [
                "productEnergyCarbonFootprint",
                "productEmbodiedCarbon"
            ];

            if (!corporateTypes.includes(kpiType)
                    && !productTypes.includes(kpiType)) {
                return;
            }

            const selectedProduct = productSelect.value;

            if (productTypes.includes(kpiType) && !selectedProduct) {
                alert("Please select a manufactured product first.");
                return;
            }

            clearEnergyKpiResults();

            let url;

            if (corporateTypes.includes(kpiType)) {

                url = "ontologyReaderAjax"
                        + "?type=corporateEnergyCarbonFootprint"
                        + "&individual=" + encodeURIComponent(processName);

            } else {

                url = "ontologyReaderAjax"
                        + "?type=" + encodeURIComponent(kpiType)
                        + "&individual=" + encodeURIComponent(selectedProduct)
                        + "&processSustainability="
                        + encodeURIComponent(processName);
            }

            fetch(url)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("HTTP error: " + response.status);
                        }
                        return response.json();
                    })
                    .then(data => {

                        if (data.error) {
                            throw new Error(data.error);
                        }

                        if (!Array.isArray(data.energySources)
                                || data.energySources.length === 0) {
                            throw new Error("Missing energy source breakdown.");
                        }

                        if (kpiType === "corporateEnergyCarbonFootprint") {

                            if (!Number.isFinite(Number(data.totalEnergyCarbonKg))
                                    || !Number.isFinite(Number(data.totalEnergyCarbonPerKg))
                                    || !(Number(data.productionOutputKg) > 0)) {
                                throw new Error("Invalid Corporate Energy data.");
                            }

                            renderCorporateEnergyCarbonChart(data);
                            renderCorporateEnergyCarbonResults(data);

                        } else if (kpiType === "corporateRenewableElectricity") {

                            const total = Number(data.totalElectricityKWh);
                            const renewable = Number(data.renewableElectricityKWh);
                            const percentage = Number(data.renewableElectricityPercentage);

                            if (!(total > 0)
                                    || !Number.isFinite(renewable)
                                    || renewable < 0
                                    || renewable > total
                                    || !Number.isFinite(percentage)
                                    || percentage < 0
                                    || percentage > 100) {
                                throw new Error("Invalid renewable electricity data.");
                            }

                            renderCorporateRenewableElectricityChart(data);
                            renderCorporateRenewableElectricityResults(data);

                        } else if (kpiType === "productEnergyCarbonFootprint") {

                            if (!Number.isFinite(Number(data.totalAllocatedCarbonKg))
                                    || !Number.isFinite(Number(data.totalProductCarbonPerKg))
                                    || !(Number(data.productWeightKg) > 0)) {
                                throw new Error("Invalid Product Energy data.");
                            }

                            renderProductEnergyCarbonChart(data);
                            renderProductEnergyCarbonResults(data);

                        } else if (kpiType === "productEmbodiedCarbon") {

                            if (!Number.isFinite(Number(data.embodiedCarbonKg))
                                    || !Number.isFinite(Number(data.embodiedCarbonPerKg))
                                    || !(Number(data.productWeightKg) > 0)) {
                                throw new Error("Invalid Product Embodied Carbon data.");
                            }

                            renderProductEmbodiedCarbonChart(data);
                            renderProductEmbodiedCarbonResults(data);
                        }

                        // Explanation for the successfully rendered Energy KPI
                        renderEnergyKpiExplanation(kpiType, data);

                        renderEnergyBenchmark(
                                kpiType, data, selectedProduct, processName);

                    })
                    .catch(error => {
                        console.error("Error fetching Energy KPI:", error);
                        alert(error.message);
                    });
        });
    });




    productSelect.addEventListener("change", function () {
        clearKpiResults();
        clearProductKpiResults();
        clearEnergyKpiResults();
        clearWaterKpiResults();
        clearWasteKpiResults();

        const explanation =
                document.getElementById("waterKpiExplanation");

        if (explanation) {
            explanation.innerHTML = "";
            explanation.style.display = "none";
        }
    });

    // 1. Populate the product list
    fetch("ontologyReaderAjax?type=individualsByClass&className=ManufacturedProduct")
            .then(response => response.json())
            .then(data => {
                console.log("Fetched Renewable Content data:", data);

                if (data.individuals && Array.isArray(data.individuals)) {
                    productSelect.innerHTML = '<option value="">-- Choose a Manufactured Product --</option>';
                    const seen = new Set();
                    data.individuals.forEach(product => {
                        if (!seen.has(product)) {
                            seen.add(product);
                            const option = document.createElement("option");
                            option.value = product;
                            option.textContent = product;
                            productSelect.appendChild(option);
                        }
                    });
                }
            })
            .catch(error => console.error("Error loading products:", error));


// Handle Product KPI button clicks separately.
    document.querySelectorAll(".product-kpi-btn").forEach(button => {
        button.addEventListener("click", function () {
            const selectedProduct = productSelect.value;
            const kpiType = this.getAttribute("data-kpi");

            clearProductKpiResults();

            if (!selectedProduct) {
                alert("Please select a product first.");
                return;
            }

            if (kpiType === "productMaterialEfficiency") {
                fetch(
                        `ontologyReaderAjax?type=productMaterialEfficiency&individual=${encodeURIComponent(selectedProduct)}`
                        )
                        .then(response => {
                            if (!response.ok) {
                                throw new Error(
                                        `HTTP error: ${response.status}`
                                        );
                            }
                            return response.json();
                        })
                        .then(data => {
                            if (data.error) {
                                throw new Error(data.error);
                            }

                            if (!Array.isArray(data.materialIntensities)
                                    || !Number.isFinite(Number(data.materialEfficiency))) {
                                throw new Error(
                                        "Invalid Product Material Efficiency response."
                                        );
                            }

                            renderProductMaterialEfficiencyChart(data);
                            renderProductMaterialEfficiencyResults(data);
                            renderProductKpiExplanation("productMaterialEfficiency", data);

                            renderProductMaterialEfficiencyChart(data);
                            renderProductMaterialEfficiencyResults(data);
                            renderProductKpiExplanation("productMaterialEfficiency", data);

// NEW: display product-level benchmark and Save button.
                            renderProductMaterialEfficiencyBenchmark(data);
                        })
                        .catch(error => {
                            console.error(
                                    "Error fetching Product Material Efficiency:",
                                    error
                                    );
                            alert(error.message);
                        });
            }


            if (kpiType === "productTransportCarbon") {
                fetch(
                        `ontologyReaderAjax?type=supplyTransportCarbonFootprint&individual=${encodeURIComponent(selectedProduct)}`
                        )
                        .then(response => {
                            if (!response.ok) {
                                throw new Error(`HTTP error: ${response.status}`);
                            }
                            return response.json();
                        })
                        .then(data => {
                            if (data.error) {
                                throw new Error(data.error);
                            }

                            if (!Array.isArray(data.materialFootprints)
                                    || !Number.isFinite(Number(data.totalTransportCarbonKg))) {
                                throw new Error("Invalid transport carbon response.");
                            }

                            renderProductTransportCarbonChart(data);
                            renderProductTransportCarbonResults(data);
                            renderProductKpiExplanation("productTransportCarbon", data);

                            renderProductTransportCarbonChart(data);
                            renderProductTransportCarbonResults(data);
                            renderProductKpiExplanation("productTransportCarbon", data);

// NEW: Product-level transport carbon benchmarking and saving
                            renderProductTransportCarbonBenchmark(data);
                        })
                        .catch(error => {
                            console.error("Error fetching Product Transport Carbon:", error);
                            alert(error.message);
                        });
            }


            if (kpiType === "productRenewableMaterial") {
                fetch(
                        `ontologyReaderAjax?type=productRenewableMaterial&individual=${encodeURIComponent(selectedProduct)}`
                        )
                        .then(response => {
                            if (!response.ok) {
                                throw new Error(`HTTP error: ${response.status}`);
                            }
                            return response.json();
                        })
                        .then(data => {
                            if (data.error) {
                                throw new Error(data.error);
                            }

                            if (!Array.isArray(data.renewableContents)
                                    || !Number.isFinite(Number(data.productRenewablePercentage))
                                    || !Number.isFinite(Number(data.totalMaterialInputWeight))
                                    || !Number.isFinite(Number(data.totalRenewableWeight))
                                    || Number(data.totalMaterialInputWeight) <= 0) {
                                throw new Error("Invalid Product Renewable Material response.");
                            }

                            renderProductRenewableMaterialChart(data);
                            renderProductRenewableMaterialResults(data);
                            renderProductKpiExplanation("productRenewableMaterial", data);
                            // NEW: Product Renewable Material benchmarking and saving
                            renderProductRenewableMaterialBenchmark(data);
                        })
                        .catch(error => {
                            console.error("Error fetching Product Renewable Material:", error);
                            alert(error.message);
                        });
            }


            if (kpiType === "productRecycledMaterial") {
                fetch(
                        `ontologyReaderAjax?type=productRecycledMaterial&individual=${encodeURIComponent(selectedProduct)}`
                        )
                        .then(response => {
                            if (!response.ok) {
                                throw new Error(`HTTP error: ${response.status}`);
                            }
                            return response.json();
                        })
                        .then(data => {
                            if (data.error) {
                                throw new Error(data.error);
                            }

                            if (!Array.isArray(data.recycledContents)
                                    || !Number.isFinite(Number(data.productRecycledPercentage))
                                    || !Number.isFinite(Number(data.totalMaterialInputWeight))
                                    || !Number.isFinite(Number(data.totalRecycledWeight))
                                    || Number(data.totalMaterialInputWeight) <= 0) {
                                throw new Error("Invalid Product Recycled Material response.");
                            }

                            renderProductRecycledMaterialChart(data);
                            renderProductRecycledMaterialResults(data);
                            renderProductKpiExplanation("productRecycledMaterial", data);

                            // NEW: Product Recycled Material benchmarking and saving
                            renderProductRecycledMaterialBenchmark(data);
                        })
                        .catch(error => {
                            console.error("Error fetching Product Recycled Material:", error);
                            alert(error.message);
                        });
            }


        });
    });


    // 2. Handle KPI button clicks
    document.querySelectorAll(".kpi-btn").forEach(button => {
        button.addEventListener("click", function () {
            const selectedProduct = productSelect.value;
            const kpiType = this.getAttribute("data-kpi");

            clearKpiResults();



            if (!selectedProduct) {
                alert("Please select a product first.");
                return;
            }
// Display the explanation for the selected Material KPI
            renderMaterialKpiExplanation(kpiType);

            console.log(`KPI button clicked: ${kpiType} for product ${selectedProduct}`);


            if (kpiType === "materialIntensity") {

                fetch(
                        `ontologyReaderAjax?type=materialIntensity&individual=${encodeURIComponent(selectedProduct)}`
                        )
                        .then(async response => {
                            const data = await response.json();

                            if (!response.ok || data.error) {
                                throw new Error(
                                        data.error ||
                                        "Material Intensity GET failed: HTTP " +
                                        response.status
                                        );
                            }

                            return data;
                        })
                        .then(data => {

                            if (!Array.isArray(data.materialIntensities) ||
                                    data.materialIntensities.length === 0) {
                                throw new Error(
                                        "No Material Intensity results returned."
                                        );
                            }

                            const chartData = {};

                            data.materialIntensities.forEach(item => {
                                chartData[item.material] =
                                        Number(item.intensity);
                            });

                            // Retain the existing chart and material table.
                            renderMaterialIntensityChart(chartData);
                            renderMaterialIntensityTable(data);

                            renderMaterialKpiExplanation(
                                    "materialIntensity", data
                                    );

                            // New benchmark comparison and Save button.
                            renderMaterialIntensityBenchmark(data);
                        })
                        .catch(error => {
                            console.error(
                                    "Error fetching Material Intensity:", error
                                    );
                            alert(error.message);
                        });
            }




            if (kpiType === "renewableContent") {

                fetch(
                        `ontologyReaderAjax?type=renewableContent&individual=${encodeURIComponent(selectedProduct)}`
                        )
                        .then(response => {
                            if (!response.ok) {
                                throw new Error(
                                        "Renewable Content request failed: HTTP " + response.status
                                        );
                            }
                            return response.json();
                        })
                        .then(data => {
                            if (data.error || !Array.isArray(data.renewableContents)) {
                                throw new Error(
                                        data.error || "Missing Renewable Content material data."
                                        );
                            }

                            renderRenewableContentChart(data.renewableContents);
                            renderRenewableContentTable(data);

                            renderRenewableContentBenchmark(data);

                            renderMaterialKpiExplanation("renewableContent", data);
                        })
                        .catch(error => {
                            console.error("Error fetching Renewable Content:", error);
                            alert(error.message);
                        });
            }




            if (kpiType === "recycledContent") {
                fetch(
                        `ontologyReaderAjax?type=recycledContent&individual=${encodeURIComponent(selectedProduct)}`
                        )
                        .then(response => {
                            if (!response.ok) {
                                throw new Error(
                                        "Recycled Content request failed: HTTP " +
                                        response.status
                                        );
                            }
                            return response.json();
                        })
                        .then(data => {
                            if (data.error ||
                                    !Array.isArray(data.recycledContents)) {
                                throw new Error(
                                        data.error ||
                                        "Missing Recycled Content material data."
                                        );
                            }

                            renderRecycledContentChart(data.recycledContents);
                            renderRecycledContentTable(data);
                            renderRecycledContentBenchmark(data);
                            renderMaterialKpiExplanation("recycledContent", data);
                        })
                        .catch(error => {
                            console.error(
                                    "Error fetching Recycled Content:",
                                    error
                                    );
                            alert(error.message);
                        });
            }



            if (kpiType === "supplyTransportCarbonFootprint") {

                fetch(
                        `ontologyReaderAjax?type=supplyTransportCarbonFootprint&individual=${encodeURIComponent(selectedProduct)}`
                        )
                        .then(async response => {

                            const data = await response.json();

                            if (!response.ok || data.error) {
                                throw new Error(
                                        data.error ||
                                        "Supply Transport Carbon Footprint GET failed: HTTP " +
                                        response.status
                                        );
                            }

                            return data;
                        })
                        .then(data => {

                            if (!Array.isArray(data.materialFootprints) ||
                                    data.materialFootprints.length === 0) {

                                throw new Error(
                                        "Missing Supply Transport Carbon Footprint material data."
                                        );
                            }

                            // Preserve the existing road/sea chart.
                            renderSupplyTransportCarbonFootprintChart(
                                    data.materialFootprints
                                    );

                            // Preserve the existing transport breakdown table.
                            renderSupplyTransportCarbonFootprintTable(data);

                            // NEW: render benchmark results and save button.
                            renderSupplyTransportCarbonFootprintBenchmark(data);

                        })
                        .catch(error => {

                            console.error(
                                    "Error fetching Supply Transport Carbon Footprint:",
                                    error
                                    );

                            alert(error.message);
                        });
            }



        });
    });

    // 3. Render Bar Chart
    function renderMaterialIntensityChart(data) {
        const labels = Object.keys(data);
        const values = Object.values(data);

        const canvas = document.getElementById("materialKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.materialChartInstance) {
            window.materialChartInstance.destroy();
        }

        window.materialChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                        label: 'Material Intensity',
                        data: values,
                        backgroundColor: '#007bff'
                    }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });


    }


    function renderMaterialIntensityBenchmark(data) {

        const container =
                document.getElementById("materialIntensityBenchmarkResults");

        if (!container) {
            console.error("Missing materialIntensityBenchmarkResults container.");
            return;
        }

        container.replaceChildren();
        container.style.display = "block";

        const materials = data.materialIntensities || [];

        // ==========================================
        // 1. BENCHMARK COMPARISON TABLE
        // ==========================================

        const title = document.createElement("h5");
        title.className = "font-weight-bold mt-3";
        title.textContent = "Material Intensity Benchmarking";
        container.appendChild(title);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Raw material",
            "Actual (kg/kg)",
            "Benchmark (kg/kg)",
            "Assessment",
            "Deviation (kg/kg)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        let validCount = 0;
        let benchmarkSource = "";

        materials.forEach(item => {

            const benchmark = item.benchmarkAssessment;
            const actual = Number(item.intensity);

            const valid = benchmark &&
                    benchmark.status === "success" &&
                    Number.isFinite(actual) &&
                    Number.isFinite(Number(benchmark.benchmarkValue)) &&
                    Number.isFinite(Number(benchmark.absoluteDeviation)) &&
                    typeof benchmark.meetsBenchmark === "boolean";

            if (valid) {
                validCount++;
                benchmarkSource =
                        benchmark.benchmarkSource || benchmarkSource;
            }

            const values = [
                item.material || "Unknown",
                Number.isFinite(actual)
                        ? actual.toFixed(3)
                        : "Unavailable",
                valid
                        ? benchmark.comparisonOperator + " " +
                        Number(benchmark.benchmarkValue).toFixed(3)
                        : "Unavailable",
                valid
                        ? (benchmark.meetsBenchmark
                                ? "TARGET_MET"
                                : "TARGET_NOT_MET")
                        : "Assessment unavailable",
                valid
                        ? Number(benchmark.absoluteDeviation).toFixed(3)
                        : "—"
            ];

            const row = document.createElement("tr");

            values.forEach((value, index) => {

                const td = document.createElement("td");
                td.textContent = value;

                if (index === 3 && valid) {
                    td.className = benchmark.meetsBenchmark
                            ? "text-success font-weight-bold"
                            : "text-danger font-weight-bold";
                }

                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        table.appendChild(tbody);
        container.appendChild(table);

        // ==========================================
        // 2. HORIZONTAL BENCHMARK BARS
        // ==========================================

        const barsTitle = document.createElement("h6");
        barsTitle.className = "font-weight-bold mt-4";
        barsTitle.textContent =
                "Actual Material Intensity vs Maximum Allowed";

        container.appendChild(barsTitle);

        // Use a common scale for all materials.
        // The scale is calculated from the actual values
        // and benchmark values returned by the ontology.

        const maxScale = Math.max(
                1,
                ...materials.map(item => Math.max(
                            Number(item.intensity) || 0,
                            Number(item.benchmarkAssessment?.benchmarkValue) || 0
                            ))
                ) * 1.12;

        materials.forEach(item => {

            const benchmark = item.benchmarkAssessment;

            if (!benchmark || benchmark.status !== "success") {
                return;
            }

            const actual = Number(item.intensity);
            const target = Number(benchmark.benchmarkValue);

            if (!Number.isFinite(actual) ||
                    !Number.isFinite(target) ||
                    actual < 0 ||
                    target < 0) {
                return;
            }

            const block = document.createElement("div");
            block.className = "mb-3";

            const labelRow = document.createElement("div");
            labelRow.className =
                    "d-flex justify-content-between small mb-1";

            const materialName = document.createElement("strong");
            materialName.textContent = item.material;

            const valueLabel = document.createElement("span");
            valueLabel.textContent =
                    actual.toFixed(2) + " kg/kg";

            labelRow.appendChild(materialName);
            labelRow.appendChild(valueLabel);

            block.appendChild(labelRow);

            // Background bar.
            const track = document.createElement("div");

            track.style.cssText =
                    "height:20px;" +
                    "position:relative;" +
                    "background:#e9ecef;" +
                    "border-radius:5px;";

            // Actual Material Intensity.
            const fill = document.createElement("div");

            fill.style.height = "100%";
            fill.style.width =
                    (actual / maxScale * 100) + "%";

            fill.style.backgroundColor =
                    benchmark.meetsBenchmark
                    ? "#28a745"
                    : "#dc3545";

            fill.style.borderRadius = "5px";

            track.appendChild(fill);

            // Vertical benchmark marker.
            const marker = document.createElement("div");

            marker.style.cssText =
                    "position:absolute;" +
                    "top:-4px;" +
                    "height:28px;" +
                    "width:3px;" +
                    "background:#212529;";

            marker.style.left =
                    (target / maxScale * 100) + "%";

            marker.title =
                    "Maximum benchmark: " +
                    target.toFixed(2) + " kg/kg";

            track.appendChild(marker);

            block.appendChild(track);
            container.appendChild(block);
        });

        // ==========================================
        // 3. BENCHMARK SOURCE AND INTERPRETATION
        // ==========================================

        const note = document.createElement("p");
        note.className = "text-muted small mt-3";

        note.textContent =
                "One shared maximum benchmark is applied to each raw material. " +
                "Negative deviation means the material is below the maximum. " +
                "Benchmark source: " +
                (benchmarkSource || "Not available");

        container.appendChild(note);

        // Overall intensity is informational only.
        // It is not saved as an additional per-material evaluation.

        if (data.overallMaterialIntensity !== undefined) {

            const overall = document.createElement("p");
            overall.className = "alert alert-light border";

            overall.textContent =
                    "Overall combined intensity (informational only): " +
                    Number(data.overallMaterialIntensity).toFixed(3) +
                    " kg/kg. This is not saved as a per-material evaluation.";

            container.appendChild(overall);
        }

        // ==========================================
        // 4. SAVE EVALUATIONS AND ASSESSMENTS
        // ==========================================

        const saveButton = document.createElement("button");

        saveButton.type = "button";
        saveButton.className = "btn btn-success mt-2";

        saveButton.textContent =
                "Save Material Intensity Evaluations & Assessments";

        // Only allow saving when all material assessments
        // were successfully calculated.

        saveButton.disabled =
                materials.length === 0 ||
                validCount !== materials.length;

        container.appendChild(saveButton);

        const feedback = document.createElement("div");
        feedback.className = "mt-3";
        feedback.setAttribute("role", "status");

        container.appendChild(feedback);

        saveButton.addEventListener("click", async function () {

            if (saveButton.disabled) {
                return;
            }

            saveButton.disabled = true;

            feedback.className = "alert alert-info mt-3";
            feedback.textContent = "Saving to ontology...";

            try {

                const response = await fetch("ontologyReaderAjax", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        action: "saveMaterialIntensityEvaluation",
                        individual: data.product
                    })
                });

                const result = await response.json();

                if (!response.ok || result.status !== "success") {
                    throw new Error(
                            result.message ||
                            result.error ||
                            "Save failed."
                            );
                }

                // Verify that every material has a saved
                // KPIEvaluation and BenchmarkAssessment.

                if (!Array.isArray(result.materialEvaluations) ||
                        result.materialEvaluations.length !== materials.length ||
                        result.materialEvaluations.some(item =>
                            !item.evaluationIndividual ||
                                    !item.benchmarkAssessmentIndividual)) {

                    throw new Error(
                            "Incomplete save response; " +
                            "check the ontology before retrying."
                            );
                }

                // ==========================================
                // 5. DISPLAY SAVED ONTOLOGY INDIVIDUALS
                // ==========================================

                feedback.className = "alert alert-success mt-3";
                feedback.replaceChildren();

                const summary = document.createElement("p");

                summary.textContent =
                        "Successfully saved " +
                        result.materialEvaluations.length +
                        " KPI evaluations and benchmark assessments.";

                feedback.appendChild(summary);

                const list = document.createElement("ul");

                result.materialEvaluations.forEach(item => {

                    const li = document.createElement("li");

                    li.textContent =
                            item.material + ": " +
                            item.evaluationIndividual +
                            " → " +
                            item.benchmarkAssessmentIndividual;

                    list.appendChild(li);
                });

                feedback.appendChild(list);

                // The Save button remains disabled after success,
                // preventing duplicate writes from another click.

            } catch (error) {

                console.error(
                        "Material Intensity save failed:",
                        error
                        );

                feedback.className = "alert alert-danger mt-3";

                feedback.textContent =
                        "Save not confirmed: " +
                        error.message +
                        " Some records may already have been created; " +
                        "check the ontology before retrying.";
            }
        });
    }


    // 4. Render Data Table

    function renderMaterialIntensityTable(data) {
        let tableContainer =
                document.getElementById("materialIntensityTable");

        if (!tableContainer) {
            tableContainer = document.createElement("div");
            tableContainer.id = "materialIntensityTable";

            const canvas = document.getElementById("materialKpiChart");
            canvas.parentNode.insertBefore(
                    tableContainer, canvas.nextSibling);
        }

        //tableContainer.innerHTML = "";

        // const table = document.createElement("table");

        tableContainer.innerHTML = "";

// ==========================================
// MATERIAL INTENSITY TABLE HEADING
// ==========================================

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent = "Material Intensity by Raw Material";
        tableContainer.appendChild(heading);

        const description = document.createElement("p");
        description.textContent =
                "Material intensity (kg/kg) is calculated separately for each raw material.";
        tableContainer.appendChild(description);

// ==========================================
// EXISTING MATERIAL INTENSITY TABLE
// ==========================================

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Material",
            "Weight per unit (kg)",
            "Total input (kg)",
            "Intensity (kg/kg)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.materialIntensities.forEach(item => {
            const row = document.createElement("tr");

            [
                item.material,
                Number(item.weightPerUnit).toFixed(2),
                Number(item.totalMaterialWeight).toFixed(2),
                Number(item.intensity).toFixed(2)
            ].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });

            tbody.appendChild(row);
        });

        table.appendChild(tbody);
        tableContainer.appendChild(table);
    }



    function renderRenewableContentChart(data) {
        const canvas = document.getElementById("materialKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.materialChartInstance) {
            window.materialChartInstance.destroy();
        }

        const labels = data.map(item => item.material);
        const values = data.map(item =>
            Number(item.renewableFraction) * 100
        );

        window.materialChartInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                        label: "Renewable share of material (%)",
                        data: values,
                        backgroundColor: "#28a745"
                    }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: {
                            display: true,
                            text: "Renewable material (%)"
                        }
                    }
                }
            }
        });
    }



    function renderRenewableContentBenchmark(data) {
        const container =
                document.getElementById("renewableBenchmarkResults");

        if (!container) {
            console.error("Missing renewableBenchmarkResults container.");
            return;
        }

        container.replaceChildren();
        container.style.display = "block";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent = "Renewable Content Benchmarking";
        container.appendChild(heading);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Raw material",
            "Actual",
            "Benchmark",
            "Assessment",
            "Deviation (pp)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");
        const materials = data.renewableContents || [];

        let validCount = 0;
        let benchmarkSource = "";

        materials.forEach(item => {
            const benchmark = item.benchmarkAssessment;
            const actual = Number(item.renewableFraction) * 100;

            const valid = benchmark &&
                    benchmark.status === "success" &&
                    Number.isFinite(actual) &&
                    Number.isFinite(Number(benchmark.benchmarkValue)) &&
                    Number.isFinite(Number(benchmark.absoluteDeviation)) &&
                    typeof benchmark.meetsBenchmark === "boolean";

            const values = valid
                    ? [
                        item.material,
                        actual.toFixed(2) + "%",
                        benchmark.comparisonOperator + " " +
                                Number(benchmark.benchmarkValue).toFixed(2) +
                                (benchmark.benchmarkUnit || "%"),
                        benchmark.meetsBenchmark
                                ? "TARGET_MET"
                                : "TARGET_NOT_MET",
                        Number(benchmark.absoluteDeviation).toFixed(2)
                    ]
                    : [
                        item.material || "Unknown",
                        "Unavailable",
                        "Unavailable",
                        "Assessment unavailable",
                        "—"
                    ];

            const row = document.createElement("tr");

            values.forEach((value, index) => {
                const td = document.createElement("td");
                td.textContent = value;

                if (index === 3 && valid) {
                    td.className = benchmark.meetsBenchmark
                            ? "text-success font-weight-bold"
                            : "text-danger font-weight-bold";
                }

                row.appendChild(td);
            });

            tbody.appendChild(row);

            if (valid) {
                validCount++;
                benchmarkSource =
                        benchmark.benchmarkSource || benchmarkSource;
            }
        });

        table.appendChild(tbody);
        container.appendChild(table);


        // ==========================================
        // RENEWABLE CONTENT BENCHMARK VISUAL BARS
        // ==========================================

        const barSection = document.createElement("div");
        barSection.className = "mt-4 mb-4";

        const barTitle = document.createElement("h6");
        barTitle.className = "font-weight-bold";
        barTitle.textContent = "Actual Renewable Content vs Benchmark";
        barSection.appendChild(barTitle);

        materials.forEach(item => {
            const benchmark = item.benchmarkAssessment;

            if (!benchmark || benchmark.status !== "success") {
                return;
            }

            const actual = Number(item.renewableFraction) * 100;
            const target = Number(benchmark.benchmarkValue);

            if (!Number.isFinite(actual) ||
                    !Number.isFinite(target) ||
                    actual < 0 || actual > 100 ||
                    target < 0 || target > 100) {
                return;
            }

            const meets = benchmark.meetsBenchmark === true;

            const materialBlock = document.createElement("div");
            materialBlock.className = "mb-4";

            // Material name and actual value
            const labelRow = document.createElement("div");
            labelRow.className =
                    "d-flex justify-content-between align-items-center mb-2";

            const materialLabel = document.createElement("strong");
            materialLabel.textContent = item.material;

            const valueLabel = document.createElement("span");
            valueLabel.className = meets
                    ? "text-success font-weight-bold"
                    : "text-danger font-weight-bold";

            valueLabel.textContent =
                    actual.toFixed(1) + "% — " +
                    (meets ? "Target met" : "Target not met");

            labelRow.appendChild(materialLabel);
            labelRow.appendChild(valueLabel);
            materialBlock.appendChild(labelRow);

            // Horizontal bar representing 0–100%
            const track = document.createElement("div");
            track.style.position = "relative";
            track.style.width = "100%";
            track.style.height = "20px";
            track.style.backgroundColor = "#e9ecef";
            track.style.borderRadius = "6px";

            // Actual renewable content
            const fill = document.createElement("div");
            fill.style.width = actual + "%";
            fill.style.height = "100%";
            fill.style.backgroundColor = meets
                    ? "#28a745"
                    : "#dc3545";
            fill.style.borderRadius = "6px";

            track.appendChild(fill);

            // Vertical benchmark marker
            const marker = document.createElement("div");
            marker.style.position = "absolute";
            marker.style.left = target + "%";
            marker.style.top = "-5px";
            marker.style.height = "30px";
            marker.style.width = "3px";
            marker.style.backgroundColor = "#212529";
            marker.style.transform = "translateX(-50%)";

            track.appendChild(marker);
            materialBlock.appendChild(track);

            // Axis labels
            const axis = document.createElement("div");
            axis.className =
                    "d-flex justify-content-between text-muted small mt-1";

            const zero = document.createElement("span");
            zero.textContent = "0%";

            const targetLabel = document.createElement("span");
            targetLabel.textContent =
                    "Benchmark: " + target.toFixed(1) + "%";

            const hundred = document.createElement("span");
            hundred.textContent = "100%";

            axis.appendChild(zero);
            axis.appendChild(targetLabel);
            axis.appendChild(hundred);

            materialBlock.appendChild(axis);
            barSection.appendChild(materialBlock);
        });

        container.appendChild(barSection);


        if (benchmarkSource) {
            const note = document.createElement("p");
            note.className = "text-muted small";
            note.textContent =
                    "Deviation is measured in percentage points. " +
                    "Benchmark source: " + benchmarkSource;
            container.appendChild(note);
        }

        const saveButton = document.createElement("button");
        saveButton.type = "button";
        saveButton.className = "btn btn-success mt-2";
        saveButton.textContent =
                "Save Renewable Content Evaluations & Assessments";

        saveButton.disabled =
                materials.length === 0 ||
                validCount !== materials.length;

        container.appendChild(saveButton);

        const feedback = document.createElement("div");
        feedback.className = "mt-3";
        feedback.setAttribute("role", "status");
        container.appendChild(feedback);

        saveButton.addEventListener("click", async function () {
            if (saveButton.disabled) {
                return;
            }

            saveButton.disabled = true;
            feedback.className = "alert alert-info mt-3";
            feedback.textContent = "Saving to ontology...";

            try {
                const response = await fetch("ontologyReaderAjax", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        action: "saveRenewableContentEvaluation",
                        individual: productSelect.value
                    })
                });

                const result = await response.json();

                if (!response.ok || result.status !== "success") {
                    throw new Error(
                            result.message ||
                            result.error ||
                            "Saving failed."
                            );
                }

                if (!Array.isArray(result.materialEvaluations) ||
                        result.materialEvaluations.length !== materials.length ||
                        result.materialEvaluations.some(item =>
                            !item.evaluationIndividual ||
                                    !item.benchmarkAssessmentIndividual)) {
                    throw new Error(
                            "Incomplete save response. Check the ontology " +
                            "before attempting another save."
                            );
                }

                feedback.className = "alert alert-success mt-3";
                feedback.replaceChildren();

                const summary = document.createElement("p");
                summary.textContent =
                        "Successfully saved " +
                        result.materialEvaluations.length +
                        " KPI evaluations and benchmark assessments.";

                feedback.appendChild(summary);

                const list = document.createElement("ul");

                result.materialEvaluations.forEach(item => {
                    const li = document.createElement("li");
                    li.textContent =
                            item.material + ": " +
                            item.evaluationIndividual +
                            " → " +
                            item.benchmarkAssessmentIndividual;
                    list.appendChild(li);
                });

                feedback.appendChild(list);

                // Do not re-enable the button after a successful save:
                // another click would create duplicate individuals.

            } catch (error) {
                console.error("Renewable Content save failed:", error);

                feedback.className = "alert alert-danger mt-3";
                feedback.textContent =
                        "Save not confirmed: " + error.message +
                        " Some records may already have been created. " +
                        "Check the ontology before retrying.";
            }
        });
    }



    // ==========================================
    // RECYCLED CONTENT BENCHMARKING
    // ==========================================

    function renderRecycledContentBenchmark(data) {

        const container =
                document.getElementById("recycledBenchmarkResults");

        if (!container) {
            console.error("Missing recycledBenchmarkResults container.");
            return;
        }

        container.replaceChildren();
        container.style.display = "block";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent = "Recycled Content Benchmarking";
        container.appendChild(heading);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Raw material",
            "Actual",
            "Benchmark",
            "Assessment",
            "Deviation (pp)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");
        const materials = data.recycledContents || [];

        let validCount = 0;
        let benchmarkSource = "";

        materials.forEach(item => {

            const benchmark = item.benchmarkAssessment;

            // IMPORTANT: Use the material's recycled fraction,
            // not its contribution to total product weight.
            const actual = Number(item.recycledFraction) * 100;

            const valid = benchmark &&
                    benchmark.status === "success" &&
                    Number.isFinite(actual) &&
                    actual >= 0 &&
                    actual <= 100 &&
                    Number.isFinite(Number(benchmark.benchmarkValue)) &&
                    Number.isFinite(Number(benchmark.absoluteDeviation)) &&
                    typeof benchmark.meetsBenchmark === "boolean";

            const values = valid
                    ? [
                        item.material,
                        actual.toFixed(2) + "%",
                        benchmark.comparisonOperator + " " +
                                Number(benchmark.benchmarkValue).toFixed(2) +
                                (benchmark.benchmarkUnit || "%"),
                        benchmark.meetsBenchmark
                                ? "TARGET_MET"
                                : "TARGET_NOT_MET",
                        Number(benchmark.absoluteDeviation).toFixed(2)
                    ]
                    : [
                        item.material || "Unknown",
                        "Unavailable",
                        "Unavailable",
                        "Assessment unavailable",
                        "—"
                    ];

            const row = document.createElement("tr");

            values.forEach((value, index) => {

                const td = document.createElement("td");
                td.textContent = value;

                if (index === 3 && valid) {
                    td.className = benchmark.meetsBenchmark
                            ? "text-success font-weight-bold"
                            : "text-danger font-weight-bold";
                }

                row.appendChild(td);
            });

            tbody.appendChild(row);

            if (valid) {
                validCount++;
                benchmarkSource =
                        benchmark.benchmarkSource || benchmarkSource;
            }
        });

        table.appendChild(tbody);
        container.appendChild(table);

        // ==========================================
        // HORIZONTAL BENCHMARK BARS
        // ==========================================

        const barSection = document.createElement("div");
        barSection.className = "mt-4 mb-4";

        const barTitle = document.createElement("h6");
        barTitle.className = "font-weight-bold";
        barTitle.textContent = "Actual Recycled Content vs Benchmark";
        barSection.appendChild(barTitle);

        materials.forEach(item => {

            const benchmark = item.benchmarkAssessment;

            if (!benchmark || benchmark.status !== "success") {
                return;
            }

            const actual = Number(item.recycledFraction) * 100;
            const target = Number(benchmark.benchmarkValue);

            if (!Number.isFinite(actual) ||
                    !Number.isFinite(target) ||
                    actual < 0 || actual > 100 ||
                    target < 0 || target > 100) {
                return;
            }

            const meets = benchmark.meetsBenchmark === true;

            const materialBlock = document.createElement("div");
            materialBlock.className = "mb-4";

            const labelRow = document.createElement("div");
            labelRow.className =
                    "d-flex justify-content-between align-items-center mb-2";

            const materialLabel = document.createElement("strong");
            materialLabel.textContent = item.material;

            const valueLabel = document.createElement("span");
            valueLabel.className = meets
                    ? "text-success font-weight-bold"
                    : "text-danger font-weight-bold";

            valueLabel.textContent =
                    actual.toFixed(1) + "% — " +
                    (meets ? "Target met" : "Target not met");

            labelRow.appendChild(materialLabel);
            labelRow.appendChild(valueLabel);
            materialBlock.appendChild(labelRow);

            // Horizontal bar from 0% to 100%.
            const track = document.createElement("div");
            track.style.position = "relative";
            track.style.width = "100%";
            track.style.height = "20px";
            track.style.backgroundColor = "#e9ecef";
            track.style.borderRadius = "6px";

            const fill = document.createElement("div");
            fill.style.width = actual + "%";
            fill.style.height = "100%";
            fill.style.backgroundColor = meets
                    ? "#28a745"
                    : "#dc3545";
            fill.style.borderRadius = "6px";

            track.appendChild(fill);

            // Vertical line indicating the benchmark threshold.
            const marker = document.createElement("div");
            marker.style.position = "absolute";
            marker.style.left = target + "%";
            marker.style.top = "-5px";
            marker.style.height = "30px";
            marker.style.width = "3px";
            marker.style.backgroundColor = "#212529";
            marker.style.transform = "translateX(-50%)";

            track.appendChild(marker);
            materialBlock.appendChild(track);

            const axis = document.createElement("div");
            axis.className =
                    "d-flex justify-content-between text-muted small mt-1";

            const zero = document.createElement("span");
            zero.textContent = "0%";

            const targetLabel = document.createElement("span");
            targetLabel.textContent =
                    "Benchmark: " + target.toFixed(1) + "%";

            const hundred = document.createElement("span");
            hundred.textContent = "100%";

            axis.appendChild(zero);
            axis.appendChild(targetLabel);
            axis.appendChild(hundred);

            materialBlock.appendChild(axis);
            barSection.appendChild(materialBlock);
        });

        container.appendChild(barSection);

        if (benchmarkSource) {

            const note = document.createElement("p");
            note.className = "text-muted small";

            note.textContent =
                    "Deviation is measured in percentage points. " +
                    "Benchmark source: " + benchmarkSource;

            container.appendChild(note);
        }

        // ==========================================
        // SAVE KPI EVALUATIONS AND ASSESSMENTS
        // ==========================================

        const saveButton = document.createElement("button");
        saveButton.type = "button";
        saveButton.className = "btn btn-success mt-2";
        saveButton.textContent =
                "Save Recycled Content Evaluations & Assessments";

        saveButton.disabled =
                materials.length === 0 ||
                validCount !== materials.length;

        container.appendChild(saveButton);

        const feedback = document.createElement("div");
        feedback.className = "mt-3";
        feedback.setAttribute("role", "status");
        container.appendChild(feedback);

        saveButton.addEventListener("click", async function () {

            if (saveButton.disabled) {
                return;
            }

            saveButton.disabled = true;

            feedback.className = "alert alert-info mt-3";
            feedback.textContent = "Saving to ontology...";

            try {

                const response = await fetch("ontologyReaderAjax", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        action: "saveRecycledContentEvaluation",
                        individual: data.product
                    })
                });

                const result = await response.json();

                if (!response.ok || result.status !== "success") {
                    throw new Error(
                            result.message ||
                            result.error ||
                            "Saving failed."
                            );
                }

                if (!Array.isArray(result.materialEvaluations) ||
                        result.materialEvaluations.length !== materials.length ||
                        result.materialEvaluations.some(item =>
                            !item.evaluationIndividual ||
                                    !item.benchmarkAssessmentIndividual)) {

                    throw new Error(
                            "Incomplete save response. Check the ontology " +
                            "before attempting another save."
                            );
                }

                feedback.className = "alert alert-success mt-3";
                feedback.replaceChildren();

                const summary = document.createElement("p");

                summary.textContent =
                        "Successfully saved " +
                        result.materialEvaluations.length +
                        " KPI evaluations and benchmark assessments.";

                feedback.appendChild(summary);

                const list = document.createElement("ul");

                result.materialEvaluations.forEach(item => {

                    const li = document.createElement("li");

                    li.textContent =
                            item.material + ": " +
                            item.evaluationIndividual +
                            " → " +
                            item.benchmarkAssessmentIndividual;

                    list.appendChild(li);
                });

                feedback.appendChild(list);

                // Keep the button disabled after a successful save
                // to avoid creating duplicate ontology individuals.

            } catch (error) {

                console.error("Recycled Content save failed:", error);

                feedback.className = "alert alert-danger mt-3";

                feedback.textContent =
                        "Save not confirmed: " + error.message +
                        " Some records may already have been created. " +
                        "Check the ontology before retrying.";
            }
        });
    }

    // END RECYCLED CONTENT BENCHMARKING



    function renderRenewableContentTable(data) {
        let tableContainer =
                document.getElementById("renewableContentTable");

        if (!tableContainer) {
            tableContainer = document.createElement("div");
            tableContainer.id = "renewableContentTable";

            const canvas = document.getElementById("materialKpiChart");
            canvas.parentNode.insertBefore(
                    tableContainer, canvas.nextSibling);
        }

        tableContainer.innerHTML = "";


        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent = "Renewable Content by Raw Material";

        tableContainer.appendChild(heading);

        const summary = document.createElement("p");
        summary.textContent =
                "The renewable share (%) is calculated separately " +
                "for each raw material.";

        tableContainer.appendChild(summary);


        const table = document.createElement("table");
        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Material",
            "Total input (kg)",
            "Renewable share (%)",
            "Renewable input (kg)",
            "Product contribution (%)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.renewableContents.forEach(item => {
            const row = document.createElement("tr");

            [
                item.material,
                Number(item.totalMaterialWeight).toFixed(2),
                (Number(item.renewableFraction) * 100).toFixed(2) + "%",
                Number(item.renewableWeight).toFixed(2),
                Number(item.renewablePercentage).toFixed(2) + "%"
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        table.appendChild(tbody);
        tableContainer.appendChild(table);
    }


    function renderRecycledContentChart(data) {
        const canvas = document.getElementById("materialKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.materialChartInstance) {
            window.materialChartInstance.destroy();
        }

        const labels = data.map(item => item.material);
        const values = data.map(item =>
            Number(item.recycledFraction) * 100
        );

        window.materialChartInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                        label: "Recycled share of material (%)",
                        data: values,
                        backgroundColor: "#17a2b8"
                    }]
            },
            options: {
                responsive: true,
                scales: {y: {
                        beginAtZero: true,
                        max: 100,
                        title: {
                            display: true,
                            text: "Recycled material (%)"
                        }
                    }
                }
            }
        });
    }


    function renderRecycledContentTable(data) {
        let tableContainer =
                document.getElementById("recycledContentTable");

        if (!tableContainer) {
            tableContainer = document.createElement("div");
            tableContainer.id = "recycledContentTable";

            const canvas = document.getElementById("materialKpiChart");
            canvas.parentNode.insertBefore(
                    tableContainer, canvas.nextSibling);
        }

        tableContainer.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent =
                `Overall Recycled Content: ${Number(
                        data.overallRecycledPercentage
                        ).toFixed(2)}%`;

        tableContainer.appendChild(heading);

        const summary = document.createElement("p");
        summary.textContent =
                `Recycled material input: ${Number(
                        data.totalRecycledWeight
                        ).toFixed(2)} kg | Finished product weight: ${Number(
                data.productWeight
                ).toFixed(2)} kg`;

        tableContainer.appendChild(summary);

        const table = document.createElement("table");
        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Material",
            "Total input (kg)",
            "Recycled share (%)",
            "Recycled input (kg)",
            "Product contribution (%)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.recycledContents.forEach(item => {
            const row = document.createElement("tr");

            [
                item.material,
                Number(item.totalMaterialWeight).toFixed(2),
                (Number(item.recycledFraction) * 100).toFixed(2) + "%",
                Number(item.recycledWeight).toFixed(2),
                Number(item.recycledPercentage).toFixed(2) + "%"
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        table.appendChild(tbody);
        tableContainer.appendChild(table);
    }


    function renderSupplyTransportCarbonFootprintChart(data) {
        const canvas = document.getElementById("materialKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.materialChartInstance) {
            window.materialChartInstance.destroy();
        }

        const labels = data.map(item => item.material);
        const roadValues = data.map(item =>
            Number(item.roadCarbonKg)
        );
        const seaValues = data.map(item =>
            Number(item.seaCarbonKg)
        );

        window.materialChartInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: "Road transport (kg CO₂eq)",
                        data: roadValues,
                        backgroundColor: "#fd7e14"
                    },
                    {
                        label: "Sea transport (kg CO₂eq)",
                        data: seaValues,
                        backgroundColor: "#007bff"
                    }
                ]
            },
            options: {
                responsive: true,
                scales: {
                    x: {
                        stacked: true
                    },
                    y: {
                        stacked: true,
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: "Transport emissions (kg CO₂eq)"
                        }
                    }
                }
            }
        });
    }


    function renderSupplyTransportCarbonFootprintTable(data) {
        let tableContainer = document.getElementById(
                "supplyTransportCarbonFootprintTable"
                );

        if (!tableContainer) {
            tableContainer = document.createElement("div");
            tableContainer.id = "supplyTransportCarbonFootprintTable";

            const canvas = document.getElementById("materialKpiChart");
            canvas.parentNode.insertBefore(
                    tableContainer, canvas.nextSibling
                    );
        }

        tableContainer.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent =
                `Total Supply Transport Carbon Footprint: ${
                Number(data.totalTransportCarbonKg).toFixed(2)
                } kg CO₂eq`;

        tableContainer.appendChild(heading);

        const summary = document.createElement("p");
        summary.textContent =
                `Road: ${Number(data.totalRoadCarbonKg).toFixed(2)} kg CO₂eq | ` +
                `Sea: ${Number(data.totalSeaCarbonKg).toFixed(2)} kg CO₂eq`;

        tableContainer.appendChild(summary);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Material",
            "Road CO₂eq (kg)",
            "Sea CO₂eq (kg)",
            "Total CO₂eq (kg)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.materialFootprints.forEach(item => {
            const row = document.createElement("tr");

            [
                item.material,
                Number(item.roadCarbonKg).toFixed(3),
                Number(item.seaCarbonKg).toFixed(3),
                Number(item.totalCarbonKg).toFixed(3)
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        const totalRow = document.createElement("tr");
        totalRow.className = "font-weight-bold";

        [
            "TOTAL",
            Number(data.totalRoadCarbonKg).toFixed(3),
            Number(data.totalSeaCarbonKg).toFixed(3),
            Number(data.totalTransportCarbonKg).toFixed(3)
        ].forEach(value => {
            const td = document.createElement("td");
            td.textContent = value;
            totalRow.appendChild(td);
        });

        tbody.appendChild(totalRow);
        table.appendChild(tbody);
        tableContainer.appendChild(table);
    }

    // ======================================================
// SUPPLY TRANSPORT CARBON FOOTPRINT BENCHMARKING
// ======================================================

    function renderSupplyTransportCarbonFootprintBenchmark(data) {

        const container = document.getElementById(
                "supplyTransportBenchmarkResults"
                );

        if (!container) {
            console.error(
                    "Missing supplyTransportBenchmarkResults container."
                    );
            return;
        }

        container.replaceChildren();
        container.style.display = "block";

        const materials = Array.isArray(data.materialFootprints)
                ? data.materialFootprints
                : [];

        const unit = "kg CO₂eq";

        // --------------------------------------------------
        // 1. Validate benchmark assessments
        // --------------------------------------------------

        let validCount = 0;
        let benchmarkSource = "";

        const benchmarkRows = materials.map(item => {

            const benchmark = item.benchmarkAssessment;

            const actual = Number(item.totalCarbonKg);

            const valid =
                    benchmark &&
                    benchmark.status === "success" &&
                    benchmark.kpi === "SupplyTransportCarbonFootprintKPI" &&
                    benchmark.benchmark ===
                    "SupplyTransportCarbonFootprintBenchmark" &&
                    benchmark.benchmarkUnit === "kg CO2eq" &&
                    Number.isFinite(actual) &&
                    actual >= 0 &&
                    Number.isFinite(
                            Number(benchmark.benchmarkValue)
                            ) &&
                    Number.isFinite(
                            Number(benchmark.absoluteDeviation)
                            ) &&
                    typeof benchmark.meetsBenchmark === "boolean";

            if (valid) {
                validCount++;

                benchmarkSource =
                        benchmark.benchmarkSource || benchmarkSource;
            }

            return {
                material: item.material || "Unknown",
                actual: actual,
                benchmark: benchmark,
                valid: Boolean(valid)
            };
        });

        // --------------------------------------------------
        // 2. Benchmark comparison table
        // --------------------------------------------------

        const title = document.createElement("h5");

        title.className = "font-weight-bold mt-3";
        title.textContent =
                "Supply Transport Carbon Footprint Benchmarking";

        container.appendChild(title);

        const table = document.createElement("table");

        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");

        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Raw material",
            "Actual (kg CO₂eq)",
            "Benchmark (kg CO₂eq)",
            "Assessment",
            "Deviation (kg CO₂eq)"
        ].forEach(label => {

            const th = document.createElement("th");

            th.textContent = label;

            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        benchmarkRows.forEach(item => {

            const benchmark = item.benchmark;

            const values = [
                item.material,

                Number.isFinite(item.actual)
                        ? item.actual.toFixed(3)
                        : "Unavailable",

                item.valid
                        ? benchmark.comparisonOperator + " " +
                        Number(benchmark.benchmarkValue).toFixed(3)
                        : "Unavailable",

                item.valid
                        ? (
                                benchmark.meetsBenchmark
                                ? "TARGET_MET"
                                : "TARGET_NOT_MET"
                                )
                        : "Assessment unavailable",

                item.valid
                        ? Number(
                                benchmark.absoluteDeviation
                                ).toFixed(3)
                        : "—"
            ];

            const row = document.createElement("tr");

            values.forEach((value, index) => {

                const td = document.createElement("td");

                td.textContent = value;

                if (index === 3 && item.valid) {

                    td.className =
                            benchmark.meetsBenchmark
                            ? "text-success font-weight-bold"
                            : "text-danger font-weight-bold";
                }

                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        table.appendChild(tbody);
        container.appendChild(table);

        // --------------------------------------------------
        // 3. Visual comparison with maximum benchmark
        // --------------------------------------------------

        const barsTitle = document.createElement("h6");

        barsTitle.className = "font-weight-bold mt-4";

        barsTitle.textContent =
                "Actual Transport Carbon Footprint vs Maximum Allowed";

        container.appendChild(barsTitle);

        const validRows = benchmarkRows.filter(item => item.valid);

        const maxScale = Math.max(
                1,
                ...validRows.map(item => Math.max(
                            item.actual,
                            Number(item.benchmark.benchmarkValue)
                            ))
                ) * 1.12;

        validRows.forEach(item => {

            const benchmark = item.benchmark;

            const actual = item.actual;

            const target = Number(benchmark.benchmarkValue);

            const block = document.createElement("div");

            block.className = "mb-3";

            // Material name and actual value.

            const labelRow = document.createElement("div");

            labelRow.className =
                    "d-flex justify-content-between small mb-1";

            const materialName = document.createElement("strong");

            materialName.textContent = item.material;

            const valueLabel = document.createElement("span");

            valueLabel.textContent =
                    actual.toFixed(2) + " " + unit;

            labelRow.appendChild(materialName);
            labelRow.appendChild(valueLabel);

            block.appendChild(labelRow);

            // Bar background.

            const track = document.createElement("div");

            track.style.cssText =
                    "height:20px;" +
                    "position:relative;" +
                    "background:#e9ecef;" +
                    "border-radius:5px;";

            // Actual transport footprint.

            const fill = document.createElement("div");

            fill.style.height = "100%";

            fill.style.width =
                    Math.min(
                            100,
                            actual / maxScale * 100
                            ) + "%";

            fill.style.backgroundColor =
                    benchmark.meetsBenchmark
                    ? "#28a745"
                    : "#dc3545";

            fill.style.borderRadius = "5px";

            track.appendChild(fill);

            // Vertical benchmark marker.

            const marker = document.createElement("div");

            marker.style.cssText =
                    "position:absolute;" +
                    "top:-4px;" +
                    "height:28px;" +
                    "width:3px;" +
                    "background:#212529;";

            marker.style.left =
                    Math.min(
                            100,
                            target / maxScale * 100
                            ) + "%";

            marker.title =
                    "Maximum benchmark: " +
                    target.toFixed(2) + " " + unit;

            track.appendChild(marker);

            block.appendChild(track);
            container.appendChild(block);
        });

        // --------------------------------------------------
        // 4. Benchmark source and interpretation
        // --------------------------------------------------

        const note = document.createElement("p");

        note.className = "text-muted small mt-3";

        note.textContent =
                "One shared maximum benchmark is applied to each " +
                "raw material. Negative deviation means the " +
                "material is below the maximum. Benchmark source: " +
                (benchmarkSource || "Not available");

        container.appendChild(note);

        // Overall footprint is informational only.
        // The ontology benchmark is defined per raw material.

        if (Number.isFinite(Number(data.totalTransportCarbonKg))) {

            const overall = document.createElement("p");

            overall.className = "alert alert-light border";

            overall.textContent =
                    "Overall combined supply transport footprint " +
                    "(informational only): " +
                    Number(data.totalTransportCarbonKg).toFixed(3) +
                    " " + unit +
                    ". This is not saved as an additional " +
                    "per-material evaluation.";

            container.appendChild(overall);
        }

        // --------------------------------------------------
        // 5. Save KPI evaluations and benchmark assessments
        // --------------------------------------------------

        const saveButton = document.createElement("button");

        saveButton.type = "button";

        saveButton.className = "btn btn-success mt-2";

        saveButton.textContent =
                "Save Supply Transport Carbon Footprint " +
                "Evaluations & Assessments";

        // Do not save unless every material has a
        // successful benchmark assessment.

        saveButton.disabled =
                materials.length === 0 ||
                validCount !== materials.length;

        container.appendChild(saveButton);

        const feedback = document.createElement("div");

        feedback.className = "mt-3";

        feedback.setAttribute("role", "status");

        container.appendChild(feedback);

        if (saveButton.disabled) {

            const warning = document.createElement("p");

            warning.className = "text-danger small mt-2";

            warning.textContent =
                    "Saving is unavailable because one or more " +
                    "material benchmark assessments are missing " +
                    "or invalid in the GET response.";

            container.appendChild(warning);
        }

        // --------------------------------------------------
        // 6. POST save request
        // --------------------------------------------------

        saveButton.addEventListener("click", async function () {

            if (saveButton.disabled) {
                return;
            }

            saveButton.disabled = true;

            feedback.className = "alert alert-info mt-3";

            feedback.textContent = "Saving to ontology...";

            try {

                const productName =
                        data.product || data.manufacturedProduct;

                if (!productName) {
                    throw new Error(
                            "Manufactured product name is missing."
                            );
                }

                const response = await fetch("ontologyReaderAjax", {

                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        action:
                                "saveSupplyTransportCarbonFootprintEvaluation",
                        individual: productName
                    })
                });

                const result = await response.json();

                if (!response.ok ||
                        result.status !== "success") {

                    throw new Error(
                            result.message ||
                            result.error ||
                            "Saving failed."
                            );
                }

                // Verify that all four materials, or however
                // many materials the selected product contains,
                // have both ontology individuals saved.

                if (!Array.isArray(result.materialEvaluations) ||
                        result.materialEvaluations.length !==
                        materials.length ||
                        result.materialEvaluations.some(item =>
                            !item.evaluationIndividual ||
                                    !item.benchmarkAssessmentIndividual
                        )) {

                    throw new Error(
                            "Incomplete save response. Check the ontology " +
                            "before attempting another save."
                            );
                }

                // --------------------------------------------------
                // 7. Success feedback
                // --------------------------------------------------

                feedback.className = "alert alert-success mt-3";

                feedback.replaceChildren();

                const summary = document.createElement("p");

                summary.textContent =
                        "Successfully saved " +
                        result.materialEvaluations.length +
                        " Supply Transport Carbon Footprint KPI " +
                        "evaluations and benchmark assessments.";

                feedback.appendChild(summary);

                const list = document.createElement("ul");

                result.materialEvaluations.forEach(item => {

                    const li = document.createElement("li");

                    li.textContent =
                            item.material + ": " +
                            item.evaluationIndividual +
                            " → " +
                            item.benchmarkAssessmentIndividual;

                    list.appendChild(li);
                });

                feedback.appendChild(list);

                // Keep the Save button disabled after success
                // to prevent accidental duplicate evaluations.

            } catch (error) {

                console.error(
                        "Supply Transport Carbon Footprint save failed:",
                        error
                        );

                feedback.className = "alert alert-danger mt-3";

                feedback.textContent =
                        "Save not confirmed: " +
                        error.message +
                        " Some records may already have been created. " +
                        "Check the ontology before retrying.";
            }
        });
    }

// END SUPPLY TRANSPORT CARBON FOOTPRINT BENCHMARKING


    function renderProductMaterialEfficiencyChart(data) {
        const canvas = document.getElementById("productKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.productChartInstance) {
            window.productChartInstance.destroy();
        }

        window.productChartInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: data.materialIntensities.map(
                        item => item.material
                ),
                datasets: [{
                        label: "Contribution to Material Efficiency (kg/kg)",
                        data: data.materialIntensities.map(
                                item => Number(item.intensity)
                        ),
                        backgroundColor: "#007bff"
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 3.5,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: "Material input / product weight (kg/kg)"
                        }
                    }
                }
            }
        });
    }

    function renderProductMaterialEfficiencyResults(data) {
        const container = document.getElementById(
                "productKpiResults"
                );

        container.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent =
                "Product Material Efficiency: " +
                Number(data.materialEfficiency).toFixed(2) +
                " kg/kg";

        container.appendChild(heading);

        const summary = document.createElement("p");
        summary.textContent =
                "Total material input: " +
                Number(data.totalMaterialInputWeight).toFixed(2) +
                " kg | Manufactured product weight: " +
                Number(data.productWeight).toFixed(2) + " kg";

        container.appendChild(summary);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Material",
            "Total input (kg)",
            "Contribution (kg/kg)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.materialIntensities.forEach(item => {
            const row = document.createElement("tr");

            [
                item.material,
                Number(item.totalMaterialWeight).toFixed(2),
                Number(item.intensity).toFixed(2)
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        const totalRow = document.createElement("tr");
        totalRow.className = "font-weight-bold";

        [
            "TOTAL",
            Number(data.totalMaterialInputWeight).toFixed(2),
            Number(data.materialEfficiency).toFixed(2)
        ].forEach(value => {
            const td = document.createElement("td");
            td.textContent = value;
            totalRow.appendChild(td);
        });

        tbody.appendChild(totalRow);
        table.appendChild(tbody);
        container.appendChild(table);
    }


// ==========================================
// PRODUCT MATERIAL EFFICIENCY BENCHMARK
// ==========================================

    function renderProductMaterialEfficiencyBenchmark(data) {

        const container = document.getElementById(
                "productMaterialEfficiencyBenchmarkResults");

        if (!container) {
            console.error(
                    "Missing productMaterialEfficiencyBenchmarkResults.");
            return;
        }

        container.replaceChildren();
        container.style.display = "block";

        const benchmark = data.benchmarkAssessment;

        const title = document.createElement("h5");
        title.className = "font-weight-bold mt-3";
        title.textContent = "Product Material Efficiency Benchmarking";
        container.appendChild(title);

        if (!benchmark || benchmark.status !== "success") {

            const warning = document.createElement("div");
            warning.className = "alert alert-warning";
            warning.textContent =
                    "Product benchmark evaluation is unavailable. "
                    + "The KPI can be viewed, but saving is disabled.";

            container.appendChild(warning);
            return;
        }

        const actual = Number(data.materialEfficiency);
        const target = Number(benchmark.benchmarkValue);
        const deviation = Number(benchmark.absoluteDeviation);
        const deviationPercent =
                Number(benchmark.deviationPercentage);

        if (!Number.isFinite(actual)
                || !Number.isFinite(target)
                || !Number.isFinite(deviation)
                || !Number.isFinite(deviationPercent)
                || typeof benchmark.meetsBenchmark !== "boolean"
                || benchmark.benchmark !==
                "ProductMaterialEfficiencyBenchmark") {

            const error = document.createElement("div");
            error.className = "alert alert-danger";
            error.textContent =
                    "Invalid Product Material Efficiency benchmark data.";

            container.appendChild(error);
            return;
        }

        // 1. Benchmark comparison table
        const table = document.createElement("table");
        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Actual (kg/kg)",
            "Benchmark",
            "Assessment",
            "Deviation (kg/kg)",
            "Deviation (%)"
        ].forEach(label => {

            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");
        const row = document.createElement("tr");

        const assessmentText = benchmark.meetsBenchmark
                ? "TARGET MET"
                : "TARGET NOT MET";

        [
            actual.toFixed(2),
            benchmark.comparisonOperator + " " + target.toFixed(2),
            assessmentText,
            deviation.toFixed(2),
            deviationPercent.toFixed(2) + "%"
        ].forEach(value => {

            const td = document.createElement("td");
            td.textContent = value;
            row.appendChild(td);
        });

        tbody.appendChild(row);
        table.appendChild(tbody);
        container.appendChild(table);

        // 2. Assessment summary
        const assessment = document.createElement("div");

        assessment.className = benchmark.meetsBenchmark
                ? "alert alert-success"
                : "alert alert-danger";

        assessment.textContent = benchmark.meetsBenchmark
                ? "Target met: Product material input intensity "
                + "satisfies the demonstration benchmark."
                : "Target not met: Product material input intensity "
                + "exceeds the demonstration benchmark by "
                + deviation.toFixed(2) + " kg/kg ("
                + deviationPercent.toFixed(2) + "%).";

        container.appendChild(assessment);

        // ==========================================
// PRODUCT BENCHMARK STATUS BAR
// ==========================================

        const barContainer = document.createElement("div");

        barContainer.style.width = "100%";
        barContainer.style.height = "14px";
        barContainer.style.backgroundColor = "#e9ecef";
        barContainer.style.borderRadius = "8px";
        barContainer.style.overflow = "hidden";
        barContainer.style.marginTop = "15px";
        barContainer.style.marginBottom = "10px";

        const statusBar = document.createElement("div");

        statusBar.style.width = "100%";
        statusBar.style.height = "100%";
        statusBar.style.borderRadius = "8px";

        statusBar.style.backgroundColor = benchmark.meetsBenchmark
                ? "#28a745"
                : "#dc3545";

        statusBar.setAttribute("role", "img");

        statusBar.setAttribute(
                "aria-label",
                benchmark.meetsBenchmark
                ? "Benchmark target met"
                : "Benchmark target not met"
                );

        barContainer.appendChild(statusBar);
        container.appendChild(barContainer);

// END PRODUCT BENCHMARK STATUS BAR

        // 3. Benchmark provenance
        const source = document.createElement("p");
        source.className = "text-muted small";
        source.textContent =
                "Benchmark source: "
                + (benchmark.benchmarkSource || "Not specified");

        container.appendChild(source);

        // 4. Save button
        const saveButton = document.createElement("button");

        saveButton.type = "button";
        saveButton.className = "btn btn-success mt-2";
        saveButton.textContent =
                "Save Product KPI Evaluation & Benchmark Assessment";

        container.appendChild(saveButton);

        // 5. Save feedback
        const feedback = document.createElement("div");
        feedback.className = "mt-3";
        container.appendChild(feedback);

        saveButton.addEventListener("click", async function () {

            saveButton.disabled = true;
            saveButton.textContent = "Saving...";
            feedback.replaceChildren();

            try {

                const response = await fetch(
                        "ontologyReaderAjax",
                        {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({
                                action:
                                        "saveProductMaterialEfficiencyEvaluation",
                                individual: data.product
                            })
                        }
                );

                const result = await response.json();

                if (!response.ok || result.status !== "success") {
                    throw new Error(
                            result.message
                            || result.error
                            || "Saving Product Material Efficiency failed.");
                }

                if (!result.evaluationIndividual
                        || !result.benchmarkAssessmentIndividual) {

                    throw new Error(
                            "Incomplete save response. "
                            + "Check the ontology before retrying.");
                }

                feedback.className = "alert alert-success mt-3";

                const message = document.createElement("p");
                message.className = "font-weight-bold";
                message.textContent =
                        "Product KPI evaluation and benchmark "
                        + "assessment saved successfully.";

                feedback.appendChild(message);

                const evaluation = document.createElement("p");
                evaluation.className = "mb-2";
                evaluation.textContent =
                        "KPIEvaluation: "
                        + result.evaluationIndividual;

                feedback.appendChild(evaluation);

                const assessmentIndividual =
                        document.createElement("p");

                assessmentIndividual.className = "mb-0";
                assessmentIndividual.textContent =
                        "BenchmarkAssessment: "
                        + result.benchmarkAssessmentIndividual;

                feedback.appendChild(assessmentIndividual);

                saveButton.textContent = "Saved";
                saveButton.disabled = true;

            } catch (error) {

                console.error(
                        "Error saving Product Material Efficiency:",
                        error);

                feedback.className = "alert alert-danger mt-3";
                feedback.textContent =
                        error.message
                        + " Check the ontology before retrying; "
                        + "a partial save may have occurred.";

                saveButton.textContent = "Save Failed";
                saveButton.disabled = true;
            }
        });
    }

// END PRODUCT MATERIAL EFFICIENCY BENCHMARK

    function renderProductTransportCarbonChart(data) {

        const centerTextPlugin = {
            id: "transportCarbonCenterText",

            afterDraw(chart) {
                const arc = chart.getDatasetMeta(0).data[0];

                if (!arc) {
                    return;
                }

                const ctx = chart.ctx;

                ctx.save();
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                ctx.fillStyle = "#212529";

                ctx.font = "bold 28px Arial";
                ctx.fillText(
                        Number(data.totalTransportCarbonKg).toFixed(2),
                        arc.x,
                        arc.y - 10
                        );

                ctx.font = "13px Arial";
                ctx.fillText("kg CO₂eq", arc.x, arc.y + 18);

                ctx.restore();
            }
        };

        const canvas = document.getElementById("productKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.productChartInstance) {
            window.productChartInstance.destroy();
        }

        window.productChartInstance = new Chart(ctx, {
            type: "doughnut",

            data: {
                labels: ["Road transport", "Sea transport"],
                datasets: [{
                        data: [
                            Number(data.totalRoadCarbonKg),
                            Number(data.totalSeaCarbonKg)
                        ],
                        backgroundColor: ["#fd7e14", "#007bff"]
                    }]
            },

            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 3.5,

                plugins: {
                    legend: {
                        position: "bottom",

                        labels: {
                            generateLabels: function (chart) {
                                const dataset = chart.data.datasets[0];
                                const total = Number(
                                        data.totalTransportCarbonKg
                                        );

                                return chart.data.labels.map((label, index) => {
                                    const value = Number(dataset.data[index]);
                                    const percentage = total > 0
                                            ? (value / total) * 100
                                            : 0;

                                    return {
                                        text: `${label}: ${value.toFixed(2)} kg CO₂eq (${percentage.toFixed(1)}%)`,
                                        fillStyle: dataset.backgroundColor[index],
                                        strokeStyle: dataset.backgroundColor[index],
                                        lineWidth: 0,
                                        hidden: !chart.getDataVisibility(index),
                                        index: index
                                    };
                                });
                            }
                        }
                    },

                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const value = Number(context.raw);
                                const total = Number(
                                        data.totalTransportCarbonKg
                                        );

                                const percentage = total > 0
                                        ? (value / total) * 100
                                        : 0;

                                return context.label + ": " +
                                        value.toFixed(2) + " kg CO₂eq (" +
                                        percentage.toFixed(1) + "%)";
                            }
                        }
                    }
                }
            },

            plugins: [centerTextPlugin]
        });
    }



    function renderProductTransportCarbonResults(data) {
        const container = document.getElementById("productKpiResults");
        container.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent =
                "Total Supply Transport Carbon Footprint: " +
                Number(data.totalTransportCarbonKg).toFixed(2) +
                " kg CO₂eq";
        container.appendChild(heading);

        const summary = document.createElement("p");
        summary.textContent =
                "Road: " +
                Number(data.totalRoadCarbonKg).toFixed(2) +
                " kg CO₂eq | Sea: " +
                Number(data.totalSeaCarbonKg).toFixed(2) +
                " kg CO₂eq";
        container.appendChild(summary);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");
        [
            "Material",
            "Road (kg CO₂eq)",
            "Sea (kg CO₂eq)",
            "Total (kg CO₂eq)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.materialFootprints.forEach(item => {
            const row = document.createElement("tr");

            [
                item.material,
                Number(item.roadCarbonKg).toFixed(3),
                Number(item.seaCarbonKg).toFixed(3),
                Number(item.totalCarbonKg).toFixed(3)
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        const totalRow = document.createElement("tr");
        totalRow.className = "font-weight-bold";

        [
            "TOTAL",
            Number(data.totalRoadCarbonKg).toFixed(3),
            Number(data.totalSeaCarbonKg).toFixed(3),
            Number(data.totalTransportCarbonKg).toFixed(3)
        ].forEach(value => {
            const td = document.createElement("td");
            td.textContent = value;
            totalRow.appendChild(td);
        });

        tbody.appendChild(totalRow);
        table.appendChild(tbody);
        container.appendChild(table);
    }

    // ==========================================
// PRODUCT TOTAL TRANSPORT CARBON BENCHMARK
// ==========================================

    function renderProductTransportCarbonBenchmark(data) {

        const container = document.getElementById(
                "productTransportCarbonBenchmarkResults");

        if (!container) {
            console.error(
                    "Missing productTransportCarbonBenchmarkResults.");
            return;
        }

        container.replaceChildren();
        container.style.display = "block";

        const benchmark = data.productBenchmarkAssessment;

        const title = document.createElement("h5");
        title.className = "font-weight-bold mt-3";
        title.textContent = "Product Total Transport Carbon Benchmarking";
        container.appendChild(title);

        if (!benchmark || benchmark.status !== "success") {

            const warning = document.createElement("div");
            warning.className = "alert alert-warning";
            warning.textContent =
                    "Product transport carbon benchmark is unavailable. "
                    + "Saving is disabled.";

            container.appendChild(warning);
            return;
        }

        const actual = Number(data.totalTransportCarbonKg);
        const target = Number(benchmark.benchmarkValue);
        const deviation = Number(benchmark.absoluteDeviation);
        const deviationPercent =
                Number(benchmark.deviationPercentage);

        if (!Number.isFinite(actual)
                || !Number.isFinite(target)
                || !Number.isFinite(deviation)
                || !Number.isFinite(deviationPercent)
                || typeof benchmark.meetsBenchmark !== "boolean"
                || benchmark.benchmark !== "ProductTransportCarbonBenchmark"
                || benchmark.kpi !== "ProductTransportCarbonKPI") {

            const error = document.createElement("div");
            error.className = "alert alert-danger";
            error.textContent =
                    "Invalid product transport carbon benchmark data.";

            container.appendChild(error);
            return;
        }

        // 1. Comparison table
        const table = document.createElement("table");
        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Actual (kg CO₂eq)",
            "Benchmark",
            "Assessment",
            "Deviation (kg CO₂eq)",
            "Deviation (%)"
        ].forEach(label => {

            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");
        const row = document.createElement("tr");

        const assessmentText = benchmark.meetsBenchmark
                ? "TARGET MET"
                : "TARGET NOT MET";

        const operator = benchmark.comparisonOperator || "<=";

        [
            actual.toFixed(2),
            operator + " " + target.toFixed(2),
            assessmentText,
            deviation.toFixed(2),
            deviationPercent.toFixed(2) + "%"
        ].forEach(value => {

            const td = document.createElement("td");
            td.textContent = value;
            row.appendChild(td);
        });

        tbody.appendChild(row);
        table.appendChild(tbody);
        container.appendChild(table);

        // 2. Assessment summary
        const assessment = document.createElement("div");

        assessment.className = benchmark.meetsBenchmark
                ? "alert alert-success mt-3"
                : "alert alert-danger mt-3";

        assessment.textContent = benchmark.meetsBenchmark
                ? "Target met: Product Total Transport Carbon "
                + "satisfies the demonstration benchmark."
                : "Target not met: Product Total Transport Carbon "
                + "exceeds the demonstration benchmark by "
                + deviation.toFixed(2) + " kg CO₂eq ("
                + deviationPercent.toFixed(2) + "%).";

        container.appendChild(assessment);

        // 3. Full-width benchmark status bar
        // This matches the bar added to Product Material Efficiency.

        const barContainer = document.createElement("div");

        barContainer.style.width = "100%";
        barContainer.style.height = "14px";
        barContainer.style.backgroundColor = "#e9ecef";
        barContainer.style.borderRadius = "8px";
        barContainer.style.overflow = "hidden";
        barContainer.style.marginTop = "15px";
        barContainer.style.marginBottom = "10px";

        const statusBar = document.createElement("div");

        statusBar.style.width = "100%";
        statusBar.style.height = "100%";
        statusBar.style.borderRadius = "8px";
        statusBar.style.backgroundColor = benchmark.meetsBenchmark
                ? "#28a745"
                : "#dc3545";

        statusBar.setAttribute("role", "img");
        statusBar.setAttribute(
                "aria-label",
                benchmark.meetsBenchmark
                ? "Product transport carbon benchmark target met"
                : "Product transport carbon benchmark target not met"
                );

        barContainer.appendChild(statusBar);
        container.appendChild(barContainer);

        // 4. Benchmark source
        const source = document.createElement("p");
        source.className = "text-muted small mt-3";
        source.textContent =
                "Benchmark source: "
                + (benchmark.benchmarkSource || "Not specified");

        container.appendChild(source);

        // 5. Save button
        const saveButton = document.createElement("button");

        saveButton.type = "button";
        saveButton.className = "btn btn-success mt-2";
        saveButton.textContent =
                "Save Product Transport Carbon Evaluation & Benchmark Assessment";

        container.appendChild(saveButton);

        // 6. Save feedback
        const feedback = document.createElement("div");
        feedback.className = "mt-3";
        container.appendChild(feedback);

        saveButton.addEventListener("click", async function () {

            saveButton.disabled = true;
            saveButton.textContent = "Saving...";
            feedback.replaceChildren();

            try {

                const response = await fetch(
                        "ontologyReaderAjax",
                        {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({
                                action: "saveProductTransportCarbonEvaluation",
                                individual: data.product
                            })
                        }
                );

                const result = await response.json();

                if (!response.ok || result.status !== "success") {
                    throw new Error(
                            result.message
                            || result.error
                            || "Saving Product Total Transport Carbon failed.");
                }

                if (!result.evaluationIndividual
                        || !result.benchmarkAssessmentIndividual) {

                    throw new Error(
                            "Incomplete save response. "
                            + "Check the ontology before retrying.");
                }

                feedback.className = "alert alert-success mt-3";

                const message = document.createElement("p");
                message.className = "font-weight-bold";
                message.textContent =
                        "Product Transport Carbon KPI evaluation and "
                        + "benchmark assessment saved successfully.";

                feedback.appendChild(message);

                const evaluation = document.createElement("p");
                evaluation.className = "mb-2";
                evaluation.textContent =
                        "KPIEvaluation: "
                        + result.evaluationIndividual;

                feedback.appendChild(evaluation);

                const assessmentIndividual =
                        document.createElement("p");

                assessmentIndividual.className = "mb-0";
                assessmentIndividual.textContent =
                        "BenchmarkAssessment: "
                        + result.benchmarkAssessmentIndividual;

                feedback.appendChild(assessmentIndividual);

                saveButton.textContent = "Saved";
                saveButton.disabled = true;

            } catch (error) {

                console.error(
                        "Error saving Product Total Transport Carbon:",
                        error);

                feedback.className = "alert alert-danger mt-3";
                feedback.textContent =
                        error.message
                        + " Check the ontology before retrying; "
                        + "a partial save may have occurred.";

                saveButton.textContent = "Save Failed";
                saveButton.disabled = true;
            }
        });
    }

// END PRODUCT TOTAL TRANSPORT CARBON BENCHMARK


    function renderProductRenewableMaterialChart(data) {

        const total = Number(data.totalMaterialInputWeight);
        const renewable = Number(data.totalRenewableWeight);
        const nonRenewable = total - renewable;
        const percentage = Number(data.productRenewablePercentage);

        const centerTextPlugin = {
            id: "renewableMaterialCenterText",

            afterDraw(chart) {
                const arc = chart.getDatasetMeta(0).data[0];
                if (!arc) {
                    return;
                }

                const ctx = chart.ctx;

                ctx.save();
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                ctx.fillStyle = "#212529";

                ctx.font = "bold 28px Arial";
                ctx.fillText(
                        percentage.toFixed(2) + "%",
                        arc.x,
                        arc.y - 10
                        );

                ctx.font = "13px Arial";
                ctx.fillText("Renewable", arc.x, arc.y + 18);

                ctx.restore();
            }
        };

        const canvas = document.getElementById("productKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.productChartInstance) {
            window.productChartInstance.destroy();
        }

        window.productChartInstance = new Chart(ctx, {
            type: "doughnut",

            data: {
                labels: ["Renewable", "Non-renewable"],
                datasets: [{
                        data: [renewable, nonRenewable],
                        backgroundColor: ["#28a745", "#adb5bd"]
                    }]
            },

            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 3.5,

                plugins: {
                    legend: {
                        position: "bottom",

                        labels: {
                            generateLabels: function (chart) {
                                const dataset = chart.data.datasets[0];

                                return chart.data.labels.map((label, index) => {
                                    const value = Number(dataset.data[index]);
                                    const share = total > 0
                                            ? (value / total) * 100
                                            : 0;

                                    return {
                                        text: `${label}: ${value.toFixed(2)} kg (${share.toFixed(1)}%)`,
                                        fillStyle: dataset.backgroundColor[index],
                                        strokeStyle: dataset.backgroundColor[index],
                                        lineWidth: 0,
                                        hidden: !chart.getDataVisibility(index),
                                        index: index
                                    };
                                });
                            }
                        }
                    },

                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const value = Number(context.raw);
                                const share = total > 0
                                        ? (value / total) * 100
                                        : 0;

                                return context.label + ": " +
                                        value.toFixed(2) + " kg (" +
                                        share.toFixed(1) + "%)";
                            }
                        }
                    }
                }
            },

            plugins: [centerTextPlugin]
        });
    }


    function renderProductRenewableMaterialResults(data) {

        const container = document.getElementById("productKpiResults");
        container.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent =
                "Product Renewable Material: " +
                Number(data.productRenewablePercentage).toFixed(2) + "%";

        container.appendChild(heading);

        const total = Number(data.totalMaterialInputWeight);
        const renewable = Number(data.totalRenewableWeight);

        const summary = document.createElement("p");
        summary.textContent =
                "Renewable material: " + renewable.toFixed(2) +
                " kg | Non-renewable material: " +
                (total - renewable).toFixed(2) +
                " kg | Total material input: " +
                total.toFixed(2) + " kg";

        container.appendChild(summary);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Material",
            "Total input (kg)",
            "Renewable share (%)",
            "Renewable input (kg)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.renewableContents.forEach(item => {
            const row = document.createElement("tr");

            [
                item.material,
                Number(item.totalMaterialWeight).toFixed(2),
                (Number(item.renewableFraction) * 100).toFixed(2) + "%",
                Number(item.renewableWeight).toFixed(2)
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        const totalRow = document.createElement("tr");
        totalRow.className = "font-weight-bold";

        [
            "TOTAL",
            total.toFixed(2),
            Number(data.productRenewablePercentage).toFixed(2) + "%",
            renewable.toFixed(2)
        ].forEach(value => {
            const td = document.createElement("td");
            td.textContent = value;
            totalRow.appendChild(td);
        });

        tbody.appendChild(totalRow);
        table.appendChild(tbody);
        container.appendChild(table);
    }


    // ==========================================
// PRODUCT RENEWABLE MATERIAL BENCHMARK
// ==========================================

    function renderProductRenewableMaterialBenchmark(data) {

        const container = document.getElementById(
                "productRenewableMaterialBenchmarkResults");

        if (!container) {
            console.error(
                    "Missing productRenewableMaterialBenchmarkResults.");
            return;
        }

        container.replaceChildren();
        container.style.display = "block";

        const benchmark = data.benchmarkAssessment;

        const title = document.createElement("h5");
        title.className = "font-weight-bold mt-3";
        title.textContent = "Product Renewable Material Benchmarking";
        container.appendChild(title);

        if (!benchmark || benchmark.status !== "success") {

            const warning = document.createElement("div");
            warning.className = "alert alert-warning";
            warning.textContent =
                    "Product Renewable Material benchmark is unavailable. "
                    + "Saving is disabled.";

            container.appendChild(warning);
            return;
        }

        const actual = Number(data.productRenewablePercentage);
        const target = Number(benchmark.benchmarkValue);
        const deviation = Number(benchmark.absoluteDeviation);
        const deviationPercent = Number(benchmark.deviationPercentage);

        if (!Number.isFinite(actual)
                || !Number.isFinite(target)
                || !Number.isFinite(deviation)
                || (target !== 0 && !Number.isFinite(deviationPercent))
                || actual < 0 || actual > 100
                || target < 0 || target > 100
                || typeof benchmark.meetsBenchmark !== "boolean"
                || benchmark.kpi !== "ProductRenewableMaterialKPI"
                || benchmark.benchmark !== "ProductRenewableMaterialBenchmark"
                || benchmark.comparisonOperator !== ">=") {

            const error = document.createElement("div");
            error.className = "alert alert-danger";
            error.textContent =
                    "Invalid Product Renewable Material benchmark data.";

            container.appendChild(error);
            return;
        }

        // ==========================================
        // 1. BENCHMARK COMPARISON TABLE
        // ==========================================

        const table = document.createElement("table");
        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Actual (%)",
            "Benchmark",
            "Assessment",
            "Deviation (percentage points)",
            "Deviation (%)"
        ].forEach(label => {

            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");
        const row = document.createElement("tr");

        const assessmentText = benchmark.meetsBenchmark
                ? "TARGET MET"
                : "TARGET NOT MET";

        const relativeDeviationText = target === 0
                ? "N/A"
                : deviationPercent.toFixed(2) + "%";

        [
            actual.toFixed(2) + "%",
            "≥ " + target.toFixed(2) + "%",
            assessmentText,
            deviation.toFixed(2) + " pp",
            relativeDeviationText
        ].forEach(value => {

            const td = document.createElement("td");
            td.textContent = value;
            row.appendChild(td);
        });

        tbody.appendChild(row);
        table.appendChild(tbody);
        container.appendChild(table);

        // ==========================================
        // 2. ASSESSMENT SUMMARY
        // ==========================================

        const assessment = document.createElement("div");

        assessment.className = benchmark.meetsBenchmark
                ? "alert alert-success mt-3"
                : "alert alert-danger mt-3";

        if (benchmark.meetsBenchmark) {

            assessment.textContent =
                    "Target met: Renewable material content is "
                    + Math.abs(deviation).toFixed(2)
                    + " percentage points above or equal to "
                    + "the demonstration benchmark.";

        } else {

            assessment.textContent =
                    "Target not met: Renewable material content is "
                    + Math.abs(deviation).toFixed(2)
                    + " percentage points below "
                    + "the demonstration benchmark.";
        }

        container.appendChild(assessment);

        // ==========================================
        // 3. RED/GREEN BENCHMARK STATUS BAR
        // ==========================================
        // Same full-width status indicator as the other Product KPIs.

        const barContainer = document.createElement("div");

        barContainer.style.width = "100%";
        barContainer.style.height = "14px";
        barContainer.style.backgroundColor = "#e9ecef";
        barContainer.style.borderRadius = "8px";
        barContainer.style.overflow = "hidden";
        barContainer.style.marginTop = "15px";
        barContainer.style.marginBottom = "10px";

        const statusBar = document.createElement("div");

        statusBar.style.width = "100%";
        statusBar.style.height = "100%";
        statusBar.style.borderRadius = "8px";
        statusBar.style.backgroundColor = benchmark.meetsBenchmark
                ? "#28a745"
                : "#dc3545";

        statusBar.setAttribute("role", "img");

        statusBar.setAttribute(
                "aria-label",
                benchmark.meetsBenchmark
                ? "Renewable Material benchmark target met"
                : "Renewable Material benchmark target not met"
                );

        barContainer.appendChild(statusBar);
        container.appendChild(barContainer);

        // ==========================================
        // 4. BENCHMARK SOURCE
        // ==========================================

        const source = document.createElement("p");
        source.className = "text-muted small mt-3";
        source.textContent =
                "Benchmark source: "
                + (benchmark.benchmarkSource || "Not specified");

        container.appendChild(source);

        // ==========================================
        // 5. SAVE BUTTON
        // ==========================================

        const saveButton = document.createElement("button");

        saveButton.type = "button";
        saveButton.className = "btn btn-success mt-2";
        saveButton.textContent =
                "Save Product Renewable Material Evaluation & Benchmark Assessment";

        container.appendChild(saveButton);

        // ==========================================
        // 6. SAVE FEEDBACK
        // ==========================================

        const feedback = document.createElement("div");
        feedback.className = "mt-3";
        container.appendChild(feedback);

        saveButton.addEventListener("click", async function () {

            saveButton.disabled = true;
            saveButton.textContent = "Saving...";
            feedback.replaceChildren();

            try {

                const response = await fetch(
                        "ontologyReaderAjax",
                        {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({
                                action: "saveProductRenewableMaterialEvaluation",
                                individual: data.product
                            })
                        }
                );

                const result = await response.json();

                if (!response.ok || result.status !== "success") {
                    throw new Error(
                            result.message
                            || result.error
                            || "Saving Product Renewable Material failed.");
                }

                if (!result.evaluationIndividual
                        || !result.benchmarkAssessmentIndividual) {

                    throw new Error(
                            "Incomplete save response. "
                            + "Check the ontology before retrying.");
                }

                feedback.className = "alert alert-success mt-3";

                const message = document.createElement("p");
                message.className = "font-weight-bold";
                message.textContent =
                        "Product Renewable Material KPI evaluation and "
                        + "benchmark assessment saved successfully.";

                feedback.appendChild(message);

                const evaluation = document.createElement("p");
                evaluation.className = "mb-2";
                evaluation.textContent =
                        "KPIEvaluation: "
                        + result.evaluationIndividual;

                feedback.appendChild(evaluation);

                const assessmentIndividual = document.createElement("p");
                assessmentIndividual.className = "mb-0";
                assessmentIndividual.textContent =
                        "BenchmarkAssessment: "
                        + result.benchmarkAssessmentIndividual;

                feedback.appendChild(assessmentIndividual);

                saveButton.textContent = "Saved";
                saveButton.disabled = true;

            } catch (error) {

                console.error(
                        "Error saving Product Renewable Material:",
                        error);

                feedback.className = "alert alert-danger mt-3";
                feedback.textContent =
                        error.message
                        + " Check the ontology before retrying; "
                        + "a partial save may have occurred.";

                saveButton.textContent = "Save Failed";
                saveButton.disabled = true;
            }
        });
    }

// END PRODUCT RENEWABLE MATERIAL BENCHMARK


    function renderProductRecycledMaterialChart(data) {

        const total = Number(data.totalMaterialInputWeight);
        const recycled = Number(data.totalRecycledWeight);
        const nonRecycled = total - recycled;
        const percentage = Number(data.productRecycledPercentage);

        const centerTextPlugin = {
            id: "recycledMaterialCenterText",

            afterDraw(chart) {
                const arc = chart.getDatasetMeta(0).data[0];

                if (!arc) {
                    return;
                }

                const ctx = chart.ctx;

                ctx.save();
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                ctx.fillStyle = "#212529";

                ctx.font = "bold 28px Arial";
                ctx.fillText(
                        percentage.toFixed(2) + "%",
                        arc.x,
                        arc.y - 10
                        );

                ctx.font = "13px Arial";
                ctx.fillText("Recycled", arc.x, arc.y + 18);

                ctx.restore();
            }
        };

        const canvas = document.getElementById("productKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.productChartInstance) {
            window.productChartInstance.destroy();
        }

        window.productChartInstance = new Chart(ctx, {
            type: "doughnut",

            data: {
                labels: ["Recycled", "Non-recycled"],
                datasets: [{
                        data: [recycled, nonRecycled],
                        backgroundColor: ["#17a2b8", "#adb5bd"]
                    }]
            },

            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 3.5,

                plugins: {
                    legend: {
                        position: "bottom",

                        labels: {
                            generateLabels: function (chart) {
                                const dataset = chart.data.datasets[0];

                                return chart.data.labels.map((label, index) => {
                                    const value = Number(dataset.data[index]);
                                    const share = total > 0
                                            ? (value / total) * 100
                                            : 0;

                                    return {
                                        text: `${label}: ${value.toFixed(2)} kg (${share.toFixed(1)}%)`,
                                        fillStyle: dataset.backgroundColor[index],
                                        strokeStyle: dataset.backgroundColor[index],
                                        lineWidth: 0,
                                        hidden: !chart.getDataVisibility(index),
                                        index: index
                                    };
                                });
                            }
                        }
                    },

                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const value = Number(context.raw);
                                const share = total > 0
                                        ? (value / total) * 100
                                        : 0;

                                return context.label + ": " +
                                        value.toFixed(2) + " kg (" +
                                        share.toFixed(1) + "%)";
                            }
                        }
                    }
                }
            },

            plugins: [centerTextPlugin]
        });
    }


    function renderProductRecycledMaterialResults(data) {

        const container = document.getElementById("productKpiResults");
        container.innerHTML = "";

        const total = Number(data.totalMaterialInputWeight);
        const recycled = Number(data.totalRecycledWeight);
        const percentage = Number(data.productRecycledPercentage);

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent =
                "Product Recycled Material: " +
                percentage.toFixed(2) + "%";

        container.appendChild(heading);

        const summary = document.createElement("p");
        summary.textContent =
                "Recycled material: " + recycled.toFixed(2) +
                " kg | Non-recycled material: " +
                (total - recycled).toFixed(2) +
                " kg | Total material input: " +
                total.toFixed(2) + " kg";

        container.appendChild(summary);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Material",
            "Total input (kg)",
            "Recycled share (%)",
            "Recycled input (kg)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.recycledContents.forEach(item => {
            const row = document.createElement("tr");

            [
                item.material,
                Number(item.totalMaterialWeight).toFixed(2),
                (Number(item.recycledFraction) * 100).toFixed(2) + "%",
                Number(item.recycledWeight).toFixed(2)
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        const totalRow = document.createElement("tr");
        totalRow.className = "font-weight-bold";

        [
            "TOTAL",
            total.toFixed(2),
            percentage.toFixed(2) + "%",
            recycled.toFixed(2)
        ].forEach(value => {
            const td = document.createElement("td");
            td.textContent = value;
            totalRow.appendChild(td);
        });

        tbody.appendChild(totalRow);
        table.appendChild(tbody);
        container.appendChild(table);
    }

    // ==========================================
// PRODUCT RECYCLED MATERIAL BENCHMARK
// ==========================================

    function renderProductRecycledMaterialBenchmark(data) {

        const container = document.getElementById(
                "productRecycledMaterialBenchmarkResults");

        if (!container) {
            console.error(
                    "Missing productRecycledMaterialBenchmarkResults.");
            return;
        }

        container.replaceChildren();
        container.style.display = "block";

        const benchmark = data.benchmarkAssessment;

        const title = document.createElement("h5");
        title.className = "font-weight-bold mt-3";
        title.textContent = "Product Recycled Material Benchmarking";
        container.appendChild(title);

        if (!benchmark || benchmark.status !== "success") {

            const warning = document.createElement("div");
            warning.className = "alert alert-warning";
            warning.textContent =
                    "Product Recycled Material benchmark is unavailable. "
                    + "Saving is disabled.";

            container.appendChild(warning);
            return;
        }

        const actual = Number(data.productRecycledPercentage);
        const target = Number(benchmark.benchmarkValue);
        const deviation = Number(benchmark.absoluteDeviation);

        const deviationPercent =
                benchmark.deviationPercentage == null
                ? NaN
                : Number(benchmark.deviationPercentage);

        if (!Number.isFinite(actual)
                || !Number.isFinite(target)
                || !Number.isFinite(deviation)
                || (target !== 0 && !Number.isFinite(deviationPercent))
                || actual < 0 || actual > 100
                || target < 0 || target > 100
                || typeof benchmark.meetsBenchmark !== "boolean"
                || benchmark.kpi !== "ProductRecycledMaterialKPI"
                || benchmark.benchmark !== "ProductRecycledMaterialBenchmark"
                || benchmark.comparisonOperator !== ">=") {

            const error = document.createElement("div");
            error.className = "alert alert-danger";
            error.textContent =
                    "Invalid Product Recycled Material benchmark data.";

            container.appendChild(error);
            return;
        }

        // ==========================================
        // 1. BENCHMARK COMPARISON TABLE
        // ==========================================

        const table = document.createElement("table");
        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Actual (%)",
            "Benchmark",
            "Assessment",
            "Deviation (percentage points)",
            "Deviation (%)"
        ].forEach(label => {

            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");
        const row = document.createElement("tr");

        const assessmentText = benchmark.meetsBenchmark
                ? "TARGET MET"
                : "TARGET NOT MET";

        const relativeDeviationText = target === 0
                ? "N/A"
                : deviationPercent.toFixed(2) + "%";

        [
            actual.toFixed(2) + "%",
            "≥ " + target.toFixed(2) + "%",
            assessmentText,
            deviation.toFixed(2) + " pp",
            relativeDeviationText
        ].forEach(value => {

            const td = document.createElement("td");
            td.textContent = value;
            row.appendChild(td);
        });

        tbody.appendChild(row);
        table.appendChild(tbody);
        container.appendChild(table);

        // ==========================================
        // 2. ASSESSMENT SUMMARY
        // ==========================================

        const assessment = document.createElement("div");

        assessment.className = benchmark.meetsBenchmark
                ? "alert alert-success mt-3"
                : "alert alert-danger mt-3";

        if (benchmark.meetsBenchmark) {

            assessment.textContent =
                    "Target met: Recycled material content is "
                    + Math.abs(deviation).toFixed(2)
                    + " percentage points above or equal to "
                    + "the demonstration benchmark.";

        } else {

            assessment.textContent =
                    "Target not met: Recycled material content is "
                    + Math.abs(deviation).toFixed(2)
                    + " percentage points below "
                    + "the demonstration benchmark ("
                    + Math.abs(deviationPercent).toFixed(2)
                    + "% below target).";
        }

        container.appendChild(assessment);

        // ==========================================
        // 3. FULL-WIDTH RED/GREEN STATUS BAR
        // ==========================================

        const barContainer = document.createElement("div");

        barContainer.style.width = "100%";
        barContainer.style.height = "14px";
        barContainer.style.backgroundColor = "#e9ecef";
        barContainer.style.borderRadius = "8px";
        barContainer.style.overflow = "hidden";
        barContainer.style.marginTop = "15px";
        barContainer.style.marginBottom = "10px";

        const statusBar = document.createElement("div");

        statusBar.style.width = "100%";
        statusBar.style.height = "100%";
        statusBar.style.borderRadius = "8px";

        statusBar.style.backgroundColor = benchmark.meetsBenchmark
                ? "#28a745"
                : "#dc3545";

        statusBar.setAttribute("role", "img");

        statusBar.setAttribute(
                "aria-label",
                benchmark.meetsBenchmark
                ? "Product Recycled Material benchmark target met"
                : "Product Recycled Material benchmark target not met"
                );

        barContainer.appendChild(statusBar);
        container.appendChild(barContainer);

        // ==========================================
        // 4. BENCHMARK SOURCE
        // ==========================================

        const source = document.createElement("p");
        source.className = "text-muted small mt-3";

        source.textContent =
                "Benchmark source: "
                + (benchmark.benchmarkSource || "Not specified");

        container.appendChild(source);

        // ==========================================
        // 5. SAVE BUTTON
        // ==========================================

        const saveButton = document.createElement("button");

        saveButton.type = "button";
        saveButton.className = "btn btn-success mt-2";

        saveButton.textContent =
                "Save Product Recycled Material Evaluation & Benchmark Assessment";

        container.appendChild(saveButton);

        // ==========================================
        // 6. SAVE FEEDBACK
        // ==========================================

        const feedback = document.createElement("div");
        feedback.className = "mt-3";
        container.appendChild(feedback);

        saveButton.addEventListener("click", async function () {

            saveButton.disabled = true;
            saveButton.textContent = "Saving...";
            feedback.replaceChildren();

            try {

                const response = await fetch(
                        "ontologyReaderAjax",
                        {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({
                                action: "saveProductRecycledMaterialEvaluation",
                                individual: data.product
                            })
                        }
                );

                const result = await response.json();

                if (!response.ok || result.status !== "success") {

                    throw new Error(
                            result.message
                            || result.error
                            || "Saving Product Recycled Material failed.");
                }

                if (!result.evaluationIndividual
                        || !result.benchmarkAssessmentIndividual) {

                    throw new Error(
                            "Incomplete save response. "
                            + "Check the ontology before retrying.");
                }

                feedback.className = "alert alert-success mt-3";

                const message = document.createElement("p");
                message.className = "font-weight-bold";

                message.textContent =
                        "Product Recycled Material KPI evaluation and "
                        + "benchmark assessment saved successfully.";

                feedback.appendChild(message);

                const evaluation = document.createElement("p");
                evaluation.className = "mb-2";

                evaluation.textContent =
                        "KPIEvaluation: "
                        + result.evaluationIndividual;

                feedback.appendChild(evaluation);

                const assessmentIndividual = document.createElement("p");
                assessmentIndividual.className = "mb-0";

                assessmentIndividual.textContent =
                        "BenchmarkAssessment: "
                        + result.benchmarkAssessmentIndividual;

                feedback.appendChild(assessmentIndividual);

                saveButton.textContent = "Saved";
                saveButton.disabled = true;

            } catch (error) {

                console.error(
                        "Error saving Product Recycled Material:",
                        error);

                feedback.className = "alert alert-danger mt-3";

                feedback.textContent =
                        error.message
                        + " Check the ontology before retrying; "
                        + "a partial save may have occurred.";

                saveButton.textContent = "Save Failed";
                saveButton.disabled = true;
            }
        });
    }

// END PRODUCT RECYCLED MATERIAL BENCHMARK


    function renderCorporateEnergyCarbonChart(data) {

        const canvas = document.getElementById("energyKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.energyChartInstance) {
            window.energyChartInstance.destroy();
        }

        const sources = data.energySources;

        const colors = {
            Electricity: "#ffc107",
            Gasoil: "#fd7e14",
            NaturalGas: "#007bff",
            Other: "#6c757d"
        };

        window.energyChartInstance = new Chart(ctx, {

            type: "bar",

            data: {
                labels: sources.map(item => item.sourceType),

                datasets: [{
                        label: "Carbon footprint (kg CO₂eq/kg)",
                        data: sources.map(
                                item => Number(item.carbonPerKg)
                        ),
                        backgroundColor: sources.map(
                                item => colors[item.sourceType] || "#6c757d"
                        )
                    }]
            },

            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 3.5,

                plugins: {
                    legend: {
                        position: "bottom"
                    },

                    tooltip: {
                        callbacks: {
                            label: function (context) {

                                return "Carbon footprint: " +
                                        Number(context.raw).toFixed(4) +
                                        " kg CO₂eq/kg";
                            }
                        }
                    }
                },

                scales: {
                    y: {
                        beginAtZero: true,

                        title: {
                            display: true,
                            text: "kg CO₂eq / kg production output"
                        }
                    }
                }
            }

        });
    }


    function renderCorporateEnergyCarbonResults(data) {

        const container = document.getElementById(
                "energyKpiResults"
                );

        container.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";

        heading.textContent =
                "Corporate Energy Carbon Footprint: " +
                Number(data.totalEnergyCarbonPerKg).toFixed(4) +
                " kg CO₂eq/kg";

        container.appendChild(heading);

        const summary = document.createElement("p");

        summary.textContent =
                "Total energy emissions: " +
                Number(data.totalEnergyCarbonKg).toFixed(2) +
                " kg CO₂eq | Production output: " +
                Number(data.productionOutputKg).toFixed(2) +
                " kg";

        container.appendChild(summary);

        const table = document.createElement("table");
        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Energy source",
            "Consumption",
            "Emission factor",
            "Emissions (kg CO₂eq)",
            "Carbon intensity (kg CO₂eq/kg)"
        ].forEach(label => {

            const th = document.createElement("th");
            th.textContent = label;

            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.energySources.forEach(item => {

            const row = document.createElement("tr");

            [
                item.sourceType,
                Number(item.consumption).toFixed(2) +
                        " " + item.consumptionUnit,
                Number(item.emissionFactor).toFixed(3) +
                        " kg CO₂eq/" + item.consumptionUnit,
                Number(item.carbonKg).toFixed(2),
                Number(item.carbonPerKg).toFixed(4)
            ].forEach(value => {

                const td = document.createElement("td");
                td.textContent = value;

                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        const totalRow = document.createElement("tr");
        totalRow.className = "font-weight-bold";

        [
            "TOTAL",
            "—",
            "—",
            Number(data.totalEnergyCarbonKg).toFixed(2),
            Number(data.totalEnergyCarbonPerKg).toFixed(4)
        ].forEach(value => {

            const td = document.createElement("td");
            td.textContent = value;

            totalRow.appendChild(td);
        });

        tbody.appendChild(totalRow);
        table.appendChild(tbody);

        container.appendChild(table);
    }

    function renderCorporateRenewableElectricityChart(data) {

        const canvas = document.getElementById("energyKpiChart");
        const ctx = canvas.getContext("2d");

        if (window.energyChartInstance) {
            window.energyChartInstance.destroy();
            window.energyChartInstance = null;
        }

        const renewableKWh = Number(data.renewableElectricityKWh);
        const totalKWh = Number(data.totalElectricityKWh);
        const nonRenewableKWh = totalKWh - renewableKWh;

        window.energyChartInstance = new Chart(ctx, {

            type: "doughnut",

            data: {
                labels: [
                    "Renewable Electricity",
                    "Non-Renewable Electricity"
                ],

                datasets: [{
                        data: [
                            renewableKWh,
                            nonRenewableKWh
                        ],

                        backgroundColor: [
                            "#28a745",
                            "#dee2e6"
                        ],

                        borderWidth: 2,
                        borderColor: "#ffffff"
                    }]
            },

            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 3.5,
                cutout: "68%",

                plugins: {
                    legend: {
                        position: "bottom"
                    },

                    tooltip: {
                        callbacks: {
                            label: function (context) {

                                const kWh = Number(context.raw);
                                const percentage =
                                        totalKWh > 0
                                        ? (kWh / totalKWh) * 100
                                        : 0;

                                return context.label + ": " +
                                        kWh.toFixed(2) + " kWh (" +
                                        percentage.toFixed(1) + "%)";
                            }
                        }
                    }
                }
            }
        });
    }


    function renderCorporateRenewableElectricityResults(data) {

        const container = document.getElementById(
                "energyKpiResults"
                );

        container.innerHTML = "";

        const totalKWh = Number(data.totalElectricityKWh);
        const renewableKWh = Number(data.renewableElectricityKWh);
        const nonRenewableKWh = totalKWh - renewableKWh;

        const renewablePercent =
                Number(data.renewableElectricityPercentage);

        const nonRenewablePercent = 100 - renewablePercent;

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";

        heading.textContent =
                "Renewable Electricity Used: " +
                renewablePercent.toFixed(2) + "%";

        container.appendChild(heading);

        const description = document.createElement("p");
        description.className = "text-muted";

        description.textContent =
                "Percentage of electricity consumption supplied " +
                "by renewable sources.";

        container.appendChild(description);

        const table = document.createElement("table");

        table.className =
                "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        ["Electricity source", "Consumption (kWh)", "Share (%)"]
                .forEach(label => {

                    const th = document.createElement("th");
                    th.textContent = label;
                    headerRow.appendChild(th);
                });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        const rows = [
            [
                "Renewable",
                renewableKWh.toFixed(2),
                renewablePercent.toFixed(2)
            ],
            [
                "Non-renewable",
                nonRenewableKWh.toFixed(2),
                nonRenewablePercent.toFixed(2)
            ],
            [
                "TOTAL",
                totalKWh.toFixed(2),
                "100.00"
            ]
        ];

        rows.forEach((values, index) => {

            const tr = document.createElement("tr");

            if (index === rows.length - 1) {
                tr.className = "font-weight-bold";
            }

            values.forEach(value => {

                const td = document.createElement("td");
                td.textContent = value;
                tr.appendChild(td);
            });

            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        container.appendChild(table);

        const note = document.createElement("p");
        note.className = "text-muted small mt-2";

        note.textContent =
                "This indicator measures renewable electricity only. " +
                "It does not combine electricity, gasoil, natural gas, " +
                "or other energy sources.";

        container.appendChild(note);
    }


    function renderProductEnergyCarbonChart(data) {

        const canvas = document.getElementById("energyKpiChart");
        const ctx = canvas.getContext("2d");

        const colors = {
            Electricity: "#ffc107",
            Gasoil: "#fd7e14",
            NaturalGas: "#007bff",
            Other: "#6c757d"
        };

        window.energyChartInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: data.energySources.map(item => item.sourceType),
                datasets: [{
                        label: "Product carbon intensity (kg CO₂eq/kg)",
                        data: data.energySources.map(item => Number(item.carbonPerKg)),
                        backgroundColor: data.energySources.map(
                                item => colors[item.sourceType] || "#6c757d"
                        )
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 3.5,
                plugins: {
                    legend: {
                        position: "bottom"
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return Number(context.raw).toFixed(4)
                                        + " kg CO₂eq/kg product";
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: "kg CO₂eq / kg product"
                        }
                    }
                }
            }
        });
    }


    function renderProductEnergyCarbonResults(data) {

        const container = document.getElementById("energyKpiResults");
        container.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent =
                "Product Energy Carbon Footprint: "
                + Number(data.totalProductCarbonPerKg).toFixed(4)
                + " kg CO₂eq/kg product";
        container.appendChild(heading);

        const summary = document.createElement("p");
        summary.textContent =
                "Allocated emissions: "
                + Number(data.totalAllocatedCarbonKg).toFixed(2)
                + " kg CO₂eq | Product weight: "
                + Number(data.productWeightKg).toFixed(2)
                + " kg | Allocation: "
                + (Number(data.allocationFraction) * 100).toFixed(2)
                + "%";
        container.appendChild(summary);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Energy source",
            "Corporate emissions (kg CO₂eq)",
            "Allocated emissions (kg CO₂eq)",
            "Product intensity (kg CO₂eq/kg)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        data.energySources.forEach(item => {
            const row = document.createElement("tr");

            [
                item.sourceType,
                Number(item.corporateCarbonKg).toFixed(2),
                Number(item.allocatedCarbonKg).toFixed(2),
                Number(item.carbonPerKg).toFixed(4)
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        const totalRow = document.createElement("tr");
        totalRow.className = "font-weight-bold";

        [
            "TOTAL",
            Number(data.corporateTotalCarbonKg).toFixed(2),
            Number(data.totalAllocatedCarbonKg).toFixed(2),
            Number(data.totalProductCarbonPerKg).toFixed(4)
        ].forEach(value => {
            const td = document.createElement("td");
            td.textContent = value;
            totalRow.appendChild(td);
        });

        tbody.appendChild(totalRow);
        table.appendChild(tbody);
        container.appendChild(table);

        const note = document.createElement("p");
        note.className = "text-muted small mt-2";
        note.textContent =
                "Prototype mass allocation using the selected product weight "
                + "and the explicitly specified energy process. "
                + "The product-to-process association has not yet been verified "
                + "through an ontology relationship.";
        container.appendChild(note);
    }


    function renderProductEmbodiedCarbonChart(data) {

        const canvas = document.getElementById("energyKpiChart");
        const ctx = canvas.getContext("2d");

        const total = Number(data.embodiedCarbonKg);

        const colors = {
            Electricity: "#ffc107",
            Gasoil: "#fd7e14",
            NaturalGas: "#007bff",
            Other: "#6c757d"
        };

        window.energyChartInstance = new Chart(ctx, {
            type: "doughnut",
            data: {
                labels: data.energySources.map(item => item.sourceType),
                datasets: [{
                        data: data.energySources.map(
                                item => Number(item.allocatedCarbonKg)
                        ),
                        backgroundColor: data.energySources.map(
                                item => colors[item.sourceType] || "#6c757d"
                        ),
                        borderColor: "#ffffff",
                        borderWidth: 2
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 3.5,
                cutout: "68%",
                plugins: {
                    legend: {
                        position: "bottom"
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const value = Number(context.raw);
                                const share = total > 0
                                        ? (value / total) * 100
                                        : 0;

                                return context.label + ": "
                                        + value.toFixed(2)
                                        + " kg CO₂eq ("
                                        + share.toFixed(1) + "%)";
                            }
                        }
                    }
                }
            }
        });
    }


    function renderProductEmbodiedCarbonResults(data) {

        const container = document.getElementById("energyKpiResults");
        container.innerHTML = "";

        const heading = document.createElement("h5");
        heading.className = "font-weight-bold mt-3";
        heading.textContent =
                "Energy-Related Product Embodied Carbon: "
                + Number(data.embodiedCarbonKg).toFixed(2)
                + " kg CO₂eq";
        container.appendChild(heading);

        const summary = document.createElement("p");
        summary.textContent =
                "Carbon intensity: "
                + Number(data.embodiedCarbonPerKg).toFixed(4)
                + " kg CO₂eq/kg product | Product weight: "
                + Number(data.productWeightKg).toFixed(2)
                + " kg";
        container.appendChild(summary);

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const thead = document.createElement("thead");
        thead.className = "thead-dark";

        const headerRow = document.createElement("tr");

        [
            "Energy source",
            "Embodied carbon (kg CO₂eq)",
            "Carbon intensity (kg CO₂eq/kg)",
            "Contribution (%)"
        ].forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");
        const total = Number(data.embodiedCarbonKg);

        data.energySources.forEach(item => {
            const carbon = Number(item.allocatedCarbonKg);
            const share = total > 0 ? carbon / total * 100 : 0;

            const row = document.createElement("tr");

            [
                item.sourceType,
                carbon.toFixed(2),
                Number(item.carbonPerKg).toFixed(4),
                share.toFixed(2) + "%"
            ].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(td);
            });

            tbody.appendChild(row);
        });

        const totalRow = document.createElement("tr");
        totalRow.className = "font-weight-bold";

        [
            "TOTAL",
            total.toFixed(2),
            Number(data.embodiedCarbonPerKg).toFixed(4),
            total > 0 ? "100.00%" : "0.00%"
        ].forEach(value => {
            const td = document.createElement("td");
            td.textContent = value;
            totalRow.appendChild(td);
        });

        tbody.appendChild(totalRow);
        table.appendChild(tbody);
        container.appendChild(table);

        const note = document.createElement("p");
        note.className = "text-muted small mt-2";
        note.textContent =
                "Boundary: production energy only. This is not a complete "
                + "lifecycle embodied carbon footprint. "
                + "The mass allocation uses an explicitly selected prototype "
                + "process, not a verified product-to-process ontology link.";
        container.appendChild(note);
    }



});
