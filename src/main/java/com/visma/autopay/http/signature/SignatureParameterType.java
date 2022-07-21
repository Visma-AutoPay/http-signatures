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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines types of Signature Parameters
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-10.html#name-signature-parameters">Signature Parameters</a>
 */
public enum SignatureParameterType {
    /**
     * created - creation time as an Integer UNIX timestamp value
     */
    CREATED("created"),

    /**
     * expires - expiration time as an Integer UNIX timestamp value
     */
    EXPIRES("expires"),

    /**
     * nonce - a random unique value generated for the signature as a String value
     */
    NONCE("nonce"),

    /**
     * alg - the HTTP message signature algorithm from the HTTP Message Signature Algorithm Registry, as a String value
     */
    ALGORITHM("alg"),

    /**
     * keyid - the identifier for the key material as a String value
     */
    KEY_ID("keyid"),
    ;

    private final String identifier;
    private static final Map<String, SignatureParameterType> id2Enum;

    SignatureParameterType(String identifier) {
        this.identifier = identifier;
    }

    static {
        id2Enum = Stream.of(values())
                .collect(Collectors.toMap(SignatureParameterType::getIdentifier, sp -> sp));
    }

    /**
     * Returns {@link SignatureParameterType} for given identifier, e.g. {@link #CREATED} for "created"
     *
     * @param identifier Signature Parameter Type's identifier
     * @return SignatureParameterType or null if identifier is unknown
     */
    public static SignatureParameterType fromIdentifier(String identifier) {
        return id2Enum.get(identifier);
    }

    /**
     * Returns identifier of this {@link SignatureParameterType}, e.g. "created" for {@link #CREATED}
     *
     * @return This Signature Parameter Type's identifier
     */
    public String getIdentifier() {
        return identifier;
    }
}
