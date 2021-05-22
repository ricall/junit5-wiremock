/*
 * Copyright (c) 2021 Richard Allwood
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.github.ricall.junit5.wiremock;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import org.github.ricall.junit5.wiremock.implementation.DefaultMockServer;

import java.util.function.Consumer;

/**
 * Builder used to create a MockServer for testing mock http services.
 * <br/>
 * To use the builder:
 * <pre>{@code
 * public class TestWireMockLibrary {
 *
 *     @RegisterExtension
 *     public MockServer server = MockServer.withPort(8085);
 *
 *     @Test
 *     public void verifyWiremockWorksAsExpected() {
 *         server.stubFor(get(urlEqualTo("/hello"))
 *                 .willReturn(aResponse()
 *                         .withStatus(200)
 *                         .withBody("Hello World")));
 *
 *         // You can now query server.url("/hello")
 *     }
 *
 * }
 * }</pre>
 */
public interface MockServer extends Admin, Stubbing {

    static MockServer with(Consumer<WireMockConfiguration> configurator) {
        final WireMockConfiguration options = WireMockConfiguration.options();
        configurator.accept(options);

        return new DefaultMockServer(options);
    }

    static MockServer withPort(int port) {
        return MockServer.with(options -> options.port(port));
    }

}
