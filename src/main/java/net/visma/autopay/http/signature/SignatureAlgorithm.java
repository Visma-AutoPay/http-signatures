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


import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Signature algorithms
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-http-signature-algorithms-r">
 *      HTTP Signature Algorithms Registry</a>
 */
public enum SignatureAlgorithm {
    /**
     * RSASSA-PSS using SHA-512
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-rsassa-pss-using-sha-512">RSASSA-PSS using SHA-512</a>
     */
    RSA_PSS_SHA_512("rsa-pss-sha512", "RSASSA-PSS", SignatureKeyAlgorithm.RSA, new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1)),

    /**
     * RSASSA-PKCS1-v1_5 using SHA-256
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-rsassa-pkcs1-v1_5-using-sha">
     *      RSASSA-PKCS1-v1_5 using SHA-256</a>
     */
    RSA_SHA_256("rsa-v1_5-sha256", "SHA256withRSA", SignatureKeyAlgorithm.RSA),

    /**
     * HMAC using SHA-256
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-hmac-using-sha-256">HMAC using SHA-256</a>
     */
    HMAC_SHA_256("hmac-sha256", "HmacSHA256", SignatureKeyAlgorithm.HMAC),

    /**
     * ECDSA using curve P-256 DSS and SHA-256
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-ecdsa-using-curve-p-256-dss">
     *      ECDSA using curve P-256 DSS and SHA-256</a>
     */
    ECDSA_P256_SHA_256("ecdsa-p256-sha256", "SHA256withECDSAinP1363Format", SignatureKeyAlgorithm.EC),

    /**
     * Edwards Curve DSA using curve edwards25519
     *
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-eddsa-using-curve-edwards25">
     *      EdDSA using curve edwards25519</a>
     */
    ED_25519("ed25519", "Ed25519", SignatureKeyAlgorithm.ED_25519),
    ;

    private static final Map<String, SignatureAlgorithm> id2Enum;
    private final String identifier;
    private final String jvmName;
    private final SignatureKeyAlgorithm keyAlgorithm;
    private final AlgorithmParameterSpec parameterSpec;

    static {
        id2Enum = Stream.of(values())
                .collect(Collectors.toMap(SignatureAlgorithm::getIdentifier, dc -> dc));
    }

    SignatureAlgorithm(String identifier, String jvmName, SignatureKeyAlgorithm keyAlgorithm) {
        this(identifier, jvmName, keyAlgorithm, null);
    }

    SignatureAlgorithm(String identifier, String jvmName, SignatureKeyAlgorithm keyAlgorithm, AlgorithmParameterSpec parameterSpec) {
        this.identifier = identifier;
        this.jvmName = jvmName;
        this.keyAlgorithm = keyAlgorithm;
        this.parameterSpec = parameterSpec;
    }

    /**
     * Constructs {@link SignatureAlgorithm} from algorithm identifier
     *
     * @param identifier Algorithm identifier
     * @return Created SignatureAlgorithm
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-initial-contents">
     *      Algorithm names in the Algorithms Registry</a>
     */
    static SignatureAlgorithm fromIdentifier(String identifier) {
        return id2Enum.get(identifier);
    }

    /**
     * Returns identifier of this algorithm
     * 
     * @return Algorithm identifier
     */
    String getIdentifier() {
        return identifier;
    }

    /**
     * Returns JMV name of this signature algorithm
     * 
     * @return Algorithm name accepted by {@link java.security.Signature#getInstance(String)}
     */
    String getJvmName() {
        return jvmName;
    }

    /**
     * Returns algorithm of private and public keys related with this signature algorithm
     *
     * @return Private and public key algorithm
     */
    SignatureKeyAlgorithm getKeyAlgorithm() {
        return keyAlgorithm;
    }

    /**
     * Returns parameter spec of this signature algorithm
     * 
     * @return Parameter spec accepted by {@link java.security.Signature#setParameter(AlgorithmParameterSpec)}
     */
    AlgorithmParameterSpec getParameterSpec() {
        return parameterSpec;
    }
}
