FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

COPY task-tracker-common/pom.xml task-tracker-common/pom.xml
COPY task-tracker-backend/pom.xml task-tracker-backend/pom.xml
COPY task-tracker-scheduler/pom.xml task-tracker-scheduler/pom.xml
COPY email-sender-service/pom.xml email-sender-service/pom.xml
COPY summarization-service/pom.xml summarization-service/pom.xml

RUN ./mvnw -B -pl summarization-service -am dependency:go-offline

COPY task-tracker-common/src task-tracker-common/src
COPY summarization-service/src summarization-service/src

RUN ./mvnw -B -pl summarization-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN adduser --system app-user

COPY --from=builder /app/summarization-service/target/*.jar app.jar

USER app-user

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]