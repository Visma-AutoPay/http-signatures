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

import net.visma.autopay.http.structured.StructuredDictionary;
import org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.Provider;
import java.security.Security;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


// Copied from https://datatracker.ietf.org/doc/html/draft-ietf-httpbis-message-signatures
class SignatureSpecificationTest {
    private String requestMethod = "POST";
    private String requestUri;
    private Map<String, String> requestHeaders;
    private SignatureContext requestContext;
    private int responseStatus;
    private byte[] relatedSignatureData;


    @BeforeEach
    void setUp(TestInfo testInfo) {
        requestMethod = "POST";
        requestUri = "https://example.com/foo?param=Value&Pet=dog";
        requestHeaders = Map.of(
                "Host", "example.com",
                "Date", "Tue, 20 Apr 2021 02:07:55 GMT",
                "Content-Type", "application/json",
                "Content-Digest", "sha-512=:WZDPaVn/7XgHaAy8pmojAkGWoRx2UFChF41A2svX+TaPm+AbwAgBWnrIiYllu7BNNyealdVLvRwEmTHWXvJwew==:",
                "Content-Length", "18"
        );

        requestContext = SignatureContext.builder()
                .method(requestMethod)
                .targetUri(requestUri)
                .headers(requestHeaders)
                .build();

        responseStatus = 200;

        if (testInfo.getTags().contains("Ed25519")) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        if (testInfo.getTags().contains("Ed25519")) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test
    @DisplayName("Minimal Signature Using rsa-pss-sha512")
    void minimalSignatureRsaPss() throws Exception {
        // setup
        var signatureLabel = "sig-b21";
        var keyId = ObjectMother.getRsaPssKeyId();
        var algorithm = SignatureAlgorithm.RSA_PSS_SHA_512;
        var nonce = "b3k2pp5k7z-50gnwp.yemd";

        var signatureParams = SignatureParameters.builder()
                .created(Instant.ofEpochSecond(1618884473))
                .keyId(keyId)
                .nonce(nonce)
                .algorithm(algorithm)
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getRsaPssPrivateKey())
                .context(requestContext)
                .parameters(signatureParams)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getRsaPssPublicKey())
                .build();
        var expectedSignatureInput = "sig-b21=();created=1618884473;keyid=\"test-key-rsa-pss\";nonce=\"b3k2pp5k7z-50gnwp.yemd\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature result
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "sig-b21=:d2pmTvmbncD3xQm8E9ZV2828BjQWGgiwAaw5bAkgibUopemLJcWDy/lkbbHA" +
                "ve4cRAtx31Iq786U7it++wgGxbtRxf8Udx7zFZsckzXaJMkA7ChG52eSkFxykJeNqsrWH5S+oxNFlD4dzV" +
                "uwe8DhTSja8xxbR/Z2cOGdCbzR72rgFWhzx2VjBqJzsPLMIQKhO4DGezXehhWwE56YCE+O6c0mKZsfxVro" +
                "gUvA4HELjVKWmAvtl6UnCh8jYzuVG5WSb/QEVPnP5TmcAnLH1g+s++v6d4s8m0gCw1fV5/SITLq9mhho8K" +
                "3+7EPYTU8IU1bLhdxO5Nyt8C8ssinQ98Xw9Q==:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("Selective Covered Components using rsa-pss-sha512")
    void selectiveCoveredComponentsRsaPss() throws Exception {
        // setup
        var signatureLabel = "sig-b22";
        var keyId = ObjectMother.getRsaPssKeyId();
        var algorithm = SignatureAlgorithm.RSA_PSS_SHA_512;
        var tag = "header-example";

        SignatureParameters signatureParams = SignatureParameters.builder()
                .created(1618884473)
                .keyId(keyId)
                .tag(tag)
                .algorithm(algorithm)
                .build();
        var signatureComponents = SignatureComponents.builder()
                .authority()
                .header("Content-Digest")
                .queryParam("Pet")
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getRsaPssPrivateKey())
                .context(requestContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getRsaPssPublicKey())
                .build();
        var expectedSignatureInput = "sig-b22=(\"@authority\" \"content-digest\" \"@query-param\";name=\"Pet\");created=1618884473;keyid=\"test-key-rsa-pss\"" +
                ";tag=\"header-example\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "sig-b22=:LjbtqUbfmvjj5C5kr1Ugj4PmLYvx9wVjZvD9GsTT4F7GrcQ" +
                "EdJzgI9qHxICagShLRiLMlAJjtq6N4CDfKtjvuJyE5qH7KT8UCMkSowOB4+ECxCmT" +
                "8rtAmj/0PIXxi0A0nxKyB09RNrCQibbUjsLS/2YyFYXEu4TRJQzRw1rLEuEfY17SA" +
                "RYhpTlaqwZVtR8NV7+4UKkjqpcAoFqWFQh62s7Cl+H2fjBSpqfZUJcsIk4N6wiKYd" +
                "4je2U/lankenQ99PZfB4jY3I5rSV2DSBVkSFsURIjYErOs0tFTQosMTAoxk//0RoK" +
                "UqiYY8Bh0aaUEb0rQl3/XaVe4bXTugEjHSw==:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("Full Coverage using rsa-pss-sha512")
    void fullCoverageRsaPss() throws Exception {
        // setup
        var signatureLabel = "sig-b23";
        var keyId = ObjectMother.getRsaPssKeyId();
        var algorithm = SignatureAlgorithm.RSA_PSS_SHA_512;

        SignatureParameters signatureParams = getSignatureParameters(keyId, algorithm);
        var signatureComponents = SignatureComponents.builder()
                .header("Date")
                .method().path().query().authority()
                .headers("Content-Type", "Content-Digest", "Content-Length")
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getRsaPssPrivateKey())
                .context(requestContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getRsaPssPublicKey())
                .build();
        var expectedSignatureInput = "sig-b23=(\"date\" \"@method\" \"@path\" \"@query\" \"@authority\" \"content-type\" \"content-digest\" " +
                "\"content-length\");created=1618884473;keyid=\"test-key-rsa-pss\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "sig-b23=:bbN8oArOxYoyylQQUU6QYwrTuaxLwjAC9fbY2F6SVWvh0yBiMIRGOnMYwZ/5" +
                "MR6fb0Kh1rIRASVxFkeGt683+qRpRRU5p2voTp768ZrCUb38K0fUxN0O0iC59DzYx8DFll5GmydPxSmme9" +
                "v6ULbMFkl+V5B1TP/yPViV7KsLNmvKiLJH1pFkh/aYA2HXXZzNBXmIkoQoLd7YfW91kE9o/CCoC1xMy7JA" +
                "1ipwvKvfrs65ldmlu9bpG6A9BmzhuzF8Eim5f8ui9eH8LZH896+QIF61ka39VBrohr9iyMUJpvRX2Zbhl5" +
                "ZJzSRxpJyoEZAFL2FUo5fTIztsDZKEgM4cUA==:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("Signing a Response using ecdsa-p256-sha256")
    void signingResponseEcdsaP256Sha256() throws Exception {
        // setup
        var signatureLabel = "sig-b24";
        var keyId = ObjectMother.getEc256KeyId();
        var algorithm = SignatureAlgorithm.ECDSA_P256_SHA_256;
        requestHeaders = Map.of(
                "Date", "Tue, 20 Apr 2021 02:07:56 GMT",
                "Content-Type", "application/json",
                "Content-Digest", "sha-512=:mEWXIS7MaLRuGgxOBdODa3xqM1XdEvxoYhvlCFJ41QJgJc4GTsPp29l5oGX69wWdXymyU0rjJuahq4l5aGgfLQ==:",
                "Content-Length", "23"
        );
        requestContext = SignatureContext.builder()
                .status(responseStatus)
                .headers(requestHeaders)
                .build();
        SignatureParameters signatureParams = getSignatureParameters(keyId, algorithm);
        var signatureComponents = SignatureComponents.builder()
                .status()
                .headers("Content-Type", "Content-Digest", "Content-Length")
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getEc256PrivateKey())
                .context(requestContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getEc256PublicKey())
                .build();
        var expectedSignatureInput = "sig-b24=(\"@status\" \"content-type\" \"content-digest\" \"content-length\");created=1618884473;" +
                "keyid=\"test-key-ecc-p256\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "sig-b24=:wNmSUAhwb5LxtOtOpNa6W5xj067m5hFrj0XQ4fvpaCLx0NKocgPquLgyahnzDnDAUy5eCdlYUEkLIj+32oiasw==:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult.getSignatureInput(), validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("Signing a Response using ecdsa-p384-sha384")
    void signingResponseEcdsaP384Sha384() throws Exception {
        // setup
        var signatureLabel = "sig-b24a";
        var keyId = ObjectMother.getEc384KeyId();
        var algorithm = SignatureAlgorithm.ECDSA_P384_SHA_384;
        requestHeaders = Map.of(
                "Date", "Tue, 20 Apr 2021 02:07:56 GMT",
                "Content-Type", "application/json",
                "Content-Digest", "sha-512=:mEWXIS7MaLRuGgxOBdODa3xqM1XdEvxoYhvlCFJ41QJgJc4GTsPp29l5oGX69wWdXymyU0rjJuahq4l5aGgfLQ==:",
                "Content-Length", "23"
        );
        requestContext = SignatureContext.builder()
                .status(responseStatus)
                .headers(requestHeaders)
                .build();
        SignatureParameters signatureParams = getSignatureParameters(keyId, algorithm);
        var signatureComponents = SignatureComponents.builder()
                .status()
                .headers("Content-Type", "Content-Digest", "Content-Length")
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getEc384PrivateKey())
                .context(requestContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getEc384PublicKey())
                .build();
        var expectedSignatureInput = "sig-b24a=(\"@status\" \"content-type\" \"content-digest\" \"content-length\");created=1618884473;" +
                "keyid=\"test-key-ecc-p384\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "sig-b24a=:baHYylvLrO/oP/gozq3wlBvU3GjV9e7/HEx+VuzLUshMX0ghKAfPvccYpL1" +
                "PEFROU77G4fQfy/TcYicBC22aIk3o8c0M1S3f1K/6lAZAhIFUflun77r33Pn2weHlwN8v:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult.getSignatureInput(), validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("Signing a Request using hmac-sha256")
    void signingRequestHmacSha256() throws Exception {
        // setup
        var signatureLabel = "sig-b25";
        var keyId = ObjectMother.getHmacKeyId();
        var algorithm = SignatureAlgorithm.HMAC_SHA_256;

        SignatureParameters signatureParams = getSignatureParameters(keyId, algorithm);
        var signatureComponents = SignatureComponents.builder()
                .header("Date")
                .authority()
                .header("Content-Type")
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getHmacKey())
                .context(requestContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getHmacKey())
                .build();
        var expectedSignatureInput = "sig-b25=(\"date\" \"@authority\" \"content-type\");created=1618884473;keyid=\"test-shared-secret\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "sig-b25=:pxcQw6G3AjtMBQjwo8XzkZf/bws5LelbaMk5rGIGtE8=:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("Signing a Request using ed25519")
    @Tag("Ed25519")
    void signingRequestEd25519() throws Exception {
        // setup
        var signatureLabel = "sig-b26";
        var keyId = ObjectMother.getEdKeyId();
        var algorithm = SignatureAlgorithm.ED_25519;

        SignatureParameters signatureParams = getSignatureParameters(keyId, algorithm);
        var signatureComponents = SignatureComponents.builder()
                .header("Date")
                .method().path().authority()
                .headers("Content-Type", "Content-Length")
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getEdPrivateKey())
                .context(requestContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getEdPublicKey())
                .build();
        var expectedSignatureInput = "sig-b26=(\"date\" \"@method\" \"@path\" \"@authority\" \"content-type\" \"content-length\");created=1618884473;" +
                "keyid=\"test-key-ed25519\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "sig-b26=:wqcAqbmYJ2ji2glfAMaRy4gruYYnx2nEFN2HN6jrnDnQCK1u02Gb04v9EDgwUPiu4A0w6vuQv5lIp5WPpBKRCw==:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("TLS-Terminating Proxies")
    void tlsTerminatingProxies() throws Exception {
        // setup
        var signatureLabel = "ttrp";
        var keyId = ObjectMother.getEc256KeyId();
        var algorithm = SignatureAlgorithm.ECDSA_P256_SHA_256;
        requestUri = "https://service.internal.example/foo?param=Value&Pet=dog";

        requestHeaders = Map.of(
                "client-cert", ":MIIBqDCCAU6gAwIBAgIBBzAKBggqhkjOPQQDAjA6MRswGQYDVQQKDBJMZXQncyBBdX" +
                        "RoZW50aWNhdGUxGzAZBgNVBAMMEkxBIEludGVybWVkaWF0ZSBDQTAeFw0yMDAxMTQyMjU1MzNa" +
                        "Fw0yMTAxMjMyMjU1MzNaMA0xCzAJBgNVBAMMAkJDMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQg" +
                        "AE8YnXXfaUgmnMtOXU/IncWalRhebrXmckC8vdgJ1p5Be5F/3YC8OthxM4+k1M6aEAEFcGzkJi" +
                        "Ny6J84y7uzo9M6NyMHAwCQYDVR0TBAIwADAfBgNVHSMEGDAWgBRm3WjLa38lbEYCuiCPct0ZaS" +
                        "ED2DAOBgNVHQ8BAf8EBAMCBsAwEwYDVR0lBAwwCgYIKwYBBQUHAwIwHQYDVR0RAQH/BBMwEYEP" +
                        "YmRjQGV4YW1wbGUuY29tMAoGCCqGSM49BAMCA0gAMEUCIBHda/r1vaL6G3VliL4/Di6YK0Q6bM" +
                        "jeSkC3dFCOOB8TAiEAx/kHSB4urmiZ0NX5r5XarmPk0wmuydBVoU4hBVZ1yhk=:"
        );
        var responseContext = SignatureContext.builder()
                .method("POST")
                .targetUri(requestUri)
                .headers(requestHeaders)
                .build();
        SignatureParameters signatureParams = getSignatureParameters(keyId, algorithm);
        var signatureComponents = SignatureComponents.builder()
                .path().query().method().authority()
                .header("client-cert")
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getEc256PrivateKey())
                .context(responseContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getEc256PublicKey())
                .build();
        var expectedSignatureInput = "ttrp=(\"@path\" \"@query\" \"@method\" \"@authority\" \"client-cert\");created=1618884473;keyid=\"test-key-ecc-p256\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "ttrp=:xVMHVpawaAC/0SbHrKRs9i8I3eOs5RtTMGCWXm/9nvZzoHsIg6Mce9315T6xoklyy0yzhD9ah4JHRwMLOgmizw==:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("Request components in response")
    void requestResponseBinding() throws Exception {
        // setup
        var requestSignatureLabel = "sig1";
        var signatureLabel = "reqres";
        var keyId = ObjectMother.getEc256KeyId();
        var algorithm = SignatureAlgorithm.ECDSA_P256_SHA_256;
        responseStatus = 503;

        SignatureParameters signatureParams = SignatureParameters.builder()
                .created(Instant.ofEpochSecond(1618884479))
                .keyId(keyId)
                .algorithm(algorithm)
                .build();
        var signatureComponents = SignatureComponents.builder()
                .status()
                .headers("Content-Length", "Content-Type")
                .relatedRequestDictionaryMember("Signature", requestSignatureLabel)
                .build();
        requestHeaders = Map.of(
                "Content-Length", "62",
                "Content-Type", "application/json"
        );
        relatedSignatureData = Base64.getDecoder().decode("LAH8BjcfcOcLojiuOBFWn0P5keD3xAOuJRGziCLu" +
                "D8r5MW9S0RoXXLzLSRfGY/3SF8kVIkHjE13SEFdTo4Af/fJ/Pu9wheqoLVdwXyY/UkBIS1M8Brc8IODsn5" +
                "DFIrG0IrburbLi0uCc+E2ZIIb6HbUJ+o+jP58JelMTe0QE3IpWINTEzpxjqDf5/Df+InHCAkQCTuKsamjW" +
                "XUpyOT1Wkxi7YPVNOjW4MfNuTZ9HdbD2Tr65+BXeTG9ZS/9SWuXAc+BZ8WyPz0QRz//ec3uWXd7bYYODSj" +
                "RAxHqX+S1ag3LZElYyUKaAIjZ8MGOt4gXEwCSLDv/zqxZeWLj/PDkn6w==");
        requestContext = SignatureContext.builder()
                .status(503)
                .headers(requestHeaders)
                .relatedRequest(SignatureContext.builder()
                        .header("Signature", StructuredDictionary.of(requestSignatureLabel, relatedSignatureData).serialize())
                        .build())
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getEc256PrivateKey())
                .context(requestContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getEc256PublicKey())
                .build();
        var expectedSignatureInput = "reqres=(\"@status\" \"content-length\" \"content-type\" \"signature\";req;key=\"sig1\");created=1618884479;" +
                "keyid=\"test-key-ecc-p256\"";

