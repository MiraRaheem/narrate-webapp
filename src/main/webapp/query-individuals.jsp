<%-- 
    Document   : query-individuals
    Created on : Mar 25, 2025, 11:50:24 PM
    Author     : amal.elgammal
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Query Blueprints</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="css/style.css">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css" rel="stylesheet">
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <!-- ✅ Add this inside the <head> or before </body> -->
        <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
        <!-- ✅ Add Bootstrap Bundle which includes Popper.js + Bootstrap.js -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        

    </head>

    <!-- Topbar Start -->
    <div class="container-fluid bg-dark">
        <div class="row py-2 px-lg-5">
            <div class="col-lg-6 text-center text-lg-left mb-2 mb-lg-0">
                <div class="d-inline-flex align-items-center text-white">
                    <small><i class="fa fa-phone-alt mr-2"></i>+012 345 6789</small>
                    <small class="px-3">|</small>
                    <small><i class="fa fa-envelope mr-2"></i>info@example.com</small>
                </div>
            </div>
            <div class="col-lg-6 text-center text-lg-right">
                <div class="d-inline-flex align-items-center">
                    <a class="text-white px-2" href="">
                        <i class="fab fa-facebook-f"></i>
                    </a>
                    <a class="text-white px-2" href="">
                        <i class="fab fa-twitter"></i>
                    </a>
                    <a class="text-white px-2" href="">
                        <i class="fab fa-linkedin-in"></i>
                    </a>
                    <a class="text-white px-2" href="">
                        <i class="fab fa-instagram"></i>
                    </a>
                    <a class="text-white pl-2" href="">
                        <i class="fab fa-youtube"></i>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- Topbar End -->


    <!-- Navbar Start -->
    <div class="container-fluid p-0">
        <nav class="navbar navbar-expand-lg bg-light navbar-light py-3 py-lg-0 px-lg-5">
            <a href="index.html" class="navbar-brand ml-lg-3">
                <!-- <h1 class="m-0 display-5 text-uppercase text-primary">
                     <i class="fa fa-truck mr-2"></i>Faster</h1> -->
                <img src="images/Main Logo Transparent.png" alt="Logo" height="50">
            </a>
            <button type="button" class="navbar-toggler" data-toggle="collapse" data-target="#navbarCollapse">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse justify-content-between px-lg-3" id="navbarCollapse">
                <div class="navbar-nav m-auto py-0">
                    <a href="index.html" class="nav-item nav-link active">Home</a>
                    <a href="about.html" class="nav-item nav-link">About</a>
                    <a href="service.html" class="nav-item nav-link">Service</a>
                    <a href="price.html" class="nav-item nav-link">Price</a>
                    <div class="nav-item dropdown">
                        <a href="#" class="nav-link dropdown-toggle" data-toggle="dropdown">Pages</a>
                        <div class="dropdown-menu rounded-0 m-0">
                            <a href="blog.html" class="dropdown-item">Blog Grid</a>
                            <a href="single.html" class="dropdown-item">Blog Detail</a>
                        </div>
                    </div>
                    <a href="contact.html" class="nav-item nav-link">Contact</a>
                </div>
                <a href="" class="btn btn-primary py-2 px-4 d-none d-lg-block">Get A Quote</a>
            </div>
        </nav>
    </div>
    <!-- Navbar End -->

    <body>
        <h1 class="text-center my-4">Query Blueprints</h1>

        <div id="alertsContainer" class="mt-3"></div>

        <div class="container mt-5">

            <h2 class="text-primary">Retrieve Instances</h2>

            <!-- Class Selector -->
            <div class="form-group">
                <label for="classSelect">Select Class:</label>
                <select class="form-control" id="classSelect">
                    <option value="">-- Select a Class --</option>
                </select>
            </div>

            <!-- Data Properties Table -->
            <div id="dataPropertiesContainer" class="mb-4">
                <h4 id="dataPropsHeader" class="d-none">Data Properties</h4>
                <!-- Table will be generated dynamically here -->
            </div>

            <!-- Object Properties Table -->
            <div id="objectPropertiesContainer" class="mb-4">
                <h4 id="objectPropsHeader" class="d-none">Object Properties</h4>

                <!-- Table will be generated dynamically here -->
            </div>

            <div class="form-check mb-2">
                <input class="form-check-input" type="checkbox" id="fuzzyMatchCheckbox">
                <label class="form-check-label" for="fuzzyMatchCheckbox">
                    Include similar matches (text data)
                </label>
            </div>

            <div class="mb-3" id="similarityThresholdContainer" style="display:none;">
                <label for="similarityThreshold" class="form-label">Minimum Similarity (%)</label>
                <input type="range" class="form-range" id="similarityThreshold" min="50" max="100" value="80">
                <span id="similarityValueLabel">80%</span>
            </div>


            <!-- Search Button -->
            <div class="text-center mb-4">
                <button id="searchBtn" class="btn btn-primary" disabled="">Search Individuals</button>
            </div>

            <!-- Results Table -->
            <div id="resultsContainer" class="mt-5">
                <h4 id="resultsHeader" class="d-none">Matching Individuals</h4>

                <!-- Results table will be generated here -->
            </div>
        </div>

        <script src="js/script-query-individuals.js"></script>



    </body>
</html>
