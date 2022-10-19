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

import java.util.Objects;

/**
 * Thrown in case of problems when computing or verifying signatures, or when verified signature is incorrect
 */
public class SignatureException extends Exception {
    private static final long serialVersionUID = -575176407710192023L;

    /**
     * Error code for this exception
     */
    private final ErrorCode errorCode;

    /**
     * Constructs new exception with provided error code and message
     *
     * @param errorCode Error code
     * @param message Detail message
     */
    SignatureException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    /**
     * Constructs new exception with provided error code, message and cause
     *
     * @param errorCode Error code
     * @param message Detail message
     * @param cause Exception causing the constructed one
     */
    SignatureException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    /**
     * Returns error code
     *
     * @return Error code of this exception
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Error codes to classify Signature Exceptions
     */
    public enum ErrorCode {
        /**
         * When verifying, provided signature does not match computed Signature Base and provided public key. It's syntactically correct but the value itself
         * is not correct. Exceptions' message contains used algorithm and computed Signature Base.
         */
        INCORRECT_SIGNATURE,

        /**
         * Verified signature has expired.
         * <ul>
         *     <li>Value of <em>expires</em> parameter is in the past</li>
         *     <li>Or the signature is older than <em>maximumAge</em> requested in {@link VerificationSpec}, computed by using <em>created</em> parameter</li>
         *     <li>Or the signature is from the "future": <em>created</em> parameter exceeds <em>maximumSkew</em> requested in {@link VerificationSpec}</li>
         * </ul>
         *
         */
        SIGNATURE_EXPIRED,

        /**
         * Algorithm of signature, private key or public key is not available in JVM.
         * <p>
         * Indicates problems with used Security {@link java.security.Provider}
         */
        UNKNOWN_ALGORITHM,

        /**
         * Unable to determine signature algorithm when verifying.
         * <p>
         * Algorithm must be provided either in {@link PublicKeyInfo} object or <em>alg</em> parameter
         */
        MISSING_ALGORITHM,

        /**
         * Invalid public or public key provided.
         * <p>
         * E.g. key algorithm not matching signature algorithm, problems with string-encoded keys
         */
        INVALID_KEY,

        /**
         * Header value for a <em>Header Component</em> is missing when signing or verifying,
         * or <em>Signature</em> or <em>Signature-Input</em> is missing when verifying
         */
        MISSING_HEADER,

        /**
         * {@link SignatureContext} for related request is missing for a component from related request (<em>req</em>)
         */
        MISSING_RELATED_REQUEST,

        /**
         * Invalid syntax for used Structured Headers, e.g. <em>Signature-Input</em>
         */
        INVALID_STRUCTURED_HEADER,

        /**
         * Missing key for Structured Dictionary header, e.g. missing requested signature label in <em>Signature</em> or <em>Signature-Input</em>
         */
        MISSING_DICTIONARY_KEY,

        /**
         * Target URI is missing a query or query parameter for <em>@query-param</em> Derived Component
         */
        MISSING_QUERY_PARAM,

        /**
         * Required Component or Parameter defined in {@link VerificationSpec} is missing
         */
        MISSING_REQUIRED,

        /**
         * A Derived Component cannot be obtained from provided target URI
         */
        MISSING_COMPONENT,

        /**
         * Parameter defined as forbidden in {@link VerificationSpec} is present in <em>Signature-Input</em> when verifying
         */
        FORBIDDEN_PRESENT,

        /**
         * Signature with requested <em>tag</em> not found when verifying
         */
        MISSING_TAG,

        /**
         * Found multiple signatures with requested <em>tag</em> when verifying
         */
        DUPLICATE_TAG,

        /**
         * Generic security problem. Relates to {@link java.security.GeneralSecurityException}.
         */
        GENERIC,
    }
}
