# Etapa 1: Build del JAR con Maven y Java 21
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final de runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiar JAR desde el build anterior
COPY --from=build /app/target/*.jar app.jar

# Copiar el script que genera el archivo de credenciales
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Definir la ruta donde estará el archivo de credenciales
ENV GOOGLE_APPLICATION_CREDENTIALS=/tmp/credentials.json

# Exponer el puerto de Spring Boot
EXPOSE 8080

# Ejecutar el script de arranque
ENTRYPOINT ["/app/entrypoint.sh"]
