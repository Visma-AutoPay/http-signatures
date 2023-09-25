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

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * Class for verifying signatures
 */
final class DataVerifier {
    /**
     * Verifies given signature against give text, using given algorithm and public key
     *
     * @param text      Data to verify against. Encoded using UTF-8.
     * @param signature Signature to verify
     * @param publicKey Public key object
     * @param algorithm Used algorithm
     * @return True when the signature verified OK, false when it's incorrect
     * @throws SignatureException Problems with the public key, JMV not supporting given algorithm, etc.
     */
    static boolean verify(String text, byte[] signature, PublicKey publicKey, SignatureAlgorithm algorithm) throws SignatureException {
        var data = text.getBytes(StandardCharsets.UTF_8);
        boolean ok;

        try {
            if (algorithm.getKeyAlgorithm().isSymmetric()) {
                ok = verifyHmac(data, signature, publicKey, algorithm);
            } else {
                ok = verifyAsymmetric(data, signature, publicKey, algorithm);
            }

            return ok;

        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException(SignatureException.ErrorCode.UNKNOWN_ALGORITHM, "Unknown algorithm " + algorithm.getJvmName(), e);
        } catch (InvalidKeyException e) {
            throw new SignatureException(SignatureException.ErrorCode.INVALID_KEY, "Invalid public key " + publicKey, e);
        } catch (GeneralSecurityException e) {
            throw new SignatureException(SignatureException.ErrorCode.GENERIC, "Problem while verifying data", e);
        }
    }

    private static boolean verifyAsymmetric(byte[] data, byte[] signature, PublicKey publicKey, SignatureAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, java.security.SignatureException {

        var signatureObject = DataSigner.createSignatureObject(publicKey, algorithm);
        signatureObject.initVerify(publicKey);
        signatureObject.update(data);

        return signatureObject.verify(signature);
    }

    private static boolean verifyHmac(byte[] data, byte[] signature, PublicKey publicKey, SignatureAlgorithm algorithm) throws NoSuchAlgorithmException,
            InvalidKeyException {
        var mac = Mac.getInstance(algorithm.getJvmName());
        mac.init(publicKey);
        var dataSignature = mac.doFinal(data);

        return Arrays.equals(dataSignature, signature);
    }

    private DataVerifier() {
        throw new UnsupportedOperationException();
    }
}
