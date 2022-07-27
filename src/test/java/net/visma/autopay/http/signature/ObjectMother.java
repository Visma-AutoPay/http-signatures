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


final class ObjectMother {
    static final String SIGNATURE_LABEL = "test";

    static String getRsaKeyId() {
        return "test-key-rsa";
    }

    static String getRsaPublicKey() {
        return "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhAKYdtoeoy8zcAcR874L\n" +
                "8cnZxKzAGwd7v36APp7Pv6Q2jdsPBRrwWEBnez6d0UDKDwGbc6nxfEXAy5mbhgaj\n" +
                "zrw3MOEt8uA5txSKobBpKDeBLOsdJKFqMGmXCQvEG7YemcxDTRPxAleIAgYYRjTS\n" +
                "d/QBwVW9OwNFhekro3RtlinV0a75jfZgkne/YiktSvLG34lw2zqXBDTC5NHROUqG\n" +
                "TlML4PlNZS5Ri2U4aCNx2rUPRcKIlE0PuKxI4T+HIaFpv8+rdV6eUgOrB2xeI1dS\n" +
                "FFn/nnv5OoZJEIB+VmuKn3DCUcCZSFlQPSXSfBDiUGhwOw76WuSSsf1D4b/vLoJ1\n" +
                "0wIDAQAB\n" +
                "-----END PUBLIC KEY-----\n";
    }

    static String getRsaPrivateKey() {
        return "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEwgIBADANBgkqhkiG9w0BAQEFAASCBKwwggSoAgEAAoIBAQCEAph22h6jLzNw\n" +
                "BxHzvgvxydnErMAbB3u/foA+ns+/pDaN2w8FGvBYQGd7Pp3RQMoPAZtzqfF8RcDL\n" +
                "mZuGBqPOvDcw4S3y4Dm3FIqhsGkoN4Es6x0koWowaZcJC8Qbth6ZzENNE/ECV4gC\n" +
                "BhhGNNJ39AHBVb07A0WF6SujdG2WKdXRrvmN9mCSd79iKS1K8sbfiXDbOpcENMLk\n" +
                "0dE5SoZOUwvg+U1lLlGLZThoI3HatQ9FwoiUTQ+4rEjhP4choWm/z6t1Xp5SA6sH\n" +
                "bF4jV1IUWf+ee/k6hkkQgH5Wa4qfcMJRwJlIWVA9JdJ8EOJQaHA7Dvpa5JKx/UPh\n" +
                "v+8ugnXTAgMBAAECggEAb8lm5JZ2hUduLnq+OAKCSODeWQ7Uqs7eet2bqeuAD0/2\n" +
                "po+PG4qhZoo7VwFCUTWlJan9wqdxiAPlbEQKkCdFRcbakbjN2TMJjMCHWL5zfgvq\n" +
                "hmgeyKsrqg1wSce97J1/Mkvn3fh6CbqnwNb6bVFDvTJS3i5FzRhKiv6rUsYm8ZAd\n" +
                "F4XRaYkFkeuHPl7rc+ruUTSAjC4GovxIxoDJFe0r4kbFmkiZOr40e8RZYK7T1IKr\n" +
                "Svzfxx5AjnlK/OZOTCq0L7wBPbMW+IxmQpFCjpI+yuoi3FlZG3LaLNrBMXQF/lLZ\n" +
                "UDHs77q3fAGxDWwum2hKBfdBuUQtjlqwjQlgXPsskQKBiQCyp5QmapcTcs/y1igi\n" +
                "MwgAqJOb2jqmw+VzwKssj0IfRRu5oDYkI4xwI2rxLJhtOqCdaUH1l9wCb9wWkDy1\n" +
                "hyL2bm9grwc3FCv7wVLdCjw31Enx3RTkKzAPMxh9GCEB9QbCaVaPmGnWlDMC6HBs\n" +
                "5cW3EodWww+HPUgG0X1jyO+CqBgctubKK7WbAnkAvSlgXQbvHzWmuUBFRHAejRh/\n" +
                "naQTDV3GnH4lcRHuFBFZCSLn82xQS2/7xFOqfabqq17kNcvKfzdvWpGxxJ2cILAq\n" +
                "0pZS6DmrZlvBU4IkK2ZHCac/XfWVZFh+PrsH/EnVkDpfcYR/iw1F40C1q5w8R6WB\n" +
                "Haew3SApAoGIaiodZsrWpi8HFfZfeRs8OS/0L5x6WBl3Y9btoZgsIeruc9uZ8NXT\n" +
                "IdxaM6FdnyNEyOYA1VH94tDYR+xEt1br1ud/dkPslLV/Aac7d7EaYc7cdkb7oC9t\n" +
                "6sphVg0dqE0UTDlOwBxBYMtGmQbJsFzGpmjzVgKqWqJ3B947li2U7t63HXEvKprY\n" +
                "2wJ4b0DzpSMb5p42dcQgOTU8Mr4S6JOEhRr/YjErMkpaXUEqvZ3jEB9HRmcRi5Gt\n" +
                "t4NBiBMiY6V9br8a5gjEpiAQoIUcWokBMAYjEeurU8M6JLBd3YaZVVjISaFmdtyn\n" +
                "wLFoQxCh6/EC1rSywwrfDpSwO29S9i8XbaapAoGIPkbARLOwU/LcZrQy9mmfcPoQ\n" +
                "lAuCyeu1Q9nH7PYSnbHTFzmiud4Hl8bIXU9a0/58blDoOl3PctF+b4rAEJYUpCOD\n" +
                "u5PFyN6uEFYRg+YQwpjBMkXk8Eb39128ctARB40Lx8caDhRdTyaEedIG3cQDXSpA\n" +
                "l9EOzXkzfx4bZxjAHU9mkMdJwOcMDQ==\n" +
                "-----END PRIVATE KEY-----\n";
    }

