<%-- 
    Document   : delete-individual.jsp
    Created on : Mar 17, 2025, 2:21:46 PM
    Author     : amal.elgammal
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Delete Blueprints</title>
        <link rel="stylesheet" href="css/style.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css" rel="stylesheet">

        <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
        <!-- ✅ Add this inside the <head> or before </body> -->
        <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

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

        <header>
            <h1 class="text-center my-4">Delete Blueprints</h1>
        </header>

        <section id="delete-section" class="container py-5">
            <div class="container mt-5">
                <h2 class="text-danger">Delete Instance</h2>

                <!-- Select Ontology Class -->
                <div class="form-group">
                    <label for="classSelect">Select Class:</label>
                    <select id="classSelect" class="form-control">
                        <option value="">-- Select a Class --</option>
                    </select>
                </div>

                <!-- Select Individual -->
                <div class="form-group">
                    <label for="individualSelect">Select Individual:</label>
                    <select id="individualSelect" class="form-control" disabled>
                        <option value="">-- Select an Individual --</option>
                    </select>
                </div>

                <!-- Display Properties -->
                <div id="individualDetails" class="mt-3"></div>

                <!-- Container for displaying triples dynamically -->
                <div id="triplesTableContainer" class="mt-4"></div>

                <!-- Delete Button with Confirmation -->
                <button id="deleteIndividualBtn" class="btn btn-danger mt-3" disabled>Delete</button>
            </div>
        </section>

        <!-- Include JavaScript file -->
        <script src="js/script-delete-ajax.js"></script>


    </body>
</html>
