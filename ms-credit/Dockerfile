# Etapa 1: Construcción
#FROM maven:3.9.4-eclipse-temurin-17 AS build
FROM maven:latest AS build
WORKDIR /app

# Copia el archivo pom.xml y descarga las dependencias (cache eficiente)
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Construir el módulo customer-command
WORKDIR /app/ms-credit
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final
#FROM eclipse-temurin:17-jdk
FROM openjdk:17-slim
WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/ms-credit/target/*.jar app.jar

# Exponer el puerto del microservicio
EXPOSE 8082

# Iniciar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]