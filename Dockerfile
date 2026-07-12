# --- ETAPA 1: CONSTRUCCIÓN (BUILD) ---
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# 1. Copiamos SOLO el pom.xml primero
COPY pom.xml .

# 2. Descargamos las dependencias. Esta capa se guardará en la caché de Docker.
# Si el pom.xml no cambia, Docker saltará este paso en futuros builds, ahorrando minutos.
RUN mvn dependency:go-offline -B

# 3. Copiamos el código fuente
COPY src ./src

# 4. Compilamos el proyecto (modo offline para aprovechar lo descargado en el paso 2)
RUN mvn clean package -DskipTests -o

# --- ETAPA 2: EJECUCIÓN (RUN) ---
# Usamos JRE (Java Runtime Environment) en lugar de JDK para reducir tamaño y vulnerabilidades
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 5. Por seguridad (Best Practice en AWS ECS), creamos un usuario sin privilegios de root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 6. Copiamos el JAR compilado desde la Etapa 1
COPY --from=build /app/target/*.jar app.jar

# 7. Exponemos el puerto (Útil para el mapeo en AWS ECS Target Groups)
EXPOSE 8080

# 8. Variables de entorno por defecto para optimizar la memoria de la JVM y fijar la zona horaria
ENV JAVA_OPTS="-Duser.timezone=America/Lima -XX:MaxRAMPercentage=75.0"

# 9. Healthcheck de liveness (wget viene con busybox en Alpine). Comprueba solo
# la salud del proceso: no reinicia el contenedor si falla una dependencia (BD/
# Redis) — de eso se encarga el probe de readiness del ALB.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/actuator/health/liveness || exit 1

# 10. Comando de inicio usando las variables de entorno
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]