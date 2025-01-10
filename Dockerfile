FROM amazoncorretto:23.0.1-alpine
EXPOSE 8080
WORKDIR /app
COPY target/credible-badger-server-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
