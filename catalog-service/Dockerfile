# Multi-stage Dockerfile for Quarkus JVM Mode
# Stage 1: Build the Maven application
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copy root Maven wrapper and POM files
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY bloom-common/pom.xml bloom-common/
COPY bloom-catalog-service/pom.xml bloom-catalog-service/
COPY bloom-order-service/pom.xml bloom-order-service/
COPY bloom-inventory-service/pom.xml bloom-inventory-service/
COPY bloom-notification-service/pom.xml bloom-notification-service/
COPY bloom-gateway/pom.xml bloom-gateway/

# Copy sources for common and catalog service
COPY bloom-common/src bloom-common/src
COPY bloom-catalog-service/src bloom-catalog-service/src

# Package bloom-catalog-service with Quarkus fast-jar
RUN ./mvnw clean package -pl bloom-catalog-service -am -DskipTests

# Stage 2: Minimal Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /deployments

# Copy Quarkus fast-jar files from the builder stage
COPY --from=builder /workspace/bloom-catalog-service/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder /workspace/bloom-catalog-service/target/quarkus-app/*.jar /deployments/
COPY --from=builder /workspace/bloom-catalog-service/target/quarkus-app/app/ /deployments/app/
COPY --from=builder /workspace/bloom-catalog-service/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8081
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /deployments/quarkus-run.jar"]
