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
        <!-- Favicon -->
        <link href="img/favicon.ico" rel="icon">
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
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>
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
                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-outline-primary w-100 kpi-btn" data-kpi="materialIntensity">
                                            <i class="fas fa-cubes"></i>
                                            <span>Material Intensity</span>
                                        </button>
                                    </div>
                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-outline-danger w-100 kpi-btn" data-kpi="supplyTransportCarbonFootprint">
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



                                <canvas id="materialKpiChart" height="300"></canvas>

                                <!-- Material Intensity benchmark results -->
                                <div id="materialIntensityBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>
                                <!-- Renewable Content benchmark results -->
                                <div id="renewableBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>

                                <!-- Recycled Content benchmark results -->
                                <div id="recycledBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>

                                <!-- Supply Transport Carbon Footprint benchmark results -->
                                <div id="supplyTransportBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>


                                <div id="materialKpiExplanation"
                                     class="alert alert-light border mt-3"
                                     style="display: none;">
                                </div>






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

                        <div id="collapseProduct" class="collapse"
                             data-parent="#kpiAccordion">
                            <div class="card-body">


                                <div class="row text-center">

                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-outline-primary w-100 product-kpi-btn"
                                                data-kpi="productMaterialEfficiency">
                                            <i class="fas fa-weight-hanging"></i>
                                            <span>Material Efficiency</span>
                                        </button>
                                    </div>

                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-outline-danger w-100 product-kpi-btn"
                                                data-kpi="productTransportCarbon">
                                            <i class="fas fa-truck-moving"></i>
                                            <span>Total Transport Carbon</span>
                                        </button>
                                    </div>

                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-outline-success w-100 product-kpi-btn"
                                                data-kpi="productRenewableMaterial">
                                            <i class="fas fa-leaf"></i>
                                            <span>% Renewable Material</span>
                                        </button>
                                    </div>

                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-outline-warning w-100 product-kpi-btn"
                                                data-kpi="productRecycledMaterial">
                                            <i class="fas fa-recycle"></i>
                                            <span>% Recycled Material</span>
                                        </button>
                                    </div>

                                </div>



                                <canvas id="productKpiChart" height="300"></canvas>

                                <!-- Product KPI Explanation -->
                                <div id="productKpiExplanation"
                                     class="alert alert-light border mt-3"
                                     style="display: none;">
                                </div>

                                <div id="productKpiResults"></div>

                                <!-- Product Material Efficiency benchmarking and saving -->
                                <div id="productMaterialEfficiencyBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>

                                <!-- Product Total Transport Carbon benchmarking and saving -->
                                <div id="productTransportCarbonBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>

                                <!-- Product Renewable Material benchmarking and saving -->
                                <div id="productRenewableMaterialBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>

                                <!-- Product Recycled Material benchmarking and saving -->
                                <div id="productRecycledMaterialBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>


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

                        <div id="collapseEnergy" class="collapse"
                             data-parent="#kpiAccordion">

                            <div class="card-body">

                                <h5 class="mb-3">Corporate Energy KPIs</h5>

                                <div class="row text-center">

                                    <div class="col-md-4 mb-3">
                                        <button
                                            type="button"
                                            class="btn btn-outline-warning w-100 energy-kpi-btn"
                                            data-kpi="corporateEnergyCarbonFootprint">

                                            <i class="fas fa-bolt"></i>
                                            <span>Energy Carbon Footprints</span>

                                        </button>
                                    </div>


                                    <div class="col-md-4 mb-3">
                                        <button
                                            type="button"
                                            class="btn btn-outline-success w-100 energy-kpi-btn"
                                            data-kpi="corporateRenewableElectricity">

                                            <i class="fas fa-leaf"></i>
                                            <span>% Renewable Electricity Used</span>

                                        </button>
                                    </div>


                                </div>


                                <canvas id="energyKpiChart" height="300"></canvas>

                                <!-- Energy KPI Explanation -->
                                <div id="energyKpiExplanation"
                                     class="alert alert-light border mt-3"
                                     style="display: none;">
                                </div>

                                <div id="energyKpiResults"></div>

                                <!-- Corporate Energy Carbon Intensity benchmarking and saving -->
                                <div id="corporateEnergyCarbonBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>

                                <!-- Corporate Renewable Electricity benchmarking and saving -->
                                <div id="corporateRenewableElectricityBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>

                                <!-- Product Energy Carbon Footprint benchmarking and saving -->
                                <div id="productEnergyCarbonBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>

                                <!-- Product Embodied Carbon benchmarking and saving -->
                                <div id="productEmbodiedCarbonBenchmarkResults"
                                     class="mt-4"
                                     style="display: none;">
                                </div>


                                <hr class="my-4">

                                <h5 class="mb-3">Product Energy KPIs</h5>

                                <div class="row text-center">

                                    <div class="col-md-4 mb-3">
                                        <button
                                            type="button"
                                            class="btn btn-outline-warning w-100 energy-kpi-btn"
                                            data-kpi="productEnergyCarbonFootprint">

                                            <i class="fas fa-bolt"></i>
                                            <span>Product Energy Carbon Footprints</span>

                                        </button>
                                    </div>

                                    <div class="col-md-4 mb-3">
                                        <button
                                            type="button"
                                            class="btn btn-outline-info w-100 energy-kpi-btn"
                                            data-kpi="productEmbodiedCarbon">

                                            <i class="fas fa-leaf"></i>
                                            <span>Product Embodied Carbon</span>

                                        </button>
                                    </div>

                                </div>

                            </div>
                        </div>

                    </div>

                    <!-- Water KPIs -->

                    <!-- Water KPIs -->
                    <div class="card mb-3">
                        <div class="card-header bg-info text-white" id="headingWater">
                            <h2 class="mb-0">
                                <button class="btn btn-link text-white collapsed"
                                        type="button"
                                        data-toggle="collapse"
                                        data-target="#collapseWater">
                                    💧 Water KPIs
                                </button>
                            </h2>
                        </div>

                        <div id="collapseWater" class="collapse"
                             data-parent="#kpiAccordion">

                            <div class="card-body">

                                <h5 class="mb-3">Corporate Water KPIs</h5>

                                <div class="row text-center">
                                    <div class="col-md-6 mb-3">
                                        <button type="button"
                                                class="btn btn-outline-info w-100 water-kpi-btn"
                                                data-kpi="corporateWaterKpis">
                                            <i class="fas fa-tint"></i>
                                            <span>Corporate Water Demand &amp; Wastewater</span>
                                        </button>
                                    </div>
                                </div>

                                <hr class="my-4">

                                <h5 class="mb-3">Product Water KPIs</h5>

                                <div class="row text-center">
                                    <div class="col-md-6 mb-3">
                                        <button type="button"
                                                class="btn btn-outline-primary w-100 water-kpi-btn"
                                                data-kpi="productWaterKpis">
                                            <i class="fas fa-water"></i>
                                            <span>Product Water Demand &amp; Wastewater</span>
                                        </button>
                                    </div>
                                </div>


                                <div class="row mt-3">

                                    <div class="col-lg-4 col-md-6 mb-3">
                                        <div class="border rounded p-2">
                                            <h6 class="text-center" id="waterChartTitle">
                                                Water KPIs
                                            </h6>
                                            <div style="height: 190px;">
                                                <canvas id="waterKpiChart"></canvas>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-lg-4 col-md-6 mb-3">
                                        <div class="border rounded p-2">
                                            <h6 class="text-center">Wastewater / Water Demand</h6>
                                            <div style="height: 190px;">
                                                <canvas id="waterRatioChart"></canvas>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-lg-4 col-md-12 mb-3">
                                        <div class="border rounded p-2">
                                            <h6 class="text-center">Corporate vs Product</h6>
                                            <div style="height: 190px;">
                                                <canvas id="waterComparisonChart"></canvas>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                                <div id="waterKpiExplanation"
                                     class="alert alert-light border mt-2"
                                     style="display: none;">
                                </div>

                                <div id="waterKpiResults"></div>

                                <div id="corporateWaterUseBenchmarkResults" class="mt-4" style="display: none;"></div>
                                <div id="corporateWastewaterBenchmarkResults" class="mt-4" style="display: none;"></div>
                                <div id="productWaterUseBenchmarkResults" class="mt-4" style="display: none;"></div>
                                <div id="productWastewaterBenchmarkResults" class="mt-4" style="display: none;"></div>


                            </div>
                        </div>
                    </div>


                    <!-- Waste KPIs -->

                    <!-- Waste KPIs -->
                    <div class="card mb-3">
                        <div class="card-header bg-danger text-white" id="headingWaste">
                            <h2 class="mb-0">
                                <button class="btn btn-link text-white collapsed"
                                        type="button"
                                        data-toggle="collapse"
                                        data-target="#collapseWaste">
                                    ♻️ Waste KPIs
                                </button>
                            </h2>
                        </div>

                        <div id="collapseWaste" class="collapse"
                             data-parent="#kpiAccordion">

                            <div class="card-body">

                                <h5 class="mb-3">Corporate Waste KPIs</h5>

                                <div class="row text-center">
                                    <div class="col-md-6 mb-3">
                                        <button type="button"
                                                class="btn btn-outline-danger w-100 waste-kpi-btn"
                                                data-kpi="corporateWasteKpis">
                                            <i class="fas fa-industry"></i>
                                            <span>Corporate Waste Generation &amp; Treatment</span>
                                        </button>
                                    </div>
                                </div>

                                <hr class="my-4">

                                <h5 class="mb-3">Product Waste KPIs</h5>

                                <div class="row text-center">
                                    <div class="col-md-6 mb-3">
                                        <button type="button"
                                                class="btn btn-outline-success w-100 waste-kpi-btn"
                                                data-kpi="productWasteKpis">
                                            <i class="fas fa-recycle"></i>
                                            <span>Product Waste Generation &amp; Treatment</span>
                                        </button>
                                    </div>
                                </div>

                                <!-- Three Waste KPI charts -->
                                <div class="row mt-3">

                                    <!-- Chart 1: Waste generation -->
                                    <div class="col-lg-4 col-md-6 mb-3">
                                        <div class="border rounded p-2">
                                            <h6 class="text-center" id="wasteChartTitle">
                                                Waste Generation
                                            </h6>
                                            <div style="height: 220px;">
                                                <canvas id="wasteKpiChart"></canvas>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Chart 2: Hazardous waste treatment -->
                                    <div class="col-lg-4 col-md-6 mb-3">
                                        <div class="border rounded p-2">
                                            <h6 class="text-center">
                                                Hazardous Waste Treatment
                                            </h6>
                                            <div style="height: 220px;">
                                                <canvas id="hazardousWasteChart"></canvas>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Chart 3: Non-hazardous waste treatment -->
                                    <div class="col-lg-4 col-md-12 mb-3">
                                        <div class="border rounded p-2">
                                            <h6 class="text-center">
                                                Non-Hazardous Waste Treatment
                                            </h6>
                                            <div style="height: 220px;">
                                                <canvas id="nonHazardousWasteChart"></canvas>
                                            </div>
                                        </div>
                                    </div>

                                </div>

                                <!-- Explanation section: same pattern as Water KPIs -->
                                <div id="wasteKpiExplanation"
                                     class="alert alert-light border mt-2"
                                     style="display: none;">
                                </div>

                                <!-- Detailed KPI results -->
                                <div id="wasteKpiResults"></div>

                                <div id="corporateHazardousWasteBenchmarkResults" class="mt-4" style="display: none;"></div>
                                <div id="corporateNonHazardousWasteBenchmarkResults" class="mt-4" style="display: none;"></div>
                                <div id="productHazardousWasteBenchmarkResults" class="mt-4" style="display: none;"></div>
                                <div id="productNonHazardousWasteBenchmarkResults" class="mt-4" style="display: none;"></div>

                            </div>
                        </div>
                    </div>
                    <!-- End Waste KPIs -->


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
