/*
 * Copyright (c) 2022-2024 Visma Autopay AS
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.Security;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


class SignatureBaseSpecificationTest {
    @BeforeAll
    static void beforeAll() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @AfterAll
    static void afterAll() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    @Test
    void emptyHeaderIsIncluded() throws Exception {
        // setup
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .header("My-Header")
                        .build())
                .context(SignatureContext.builder()
                        .header("My-Header", "")
                        .build())
                .build();

        var expectedSignatureBase = "\"my-header\": \n" +
                "\"@signature-params\": (\"my-header\")";
        var expectedSignatureInput = "test=(\"my-header\")";
        var expectedSignature = "test=:wLIeqFOgn2lMFxDxQyL8WXXkE5Pyf9WZePxocVDXD1vSQ0aU3KRIIe6MGr8v37fJvGuK1Jwm9Q5a3iaKB08/Bg==:";

        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .header("My-Header", "")
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void spacesAndNewLinesAreReplaced() throws Exception {
        // setup
        var headerOne = "  Leading and trailing whitespace.  ";
        var headerTwo = "Line 1\n  Line 2\r Line 3\r\nLine 4 ";
        var headerThree1 = "First value";
        var headerThree2 = "   Second value";
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .headers(List.of("Header-One", "Header-Two", "Header-Three"))
                        .build())
                .context(SignatureContext.builder()
                        .header("Header-One", headerOne)
                        .header("Header-Two", headerTwo)
                        .header("Header-Three", headerThree1)
                        .header("Header-Three", headerThree2)
                        .build())
                .build();
        var expectedSignatureInput = "test=(\"header-one\" \"header-two\" \"header-three\")";
        var expectedSignature = "test=:kFCDaD5gIB81E8e8/XsYD26easpvdAEC/OYGdS1xDAcMZ9f9IDeo1JEjhapJ2vgTYoHXlZ3acHxbim4f4NyaCw==:";
        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .header("Header-One", headerOne)
                        .header("Header-Two", headerTwo)
                        .header("Header-Three", headerThree1)
                        .header("Header-Three", headerThree2)
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void structuredFields() throws Exception {
        // setup
        var headerValue = "   a=1,    b=2; x=1.00;y=2,   c=(a   b   c)  ";
        var headerName = "Example-Dict";
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .structuredHeader(headerName)
                        .build())
                .context(SignatureContext.builder()
                        .header(headerName, headerValue)
                        .build())
                .build();

        var expectedSignatureBase = "\"example-dict\";sf: a=1, b=2;x=1.0;y=2, c=(a b c)\n" +
                "\"@signature-params\": (\"example-dict\";sf)";
        var expectedSignatureInput = "test=(\"example-dict\";sf)";
        var expectedSignature = "test=:RvfPV4DkES+4I/SX9wjeN4Gv35GbGNbmuo1TBu+w3oS71So4yiTY/i3wV2XxMJefhkwJoMBRDRRH4trsUvd0Ag==:";

        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .header(headerName, headerValue)
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void dictionaryFiledMembers() throws Exception {
        // setup
        var headerValue = " a=1, b=2;x=1;y=2, c=(a   b    c) , d ";
        var headerName = "Example-Dict";
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .dictionaryMember(headerName, "a")
                        .dictionaryMember(headerName, "d")
                        .dictionaryMember(headerName, "b")
                        .dictionaryMember(headerName, "c")
                        .build())
                .context(SignatureContext.builder()
                        .header(headerName, headerValue)
                        .build())
                .build();

        var expectedSignatureBase = "\"example-dict\";key=\"a\": 1\n" +
                "\"example-dict\";key=\"d\": ?1\n" +
                "\"example-dict\";key=\"b\": 2;x=1;y=2\n" +
                "\"example-dict\";key=\"c\": (a b c)\n" +
                "\"@signature-params\": (\"example-dict\";key=\"a\" \"example-dict\";key=\"d\" \"example-dict\";key=\"b\" \"example-dict\";key=\"c\")";
        var expectedSignatureInput = "test=(\"example-dict\";key=\"a\" \"example-dict\";key=\"d\" \"example-dict\";key=\"b\" \"example-dict\";key=\"c\")";
        var expectedSignature = "test=:p702zorDQysNbb0pAQQUqZ69XACFnftgIiMTdmMUUYMgLpZ1DbwtPZyKryFR+KQkBfmDHCu/tz7WzIp426ieDw==:";

        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .header(headerName, headerValue)
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void derivedComponents() throws Exception {
        // setup
        var url = "https://example.com/foo?cat=red&dog=white&ok&blue";
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .method().targetUri().authority()
                        .requestTarget().scheme().path()
                        .status().query()
                        .queryParam("dog")
                        .queryParam("cat")
                        .queryParam("blue")
                        .relatedRequestDictionaryMember("Signature", "req")
                        .build())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                .context(SignatureContext.builder()
                        .targetUri(url)
                        .method("POST")
                        .status(200)
                        .relatedRequest(SignatureContext.builder()
                                .header("Signature", StructuredDictionary.of("req", new byte[]{1}))
                                .build())
                        .build())
                .build();
        var expectedSignatureBase = "\"@method\": POST\n" +
                "\"@target-uri\": https://example.com/foo?cat=red&dog=white&ok&blue\n" +
                "\"@authority\": example.com\n" +
                "\"@request-target\": /foo?cat=red&dog=white&ok&blue\n" +
                "\"@scheme\": https\n" +
                "\"@path\": /foo\n" +
                "\"@status\": 200\n" +
                "\"@query\": ?cat=red&dog=white&ok&blue\n" +
                "\"@query-param\";name=\"dog\": white\n" +
                "\"@query-param\";name=\"cat\": red\n" +
                "\"@query-param\";name=\"blue\": \n" +
                "\"signature\";req;key=\"req\": :AQ==:\n" +
                "\"@signature-params\": (\"@method\" \"@target-uri\" \"@authority\" \"@request-target\" \"@scheme\" \"@path\" \"@status\" \"@query\" " +
                "\"@query-param\";name=\"dog\" \"@query-param\";name=\"cat\" \"@query-param\";name=\"blue\" \"signature\";req;key=\"req\")";
        var expectedSignatureInput = "test=(\"@method\" \"@target-uri\" \"@authority\" \"@request-target\" \"@scheme\" \"@path\" \"@status\" \"@query\" " +
                "\"@query-param\";name=\"dog\" \"@query-param\";name=\"cat\" \"@query-param\";name=\"blue\" \"signature\";req;key=\"req\")";
        var expectedSignature = "test=:SOvyiRZJ6buyqPntoR9Ozd/o/PNvygZRF0mE5WyAIWJVmWVnp+pkzvNkShKzL4Dtetd1sB43rEt2DjDuW9LHCQ==:";

        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .targetUri(url)
                        .method("POST")
                        .status(200)
                        .relatedRequest(SignatureContext.builder()
                                .header("Signature", StructuredDictionary.of("req", new byte[]{1}))
                                .build())
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/a%21b", "/a%C3%B3b", "/a!b"})
    void pathBeforeDecodingIsUsed(String path) throws Exception {
        // setup
        var url = "https://example.com" + path;
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .targetUri()
                        .requestTarget()
                        .path()
                        .build())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                .context(SignatureContext.builder()
                        .targetUri(url)
                        .method("POST")
                        .build())
                .build();
        var expectedSignatureBase = "\"@target-uri\": https://example.com" + path + "\n" +
                "\"@request-target\": " + path + "\n" +
                "\"@path\": " + path + "\n" +
                "\"@signature-params\": (\"@target-uri\" \"@request-target\" \"@path\")";
        var expectedSignatureInput = "test=(\"@target-uri\" \"@request-target\" \"@path\")";

        // execute
        var result = signatureSpec.sign();

        // verify signature base and input
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify signature
        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, result.getSignature())
                        .targetUri(url)
                        .method("POST")
                        .build())
                .build();

        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void queryBeforeDecodingIsUsed() throws Exception {
        // setup
        var query = "?one=a%21b&two=a%C3%B3b&t:ree=a!b";
        var url = "https://example.com" + query;
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .query()
                        .targetUri()
                        .requestTarget()
                        .build())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                .context(SignatureContext.builder()
                        .targetUri(url)
                        .method("POST")
                        .build())
                .build();
        var expectedSignatureBase = "\"@query\": " + query + "\n" +
                "\"@target-uri\": " + url + "\n" +
                "\"@request-target\": /" + query + "\n" +
                "\"@signature-params\": (\"@query\" \"@target-uri\" \"@request-target\")";
        var expectedSignatureInput = "test=(\"@query\" \"@target-uri\" \"@request-target\")";
        var expectedSignature = "test=:kjOWEWKlZpq9eNaO0CUiuYcOlouxxQNPgxR5DS5RF0QXfbhKMHhdXy6mXGRlETRjCOntPcpg5Yc10DVUPhVvDw==:";

        // execute
        var result = signatureSpec.sign();

        // verify signature base and input
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify signature
        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .targetUri(url)
                        .method("POST")
                        .build())
                .build();

        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void decodedQueryParamsAreUsed() throws Exception {
        // setup
        var decodedParamName = "q$p r+s t";
        var paramName = "q%24p+r%2Bs%20t";
        var reEncodedParamName = "q%24p%20r%2Bs%20t";
        var paramValue = "a+b%21c%2Cd%20e%2Df-g%48h%2Bi";
        var reEncodedValue = "a%20b%21c%2Cd%20e-f-gHh%2Bi";
        var url = "https://example.com?" + paramName + "=" + paramValue;
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .queryParam(decodedParamName)
                        .build())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                .context(SignatureContext.builder()
                        .targetUri(url)
                        .method("POST")
                        .build())
                .build();
        var expectedSignatureBase = "\"@query-param\";name=\"" + reEncodedParamName + "\": " + reEncodedValue + "\n" +
                "\"@signature-params\": (\"@query-param\";name=\"" + reEncodedParamName + "\")";
        var expectedSignatureInput = "test=(\"@query-param\";name=\"" + reEncodedParamName + "\")";
        var expectedSignature = "test=:re8gjxFsm11CiIcczGWI87eARrPVUnu8ajauA/GiJ5be9YO3hj5JC736AEHiqwZ/Rrm4zwl5QhsyPVKbH3yBDQ==:";

        // execute
        var result = signatureSpec.sign();

        // verify signature base and input
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);

        // verify signature
        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .targetUri(url)
                        .method("POST")
                        .build())
                .build();

        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void absentQueryStringAndPath() throws Exception {
        // setup
        var url = "https://example.com";
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .path().query()
                        .build())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                .context(SignatureContext.builder()
                        .targetUri(url)
                        .build())
                .build();

        var expectedSignatureBase = "\"@path\": /\n" +
                "\"@query\": ?\n" +
                "\"@signature-params\": (\"@path\" \"@query\")";
        var expectedSignatureInput = "test=(\"@path\" \"@query\")";
        var expectedSignature = "test=:ELoRyyb4TjhQq/F011/THBXqXo3yV6k9PHoekCds2jyXF+9tKLzENiZ0QOKc0kbqGSmb57s9VfC4Fty06GblAA==:";

        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .targetUri(url)
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void signatureParameters() throws Exception {
        // setup
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .parameters(SignatureParameters.builder()
                        .keyId(ObjectMother.getEdKeyId())
                        .visibleAlgorithm(SignatureAlgorithm.ED_25519)
                        .created(Instant.parse("2022-04-28T12:22:11Z"))
                        .expiresAfter(472_744_687)
                        .build())
                .build();
        var expectedSignatureBase = "\"@signature-params\": ();keyid=\"test-key-ed25519\";alg=\"ed25519\";created=1651148531;expires=2123893218";
        var expectedSignatureInput = "test=();keyid=\"test-key-ed25519\";alg=\"ed25519\";created=1651148531;expires=2123893218";
        var expectedSignature = "test=:KIFIiq1eGJtJ3KL/cbaqulbB/Kl4nsW1z3QkOe/j7PrTHDpHpmP4BodEkcPBwQKNaVmVuHozvV/Fbr0hVx22DQ==:";
        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void relatedRequest() throws Exception {
        // setup
        var url = "https://example.com";
        var relatedUrl = "https://service.com/api/users?name=alice";
        var relatedRequestContext = SignatureContext.builder()
                .targetUri(relatedUrl)
                .method("GET")
                .header("Content-Type", "text/html")
                .header("Dict", "x=y , a=b")
                .build();
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .relatedRequestHeader("Content-Type")
                        .relatedRequestStructuredHeader("Dict")
                        .relatedRequestAuthority()
                        .relatedRequestMethod()
                        .relatedRequestPath()
                        .relatedRequestQuery()
                        .relatedRequestQueryParam("name")
                        .relatedRequestRequestTarget()
                        .relatedRequestScheme()
                        .relatedRequestTargetUri()
                        .path().query()
                        .header("Content-Type")
                        .build())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                .context(SignatureContext.builder()
                        .targetUri(url)
                        .header("Content-type", "text/plain")
                        .relatedRequest(relatedRequestContext)
                        .build())
                .build();

        var expectedSignatureBase = "\"content-type\";req: text/html\n" +
                "\"dict\";req;sf: x=y, a=b\n" +
                "\"@authority\";req: service.com\n" +
                "\"@method\";req: GET\n" +
                "\"@path\";req: /api/users\n" +
                "\"@query\";req: ?name=alice\n" +
                "\"@query-param\";req;name=\"name\": alice\n" +
                "\"@request-target\";req: /api/users?name=alice\n" +
                "\"@scheme\";req: https\n" +
                "\"@target-uri\";req: https://service.com/api/users?name=alice\n" +
                "\"@path\": /\n" +
                "\"@query\": ?\n" +
                "\"content-type\": text/plain\n" +
                "\"@signature-params\": (\"content-type\";req \"dict\";req;sf \"@authority\";req \"@method\";req \"@path\";req \"@query\";req " +
                "\"@query-param\";req;name=\"name\" \"@request-target\";req \"@scheme\";req \"@target-uri\";req \"@path\" \"@query\" \"content-type\")";
        var expectedSignatureInput = "test=(\"content-type\";req \"dict\";req;sf \"@authority\";req \"@method\";req \"@path\";req \"@query\";req " +
                "\"@query-param\";req;name=\"name\" \"@request-target\";req \"@scheme\";req \"@target-uri\";req \"@path\" \"@query\" \"content-type\")";
        var expectedSignature = "test=:Mi6stBhUJkvXKS1UZTCCQkzb88wNm1fQYVjZ/nJmtxoE8d6VOT/BSfSUrli84/uujzdX5zHl33YWigATDAfJBw==:";
        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .header("Content-Type", "text/plain")
                        .targetUri(url)
                        .relatedRequest(relatedRequestContext)
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void optionalComponents() throws Exception {
        // setup
        var requiredComponents = SignatureComponents.builder()
                .header("required")
                .build();
        var usedIfPresentComponents = SignatureComponents.builder()
                .headers("optional-one", "optional-two")
                .dictionaryMember("optional-dict", "three")
                .dictionaryMember("optional-dict", "four")
                .queryParam("name")
                .queryParam("age")
                .method()
                .targetUri()
                .authority()
                .scheme()
                .requestTarget()
                .path()
                .query()
                .status()
                .relatedRequestHeader("related-one")
                .relatedRequestHeader("related-two")
                .build();
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(requiredComponents)
                .usedIfPresentComponents(usedIfPresentComponents)
                .context(SignatureContext.builder()
                        .header("required", "present")
                        .header("optional-one", "111")
                        .header("optional-dict", "one=11,four=44")
                        .targetUri("https://api.vimsa.com/api?age=88")
                        .relatedRequest(SignatureContext.builder()
                                .header("related-one", "ONE")
                                .build())
                        .build())
                .build();
        var expectedSignatureBase = "\"required\": present\n" +
                "\"optional-one\": 111\n" +
                "\"optional-dict\";key=\"four\": 44\n" +
                "\"@query-param\";name=\"age\": 88\n" +
                "\"@target-uri\": https://api.vimsa.com/api?age=88\n" +
                "\"@authority\": api.vimsa.com\n" +
                "\"@scheme\": https\n" +
                "\"@request-target\": /api?age=88\n" +
                "\"@path\": /api\n" +
                "\"@query\": ?age=88\n" +
                "\"related-one\";req: ONE\n" +
                "\"@signature-params\": (\"required\" \"optional-one\" \"optional-dict\";key=\"four\" \"@query-param\";name=\"age\" \"@target-uri\" " +
                "\"@authority\" \"@scheme\" \"@request-target\" \"@path\" \"@query\" \"related-one\";req)";
        var expectedSignatureInput = "test=(\"required\" \"optional-one\" \"optional-dict\";key=\"four\" \"@query-param\";name=\"age\" \"@target-uri\" " +
                "\"@authority\" \"@scheme\" \"@request-target\" \"@path\" \"@query\" \"related-one\";req)";
        var expectedSignature = "test=:sPkc0h1Rm9JLtZmcPi99N0svX637MDCC3TzwGHnXoByrwZmkBw6bsHfK6BWz41q1Ldok88q6RCPntI64j8i2AA==:";
        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .header("required", "present")
                        .header("optional-one", "111")
                        .header("optional-dict", "one=11,four=44")
                        .targetUri("https://api.vimsa.com/api?age=88")
                        .relatedRequest(SignatureContext.builder()
                                .header("related-one", "ONE")
                                .build())
                        .build())
                .requiredComponents(requiredComponents)
                .requiredIfPresentComponents(usedIfPresentComponents)
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void binaryWrappedHeader() throws Exception {
        // setup
        var url = "https://example.com";
        var relatedRequestContext = SignatureContext.builder()
                .header("Request-Header", "first")
                .header("Request-Header", "")
                .header("Request-Header", "last")
                .build();
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .binaryWrappedHeader("Example-Header")
                        .binaryWrappedHeader("Example-Header-2")
                        .relatedRequestBinaryWrappedHeader("Request-Header")
                        .build())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                .context(SignatureContext.builder()
                        .targetUri(url)
                        .header("Example-Header", "value, with, lots")
                        .header("Example-Header", "of, commas")
                        .header("Example-Header-2", "value, with, lots, of, commas")
                        .relatedRequest(relatedRequestContext)
                        .build())
                .build();

        var expectedSignatureBase = "\"example-header\";bs: :dmFsdWUsIHdpdGgsIGxvdHM=:, :b2YsIGNvbW1hcw==:\n" +
                "\"example-header-2\";bs: :dmFsdWUsIHdpdGgsIGxvdHMsIG9mLCBjb21tYXM=:\n" +
                "\"request-header\";req;bs: :Zmlyc3Q=:, ::, :bGFzdA==:\n" +
                "\"@signature-params\": (\"example-header\";bs \"example-header-2\";bs \"request-header\";req;bs)";
        var expectedSignatureInput = "test=(\"example-header\";bs \"example-header-2\";bs \"request-header\";req;bs)";
        var expectedSignature = "test=:0xPltZydMtTl/8EyZCH0CAeRE7q/OZRjnZ4NY/VjmpmlP77zPIHXtmGVwJv89w5EXNquHXpg28mnR17wAtbWDw==:";

        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .headers(Map.of("Example-Header", List.of("value, with, lots", "of, commas")))
                        .header("Example-Header-2", "value, with, lots, of, commas")
                        .targetUri(url)
                        .relatedRequest(relatedRequestContext)
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();
    }

    @Test
    void trailers() throws Exception {
        // setup
        var url = "https://example.com";
        var relatedRequestContext = SignatureContext.builder()
                .trailer("Related-Trailer", "source")
                .trailer("Related-Structured-Trailer", "a=b,  c=(1   3)")
                .trailer("Related-Dictionary", "map=(a  b c), oth=4")
                .trailer("Related-Binary-Wrapped", "last")
                .build();
        var signatureSpec = ObjectMother.getSignatureSpecBuilder()
                .components(SignatureComponents.builder()
                        .trailer("Single-Trailer")
                        .trailers(List.of("Collection-Trailer-1", "Collection-Trailer-2"))
                        .trailers("Multi-Trailer-1", "Multi-Trailer-2")
                        .relatedRequestTrailer("Related-Trailer")
                        .structuredTrailer("Structured-Trailer")
                        .relatedRequestStructuredTrailer("Related-Structured-Trailer")
                        .trailerDictionaryMember("Dictionary-Member", "two")
                        .relatedRequestTrailerDictionaryMember("Related-Dictionary", "map")
                        .binaryWrappedTrailer("Binary-Wrapped")
                        .relatedRequestBinaryWrappedTrailer("Related-Binary-Wrapped")
                        .build())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build())
                .context(SignatureContext.builder()
                        .targetUri(url)
                        .trailer("Single-Trailer", "sing")
                        .trailer("Collection-Trailer-1", "col1")
                        .trailer("Collection-Trailer-1", "col1a")
                        .trailer("Collection-Trailer-2", "col2")
                        .trailer("Multi-Trailer-1", "MT 1")
                        .trailer("Multi-Trailer-2", "Multi 2")
                        .trailers(Map.of("Structured-Trailer", "str1,  str2, (a  b c)"))
                        .trailers(Map.of("Dictionary-Member", List.of("one=1", "two=dos")))
                        .trailers(Map.of("Binary-Wrapped", List.of("first", "second")))
                        .relatedRequest(relatedRequestContext)
                        .build())
                .build();

        var expectedSignatureBase = "\"single-trailer\";tr: sing\n" +
                "\"collection-trailer-1\";tr: col1, col1a\n" +
                "\"collection-trailer-2\";tr: col2\n" +
                "\"multi-trailer-1\";tr: MT 1\n" +
                "\"multi-trailer-2\";tr: Multi 2\n" +
                "\"related-trailer\";req;tr: source\n" +
                "\"structured-trailer\";sf;tr: str1, str2, (a b c)\n" +
                "\"related-structured-trailer\";req;sf;tr: a=b, c=(1 3)\n" +
                "\"dictionary-member\";key=\"two\";tr: dos\n" +
                "\"related-dictionary\";req;key=\"map\";tr: (a b c)\n" +
                "\"binary-wrapped\";bs;tr: :Zmlyc3Q=:, :c2Vjb25k:\n" +
                "\"related-binary-wrapped\";req;bs;tr: :bGFzdA==:\n" +
                "\"@signature-params\": (\"single-trailer\";tr \"collection-trailer-1\";tr \"collection-trailer-2\";tr \"multi-trailer-1\";tr " +
                "\"multi-trailer-2\";tr \"related-trailer\";req;tr \"structured-trailer\";sf;tr \"related-structured-trailer\";req;sf;tr " +
                "\"dictionary-member\";key=\"two\";tr \"related-dictionary\";req;key=\"map\";tr \"binary-wrapped\";bs;tr \"related-binary-wrapped\";req;bs;tr)";
        var expectedSignatureInput = "test=(\"single-trailer\";tr \"collection-trailer-1\";tr \"collection-trailer-2\";tr \"multi-trailer-1\";tr " +
                "\"multi-trailer-2\";tr \"related-trailer\";req;tr \"structured-trailer\";sf;tr \"related-structured-trailer\";req;sf;tr " +
                "\"dictionary-member\";key=\"two\";tr \"related-dictionary\";req;key=\"map\";tr \"binary-wrapped\";bs;tr \"related-binary-wrapped\";req;bs;tr)";
        var expectedSignature = "test=:64Kkxx+JueI8qNrBXC9/4Sv8vpe88TqGD7A/C7uJXunDTyUmYp6qlFeVCKNDxbiINq/oMJUR6v802NQfZZTPBw==:";

        var verificationSpec = ObjectMother.getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, expectedSignatureInput)
                        .header(SignatureHeaders.SIGNATURE, expectedSignature)
                        .targetUri(url)
                        .trailer("Single-Trailer", "sing")
                        .trailer("Collection-Trailer-1", "col1")
                        .trailer("Collection-Trailer-1", "col1a")
                        .trailer("Collection-Trailer-2", "col2")
                        .trailer("Multi-Trailer-1", "MT 1")
                        .trailer("Multi-Trailer-2", "Multi 2")
                        .trailers(Map.of("Structured-Trailer", "str1,  str2, (a  b c)"))
                        .trailers(Map.of("Dictionary-Member", List.of("one=1", "two=dos")))
                        .trailers(Map.of("Binary-Wrapped", List.of("first", "second")))
                        .relatedRequest(relatedRequestContext)
                        .build())
                .build();

        // execute
        var result = signatureSpec.sign();

        // verify signature
        assertThat(result.getSignatureBase()).isEqualTo(expectedSignatureBase);
        assertThat(result.getSignatureInput()).isEqualTo(expectedSignatureInput);
        assertThat(result.getSignature()).isEqualTo(expectedSignature);

        // signature verification
        assertThatCode(verificationSpec::verify).doesNotThrowAnyException();

    }
}
