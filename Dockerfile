# Stage 1: Build the WAR file using Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# Stage 2: WildFly image with Oracle module and deployment
FROM quay.io/wildfly/wildfly:27.0.0.Final-jdk17 AS runtime

# Copy Oracle module
COPY --chown=jboss:root oracle-module /opt/jboss/wildfly/modules/com/oracle/ojdbc/

# Copy built WAR
COPY --from=builder /app/target/mybank-0.0.1.war /opt/jboss/wildfly/standalone/deployments/

# 📊 Copy JMX Exporter agent and config
COPY monitoring/jmx_prometheus_javaagent-0.20.0.jar /opt/jmx/
COPY monitoring/jmx_config.yml /opt/jmx/

# Inject agent into WildFly
RUN echo 'JAVA_OPTS="$JAVA_OPTS -javaagent:/opt/jmx/jmx_prometheus_javaagent-0.20.0.jar=9010:/opt/jmx/jmx_config.yml"' >> /opt/jboss/wildfly/bin/standalone.conf

EXPOSE 8085 9010

# Run WildFly
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
