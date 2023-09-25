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

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * Builder class for constructing Signature Parameters. Used when creating signatures.
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-19.html#name-signature-parameters">Signature Parameters</a>
 * @see SignatureParameterType
 */
public class SignatureParameters {
    private final Map<SignatureParameterType, Object> parameters;
    private final boolean algorithmVisible;

    private SignatureParameters(Map<SignatureParameterType, Object> parameters, boolean algorithmVisible) {
        this.parameters = parameters;
        this.algorithmVisible = algorithmVisible;
    }

    /**
     * Returns value of <em>created</em> parameter
     *
     * @return Value of <em>created</em> parameter
     */
    Instant getCreated() {
        return (Instant) parameters.get(SignatureParameterType.CREATED);
    }

    /**
     * Returns value of <em>expires</em> parameter
     *
     * @return Value of <em>expires</em> parameter
     */
    Instant getExpires() {
        return (Instant) parameters.get(SignatureParameterType.EXPIRES);
    }

    /**
     * Returns value of <em>alg</em> parameter
     *
     * @return Value of <em>alg</em> parameter
     */
    SignatureAlgorithm getAlgorithm() {
        return (SignatureAlgorithm) parameters.get(SignatureParameterType.ALGORITHM);
    }

    /**
     * Returns value of <em>keyid</em> parameter
     *
     * @return Value of <em>keyid</em> parameter
     */
    String getKeyId() {
        return (String) parameters.get(SignatureParameterType.KEY_ID);
    }

    /**
     * Returns map of Signature Parameters
     * <p>
     * Map keys are Parameter Types, and values are parameter values.
     * Actual class of a value is the same as corresponding getter's return type.
     * Returned map is ordered, backed by {@link LinkedHashMap}, preserving order of adding parameters in the builder.
     *
     * @return Signature Parameter map
     */
    Map<SignatureParameterType, Object> getParameters() {
        return parameters;
    }

    /**
     * Returns true if signature algorithm (<em>alg</em>) should be included in <em>Signature-Input</em> header.
     * <p>
     * If false, given algorithm is used only for calculating the signature
     *
     * @return True if algorithm should be included in <em>Signature-Input</em> header
     */
    boolean isAlgorithmVisible() {
        return algorithmVisible;
    }

    /**
     * Returns a builder used to construct {@link SignatureParameters} object
     * <p>
     * When creating signatures, order of parameters added in the builder is preserved in produced content of <em>Signature-Input</em>.
     *
     * @return A SignatureParameters builder
     */
    public static Builder builder() {
        return new Builder();
    }


    /**
     * Builder class to build {@link SignatureParameters} objects
     */
    public static class Builder {
        private final Map<SignatureParameterType, Object> parameters;
        private boolean algorithmVisible;

        private Builder() {
            parameters = new LinkedHashMap<>();
        }

        /**
         * Sets <em>created</em> parameter - signature creation time
         *
         * @param created Value of <em>created</em> given as {@link Instant} object
         * @return This builder
         */
        public Builder created(Instant created) {
            parameters.put(SignatureParameterType.CREATED, created);
            return this;
        }

        /**
         * Sets <em>created</em> parameter - signature creation time
         *
         * @param epochSecond Value of <em>created</em> given as UNIX timestamp
         * @return This builder
         */
        public Builder created(long epochSecond) {
            return created(Instant.ofEpochSecond(epochSecond));
        }

        /**
         * Sets <em>created</em> parameter, signature creation time, to the current timestamp
         *
         * @return This builder
         */
        public Builder createdNow() {
            return created(Instant.now());
        }

        /**
         * Sets <em>expires</em> parameter - signature expiration time
         *
         * @param expires Value of <em>expires</em> given as {@link Instant} object
         * @return This builder
         */
        public Builder expires(Instant expires) {
            parameters.put(SignatureParameterType.EXPIRES, expires);
            return this;
        }

        /**
         * Sets <em>expires</em> parameter - signature expiration time
         *
         * @param epochSecond Value of <em>expires</em> given as UNIX timestamp
         * @return This builder
         */
        public Builder expires(long epochSecond) {
            return expires(Instant.ofEpochSecond(epochSecond));
        }

        /**
         * Sets <em>expires</em> parameter, signature expiration time, by adding given seconds to <em>created</em>
         *
         * @param seconds Seconds to expire after <em>created</em>
         * @return This builder
         */
        public Builder expiresAfter(int seconds) {
            return expires(Objects.requireNonNullElse((Instant) parameters.get(SignatureParameterType.CREATED), Instant.now()).plusSeconds(seconds));
        }

        /**
         * Sets <em>nonce</em> parameter - to randomize signature input
         *
         * @param nonce Value of <em>nonce</em>
         * @return This builder
         */
        public Builder nonce(String nonce) {
            parameters.put(SignatureParameterType.NONCE, nonce);
            return this;
        }

        /**
         * Sets <em>nonce</em> parameter to a random value
         * <p>
         * Internally, random UUIDs with removed "-" characters are used
         *
         * @return This builder
         */
        public Builder randomNonce() {
            return nonce(UUID.randomUUID().toString().replace("-", ""));
        }

        /**
         * Sets signature algorithm. Algorithm will be used when computing the signature but will not be revealed in <em>alg</em> parameter.
         *
         * @param algorithm Signature algorithm
         * @return This builder
         */
        public Builder algorithm(SignatureAlgorithm algorithm) {
            parameters.put(SignatureParameterType.ALGORITHM, algorithm);
            return this;
        }

        /**
         * Sets signature algorithm and <em>alg</em> parameter
         *
         * @param algorithm Signature algorithm
         * @return This builder
         */
        public Builder visibleAlgorithm(SignatureAlgorithm algorithm) {
            algorithmVisible = true;
            return algorithm(algorithm);
        }

        /**
         * Sets <em>keyid</em> parameter
         *
         * @param keyId Value of <em>keyid</em>
         * @return This builder
         */
        public Builder keyId(String keyId) {
            parameters.put(SignatureParameterType.KEY_ID, keyId);
            return this;
        }

        /**
         * Sets <em>tag</em> parameter
         *
         * @param tag Value of <em>tag</em>
         * @return This builder
         */
        public Builder tag(String tag) {
            parameters.put(SignatureParameterType.TAG, tag);
            return this;
        }

        /**
         * Constructs {@link SignatureParameters} object from this builder
         *
         * @return SignatureParameters object
         */
        public SignatureParameters build() {
            return new SignatureParameters(parameters, algorithmVisible);
        }
    }

    /**
     * String representation of this object
     *
     * @return String representation of this SignatureParameters
     */
    @Override
    public String toString() {
        return "SignatureParameters{" +
                "parameters=" + parameters +
                ", algorithmVisible=" + algorithmVisible +
                '}';
    }

    /**
     * Compares the specified object with this SignatureParameters for equality. Returns true if the given object is of the same class as this
     * SignatureParameters, and all contained parameters are equal.
     *
     * @param o Object to be compared with this SignatureParameters
     * @return True is specified object is equal to this SignatureParameters
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (SignatureParameters) o;
        return algorithmVisible == that.algorithmVisible && parameters.equals(that.parameters);
    }

    /**
     * Returns hash code for this SignatureParameters, which is composed of hash codes of all contained components
     *
     * @return The hash code for this SignatureParameters
     */
    @Override
    public int hashCode() {
        return Objects.hash(parameters, algorithmVisible);
    }
}
