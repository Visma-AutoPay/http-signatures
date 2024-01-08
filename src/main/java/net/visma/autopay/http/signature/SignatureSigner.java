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

import net.visma.autopay.http.structured.StructuredItem;
import net.visma.autopay.http.structured.StructuredDictionary;
import net.visma.autopay.http.structured.StructuredInnerList;
import net.visma.autopay.http.structured.StructuredInteger;
import net.visma.autopay.http.structured.StructuredString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A class to compute signatures given as {@link SignatureSpec} object
 */
final class SignatureSigner {
    /**
     * Given a {@link SignatureSpec} object computes the signature and returns values to be copied to <em>Signature-Input</em> and <em>Signature</em> HTTP
     * headers.
     *
     * @param signatureSpec Signature specification: parameters, components, context of HTTP request or response, private key, signature label
     * @return Signature result: values to be copied to <em>Signature-Input</em> and <em>Signature</em> headers, and signature base which could be used for
     *         logging or debugging
     * @throws SignatureException Problems with signature calculation, e.g. missing or malformatted values in Signature Context, problems with the private key.
     *                            For detailed reason call {@link SignatureException#getErrorCode()}.
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-19.html#name-creating-a-signature">Creating a Signature</a>
     */
    static SignatureResult sign(SignatureSpec signatureSpec) throws SignatureException {
        var components = extractUsedComponents(signatureSpec);
        var signatureInputList = getSignatureInput(components, signatureSpec.getParameters());
        var signatureBase = getSignatureBase(components, signatureSpec.getSignatureContext(), signatureInputList);

        var byteSignature = DataSigner.sign(signatureBase, signatureSpec.getPrivateKey(), signatureSpec.getParameters().getAlgorithm());
        var signatureInputDict = StructuredDictionary.of(signatureSpec.getSignatureLabel(), signatureInputList);
        var signatureDict = StructuredDictionary.of(signatureSpec.getSignatureLabel(), byteSignature);

        return new SignatureResult(signatureInputDict.serialize(), signatureDict.serialize(), signatureBase);
    }

    /**
     * Computes Signature Base, which then can be used for signature creation or verification.
     *
     * @param components       Components included in the signature. All of them are supposed to have related values in the Signature Context.
     *                         "Used/required if present" logic must be applied before calling this method.
     * @param signatureContext Signature Context
     * @param signatureInput   Value of <em>Signature-Input</em> header provided as Structured Inner List
     * @return Signature base to be used for signature creation or verification
     * @throws SignatureException In case of problems with extracting values from the Signature Context, e.g. missing or malformatted HTTP header
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-19.html#create-sig-input">Creating the Signature Base</a>
     */
    static String getSignatureBase(List<Component> components, SignatureContext signatureContext, StructuredInnerList signatureInput)
            throws SignatureException {
        var baseBuilder = new StringBuilder();

        for (var component : components) {
            baseBuilder.append(component.getName().serialize())
                    .append(": ")
                    .append(component.extractValue(signatureContext))
                    .append("\n");
        }

        baseBuilder.append('"')
                .append(DerivedComponentType.SIGNATURE_PARAMS.getIdentifier())
                .append("\": ")
                .append(signatureInput.serialize());

        return baseBuilder.toString();
    }

    private static List<Component> extractUsedComponents(SignatureSpec signatureSpec) {
        var optionalComponents = signatureSpec.getUsedIfPresentComponents().getComponents().stream()
                .filter(component -> component.isValuePresent(signatureSpec.getSignatureContext()))
                .collect(Collectors.toList());

        var allComponents = signatureSpec.getComponents().getComponents();

        if (!optionalComponents.isEmpty()) {
            allComponents = new ArrayList<>(allComponents);
            allComponents.addAll(optionalComponents);
        }

        return allComponents;
    }

    private static StructuredInnerList getSignatureInput(List<Component> components, SignatureParameters parameters) {
        var items = components.stream().map(Component::getName).collect(Collectors.toList());
        var parameterMap = new LinkedHashMap<String, StructuredItem>();

        for (var entry : parameters.getParameters().entrySet()) {
            var parameter = entry.getKey();
            var value = entry.getValue();

            if (parameter != SignatureParameterType.ALGORITHM || parameters.isAlgorithmVisible()) {
                parameterMap.put(parameter.getIdentifier(), parameterToStructuredItem(parameter, value));
            }
        }

        return StructuredInnerList.withParams(items, parameterMap);
    }

    private static StructuredItem parameterToStructuredItem(SignatureParameterType parameter, Object value) {
        switch (parameter) {
            case CREATED:
            case EXPIRES:
                return StructuredInteger.of(((Instant) value).getEpochSecond());
            case NONCE:
            case KEY_ID:
            case TAG:
                return StructuredString.of(value.toString());
            case ALGORITHM:
                return StructuredString.of(((SignatureAlgorithm) value).getIdentifier());
            default:
                throw new IllegalArgumentException();
        }
    }

    private SignatureSigner() {
        throw new UnsupportedOperationException();
    }
}
