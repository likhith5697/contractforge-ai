# --- Build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies separately from source for faster rebuilds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests \
    && mv target/contractforge-ai.jar target/app.jar

# --- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --uid 10001 --shell /usr/sbin/nologin --no-create-home appuser

COPY --from=build /app/target/app.jar ./app.jar

# ContractForge writes runtime artifacts (scenario artifacts, execution reports) under
# target/contractforge/... relative to the working directory - /app must be writable by
# the non-root user for that, since WORKDIR/COPY above run as root by default.
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
