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

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy application
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war


# ====== Ontology storage ======
RUN mkdir -p /data

COPY --from=build \
    /tmp/NARRATE-blueprints-rdf-xml.rdf \
    /data/NARRATE-blueprints-rdf-xml.rdf


# ====== Tomcat configuration ======

# Disable Tomcat shutdown port
RUN sed -ri 's/port="8005"/port="-1"/' \
    /usr/local/tomcat/conf/server.xml

# Limit Tomcat request concurrency.
#
# This is important on the 0.5 CPU / 512 MB Starter instance.
# Too many simultaneous requests can cause:
# - CPU saturation
# - excessive JVM memory usage
# - many Jena operations running simultaneously
# - long response times / 504s
#
RUN sed -ri \
    's/(<Connector port="8080"[^>]*)(>)/\1 maxThreads="40" minSpareThreads="2" acceptCount="20" connectionTimeout="30000"\2/' \
    /usr/local/tomcat/conf/server.xml


# ====== Environment ======

ENV ONTOLOGY_PATH=/data/NARRATE-blueprints-rdf-xml.rdf

# Starter plan = 512 MB TOTAL container memory.
#
# IMPORTANT:
# Xmx is NOT the Render memory limit.
# The JVM, Tomcat, native memory, metaspace, thread stacks, etc.
# all share the 512 MB container limit.
#
# Keep Java heap around 256 MB so the container has room for
# Tomcat/JVM/native overhead.
#
ENV CATALINA_OPTS="\
-Xms64m \
-Xmx256m \
-XX:MaxMetaspaceSize=96m \
-XX:ReservedCodeCacheSize=32m \
-XX:MaxDirectMemorySize=32m \
-Xss512k \
-XX:+UseG1GC \
-XX:+ExitOnOutOfMemoryError \
-Djava.security.egd=file:/dev/./urandom \
"

EXPOSE 8080

CMD ["catalina.sh", "run"]