    static String getRsaPssKeyId() {
        return "test-key-rsa-pss";
    }

    static String getRsaPssPublicKey() {
        return "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr4tmm3r20Wd/PbqvP1s2\n" +
                "+QEtvpuRaV8Yq40gjUR8y2Rjxa6dpG2GXHbPfvMs8ct+Lh1GH45x28Rw3Ry53mm+\n" +
                "oAXjyQ86OnDkZ5N8lYbggD4O3w6M6pAvLkhk95AndTrifbIFPNU8PPMO7OyrFAHq\n" +
                "gDsznjPFmTOtCEcN2Z1FpWgchwuYLPL+Wokqltd11nqqzi+bJ9cvSKADYdUAAN5W\n" +
                "Utzdpiy6LbTgSxP7ociU4Tn0g5I6aDZJ7A8Lzo0KSyZYoA485mqcO0GVAdVw9lq4\n" +
                "aOT9v6d+nb4bnNkQVklLQ3fVAvJm+xdDOp9LCNCN48V2pnDOkFV6+U9nV5oyc6XI\n" +
                "2wIDAQAB\n" +
                "-----END PUBLIC KEY-----\n";
    }

    static String getRsaPssPrivateKey() {
        return "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvgIBADALBgkqhkiG9w0BAQoEggSqMIIEpgIBAAKCAQEAr4tmm3r20Wd/Pbqv\n" +
                "P1s2+QEtvpuRaV8Yq40gjUR8y2Rjxa6dpG2GXHbPfvMs8ct+Lh1GH45x28Rw3Ry5\n" +
                "3mm+oAXjyQ86OnDkZ5N8lYbggD4O3w6M6pAvLkhk95AndTrifbIFPNU8PPMO7Oyr\n" +
                "FAHqgDsznjPFmTOtCEcN2Z1FpWgchwuYLPL+Wokqltd11nqqzi+bJ9cvSKADYdUA\n" +
                "AN5WUtzdpiy6LbTgSxP7ociU4Tn0g5I6aDZJ7A8Lzo0KSyZYoA485mqcO0GVAdVw\n" +
                "9lq4aOT9v6d+nb4bnNkQVklLQ3fVAvJm+xdDOp9LCNCN48V2pnDOkFV6+U9nV5oy\n" +
                "c6XI2wIDAQABAoIBAQCUB8ip+kJiiZVKF8AqfB/aUP0jTAqOQewK1kKJ/iQCXBCq\n" +
                "pbo360gvdt05H5VZ/RDVkEgO2k73VSsbulqezKs8RFs2tEmU+JgTI9MeQJPWcP6X\n" +
                "aKy6LIYs0E2cWgp8GADgoBs8llBq0UhX0KffglIeek3n7Z6Gt4YFge2TAcW2WbN4\n" +
                "XfK7lupFyo6HHyWRiYHMMARQXLJeOSdTn5aMBP0PO4bQyk5ORxTUSeOciPJUFktQ\n" +
                "HkvGbym7KryEfwH8Tks0L7WhzyP60PL3xS9FNOJi9m+zztwYIXGDQuKM2GDsITeD\n" +
                "2mI2oHoPMyAD0wdI7BwSVW18p1h+jgfc4dlexKYRAoGBAOVfuiEiOchGghV5vn5N\n" +
                "RDNscAFnpHj1QgMr6/UG05RTgmcLfVsI1I4bSkbrIuVKviGGf7atlkROALOG/xRx\n" +
                "DLadgBEeNyHL5lz6ihQaFJLVQ0u3U4SB67J0YtVO3R6lXcIjBDHuY8SjYJ7Ci6Z6\n" +
                "vuDcoaEujnlrtUhaMxvSfcUJAoGBAMPsCHXte1uWNAqYad2WdLjPDlKtQJK1diCm\n" +
                "rqmB2g8QE99hDOHItjDBEdpyFBKOIP+NpVtM2KLhRajjcL9Ph8jrID6XUqikQuVi\n" +
                "4J9FV2m42jXMuioTT13idAILanYg8D3idvy/3isDVkON0X3UAVKrgMEne0hJpkPL\n" +
                "FYqgetvDAoGBAKLQ6JZMbSe0pPIJkSamQhsehgL5Rs51iX4m1z7+sYFAJfhvN3Q/\n" +
                "OGIHDRp6HjMUcxHpHw7U+S1TETxePwKLnLKj6hw8jnX2/nZRgWHzgVcY+sPsReRx\n" +
                "NJVf+Cfh6yOtznfX00p+JWOXdSY8glSSHJwRAMog+hFGW1AYdt7w80XBAoGBAImR\n" +
                "NUugqapgaEA8TrFxkJmngXYaAqpA0iYRA7kv3S4QavPBUGtFJHBNULzitydkNtVZ\n" +
                "3w6hgce0h9YThTo/nKc+OZDZbgfN9s7cQ75x0PQCAO4fx2P91Q+mDzDUVTeG30mE\n" +
                "t2m3S0dGe47JiJxifV9P3wNBNrZGSIF3mrORBVNDAoGBAI0QKn2Iv7Sgo4T/XjND\n" +
                "dl2kZTXqGAk8dOhpUiw/HdM3OGWbhHj2NdCzBliOmPyQtAr770GITWvbAI+IRYyF\n" +
                "S7Fnk6ZVVVHsxjtaHy1uJGFlaZzKR4AGNaUTOJMs6NadzCmGPAxNQQOCqoUjn4XR\n" +
                "rOjr9w349JooGXhOxbu8nOxX\n" +
                "-----END PRIVATE KEY-----\n";
    }

