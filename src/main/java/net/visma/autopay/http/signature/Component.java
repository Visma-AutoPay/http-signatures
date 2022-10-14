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
package net.visma.autopay.http.signature;

import net.visma.autopay.http.structured.StructuredString;

import java.util.Objects;

/**
 * Base class for HTTP Message Components
 * <p>
 * Backed by {@link StructuredString}. Contains component definition - value should be provided in {@link SignatureContext}
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-13.html#name-http-message-components">HTTP Message Components</a>
 */
abstract class Component {
    /**
     * Structured Parameter key for Structured Dictionary members
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-13.html#name-dictionary-structured-field">
     *      Dictionary Structured Field Members</a>
     */
    protected static final String DICTIONARY_KEY_PARAM = "key";

    /**
     * Structured Parameter key for Canonicalized fields
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-13.html#name-canonicalized-structured-ht">
     *      Canonicalized Structured HTTP Fields</a>
     */
    protected static final String CANONICALIZED_FIELD_PARAM = "sf";

    /**
     * Structured Parameter key for components from Related Request
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-13.html#name-request-response-signature-">
     *      Request-Response Signature Binding</a>
     */
    protected static final String RELATED_REQUEST_PARAM = "req";

    /**
     * Structured Parameter key for Query Param name
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-13.html#name-query-parameters">Query Parameters</a>
     */
    protected static final String QUERY_PARAM_NAME_PARAM = "name";

    /**
     * Structures Parameter key for Binary-wrapped Fields
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-13.html#name-binary-wrapped-http-fields">
     *      Binary-wrapped HTTP Fields</a>
     */
    protected static final String BINARY_WRAPPED_PARAM = "bs";

    private final StructuredString name;

    /**
     * Constructs component of the given component name
     *
     * @param name Name given as sf-string. Must contain all structured parameters defined for the component.
     */
    protected Component(StructuredString name) {
        this.name = name;
    }

    /**
     * Returns component name as sf-string
     *
     * @return Component name
     */
    StructuredString getName() {
        return name;
    }

    /**
     * Extracts value of component from given signature context. Result is to be directly used when creating signature base.
     *
     * @param signatureContext Context to extract value from. If component refers to the related request then the parameter must contain related request's
     *                         context.
     * @return Component value to be included in signature base
     * @throws SignatureException Thrown in case of missing or malformatted data in the context
     */
    String extractValue(SignatureContext signatureContext) throws SignatureException {
        if (isFromRelatedRequest()) {
            signatureContext = signatureContext.getRelatedRequestContext();

            if (signatureContext == null) {
                throw new SignatureException(SignatureException.ErrorCode.MISSING_RELATED_REQUEST, "Related request context is missing");
            }
        }

        return computeValue(signatureContext);
    }

    /**
     * Extracts value of component from given signature context. Result is to be directly used when creating signature base.
     *
     * @param signatureContext Context to extract value from. If component refers to the related request then related request's context is provided.
     * @return Component value to be included in signature base
     * @throws SignatureException Thrown in case of missing or malformatted data in the context
     */
    abstract String computeValue(SignatureContext signatureContext) throws SignatureException;

    /**
     * Returns true if value for this component can be computed for given context, e.g. if the header defined by this component is present there.
     *
     * @param signatureContext Context to extract value from. It might contain the context of the related request.
     * @return True if given context contains value defined by this component
     */
    boolean isValuePresent(SignatureContext signatureContext) {
        if (isFromRelatedRequest()) {
            signatureContext = signatureContext.getRelatedRequestContext();
            return signatureContext != null && isValueInContext(signatureContext);
        } else {
            return isValueInContext(signatureContext);
        }
    }

    /**
     * Returns true if value for this component can be computed for given context, e.g. if the header defined by this component is present there.
     *
     * @param signatureContext Context to extract value from. If component refers to the related request then related request's context is provided.
     * @return True if given context contains value defined by this component
     */
    abstract boolean isValueInContext(SignatureContext signatureContext);

    private boolean isFromRelatedRequest() {
        return name.boolParam(RELATED_REQUEST_PARAM).orElse(false);
    }

    /**
     * Compares the specified object with this Component for equality. Returns true if the given object is of the same class as this Component, and has the
     * same Structured String definition.
     *
     * @param o Object to be compared with this Component
     * @return True is specified object is equal to this Component
     * @see StructuredString#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var component = (Component) o;

        return name.equals(component.name);
    }

    /**
     * Returns hash code for this Component, which is equal to the hash of its Structured String definition
     *
     * @return The hash code for this Component
     * @see StructuredString#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the string representation of this Component
     *
     * @return A string representation of this Component
     * @see StructuredString#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "name=" + name +
                '}';
    }
}