        // execute
        var signatureResult = signatureSpec.sign();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "reqres=:Cm7JJUxsmBy406AxT9gOnrHZrqUXUclJyOQm9ymX3uA1oBHzHS49Xo+AWJQbXeB40zR18khj4g/iE9uokVdK1A==:";
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, validSignature);
        verificationSpec.verify();
    }

    @Test
    @DisplayName("Multiple Signatures")
    void multipleSignatures() throws Exception {
        // setup
        var prevSignatureLabel = "sig1";
        var signatureLabel = "proxy_sig";
        var keyId = ObjectMother.getRsaKeyId();
        var algorithm = SignatureAlgorithm.RSA_SHA_256;

        SignatureParameters signatureParams = SignatureParameters.builder()
                .created(Instant.ofEpochSecond(1618884480))
                .expires(2145809960)
                .keyId(keyId)
                .visibleAlgorithm(algorithm)
                .build();
        var signatureComponents = SignatureComponents.builder()
                .dictionaryMember("Signature", prevSignatureLabel)
                .header("forwarded")
                .build();
        var prevSignature = "sig1=:LAH8BjcfcOcLojiuOBFWn0P5keD3xAOuJRGziCLuD8r5MW9S0RoXXLzLSRfGY/3S" +
                "F8kVIkHjE13SEFdTo4Af/fJ/Pu9wheqoLVdwXyY/UkBIS1M8Brc8IODsn5DFIrG0IrburbLi0uCc+E2ZII" +
                "b6HbUJ+o+jP58JelMTe0QE3IpWINTEzpxjqDf5/Df+InHCAkQCTuKsamjWXUpyOT1Wkxi7YPVNOjW4MfNu" +
                "TZ9HdbD2Tr65+BXeTG9ZS/9SWuXAc+BZ8WyPz0QRz//ec3uWXd7bYYODSjRAxHqX+S1ag3LZElYyUKaAIj" +
                "Z8MGOt4gXEwCSLDv/zqxZeWLj/PDkn6w==:";
        requestHeaders = Map.of(
                "Signature", prevSignature,
                "Forwarded", "for=192.0.2.123"
        );
        requestContext = SignatureContext.builder()
                .headers(requestHeaders)
                .build();
        var signatureSpec = SignatureSpec.builder()
                .signatureLabel(signatureLabel)
                .privateKey(ObjectMother.getRsaPrivateKey())
                .context(requestContext)
                .parameters(signatureParams)
                .components(signatureComponents)
                .build();
        var publicKeyInfo = PublicKeyInfo.builder()
                .algorithm(algorithm)
                .publicKey(ObjectMother.getRsaPublicKey())
                .build();
        var expectedSignatureInput = "proxy_sig=(\"signature\";key=\"sig1\" \"forwarded\");created=1618884480;expires=2145809960;keyid=\"test-key-rsa\";" +
                "alg=\"rsa-v1_5-sha256\"";

        // execute
        var signatureResult = signatureSpec.sign();
        var multiSignatureHeader = prevSignature + "," + signatureResult.getSignature();

        // verify signature input
        assertThat(signatureResult.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify self signature
        var verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, multiSignatureHeader);
        verificationSpec.verify();

        // verify example signature
        var validSignature = "proxy_sig=:ExHrNxgtUCrDoUrHIOxVIZkTf8XOzg9dhFtvN27X40o8Bn+cUTHT618rqj" +
                "0omkTCgaWyy1/w2Jh35yLyP8pu4XpAP3IfER5rDqaGLA1yF2Y8+5XHJFdMzouQGUo7EItKMQT3YhgUGA/q" +
                "jje6EAbtGoYM/fVgB40mlx8QPztZH3mw/3qEt9zdWpjCUWJ/ClNPM/gmTfWGztT2TbF3v0/z4UfD+AexgK" +
                "3fTfHlehx6M6zQvBvIWsg9l0OkHxWqGxXSnLziEi2LDEweXQG6Xgj29sqFH5rAsJVZWhXS77CthSvHaRbN" +
                "FSybcVw/SCHD1Kvw9q6feJquj08pezf5tmoL2g==:";
        multiSignatureHeader = prevSignature + "," + validSignature;
        verificationSpec = getVerificationSpec(signatureLabel, keyId, publicKeyInfo, expectedSignatureInput, multiSignatureHeader);
        verificationSpec.verify();
    }

    @Nested
    class BouncyCastleTest {
        @BeforeEach
        void setUp() {
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
            Security.insertProviderAt(new BouncyCastleP1363Provider(), 1);
        }

        @AfterEach
        void tearDown() {
            Security.removeProvider("BC");
            Security.removeProvider("BcP1363");
        }

        @ParameterizedTest
        @ValueSource(strings = {"minimalSignatureRsaPss", "selectiveCoveredComponentsRsaPss", "fullCoverageRsaPss", "signingResponseEcdsaP256Sha256",
                "signingResponseEcdsaP384Sha384", "signingRequestHmacSha256", "signingRequestEd25519", "tlsTerminatingProxies", "requestResponseBinding",
                "multipleSignatures"})
        void bouncyCastle(String methodName) throws Exception {
            var method = SignatureSpecificationTest.class.getDeclaredMethod(methodName);
            assertThatCode(() -> method.invoke(SignatureSpecificationTest.this)).doesNotThrowAnyException();
        }
    }

    private static class BouncyCastleP1363Provider extends Provider {
        public BouncyCastleP1363Provider() {
            super("BcP1363", "1.0", "Bouncy Castle - P1363 Bridge");
            put("Signature.SHA256withECDSAinP1363Format", SignatureSpi.ecCVCDSA256.class.getName());
            put("Signature.SHA384withECDSAinP1363Format", SignatureSpi.ecCVCDSA384.class.getName());
        }
    }

    private SignatureParameters getSignatureParameters(String keyId, SignatureAlgorithm algorithm) {
        return SignatureParameters.builder()
                .created(1618884473)
                .keyId(keyId)
                .algorithm(algorithm)
                .build();
    }

    private VerificationSpec getVerificationSpec(String signatureLabel, String keyId, PublicKeyInfo publicKeyInfo, SignatureResult signatureResult) {
        return getVerificationSpec(signatureLabel, keyId, publicKeyInfo, signatureResult.getSignatureInput(), signatureResult.getSignature());
    }

    private VerificationSpec getVerificationSpec(String signatureLabel, String keyId, PublicKeyInfo publicKeyInfo, String signatureInputHeader,
                                                 String signatureHeader) {
        return getVerificationSpec(signatureLabel, keyId, publicKeyInfo, requestHeaders, signatureInputHeader, signatureHeader);
    }

    private VerificationSpec getVerificationSpec(String signatureLabel, String keyId, PublicKeyInfo publicKeyInfo, Map<String, String> headers,
                                                 String signatureInputHeader, String signatureHeader) {
        var requiredParameters = Set.of(SignatureParameterType.CREATED, SignatureParameterType.KEY_ID);

        var signedHeaders = new HashMap<>(headers);
        signedHeaders.put(SignatureHeaders.SIGNATURE_INPUT, signatureInputHeader);
        signedHeaders.put(SignatureHeaders.SIGNATURE, signatureHeader);

        var verificationSignatureContextBuilder = SignatureContext.builder()
                .status(responseStatus)
                .method(requestMethod)
                .targetUri(requestUri)
                .headers(signedHeaders);

        if (relatedSignatureData != null) {
            verificationSignatureContextBuilder.relatedRequest(SignatureContext.builder()
                    .header("Signature", StructuredDictionary.of("sig1", relatedSignatureData))
                    .build());
        }

        var verificationSignatureContext = verificationSignatureContextBuilder.build();

        return VerificationSpec.builder()
                .signatureLabel(signatureLabel)
                .publicKeyGetter(id -> id.equals(keyId) ? publicKeyInfo : null)
                .requiredParameters(requiredParameters)
                .context(verificationSignatureContext)
                .build();
    }
}
