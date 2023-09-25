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

import net.visma.autopay.http.structured.StructuredString;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Class representing Derived Components
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-19.html#name-derived-components">Derived Components</a>
 */
final class DerivedComponent extends Component {
    private final DerivedComponentType componentType;
    private final String queryParamName;

    /**
     * Creates a Derived Component of given type
     *
     * @param componentType Component type. Must not be {@link DerivedComponentType#QUERY_PARAM} - for query params use
     *                      {@link #DerivedComponent(DerivedComponentType, String, boolean)}.
     */
    DerivedComponent(DerivedComponentType componentType) {
        super(StructuredString.of(componentType.getIdentifier()));
        this.componentType = componentType;
        this.queryParamName = null;
    }

    /**
     * Creates a Derived Component
     *
     * @param componentType      Component Type
     * @param queryParamName     Param name for {@link DerivedComponentType#QUERY_PARAM}
     * @param fromRelatedRequest True if component is from related request
     * @throws NullPointerException When param name is not given for &#64;query-param component
     */
    DerivedComponent(DerivedComponentType componentType, String queryParamName, boolean fromRelatedRequest) {
        super(getStructuredName(componentType, queryParamName, fromRelatedRequest));
        this.componentType = componentType;

        if (componentType == DerivedComponentType.QUERY_PARAM && (queryParamName == null || queryParamName.isEmpty())) {
            throw new NullPointerException("Query param name is missing");
        }

        if (componentType == DerivedComponentType.QUERY_PARAM) {
            queryParamName = encodeQueryPart(queryParamName);
        }

        this.queryParamName = queryParamName;
    }

    /**
     * Creates Derived Component object from its {@link StructuredString} representation
     *
     * @param structuredHeader Structured String representation of the component
     * @throws NullPointerException     When param name is not given for &#64;query-param component
     * @throws IllegalArgumentException When illegal or unknown param is provided
     */
    DerivedComponent(StructuredString structuredHeader) {
        super(structuredHeader);
        this.componentType = Objects.requireNonNull(DerivedComponentType.fromIdentifier(structuredHeader.stringValue()));
        this.queryParamName = structuredHeader.stringParam(QUERY_PARAM_NAME_PARAM).orElse(null);

        validateParams(structuredHeader);
    }

    private static StructuredString getStructuredName(DerivedComponentType componentType, String queryParamName, boolean fromRelatedRequest) {
        var params = new LinkedHashMap<String, Object>();

        if (fromRelatedRequest) {
            params.put(RELATED_REQUEST_PARAM, true);
        }

        if (componentType == DerivedComponentType.QUERY_PARAM && queryParamName != null) {
            params.put(QUERY_PARAM_NAME_PARAM, encodeQueryPart(queryParamName));
        }

        return StructuredString.withParams(componentType.getIdentifier(), params);
    }

    private void validateParams(StructuredString structuredHeader) {
        Set<String> allowedParams;

        if (componentType == DerivedComponentType.QUERY_PARAM) {
            if (queryParamName == null || queryParamName.isEmpty()) {
                throw new NullPointerException("Query param name is missing");
            }

            allowedParams = Set.of(QUERY_PARAM_NAME_PARAM, RELATED_REQUEST_PARAM);
        } else {
            allowedParams = Set.of(RELATED_REQUEST_PARAM);
        }

        for (var paramName : structuredHeader.parameters().keySet()) {
            if (!allowedParams.contains(paramName)) {
                throw new IllegalArgumentException("Illegal component parameter " + paramName + " for component " + structuredHeader.stringValue());
            }
        }
    }

    @Override
    String computeValue(SignatureContext signatureContext) throws SignatureException {
        var targetUri = signatureContext.getTargetUri();

        try {
            switch (componentType) {
                case QUERY_PARAM:
                    return getQueryParamValue(targetUri.getRawQuery());
                case METHOD:
                    return Objects.requireNonNull(signatureContext.getMethod());
                case TARGET_URI:
                    return targetUri.toString();
                case AUTHORITY:
                    return Objects.requireNonNull(targetUri.getAuthority());
                case SCHEME:
                    return Objects.requireNonNull(targetUri.getScheme());
                case REQUEST_TARGET:
                    return formatPath(targetUri.getRawPath()) + (targetUri.getRawQuery() != null ? formatQuery(targetUri.getRawQuery()) : "");
                case PATH:
                    return formatPath(targetUri.getRawPath());
                case QUERY:
                    return '?' + Objects.toString(targetUri.getRawQuery(), "");
                case STATUS:
                    return Integer.toString(signatureContext.getStatus());
                case SIGNATURE_PARAMS:
                default:
                    throw new IllegalArgumentException();
            }
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureException(SignatureException.ErrorCode.MISSING_COMPONENT, "Unable to compute value of " + componentType.getIdentifier(), e);
        }
    }

    @Override
    boolean isValueInContext(SignatureContext signatureContext) {
        var targetUri = signatureContext.getTargetUri();
        var uriPresent = targetUri != null;

        switch (componentType) {
            case QUERY_PARAM:
                return uriPresent && isQueryInContext(targetUri.getRawQuery()) && isQueryParamInContext(targetUri.getRawQuery());
            case METHOD:
                return signatureContext.getMethod() != null;
            case TARGET_URI:
            case REQUEST_TARGET:
                return uriPresent;
            case AUTHORITY:
                return uriPresent && targetUri.getAuthority() != null;
            case SCHEME:
                return uriPresent && targetUri.getScheme() != null;
            case PATH:
                return uriPresent && isPathInContext(targetUri.getRawPath());
            case QUERY:
                return uriPresent && isQueryInContext(targetUri.getRawQuery());
            case STATUS:
                return signatureContext.getStatus() != null;
            case SIGNATURE_PARAMS:
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean isPathInContext(String path) {
        return path != null && !path.isEmpty();
    }

    private boolean isQueryInContext(String query) {
        return query != null;
    }

    private boolean isQueryParamInContext(String query) {
        try {
            getQueryParamValue(query);
            return true;
        } catch (SignatureException e) {
            return false;
        }
    }

    private String formatQuery(String query) {
        return '?' + Objects.toString(query, "");
    }

    private String formatPath(String path) {
        return (path == null || path.isEmpty()) ? "/" : path;
    }

    private String getQueryParamValue(String query) throws SignatureException {
        if (query == null || query.isEmpty()) {
            throw new SignatureException(SignatureException.ErrorCode.MISSING_QUERY_PARAM, "Query is missing");
        }

        for (String pair : query.split("&")) {
            var idx = pair.indexOf("=");
            var key = idx > 0 ? pair.substring(0, idx) : pair;

            if (queryParamName.equals(reEncodeQueryPart(key))) {
                var rawValue = (idx > 0 && pair.length() > idx + 1) ? pair.substring(idx + 1) : "";
                return reEncodeQueryPart(rawValue);
            }
        }

        throw new SignatureException(SignatureException.ErrorCode.MISSING_QUERY_PARAM, "Query param " + queryParamName + " is missing");
    }

    private static String reEncodeQueryPart(String rawQueryPart) {
        return encodeQueryPart(URLDecoder.decode(rawQueryPart, StandardCharsets.UTF_8));
    }

    private static String encodeQueryPart(String decodedQueryPart) {
        return URLEncoder.encode(decodedQueryPart, StandardCharsets.UTF_8).replace("+", "%20");
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}
