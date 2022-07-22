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

import com.visma.autopay.http.signature.SignatureException.ErrorCode;
import com.visma.autopay.http.structured.StructuredDictionary;
import com.visma.autopay.http.structured.StructuredException;
import com.visma.autopay.http.structured.StructuredField;
import com.visma.autopay.http.structured.StructuredString;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Class representing HTTP Fields (Headers) Component
 * <p>
 * Both "plain" headers, canonicalized fields and dictionary field members are represented.
 * From both "this" and "related" request.
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-http-fields">HTTP Fields</a>
 */
final class HeaderComponent extends Component {
    private final String headerName;
    private final String dictionaryKey;
    private final boolean canonicalized;

    /**
     * Creates a Header Component of given header name
     *
     * @param headerName HTTP header name
     */
    HeaderComponent(String headerName) {
        super(StructuredString.of(headerName));
        this.headerName = headerName;
        this.dictionaryKey = null;
        this.canonicalized = false;
    }

    /**
     * Creates a Header Component of given header
     *
     * @param headerName         HTTP header name
     * @param dictionaryKey      Dictionary key for Dictionary Structured Field Members
     * @param canonicalized      True for Canonicalized Structured HTTP Fields
     * @param fromRelatedRequest True if component is from related request
     */
    HeaderComponent(String headerName, String dictionaryKey, boolean canonicalized, boolean fromRelatedRequest) {
        super(getStructuredName(headerName, dictionaryKey, canonicalized, fromRelatedRequest));
        this.headerName = headerName;
        this.dictionaryKey = dictionaryKey;
        this.canonicalized = canonicalized;

        if (dictionaryKey != null && dictionaryKey.isEmpty()) {
            throw new IllegalArgumentException("Invalid dictionary key: " + dictionaryKey);
        }
    }

    /**
     * Creates Header Component object from its {@link StructuredString} representation
     *
     * @param structuredHeader Structured String representation of the component
     */
    HeaderComponent(StructuredString structuredHeader) {
        super(structuredHeader);
        this.headerName = structuredHeader.stringValue();
        this.dictionaryKey = structuredHeader.stringParam(DICTIONARY_KEY_PARAM).orElse(null);
        this.canonicalized = structuredHeader.boolParam(CANONICALIZED_FIELD_PARAM).orElse(false);
    }

    private static StructuredString getStructuredName(String headerName, String dictionaryKey, boolean canonicalized, boolean fromRelatedRequest) {
        var params = new LinkedHashMap<String, Object>();

        if (fromRelatedRequest) {
            params.put(RELATED_REQUEST_PARAM, true);
        }

        if (canonicalized && dictionaryKey == null) {
            params.put(CANONICALIZED_FIELD_PARAM, true);
        }

        if (dictionaryKey != null) {
            params.put(DICTIONARY_KEY_PARAM, dictionaryKey);
        }

        return StructuredString.withParams(headerName, params);
    }

    @Override
    String computeValue(SignatureContext signatureContext) throws SignatureException {
        var headerValue = signatureContext.getHeaders().get(headerName);

        if (headerValue == null) {
            throw new SignatureException(ErrorCode.MISSING_HEADER, "Header " + headerName + " is missing");
        }

        if (dictionaryKey != null) {
            return getDictionaryMember(headerValue);
        } else if (canonicalized) {
            return getCanonicalized(headerValue);
        } else {
            return headerValue;
        }
    }

    @Override
    boolean isValueInContext(SignatureContext signatureContext) {
        var headerValue = signatureContext.getHeaders().get(headerName);

        if (headerValue == null) {
            return false;
        } else if (dictionaryKey == null) {
            return true;
        } else {
            try {
                getDictionaryMember(headerValue);
                return true;
            } catch (SignatureException e) {
                return e.getErrorCode() != ErrorCode.MISSING_DICTIONARY_KEY;
            }
        }
    }

    private String getCanonicalized(String headerValue) throws SignatureException {
        try {
            return StructuredField.parse(headerValue).serialize();
        } catch (StructuredException e) {
            throw new SignatureException(ErrorCode.INVALID_STRUCTURED_HEADER, "Cannot parse structured header " + headerName, e);
        }
    }

    private String getDictionaryMember(String headerValue) throws SignatureException {
        StructuredDictionary structured;

        try {
            structured = StructuredDictionary.parse(headerValue);
        } catch (StructuredException e) {
            throw new SignatureException(ErrorCode.INVALID_STRUCTURED_HEADER, "Invalid structured dictionary " + headerName, e);
        }

        var optionalKeyValue = structured.getItem(dictionaryKey);

        if (optionalKeyValue.isPresent()) {
            return optionalKeyValue.get().serialize();
        } else {
            throw new SignatureException(ErrorCode.MISSING_DICTIONARY_KEY, "Key " + dictionaryKey + " is missing in header " + headerName);
        }
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