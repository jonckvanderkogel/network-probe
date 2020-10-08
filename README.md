# Build the project
mvn clean package

# Run the resulting jar
java -jar target/network-probe-0.0.1.jar

# Run with externalized configuration
sudo java -jar target/network-probe-0.0.1.jar --spring.config.location=file:///Users/jonck/Documents/dev/code/caiway-probe/test-connectivity.properties
