$(document).ready(function () {
    // Load ontology classes dynamically
    $.getJSON("ontologyReaderAjax", {type: "classAnnotation", annotation: "blueprintClass"}, function (groupedClasses) {
        console.log("📦 Loaded grouped classes:", groupedClasses);

        const classSelect = $("#classSelect");
        classSelect.empty();

        // Placeholder option
        classSelect.append(`<option value="" disabled selected style="color:#999;">Select blueprint</option>`);

        // Count how many times each class appears (for ⚡ indicator)
        const classCounts = {};
        Object.values(groupedClasses).forEach(classList => {
            classList.forEach(cls => {
                classCounts[cls] = (classCounts[cls] || 0) + 1;
            });
        });

        // Build the grouped dropdown
        Object.keys(groupedClasses).sort().forEach(blueprint => {
            classSelect.append(`<option disabled>🔹 𝐁𝐋𝐔𝐄𝐏𝐑𝐈𝐍𝐓: ${blueprint}</option>`);

            groupedClasses[blueprint].sort().forEach(cls => {
                const coreName = blueprint.replace(/Blueprint$/, "").toLowerCase();
                const isMain = cls.toLowerCase() === coreName;
                const isShared = classCounts[cls] > 1;

                const style = isMain ? 'style="font-weight:bold; color:#007bff;"' : "";
                const label = isMain
                        ? `⭐ ${cls}`
                        : isShared
                        ? `  ${cls} ⚡`
                        : `  ${cls}`;

                classSelect.append(`<option value="${cls}" ${style}>${label}</option>`);
            });
        });
    }).fail(function (jqXHR, textStatus, errorThrown) {
        console.error("Failed to load grouped classes:", textStatus, errorThrown);
    });


    // Load individuals dynamically based on selected class
    $("#classSelect").change(function () {
        let selectedClass = $(this).val();
        let individualSelect = $("#individualSelect");

        // Reset UI
        individualSelect.prop("disabled", true).empty().append(new Option("-- Select an Individual --", ""));
        $("#individualDetails").empty();
        $("#triplesTableContainer").empty();
        $("#deleteIndividualBtn").prop("disabled", true);

        if (selectedClass) {
            $.getJSON("ontologyReaderAjax", {type: "directInstances", className: selectedClass}, function (individuals) {
                console.log("Instances received:", individuals);
                if (individuals.length > 0) {
                    individuals.forEach(ind => {
                        individualSelect.append(new Option(ind, ind));
                    });
                    individualSelect.prop("disabled", false);
                } else {
                    console.log("No instances found for class: " + selectedClass);
                }
            }).fail(handleAjaxError);
        }
    });

    // Load individual details and triples when an individual is selected
    $("#individualSelect").change(function () {
        let selectedIndividual = $(this).val();
        let detailsContainer = $("#individualDetails");
        let triplesContainer = $("#triplesTableContainer");

        // Reset UI
        detailsContainer.empty();
        triplesContainer.empty();
        $("#deleteIndividualBtn").prop("disabled", true);

        if (selectedIndividual) {
            fetchIndividualDetails(selectedIndividual);
        }
    });

    // Fetch individual details (Data & Object Properties)
    function fetchIndividualDetails(individual) {
        let detailsContainer = $("#individualDetails");

        $.getJSON("ontologyReaderAjax", {type: "individualDetails", individual: individual}, function (details) {
            console.log("Individual Details:", details);

            let table = $('<table class="table table-bordered text-white">');
            table.append('<thead><tr><th>Property</th><th>Value</th></tr></thead>');
            let tbody = $('<tbody>');

            let hasProperties = false;

            Object.entries(details).forEach(([key, value]) => {
                if (key !== "type") { // Ignore "type" property
                    if (Array.isArray(value)) {
                        value.forEach(val => {
                            let cleanValue;
                            if (typeof val === "string") {
                                if (val.includes("^^")) {
                                    cleanValue = val.split("^^")[0]; // ✅ Extract numeric value (before datatype annotation)
                                } else if (val.includes("#")) {
                                    cleanValue = val.split("#").pop(); // ✅ Extract local name from namespace
                                } else if (val.startsWith("http")) {
                                    cleanValue = val.substring(val.lastIndexOf("/") + 1); // ✅ Extract name from full URL
                                } else {
                                    cleanValue = val; // ✅ Keep as is
                                }
                            } else {
                                cleanValue = val;
                            }
                            tbody.append('<tr><td>' + key + '</td><td>' + cleanValue + '</td></tr>');
                        });
                    } else {
                        let cleanValue;
                        if (typeof value === "string") {
                            if (value.includes("^^")) {
                                cleanValue = value.split("^^")[0]; // ✅ Extract numeric value
                            } else if (value.includes("#")) {
                                cleanValue = value.split("#").pop(); // ✅ Extract local name
                            } else if (value.startsWith("http")) {
                                cleanValue = value.substring(value.lastIndexOf("/") + 1); // ✅ Extract name from full URL
                            } else {
                                cleanValue = value; // ✅ Keep as is
                            }
                        } else {
                            cleanValue = value;
                        }
                        tbody.append('<tr><td>' + key + '</td><td>' + cleanValue + '</td></tr>');
                    }
                    hasProperties = true;
            }
            });


            if (!hasProperties) {
                tbody.append('<tr><td colspan="2">No Data/Object Properties Found</td></tr>');
            }

            table.append(tbody);
            detailsContainer.append("<h4>Data & Object Properties</h4>").append(table);
            $("#deleteIndividualBtn").prop("disabled", false);

            // Load triples AFTER properties
            loadTriples(individual);
        }).fail(handleAjaxError);
    }

    // Fetch triples for the selected individual
    function loadTriples(individual) {
        $.getJSON("ontologyReaderAjax", {type: "individualTriples", individual: individual}, function (triples) {
            console.log("Triples:", triples);
            let triplesContainer = $("#triplesTableContainer");
            triplesContainer.empty(); // Clear previous content

            if (triples.length > 0) {
                let triplesTable = $('<table class="table table-striped table-dark">');
                let thead = $('<thead><tr><th>Subject</th><th>Predicate</th><th>Object</th></tr></thead>');
                let tbody = $('<tbody>');

                triples.forEach(triple => {
                    let parts = triple.split(" ");
                    let subject = parts[0].includes("#") ? parts[0].split("#")[1] : parts[0];
                    let predicate = parts[1].includes("#") ? parts[1].split("#")[1] : parts[1];
                    let object = parts[2].includes("#") ? parts[2].split("#")[1] : parts[2];

                    tbody.append('<tr><td>' + subject + '</td><td>' + predicate + '</td><td>' + object + '</td></tr>');
                });

                triplesTable.append(thead).append(tbody);
                triplesContainer.append("<h4>Triples where " + individual + " appears:</h4>");
                triplesContainer.append(triplesTable);
                triplesContainer.show();
            } else {
                triplesContainer.append("<p>No triples found.</p>");
                triplesContainer.show();
            }
        }).fail(handleAjaxError);
    }



// ✅ Handle delete individual
    $("#deleteIndividualBtn").click(function () {
        let selectedClass = $("#classSelect").val();
        let selectedIndividual = $("#individualSelect").val();

        if (selectedClass && selectedIndividual) {
            // ✅ Show SweetAlert2 confirmation before deleting
            Swal.fire({
                title: "Are you sure?",
                text: "This will also remove all triples where " + selectedIndividual + " is subject or object.",
                icon: "warning",
                showCancelButton: true,
                confirmButtonColor: "#d33",
                cancelButtonColor: "#3085d6",
                confirmButtonText: "Yes, delete it!"
            }).then((result) => {
                if (result.isConfirmed) {
                    // ✅ Proceed with deletion if confirmed
                    $.post("DeleteIndividualServlet",
                            {className: selectedClass, individualName: selectedIndividual},
                            function (response) {
                                console.log("🗑️ Delete response:", response);

                                if (response.status === "success") {
                                    // ✅ Show success message with SweetAlert2
                                    Swal.fire({
                                        title: "Deleted!",
                                        text: response.message,
                                        icon: "success",
                                        confirmButtonText: "OK"
                                    }).then(() => {
                                        // ✅ Wait 1 second to allow ontology model to reload before fetching new individuals
                                        setTimeout(function () {
                                            $.getJSON("ontologyReaderAjax", {type: "directInstances", className: selectedClass, timestamp: new Date().getTime()}) // Force fresh request
                                                    .done(function (individuals) {
                                                        console.log("✅ Updated individuals list:", individuals);

                                                        let individualSelect = $("#individualSelect");
                                                        individualSelect.empty().append(new Option("-- Select an Individual --", ""));

                                                        if (Array.isArray(individuals) && individuals.length > 0) {
                                                            individuals.forEach(ind => {
                                                                individualSelect.append(new Option(ind, ind));
                                                            });
                                                            individualSelect.prop("disabled", false);
                                                        } else {
                                                            individualSelect.prop("disabled", true);
                                                        }

                                                        // ✅ After dropdown updates, refresh the page to ensure consistency
                                                        setTimeout(function () {
                                                            location.reload();
                                                        }, 1000);

                                                    })
                                                    .fail(function (jqXHR, textStatus, errorThrown) {
                                                        console.error("⚠️ Failed to fetch updated individuals:", textStatus, errorThrown);
                                                    });
                                        }, 1000); // ✅ Delay before fetching updated individuals to ensure ontology reloads first
                                    });

                                } else {
                                    // ❌ Show error message with SweetAlert2
                                    Swal.fire({
                                        title: "Error!",
                                        text: response.message,
                                        icon: "error",
                                        confirmButtonText: "OK"
                                    });
                                }
                            }, "json").fail(function (jqXHR, textStatus, errorThrown) {
                        console.error("⚠️ AJAX request failed:", textStatus, errorThrown);
                        Swal.fire({
                            title: "Error!",
                            text: "An error occurred while deleting the individual.",
                            icon: "error",
                            confirmButtonText: "OK"
                        });
                    });
                }
            });
        }
    });


    // General AJAX error handler
    function handleAjaxError(jqXHR, textStatus, errorThrown) {
        console.error("AJAX request failed:", textStatus, errorThrown);
        alert("An error occurred while processing the request.");
    }
});
