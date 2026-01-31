# 1. Imagen base: Usamos una versión ligera de Java 21
FROM eclipse-temurin:21-jdk-alpine

# 2. Carpeta de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiamos el archivo ejecutable de tu proyecto (el .jar) al contenedor
# Nota: Esto asume que primero compilarás tu proyecto con Maven
COPY target/*.jar app.jar

# 4. El comando que se ejecutará cuando inicies el contenedor
ENTRYPOINT ["java", "-jar", "/app/app.jar"]