# Stage 1: Build the WAR file using Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: WildFly image with Oracle module and deployment
FROM quay.io/wildfly/wildfly:27.0.0.Final-jdk17 as runtime

COPY --chown=jboss:root oracle-module /opt/jboss/wildfly/modules/com/oracle/ojdbc/

COPY --from=builder /app/target/mybank-0.0.1.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8085

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
