# JUnit 5 WireMock extension

## What is it?

This JUnit 5 extension for [Wiremock](http://wiremock.org) provides a way to run a WireMockServer in your JUnit code.

## How to use it

### Gradle
Add the dependency to gradle
```groovy
testImplementation 'io.github.ricall.junit5-wiremock:junit5-wiremock:1.0.0'
```

### Maven
Add the dependeny to mvn pom.xml
```xml
<dependency>
    <groupId>io.github.ricall.junit5-wiremock</groupId>
    <artifactId>junit5-wiremock</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

### Using the `JUnit5` extension
```java
@ExtendWith(WireMockExtension.class)
public class TestWireMockExtension {

    @WireMockOptions
    public Options options = WireMockConfiguration.options()
            .port(8085);

    @Test
    public void verifyWiremockWorksAsExpected(final WireMockServer server) {
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