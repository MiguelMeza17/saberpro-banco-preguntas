# Etapa 1: compila el .jar ejecutable (equivale a ./mvnw clean package -DskipTests)
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# Etapa 2: imagen liviana solo con el JRE 17 y el .jar
# (openjdk:17-jdk-slim, la imagen de la guia, esta descontinuada en Docker Hub)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
