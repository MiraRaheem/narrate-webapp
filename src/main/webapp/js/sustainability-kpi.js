/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

document.addEventListener("DOMContentLoaded", function () {
    const productSelect = document.getElementById("productSelect");

    // 1. Populate the product list
    fetch("ontologyReaderAjax?type=individualsByClass&className=Product")
            .then(response => response.json())
            .then(data => {
                console.log("Fetched Renewable Content data:", data);

                if (data.individuals && Array.isArray(data.individuals)) {
                    productSelect.innerHTML = '<option value="">-- Choose a Product --</option>';
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

    // 2. Handle KPI button clicks
    document.querySelectorAll(".kpi-btn").forEach(button => {

        button.addEventListener("click", function () {
            const selectedProduct = productSelect.value;
            const kpiType = this.getAttribute("data-kpi");

            if (!selectedProduct) {
                alert("Please select a product first.");
                return;
            }

            console.log(`KPI button clicked: ${kpiType} for product ${selectedProduct}`);

            if (kpiType === "materialIntensity") {
                console.log("Fetching Material Intensity for:", selectedProduct); // ✅ Debug

                fetch(`ontologyReaderAjax?type=materialIntensity&individual=${selectedProduct}`)
                        .then(response => {
                            console.log("Raw response:", response); // ✅ Debug
                            return response.json();
                        })
                        .then(data => {
                            console.log("Parsed data:", data); // ✅ Debug

                            if (data && Array.isArray(data.materialIntensities)) {
                                const parsedData = {};
                                data.materialIntensities.forEach(item => {
                                    parsedData[item.material] = item.intensity;
                                });

                                renderMaterialIntensityChart(parsedData);
                                renderMaterialIntensityTable(parsedData);
                            } else {
                                alert("No Material Intensity data found.");
                            }
                        })
                        .catch(err => {
                            console.error("Error fetching Material Intensity:", err);
                        });
            }
            if (kpiType === "renewableContent") {
                fetch(`ontologyReaderAjax?type=renewableContent&individual=${selectedProduct}`)
                        .then(response => response.json())
                        .then(data => {
                            if (data && Array.isArray(data.renewableContents)) {
                                renderRenewableContentChart(data.renewableContents);
                                //renderRenewableContentTable(data.renewableContents);
                                renderRenewableContentTable(data.renewableContents, data.productWeight);

                            } else {
                                console.warn("No renewable content data found.");
                            }
                        })
                        .catch(err => {
                            console.error("Error fetching Renewable Content:", err);
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


    // 4. Render Data Table
    function renderMaterialIntensityTable(data) {
        // Look for or create a container for the table
        let tableContainer = document.getElementById("materialIntensityTable");

        if (!tableContainer) {
            tableContainer = document.createElement("div");
            tableContainer.setAttribute("id", "materialIntensityTable");

            // Insert table below the canvas
            const canvas = document.getElementById("materialKpiChart");
            canvas.parentNode.insertBefore(tableContainer, canvas.nextSibling);
        }

        // Clear previous table if any
        tableContainer.innerHTML = "";

        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const rows = Object.entries(data)
                .map(([mat, val]) => `<tr><td>${mat}</td><td>${val.toFixed(4)}</td></tr>`)
                .join("");

        table.innerHTML = `
        <thead class="thead-dark">
            <tr><th>Material</th><th>Intensity</th></tr>
        </thead>
        <tbody>${rows}</tbody>
    `;

        tableContainer.appendChild(table);
    }

    function renderRenewableContentChart(data) {
        const canvas = document.getElementById("materialKpiChart");

        // Clear previous chart instance if exists
        if (window.materialChartInstance) {
            window.materialChartInstance.destroy();
        }

        const ctx = canvas.getContext("2d");

        const labels = data.map(item => item.material);
        const values = data.map(item => item.renewableFraction * 100); // fraction → %

        window.materialChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                        label: '% Renewable Content',
                        data: values,
                        backgroundColor: '#28a745'
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
                            text: 'Percentage (%)'
                        }
                    }
                }
            }
        });
    }


    function renderRenewableContentTable(data, productWeight) {
        console.log("Rendering Renewable Table with data:", data);

        // 1. Find or create the container after the canvas
        let tableContainer = document.getElementById("renewableContentTable");

        if (!tableContainer) {
            tableContainer = document.createElement("div");
            tableContainer.setAttribute("id", "renewableContentTable");

            // Insert it below the canvas like Material Intensity
            const canvas = document.getElementById("materialKpiChart");
            canvas.parentNode.insertBefore(tableContainer, canvas.nextSibling);
        }

        // 2. Clear previous table
        tableContainer.innerHTML = "";

        // 3. Add product weight heading
        const weightHeading = document.createElement("h5");
        weightHeading.className = "font-weight-bold mt-3";
        weightHeading.textContent = `Total Product Weight: ${productWeight.toFixed(2)} kg`;
        tableContainer.appendChild(weightHeading);

        // 4. Build the table
        const table = document.createElement("table");
        table.className = "table table-striped table-bordered mt-3";

        const rows = data
                .map(item => `
            <tr>
                <td>${item.material || "Unknown"}</td>
                <td>${item.weight?.toFixed(2) || "N/A"}</td>
                <td>${(item.renewableFraction * 100).toFixed(2)}%</td>
                <td>${(item.renewablePercentage * 100).toFixed(2)}%</td>
            </tr>
        `)
                .join("");

        table.innerHTML = `
        <thead class="thead-dark">
            <tr>
                <th>Material</th>
                <th>Weight (kg)</th>
                <th>Renewable Fraction (%)</th>
                <th>Renewable %</th>
            </tr>
        </thead>
        <tbody>${rows}</tbody>
    `;

        // 5. Append table to container
        tableContainer.appendChild(table);
    }





});

