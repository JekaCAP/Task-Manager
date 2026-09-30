# syntax=docker/dockerfile:1

# ---------- Stage 1: React UI -> src/main/resources/static ----------
# node:24 (npm 11+) — npm 10 в node:22 не читает lockfile с optional-пакетами без version
FROM node:24-alpine AS frontend
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./frontend/
RUN cd frontend && npm ci --no-audit --no-fund
COPY frontend/ ./frontend/
RUN mkdir -p src/main/resources/static \
    && cd frontend && npm run build:embed

# ---------- Stage 2: Maven package ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline
COPY src ./src
COPY --from=frontend /build/src/main/resources/static ./src/main/resources/static
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests

# ---------- Stage 3: runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /build/target/task-manager-*.jar app.jar

# В контейнере биндимся на 0.0.0.0 (на VPS был 127.0.0.1 — там nginx был на хосте).
# X-Forwarded-* от nginx должны учитываться Spring'ом.
ENV SERVER_PORT=8090 \
    SERVER_ADDRESS=0.0.0.0 \
    SERVER_FORWARD_HEADERS_STRATEGY=framework \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER spring
EXPOSE 8090
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=6 \
  CMD wget -q -O /dev/null "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
