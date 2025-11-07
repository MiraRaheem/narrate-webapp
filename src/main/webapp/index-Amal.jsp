<%-- 
    Document   : index
    Created on : Mar 11, 2025, 11:05:27 PM
    Author     : amal.elgammal
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Ontology Web App</title>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <script src="js/script-ajax.js"></script>
        <link rel="stylesheet" href="css/styles.css">
        <style>
            body {
                /*background-color: #b4dbb4;  green */
                
                background-color:#ffffff;
                color: #000000; /* Black text for better contrast */
                font-family: 'Arial', sans-serif;
            }
            .container {
                /*background: linear-gradient(135deg, #ff8000, #00bfa6);*/
                background: linear-gradient(135deg, #dcddde, #00bfa6);
                padding: 30px;
                border-radius: 15px;
                box-shadow: 5px 5px 15px rgba(0, 0, 0, 0.5);
            }
            h1, h2 {
                font-weight: bold;
                color: #062340;
            }
            label {
                font-weight: bold;
                color: #ffd700;
            }
            .btn-custom {
                background-color: #ff8000;
                color: white;
                font-weight: bold;
                border: none;
            }
            .btn-custom:hover {
                background-color: #00bfa6;
            }
            .form-control {
                border-radius: 10px;
                border: 2px solid #ffd700;
            }
            .header {
                display: flex;
                align-items: center;
                justify-content: start;
                padding: 15px;
                background-color: #ecf7e1;
            }
            .header img {
                height: 150px;
                margin-right: 200px;
            }
        </style>
    </head>
    <body>
        <div class="header">
            <img src="images/Narrate.PNG" alt="Logo">
            <h1>Blueprint Management System </h1>
        </div>

        <div class="container mt-4">
            <h2>Create Blueprint</h2>
            <form id="individualForm">
                <div class="mb-3">
                    <label for="classSelect" class="form-label label-bold">Class:</label>
                    <select id="classSelect" name="className" class="form-select"></select>
                </div>

                <div class="mb-3">
                    <label for="individualName" class="form-label label-bold">Individual Name:</label>
                    <input type="text" id="individualName" name="individualName" class="form-control" required>
                </div>

                <!-- Data Properties -->
                <div id="dataPropertiesContainer" class="mb-3">
                    <button type="button" id="addDataProperty" class="btn btn-success">+ Add Data Property</button>
                </div>

                <!-- Object Properties -->
                <div id="objectPropertiesContainer" class="mb-3">
                    <button type="button" id="addObjectProperty" class="btn btn-warning">+ Add Object Property</button>
                </div>

                <button type="submit" class="btn btn-success w-100">Add Individual</button>
            </form>
            <p id="responseMessage" class="mt-3"></p>
        </div>
    </body>
</html>


<%-- 
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ontology Web Application</title>
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <script src="js/script-ajax.js"></script> 
        <link rel="stylesheet" href="css/styles.css">

    </head>
    <body>
        <h1>Welcome to Ontology Web App--YARAB! : Now Add Individuals</h1>

        <a href="OntologyServlet">View Ontology Classes</a><br><br>

        <div class="container">
            <h2>Add an Individual to the Ontology</h2>
            <form id="individualForm">
                <label for="classSelect">Class:</label>
                <select id="classSelect" name="className"></select><br>

                <label for="individualName">Individual Name:</label>
                <input type="text" id="individualName" name="individualName" required><br>

                <!-- Dynamic Data Property Section -->
                <div id="dataPropertiesContainer">
                    <button type="button" id="addDataProperty">+ Add Data Property</button>
                </div>

                <!-- Dynamic Object Property Section -->
                <div id="objectPropertiesContainer">
                    <button type="button" id="addObjectProperty">+ Add Object Property</button>
                </div>

                <button type="submit">Add Individual</button>
            </form>
            <p id="responseMessage"></p>
        </div>
    </body>
</html>
--%>
