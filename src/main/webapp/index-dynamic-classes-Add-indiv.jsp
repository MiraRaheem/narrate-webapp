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

        <form action="AddIndividualServlet" method="post">
            <label>Class Name:</label>

            <label for="classDropdown">Class Name:</label>
            <select id="classDropdown" name="className">
                <option value="">Loading...</option>
            </select>


            <br><br>
            <label>Individual Name:</label>
            <input type="text" name="individualName" required>
            <br><br>
            <button type="submit">Add Individual</button>
        </form>




        <%-- 
        <form action="OntologyServletAdd" method ="post">
            Class Name: <input type = "text" name ="ClassName" required><br>
            Individual Name: <input type ="text" name="individualName" required><br>
            <input type="submit" value="Add Individual">
            
        </form>
        --%>
    </body>
</html>
