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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * Builder class for constructing HTTP Message Components. Used when signing and verifying signatures.
 * Contains component definitions - values should be provided in {@link SignatureContext}.
 * Internally, backed by a list of {@link Component} objects.
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-http-message-components">HTTP Message Components</a>
 * @see Component
 */
public class SignatureComponents {
    private final List<Component> components;

    private SignatureComponents(List<Component> components) {
        this.components = components;
    }

    /**
     * Returns list of components hold by this Signature Components object
     *
     * @return Component list
     */
    List<Component> getComponents() {
        return components;
    }

    /**
     * Returns a builder used to construct {@link SignatureComponents} object
     * <p>
     * When creating signatures, order of components added in the builder is preserved in produced content of <em>Signature-Input</em>.
     * Adding the same component multiple times will create duplicates - no checks in that area are performed.
     *
     * @return A SignatureComponents builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class to build {@link SignatureComponents} objects
     */
    public static class Builder {
        private final List<Component> components;

        private Builder() {
            components = new ArrayList<>();
        }

        /**
         * Adds a single HTTP Field (header) component
         *
         * @param headerName Header name
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-http-fields">HTTP Fields</a>
         * @see HeaderComponent
         */
        public Builder header(String headerName) {
            components.add(new HeaderComponent(headerName.toLowerCase()));
            return this;
        }

        /**
         * Adds multiple HTTP Field (header) components
         *
         * @param headerNames Header names
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-http-fields">HTTP Fields</a>
         * @see HeaderComponent
         */
        public Builder headers(Collection<String> headerNames) {
            headerNames.forEach(this::header);
            return this;
        }

        /**
         * Adds multiple HTTP Field (header) components
         *
         * @param headerNames Header names
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-http-fields">HTTP Fields</a>
         * @see HeaderComponent
         */
        public Builder headers(String... headerNames) {
            for (String headerName : headerNames) {
                header(headerName);
            }
            return this;
        }

        /**
         * Adds a single HTTP Field (header) component for Related Request (req)
         *
         * @param headerName Header name from the related request
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-http-fields">HTTP Fields</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>*
         * @see HeaderComponent
         */
        public Builder relatedRequestHeader(String headerName) {
            components.add(new HeaderComponent(headerName.toLowerCase(), null, false, true, false));
            return this;
        }

        /**
         * Adds a single Canonicalized Structured Field (header) component
         *
         * @param headerName Header name
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-canonicalized-structured-ht">
         *      Canonicalized Structured HTTP Fields</a>
         * @see HeaderComponent
         */
        public Builder canonicalizedHeader(String headerName) {
            components.add(new HeaderComponent(headerName.toLowerCase(), null, true, false, false));
            return this;
        }

        /**
         * Adds a single Canonicalized Structured Field (header) component for Related Request
         *
         * @param headerName Header name
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-canonicalized-structured-ht">
         *      Canonicalized Structured HTTP Fields</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see HeaderComponent
         */
        public Builder relatedRequestCanonicalizedHeader(String headerName) {
            components.add(new HeaderComponent(headerName.toLowerCase(), null, true, true, false));
            return this;
        }

        /**
         * Adds a single Dictionary Structured Field Member component
         *
         * @param headerName    Header name
         * @param dictionaryKey Dictionary member key
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-dictionary-structured-field">
         *      Dictionary Structured Field Members</a>
         * @see HeaderComponent
         */
        public Builder dictionaryMember(String headerName, String dictionaryKey) {
            components.add(new HeaderComponent(headerName.toLowerCase(), Objects.requireNonNull(dictionaryKey), false, false, false));
            return this;
        }

        /**
         * Adds a single Dictionary Structured Field Member component for Related Request
         *
         * @param headerName    Header name
         * @param dictionaryKey Dictionary member key
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-dictionary-structured-field">
         *      Dictionary Structured Field Members</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see HeaderComponent
         */
        public Builder relatedRequestDictionaryMember(String headerName, String dictionaryKey) {
            components.add(new HeaderComponent(headerName.toLowerCase(), Objects.requireNonNull(dictionaryKey), false, true, false));
            return this;
        }

        /**
         * Adds a single Binary-wrapped HTTP Field component
         *
         * @param headerName Header name
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-binary-wrapped-http-fields">
         *      Binary-wrapped HTTP Fields</a>
         */
        public Builder binaryWrappedHeader(String headerName) {
            components.add(new HeaderComponent(headerName.toLowerCase(), null, false, false, true));
            return this;
        }

        /**
         * Adds a single Binary-wrapped HTTP Field component for Related request
         *
         * @param headerName Header name
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-binary-wrapped-http-fields">
         *      Binary-wrapped HTTP Fields</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         */
        public Builder relatedRequestBinaryWrappedHeader(String headerName) {
            components.add(new HeaderComponent(headerName.toLowerCase(), null, false, true, true));
            return this;
        }

        private Builder derivedComponent(DerivedComponentType component) {
            components.add(new DerivedComponent(component));
            return this;
        }

        private Builder relatedRequestDerivedComponent(DerivedComponentType component) {
            components.add(new DerivedComponent(component, null, true));
            return this;
        }

        /**
         * Adds a &#64;method derived component - the method used for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-method">Method</a>
         * @see DerivedComponent
         */
        public Builder method() {
            return derivedComponent(DerivedComponentType.METHOD);
        }

