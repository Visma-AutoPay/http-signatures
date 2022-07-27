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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class PublicKeyInfoTest {
    @Test
    void toStringEqualsHashCode() throws Exception {
        // setup
        var key1 = PublicKeyInfo.builder()
                .algorithm(SignatureAlgorithm.RSA_SHA_256)
                .publicKey(new byte[]{2, 4, 2})
                .publicKey("hello")
                .publicKey(SignatureKeyFactory.decodePublicKey(new byte[] {3, 2, 1}, SignatureKeyAlgorithm.HMAC))
                .build();
        var key2 = PublicKeyInfo.builder()
                .algorithm(SignatureAlgorithm.RSA_SHA_256)
                .publicKey(new byte[]{2, 4, 2})
                .publicKey("hello")
                .publicKey(SignatureKeyFactory.decodePublicKey(new byte[] {3, 2, 1}, SignatureKeyAlgorithm.HMAC))
                .build();

        // verify
        assertThat(key1).isEqualTo(key2)
                .hasSameHashCodeAs(key2)
                .hasToString(key2.toString());
    }
}
