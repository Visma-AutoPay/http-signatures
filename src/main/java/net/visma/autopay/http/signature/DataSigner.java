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

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.ECKey;

/**
 * Class for signing raw data
 */
final class DataSigner {
    /**
     * Signs given text by using given algorithm and private key.
     *
     * @param text       Text to be signed. When signing, encoded as UTF-8
     * @param privateKey Private key used for the signature
     * @param algorithm  Used algorithm
     * @return Signature bytes
     * @throws SignatureException Problems with the private key, JMV not supporting given algorithm, etc.
     */
    static byte[] sign(String text, PrivateKey privateKey, SignatureAlgorithm algorithm) throws SignatureException {
        var data = text.getBytes(StandardCharsets.UTF_8);

        try {
            if (algorithm.getKeyAlgorithm().isSymmetric()) {
                return signHmac(data, privateKey, algorithm);
            } else {
                return signAsymmetric(data, privateKey, algorithm);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException(SignatureException.ErrorCode.UNKNOWN_ALGORITHM, "Unknown algorithm " + algorithm.getJvmName(), e);
        } catch (InvalidKeyException e) {
            throw new SignatureException(SignatureException.ErrorCode.INVALID_KEY, "Invalid private key " + privateKey, e);
        } catch (GeneralSecurityException e) {
            throw new SignatureException(SignatureException.ErrorCode.GENERIC, "Problem while signing data", e);
        }
    }

    /**
     * Creates and populates parameters of {@link Signature} object. Checks if used elliptic curve matches the algorithm.
     *
     * @param key Public or private key to check
     * @param algorithm Signature algorithm
     * @return Created {@link Signature} object
     * @throws InvalidKeyException EC key does not match the algorithm
     * @throws NoSuchAlgorithmException Signature algorithm not found in JVM
     * @throws InvalidAlgorithmParameterException Invalid signature parameter
     */
    static Signature createSignatureObject(Key key, SignatureAlgorithm algorithm) throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        if (key instanceof ECKey) {
            EllipticCurveValidator.validate((ECKey) key, algorithm);
        }

        var signatureObject = Signature.getInstance(algorithm.getJvmName());

        if (algorithm.getParameterSpec() != null) {
            signatureObject.setParameter(algorithm.getParameterSpec());
        }

        return signatureObject;
    }

    private static byte[] signAsymmetric(byte[] data, PrivateKey privateKey, SignatureAlgorithm algorithm) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, java.security.SignatureException {

        var signatureObject = createSignatureObject(privateKey, algorithm);
        signatureObject.initSign(privateKey);
        signatureObject.update(data);

        return signatureObject.sign();
    }

    private static byte[] signHmac(byte[] data, PrivateKey privateKey, SignatureAlgorithm algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        var mac = Mac.getInstance(algorithm.getJvmName());
        mac.init(privateKey);

        return mac.doFinal(data);
    }

    private DataSigner() {
        throw new UnsupportedOperationException();
    }
}
