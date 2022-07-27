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

/**
 * Algorithm used to create public and private keys
 */
enum SignatureKeyAlgorithm {
    /**
     * RSA
     */
    RSA("RSA"),

    /**
     * RSA PSS
     */
    RSA_PSS("RSASSA-PSS"),

    /**
     * Elliptical curve
     */
    EC("EC"),

    /**
     * Edwards-Curve
     */
    ED_25519("Ed25519"),

    /**
     * HMAC. It's symmetric.
     */
    HMAC(""),
    ;

    private final String jvmName;

    SignatureKeyAlgorithm(String jvmName) {
        this.jvmName = jvmName;
    }

    /**
     * Returns JMV name of this key algorithm
     *
     * @return Algorithm name accepted by {@link java.security.KeyFactory#getInstance(String)}
     */
    String getJvmName() {
        return jvmName;
    }

    /**
     * Returns true if this key algorithm is for symmetric keys
     *
     * @return True if symmetric
     */
    boolean isSymmetric() {
        return this == HMAC;
    }
}
