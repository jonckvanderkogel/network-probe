# Build the project
mvn clean package

# Start Elasticsearch/Kibana
docker compose up

# Run the resulting jar
java -jar target/network-probe-0.0.1.jar

# Run with externalized configuration
java -jar target/network-probe-0.0.1.jar --spring.config.location=file:///Users/jonck/Documents/dev/code/network-probe/test-connectivity.properties

# Consume the data
The data is written to Elasticsearch on the network-probe-index index. You can then use Kibana for visualization:
```
http://localhost:5601
```

Or to consume the data for your custom visualization, the stream of events can be consumed from this endpoint:
```
http://localhost:8080/response
```
This gives you a JSON response in the following format:
```
{"reachable":true,"responseTime":81,"server":"https://www.google.com","reachableState":"REACHABLE"}
```
