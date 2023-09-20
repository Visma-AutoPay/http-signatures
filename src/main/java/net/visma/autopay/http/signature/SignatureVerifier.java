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

import net.visma.autopay.http.signature.SignatureException.ErrorCode;
import net.visma.autopay.http.structured.StructuredDictionary;
import net.visma.autopay.http.structured.StructuredInnerList;
import net.visma.autopay.http.structured.StructuredString;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * A class to verify signatures given as {@link VerificationSpec} object
 */
final class SignatureVerifier {
    private final VerificationSpec verificationSpec;
    private StructuredInnerList signatureInput;
    private String signatureLabel;
    private SignatureParameters signatureParameters;
    private SignatureContext signatureContext;

    /**
     * Given a {@link VerificationSpec} object verifies the signature and throws an exception when signature is incorrect or any other problem occurs.
     *
     * @param verificationSpec Verification specification: parameters, components, context of HTTP request or response, public key supplier, signature label
     * @throws SignatureException Incorrect signature or problems with verification, e.g. missing or malformatted values in Signature Context, problems with the
     *                            public key. For detailed reason call {@link SignatureException#getErrorCode()}.
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-19.html#name-verifying-a-signature">Verifying a Signature</a>
     */
    static void verify(VerificationSpec verificationSpec) throws SignatureException {
        new SignatureVerifier(verificationSpec).verify();
    }

    private SignatureVerifier(VerificationSpec verificationSpec) {
        this.verificationSpec = verificationSpec;
    }

    private void verify() throws SignatureException {
        signatureContext = verificationSpec.getSignatureContext();
        populateSignatureInput();
        populateSignatureParameters();
        verifyUniqueness();
        verifyRequired();
        verifyForbidden();
        verifyExpiration();
        var givenSignature = getSignature();
        var signatureComponents = getComponents();
        var signatureBase = SignatureSigner.getSignatureBase(signatureComponents, signatureContext, signatureInput);

        var publicKeyInfo = getPublicKeyInfo();
        var algorithm = getAlgorithm(publicKeyInfo);
        var publicKey = publicKeyInfo.getPublicKey(algorithm.getKeyAlgorithm());

        if (!DataVerifier.verify(signatureBase, givenSignature, publicKey, algorithm)) {
            throw new SignatureException(ErrorCode.INCORRECT_SIGNATURE, "Provided signature different from computed one.\nUsed algorithm: "
                    + algorithm.getIdentifier() + "\nSignature base:\n" + signatureBase);
        }
    }

    private SignatureAlgorithm getAlgorithm(PublicKeyInfo publicKeyInfo) throws SignatureException {
        var algorithmInParameters = signatureParameters.getAlgorithm();
        var publicKeyAlgorithm = publicKeyInfo.getAlgorithm();

        var algorithm = publicKeyAlgorithm != null ? publicKeyAlgorithm : algorithmInParameters;

        if (algorithm == null) {
            throw new SignatureException(ErrorCode.MISSING_ALGORITHM, "Signature algorithm not provided");
        }

        return algorithm;
    }

    private PublicKeyInfo getPublicKeyInfo() throws SignatureException {
        try {
            return verificationSpec.getPublicKeyGetter().apply(signatureParameters.getKeyId());
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureException(ErrorCode.INVALID_KEY, "Exception when fetching public key", e);
        }
    }

    private void populateSignatureInput() throws SignatureException {
        var header = signatureContext.getHeaders().get(SignatureHeaders.SIGNATURE_INPUT.toLowerCase());
        var requestedLabel = verificationSpec.getSignatureLabel();
        var requestedTag = verificationSpec.getApplicationTag();

        if (header == null) {
            throw new SignatureException(ErrorCode.MISSING_HEADER, "Missing Signature-Input header");
        }

        try {
            var inputDictionary = StructuredDictionary.parse(header);

            if (requestedLabel != null) {
                signatureInput = getSignatureInputByLabelAndTag(inputDictionary, requestedLabel, requestedTag);
                signatureLabel = requestedLabel;
            } else {
                var inputEntry = getSignatureInputByTag(inputDictionary, requestedTag);
                signatureInput = inputEntry.getValue();
                signatureLabel = inputEntry.getKey();
            }
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureException(ErrorCode.INVALID_STRUCTURED_HEADER, "Unable to parse Signature-Input header", e);
        }
    }

    private StructuredInnerList getSignatureInputByLabelAndTag(StructuredDictionary inputDictionary, String requestedLabel, String requestedTag)
            throws SignatureException {
        var optionalSignatureInput = inputDictionary.getItem(requestedLabel, StructuredInnerList.class);

        if (requestedTag != null && optionalSignatureInput.isPresent() && !isTagPresent(requestedTag, optionalSignatureInput.get())) {
            throw new SignatureException(ErrorCode.MISSING_TAG, "Missing " + requestedTag + " tag in Signature-Input");
        } else if (optionalSignatureInput.isEmpty()) {
            throw new SignatureException(ErrorCode.MISSING_DICTIONARY_KEY, "Missing " + requestedLabel + " label in Signature-Input");
        } else {
            return optionalSignatureInput.get();
        }
    }

    private Map.Entry<String, StructuredInnerList> getSignatureInputByTag(StructuredDictionary inputDictionary, String requestedTag) throws SignatureException {
        var inputEntries = inputDictionary.entrySet(StructuredInnerList.class).stream()
                .filter(entry -> isTagPresent(requestedTag, entry.getValue()))
                .limit(2)
                .collect(Collectors.toList());

        if (inputEntries.size() == 1) {
            return inputEntries.get(0);
        } else if (inputEntries.isEmpty()) {
            throw new SignatureException(ErrorCode.MISSING_TAG, "Missing " + requestedTag + " tag in Signature-Input");
        } else {
            throw new SignatureException(ErrorCode.DUPLICATE_TAG, "Multiple " + requestedTag + " tags in Signature-Input");
        }
    }

