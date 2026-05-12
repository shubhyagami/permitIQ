FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/uploads
COPY --from=build /app/target/smart-permit-monitoring-system-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV SERVER_PORT=${PORT:-8080}
ENTRYPOINT ["java", "-jar", "app.jar"]
