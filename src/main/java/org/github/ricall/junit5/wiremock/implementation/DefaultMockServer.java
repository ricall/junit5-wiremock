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

package org.github.ricall.junit5.wiremock.implementation;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.github.ricall.junit5.wiremock.MockServer;
import org.github.ricall.junit5.wiremock.WireMockUrl;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;

@RequiredArgsConstructor
public class DefaultMockServer implements MockServer, BeforeAllCallback, BeforeEachCallback, AfterEachCallback,
        AfterAllCallback, ParameterResolver {

    public static final String NAMESPACE = DefaultMockServer.class.getName();
    public static final String SERVER_KEY = WireMockServer.class.getName();

    private final Options options;

    @Delegate
    private transient JunitFriendlyWireMockServer server;
    private transient boolean serverPerMethod;

    @Override
    public void beforeAll(final ExtensionContext context) {
        initialiseServer(context);
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        if (this.server == null) {
            serverPerMethod = true;
            initialiseServer(context);
        }
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        if (serverPerMethod) {
            cleanupServer(context);
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        if (server != null) {
            cleanupServer(context);
        }
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Parameter parameter = parameterContext.getParameter();

        return isWiremockUrlParameter(parameter);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext context) {
        final Class<?> type = parameterContext.getParameter().getType();
        if (type == String.class) {
            return getWiremockUrl(context);
        }
        return null;
    }

    private JunitFriendlyWireMockServer getServer(final ExtensionContext context) {
        return context.getRoot()
                .getStore(Namespace.create(NAMESPACE))
                .get(SERVER_KEY, JunitFriendlyWireMockServer.class);
    }

    private void initialiseServer(final ExtensionContext context) {
        JunitFriendlyWireMockServer wireMockServer = getServer(context);
        if (wireMockServer == null) {
            final Store store = context.getRoot()
                    .getStore(Namespace.create(NAMESPACE));
            wireMockServer = new JunitFriendlyWireMockServer(options);
            store.put(SERVER_KEY, wireMockServer);

            wireMockServer.start();
        }
        wireMockServer.resetClientMappings();
        this.server = wireMockServer;
    }

    @SuppressWarnings("PMD.NullAssignment")
    private void cleanupServer(final ExtensionContext context) {
        getServer(context).stop();
        context.getRoot()
                .getStore(Namespace.create(NAMESPACE))
                .remove(SERVER_KEY);
        server = null;
    }

    private boolean isWiremockUrlParameter(final Parameter parameter) {
        return parameter.getType() == String.class && parameter.getAnnotation(WireMockUrl.class) != null;
    }

    private String getWiremockUrl(final ExtensionContext context) {
        return getServer(context).baseUrl();
    }

}
