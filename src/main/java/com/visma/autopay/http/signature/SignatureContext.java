/*
 * Copyright (c) 2022 Visma Autopay AS
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
package com.visma.autopay.http.signature;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Container for actual values used in HTTP requests and responses, like HTTP header values, status code, HTTP method, target URI.
 * <p>
 * Values stored in Signature Context are extracted during signature creation and verification by using component definitions stored in
 * {@link SignatureComponents}.
 * Values for Related Request are stored in an inner SignatureContext object.
 */
public class SignatureContext {
    private final Integer status;
    private final String method;
    private final URI targetUri;
    private final Map<String, String> headers;
    private final SignatureContext relatedRequestContext;

    private SignatureContext(Integer status, String method, URI targetUri, Map<String, String> headers, SignatureContext relatedRequestContext) {
        this.status = status;
        this.method = method;
        this.targetUri = targetUri;
        this.headers = headers;
        this.relatedRequestContext = relatedRequestContext;
    }

    /**
     * Returns status code of HTTP response
     *
     * @return Status code
     */
    Integer getStatus() {
        return status;
    }

    /**
     * Returns HTTP method of HTTP request
     *
     * @return HTTP method name, uppercase
     */
    String getMethod() {
        return method;
    }

    /**
     * Returns target URI of HTTP request
     *
     * @return Target URI
     */
    URI getTargetUri() {
        return targetUri;
    }

    /**
     * Returns a map of HTTP headers. Map keys are header names, lowercase. Map values are header values.
     *
     * @return Map of http headers
     */
    Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns Signature Context for Related Request
     *
     * @return SignatureContext of Related Request
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
     *      Request-Response Signature Binding</a>
     */
    SignatureContext getRelatedRequestContext() {
        return relatedRequestContext;
    }

    /**
     * Returns builder used to construct {@link SignatureContext} object
     *
     * @return A SignatureContext builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class to build {@link SignatureContext} objects
     */
    public static class Builder {
        private Integer status;
        private String method;
        private URI targetUri;
        private final Map<String, String> headers;
        private SignatureContext relatedRequestContext;

        private Builder() {
            headers = new HashMap<>();
        }

        /**
         * Sets HTTP status code
         *
         * @param status Status code
         * @return This builder
         */
        public Builder status(int status) {
            this.status = status;
            return this;
        }

        /**
         * Sets HTTP method name
         * <p>
         * Method is internally converted to uppercase
         *
         * @param method HTTP method.
         * @return This builder
         */
        public Builder method(String method) {
            this.method = method.toUpperCase();
            return this;
        }

        /**
         * Sets target URI
         *
         * @param targetUri Target URI given as {@link URI} object
         * @return This builder
         */
        public Builder targetUri(URI targetUri) {
            this.targetUri = targetUri;
            return this;
        }

        /**
         * Sets target URI
         *
         * @param targetUri Target URI given as {@link String}
         * @return This builder
         */
        public Builder targetUri(String targetUri) {
            this.targetUri = URI.create(targetUri);
            return this;
        }

        /**
         * Adds an HTTP header of given name and value
         * <p>
         * Internally, the name is converted to lowercase, and the value is converted to its toString() representation.
         * When called multiple times with the same headerName, header values are concatenated by using a single space and a single comma,
         * with stripped leading and trailing whitespaces.
         *
         * @param headerName  HTTP header (field) name
         * @param headerValue Header value
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-http-fields">HTTP Fields</a>
         */
        public Builder header(String headerName, Object headerValue) {
            headerName = headerName.toLowerCase();
            var sanitizedValue = sanitizeHeaderValue(headerValue);

            if (headers.containsKey(headerName)) {
                var existingValue = headers.get(headerName);

                if (existingValue == null || existingValue.isEmpty()) {
                    headers.put(headerName, sanitizedValue);
                } else if (sanitizedValue != null && !sanitizedValue.isEmpty()) {
                    headers.put(headerName, existingValue + ", " + sanitizedValue);
                }
            } else {
                headers.put(headerName, sanitizedValue);
            }

            return this;
        }

        /**
         * Adds HTTP headers from given map
         * <p>
         * Map keys are header names. Map values are header values.
         * Internally, names is converted to lowercase, and values are converted to their toString() representation.
         * <p>
         * If the value is an instance of {@link Iterable}, like List or Set, it's treated as multiple values of the header.
         * This way, <em>multimap</em> header holders, like {@code javax.ws.rs.core.MultivaluedMap}, can be used directly.
         * Multiple header values are concatenated by using a single space and a single comma, with stripped leading and trailing whitespaces.
         *
         * @param headers A map of HTTP header names and values
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-http-fields">HTTP Fields</a>
         */
        public Builder headers(Map<String, ?> headers) {
            for (var entry : headers.entrySet()) {
                var headerName = entry.getKey();
                var headerValue = entry.getValue();

                if (headerValue instanceof Iterable<?>) {
                    ((Iterable<?>) headerValue).forEach(val -> header(headerName, val));
                } else {
                    header(headerName, headerValue);
                }
            }

            return this;
        }

        /**
         * Sets Signature Context of the Related Request
         * @param relatedRequestContext Related Request's Signature Context
         * @return This builder
         */
        public Builder relatedRequest(SignatureContext relatedRequestContext) {
            this.relatedRequestContext = relatedRequestContext;
            return this;
        }

        /**
         * Constructs {@link PublicKeyInfo} object from this builder
         *
         * @return SignatureContext object
         */
        public SignatureContext build() {
            return new SignatureContext(status, method, targetUri, headers, relatedRequestContext);
        }

        private String sanitizeHeaderValue(Object headerValue) {
            if (headerValue != null) {
                headerValue = Objects.toString(headerValue)
                        .lines()
                        .map(String::strip)
                        .filter(str -> !str.isEmpty())
                        .collect(Collectors.joining(" "));
            }

            return (String) headerValue;
        }
    }

    /**
     * String representation of this object
     *
     * @return String representation of this SignatureContext
     */
    @Override
    public String toString() {
        return "SignatureContext{" +
                "status=" + status +
                ", method='" + method + '\'' +
                ", targetUri=" + targetUri +
                ", headers=" + headers +
                ", relatedRequestContext=" + relatedRequestContext +
                '}';
    }

    /**
     * Compares the specified object with this SignatureContext for equality. Returns true if the given object is of the same class as this
     * SignatureContext, and all their fields are equal.
     *
     * @param o Object to be compared with this SignatureContext
     * @return True is specified object is equal to this SignatureContext
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (SignatureContext) o;

        return Objects.equals(status, that.status) && Objects.equals(method, that.method) && Objects.equals(targetUri, that.targetUri)
                && Objects.equals(headers, that.headers) && Objects.equals(relatedRequestContext, that.relatedRequestContext);
    }

    /**
     * Returns hash code for this SignatureContext, which is composed of hash codes of all object fields
     *
     * @return The hash code for this SignatureContext
     */
    @Override
    public int hashCode() {
        return Objects.hash(status, method, targetUri, headers, relatedRequestContext);
    }
}
