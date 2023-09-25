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
package net.visma.autopay.http.digest;


import java.util.Optional;

/**
 * Hash algorithms. Algorithms with status <em>Deprecated</em> are not handled.
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-13.html#name-establish-the-hash-algorith">
 *      Hash Algorithms for HTTP Digest Fields Registry</a>
 */
public enum DigestAlgorithm {
    /**
     * <em>SHA-256</em> algorithm
     */
    SHA_256("sha-256", "SHA-256"),

    /**
     * <em>SHA-512</em> algorithm
     */
    SHA_512("sha-512", "SHA-512");

    private final String httpKey;
    private final String jvmName;

    DigestAlgorithm(String httpKey, String jvmName) {
        this.httpKey = httpKey;
        this.jvmName = jvmName;
    }

    /**
     * Returns algorithm key to be used as dictionary key in Digest HTTP headers
     *
     * @return Algorithm key as defined in <em>Digest Fields</em>
     */
    public String getHttpKey() {
        return httpKey;
    }

    /**
     * Returns "JVM" name of this algorithm, which then can be used to call {@link java.security.MessageDigest#getInstance(String)}
     *
     * @return JVM name of this algorithm
     */
    public String getJvmName() {
        return jvmName;
    }

    /**
     * Returns algorithm corresponding to <em>Digest Fields</em>' algorithm key
     *
     * @param httpKey Algorithm key
     * @return DigestAlgorithm for provided key or empty Optional if the key is unknown
     */
    public static Optional<DigestAlgorithm> fromHttpKey(String httpKey) {
        if (SHA_256.httpKey.equals(httpKey)) {
            return Optional.of(SHA_256);
        } else if (SHA_512.httpKey.equals(httpKey)) {
            return Optional.of(SHA_512);
        } else {
            return Optional.empty();
        }
    }
}
