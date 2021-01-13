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

package au.org.rma.junit5.wiremock;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public final class SimpleService {
    private final URI baseUrl;
    private final OkHttpClient client = new OkHttpClient();

    public SimpleService(final String baseUrl) throws MalformedURLException, URISyntaxException {
        this.baseUrl = new URL(baseUrl).toURI();
    }

    public String query(final String path) throws IOException {
        final Request request = new Request.Builder()
                .url(baseUrl.resolve(path).toURL())
                .header("Accept", "text/plain")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return Optional.of(response)
                    .map(Response::body)
                    .map(this::responseAsString)
                    .orElse(null);
        }
    }

    private String responseAsString(final ResponseBody response) {
        try {
            return response.string();
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to get body string", ioe);
        }
    }

}
