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

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Objects;

/**
 * Public key properties, needed when verifying signatures
 * <p>
 * Provided by a user function defined in {@link VerificationSpec}
 *
 * @see VerificationSpec.Builder#publicKeyGetter(CheckedFunction)
 */
public class PublicKeyInfo {
    private final SignatureAlgorithm algorithm;
    private final PublicKey publicKey;
    private final String stringPublicKey;
    private final byte[] bytePublicKey;

    private PublicKeyInfo(SignatureAlgorithm algorithm, PublicKey publicKey, String stringPublicKey, byte[] bytePublicKey) {
        this.algorithm = algorithm;
        this.publicKey = publicKey;
        this.stringPublicKey = stringPublicKey;
        this.bytePublicKey = bytePublicKey;
    }

    /**
     * Returns signature algorithm associated with this public key
     *
     * @return Signature algorithm for verifying the signature
     */
    SignatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns {@link PublicKey} object for this Public Key Info.
     * <p>
     * If {@link PublicKey} object has been provided in the builder, then it is returned.
     * If encoded key has been provided, as byte[] or String, it is decoded to {@link PublicKey} object.
     * In this case the {@link SignatureAlgorithm} must be provided as a parameter of this method or when building this {@link PublicKeyInfo} object.
     *
     * @param algorithm Signature algorithm to decode the key. Must be provided if the key stored in this object is in encoded form and algorithm has not been
     *                  provided when building this object.
     * @return PublicKey object used to verify the signature
     * @throws SignatureException In case of problem when decoding the key
     */
    PublicKey getPublicKey(SignatureKeyAlgorithm algorithm) throws SignatureException {
        if (publicKey != null) {
            return publicKey;
        } else if (stringPublicKey != null) {
            return SignatureKeyFactory.decodePublicKey(stringPublicKey, algorithm);
        } else {
            return SignatureKeyFactory.decodePublicKey(bytePublicKey, algorithm);
        }
    }


    /**
     * Returns builder used to construct {@link PublicKeyInfo} object
     *
     * @return A PublicKeyInfo builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class to build {@link PublicKeyInfo} objects.
     * <p>
     * Public key can be provided as either {@link PublicKey} object or X.509-encoded byte[] or X.509-Base64-encoded String.
     */
    public static class Builder {
        private SignatureAlgorithm algorithm;
        private PublicKey publicKey;
        private String stringPublicKey;
        private byte[] bytePublicKey;

        private Builder() {
        }

        /**
         * Sets signature algorithm for the key.
         * <p>
         * The algorithm must be provided here or as <em>alg</em> parameter in <em>Signature-Input</em> header.
         *
         * @param algorithm Signature algorithm used to verify the signature
         * @return This builder
         */
        public Builder algorithm(SignatureAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        /**
         * Public key provided as an object
         *
         * @param publicKey Public key to verify the signature
         * @return This builder
         */
        public Builder publicKey(PublicKey publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        /**
         * Encoded public key provided as bytes[] object
         *
         * @param publicKey X.509-encoded public key
         * @return This builder
         * @see #publicKey(String)
         */
        public Builder publicKey(byte[] publicKey) {
            bytePublicKey = publicKey;
            return this;
        }

        /**
         * Encoded public key provided as String object
         * <p>
         * It must be in X.509 format. It can contain multiple lines, including <em>BEGIN PUBLIC KEY</em> and <em>END PUBLIC KEY</em>.
         * PKCS#1 format, like "BEGIN RSA PUBLIC KEY" is not accepted.
         *
         * @param publicKey X.509-base-64-encoded public key
         * @return This builder
         */
        public Builder publicKey(String publicKey) {
            stringPublicKey = publicKey;
            return this;
        }

        /**
         * Constructs {@link PublicKeyInfo} object from this builder
         * <p>
         * Public key data must be provided using any of publicKey() builder methods
         *
         * @return PublicKeyInfo object
         */
        public PublicKeyInfo build() {
            if (publicKey == null && stringPublicKey == null && bytePublicKey == null) {
                throw new NullPointerException("Public key not provided");
            }

            return new PublicKeyInfo(algorithm, publicKey, stringPublicKey, bytePublicKey);
        }
    }

    /**
     * String representation of this object. It does not contain public key data.
     *
     * @return String representation of this PublicKeyInfo
     */
    @Override
    public String toString() {
        return "PublicKeyInfo{" +
                "algorithm=" + algorithm +
                ", publicKey=" + (publicKey != null ? publicKey.getClass() : "null") +
                ", stringPublicKeyPresent='" + (stringPublicKey != null) + '\'' +
                ", bytePublicKeyPresent=" + (bytePublicKey != null) +
                '}';
    }

    /**
     * Compares the specified object with this PublicKeyInfo for equality. Returns true if the given object is of the same class as this PublicKeyInfo,
     * and all object fields are equal.
     *
     * @param o Object to be compared with this PublicKeyInfo
     * @return True is specified object is equal to this PublicKeyInfo
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (PublicKeyInfo) o;
        return algorithm == that.algorithm && Objects.equals(publicKey, that.publicKey) && Objects.equals(stringPublicKey, that.stringPublicKey)
                && Arrays.equals(bytePublicKey, that.bytePublicKey);
    }

    /**
     * Returns hash code for this PublicKeyInfo, which is composed of hash codes of all object fields
     *
     * @return The hash code for this PublicKeyInfo
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(algorithm, publicKey, stringPublicKey);
        result = 31 * result + Arrays.hashCode(bytePublicKey);
        return result;
    }
}
