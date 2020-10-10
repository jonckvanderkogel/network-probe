# Build the project
mvn clean package

# Run the resulting jar
java -jar target/network-probe-0.0.1.jar

# Run with externalized configuration
java -jar target/network-probe-0.0.1.jar --spring.config.location=file:///Users/jonck/Documents/dev/code/network-probe/test-connectivity.properties
