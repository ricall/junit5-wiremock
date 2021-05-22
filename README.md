# JUnit 5 WireMock library

## What is it?

This JUnit 5 library for [Wiremock](http://wiremock.org) provides a way to run a WireMockServer in your JUnit code.

## How to use it
The wiremock library can be added to projects built using gradle or maven as shown below:

### Gradle
Add the dependency to gradle
```groovy
testImplementation 'io.github.ricall.junit5-wiremock:junit5-wiremock:2.0.0'
```

### Maven
Add the dependeny to mvn pom.xml
```xml
<dependency>
    <groupId>io.github.ricall.junit5-wiremock</groupId>
    <artifactId>junit5-wiremock</artifactId>
    <version>2.0.0</version>
    <scope>test</scope>
</dependency>
```

### Using the `JUnit5` extension
```java
public class TestWireMockLibrary {

    @RegisterExtension
    public MockServer server = MockServer.withPort(8085);

    @Test
    public void verifyWiremockWorksAsExpected() {
        server.stubFor(get(urlEqualTo("/hello"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Hello World")));
        
        // You can now query server.url("/hello")
    }
    
}
```

## License
This software is licensed using [MIT](https://opensource.org/licenses/MIT) 
