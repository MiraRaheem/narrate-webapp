# ====== Build stage ======
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
COPY NARRATE-blueprints-rdf-xml.rdf /tmp/NARRATE-blueprints-rdf-xml.rdf
RUN mvn -B -ntp clean package -DskipTests

# ====== Runtime ======
FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
RUN mkdir -p /data
COPY --from=build /tmp/NARRATE-blueprints-rdf-xml.rdf /data/NARRATE-blueprints-rdf-xml.rdf

# 🔇 Disable Tomcat shutdown port to stop HEAD spam
RUN sed -ri 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml

ENV ONTOLOGY_PATH=/data/NARRATE-blueprints-rdf-xml.rdf
ENV CATALINA_OPTS="-Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080
CMD ["catalina.sh", "run"]
