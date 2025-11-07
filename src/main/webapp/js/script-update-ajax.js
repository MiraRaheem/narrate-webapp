$(document).ready(function () {
    const baseUrl = "ontologyReaderAjax";
    let cardinalities = {};
    let currentClass = null;
    let currentIndividual = null;
    let numericPropsList = [];
    let enumeratedPropsMap = {};
    let datePropsList = [];
    let uriPropsList = [];

    $.getJSON(baseUrl, {type: "default"}, function (classes) {
        /*classes.forEach(cls => {
         $("#classSelect").append(`<option value="${cls}">${cls}</option>`);
         });*/

        //classes.sort((a, b) => a.localeCompare(b)).forEach(cls => {
        //   $("#classSelect").append(`<option value="${cls}">${cls}</option>`);
        //});

        // Request grouped classes by blueprintClass
        $.getJSON(baseUrl, {type: "classAnnotation", annotation: "blueprintClass"}, function (groupedClasses) {
            console.log("📦 Loaded grouped classes:", groupedClasses);

            const $select = $("#classSelect");
            $select.empty();

            // Placeholder
            $select.append(`<option value="" disabled selected style="color:#999;">Select blueprint</option>`);

            // Count appearances to detect shared classes
            const classCounts = {};
            Object.values(groupedClasses).forEach(classList => {
                classList.forEach(cls => {
                    classCounts[cls] = (classCounts[cls] || 0) + 1;
                });
            });

            // Sort and populate grouped options
            Object.keys(groupedClasses).sort().forEach(blueprint => {
                
                    // 🔹 Insert blueprint header
        $select.append(`<option disabled>🔹 𝐁𝐋𝐔𝐄𝐏𝐑𝐈𝐍𝐓: ${blueprint}</option>`);
        
                const sortedClasses = groupedClasses[blueprint].sort();

                sortedClasses.forEach(cls => {
                    const coreName = blueprint.replace(/Blueprint$/, "").toLowerCase();
                    const isMain = cls.toLowerCase() === coreName;
                    const isShared = classCounts[cls] > 1;

                    const style = isMain ? 'style="font-weight:bold; color:#007bff;"' : "";
                    const label = isMain
                            ? `⭐ ${cls}`
                            : isShared
                            ? `  ${cls} ⚡`
                            : `  ${cls}`;

                    $select.append(`<option value="${cls}" ${style}>${label}</option>`);
                });
            });
        });



    });

    $("#classSelect").on("change", function () {
        const selectedClass = $(this).val();
        if (!selectedClass)
            return;

        currentClass = selectedClass;
        $("#individualSelect").empty().append(`<option value="">-- Select Individual --</option>`);
        $("#dataPropertiesContainer, #objectPropertiesContainer").empty();

        $.getJSON(baseUrl, {type: "directInstances", className: selectedClass}, function (individuals) {
            const $dropdown = $("#individualSelect");
            $dropdown.empty().append(`<option value="">-- Select Individual --</option>`);
            individuals.forEach(ind => {
                $dropdown.append(`<option value="${ind}" title="${ind}">${ind}</option>`);
            });

            // Reinitialize tooltips after adding new options
            $(function () {
                $('[data-bs-toggle="tooltip"]').tooltip();
            });
        });


        $.when(
                $.getJSON(baseUrl, {type: "cardinalities", class: selectedClass}),
                $.getJSON(baseUrl, {type: "numericProperties", class: selectedClass}),
                $.getJSON(baseUrl, {type: "enumeratedProperties", class: selectedClass}),
                $.getJSON(baseUrl, {type: "dateProperties", class: selectedClass}),
                $.getJSON(baseUrl, {type: "uriProperties", class: selectedClass})
                ).done(function (cardRes, numRes, enumRes, dateRes, uriRes) {
            cardinalities = cardRes[0] || {};
            numericPropsList = numRes[0] || [];
            enumeratedPropsMap = enumRes[0] || {};
            datePropsList = dateRes[0] || [];
            uriPropsList = Array.isArray(uriRes[0]) ? uriRes[0] : [];
        });
    });

    $("#individualSelect").on("change", function () {
        const selectedIndividual = $(this).val();
        if (!selectedIndividual)
            return;

        currentIndividual = selectedIndividual;

        $("#dataPropertiesContainer, #objectPropertiesContainer").empty();

        $.when(
                $.getJSON(baseUrl, {type: "dataPropertiesWithComments", class: currentClass}),
                $.getJSON(baseUrl, {type: "objectPropertiesWithComments", class: currentClass, excludeInverses: false}),
                $.getJSON(baseUrl, {type: "dataPropertyValues", individual: selectedIndividual}),
                $.getJSON(baseUrl, {type: "objectPropertyValues", individual: selectedIndividual}),
                $.getJSON(baseUrl, {type: "enumeratedProperties", class: currentClass})
                )
                .done(function (dpRes, opRes, dpValRes, opValRes, enumRes) {

                    const dataProps = dpRes[0] || {};
                    const objectProps = opRes[0] || {};
                    const dataValues = dpValRes[0] || {};
                    const objectValues = opValRes[0] || {};
                    enumeratedPropsMap = enumRes[0] || {};

                    //renderPropertiesTable(dataProps, "data", dataValues, "#dataPropertiesContainer");
                    //renderPropertiesTable(objectProps, "object", objectValues, "#objectPropertiesContainer");
                    // ✅ Directly use the array for data properties
                    renderPropertiesTable(dataProps, "data", dataValues, "#dataPropertiesContainer");


                    // 🔥 Convert object properties Map into Array
                    const objectPropsArray = Object.entries(objectProps).map(([name, comment]) => ({

                            name: name,
                            comment: comment || "",
                            order: 9999
                        }));

                    renderPropertiesTable(objectPropsArray, "object", objectValues, "#objectPropertiesContainer");

                });
    });

    function renderPropertiesTable(properties, type, values, containerId) {
        let table = `<table class="table table-bordered">
                        <thead><tr><th>Property</th><th>Value</th><th>Action</th></tr></thead><tbody>`;
        properties.forEach(property => {
            const prop = property.name;
            const comment = property.comment || "";
            const min = getMin(prop);
            const existingValues = Array.isArray(values[prop]) ? values[prop] : values[prop] ? [values[prop]] : [];
            const rows = Math.max(existingValues.length || 1, min || 1);

            for (let i = 0; i < rows; i++) {
                table += createPropertyRow(prop, type, existingValues[i] || "", i === 0, comment);
            }
        });


        table += "</tbody></table>";
        $(containerId).html(table);

        $(function () {
            $('[data-bs-toggle="tooltip"]').tooltip();
        });

    }

    function createPropertyRow(prop, type, val = "", isFirst = false, comment = "") {
        const min = getMin(prop);
        const max = getMax(prop);
        const canRemove = !isFirst || min === 0;
        let valueField = "";

        if (type === "data") {
            if (enumeratedPropsMap.hasOwnProperty(prop)) {
                const options = enumeratedPropsMap[prop].map(opt => `<option value="${opt}" ${val === opt ? "selected" : ""}>${opt}</option>`).join("");
                valueField = `<select class="form-control">${options}</select>`;
            } else if (numericPropsList.includes(prop)) {
                valueField = `<input type="number" class="form-control" value="${val}">`;
            } else if (datePropsList.includes(prop)) {
                let dateValue = val;
                if (dateValue && dateValue.includes("T")) {
                    dateValue = dateValue.split("T")[0]; // Keep only the date part
                }
                valueField = `<input type="date" class="form-control" value="${dateValue}">`;
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
                        $dropdown.append(`<option value="${inst}" ${inst === val ? "selected" : ""}>${inst}</option>`);
                    });
                });
            }, 0);
        }

        return `<tr data-type="${type}" data-prop="${prop}">
                    <td data-bs-toggle="tooltip" title="${comment}">${prop} <small class="text-muted">(${getCardinalityHint(min, max)})</small></td>
                    <td>${valueField}</td>
                    <td><button type="button" class="btn btn-sm btn-success add-row">+</button> ${canRemove ? `<button type="button" class="btn btn-sm btn-danger remove-row">✖</button>` : ""}</td>
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

    $("#updateIndividualBtn").on("click", function () {
        if (!currentClass || !currentIndividual) {
            Swal.fire("Error", "Please select a class and individual.", "error");
            return;
        }

        const dataProperties = [];
        const objectProperties = [];
        const propCounts = {};

        $("tr[data-type='data']").each(function () {
            const prop = $(this).data("prop");
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

        const payload = {
            action: "updateIndividual",
            className: currentClass,
            individualName: currentIndividual,
            dataProperties: simplifyProperties(dataProperties),
            objectProperties: simplifyProperties(objectProperties)
        };

        console.log("🚀 Submitting update payload:", payload);

        $.ajax({
            type: "POST",
            url: "UpdateIndividualServlet1",
            contentType: "application/json",
            data: JSON.stringify(payload),
            success: function (response) {
                Swal.fire({
                    icon: 'success',
                    title: '✅ Success',
                    text: response.message || 'Individual updated successfully!',
                    confirmButtonText: 'OK'
                }).then((result) => {
                    if (result.isConfirmed) {
                        // 🔥 Reload the individual details after successful update
                        $("#individualSelect").trigger("change");
                    }
                });
            }
            ,
            error: function () {
                Swal.fire("❌ Error", "Failed to update individual.", "error");
            }
        });
    });

    function simplifyProperties(properties) {
        const simplified = {};
        properties.forEach(({property, value}) => {
            if (!simplified[property]) {
                simplified[property] = [];
            }
            simplified[property].push(value);
        });
        return simplified;
    }

    function getMin(prop) {
        return cardinalities[prop]?.min ?? 0;
    }

    function getMax(prop) {
        return cardinalities[prop]?.max ?? null;
    }

    function getCardinalityHint(min, max) {
        if (min !== undefined && max !== null)
            return `${min}..${max}`;
        if (min !== undefined && max === null)
            return `${min}..n`;
        if (min === undefined && max !== null)
            return `0..${max}`;
        return "0..n";
    }
});
