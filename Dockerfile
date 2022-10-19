FROM maven:3.8.6-openjdk-18 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -Dmaven.test.skip=true -f /home/app/pom.xml clean package

# Package stage
FROM openjdk:18.0.2.1-jdk-oracle
COPY --from=build /home/app/target/transactional-outbox-0.0.1-SNAPSHOT.jar /usr/local/lib/transactional-outbox.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/transactional-outbox.jar"]
