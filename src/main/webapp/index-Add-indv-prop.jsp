<%-- 
    Document   : index
    Created on : Mar 11, 2025, 11:05:27 PM
    Author     : amal.elgammal
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ontology Web Application</title>
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script> 

        <script>
            $(document).ready(function () {
                $.ajax({
                    url: "ontologyReaderAjax", // Calls OntologyServlet
                    method: "GET",
                    success: function (data) {
                        var dropdown = $("#classDropdown");
                        dropdown.empty(); // Clear existing options
                        dropdown.append('<option value="">-- Select Class --</option>');

                        // Populate dropdown with classes from JSON response
                        data.forEach(function (className) {
                            dropdown.append('<option value="' + className + '">' + className + '</option>');
                        });
                    },
                    error: function (xhr, status, error) {
                        console.error("Error fetching classes: " + error);
                    }
                });
            });

        </script>


    </head>
    <body>
        <h1>Welcome to Ontology Web App--YARAB! : Now Add Individuals</h1>

        <a href="OntologyServlet">View Ontology Classes</a><br><br>

        <form id="addIndividualForm">
            <label>Class Name:</label>

            <label for="classDropdown">Class Name:</label>
            <select id="classDropdown" name="className">
                <option value="">Loading...</option>
            </select>


            <br><br>
            <label>Individual Name:</label>
            <input type="text" name="individualName" required>
            <br><br>


            <h3>Data Properties</h3>
            <div id="dataProperties">
                <div>
                    <select class="dataProperty">
                        <option value="hasAge">hasAge</option>
                        <option value="hasCredits">hasCredits</option>
                    </select>
                    <input type="text" class="dataValue">
                    <button type="button" onclick="removeProperty(this)">Remove</button>
                </div>
            </div>
            <button type="button" onclick="addDataProperty()">Add Data Property</button>

            <h3>Object Properties</h3>
            <div id="objectProperties">
                <div>
                    <select class="objectProperty">
                        <option value="affiliatedWith">affiliatedWith</option>
                        <option value="takesCourse">takesCourse</option>
                    </select>
                    <input type="text" class="objectValue">
                    <button type="button" onclick="removeProperty(this)">Remove</button>
                </div>
            </div>
            <button type="button" onclick="addObjectProperty()">Add Object Property</button>

            <button type="button" onclick="addIndividual()">Add Individual</button>   

        </form>

        <script>
            function addDataProperty() {
                let div = document.createElement("div");
                div.innerHTML = `<select class="dataProperty">
                        <option value="hasAge">hasAge</option>
                        <option value="hasCredits">hasCredits</option>
                     </select>
                     <input type="text" class="dataValue">
                     <button type="button" onclick="removeProperty(this)">Remove</button>`;
                console.log("hasAge =", hasAge);
                console.log("hasCredits =", hasCredits);
                document.getElementById("dataProperties").appendChild(div);
            }

            function addObjectProperty() {
                let div = document.createElement("div");
                div.innerHTML = `<select class="objectProperty">
                        <option value="affiliatedWith">affiliatedWith</option>
                        <option value="takesCourse">takesCourse</option>
                     </select>
                     <input type="text" class="objectValue">
                     <button type="button" onclick="removeProperty(this)">Remove</button>`;

                console.log("affiliatedWith =", affiliatedWith);
                console.log("takesCourse =", takesCourse);
                document.getElementById("objectProperties").appendChild(div);
            }

            function removeProperty(button) {
                button.parentElement.remove();
            }

            function addIndividual() {
                let className = document.getElementById("className").value;
                console.log("className =", className);

                let individualName = document.getElementById("individualName").value;
                console.log("individualName =", individualName);

                let dataProps = [];
                document.querySelectorAll("#dataProperties div").forEach(div => {
                    let prop = div.querySelector(".dataProperty").value;
                    console.log("data prop =", prop);
                    let value = div.querySelector(".dataValue").value;
                    console.log("value =", value);
                    dataProps.push({property: prop, value: value});
                });

                let objectProps = [];
                document.querySelectorAll("#objectProperties div").forEach(div => {
                    let prop = div.querySelector(".objectProperty").value;
                    console.log("object prop =", prop);
                    let value = div.querySelector(".objectValue").value;
                    console.log("value =", value);
                    objectProps.push({property: prop, value: value});
                });

                let requestData = JSON.stringify({
                    className: className,
                    individualName: individualName,
                    dataProperties: dataProps,
                    objectProperties: objectProps
                });
                console.log("requestData =", requestData);
                
                fetch("AddIndividualServlet2", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: requestData
                }
                )
                        .then(response => response.text())
                        .then(data => alert(data))
                        .catch(error => console.error("Error:", error));

        </script>


        <%-- 
        <form action="OntologyServletAdd" method ="post">
            Class Name: <input type = "text" name ="ClassName" required><br>
            Individual Name: <input type ="text" name="individualName" required><br>
            <input type="submit" value="Add Individual">
            
        </form>
        --%>
    </body>
</html>
