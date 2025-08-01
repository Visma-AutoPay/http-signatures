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

import net.visma.autopay.http.signature.SignatureException.ErrorCode;
import net.visma.autopay.http.structured.StructuredBytes;
import net.visma.autopay.http.structured.StructuredDictionary;
import net.visma.autopay.http.structured.StructuredException;
import net.visma.autopay.http.structured.StructuredField;
import net.visma.autopay.http.structured.StructuredItem;
import net.visma.autopay.http.structured.StructuredString;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class representing HTTP Fields (Headers) Component
 * <p>
 * Both "plain" headers, structured fields and dictionary field members are represented.
 * From both "this" and "related" request.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#name-http-fields">HTTP Fields</a>
 */
final class HeaderComponent extends Component {
    private static final Set<String> ALLOWED_PARAMS = Set.of(DICTIONARY_KEY_PARAM, STRUCTURED_FIELD_PARAM, RELATED_REQUEST_PARAM, BINARY_WRAPPED_PARAM,
            TRAILER_PARAM);

    private final String headerName;
    private final String dictionaryKey;
    private final boolean structured;
    private final boolean binaryWrapped;
    private final boolean trailer;

    /**
     * Creates a Header Component of given header name
     *
     * @param headerName HTTP header name
     */
    HeaderComponent(String headerName) {
        super(StructuredString.of(headerName));
        this.headerName = headerName;
        this.dictionaryKey = null;
        this.structured = false;
        this.binaryWrapped = false;
        this.trailer = false;
    }

    /**
     * Creates a Header Component of given header
     *
     * @param headerName         HTTP header name
     * @param dictionaryKey      Dictionary key for Dictionary Structured Field Members
     * @param structured         True for Structured HTTP Fields re-serialized to their standard form
     * @param fromRelatedRequest True if component is from related request
     * @param binaryWrapped      True if header values need to be wrapped as binary structures
     * @param trailer            True if component is HTTP trailer rather than header
     */
    HeaderComponent(String headerName, String dictionaryKey, boolean structured, boolean fromRelatedRequest, boolean binaryWrapped, boolean trailer) {
        super(getStructuredName(headerName, dictionaryKey, structured, fromRelatedRequest, binaryWrapped, trailer));
        this.headerName = headerName;
        this.dictionaryKey = dictionaryKey;
        this.structured = structured;
        this.binaryWrapped = binaryWrapped;
        this.trailer = trailer;

        if (dictionaryKey != null && dictionaryKey.isEmpty()) {
            throw new IllegalArgumentException("Invalid dictionary key: " + dictionaryKey);
        }
    }

    /**
     * Creates Header Component object from its {@link StructuredString} representation
     *
     * @param structuredHeader Structured String representation of the component
     * @throws IllegalArgumentException When illegal or unknown param is provided
     */
    HeaderComponent(StructuredString structuredHeader) {
        super(structuredHeader);
        this.headerName = structuredHeader.stringValue();
        this.dictionaryKey = structuredHeader.stringParam(DICTIONARY_KEY_PARAM).orElse(null);
        this.structured = structuredHeader.boolParam(STRUCTURED_FIELD_PARAM).orElse(false);
        this.binaryWrapped = structuredHeader.boolParam(BINARY_WRAPPED_PARAM).orElse(false);
        this.trailer = structuredHeader.boolParam(TRAILER_PARAM).orElse(false);

        validateParams(structuredHeader);
    }

    private static StructuredString getStructuredName(String headerName, String dictionaryKey, boolean structured, boolean fromRelatedRequest,
                                                      boolean binaryWrapped, boolean trailer) {
        var params = new LinkedHashMap<String, Object>();

        if (fromRelatedRequest) {
            params.put(RELATED_REQUEST_PARAM, true);
        }

        if (structured && dictionaryKey == null) {
            params.put(STRUCTURED_FIELD_PARAM, true);
        }

        if (dictionaryKey != null) {
            params.put(DICTIONARY_KEY_PARAM, dictionaryKey);
        }

        if (binaryWrapped) {
            params.put(BINARY_WRAPPED_PARAM, true);
        }

        if (trailer) {
            params.put(TRAILER_PARAM, true);
        }

        return StructuredString.withParams(headerName, params);
    }

    private void validateParams(StructuredString structuredHeader) {
        for (var paramName : structuredHeader.parameters().keySet()) {
            if (!ALLOWED_PARAMS.contains(paramName)) {
                throw new IllegalArgumentException("Illegal component parameter " + paramName + " for component " + structuredHeader.stringValue());
            }
        }
    }

    @Override
    String computeValue(SignatureContext signatureContext) throws SignatureException {
        var headerValues = getFieldValue(signatureContext);
        String computedValue;

        if (headerValues == null) {
            var fieldType = trailer ? "Trailer " : "Header ";
            throw new SignatureException(ErrorCode.MISSING_HEADER, fieldType + headerName + " is missing");
        }

        if (binaryWrapped) {
            computedValue = getBinaryWrapped(headerValues);
        } else {
            var headerValue = getSingleValue(headerValues);

            if (dictionaryKey != null) {
                computedValue = getDictionaryMember(headerValue);
            } else if (structured) {
                computedValue = reSerializeStructuredField(headerValue);
            } else {
                computedValue = headerValue;
            }
        }

        return computedValue;
    }

    @Override
    boolean isValueInContext(SignatureContext signatureContext) {
        var headerValue = getFieldValue(signatureContext);

        if (headerValue == null) {
            return false;
        } else if (dictionaryKey == null) {
            return true;
        } else {
            try {
                getDictionaryMember(getSingleValue(headerValue));
                return true;
            } catch (SignatureException e) {
                return e.getErrorCode() != ErrorCode.MISSING_DICTIONARY_KEY;
            }
        }
    }

    private String getBinaryWrapped(List<String> headerValues) {
        return headerValues.stream()
                .map(value -> StructuredBytes.of(value.getBytes(StandardCharsets.UTF_8)))
                .map(StructuredItem::serialize)
                .collect(Collectors.joining(", "));
    }

    private String reSerializeStructuredField(String headerValue) throws SignatureException {
        try {
            return StructuredField.parse(headerValue).serialize();
        } catch (StructuredException e) {
            throw new SignatureException(ErrorCode.INVALID_STRUCTURED_HEADER, "Cannot parse structured header " + headerName, e);
        }
    }

    private String getDictionaryMember(String headerValue) throws SignatureException {
        StructuredDictionary dictionary;

        try {
            dictionary = StructuredDictionary.parse(headerValue);
        } catch (StructuredException e) {
            throw new SignatureException(ErrorCode.INVALID_STRUCTURED_HEADER, "Invalid structured dictionary " + headerName, e);
        }

        var optionalKeyValue = dictionary.getItem(dictionaryKey);

        if (optionalKeyValue.isPresent()) {
            return optionalKeyValue.get().serialize();
        } else {
            throw new SignatureException(ErrorCode.MISSING_DICTIONARY_KEY, "Key " + dictionaryKey + " is missing in header " + headerName);
        }
    }

    private String getSingleValue(List<String> headerValues) {
        String headerValue;

        if (headerValues.size() == 1) {
            headerValue = headerValues.get(0);
        } else {
            headerValue = headerValues.stream()
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.joining(", "));
        }

        return headerValue;
    }

    private List<String> getFieldValue(SignatureContext signatureContext) {
        return trailer ?  signatureContext.getTrailers().get(headerName) : signatureContext.getHeaders().get(headerName);
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
