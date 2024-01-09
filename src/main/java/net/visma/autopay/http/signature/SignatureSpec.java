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

import java.security.PrivateKey;
import java.util.Objects;

/**
 * Signature specification - all data needed to create a signature.
 * <ul>
 *     <li>Signature parameters</li>
 *     <li>Signature component definitions</li>
 *     <li>Signature context containing values for defined components. Copied from signed request or response.</li>
 *     <li>Private key</li>
 *     <li>Signature label</li>
 * </ul>
 *
 * @see SignatureParameters
 * @see SignatureComponents
 * @see SignatureContext
 */
public class SignatureSpec {
    private final SignatureParameters parameters;
    private final SignatureComponents components;
    private final SignatureComponents usedIfPresentComponents;
    private final SignatureContext signatureContext;
    private final PrivateKey privateKey;
    private final String signatureLabel;

    private SignatureSpec(SignatureParameters parameters, SignatureComponents components, SignatureComponents usedIfPresentComponents,
                          SignatureContext signatureContext, PrivateKey privateKey, String signatureLabel) {
        this.parameters = parameters;
        this.components = components;
        this.usedIfPresentComponents = usedIfPresentComponents;
        this.signatureContext = signatureContext;
        this.privateKey = privateKey;
        this.signatureLabel = signatureLabel;
    }

    /**
     * Computes the signature according to this Signature Specs and returns values to be copied to <em>Signature-Input</em> and <em>Signature</em> HTTP
     * headers.
     *
     * @return Signature result: values to be copied to <em>Signature-Input</em> and <em>Signature</em> headers, and signature base which could be used for
     *         logging or debugging
     * @throws SignatureException Problems with signature calculation, e.g. missing or malformatted values in Signature Context, problems with the private key.
     *                            For detailed reason call {@link SignatureException#getErrorCode()}.
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-19.html#name-creating-a-signature">Creating a Signature</a>
     */
    public SignatureResult sign() throws SignatureException {
        return SignatureSigner.sign(this);
    }

    /**
     * Returns Signature Parameters
     *
     * @return Signature Parameters
     */
    SignatureParameters getParameters() {
        return parameters;
    }

    /**
     * Returns Signature Component definitions for required components
     *
     * @return Required Signature Components
     */
    SignatureComponents getComponents() {
        return components;
    }

    /**
     * Returns Signature Component definitions for components used only if they are present in the Signature Context
     *
     * @return Signature Components used if present in the Signature Context
     */
    SignatureComponents getUsedIfPresentComponents() {
        return usedIfPresentComponents;
    }

    /**
     * Returns Signature Context - values for defined Signature Components
     *
     * @return Signature Context
     */
    SignatureContext getSignatureContext() {
        return signatureContext;
    }

    /**
     * Returns private key used to sign
     *
     * @return Private key
     */
    PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns signature label
     *
     * @return Signature label
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-19.html#name-signature-labels">Signature Labels</a>
     */
    String getSignatureLabel() {
        return signatureLabel;
    }


    /**
     * Returns a builder used to construct {@link SignatureSpec} object
     *
     * @return A SignatureSpec builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class to build {@link SignatureSpec} objects.
     * <p>
     * Private key must be provided as either {@link PrivateKey} object, or PKCS#8-encoded byte[] or PKCS#8-Base64-encoded String.
     * Signature parameters must be provided, and they must contain the signature algorithm.
     * Signature label must be provided.
     */
    public static class Builder {
        private SignatureParameters parameters;
        private SignatureComponents components;
        private SignatureComponents usedIfPresentComponents;
        private SignatureContext signatureContext;
        private PrivateKey privateKey;
        private String stringPrivateKey;
        private byte[] bytePrivateKey;
        private String signatureLabel;

        private Builder() {
        }

