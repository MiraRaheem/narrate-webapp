# ====== Build stage ======
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app

# 1) Cache deps
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# 2) Build
COPY src ./src
# Include the RDF in build context so we can add it to the image
COPY NARRATE-blueprints-rdf-xml.rdf /tmp/NARRATE-blueprints-rdf-xml.rdf
RUN mvn -B -ntp clean package -DskipTests

# ====== Runtime (Tomcat 10 + JDK 17) ======
FROM tomcat:10.1-jdk17-temurin

# Clean the default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Put your WAR as OntologyWebApp.war so the app is served at /OntologyWebApp
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Seed the ontology file into /data and make sure the folder exists
RUN mkdir -p /data
COPY --from=build /tmp/NARRATE-blueprints-rdf-xml.rdf /data/NARRATE-blueprints-rdf-xml.rdf

# App expects this path by default, but set it explicitly too:
ENV ONTOLOGY_PATH=/data/NARRATE-blueprints-rdf-xml.rdf

# Good JVM defaults (adjust if you like)
ENV CATALINA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD curl -fsS http://localhost:8080/ || exit 1

CMD ["catalina.sh", "run"]