        /**
         * Adds a &#64;method derived component for Related Request - the method used for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-method">Method</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see DerivedComponent
         */
        public Builder relatedRequestMethod() {
            return relatedRequestDerivedComponent(DerivedComponentType.METHOD);
        }

        /**
         * Adds a &#64;target-uri derived component - the full target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-target-uri">Target URI</a>
         * @see DerivedComponent
         */
        public Builder targetUri() {
            return derivedComponent(DerivedComponentType.TARGET_URI);
        }

        /**
         * Adds a &#64;target-uri derived component for Related Request - the full target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-target-uri">Target URI</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see DerivedComponent
         */
        public Builder relatedRequestTargetUri() {
            return relatedRequestDerivedComponent(DerivedComponentType.TARGET_URI);
        }

        /**
         * Adds an &#64;authority derived component - the authority (host) of the target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-authority">Authority</a>
         * @see DerivedComponent
         */
        public Builder authority() {
            return derivedComponent(DerivedComponentType.AUTHORITY);
        }

        /**
         * Adds an &#64;authority derived component for Related Request - the authority (host) of the target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-authority">Authority</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see DerivedComponent
         */
        public Builder relatedRequestAuthority() {
            return relatedRequestDerivedComponent(DerivedComponentType.AUTHORITY);
        }

        /**
         * Adds a &#64;scheme derived component - the scheme of the target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-scheme">Scheme</a>
         * @see DerivedComponent
         */
        public Builder scheme() {
            return derivedComponent(DerivedComponentType.SCHEME);
        }

        /**
         * Adds a &#64;scheme derived component for Related Request - the scheme of the target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-scheme">Scheme</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see DerivedComponent
         */
        public Builder relatedRequestScheme() {
            return relatedRequestDerivedComponent(DerivedComponentType.SCHEME);
        }

        /**
         * Adds a &#64;request-target derived component
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-target">Request Target</a>
         * @see DerivedComponent
         */
        public Builder requestTarget() {
            return derivedComponent(DerivedComponentType.REQUEST_TARGET);
        }

        /**
         * Adds a &#64;request-target derived component for Related Request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-target">Request Target</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see DerivedComponent
         */
        public Builder relatedRequestRequestTarget() {
            return relatedRequestDerivedComponent(DerivedComponentType.REQUEST_TARGET);
        }

        /**
         * Adds a &#64;path derived component - the absolute path portion of the target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-path">Path</a>
         * @see DerivedComponent
         */
        public Builder path() {
            return derivedComponent(DerivedComponentType.PATH);
        }

        /**
         * Adds a &#64;path derived component for Related Request - the absolute path portion of the target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-path">Path</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see DerivedComponent
         */
        public Builder relatedRequestPath() {
            return relatedRequestDerivedComponent(DerivedComponentType.PATH);
        }

        /**
         * Adds a &#64;query derived component - the query portion of the target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-query">Query</a>
         * @see DerivedComponent
         */
        public Builder query() {
            return derivedComponent(DerivedComponentType.QUERY);
        }

        /**
         * Adds a &#64;query derived component for Related Request - the query portion of the target URI for a request
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-query">Query</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see DerivedComponent
         */
        public Builder relatedRequestQuery() {
            return relatedRequestDerivedComponent(DerivedComponentType.QUERY);
        }

        /**
         * Adds a &#64;query-param derived component - a parsed query parameter of the target URI for a request
         *
         * @param paramName Query parameter name
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-query-param">Query Parameters</a>
         * @see DerivedComponent
         */
        public Builder queryParam(String paramName) {
            components.add(new DerivedComponent(DerivedComponentType.QUERY_PARAM, paramName, false));
            return this;
        }

        /**
         * Adds a &#64;query-param derived component for Related Request - a parsed query parameter of the target URI for a request
         *
         * @param paramName Query parameter name
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-request-query-param">Query Parameters</a>
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-request-response-signature-">
         *      Request-Response Signature Binding</a>
         * @see DerivedComponent
         */
        public Builder relatedRequestQueryParam(String paramName) {
            components.add(new DerivedComponent(DerivedComponentType.QUERY_PARAM, paramName, true));
            return this;
        }

        /**
         * Adds a &#64;status derived component - the status code for a response
         *
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#content-status-code">Status Code</a>
         * @see DerivedComponent
         */
        public Builder status() {
            return derivedComponent(DerivedComponentType.STATUS);
        }

        /**
         * Constructs {@link SignatureComponents} object from this builder
         *
         * @return SignatureComponents object
         */
        public SignatureComponents build() {
            return new SignatureComponents(components);
        }
    }

    /**
     * String representation of this object
     *
     * @return String representation of this SignatureComponents
     */
    @Override
    public String toString() {
        return "SignatureComponents{" +
                "components=" + components +
                '}';
    }

    /**
     * Compares the specified object with this SignatureComponents for equality. Returns true if the given object is of the same class as this
     * SignatureComponents, and all contained components are equal.
     *
     * @param o Object to be compared with this SignatureComponents
     * @return True is specified object is equal to this SignatureComponents
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (SignatureComponents) o;
        return components.equals(that.components);
    }

    /**
     * Returns hash code for this SignatureComponents, which is composed of hash codes of all contained components
     *
     * @return The hash code for this SignatureComponents
     */
    @Override
    public int hashCode() {
        return Objects.hash(components);
    }
}
