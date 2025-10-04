/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

$(document).ready(function () {
    const baseUrl = "ontologyReaderAjax";
    let cardinalities = {};
    let numericPropsList = [];
    let enumeratedPropsMap = {};

    // 🔽 PLACE THIS BLOCK RIGHT HERE 👇
    $("#fuzzyMatchCheckbox").on("change", function () {
        $("#similarityThresholdContainer").toggle(this.checked);
    });

    $("#similarityThreshold").on("input", function () {
        $("#similarityValueLabel").text(this.value + "%");
    });


    console.log("📦 Query script loaded");

    // Load classes into dropdown
    $.getJSON(baseUrl, {type: "default"}, function (classes) {
        console.log("📚 Loaded classes:", classes);
        /*classes.forEach(cls => {
         $("#classSelect").append(`<option value="${cls}">${cls}</option>`);
         });*/

        classes.sort((a, b) => a.localeCompare(b)).forEach(cls => {
            $("#classSelect").append(`<option value="${cls}">${cls}</option>`);
        });
    });

    // On class selection
    $("#classSelect").on("change", function () {
        const selectedClass = $(this).val();
        if (!selectedClass)
            return;

        console.log("🏷️ Class selected:", selectedClass);

        // Reset UI
        $("#dataPropertiesContainer").empty();
        $("#objectPropertiesContainer").empty();
        $("#resultsContainer").empty();
        $("#dataPropsHeader, #objectPropsHeader, #resultsHeader").addClass("d-none");
        $("#searchBtn").prop("disabled", true);

        // Step 1: Fetch cardinalities
        $.getJSON(baseUrl, {type: "cardinalities", class: selectedClass}, function (card) {
            console.log("📏 Cardinalities:", card);
            cardinalities = card || {};

            // Step 2: Fetch numeric properties only after cardinalities
            $.getJSON(baseUrl, {type: "numericProperties", class: selectedClass}, function (numProps) {
                console.log("🧮 Numeric data properties:", numProps);
                numericPropsList = numProps || [];

                // Step 3: Fetch data & object properties only after numericPropsList is ready
                $.when(
                        $.getJSON(baseUrl, {type: "dataPropertiesWithComments", class: selectedClass}),
                        $.getJSON(baseUrl, {type: "objectPropertiesWithComments", class: selectedClass, excludeInverses: false}),
                        $.getJSON(baseUrl, {type: "enumeratedProperties", class: selectedClass})
                        ).done(function (dpRes, opRes, enumRes) {
                    enumeratedPropsMap = enumRes[0] || {};
                    console.log("🎯 Enumerated data properties:", enumeratedPropsMap);

                    const dataProps = dpRes[0];
                    const objectProps = opRes[0];

                    console.log("🧮 Data properties:", dataProps);
                    console.log("🔗 Object properties:", objectProps);

                    renderPropertiesTable(dataProps, "data", "#dataPropertiesContainer", "#dataPropsHeader");
                    renderPropertiesTable(objectProps, "object", "#objectPropertiesContainer", "#objectPropsHeader");
                    $("#searchBtn").prop("disabled", false);
                });
            });
        });
    });


    function renderPropertiesTable(properties, type, containerId, headerId) {
        if (!properties || properties.length === 0)
            return;

        console.log(`📋 Rendering ${type} table for properties:`, properties);

        let table = `<table class="table table-bordered"><thead><tr><th>Property</th><th>Value</th><th>Action</th></tr></thead><tbody>`;

        //let rowBlocks = new Array(individuals.length);

        if (Array.isArray(properties)) {
            // ✅ dataPropertiesWithComments → already array
            properties.forEach(property => {
                const prop = property.name;
                const comment = property.comment || "";
                table += createPropertyRow(prop, type, "", true, comment);
            });
        } else {
            // ✅ objectPropertiesWithComments → convert object to array
            Object.entries(properties).forEach(([prop, comment]) => {
                table += createPropertyRow(prop, type, "", true, comment);
            });
        }


        table += "</tbody></table>";
        $(containerId).html(table);
        $(headerId).removeClass("d-none");
        document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => new bootstrap.Tooltip(el));

    }

    function createPropertyRow(prop, type, val = "", isFirst = false, comment = "") {
        const min = getMin(prop);

        let max = getMax(prop);


        const canRemove = !isFirst || (min === 0);
        let valueField = "";

        if (type === "data" && enumeratedPropsMap.hasOwnProperty(prop)) {
            const options = [`<option value="">-- Select --</option>`].concat(
                    enumeratedPropsMap[prop].map(opt =>
                    `<option value="${opt}" ${val === opt ? "selected" : ""}>${opt}</option>`
            )
                    ).join("");
            //valueField = `<select class="form-control">${options</select>`;
            valueField = `<select class="form-control enum-dropdown">${options}</select>`; // ✅ Add `enum-dropdown`
        } else if (type === "data" && numericPropsList.includes(prop)) {
            // Numeric comparison UI
            valueField = `
            <div class="form-inline">
                <select class="form-control mr-2 comp-op" data-prop="${prop}">
                    <option value="=">=</option>
                    <option value=">">&gt;</option>
                    <option value=">=">&ge;</option>
                    <option value="<">&lt;</option>
                    <option value="<=">&le;</option>
                </select>
                <input type="number" class="form-control value-box" placeholder="Value">
            </div>
        `;
        } else if (type === "data") {
            // Normal text input for string-type data
            //valueField = `<input type="text" class="form-control" value="${val}">`;
            valueField = `<input type="text" class="form-control value-box" value="${val}">`;

        } else {
            // Object property dropdown (as before)
            const id = `${prop}-${Math.random().toString(36).substring(2)}`;
            valueField = `<select class="form-control object-dropdown" id="${id}" data-prop="${prop}"><option value="">Loading...</option></select>`;
            setTimeout(() => {
                $.getJSON(baseUrl, {type: "instances", relatedClass: prop}, function (instances) {
                    console.log("📥 Populating dropdown for", prop, ":", instances);
                    const $dropdown = $(`#${id}`);
                    $dropdown.empty().append(`<option value="">-- Select --</option>`);
                    instances.forEach(inst => {
                        $dropdown.append(`<option value="${inst}" ${inst === val ? "selected" : ""}>${inst}</option>`);
                    });
                });
            }, 0);
        }

        return `
        <tr data-type="${type}" data-prop="${prop}">
            <td data-bs-toggle="tooltip" data-bs-placement="top" title="${comment}">${prop}</td>


            <td>${valueField}</td>
          <td>
                ${canAdd(prop) ? '<button class="btn btn-sm btn-success add-row">+</button>' : ""}
                ${canRemove ? '<button class="btn btn-sm btn-danger remove-row">✖</button>' : ""}
           </td>


        </tr>
    `;
    }


    // Add/remove row events
    $(document).on("click", ".add-row", function () {
        const row = $(this).closest("tr");
        const type = row.data("type");
        const prop = row.data("prop");
        console.log("➕ Add row for", prop);
        const count = $(`tr[data-type='${type}'][data-prop='${prop}']`).length;

        //const max = getMax(prop);
        const maxRaw = getMax(prop);
        let max = maxRaw;

// ⛳ Ignore max cardinality for numeric properties (only in Query use case)
        if (type === "data" && numericPropsList.includes(prop)) {
            console.log("⛳ Ignoring max cardinality for numeric property:", prop);
            max = null;
        }


        if (!max || count < max) {
            const newRow = createPropertyRow(prop, type);
            row.after(newRow);
            console.log("🚧 Generated Row HTML:", newRow);
        }
    });


    $(document).on("click", ".remove-row", function () {
        const row = $(this).closest("tr");
        const prop = row.data("prop");
        const type = row.data("type");
        const count = $(`tr[data-type='${type}'][data-prop='${prop}']`).length;
        const min = getMin(prop);
        console.log("➖ Remove row for", prop);
        if (count > min)
            row.remove();
    });

    // Handle Search
    $("#searchBtn").on("click", function () {
        const className = $("#classSelect").val();
        const dataProperties = {};
        const objectProperties = {};

        const includeFuzzy = $("#fuzzyMatchCheckbox").is(":checked");
        const fuzzyThreshold = parseFloat($("#similarityThreshold").val());


        // ✅ Collect data inputs (including numeric comparisons and enumerated dropdowns)
        $("tr[data-type='data']").each(function () {
            const prop = $(this).data("prop");

            if (numericPropsList.includes(prop)) {
                const operator = $(this).find("select.comp-op").val();
                const value = $(this).find("input.value-box").val();

                if (operator && value !== "") {
                    if (!dataProperties[prop])
                        dataProperties[prop] = [];
                    dataProperties[prop].push(operator + value); // e.g., >=30
                }
            } else {
                let val;
                const input = $(this).find("input.value-box");
                const select = $(this).find("select.enum-dropdown");

                if (select.length > 0) {
                    val = select.val();  // dropdown for enumerated properties
                } else if (input.length > 0) {
                    val = input.val();
                }

                if (val !== undefined && val.trim() !== "") {
                    if (!dataProperties[prop])
                        dataProperties[prop] = [];
                    dataProperties[prop].push(val.trim());
                }
            }
        });



        // Collect object dropdowns
        $("tr[data-type='object']").each(function () {
            const prop = $(this).data("prop");
            const val = $(this).find("select").val();
            if (val) {
                if (!objectProperties[prop])
                    objectProperties[prop] = [];
                objectProperties[prop].push(val);
            }
        });

        const payload = {
            className,
            dataProperties,
            objectProperties,
            includeFuzzy: $("#fuzzyMatchCheckbox").is(":checked"),
            fuzzyThreshold: parseInt($("#similarityThreshold").val()) / 100 || 0
        };

        console.log("🚀 Submitting query:", payload);
        console.log("📤 Fuzzy Threshold (normalized):", payload.fuzzyThreshold);


        // Send request
        $.ajax({
            type: "POST",
            url: "QueryIndividualsServlet",
            contentType: "application/json",
            data: JSON.stringify(payload),
            success: function (response) {
                console.log("✅ Query result:", response);
                //displayResults(response.matchedIndividuals, response.similarityNotes);
                $.getJSON("ontologyReaderAjax", {type: "objectProperties", class: $("#classSelect").val()})
                        .done(function (objectProps) {
                            displayResults(response.matchedIndividuals, response.similarityNotes, objectProps, className);


                        });


            },
            error: function () {
                console.error("❌ Failed to retrieve individuals.");
                Swal.fire("❌ Error", "Failed to retrieve individuals.", "error");
            }
        });
    });



    function displayResults(individuals, similarityNotes = {}, objectProperties = [], className = "") {

        
         // const objectPropKeys = Object.keys(objectProperties);
         const objectPropKeys = Array.isArray(objectProperties) ? objectProperties : Object.keys(objectProperties);

        if (!individuals || individuals.length === 0) {
            $("#resultsContainer").html("<p>No matching individuals found.</p>");
            return;
        }

        let table = `
            <h4 id="resultsHeader">Matching Individuals</h4>
            <table class="table table-bordered">
             <thead>
                <tr>
                    <th>#</th>
                    <th>Individual</th>
                </tr>
              </thead>
             <tbody>`;


        let pending = individuals.length;
        individuals.sort(); // ✅ ADD THIS LINE HERE
        let rowBlocks = new Array(individuals.length); // ✅ declare array here


        individuals.forEach((individual, index) => {
            $.getJSON("ontologyReaderAjax", {type: "individualDetails", individual: individual}, function (details) {
                //let detailHtml = "<ul class='mb-0'>";

                const dataProps = [];
                const objectProps = [];
                Object.entries(details).forEach(([prop, value]) => {
                  
                    if (objectPropKeys.includes(prop)) {
                        objectProps.push({prop, value});
                    } else {
                        const order = Number.MAX_SAFE_INTEGER;  // fallback if no dataProperties
                        dataProps.push({prop, value, order});
                }
                });

                // Sort data properties by order
                dataProps.sort((a, b) => a.order - b.order);
                // Merge ordered data props + object props
                const allProps = [...dataProps, ...objectProps];
                let detailHtml = "<ul class='mb-0'>";
                //const objectPropKeys = Object.keys(objectProperties);

                allProps.forEach(entry => {
                    let cleanValue = entry.value.includes("^^")
                            ? entry.value.split("^^")[0].replace(/^"|"$/g, "")
                            : entry.value.includes("#")
                            ? entry.value.split("#").pop()
                            : entry.value;

                    if (objectPropKeys.includes(entry.prop)) {
                        // expandable object property
                        detailHtml += `
            <li>
                <strong>${entry.prop}</strong>: 
                <a href="#" class="linked-individual" data-individual="${cleanValue}">${cleanValue} [+]</a>
                <div id="linked-details-${cleanValue}" class="linked-details bg-primary-subtle border rounded p-2" style="display:none; margin-left:20px;"></div>
            </li>`;
                    } else {
                        // normal data property
                        detailHtml += `<li><strong>${entry.prop}</strong>: ${cleanValue}</li>`;
                    }
                });

                detailHtml += "</ul>";
                rowBlocks[index] = `
                    <tr id="main-${individual}">
                    <td>${index + 1}</td>
                    <td>
                    <button class="expand-btn btn btn-sm btn-primary" data-individual="${individual}">+</button>
                    ${individual}
                    </td>
                    </tr>
                    <tr id="details-${individual}" class="details-row" style="display:none;">
                        <td colspan="2">${detailHtml}</td>
                    </tr>`;
                pending--;
                if (pending === 0) {
                    table += rowBlocks.join("");
                    table += "</tbody></table>";
                    $("#resultsContainer").html(table);
                    $("#resultsHeader").removeClass("d-none");
                   // const objectPropKeys = Object.keys(objectProperties);


                    // Attach expand/collapse handler
                    $(".expand-btn").on("click", function () {
                        const indiv = $(this).data("individual");
                        const detailsRow = $(`#details-${indiv}`);
                        const btn = $(this);
                        if (detailsRow.is(":visible")) {
                            detailsRow.hide();
                            btn.text("+");
                        } else {
                            detailsRow.show();
                            btn.text("-");
                        }
                    });
                    $(".linked-individual").on("click", function (e) {
                        e.preventDefault();
                        //const meta = dataProperties.find(p => p.name === prop);

                        const indiv = $(this).data("individual");
                        const container = $(`#linked-details-${indiv}`);
                        const link = $(this);
                        if (container.is(":visible")) {
                            container.hide();
                            link.text(`${indiv} [+]`);
                            return;
                        }

                        link.text(`${indiv} [-]`);
                        container.html("Loading...").show();
                        $.when(
                                $.getJSON("ontologyReaderAjax", {type: "dataPropertyValues", individual: indiv}),
                                $.getJSON("ontologyReaderAjax", {type: "objectPropertyValues", individual: indiv}),
                                $.getJSON("ontologyReaderAjax", {type: "dataPropertiesWithComments", class: className})
                                ).done(function (dataRes, objRes, dpMetaRes) {
                            const dataValues = dataRes[0] || {};
                            const objValues = objRes[0] || {};
                            const dataMeta = dpMetaRes[0] || [];
                            // prepare arrays
                            const dataPropsArr = [];
                            const objectPropsArr = [];
                            Object.entries(dataValues).forEach(([prop, vals]) => {
                                const meta = dataMeta.find(p => p.name === prop);
                                const order = meta ? meta.order : Number.MAX_SAFE_INTEGER;
                                vals.forEach(val => {
                                    dataPropsArr.push({prop, val, order});
                                });
                            });
                            Object.entries(objValues).forEach(([prop, vals]) => {
                                vals.forEach(val => {
                                    objectPropsArr.push({prop, val});
                                });
                            });
                            // sort dataProps by order
                            dataPropsArr.sort((a, b) => a.order - b.order);
                            let html = `<ul>`;
                            dataPropsArr.forEach(entry => {
                                html += `<li><strong>${entry.prop}</strong>: ${entry.val}</li>`;
                            });
                            objectPropsArr.forEach(entry => {
                                html += `<li><strong>${entry.prop}</strong>: ${entry.val}</li>`;
                            });
                            html += `</ul>`;
                            container.html(html);
                        });
                    });
                }
            }).fail(() => {
                console.warn(`❌ Failed to fetch details for ${individual}`);
                pending--;
                if (pending === 0) {
                    table += "</tbody></table>";
                    $("#resultsContainer").html(table);
                    $("#resultsHeader").removeClass("d-none");
                }
            });
        });
    }


    function getMin(prop) {
        return cardinalities[prop]?.min ?? 0;
    }

    function getMax(prop) {
        return cardinalities[prop]?.max ?? null;
    }


    function canAdd(prop) {
        const max = getMax(prop);
        const count = $(`tr[data-prop='${prop}']`).length;
        return !max || count < max;
    }

});
