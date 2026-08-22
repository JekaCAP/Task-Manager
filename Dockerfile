FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Maven Central is often unreachable from RU VPS — mirror in .mvn/settings.xml
COPY pom.xml .
COPY .mvn/settings.xml /root/.m2/settings.xml
RUN mvn dependency:go-offline -B -q || true

COPY src ./src
RUN mvn package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
