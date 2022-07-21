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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;


class DataVerifierTest {
    @Test
    void invalidKeyIsDetected() throws Exception {
        // setup
        var publicKey = SignatureKeyFactory.decodePublicKey(ObjectMother.getEcPublicKey(), SignatureKeyAlgorithm.EC);

        // execute
        var exception = catchThrowableOfType(() -> DataVerifier.verify("text", new byte[] {1}, publicKey, SignatureAlgorithm.RSA_SHA_256),
                SignatureException.class);

        // verify
        assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_KEY);
    }

    @Test
    void generalProblemIsDetected() throws Exception {
        // setup
        var publicKey = SignatureKeyFactory.decodePublicKey(ObjectMother.getRsaPublicKey(), SignatureKeyAlgorithm.RSA);

        // execute
        var exception = catchThrowableOfType(() -> DataVerifier.verify("text", new byte[] {1}, publicKey, SignatureAlgorithm.RSA_SHA_256),
                SignatureException.class);

        // verify
        assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.GENERIC);
    }
}
