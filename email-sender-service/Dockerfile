FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .


COPY task-tracker-backend/pom.xml task-tracker-backend/pom.xml
COPY email-sender-service/pom.xml email-sender-service/pom.xml
RUN ./mvnw -B -pl email-sender-service dependency:go-offline
COPY task-tracker-backend/src task-tracker-backend/src
COPY email-sender-service/src email-sender-service/src
RUN ./mvnw -B -pl email-sender-service package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN adduser --system app-user
COPY --from=builder /app/email-sender-service/target/*.jar app.jar
USER app-user
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]