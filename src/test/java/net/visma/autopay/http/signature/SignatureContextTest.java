/*
 * Copyright (c) 2022-2023 Visma Autopay AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.visma.autopay.http.signature;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;


class SignatureContextTest {
    @Test
    void multipleHeadersAreAdded() {
        // setup
        var headers = Map.of(
                "header-1", List.of("one", "two"),
                "header-2", Arrays.asList("", "xx", "yy")
        );

        // execute
        var signatureContext = SignatureContext.builder().headers(headers).build();

        // verify
        assertThat(signatureContext.getHeaders()).containsOnly(entry("header-1", List.of("one", "two")), entry("header-2", List.of("", "xx", "yy")));
    }

    @Test
    void signatureContextCreatedFromHttpServletRequest() {
        // setup
        HttpServletRequestStub request = new HttpServletRequestStub(new StringBuffer("http://localhost"), "country=NO");
        request.addHttpHeaders("header1", List.of("header1Value1", "header1Value2"));
        request.addHttpHeaders("header2", List.of("header2Value"));

        // execute
        var signatureContext = SignatureContext.builder()
                .headers(request.getHeaderNames(), request::getHeaders)
                .targetUri(request.getRequestUrl(), request.getQueryString())
                .build();

        // verify
        assertThat(signatureContext.getHeaders()).containsOnly(
                entry("header1", List.of("header1Value1", "header1Value2")), entry("header2", List.of("header2Value")));
        assertThat(signatureContext.getTargetUri()).isNotNull();
        assertThat(signatureContext.getTargetUri()).hasToString("http://localhost?country=NO");
    }

    @Test
    void signatureContextCreatedFromHttpServletResponse() {
        HttpServletResponseStub response = new HttpServletResponseStub();
        response.addHttpHeaders("header1", List.of("header1Value1", "header1Value2"));
        response.addHttpHeaders("header2", List.of("header2Value"));

        // execute
        var signatureContext = SignatureContext.builder()
                .headers(response.getHeaderNames(), response::getHeaders)
                .build();

        // verify
        assertThat(signatureContext.getHeaders()).containsOnly(
                entry("header1", List.of("header1Value1", "header1Value2")), entry("header2", List.of("header2Value")));
    }

    private static class HttpServletRequestStub {

        private final StringBuffer requestUrl;
        private final String queryString;
        private final Map<String, List<String>> httpHeaders = new HashMap<>();
        private final List<String> headerNames = new ArrayList<>();

        public HttpServletRequestStub(StringBuffer requestUrl, String queryString) {
            this.requestUrl = requestUrl;
            this.queryString = queryString;
        }

        public void addHttpHeaders(String headerName, List<String> headerValues) {
            this.headerNames.add(headerName);
            this.httpHeaders.put(headerName, headerValues);
        }

        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(this.headerNames);
        }

        public Enumeration<String> getHeaders(String name) {
            return Collections.enumeration(this.httpHeaders.get(name));
        }

        public StringBuffer getRequestUrl() {
            return this.requestUrl;
        }

        public String getQueryString() {
            return this.queryString;
        }
    }

    private static class HttpServletResponseStub {

        private final Map<String, List<String>> httpHeaders = new HashMap<>();
        private final List<String> headerNames = new ArrayList<>();

        public HttpServletResponseStub() {
        }

        public Collection<String> getHeaderNames() {
            return this.headerNames;
        }

        public Collection<String> getHeaders(String name) {
            return this.httpHeaders.get(name);
        }

        public void addHttpHeaders(String headerName, List<String> headerValues) {
            this.headerNames.add(headerName);
            this.httpHeaders.put(headerName, headerValues);
        }
    }
}