        /**
         * Sets Signature Parameters
         *
         * @param parameters Signature Parameters used to create signature
         * @return This builder
         */
        public Builder parameters(SignatureParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        /**
         * Sets required Signature Components
         * <p>
         * If related values are not provided in the Signature Context, e.g. missing HTTP header value, an exception will be thrown when computing
         * the signature.
         *
         * @param components Required Signature Components
         * @return This builder
         */
        public Builder components(SignatureComponents components) {
            this.components = components;
            return this;
        }

        /**
         * Sets Signature Components included in the signature only if related values are present in the Signature Context.
         * <p>
         * <em>usedIfPresentComponents</em> are added to <em>Signature-Input</em> after <em>required components</em>
         *
         * @param components Signature Components used uif present in the Signature Context
         * @return This builder
         */
        public Builder usedIfPresentComponents(SignatureComponents components) {
            this.usedIfPresentComponents = components;
            return this;
        }

        /**
         * Sets Signature Context
         *
         * @param signatureContext Signature Context with values obtained from signed request or response
         * @return This builder
         */
        public Builder context(SignatureContext signatureContext) {
            this.signatureContext = signatureContext;
            return this;
        }

        /**
         * Sets private key used to create the signature
         *
         * @param privateKey PrivateKey object
         * @return This builder
         */
        public Builder privateKey(PrivateKey privateKey) {
            this.privateKey = privateKey;
            this.bytePrivateKey = null;
            this.stringPrivateKey = null;
            return this;
        }

        /**
         * Sets private key, encoded as bytes[] in PKCS#8 format
         *
         * @param privateKey PKCS#8-encoded private key
         * @return This builder
         */
        public Builder privateKey(byte[] privateKey) {
            this.bytePrivateKey = privateKey;
            this.privateKey = null;
            this.stringPrivateKey = null;
            return this;
        }

        /**
         * Sets private key, provided as PKCS#8-base-64-encoded String
         * <p>
         * It can contain multiple lines, including <em>BEGIN PRIVATE KEY</em> and <em>END PRIVATE KEY</em>.
         *
         * @param privateKey PKCS#8-base-64-encoded private key
         * @return This builder
         */
        public Builder privateKey(String privateKey) {
            this.stringPrivateKey = privateKey;
            this.privateKey = null;
            this.bytePrivateKey = null;
            return this;
        }

        /**
         * Sets signature label
         *
         * @param signatureLabel Signature label
         * @return This builder
         * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-19.html#name-signature-labels">Signature Labels</a>
         */
        public Builder signatureLabel(String signatureLabel) {
            this.signatureLabel = signatureLabel;
            return this;
        }

        /**
         * Constructs {@link SignatureSpec} object from this builder
         * <p>
         * All required data mentioned in {@link Builder} must be provided (private key, signature algorithm, signature label).
         *
         * @return SignatureSpec object
         */
        public SignatureSpec build() {
            Objects.requireNonNull(parameters, "Parameters not provided");
            Objects.requireNonNull(parameters.getAlgorithm(), "Algorithm not provided");
            Objects.requireNonNull(signatureLabel, "SignatureLabel not provided");

            if (components == null) {
                components = SignatureComponents.builder().build();
            }

            if (usedIfPresentComponents == null) {
                usedIfPresentComponents = SignatureComponents.builder().build();
            }

            if (signatureContext == null) {
                signatureContext = SignatureContext.builder().build();
            }

            return new SignatureSpec(parameters, components, usedIfPresentComponents, signatureContext, getPrivateKey(), signatureLabel);
        }

        private PrivateKey getPrivateKey() {
            try {
                var keyAlgorithm = parameters.getAlgorithm().getKeyAlgorithm();

                if (privateKey != null) {
                    return privateKey;
                } else if (bytePrivateKey != null) {
                    return SignatureKeyFactory.decodePrivateKey(bytePrivateKey, keyAlgorithm);
                } else if (stringPrivateKey != null) {
                    return SignatureKeyFactory.decodePrivateKey(stringPrivateKey, keyAlgorithm);
                } else {
                    throw new NullPointerException("Private key not provided");
                }
            } catch (SignatureException e) {
                throw new IllegalArgumentException("Invalid private key", e);
            }
        }
    }

    /**
     * String representation of this object. It does not contain private key data.
     *
     * @return String representation of this SignatureSpec
     */
    @Override
    public String toString() {
        return "SignatureSpec{" +
                "parameters=" + parameters +
                ", components=" + components +
                ", signatureContext=" + signatureContext +
                ", privateKey=" + privateKey.getClass() +
                ", signatureLabel='" + signatureLabel + '\'' +
                '}';
    }

    /**
     * Compares the specified object with this SignatureSpec for equality. Returns true if the given object is of the same class as this SignatureSpec,
     * and all object fields are equal.
     *
     * @param o Object to be compared with this SignatureSpec
     * @return True is specified object is equal to this SignatureSpec
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (SignatureSpec) o;
        return parameters.equals(that.parameters) && components.equals(that.components) && signatureContext.equals(that.signatureContext)
                && privateKey.equals(that.privateKey) && signatureLabel.equals(that.signatureLabel);
    }

    /**
     * Returns hash code for this SignatureSpec, which is composed of hash codes of all object fields
     *
     * @return The hash code for this SignatureSpec
     */
    @Override
    public int hashCode() {
        return Objects.hash(parameters, components, signatureContext, privateKey, signatureLabel);
    }
}
