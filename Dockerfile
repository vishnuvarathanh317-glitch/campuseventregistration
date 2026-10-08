# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Download MySQL connector jar
RUN mkdir -p lib && \
    wget -q "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar" \
         -O lib/mysql-connector-j-9.1.0.jar

# Copy Java source files
COPY backend/src/main/java/ src/

# Compile: util → model → dao → service → controller → Main
RUN mkdir -p out && \
    find src/util     -name "*.java" > sources.txt 2>/dev/null || true && \
    find src/model    -name "*.java" >> sources.txt 2>/dev/null || true && \
    find src/dao      -name "*.java" >> sources.txt 2>/dev/null || true && \
    find src/service  -name "*.java" >> sources.txt 2>/dev/null || true && \
    find src/controller -name "*.java" >> sources.txt 2>/dev/null || true && \
    find src -maxdepth 1 -name "*.java" >> sources.txt 2>/dev/null || true && \
    javac -cp "lib/*" -d out @sources.txt

# ── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy compiled classes and dependencies
COPY --from=builder /app/out ./out
COPY --from=builder /app/lib ./lib

# Copy config (will be overridden by Render env vars at runtime)
COPY backend/config.properties ./config.properties

# Render assigns PORT via environment variable — default 8080
EXPOSE 8080

ENV PORT=8080

CMD ["java", "-cp", "out:lib/*", "Main"]
