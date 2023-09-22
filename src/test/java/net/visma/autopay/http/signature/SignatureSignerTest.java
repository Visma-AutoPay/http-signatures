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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.within;


class SignatureSignerTest {
    @BeforeAll
    static void beforeAll() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @AfterAll
    static void afterAll() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    @Test
    void minimalSignature() throws Exception {
        // setup
        var signatureSpec = ObjectMother.getSignatureSpecBuilder().build();
        var expectedSignatureInput = "test=()";
        var expectedSignature = "test=:ZdapoyEz/RbaQf9SBIh7Qk5sqzDfWyxKMMRkg6nDZazOD1kLIl44m0ds/Sgd1fiEVdJkS/0r8QAzGDckYh5KBg==:";
        var verificationSpec = ObjectMother.getVerificationSpecBuilder(expectedSignatureInput, expectedSignature).build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void randomNonceIsCreated() throws Exception {
        // setup
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .parameters(SignatureParameters.builder()
                        .algorithm(SignatureAlgorithm.ED_25519)
                        .randomNonce()
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureInput()).matches("test=\\(\\);nonce=\"\\w{30,}\"");

        // signature verification
        var verificationSpec = ObjectMother.getVerificationSpecBuilder(result.getSignatureInput(), result.getSignature()).build();
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void createdNowIsUsed() throws Exception {
        // setup
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .parameters(SignatureParameters.builder()
                        .algorithm(SignatureAlgorithm.ED_25519)
                        .createdNow()
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        var regex = "test=\\(\\);created=(\\d{10})";
        assertThat(result.getSignatureInput()).matches(regex);
        var matcher = Pattern.compile(regex).matcher(result.getSignatureInput());
        assertThat(matcher.find()).isTrue();
        var created = Long.parseLong(matcher.group(1));
        assertThat(created).isCloseTo(System.currentTimeMillis() / 1000, within(10L));

        // signature verification
        var verificationSpec = ObjectMother.getVerificationSpecBuilder(result.getSignatureInput(), result.getSignature()).build();
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Nested
    class MissingItemTest {
        @Test
        void missingHeaderIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder()
                            .header("My-Header")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_HEADER);
            assertThat(exception)
                    .hasMessageContaining("my-header")
                    .hasMessageContaining("Header");
        }

        @Test
        void missingTrailerIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder()
                            .trailer("My-Trailer")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_HEADER);
            assertThat(exception)
                    .hasMessageContaining("my-trailer")
                    .hasMessageContaining("Trailer");
        }

        @Test
        void missingStructuredHeaderIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder()
                            .structuredHeader("My-Header")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_HEADER);
            assertThat(exception).hasMessageContaining("my-header");
        }

        @Test
        void missingDictionaryHeaderIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder()
                            .dictionaryMember("My-Header", "hello")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_HEADER);
            assertThat(exception).hasMessageContaining("my-header");
        }

        @Test
        void missingDictionaryKeyIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder()
                            .dictionaryMember("My-Header", "hello")
                            .build())
                    .context(SignatureContext.builder()
                            .header("My-header", "hi=99")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_DICTIONARY_KEY);
            assertThat(exception).hasMessageContaining("my-header");
        }

        @Test
        void missingComponentValueIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder().status().build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_COMPONENT);
            assertThat(exception).hasMessageContaining("@status");
        }

        @Test
        void missingRelatedRequestIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder()
                            .relatedRequestDictionaryMember("Signature", "my-req")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_RELATED_REQUEST);
        }

        @Test
        void missingAlgorithmIsDetected() {
            // setup
            var exception = catchThrowable(() -> ObjectMother.getSignatureSpecBuilder()
                    .parameters(SignatureParameters.builder().build())
                    .build());

            // verify
            assertThat(exception).hasMessageContaining("Algorithm");
        }
    }

    @Nested
    class MalformedItemTest {
        @Test
        void malformedStructuredHeaderIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder()
                            .structuredHeader("My-Header")
                            .build())
                    .context(SignatureContext.builder()
                            .header("My-Header", "'a'")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_STRUCTURED_HEADER);
            assertThat(exception).hasMessageContaining("my-header");
        }

        @Test
        void malformedDictionaryHeaderIsDetected() {
            // setup
            var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                    .components(SignatureComponents.builder()
                            .dictionaryMember("My-Header", "hello")
                            .build())
                    .context(SignatureContext.builder()
                            .header("My-Header", "hello=\"World")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(signatureSpec::sign, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_STRUCTURED_HEADER);
            assertThat(exception).hasMessageContaining("my-header");
        }
    }

    @Nested
    class PrivateKeyTest {
        @ParameterizedTest
        @ValueSource(strings = {"@invalid", "aGVsbG8="})
        void invalidPrivateKeyIsDetected(String privateKey) {
            // setup
            var signatureSpecBuilder = ObjectMother.getSignatureSpecBuilder().privateKey(privateKey);

            // execute
            var exception = catchThrowableOfType(signatureSpecBuilder::build, IllegalArgumentException.class);

            // verify
            assertThat(exception).hasMessageContaining("private key");
        }

        @Test
        void missingPrivateKeyIdDetected() {
            // setup
            var signatureSpecBuilder = SignatureSpec.builder()
                    .signatureLabel(ObjectMother.SIGNATURE_LABEL)
                    .parameters(SignatureParameters.builder().visibleAlgorithm(SignatureAlgorithm.ED_25519).build());

            // execute
            var exception = catchThrowableOfType(signatureSpecBuilder::build, NullPointerException.class);

            // verify
            assertThat(exception).hasMessageContaining("Private key");
        }

        @Test
        void binaryPrivateKeyIsUsed() throws Exception {
            // setup
            var signatureSpec = SignatureSpec.builder()
                    .signatureLabel(ObjectMother.SIGNATURE_LABEL)
                    .privateKey(Base64.getDecoder().decode(ObjectMother.getEdPrivateKeyStripped()))
                    .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                    .build();
            var expectedSignatureInput = "test=()";
            var expectedSignature = "test=:ZdapoyEz/RbaQf9SBIh7Qk5sqzDfWyxKMMRkg6nDZazOD1kLIl44m0ds/Sgd1fiEVdJkS/0r8QAzGDckYh5KBg==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder(expectedSignatureInput, expectedSignature).build();

            // execute
            var result = signatureSpec.sign();

            // verify signature
            assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
            assertThat(result.getSignature()).isEqualTo(expectedSignature);

            // signature verification
            assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
        }

        @Test
        void objectPrivateKeyIsUsed() throws Exception {
            // setup
            var keyFactory = KeyFactory.getInstance("Ed25519");
            var keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(ObjectMother.getEdPrivateKeyStripped()));
            var privateKey = keyFactory.generatePrivate(keySpec);

            var signatureSpec = SignatureSpec.builder()
                    .signatureLabel(ObjectMother.SIGNATURE_LABEL)
                    .privateKey(privateKey)
                    .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                    .build();
            var expectedSignatureInput = "test=()";
            var expectedSignature = "test=:ZdapoyEz/RbaQf9SBIh7Qk5sqzDfWyxKMMRkg6nDZazOD1kLIl44m0ds/Sgd1fiEVdJkS/0r8QAzGDckYh5KBg==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder(expectedSignatureInput, expectedSignature).build();

            // execute
            var result = signatureSpec.sign();

            // verify signature
            assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
            assertThat(result.getSignature()).isEqualTo(expectedSignature);

            // signature verification
            assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
        }
    }
}
