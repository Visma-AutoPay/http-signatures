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
package net.visma.autopay.http.digest;

import net.visma.autopay.http.structured.StructuredBytes;

import java.util.Arrays;


/**
 * Verifies values of <em>Content-Digest</em> and <em>Repr-Digest</em> headers
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9530.html">Digest Fields</a>
 */
public final class DigestVerifier {
    /**
     * Verifies provided value of <em>Content-Digest</em> or <em>Repr-Digest</em> header. Throws exception on failure.
     * <p>
     * If the header contains multiple digests, those with supported algorithms (defined in {@link DigestAlgorithm}) are verified,
     * and verification succeeds if any of the "supported" digests is correct.
     *
     * @param digestHeader Header read from HTTP request or response
     * @param content      Binary request or response content. Caller is responsible to use proper encoding, matching <em>Content-Type</em>
     *                     and <em>Content-Encoding</em>.
     * @throws DigestException Thrown when provided header is syntactically invalid, or any of supported algorithms is included, or digest is incorrect
     *                         (does not match the computed one).
     */
    public static void verifyDigestHeader(String digestHeader, byte[] content) throws DigestException {
        var validDigestFound = false;
        var knownAlgorithmFound = false;
        var digestDict = DigestCalculator.parseHeader(digestHeader);

        try {
            for (var entry : digestDict.entrySet(StructuredBytes.class)) {
                var algorithm = DigestAlgorithm.fromHttpKey(entry.getKey());

                if (algorithm.isPresent()) {
                    knownAlgorithmFound = true;
                    var actualDigest = DigestCalculator.calculateDigest(content, algorithm.get());

                    if (Arrays.equals(entry.getValue().bytesValue(), actualDigest)) {
                        validDigestFound = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new DigestException(DigestException.ErrorCode.INVALID_HEADER, "Invalid digest header", e);
        }

        if (!knownAlgorithmFound) {
            throw new DigestException(DigestException.ErrorCode.UNSUPPORTED_ALGORITHM, "Unsupported algorithms: " + digestDict.keySet());
        } else if (!validDigestFound) {
            throw new DigestException(DigestException.ErrorCode.INCORRECT_DIGEST, "Provided digest different from computed one");
        }
    }

    private DigestVerifier() {
        throw new UnsupportedOperationException();
    }
}
