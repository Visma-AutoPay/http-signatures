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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class SignatureResultTest {
    @Test
    void toStringMethod() {
        // setup
        var signatureResult = new SignatureResult("aaa", "bbb", "ccc");

        // execute
        var string = signatureResult.toString();

        // verify
        assertThat(string).contains("aaa").contains("bbb").contains("ccc");
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var signatureResult1 = new SignatureResult("a", "b", "c");
        var signatureResult2 = new SignatureResult("a", "bx", "c");
        var signatureResult3 = new SignatureResult("a", "b", "c");

        // execute
        assertThat(signatureResult1).hasSameHashCodeAs(signatureResult3)
                .isEqualTo(signatureResult3)
                .isEqualTo(signatureResult1)
                .isNotEqualTo(signatureResult2)
                .isNotEqualTo("abc");
    }
}
