# The scenario
Your Internet connection at home is flaky and you frequently experience hickups in your Teams/Zoom meetings with your colleagues and quite often the Internet connection drops altogether. Usually the outages are resolved pretty quickly and only last a few minutes but it’s annoying and disrupting your work. You have contacted your ISP but they indicate they cannot corroborate your findings and ask you to document the outages/hickups at which time they will investigate. Naturally as an IT engineer you want to automate this process.
 
# The assignment
Design and implement an application that will monitor your Internet connection. The output of the application should be the evidence you feel would be best to convince your ISP to look into your Internet connection. Your ISP was not very clear what they expected as output so use your imagination, what do you think will be the data that you would need in such a case and how should you present it? The only restriction we place upon you is that the assignment must be written in Java as this is the primary language we use in our area and we want to be able to gauge your skills in that area. For the rest feel free to use any framework/library/tool that you have at your disposal.
 
# What we are looking for
Remember the purpose of this assignment is so we can get a good impression of your coding skills in the broadest terms. So we are looking for good design and creativity in your code. We want to see you demonstrate to us that you understand industry standard best practices and that you keep up with modern trends in IT. During the interview we will be going through your code with a few experienced engineers so be prepared to present your code and explain your design choices and implementation details. We will be looking for how well you are able to convey/explain your original ideas and depth of thought.
 
# Tips
You are applying for a job at ING, the largest bank and one of the largest IT companies in the Netherlands. Demonstrate to us that you have what it takes to work here. Show us that you understand how software engineering is done in a group setting, that you produce stable, maintainable, performant and readable code that would successfully get through a peer-review with a limited number of WTF’s. Naturally we understand that you will need to timebox your solution and during our conversation you can describe to us how you would improve your solution had you had more time. Most important is to have some fun with this exercise, we want to have a nice conversation with you and this exercise will create some shared ground between us.

![image](https://i2.wp.com/commadot.com/wp-content/uploads/2009/02/wtf.png?w=550&ssl=1)

Good luck and looking forward to our interview together!

# Build the project
mvn clean package

# Run the resulting jar
java -jar target/network-probe-0.0.1.jar

# Run with externalized configuration
sudo java -jar target/network-probe-0.0.1.jar --spring.config.location=file:///Users/jonck/Documents/dev/code/caiway-probe/test-dns.properties
