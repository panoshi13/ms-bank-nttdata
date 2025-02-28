# Usamos una imagen base de Maven con OpenJDK 17
FROM maven:latest AS build

# Copiamos el archivo pom.xml y el código fuente al contenedor
COPY pom.xml .
COPY src ./src

# Ejecutamos Maven para compilar el proyecto y generar el JAR
RUN mvn clean install -DskipTests

# Usamos una imagen base de OpenJDK 17 para la ejecución
FROM openjdk:17-slim

# Instalamos curl en la imagen final
RUN apt-get update && apt-get install -y curl && apt-get clean

# Copiamos el archivo JAR generado desde la etapa de construcción
COPY --from=build /target/*.jar app.jar

# Expone el puerto donde corre el microservicio
EXPOSE 8761

# Comando para iniciar la aplicación
#ENTRYPOINT ["java", "-jar", "/app.jar"]
ENTRYPOINT ["sh", "-c", "sleep 20 && java -jar /app.jar"]
