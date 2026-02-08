# --- ETAPA 1: CONSTRUCCIÓN (BUILD) ---
# Usamos una imagen que tiene Maven y Java instalados
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# Creamos la carpeta de trabajo
WORKDIR /app

# Copiamos solo el archivo de configuración primero (para aprovechar la caché)
COPY pom.xml .
COPY src ./src

# Compilamos el proyecto dentro de Docker
# -DskipTests es importante para que no intente conectarse a la BD durante la construcción
RUN mvn clean package -DskipTests

# --- ETAPA 2: EJECUCIÓN (RUN) ---
# Usamos una imagen ligera solo con Java para correr la app
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Aquí está la magia: Copiamos el JAR generado en la Etapa 1 a la Etapa 2
COPY --from=build /app/target/*.jar app.jar

# Comando de inicio
ENTRYPOINT ["java", "-jar", "/app/app.jar"]