# Spring boot integration

This example shows 2 methods for integrating with a PDP on a Spring boot application.

1. Using a Web Security filter - this will simply indicate whether access is granted or not
2. Using a Component which acts as a client - this method allows more flexibility as it 
also allows examining the response from the PDP


## Build the example

In order to execute the example you should have the basic prerequisites for Java development
 installed (JDK, Maven) and in addition, you will need to install the Spring PDP Client Jar.
 (see https://github.com/build-security/opa-java-spring-client )

Install the PDPClient JAR:

    mvn install:install-file \
    -Dfile=./opa-java-spring-client-0.3.0.jar \
    -DgroupId=build.security \
    -DartifactId=opa-java-spring-client \
    -Dversion=0.3.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

Verify that the project can be built:

    mvn install

Run the Spring Boot application:

    java -jar target/spring-boot-opa-demo-0.0.1-SNAPSHOT.jar

## Running and configuring the PDP

You can manually configure the PDP with policy and data by following the instructions found
 here:  https://github.com/build-security/opa-java-spring-client/#try-it-out
 
You can also configure the policy and data using build.security's control-plane and 
launching a PDP that was defined in the control plane.

## Working with the example

### Web Security

Call the /websecurity endpoint in order to see the web-security filter working with the PDP

    curl --location --request GET 'localhost:8080/websecurity' 

### PDP Client (SDK)

Call the /sdk endpoint in order to have the PDP client interact with the PDP

    curl --location --request GET 'localhost:8080/sdk'   
