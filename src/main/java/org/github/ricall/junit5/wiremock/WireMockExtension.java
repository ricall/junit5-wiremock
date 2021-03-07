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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.github.ricall.junit5.wiremock.implementation.JunitFriendlyWireMockServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Objects;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

/**
 * Provides a {@link WireMockServer} for your Junit tests.
 *
 * <p>To use:</p>
 * <pre>{@code
 * @ExtendWith(WiremockExtension.class)
 * public class TestWiremockExtension {
 *
 * }
 * }</pre>
 *
 * <p>You can optionally add configuration to control how the {@link WireMockServer} is created</p>
 * <pre>{@code
 * @WiremockOptions
 * public Options options = WireMockConfiguration.options()
 *     .port(8085);
 * }</pre>
 *
 * <p>{@link WireMockExtension} will automatically inject the {@link WireMockServer} parameter into your tests:</p>
 * <pre>{@code
 * @Test
 * public void verifyWiremockWorksAsExpected(WireMockServer server) throws IOException {
 *     server.stubFor(get(urlEqualTo("/path"))
 *         .willReturn(aResponse()
 *             .withStatus(200)
 *             .withBody("OK")));
 *
 *      // Test the server...
 * }
 * }</pre>
 */
public class WireMockExtension implements BeforeEachCallback, AfterAllCallback, ParameterResolver {
    public static final String NAMESPACE = WireMockExtension.class.getName();
    public static final String SERVER_KEY = WireMockServer.class.getName();

    private JunitFriendlyWireMockServer getServer(final ExtensionContext context) {
        return context.getRoot()
                .getStore(Namespace.create(NAMESPACE))
                .get(SERVER_KEY, JunitFriendlyWireMockServer.class);
    }

    private Options buildOptions(final ExtensionContext context) {
        return context.getTestInstance()
                .flatMap(instance -> findAnnotatedFields(instance.getClass(), WireMockOptions.class).stream()
                        .map(field -> getOptionsFromField(field, instance))
                        .filter(Objects::nonNull)
                        .findFirst())
                .orElse(WireMockConfiguration.options());
    }

    private Options getOptionsFromField(final Field field, final Object testInstance) {
        try {
            return (Options) field.get(testInstance);
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException("Unable to access field " + field.getName(), iae);
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        JunitFriendlyWireMockServer server = getServer(context);
        if (server == null) {
            final Store store = context.getRoot()
                    .getStore(Namespace.create(NAMESPACE));
            server = new JunitFriendlyWireMockServer(buildOptions(context));
            store.put(SERVER_KEY, server);

            server.start();
        }
        server.resetClientMappings();
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        getServer(context).stop();
        context.getRoot()
                .getStore(Namespace.create(NAMESPACE))
                .remove(SERVER_KEY);
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Parameter parameter = parameterContext.getParameter();

        return isWiremockUrlParameter(parameter) || isWiremockServerParameter(parameter);
    }

    private boolean isWiremockUrlParameter(final Parameter parameter) {
        return parameter.getType() == String.class && parameter.getAnnotation(WireMockUrl.class) != null;
    }

    private boolean isWiremockServerParameter(final Parameter parameter) {
        return parameter.getType() == WireMockServer.class;
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext context) {
        final Class<?> type = parameterContext.getParameter().getType();
        if (type == String.class) {
            return getWiremockUrl(context);
        }
        if (type == WireMockServer.class) {
            return getServer(context);
        }
        return null;
    }

    private String getWiremockUrl(final ExtensionContext context) {
        return getServer(context).baseUrl();
    }

}
