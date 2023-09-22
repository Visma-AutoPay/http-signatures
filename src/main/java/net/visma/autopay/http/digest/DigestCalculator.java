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

import net.visma.autopay.http.structured.StructuredDictionary;
import net.visma.autopay.http.structured.StructuredException;
import net.visma.autopay.http.structured.StructuredInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Map;


/**
 * Calculates values for <em>Content-Digest</em> and <em>Repr-Digest</em> headers
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-13.html">Digest Fields</a>
 */
public final class DigestCalculator {
    /**
     * Calculates value of a digest, which can be directly copied to HTTP header.
     * <p>
     * To include multiple digests (with different algorithms) call this method multiple times
     * and concatenate the results using ", " as a separator.
     *
     * @param content   Binary request or response content. Caller is responsible to use proper encoding, matching <em>Content-Type</em>
     *                  and <em>Content-Encoding</em>.
     * @param algorithm Hash algorithm
     * @return Digest filed to be directly copied to the header
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-13.html#name-the-content-digest-field">The Content-Digest Field</a>
     */
    public static String calculateDigestHeader(byte[] content, DigestAlgorithm algorithm) {
        return StructuredDictionary.of(algorithm.getHttpKey(), calculateDigest(content, algorithm)).serialize();
    }

    /**
     * Calculates value of a digest, based on wanted digest taken from <em>Want-Content-Digest</em> or <em>Want-Repr-Digest</em> header.
     * <p>
     * Only algorithms defined in {@link DigestAlgorithm} are supported.
     *
     * @param content          Binary request or response content. Caller is responsible to use proper encoding, matching <em>Content-Type</em>
     *                         and <em>Content-Encoding</em>.
     * @param wantDigestHeader Value of <em>Want-Content-Digest</em> or <em>Want-Repr-Digest</em> header
     * @return Digest filed to be directly copied to the header
     * @throws DigestException Invalid <em>Want-...</em> header or unsupported wanted algorithm(s).
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-13.html#name-integrity-preference-fields">Integrity preference fields</a>
     */
    public static String calculateDigestHeader(byte[] content, String wantDigestHeader) throws DigestException {
        try {
            return parseHeader(wantDigestHeader).entrySet(StructuredInteger.class).stream()
                    .filter(entry -> entry.getValue().intValue() != 0)
                    .sorted(Comparator.<Map.Entry<String, StructuredInteger>>comparingInt(entry -> entry.getValue().intValue()).reversed())
                    .flatMap(entry -> DigestAlgorithm.fromHttpKey(entry.getKey()).stream())
                    .map(algorithm -> calculateDigestHeader(content, algorithm))
                    .findFirst()
                    .orElseThrow(() -> new DigestException(DigestException.ErrorCode.UNSUPPORTED_ALGORITHM, "Unsupported algorithms: " + wantDigestHeader));
        } catch (DigestException e) {
            throw e;
        } catch (Exception e) {
            throw new DigestException(DigestException.ErrorCode.INVALID_HEADER, "Invalid digest header", e);
        }
    }

    /**
     * Calculates digest for given content and algorithm
     *
     * @param content   Binary request or response content
     * @param algorithm Algorithm to use
     * @return Calculated digest
     */
    static byte[] calculateDigest(byte[] content, DigestAlgorithm algorithm) {
        try {
            return MessageDigest.getInstance(algorithm.getJvmName()).digest(content);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parses Digest HTTP header to {@link StructuredDictionary} object, when verifying digest or extracting "wanted" digest
     *
     * @param digestHeader HTTP header to parse
     * @return StructuredDictionary representation of the header
     * @throws DigestException Thrown if given header does not represent a valid Structured Dictionary
     */
    static StructuredDictionary parseHeader(String digestHeader) throws DigestException {
        try {
            var digestDict = StructuredDictionary.parse(digestHeader);

            if (digestDict.isEmpty()) {
                throw new DigestException(DigestException.ErrorCode.INVALID_HEADER, "Empty digest header");
            }

            return digestDict;

        } catch (StructuredException se) {
            throw new DigestException(DigestException.ErrorCode.INVALID_HEADER, "Error when parsing digest header", se);
        }
    }

    private DigestCalculator() {
        throw new UnsupportedOperationException();
    }
}
