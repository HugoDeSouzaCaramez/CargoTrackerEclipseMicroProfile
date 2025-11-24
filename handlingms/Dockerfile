FROM maven:3.5.4-jdk-8 as build

WORKDIR /helidon

COPY utilities/rabbitmqadaptor ./utilities/rabbitmqadaptor

WORKDIR /helidon/utilities/rabbitmqadaptor
RUN mvn clean install -DskipTests

WORKDIR /helidon

ADD handlingms/pom.xml .
RUN mvn dependency:go-offline -B

ADD handlingms/src src
RUN mvn package -DskipTests
RUN echo "done!"

FROM eclipse-temurin:8-jre-jammy
WORKDIR /helidon

COPY --from=build /helidon/target/handlingms.jar ./
COPY --from=build /helidon/target/libs ./libs

CMD ["java", "-jar", "handlingms.jar"]

EXPOSE 8080