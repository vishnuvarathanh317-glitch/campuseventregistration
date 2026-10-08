# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Download MySQL connector jar
RUN mkdir -p lib && \
    wget -q "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar" \
         -O lib/mysql-connector-j-9.1.0.jar

# Copy Java source files
COPY backend/src/main/java/ src/

# Compile all Java source files
RUN mkdir -p out && \
    javac -cp "lib/*" -d out $(find src -name "*.java")

# ── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy compiled classes and dependencies
COPY --from=builder /app/out ./out
COPY --from=builder /app/lib ./lib

# Copy default config if present
COPY backend/config.properties ./config.properties

# Render assigns PORT via environment variable
EXPOSE 8080

ENV PORT=8080

CMD ["java", "-cp", "out:lib/*", "Main"]

