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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class VerificationSpecTest {
    @Test
    void toStringEqualsHashCode() {
        // setup & execute
        var spec1 = VerificationSpec.builder()
                .requiredParameters(SignatureParameterType.CREATED, SignatureParameterType.NONCE)
                .forbiddenParameters(SignatureParameterType.ALGORITHM, SignatureParameterType.EXPIRES)
                .requiredComponents(SignatureComponents.builder()
                        .header("h1")
                        .canonicalizedHeader("ch")
                        .dictionaryMember("h3", "ok")
                        .method()
                        .queryParam("par")
                        .relatedRequestHeader("rh1")
                        .build())
                .context(SignatureContext.builder()
                        .status(200)
                        .method("POST")
                        .targetUri("https://visma.com/")
                        .header("h1", "one")
                        .header("h2", "two")
                        .header("ch", "ok; false")
                        .relatedRequest(SignatureContext.builder()
                                .header("rh1", "related")
                                .build())
                        .build())
                .maximumAge(10)
                .maximumSkew(30)
                .signatureLabel("test")
                .publicKeyGetter((keyId) -> PublicKeyInfo.builder().build())
                .build();
        var spec2 = VerificationSpec.builder()
                .requiredParameters(List.of(SignatureParameterType.CREATED, SignatureParameterType.NONCE))
                .forbiddenParameters(List.of(SignatureParameterType.ALGORITHM, SignatureParameterType.EXPIRES))
                .requiredComponents(SignatureComponents.builder()
                        .header("h1")
                        .canonicalizedHeader("ch")
                        .dictionaryMember("h3", "ok")
                        .method()
                        .queryParam("par")
                        .relatedRequestHeader("rh1")
                        .build())
                .context(SignatureContext.builder()
                        .status(200)
                        .method("POST")
                        .targetUri("https://visma.com/")
                        .header("h1", "one")
                        .header("h2", "two")
                        .header("ch", "ok; false")
                        .relatedRequest(SignatureContext.builder()
                                .header("rh1", "related")
                                .build())
                        .build())
                .maximumAge(10)
                .maximumSkew(30)
                .signatureLabel("test")
                .publicKeyGetter((keyId) -> PublicKeyInfo.builder().build())
                .build();

        // verify
        assertThat(spec1).isEqualTo(spec2)
                .hasSameHashCodeAs(spec2)
                .hasToString(spec2.toString());
    }
}
