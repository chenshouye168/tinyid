FROM openjdk:17.0.2-oracle
ADD tinyid-server/target/tinyid-server-0.1.0-SNAPSHOT.jar .
CMD ["java","-jar","tinyid-server-0.1.0-SNAPSHOT.jar"]