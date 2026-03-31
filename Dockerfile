FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy everything
COPY . .

# Give permission
RUN chmod +x mvnw

# Build the jar
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run jar
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]