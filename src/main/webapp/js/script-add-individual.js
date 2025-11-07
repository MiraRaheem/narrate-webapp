/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

$(document).ready(function () {
    const baseUrl = "ontologyReaderAjax";
    let cardinalities = {};
    let currentClass = null;
    let numericPropsList = [];
    let enumeratedPropsMap = {}; // { hasGender: ["male", "female"] }
    let existingIndividualsList = [];

    // Load all classes

    //$.getJSON(baseUrl, {type: "default"}, function (classes) {
    // console.log("📚 Loaded classes:", classes);
    /*classes.forEach(cls => {
     $("#classSelect").append(`<option value="${cls}">${cls}</option>`);
     });*/

    //classes.sort((a, b) => a.localeCompare(b)).forEach(cls => {
    //    $("#classSelect").append(`<option value="${cls}">${cls}</option>`);
    // });


    //});

    // Load grouped classes by blueprintClass
    $.getJSON(baseUrl, {type: "classAnnotation", annotation: "blueprintClass"}, function (groupedClasses) {
        console.log("📚 Grouped classes by blueprintClass:", groupedClasses);

        const $select = $("#classSelect");
        $select.empty();

        // 🔸 Add placeholder first
        $select.append(`<option value="" disabled selected style="color: #999;">Select blueprint</option>`);

        const sortedBlueprints = Object.keys(groupedClasses).sort();

        sortedBlueprints.forEach(blueprint => {
            // Header: blueprint name (not selectable)
            //$select.append(`<option disabled>🔹 ${blueprint}</option>`);
            $select.append(`<option disabled>🔹 𝐁𝐋𝐔𝐄𝐏𝐑𝐈𝐍𝐓: ${blueprint}</option>`);

            const sortedClasses = groupedClasses[blueprint].sort();

            // First: build a reverse map to count how many blueprints each class appears in
            const classCounts = {};
            Object.values(groupedClasses).forEach(classList => {
                classList.forEach(cls => {
                    classCounts[cls] = (classCounts[cls] || 0) + 1;
                });
            });

            sortedClasses.forEach(cls => {
                const coreName = blueprint.replace(/Blueprint$/, "").toLowerCase();
                const isMain = cls.toLowerCase() === coreName;
                const isShared = classCounts[cls] > 1;

                let style = isMain ? 'style="font-weight:bold; color:#007bff;"' : "";
                let label = isMain
                        ? `⭐ ${cls}`
                        : isShared
                        ? `  ${cls} ⚡`
                        : `  ${cls}`;

                $select.append(`<option value="${cls}" ${style}>${label}</option>`);
            });
            ;
        });
    });


// On class selection
    $("#classSelect").on("change", function () {
        const selectedClass = $(this).val();
        if (!selectedClass)
            return;

        currentClass = selectedClass;
        $("#dataPropertiesContainer, #objectPropertiesContainer").empty();
        $("#dataPropsHeader, #objectPropsHeader").addClass("d-none");
        $("#addIndividualBtn").prop("disabled", true);

        // Fetch cardinalities
        $.getJSON(baseUrl, {type: "cardinalities", class: selectedClass}, function (card) {
            console.log("📏 Cardinalities:", card);
            cardinalities = card || {};

            // Fetch data & object properties AFTER cardinalities are loaded
            $.when(
                    $.getJSON(baseUrl, {type: "dataPropertiesWithComments", class: selectedClass}),
                    $.getJSON(baseUrl, {type: "objectPropertiesWithComments", class: selectedClass, excludeInverses: false}),
                    $.getJSON(baseUrl, {type: "numericProperties", class: selectedClass}),
                    $.getJSON(baseUrl, {type: "enumeratedProperties", class: selectedClass}),
                    $.getJSON(baseUrl, {type: "dateProperties", class: selectedClass}),
                    $.getJSON(baseUrl, {type: "uriProperties", class: selectedClass}),
                    ).done(function (dpRes, opRes, numRes, enumRes, dateRes, uriRes) {
                const dataProps = dpRes[0];
                const objectProps = opRes[0];
                numericPropsList = numRes[0] || [];
                enumeratedPropsMap = enumRes[0] || [];

                datePropsList = dateRes[0] || [];
                uriPropsList = Array.isArray(uriRes[0]) ? uriRes[0] : [];


                console.log("📋 Data properties:", dataProps);
                console.log("🔗 Object properties:", objectProps);
                console.log("🧮 Numeric data properties:", numericPropsList);
                console.log("🎯 Enumerated data properties:", enumeratedPropsMap);
                console.log("📅 Date properties:", datePropsList);
                console.log("🌐 URI properties:", uriPropsList);


                // Fetch existing individuals for this class
                $.getJSON(baseUrl, {type: "directInstances", className: selectedClass}, function (instances) {
                    existingIndividualsList = instances || [];
                    console.log("🧍 Existing individuals for class:", existingIndividualsList);
                });

                // renderPropertiesTable(dataProps, "data", "#dataPropertiesContainer", "#dataPropsHeader");
                //renderPropertiesTable(objectProps, "object", "#objectPropertiesContainer", "#objectPropsHeader");

                // 🔥 Correct call for data properties
                renderPropertiesTable(dataProps, "data", "#dataPropertiesContainer", "#dataPropsHeader");

                // 🔥 TEMPORARY: for objectProps (still plain object)
                // Convert objectProps into [{name, comment, order: 9999}]
                const objectPropsArray = Object.entries(objectProps || {}).map(([name, comment]) => ({
                        name: name,
                        comment: comment || "",
                        order: 9999
                    }));

                renderPropertiesTable(objectPropsArray, "object", "#objectPropertiesContainer", "#objectPropsHeader");

                $("#addIndividualBtn").prop("disabled", false);
            });

        });
    });


    function renderPropertiesTable(properties, type, containerId, headerId) {
        if (!properties || properties.length === 0)
            return;

        let table = `<table class="table table-bordered">
                    <thead><tr><th>Property</th><th>Value</th><th>Action</th></tr></thead><tbody>`;

        properties.forEach(property => {
            const prop = property.name;
            const comment = property.comment || "";
            const min = getMin(prop);
            const rows = Math.max(min, 1);

            for (let i = 0; i < rows; i++) {
                table += createPropertyRow(prop, type, "", i === 0, comment);
            }
        });

        table += "</tbody></table>";
        $(containerId).html(table);
        $(headerId).removeClass("d-none");

        $(function () {
            $('[data-bs-toggle="tooltip"]').tooltip();
        });
    }


    function createPropertyRow(prop, type, val = "", isFirst = false, comment = "") {

        console.log("🧩 createPropertyRow → prop:", prop, "type:", type);
        console.log("🔍 enumeratedPropsMap[prop]:", enumeratedPropsMap[prop]);
        const min = getMin(prop);
        const max = getMax(prop);
        const canRemove = !isFirst || min === 0;
        let valueField = "";
        if (type === "data") {
            if (enumeratedPropsMap.hasOwnProperty(prop)) {
// Dropdown for enum values
                const options = enumeratedPropsMap[prop]
                        .map(opt => `<option value="${opt}" ${val === opt ? "selected" : ""}>${opt}</option>`)
                        .join("");
                valueField = `<select class="form-control">${options}</select>`;
            } else if (numericPropsList.includes(prop)) {
                valueField = `<input type="number" class="form-control" value="${val}">`;
            } else if (datePropsList.includes(prop)) {
                valueField = `<input type="date" class="form-control" value="${val}">`;
            } else if (uriPropsList.includes(prop)) {
                valueField = `<input type="url" class="form-control" value="${val}">`;
            } else {
                valueField = `<input type="text" class="form-control" value="${val}">`;
            }

        } else {
            const id = `${prop}-${Math.random().toString(36).substring(2)}`;
            valueField = `<select class="form-control object-dropdown" id="${id}" data-prop="${prop}"><option value="">Loading...</option></select>`;
            setTimeout(() => {
                $.getJSON(baseUrl, {type: "instances", relatedClass: prop}, function (instances) {
                    const $dropdown = $(`#${id}`);
                    $dropdown.empty().append(`<option value="">-- Select --</option>`);
                    instances.forEach(inst => {
                        $dropdown.append(`<option value="${inst}">${inst}</option>`);
                    });
                });
            }, 0);
        }

        return `
        <tr data-type="${type}" data-prop="${prop}">
            <td data-bs-toggle="tooltip" data-bs-placement="top" title="${comment}">

            ${prop}
            <small class="text-muted">
                (${getCardinalityHint(min, max)})
            </small>
            </td>
            <td>${valueField}</td>
            <td>
                <button type="button" class="btn btn-sm btn-success add-row">+</button>
                ${canRemove ? `<button type="button" class="btn btn-sm btn-danger remove-row">✖</button>` : ""}
            </td>
        </tr>`;
    }


    $(document).on("click", ".add-row", function () {
        const row = $(this).closest("tr");
        const type = row.data("type");
        const prop = row.data("prop");
        const count = $(`tr[data-type='${type}'][data-prop='${prop}']`).length;
        const max = getMax(prop);
        if (!max || count < max) {
            row.after(createPropertyRow(prop, type));
        } else {
            Swal.fire("⚠️ Max Limit", `Property '${prop}' allows at most ${max} value(s).`, "warning");
        }
    });
    $(document).on("click", ".remove-row", function () {
        const row = $(this).closest("tr");
        const type = row.data("type");
        const prop = row.data("prop");
        const count = $(`tr[data-type='${type}'][data-prop='${prop}']`).length;
        const min = getMin(prop);
        if (count > min) {
            row.remove();
        } else {
            Swal.fire("⚠️ Minimum Limit", `Property '${prop}' requires at least ${min} value(s).`, "warning");
        }
    });
    $("#addIndividualBtn").on("click", function () {
        const individualName = $("#individualName").val().trim();
        if (!currentClass || !individualName) {
            Swal.fire("Error", "Please select a class and enter an individual name.", "error");
            return;
        }

        const dataProperties = [];
        const objectProperties = [];
        const propCounts = {};
        $("tr[data-type='data']").each(function () {
            const prop = $(this).data("prop");
            //const val = $(this).find("input").val().trim();
            let input = $(this).find("input, select");
            let val = input.length > 0 ? input.val() : "";
            val = typeof val === "string" ? val.trim() : val;

            if (!propCounts[prop])
                propCounts[prop] = 0;
            if (val) {
                propCounts[prop]++;
                dataProperties.push({property: prop, value: val});
            }
        });
        $("tr[data-type='object']").each(function () {
            const prop = $(this).data("prop");
            const val = $(this).find("select").val();
            if (!propCounts[prop])
                propCounts[prop] = 0;
            if (val) {
                propCounts[prop]++;
                objectProperties.push({property: prop, value: val});
            }
        });
        // Validate cardinality
        for (const prop in cardinalities) {
            const min = getMin(prop);
            const max = getMax(prop);
            const count = propCounts[prop] || 0;
            if (count < min) {
                Swal.fire("❌ Error", `Property '${prop}' requires at least ${min} value(s).`, "error");
                return;
            }
            if (max !== null && count > max) {
                Swal.fire("❌ Error", `Property '${prop}' allows at most ${max} value(s).`, "error");
                return;
            }
        }

        const enteredName = $("#individualName").val().trim();

        if (existingIndividualsList.includes(enteredName)) {
            Swal.fire({
                icon: "error",
                title: "❌ Duplicate Name",
                text: `An individual with the name "${enteredName}" already exists. Please choose a different name.`,
                confirmButtonColor: "#dc3545"
            });
            return;
        }

        const payload = {
            className: currentClass,
            individualName,
            dataProperties,
            objectProperties
        };
        console.log("🚀 Submitting payload:", payload);
        $.ajax({
            type: "POST",
            url: "AddIndividualServlet2",
            contentType: "application/json",
            data: JSON.stringify(payload),
            success: function (response) {
                Swal.fire("✅ Success", response || "Individual added successfully!", "success")
                        .then(() => location.reload());
            },
            error: function () {
                Swal.fire("❌ Error", "Failed to add individual.", "error");
            }
        });
    });
    function getMin(prop) {
        return cardinalities[prop]?.min ?? 0;
    }

    function getMax(prop) {
        return cardinalities[prop]?.max ?? null;
    }

    function getCardinalityHint(min, max) {
        if (min === undefined && max === undefined)
            return "0..n";
        if (min !== undefined && max !== null)
            return `${min}..${max}`;
        if (min !== undefined && max === null)
            return `${min}..n`;
        if (min === undefined && max !== null)
            return `0..${max}`;
        return "0..n";
        return;
    }

});
