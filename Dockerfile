# ==============================================================================
# SECUROPS - DOCKERFILE MULTI-STAGE
# ==============================================================================

# ETAPA 1: Compilación y empaquetado con Maven Wrapper
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

# Copiar dependencias de Maven Wrapper para aprovechar cache de capas Docker
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar artefacto JAR final (omitiendo tests para velocidad)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ETAPA 2: Imagen de ejecución ligera (JRE Alpine)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup -S securops && adduser -S securops -G securops
USER securops:securops

# Copiar JAR ejecutable desde la etapa de compilación
COPY --from=builder /workspace/target/*.jar app.jar

# Variables de entorno por defecto
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
