/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

$(document).ready(function () {
    const baseUrl = "ontologyReaderAjax";
    let currentClass = null;
    let selectedIndividuals = [];
    // Load classes into dropdown
    $.getJSON(baseUrl, {type: "default"}, function (classes) {
        console.log("📚 Loaded classes:", classes);
        const $dropdown = $("#classSelect");
        $dropdown.empty().append(`<option value="">-- Select a Class --</option>`);
        classes.forEach(cls => {
            $dropdown.append(`<option value="${cls}">${cls}</option>`);
        });
    });
    // When a class is selected, load its individuals
    $("#classSelect").on("change", function () {
        const selectedClass = $(this).val();
        if (!selectedClass) {
            $("#individualsSelect").prop("disabled", true).empty();
            $("#compareBtn").prop("disabled", true);
            currentClass = null;
            return;
        }

        currentClass = selectedClass;
        $("#individualsSelect").prop("disabled", true).empty().append(`<option>Loading...</option>`);
        $.getJSON(baseUrl, {
            type: "directInstances",
            className: selectedClass,
            t: Date.now()
        }, function (individuals) {
            const $indiv = $("#individualsSelect");
            $indiv.empty();
            individuals.sort((a, b) => a.localeCompare(b)); // Sort alphabetically
            individuals.forEach(ind => {
                $indiv.append(`<option value="${ind}">${ind}</option>`);
            });
            $indiv.prop("disabled", false);
            $("#compareBtn").prop("disabled", true); // Wait until enough are selected
        });
    });
// Enable compare button when 2+ individuals are selected
    $("#individualsSelect").on("change", function () {
        selectedIndividuals = $(this).val() || [];
        $("#compareBtn").prop("disabled", selectedIndividuals.length < 2);
    });
// Handle Compare Button Click
    $("#compareBtn").on("click", function () {
        const selected = $("#individualsSelect").val();
        if (!currentClass || !selected || selected.length < 2) {
            Swal.fire("⚠️ Please select at least two individuals to compare.");
            return;
        }

        console.log("🧪 Comparing:", selected);
        const payload = {
            action: "compareIndividuals",
            className: currentClass,
            individuals: selected
        };
        $.ajax({
            type: "POST",
            url: baseUrl,
            contentType: "application/json",
            data: JSON.stringify(payload),
            success: function (response) {
                console.log("✅ Comparison result:", response);
                if (response.comparisonData) {
                    window.lastComparisonData = response.comparisonData; // ✅ Store globally for CSV export
                    renderComparisonTable(response.comparisonData);
                    // ✅ Load and render summary of numeric stats
                    $.ajax({
                        type: "POST",
                        url: baseUrl,
                        contentType: "application/json",
                        data: JSON.stringify({
                            action: "classNumericStats",
                            className: currentClass
                        }),
                        success: function (allNumericValues) {
                            console.log("✅ classNumericValues API response:", allNumericValues);
                            renderSummary(allNumericValues);
                        },
                        error: function () {
                            console.warn("❌ Failed to fetch numeric summary stats.");
                        }
                    });



                } else {
                    $("#comparisonContainer").html("<p>No comparison data available.</p>");
                }
            }
            ,
            error: function () {
                Swal.fire("❌ Failed to compare individuals.");
            }
        });
    });

    $("#exportCsvBtn").on("click", function () {
        if (window.lastComparisonData) {
            exportComparisonToCSV(window.lastComparisonData);
        }
    });



    function renderComparisonTable(response) {
        const container = $("#comparisonContainer");
        container.empty();

        if (!response || Object.keys(response).length === 0) {
            container.html(`<p>No comparison data available.</p>`);
            return;
        }

        const individuals = Object.keys(response);
        const allProperties = new Set();

        // Collect all unique property names
        individuals.forEach(ind => {
            const all = response[ind];
            if (all.data)
                Object.keys(all.data).forEach(p => allProperties.add(p));
            if (all.object)
                Object.keys(all.object).forEach(p => allProperties.add(p));
        });

        let html = `
        <table class="table table-bordered table-striped">
            <thead class="table-success">
                <tr>
                    <th class="text-nowrap" style="width: 200px;">Property</th>
                    ${individuals.map(ind => `<th class="text-center" style="width: 200px;">${ind}</th>`).join("")}
                </tr>
            </thead>
            <tbody>
    `;

        allProperties.forEach(prop => {
            html += `<tr><td class="bg-light text-dark font-weight-bold align-middle">${prop}</td>`;

            // Gather values for each individual for this property (deduplicate for rdf:type)
            const valuesForProp = individuals.map(ind => {
                const all = response[ind];
                let valList = all.data?.[prop] || all.object?.[prop] || [];

                if (!Array.isArray(valList))
                    valList = [valList];

                // ✂️ Remove duplicates only for "type"
                if (prop.toLowerCase() === "type") {
                    const seen = new Set();
                    valList = valList.filter(v => {
                        const norm = v?.toLowerCase();
                        if (!norm || seen.has(norm))
                            return false;
                        seen.add(norm);
                        return true;
                    });
                }


                return valList;
            });


            // Check if values differ
            const uniqueValues = new Set(valuesForProp.flat().map(String));
            const isTypeProperty = prop.toLowerCase() === "type";
            const isDifferent = !isTypeProperty && uniqueValues.size > 1;


            individuals.forEach((ind, i) => {
                let val = valuesForProp[i];

                if (Array.isArray(val)) {
                    val = val.map(v => `<div class="mb-1">${v}</div>`).join("");
                }

                html += `<td class="align-top ${isDifferent ? 'table-warning' : ''}">${val || ""}</td>`;
            });

            html += `</tr>`;
        });

        html += `</tbody></table>`;
        html += `<div id="summaryContainer" class="mt-4"></div>`; // ✅ Add summary placeholder

// 🔄 Count differences
        let differingCount = 0;
        allProperties.forEach(prop => {
            if (prop.toLowerCase() === "type")
                return; // ✅ Skip the "type" property

            const values = individuals.map(ind => {
                const all = response[ind];
                const v = all.data?.[prop] || all.object?.[prop] || [];
                return Array.isArray(v) ? v.join("|") : v;
            });

            const uniqueVals = new Set(values);
            if (uniqueVals.size > 1)
                differingCount++;
        });


// 🔢 Summary below table
        $("#summaryInfo").text(`Compared ${allProperties.size} properties — ${differingCount} differ.`);

        container.html(`<div class="table-responsive">${html}</div>`);
    }

// ⬇️ Export Comparison Table as CSV
    function exportComparisonToCSV(data) {
        const individuals = Object.keys(data);
        const allProperties = new Set();

        individuals.forEach(ind => {
            const all = data[ind];
            if (all.data)
                Object.keys(all.data).forEach(p => allProperties.add(p));
            if (all.object)
                Object.keys(all.object).forEach(p => allProperties.add(p));
        });

        const rows = [["Property", ...individuals]];

        allProperties.forEach(prop => {
            const row = [prop];

            individuals.forEach(ind => {
                const all = data[ind];
                const values = all.data?.[prop] || all.object?.[prop] || [];
                row.push(Array.isArray(values) ? values.join(" | ") : values);
            });

            rows.push(row);
        });

        // Convert to CSV string
        const csvContent = rows.map(r => r.map(v => `"${v}"`).join(",")).join("\n");
        const blob = new Blob([csvContent], {type: "text/csv;charset=utf-8;"});

        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = "comparison.csv";
        link.click();
    }

    function renderSummary(numericValues) {
        if (!numericValues || Object.keys(numericValues).length === 0) {
            $("#summaryContainer").html(`<p><em>No numeric statistics available.</em></p>`);
            return;
        }

        let summaryHtml = `<h5 class="mt-4">📊 Summary Statistics (All Individuals of ${currentClass})</h5>`;
        summaryHtml += `<table class="table table-sm table-bordered">
        <thead><tr><th>Property</th><th>Mean</th><th>Std Dev</th></tr></thead><tbody>`;

        console.log("📊 Raw numeric values received for summary:", numericValues);

        for (const prop in numericValues) {
            // Convert values to numbers
            const raw = Array.isArray(numericValues[prop])
                    ? numericValues[prop]
                    : [numericValues[prop]];

            console.log(`🔢 Processing property: ${prop} | Raw values:`, raw);


            const numbers = raw.map(v => parseFloat(v)).filter(v => !isNaN(v));
            console.log(`✅ Cleaned numbers for ${prop}:`, numbers);

            const n = numbers.length;
            if (n === 0)
                continue;

            const mean = numbers.reduce((sum, v) => sum + v, 0) / n;
            const variance = numbers.reduce((sum, v) => sum + Math.pow(v - mean, 2), 0) / n;
            const stddev = Math.sqrt(variance);

            summaryHtml += `<tr>
            <td>${prop}</td>
            <td>${mean.toFixed(2)}</td>
            <td>${stddev.toFixed(2)}</td>
        </tr>`;
        }

        summaryHtml += `</tbody></table>`;
        $("#summaryContainer").html(summaryHtml);
    }



});




