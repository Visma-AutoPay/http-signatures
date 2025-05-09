/*
 * Copyright (c) 2022-2025 Visma Autopay AS
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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines types of Derived Components
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#name-derived-components">Derived Components</a>
 */
enum DerivedComponentType {
    /**
     * &#64;signature-params - the signature metadata parameters
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#signature-params">Signature Parameters</a>
     */
    SIGNATURE_PARAMS("@signature-params"),

    /**
     * &#64;method - the method used for a request
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-request-method">Method</a>
     */
    METHOD("@method"),

    /**
     * &#64;target-uri - the full target URI for a request
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-target-uri">Target URI</a>
     */
    TARGET_URI("@target-uri"),

    /**
     * &#64;authority - the authority (host) of the target URI for a request
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-request-authority">Authority</a>
     */
    AUTHORITY("@authority"),

    /**
     * &#64;scheme - the scheme of the target URI for a request
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-request-scheme">Scheme</a>
     */
    SCHEME("@scheme"),

    /**
     * &#64;request-target - the request target
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-request-target">Request Target</a>
     */
    REQUEST_TARGET("@request-target"),

    /**
     * &#64;path - the absolute path portion of the target URI for a request
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-request-path">Path</a>
     */
    PATH("@path"),

    /**
     * &#64;query - the query portion of the target URI for a request
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-request-query">Query</a>
     */
    QUERY("@query"),

    /**
     * &#64;query-param - a parsed query parameter of the target URI for a request
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-request-query-param">Query Parameters</a>
     */
    QUERY_PARAM("@query-param"),

    /**
     * &#64;status - the status code for a response
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#content-status-code">Status Code</a>
     */
    STATUS("@status"),
    ;

    private final String identifier;
    private static final Map<String, DerivedComponentType> id2Enum;

    DerivedComponentType(String identifier) {
        this.identifier = identifier;
    }

    static {
        id2Enum = Stream.of(values())
                .collect(Collectors.toMap(DerivedComponentType::getIdentifier, dc -> dc));
    }

    /**
     * Returns {@link DerivedComponentType} for given identifier, e.g. {@link #QUERY} for "&#64;query"
     *
     * @param identifier Derived Component's identifier
     * @return DerivedComponentType or null if identifier is unknown
     */
    public static DerivedComponentType fromIdentifier(String identifier) {
        return id2Enum.get(identifier);
    }

    /**
     * Returns identifier of this {@link DerivedComponentType}, e.g. "&#64;query" for {@link #QUERY}
     *
     * @return This Component Type's identifier
     */
    public String getIdentifier() {
        return identifier;
    }
}
