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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.Security;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


class SignatureSpecTest {
    @BeforeAll
    static void beforeAll() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @AfterAll
    static void afterAll() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    @Test
    void toStringEqualsHashCode() throws Exception {
        // setup & execute
        var spec1 = getSignatureSpec();
        var spec2 = getSignatureSpec();

        // verify
        assertThat(spec1).isEqualTo(spec2)
                .hasSameHashCodeAs(spec2)
                .hasToString(spec2.toString());
    }

    private SignatureSpec getSignatureSpec() throws SignatureException {
        return SignatureSpec.builder()
                .parameters(SignatureParameters.builder()
                        .created(Instant.parse("2022-05-02T10:45:00Z"))
                        .expiresAfter(20)
                        .algorithm(SignatureAlgorithm.ED_25519)
                        .nonce("non-123")
                        .build())
                .components(SignatureComponents.builder()
                        .header("h1")
                        .structuredHeader("ch")
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
                .signatureLabel("test-key")
                .privateKey(SignatureKeyFactory.decodePrivateKey(ObjectMother.getEdPrivateKey(), SignatureKeyAlgorithm.ED_25519))
                .build();
    }
}
