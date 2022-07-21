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
package com.visma.autopay.http.digest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DigestCalculatorTest {
    @ParameterizedTest
    @CsvSource({"SHA_256, 1LKaloxAFzY43tjRdMhpV6+iEb5HnO4CDbpd/hJ9kco=",
            "SHA_512, coHEdEzqs9+9+KyUfLphvh5kUnyaLidyPfwWpcZMbRdqUi1WntQuxjT0jSvWL68VIeYWawbhBWcjSmeIqCyL3Q=="})
    void digestIsCalculatedForGivenAlgorithm(DigestAlgorithm algorithm, String expectedBase64) {
        // setup
        var content = new byte[]{1, 2, 4};
        var expectedHeader = algorithm.getHttpKey() + "=:" + expectedBase64 + ":";

        // execute
        var digestHeader = DigestCalculator.calculateDigestHeader(content, algorithm);

        // verify
        assertThat(digestHeader).isEqualTo(expectedHeader);
    }

    @Test
    void digestIsCalculatedAccordingToPriority() throws DigestException {
        // setup
        var content = new byte[]{1, 2, 4};
        var wantDigest = "unixsum=3, sha-256=5, sha-512=4";
        var expectedHeader = "sha-256=:1LKaloxAFzY43tjRdMhpV6+iEb5HnO4CDbpd/hJ9kco=:";

        // execute
        var digestHeader = DigestCalculator.calculateDigestHeader(content, wantDigest);

        // verify
        assertThat(digestHeader).isEqualTo(expectedHeader);
    }

    @Test
    void unsupportedAlgorithmsAreDetected() {
        // setup
        var content = new byte[]{1, 2, 4};
        var wantDigest = "unixsum=3, sha-256=0, md5=4";

        // execute & verify
        assertThatThrownBy(() -> DigestCalculator.calculateDigestHeader(content, wantDigest)).isInstanceOfSatisfying(DigestException.class, e -> {
            assertThat(e).hasMessageContaining("Unsupported algorithms");
            assertThat(e.getErrorCode()).isEqualTo(DigestException.ErrorCode.UNSUPPORTED_ALGORITHM);
        });
    }

    @Test
    void emptyWantHeaderIsDetected() {
        // setup
        var content = new byte[]{1, 2, 4};
        var wantDigest = "";

        // execute & verify
        assertThatThrownBy(() -> DigestCalculator.calculateDigestHeader(content, wantDigest)).isInstanceOfSatisfying(DigestException.class, e -> {
            assertThat(e).hasMessageContaining("Empty");
            assertThat(e.getErrorCode()).isEqualTo(DigestException.ErrorCode.INVALID_HEADER);
        });
    }

    @Test
    void invalidWantDictionaryValuesAreDetected() {
        // setup
        var content = new byte[]{1, 2, 4};
        var wantDigest = "sha-256=use-me, md5=no-way";

        // execute & verify
        assertThatThrownBy(() -> DigestCalculator.calculateDigestHeader(content, wantDigest)).isInstanceOfSatisfying(DigestException.class, e -> {
            assertThat(e).hasMessageContaining("Invalid");
            assertThat(e.getErrorCode()).isEqualTo(DigestException.ErrorCode.INVALID_HEADER);
        });
    }
}
