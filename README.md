# Build the project
mvn clean package

# Run the resulting jar
sudo java -jar target/caiway-probe-0.0.1.jar

Note that you need to run as root since ping requires the proper privileges to get access to the raw socket which is what ICMP uses.

# Run with externalized configuration
sudo java -jar target/caiway-probe-0.0.1.jar --spring.config.location=file:///Users/jonck/Documents/dev/code/caiway-probe/test-dns.properties

The included properties are set up to run with the DNS servers of Caiway but if you want to run for any other DNS server pair just override the settings and inject as described above.