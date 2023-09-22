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
package net.visma.autopay.http.signature;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;


class SignatureVerifierTest {
    @BeforeAll
    static void beforeAll() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @AfterAll
    static void afterAll() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    @Test
    void notMatchingSignatureIsDetected() {
        // setup
        var signatureInput = "test=(\"@method\" \"@path\")";
        var signature = "test=:4CpbBoaIi/KZGQrzdQ1ybHNG9DrQzwxxK2XBXRKPUj5mKebWb9uV+Rl2D4bJStym24PomE5+08f1KoBfHxLzBg==:";
        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                        .header(SignatureHeaders.SIGNATURE, signature)
                        .method("POST")
                        .targetUri(URI.create("/foo"))
                        .build())
                .build();

        // execute
        var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

        // verify
        assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INCORRECT_SIGNATURE);
        assertThat(exception).hasMessageContaining("different")
                .hasMessageContaining("ed25519")
                .hasMessageContaining("\"@method\": POST\n" +
                        "\"@path\": /foo\n" +
                        "\"@signature-params\": (\"@method\" \"@path\")");
    }

    @Test
    void existingForbiddenParameterIsDetected() {
        // setup
        var verificationSpec = ObjectMother.getVerificationSpecBuilder("test=();created=1234567890;alg=ed25519", "test=:YQ==:")
                .forbiddenParameters(SignatureParameterType.ALGORITHM)
                .build();

        // execute
        var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

        // verify
        assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.FORBIDDEN_PRESENT);
        assertThat(exception).hasMessageContaining("alg");
    }

    @Nested
    class MissingItemTest {
        @Test
        void missingHeaderIsDetected() {
            // setup
            var verificationSpec = ObjectMother.getVerificationSpecBuilder("test=(\"my-header\")", "test=:YQ==:").build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_HEADER);
            assertThat(exception).hasMessageContaining("my-header");
        }

        @Test
        void missingRequiredHeaderIsDetected() {
            // setup
            var verificationSpec = ObjectMother.getVerificationSpecBuilder("test=(\"my-header\")", "test=:YQ==:")
                    .requiredComponents(SignatureComponents.builder()
                            .header("my-required-header")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_REQUIRED);
            assertThat(exception).hasMessageContaining("my-required-header");
        }

        @Test
        void missingQueryParamInMissingQueryIsDetected() {
            var signatureInput = "test=(\"@query-param\";name=\"ok\")";
            var signature = "test=:YQ==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .targetUri("https://example.com")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            exception.printStackTrace();
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_QUERY_PARAM);
            assertThat(exception).hasMessageContaining("Query is missing");
        }

        @Test
        void missingQueryParamIsDetected() {
            var signatureInput = "test=(\"@query-param\";name=\"ok\")";
            var signature = "test=:YQ==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .targetUri("https://example.com/?name=me")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_QUERY_PARAM);
            assertThat(exception).hasMessageContaining("Query param");
        }

        @Test
        void missingQueryParamNameInInputIsDetected() {
            var signatureInput = "test=(\"@query-param\";names=\"ok\")";
            var signature = "test=:YQ==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .targetUri("https://example.com/?name=me")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_STRUCTURED_HEADER);
            assertThat(exception).hasMessageContaining("Signature-Input");
        }

        @Test
        void missingRequiredParameterIsDetected() {
            // setup
            var verificationSpec = ObjectMother.getVerificationSpecBuilder("test=();created=1234567890", "test=:YQ==:")
                    .requiredParameters(SignatureParameterType.CREATED, SignatureParameterType.NONCE)
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_REQUIRED);
            assertThat(exception).hasMessageContaining("nonce");
        }

        @Test
        void missingAlgorithmIsDetected() {
            // setup
            var verificationSpec = ObjectMother.getVerificationSpecBuilder("test=()", "test=:YQ==:")
                    .publicKeyGetter(keyId -> PublicKeyInfo.builder()
                            .publicKey(ObjectMother.getEdPublicKey())
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_ALGORITHM);
            assertThat(exception).hasMessageContaining("algorithm");
        }

        @Test
        void missingPresentHeaderIsDetected() {
            // setup
            var signatureInput = "test=(\"my-header\")";
            var signature = "test=:YQ==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .requiredIfPresentComponents(SignatureComponents.builder()
                            .header("my-required-header")
                            .build())
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .header("my-header", "one")
                            .header("my-required-header", "two")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_REQUIRED);
            assertThat(exception).hasMessageContaining("my-required-header");
        }

        @Test
        void missingPresentDictionaryKeyIsDetected() {
            // setup
            var signatureInput = "test=(\"my-header\" \"my-required-dict\";key=\"one\")";
            var signature = "test=:YQ==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .requiredIfPresentComponents(SignatureComponents.builder()
                            .dictionaryMember("my-required-dict", "two")
                            .build())
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .header("my-header", "one")
                            .header("my-required-dict", "two")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_REQUIRED);
            assertThat(exception).hasMessageContaining("my-required-dict");
        }
    }

    @Nested
    class PublicKeyTest {
        @Test
        void publicKeyAlgorithmOverridesOneInParameters() {
            // setup
            var signatureInput = "test=();alg=\"ecdsa-p256-sha256\"";
            var signature = "test=:4CpbBoaIi/KZGQrzdQ1ybHNG9DrQzwxxK2XBXRKPUj5mKebWb9uV+Rl2D4bJStym24PomE5+08f1KoBfHxLzBg==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder(signatureInput, signature).build();

            // execute & verify
            assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {"@invalid", "aGVsbG8="})
        void invalidPublicKeyIsDetected(String publicKey) {
            // setup
            var signatureInput = "test=()";
            var signature = "test=:YQ==:";
            var verificationSpec = VerificationSpec.builder()
                    .signatureLabel(ObjectMother.SIGNATURE_LABEL)
                    .publicKeyGetter(keyId -> PublicKeyInfo.builder()
                            .publicKey(publicKey)
                            .algorithm(SignatureAlgorithm.ED_25519)
                            .build())
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_KEY);
        }

        @Test
        void missingPublicKeyIdDetected() {
            // setup
            var signatureInput = "test=()";
            var signature = "test=:YQ==:";
            var verificationSpec = VerificationSpec.builder()
                    .signatureLabel(ObjectMother.SIGNATURE_LABEL)
                    .publicKeyGetter(keyId -> PublicKeyInfo.builder()
                            .algorithm(SignatureAlgorithm.ED_25519)
                            .build())
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_KEY);
            assertThat(exception).hasMessageContaining("public key");
        }

        @Test
        void binaryPublicKeyIsUsed() {
            // setup
            var signatureInput = "test=()";
            var signature = "test=:ZdapoyEz/RbaQf9SBIh7Qk5sqzDfWyxKMMRkg6nDZazOD1kLIl44m0ds/Sgd1fiEVdJkS/0r8QAzGDckYh5KBg==:";
            var verificationSpec = VerificationSpec.builder()
                    .signatureLabel(ObjectMother.SIGNATURE_LABEL)
                    .publicKeyGetter(keyId -> PublicKeyInfo.builder()
                            .publicKey(Base64.getDecoder().decode(ObjectMother.getEdPublicKeyStripped()))
                            .algorithm(SignatureAlgorithm.ED_25519)
                            .build())
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .build())
                    .build();

            // execute & verify
            assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
        }

        @Test
        void objectPublicKeyIsUsed() throws Exception {
            // setup
            var signatureInput = "test=()";
            var signature = "test=:ZdapoyEz/RbaQf9SBIh7Qk5sqzDfWyxKMMRkg6nDZazOD1kLIl44m0ds/Sgd1fiEVdJkS/0r8QAzGDckYh5KBg==:";
            var keyFactory = KeyFactory.getInstance("Ed25519");
            var keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(ObjectMother.getEdPublicKeyStripped()));
            var publicKey = keyFactory.generatePublic(keySpec);

            var verificationSpec = VerificationSpec.builder()
                    .signatureLabel(ObjectMother.SIGNATURE_LABEL)
                    .publicKeyGetter(keyId -> PublicKeyInfo.builder()
                            .publicKey(publicKey)
                            .algorithm(SignatureAlgorithm.ED_25519)
                            .build())
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .build())
                    .build();

            // execute & verify
            assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
        }

        @Test
        void exceptionWhileFetchingPublicKeyIsRethrown() {
            // setup
            var signatureInput = "test=()";
            var signature = "test=:YQ==:";
            var keyException = new RuntimeException("test problem");
            var verificationSpec = VerificationSpec.builder()
                    .signatureLabel(ObjectMother.SIGNATURE_LABEL)
                    .publicKeyGetter(keyId -> {
                        throw keyException;
                    })
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_KEY);
            assertThat(exception).hasMessageContaining("public key")
                    .hasCause(keyException);
        }
    }

    @Nested
    class ExpirationTest {
        @Test
        void futureCreatedIsDetected() {
            // setup
            var signatureInput = "test=();created=" + Instant.now().plusSeconds(100).getEpochSecond();
            var signature = "test=:YQ==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder(signatureInput, signature)
                    .maximumSkew(50)
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.SIGNATURE_EXPIRED);
            assertThat(exception).hasMessageContaining("future");
        }

        @Test
        void createdExceedingMaxAgeIsDetected() {
            // setup
            var signatureInput = "test=();created=" + Instant.now().minusSeconds(100).getEpochSecond();
            var signature = "test=:YQ==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder(signatureInput, signature)
                    .maximumAge(50)
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.SIGNATURE_EXPIRED);
            assertThat(exception).hasMessageContaining("Maximum age");
        }

        @Test
        void exceededExpirationIsDetected() {
            // setup
            var createdSeconds = Instant.now().minusSeconds(100).getEpochSecond();
            var signatureInput = "test=();created=" + createdSeconds + ";expires=" + (createdSeconds + 20);
            var signature = "test=:YQ==:";
            var verificationSpec = ObjectMother.getVerificationSpecBuilder(signatureInput, signature).build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.SIGNATURE_EXPIRED);
            assertThat(exception).hasMessageContaining("Expiration");
        }
    }

    @Nested
    class SignatureHeaderTest {
        @Test
        void missingSignatureInputHeaderIsDetected() {
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE, "test=:YQ==:")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_HEADER);
            assertThat(exception).hasMessageContaining("Signature-Input");
        }

        @Test
        void missingLabelInSignatureInputIsDetected() {
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, "hello=()")
                            .header(SignatureHeaders.SIGNATURE, "test=:YQ==:")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_DICTIONARY_KEY);
            assertThat(exception).hasMessageContaining("test");
        }

        @Test
        void missingSignatureHeaderIsDetected() {
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, "test=()")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_HEADER);
            assertThat(exception).hasMessageContaining("Signature");
        }

        @ParameterizedTest
        @CsvSource({"Signature, test=(), test=:@test:", "Signature-Input, test=test, test=:YQ==:", "Signature-Input, test=(test), test=:YQ==:"})
        void invalidSignatureHeaderIsDetected(String headerName, String signatureInput, String signature) {
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_STRUCTURED_HEADER);
            assertThat(exception).hasMessageContaining(headerName);
        }

        @Test
        void missingLabelInSignatureIsDetected() {
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, "test=()")
                            .header(SignatureHeaders.SIGNATURE, "hello=:YQ==:")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_DICTIONARY_KEY);
            assertThat(exception).hasMessageContaining("test");
        }

        @ParameterizedTest
        @CsvSource(value = {"my-header,par", "my-header,name=q", "@query-param,sf;name=q", "@query,key", "@query,par"})
        void illegalComponentParameterIsDetected(String componentName, String param) {
            // setup
            var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, "test=(\"" + componentName + "\";" + param + ");created=1234567890")
                            .header(SignatureHeaders.SIGNATURE, "test=:YQ==:")
                            .header("my-header", "example-value")
                            .build())
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_STRUCTURED_HEADER);
            assertThat(exception).hasMessageContaining(SignatureHeaders.SIGNATURE_INPUT);
        }

        @Test
        void unknownDerivedComponentIsDetected() {
            // setup
            var verificationSpec = ObjectMother.getVerificationSpecBuilder("test=(\"@hello\");created=1234567890", "test=:YQ==:")
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_STRUCTURED_HEADER);
            assertThat(exception).hasMessageContaining(SignatureHeaders.SIGNATURE_INPUT);
        }

        @ParameterizedTest
        @CsvSource(value = {"\"hello\" \"hi\" \"hello\"", "\"test1\";req;key=\"a\" \"test1\" \"test1\";key=\"a\";req"})
        void duplicateComponentIsDetected(String components) {
            // setup
            var verificationSpec = ObjectMother.getVerificationSpecBuilder("test=(" + components + ")", "test=:YQ==:")
                    .build();

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.INVALID_STRUCTURED_HEADER);
            assertThat(exception).hasMessageContaining(SignatureHeaders.SIGNATURE_INPUT);
        }
    }

    @Nested
    class ApplicationTagTest {
        @Test
        void signatureLabelIsCheckedWhenProvided() {
            // setup
            var verificationSpec = getVerificationSpec(null, "second");

            // execute & verify
            assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
        }

        @Test
        void bothSignatureLabelAndTagAreCheckedWhenProvided() {
            // setup
            var verificationSpec = getVerificationSpec("dos", "second");

            // execute & verify
            assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
        }

        @Test
        void multipleSignaturesMatchingTagAreDetected() {
            // setup
            var verificationSpec = getVerificationSpec(null, "first");

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.DUPLICATE_TAG);
            assertThat(exception).hasMessageContaining("Multiple first tags");
        }

        @Test
        void noSignatureMatchingTagIsDetected() {
            // setup
            var verificationSpec = getVerificationSpec(null, "third");

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_TAG);
            assertThat(exception).hasMessageContaining("Missing third tag");
        }

        @Test
        void signatureMatchingLabelButNotTagIsDetected() {
            // setup
            var verificationSpec = getVerificationSpec("uno", "fourth");

            // execute
            var exception = catchThrowableOfType(verificationSpec::verify, SignatureException.class);

            // verify
            assertThat(exception.getErrorCode()).isEqualTo(SignatureException.ErrorCode.MISSING_TAG);
            assertThat(exception).hasMessageContaining("Missing fourth tag");
        }

        private VerificationSpec getVerificationSpec(String signatureLabel, String applicationTag) {
            var signatureInput = "uno=();keyid=\"one\";tag=\"first\", dos0=();keyid=\"two\";tag=\"first\", dos=();keyid=\"two\";tag=\"second\"";
            var signature = "uno=:ZdapoyEz/RbaQf9SBIh7Qk5sqzDfWyxKMMRkg6nDZazOD1kLIl44m0ds/Sgd1fiEVdJkS/0r8QAzGDckYh5KBg==:, " +
                    "dos0=:BKyvE+j2+ZrB/6wzecAqIG1naIK4nHYyGzeE92HBR/RJn9ygQK73l81s3eC8Uto67GLZiiUYFlDXmbl++vp5AA==:, " +
                    "dos=:BKyvE+j2+ZrB/6wzecAqIG1naIK4nHYyGzeE92HBR/RJn9ygQK73l81s3eC8Uto67GLZiiUYFlDXmbl++vp5AA==:";

            return VerificationSpec.builder()
                    .signatureLabel(signatureLabel)
                    .applicationTag(applicationTag)
                    .publicKeyGetter(keyId -> PublicKeyInfo.builder()
                            .publicKey(ObjectMother.getEdPublicKey())
                            .algorithm(SignatureAlgorithm.ED_25519)
                            .build())
                    .context(SignatureContext.builder()
                            .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                            .header(SignatureHeaders.SIGNATURE, signature)
                            .build())
                    .build();
        }
    }
}
