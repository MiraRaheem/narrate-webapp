<%-- 
    Document   : sustainability-dashboard
    Created on : Nov 12, 2025, 2:03:12 PM
    Author     : amal.elgammal
--%>

<%@page import="java.util.List"%>
<%@page import="com.example.ontology.OntologyReader"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Sustainability KPI Dashboard - NARRATE BMS</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <!-- Styling from your template -->
        <link href="<%= request.getContextPath()%>/img/favicon.ico" rel="icon">
        <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.10.0/css/all.min.css" rel="stylesheet">
        <link href="lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">
        <link href="css/style.css" rel="stylesheet">
        <link
            href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css"
            rel="stylesheet"
            crossorigin="anonymous"
            />

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
    </head>
    <body>

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
                        <a class="text-white px-2" href=""><i class="fab fa-facebook-f"></i></a>
                        <a class="text-white px-2" href=""><i class="fab fa-twitter"></i></a>
                        <a class="text-white px-2" href=""><i class="fab fa-linkedin-in"></i></a>
                        <a class="text-white px-2" href=""><i class="fab fa-instagram"></i></a>
                        <a class="text-white pl-2" href=""><i class="fab fa-youtube"></i></a>
                    </div>
                </div>
            </div>
        </div>
        <!-- Topbar End -->

        <!-- Navbar Start -->
        <div class="container-fluid p-0">
            <nav class="navbar navbar-expand-lg bg-light navbar-light py-3 py-lg-0 px-lg-5">
                <a href="index.html" class="navbar-brand ml-lg-3">
                    <img src="images/Main Logo Transparent.png" alt="Logo" height="50">
                </a>
                <button type="button" class="navbar-toggler" data-toggle="collapse" data-target="#navbarCollapse">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse justify-content-between px-lg-3" id="navbarCollapse">
                    <div class="navbar-nav m-auto py-0">
                        <a href="index.html" class="nav-item nav-link">Home</a>
                        <a href="about.html" class="nav-item nav-link">About</a>
                        <a href="service.html" class="nav-item nav-link">Service</a>
                        <a href="price.html" class="nav-item nav-link">Price</a>
                        <div class="nav-item dropdown">
                            <a href="#" class="nav-link dropdown-toggle active" data-toggle="dropdown">Pages</a>
                            <div class="dropdown-menu rounded-0 m-0">
                                <a href="blog.html" class="dropdown-item">Blog Grid</a>
                                <a href="single.html" class="dropdown-item">Blog Detail</a>
                                <a href="sustainability-dashboard.jsp" class="dropdown-item active">Sustainability KPIs</a>
                            </div>
                        </div>
                        <a href="contact.html" class="nav-item nav-link">Contact</a>
                    </div>
                    <a href="#" class="btn btn-primary py-2 px-4 d-none d-lg-block">Get A Quote</a>
                </div>
            </nav>
        </div>
        <!-- Navbar End -->


        <!-- Dashboard Content Start -->
        <div class="container-fluid pt-5">
            <div class="container">
                <div class="text-center mb-5">
                    <h1 class="section-title px-5"><span class="px-2">Sustainability KPI Dashboard</span></h1>
                    <p>Select a product and explore its sustainability metrics grouped by category.</p>
                </div>

                <!-- Product Selector -->
                <div class="row mb-4">
                    <div class="col-md-6 offset-md-3">
                        <label for="productSelect" class="font-weight-bold">Select Product:</label>
                        <select id="productSelect" class="form-control">
                            <option value="">-- Choose a Product --</option>
                            <!-- JS will populate this -->
                        </select>
                    </div>
                </div>

                <!-- KPI Accordion -->
                <div class="accordion" id="kpiAccordion">

                    <!-- Material KPIs -->
                    <div class="card mb-3">
                        <div class="card-header bg-primary text-white" id="headingMaterial">
                            <h2 class="mb-0">
                                <button class="btn btn-link text-white" type="button" data-toggle="collapse" data-target="#collapseMaterial">
                                    🧱 Material KPIs
                                </button>
                            </h2>
                        </div>
                        <div id="collapseMaterial" class="collapse show" data-parent="#kpiAccordion">
                            <div class="card-body">
                                <div class="row text-center">
                                    <div class="row text-center">
                                        <div class="col-md-3 mb-3">
                                            <button class="btn btn-outline-primary w-100 kpi-btn" data-kpi="materialIntensity">
                                                <i class="fas fa-cubes"></i>
                                                <span>Material Intensity</span>
                                            </button>
                                        </div>
                                        <div class="col-md-3 mb-3">
                                            <button class="btn btn-outline-danger w-100 kpi-btn" data-kpi="transportCarbon">
                                                <i class="fas fa-truck-moving"></i>
                                                <span>Transport Carbon Footprint</span>
                                            </button>
                                        </div>
                                        <div class="col-md-3 mb-3">
                                            <button class="btn btn-outline-success w-100 kpi-btn" data-kpi="renewableContent">
                                                <i class="fas fa-leaf"></i>
                                                <span>Renewable Content</span>
                                            </button>
                                        </div>
                                        <div class="col-md-3 mb-3">
                                            <button class="btn btn-outline-warning w-100 kpi-btn" data-kpi="recycledContent">
                                                <i class="fas fa-recycle"></i>
                                                <span>Recycled Content</span>
                                            </button>
                                        </div>
                                    </div>

                                </div>

                                <canvas id="materialKpiChart" height="300"></canvas>

                            </div>
                        </div>
                    </div>

                    <!-- Product KPIs -->
                    <div class="card mb-3">
                        <div class="card-header bg-success text-white" id="headingProduct">
                            <h2 class="mb-0">
                                <button class="btn btn-link text-white collapsed" type="button" data-toggle="collapse" data-target="#collapseProduct">
                                    📦 Product KPIs
                                </button>
                            </h2>
                        </div>
                        <div id="collapseProduct" class="collapse" data-parent="#kpiAccordion">
                            <div class="card-body">
                                <ul>
                                    <li>Material Efficiency</li>
                                    <li>Total Transport Carbon</li>
                                    <li>% Renewable Material</li>
                                    <li>% Recycled Material</li>
                                </ul>
                                <div id="productKpiChart" style="height:300px;"></div>
                            </div>
                        </div>
                    </div>

                    <!-- Energy KPIs -->
                    <div class="card mb-3">
                        <div class="card-header bg-warning text-white" id="headingEnergy">
                            <h2 class="mb-0">
                                <button class="btn btn-link text-white collapsed" type="button" data-toggle="collapse" data-target="#collapseEnergy">
                                    ⚡ Energy KPIs
                                </button>
                            </h2>
                        </div>
                        <div id="collapseEnergy" class="collapse" data-parent="#kpiAccordion">
                            <div class="card-body">
                                <ul>
                                    <li>Electricity/Fuel/Gas Carbon Footprints</li>
                                    <li>% Renewable Energy Used</li>
                                    <li>Embodied Carbon per Product</li>
                                </ul>
                                <div id="energyKpiChart" style="height:300px;"></div>
                            </div>
                        </div>
                    </div>

                    <!-- Water KPIs -->
                    <div class="card mb-3">
                        <div class="card-header bg-info text-white" id="headingWater">
                            <h2 class="mb-0">
                                <button class="btn btn-link text-white collapsed" type="button" data-toggle="collapse" data-target="#collapseWater">
                                    💧 Water KPIs
                                </button>
                            </h2>
                        </div>
                        <div id="collapseWater" class="collapse" data-parent="#kpiAccordion">
                            <div class="card-body">
                                <ul>
                                    <li>Water Use per Kg Product</li>
                                    <li>Wastewater per Kg Product</li>
                                    <li>Total Water Demand</li>
                                    <li>Wastewater Generation</li>
                                </ul>
                                <div id="waterKpiChart" style="height:300px;"></div>
                            </div>
                        </div>
                    </div>

                    <!-- Waste KPIs -->
                    <div class="card mb-3">
                        <div class="card-header bg-danger text-white" id="headingWaste">
                            <h2 class="mb-0">
                                <button class="btn btn-link text-white collapsed" type="button" data-toggle="collapse" data-target="#collapseWaste">
                                    ♻️ Waste KPIs
                                </button>
                            </h2>
                        </div>
                        <div id="collapseWaste" class="collapse" data-parent="#kpiAccordion">
                            <div class="card-body">
                                <ul>
                                    <li>Hazardous / Non-Hazardous Waste</li>
                                    <li>External Recycling</li>
                                    <li>Landfill / Incineration Breakdown</li>
                                </ul>
                                <div id="wasteKpiChart" style="height:300px;"></div>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- Chart.js & Logic -->
                <script src="js/sustainability-kpi.js"></script>

            </div>
        </div>
        <!-- Dashboard Content End -->

    </div>
    <!-- Footer End -->

    <a href="#" class="btn btn-lg btn-primary back-to-top"><i class="fa fa-angle-double-up"></i></a>


</body>
</html>
