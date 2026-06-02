# ── Stage 1: Build Angular ────────────────────────────────────────────────────
FROM node:20-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci --silent
COPY frontend/ ./
RUN npm run build -- --configuration production

# ── Stage 2: Build Spring Boot (incluye los estáticos de Angular) ─────────────
FROM eclipse-temurin:17-jdk-alpine AS backend-build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:resolve -q
COPY src/ src/
# Copia el output de Angular a los recursos estáticos de Spring Boot
COPY --from=frontend-build /frontend/dist/medicalapp-frontend/browser/ src/main/resources/static/
RUN ./mvnw clean package -DskipTests -q

# ── Stage 3: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