    static String getEcKeyId() {
        return "test-key-ecc-p256";
    }

    static String getEcPublicKey() {
        return "-----BEGIN PUBLIC KEY-----\n" +
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEqIVYZVLCrPZHGHjP17CTW0/+D9Lf\n" +
                "w0EkjqF7xB4FivAxzic30tMM4GF+hR6Dxh71Z50VGGdldkkDXZCnTNnoXQ==\n" +
                "-----END PUBLIC KEY-----\n";
    }

    static String getEcPrivateKey() {
        return "-----BEGIN PRIVATE KEY-----\n" +
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgUpuF81l+kOxbjf7T\n" +
                "4mNSv0r5tN67Gim7rnf6EFpcYDuhRANCAASohVhlUsKs9kcYeM/XsJNbT/4P0t/D\n" +
                "QSSOoXvEHgWK8DHOJzfS0wzgYX6FHoPGHvVnnRUYZ2V2SQNdkKdM2ehd\n" +
                "-----END PRIVATE KEY-----\n";
    }

    static String getEdKeyId() {
        return "test-key-ed25519";
    }

    static String getEdPublicKey() {
        return "-----BEGIN PUBLIC KEY-----\n" +
                "MCowBQYDK2VwAyEAJrQLj5P/89iXES9+vFgrIy29clF9CC/oPPsw3c5D0bs=\n" +
                "-----END PUBLIC KEY-----\n";
    }

    static String getEdPublicKeyStripped() {
        return "MCowBQYDK2VwAyEAJrQLj5P/89iXES9+vFgrIy29clF9CC/oPPsw3c5D0bs=";
    }

    static String getEdPrivateKey() {
        return "-----BEGIN PRIVATE KEY-----\n" +
                "MC4CAQAwBQYDK2VwBCIEIJ+DYvh6SEqVTm50DFtMDoQikTmiCqirVv9mWG9qfSnF\n" +
                "-----END PRIVATE KEY-----\n";
    }

    static String getEdPrivateKeyStripped() {
        return "MC4CAQAwBQYDK2VwBCIEIJ+DYvh6SEqVTm50DFtMDoQikTmiCqirVv9mWG9qfSnF";
    }

    static String getHmacKeyId() {
        return "test-shared-secret";
    }

    static String getHmacKey() {
        return "uzvJfB4u3N0Jy4T7NZ75MDVcr8zSTInedJtkgcu46YW4XByzNJjxBdtjUkdJPBtbmHhIDi6pcl8jsasjlTMtDQ==";
    }

    static CheckedFunction<String, PublicKeyInfo> getPublicKeyGetter() {
        return keyId -> PublicKeyInfo.builder()
                .publicKey(ObjectMother.getEdPublicKey())
                .algorithm(SignatureAlgorithm.ED_25519)
                .build();
    }

    static SignatureSpec.Builder getSignatureSpecBuilder() {
        return SignatureSpec.builder()
                .signatureLabel(SIGNATURE_LABEL)
                .privateKey(ObjectMother.getEdPrivateKey())
                .parameters(SignatureParameters.builder().algorithm(SignatureAlgorithm.ED_25519).build());
    }

    static VerificationSpec.Builder getVerificationSpecBuilder() {
        return VerificationSpec.builder()
                .signatureLabel(SIGNATURE_LABEL)
                .publicKeyGetter(getPublicKeyGetter());
    }

    static VerificationSpec.Builder getVerificationSpecBuilder(String signatureInput, String signature) {
        return getVerificationSpecBuilder()
                .context(SignatureContext.builder()
                        .header(SignatureHeaders.SIGNATURE_INPUT, signatureInput)
                        .header(SignatureHeaders.SIGNATURE, signature)
                        .build());
    }

    private ObjectMother() {
        throw new UnsupportedOperationException();
    }
}
