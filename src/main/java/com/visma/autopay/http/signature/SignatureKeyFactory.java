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

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;


/**
 * Factory class to create {@link PublicKey} and {@link PrivateKey} objects from encoded key.
 * <p>
 * Its goal is not to be a feature-rich PEM parser but to facilitate simple common cases.
 * For public keys, X.509 is supported, like "BEGIN PUBLIC KEY". PKCS#1, like BEGIN RSA PUBLIC KEY, is not supported.
 * For private keys, PKCS#8 is supported, like "BEGIN PRIVATE KEY".
 * Symmetric keys, for HMAC, are supported in their "raw" format - without any additional encoding.
 */
final class SignatureKeyFactory {
    /**
     * Decodes public key provided as String object
     * <p>
     * It must be in X.509 format, Base64-encoded. It can contain multiple lines, including <em>BEGIN PUBLIC KEY</em> and <em>END PUBLIC KEY</em>.
     * PKCS#1 format, like "BEGIN RSA PUBLIC KEY" is not accepted.
     *
     * @param pemX509Key X.509-base-64 encoded public key
     * @param algorithm  Signature key algorithm associated with the public key. If {@link SignatureKeyAlgorithm#RSA} is given then both RSA and RSA-PSS are
     *                   tried.
     * @return Parsed PublicKey object
     * @throws SignatureException Invalid key or problems with security provider (e.g. unsupported algorithm)
     */
    static PublicKey decodePublicKey(String pemX509Key, SignatureKeyAlgorithm algorithm) throws SignatureException {
        return decodePublicKey(decodePemKey(pemX509Key), algorithm);
    }

    /**
     * Decodes private key provided as String object
     * <p>
     * It must be in PKCS#8 format, Base64-encoded. It can contain multiple lines, including <em>BEGIN PRIVATE KEY</em> and <em>END PRIVATE KEY</em>.
     *
     * @param pemPkcs8Key PKCS#8-base-64-encoded private key
     * @param algorithm   Signature key algorithm associated with the private key. If {@link SignatureKeyAlgorithm#RSA} is given then both RSA and RSA-PSS are
     *                    tried.
     * @return Parsed PrivateKey object
     * @throws SignatureException Invalid key or problems with security provider (e.g. unsupported algorithm)
     */
    static PrivateKey decodePrivateKey(String pemPkcs8Key, SignatureKeyAlgorithm algorithm) throws SignatureException {
        return decodePrivateKey(decodePemKey(pemPkcs8Key), algorithm);
    }

    /**
     * Decodes public key provided as byte[] object
     * <p>
     * It must be in X.509 format
     *
     * @param x509Key    X.509-encoded public key
     * @param algorithm  Signature key algorithm associated with the public key. If {@link SignatureKeyAlgorithm#RSA} is given then both RSA and RSA-PSS are
     *                   tried.
     * @return Parsed PublicKey object
     * @throws SignatureException Invalid key or problems with security provider (e.g. unsupported algorithm)
     */
    static PublicKey decodePublicKey(byte[] x509Key, SignatureKeyAlgorithm algorithm) throws SignatureException {
        if (algorithm.isSymmetric()) {
            return new HmacKey(x509Key);
        } else {
            return createAsymmetricPublicKey(x509Key, algorithm);
        }
    }

    /**
     * Decodes private key provided as String object
     * <p>
     * It must be in PKCS#8 format
     *
     * @param encodedKey  PKCS#8-encoded private key
     * @param algorithm   Signature key algorithm associated with the private key. If {@link SignatureKeyAlgorithm#RSA} is given then both RSA and RSA-PSS are
     *                    tried.
     * @return Parsed PrivateKey object
     * @throws SignatureException Invalid key or problems with security provider (e.g. unsupported algorithm)
     */
    static PrivateKey decodePrivateKey(byte[] encodedKey, SignatureKeyAlgorithm algorithm) throws SignatureException {
        if (algorithm.isSymmetric()) {
            return new HmacKey(encodedKey);
        } else {
            return createAsymmetricPrivateKey(encodedKey, algorithm);
        }
    }

    private static PrivateKey createAsymmetricPrivateKey(byte[] encodedKey, SignatureKeyAlgorithm algorithm) throws SignatureException {
        try {
            var keyFactory = KeyFactory.getInstance(algorithm.getJvmName());
            var keySpec = new PKCS8EncodedKeySpec(encodedKey);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException(ErrorCode.UNKNOWN_ALGORITHM, "Unknown algorithm " + algorithm, e);
        } catch (InvalidKeySpecException e) {
            if (algorithm == SignatureKeyAlgorithm.RSA) {
                try {
                    return createAsymmetricPrivateKey(encodedKey, SignatureKeyAlgorithm.RSA_PSS);
                } catch (Exception ee) {
                    throw getInvalidPrivateKeyException(e);
                }
            } else {
                throw getInvalidPrivateKeyException(e);
            }
        } catch (Exception e) {
            throw getInvalidPrivateKeyException(e);
        }
    }

    private static SignatureException getInvalidPrivateKeyException(Exception e) {
        return new SignatureException(ErrorCode.INVALID_KEY, "Invalid private key", e);
    }

    private static SignatureException getInvalidPublicKeyException(Exception e) {
        return new SignatureException(ErrorCode.INVALID_KEY, "Invalid public key", e);
    }

    private static PublicKey createAsymmetricPublicKey(byte[] encodedKey, SignatureKeyAlgorithm algorithm) throws SignatureException {
        try {
            var keyFactory = KeyFactory.getInstance(algorithm.getJvmName());
            var keySpec = new X509EncodedKeySpec(encodedKey);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException(ErrorCode.UNKNOWN_ALGORITHM, "Unknown algorithm " + algorithm, e);
        } catch (InvalidKeySpecException e) {
            if (algorithm == SignatureKeyAlgorithm.RSA) {
                try {
                    return createAsymmetricPublicKey(encodedKey, SignatureKeyAlgorithm.RSA_PSS);
                } catch (Exception ee) {
                    throw getInvalidPublicKeyException(e);
                }
            } else {
                throw getInvalidPublicKeyException(e);
            }
        } catch (Exception e) {
            throw getInvalidPublicKeyException(e);
        }
    }

    private static byte[] decodePemKey(String pemKey) throws SignatureException {
        try {
            var base64Key = pemKey.replaceAll("-----.*", "").replaceAll("[\n\r]", "");
            return Base64.getDecoder().decode(base64Key);
        } catch (Exception e) {
            throw new SignatureException(ErrorCode.INVALID_KEY, "Key not Base64-encoded", e);
        }
    }

    private static class HmacKey implements PrivateKey, PublicKey, SecretKey {
        private static final long serialVersionUID = -623103208224639883L;
        private final byte[] encodedKey;

        private HmacKey(byte[] encodedKey) {
            this.encodedKey = encodedKey;
        }

        @Override
        public String getAlgorithm() {
            return SignatureKeyAlgorithm.HMAC.getJvmName();
        }

        @Override
        public String getFormat() {
            return "RAW";
        }

        @Override
        public byte[] getEncoded() {
            return encodedKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            var hmacKey = (HmacKey) o;
            return Arrays.equals(encodedKey, hmacKey.encodedKey);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(encodedKey);
        }

        @Override
        public String toString() {
            return getClass().getName() + "@" + Integer.toHexString(hashCode());
        }
    }

    private SignatureKeyFactory() {
        throw new UnsupportedOperationException();
    }
}