    private boolean isTagPresent(String requestedTag, StructuredInnerList signatureInput) {
        return signatureInput.stringParam(SignatureParameterType.TAG.getIdentifier())
                .filter(requestedTag::equals)
                .isPresent();
    }

    private byte[] getSignature() throws SignatureException {
        var header = signatureContext.getHeaders().get(SignatureHeaders.SIGNATURE.toLowerCase());
        StructuredDictionary dictionary;

        if (header == null) {
            throw new SignatureException(ErrorCode.MISSING_HEADER, "Missing Signature header");
        }

        try {
            dictionary = StructuredDictionary.parse(header);
        } catch (Exception e) {
            throw new SignatureException(ErrorCode.INVALID_STRUCTURED_HEADER, "Unable to parse Signature header", e);
        }

        var signature = dictionary.getBytes(signatureLabel);

        if (signature.isEmpty()) {
            throw new SignatureException(ErrorCode.MISSING_DICTIONARY_KEY, "Missing " + signatureLabel + " in Signature");
        }

        return signature.get();
    }

    private void populateSignatureParameters() {
        signatureParameters = SignatureParameters.builder()
                .created(signatureInput.longParam(SignatureParameterType.CREATED.getIdentifier())
                        .map(Instant::ofEpochSecond).orElse(null))
                .expires(signatureInput.longParam(SignatureParameterType.EXPIRES.getIdentifier())
                        .map(Instant::ofEpochSecond).orElse(null))
                .nonce(signatureInput.stringParam(SignatureParameterType.NONCE.getIdentifier()).orElse(null))
                .algorithm(signatureInput.stringParam(SignatureParameterType.ALGORITHM.getIdentifier())
                        .map(SignatureAlgorithm::fromIdentifier).orElse(null))
                .keyId(signatureInput.stringParam(SignatureParameterType.KEY_ID.getIdentifier()).orElse(null))
                .build();
    }

    private void verifyRequired() throws SignatureException {
        var actualParameters = signatureInput.parameters().keySet();

        for (var requiredParameter : verificationSpec.getRequiredParameters()) {
            if (!actualParameters.contains(requiredParameter.getIdentifier())) {
                throw new SignatureException(ErrorCode.MISSING_REQUIRED, "Missing required parameter " + requiredParameter.getIdentifier());
            }
        }

        var actualComponents = new HashSet<>(signatureInput.itemList(StructuredString.class));

        for (var requiredComponent : verificationSpec.getRequiredComponents().getComponents()) {
            if (!actualComponents.contains(requiredComponent.getName())) {
                throw new SignatureException(ErrorCode.MISSING_REQUIRED, "Missing required component " + requiredComponent);
            }
        }

        for (var requiredComponent : verificationSpec.getRequiredIfPresentComponents().getComponents()) {
            if (!actualComponents.contains(requiredComponent.getName()) && requiredComponent.isValuePresent(signatureContext)) {
                throw new SignatureException(ErrorCode.MISSING_REQUIRED, "Missing required optionally present component " + requiredComponent);
            }
        }
    }

    private void verifyForbidden() throws SignatureException {
        var actualParameters = signatureInput.parameters().keySet();

        for (var forbiddenParameter : verificationSpec.getForbiddenParameters()) {
            if (actualParameters.contains(forbiddenParameter.getIdentifier())) {
                throw new SignatureException(ErrorCode.FORBIDDEN_PRESENT, "Forbidden parameter " + forbiddenParameter.getIdentifier() + " present");
            }
        }
    }

    private void verifyExpiration() throws SignatureException {
        var created = signatureParameters.getCreated();
        var expires = signatureParameters.getExpires();
        var maximumAgeSeconds = verificationSpec.getMaximumAgeSeconds();
        var maximumSkewSeconds = verificationSpec.getMaximumSkewSeconds();
        var now = Instant.now();

        if (created != null) {
            if (expires != null && expires.isBefore(now)) {
                throw new SignatureException(ErrorCode.SIGNATURE_EXPIRED, "Expiration " + expires + " exceeded");
            }

            if (maximumAgeSeconds != null && created.plusSeconds(maximumAgeSeconds).isBefore(now)) {
                throw new SignatureException(ErrorCode.SIGNATURE_EXPIRED, "Maximum age " + maximumAgeSeconds + " seconds exceeded");
            }

            if (maximumSkewSeconds != null && created.isAfter(now.plusSeconds(maximumSkewSeconds))) {
                throw new SignatureException(ErrorCode.SIGNATURE_EXPIRED, "Created in the future. Maximum skew " + maximumSkewSeconds + " seconds exceeded");
            }
        }
    }

    private void verifyUniqueness() throws SignatureException {
        if (signatureInput.itemList().size() > new HashSet<>(signatureInput.itemList()).size()) {
            throw new SignatureException(ErrorCode.INVALID_STRUCTURED_HEADER, "Duplicate items in Signature-Input: " + signatureInput);
        }
    }

    private List<Component> getComponents() throws SignatureException {
        try {
            return signatureInput.itemList(StructuredString.class).stream()
                    .map(ComponentFactory::create)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new SignatureException(ErrorCode.INVALID_STRUCTURED_HEADER, "Unable to parse Signature-Input header", e);
        }
    }

}
