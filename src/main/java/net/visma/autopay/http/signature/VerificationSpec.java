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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Signature verification specification - all data needed to verify a signature.
 * <ul>
 *     <li>Required signature parameters</li>
 *     <li>Forbidden signature parameters</li>
 *     <li>Required signature components</li>
 *     <li>Signature context containing values for defined components. Copied from verified request or response.</li>
 *     <li>Public key supplier</li>
 *     <li>Signature label</li>
 *     <li>Application-specific tag</li>
 *     <li>Limits for signature creation time</li>
 * </ul>
 *
 * @see SignatureParameterType
 * @see SignatureComponents
 * @see SignatureContext
 * @see PublicKeyInfo
 */
public class VerificationSpec {
    private final Set<SignatureParameterType> requiredParameters;
    private final Set<SignatureParameterType> forbiddenParameters;
    private final SignatureComponents requiredComponents;
    private final SignatureComponents requiredIfPresentComponents;
    private final SignatureContext signatureContext;
    private final Integer maximumAgeSeconds;
    private final Integer maximumSkewSeconds;
    private final CheckedFunction<String, PublicKeyInfo> publicKeyGetter;
    private final String signatureLabel;
    private final String applicationTag;


    private VerificationSpec(Set<SignatureParameterType> requiredParameters, Set<SignatureParameterType> forbiddenParameters,
                             SignatureComponents requiredComponents, SignatureComponents requiredIfPresentComponents, SignatureContext signatureContext,
                             Integer maximumAgeSeconds, Integer maximumSkewSeconds, CheckedFunction<String, PublicKeyInfo> publicKeyGetter,
                             String signatureLabel, String applicationTag) {
        this.requiredParameters = requiredParameters;
        this.forbiddenParameters = forbiddenParameters;
        this.requiredComponents = requiredComponents;
        this.requiredIfPresentComponents = requiredIfPresentComponents;
        this.signatureContext = signatureContext;
        this.maximumAgeSeconds = maximumAgeSeconds;
        this.maximumSkewSeconds = maximumSkewSeconds;
        this.publicKeyGetter = publicKeyGetter;
        this.signatureLabel = signatureLabel;
        this.applicationTag = applicationTag;
    }

    /**
     * Verifies signature according to this Verification Spec and throws an exception when signature is incorrect or any other problem occurs.
     *
     * @throws SignatureException Incorrect signature or problems with verification, e.g. missing or malformatted values in Signature Context, problems with the
     *                            public key. For detailed reason call {@link SignatureException#getErrorCode()}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html#name-verifying-a-signature">Verifying a Signature</a>
     */
    public void verify() throws SignatureException {
        SignatureVerifier.verify(this);
    }

    /**
     * Returns required Signature Parameters
     *
     * @return Required Signature Parameters
     */
    Set<SignatureParameterType> getRequiredParameters() {
        return requiredParameters;
    }

    /**
     * Returns forbidden Signature Parameters
     *
     * @return Forbidden Signature Parameters
     */
    Set<SignatureParameterType> getForbiddenParameters() {
        return forbiddenParameters;
    }

    /**
     * Returns definitions of required Signature Components
     *
     * @return Required Signature Components
     */
    SignatureComponents getRequiredComponents() {
        return requiredComponents;
    }

    /**
     * Returns definitions of Signature Components which are required if related values are present in the Signature Context, e.g. optional HTTP headers which
     * must be included in the signature if they are present.
     *
     * @return Signature Components required if present in the Signature Context
     */
    SignatureComponents getRequiredIfPresentComponents() {
        return requiredIfPresentComponents;
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
     * Returns maximum age of verified signature in seconds
     * <p>
     * Age is based on <em>created</em> Signature Parameter
     *
     * @return Maximum age of verified signature
     */
    Integer getMaximumAgeSeconds() {
        return maximumAgeSeconds;
    }

    /**
     * Returns maximum skew of <em>created</em> Signed Parameter towards future
     * <p>
     * A signature will be rejected if {@code created > now() + getMaximumSkewSeconds()}
     *
     * @return Maximum "future" skew of verified signature
     */
    Integer getMaximumSkewSeconds() {
        return maximumSkewSeconds;
    }

    /**
     * Returns public key supplier
     *
     * @return Function which for given <em>keyid</em> returns related public key
     */
    CheckedFunction<String, PublicKeyInfo> getPublicKeyGetter() {
        return publicKeyGetter;
    }

    /**
     * Returns Signature label
     *
     * @return Label of signature to verify
     */
    String getSignatureLabel() {
        return signatureLabel;
    }

    /**
     * Returns application-specific tag
     *
     * @return Tag of signature to verify
     */
    String getApplicationTag() {
        return applicationTag;
    }

    /**
     * Returns a builder used to construct {@link VerificationSpec} object
     *
     * @return A VerificationSpec builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class to build {@link VerificationSpec} objects.
     * <p>
     * Signature Context must be provided, and it should contain <em>Signature-Input</em> and <em>Signature</em> headers.
     * Public ket getter key must be provided.
     * Signature label must be provided.
     */
    public static class Builder {
        private final Set<SignatureParameterType> requiredParameters;
        private final Set<SignatureParameterType> forbiddenParameters;
        private SignatureComponents requiredComponents;
        private SignatureComponents requiredIfPresentComponents;
        private SignatureContext signatureContext;
        private Integer maximumAgeSeconds;
        private Integer maximumSkewSeconds;
        private CheckedFunction<String, PublicKeyInfo> publicKeyGetter;
        private String signatureLabel;
        private String applicationTag;


        private Builder() {
            requiredParameters = EnumSet.noneOf(SignatureParameterType.class);
            forbiddenParameters = EnumSet.noneOf(SignatureParameterType.class);
        }

        /**
         * Sets Signature Parameters which must be present in verified signature.
         * <p>
         * If any of them is not present, in <em>Signature-Input</em>, then verified signature is rejected.
         *
         * @param parameters Required Signature Parameter types, provided as vararg
         * @return This builder
         */
        public Builder requiredParameters(SignatureParameterType... parameters) {
            Collections.addAll(requiredParameters, parameters);
            return this;
        }

        /**
         * Sets Signature Parameters which must be present in verified signature.
         * <p>
         * If any of them is not present, in <em>Signature-Input</em>, then verified signature is rejected.
         *
         * @param parameters Required Signature Parameter types, provided as collection
         * @return This builder
         */
        public Builder requiredParameters(Collection<SignatureParameterType> parameters) {
            requiredParameters.addAll(parameters);
            return this;
        }

        /**
         * Sets Signature Parameters which must not be present in verified signature, e.g. <em>alg</em>
         * <p>
         * If any of them is present, in <em>Signature-Input</em>, then verified signature is rejected.
         *
         * @param parameters Forbidden Signature Parameter types, provided as varargs
         * @return This builder
         */
        public Builder forbiddenParameters(SignatureParameterType... parameters) {
            Collections.addAll(forbiddenParameters, parameters);
            return this;
        }

        /**
         * Sets Signature Parameters which must not be present in verified signature, e.g. <em>alg</em>
         * <p>
         * If any of them is present, in <em>Signature-Input</em>, then verified signature is rejected.
         *
         * @param parameters Forbidden Signature Parameter types, provided as collection
         * @return This builder
         */
        public Builder forbiddenParameters(Collection<SignatureParameterType> parameters) {
            forbiddenParameters.addAll(parameters);
            return this;
        }

        /**
         * Sets definitions of required Signature Components
         * <p>
         * If related values are not present in verified <em>Signature-Input</em>, the signature is rejected.
         *
         * @param components Required Signature Components
         * @return This builder
         */
        public Builder requiredComponents(SignatureComponents components) {
            this.requiredComponents = components;
            return this;
        }

        /**
         * Sets definitions of Signature Components which are required in the signature only if their values are defined in Signature Context.
         * <p>
         * They can be optional HTTP headers which must be included in the signature if they are present. If such headers are present in the Signature Context
         * but are missing in <em>Signature-Input</em>, the signature is rejected.
         *
         * @param components Signature Components required if present in the Signature Context
         * @return This builder
         */
        public Builder requiredIfPresentComponents(SignatureComponents components) {
            this.requiredIfPresentComponents = components;
            return this;
        }

        /**
         * Sets Signature Context
         *
         * @param signatureContext Signature Context with values obtained from verified request or response
         * @return This builder
         */
        public Builder context(SignatureContext signatureContext) {
            this.signatureContext = signatureContext;
            return this;
        }

        /**
         * Sets maximum age of verified signature in seconds
         * <p>
         * Age is based on <em>created</em> Signature Parameter. Signature is rejected if {@code created < now() - maximumAgeSeconds}.
         * Such verification is performed only if <em>created</em> Signature Parameter is present.
         *
         * @param maximumAgeSeconds Maximum age of verified signature in seconds
         * @return This builder
         */
        public Builder maximumAge(int maximumAgeSeconds) {
            this.maximumAgeSeconds = maximumAgeSeconds;
            return this;
        }

        /**
         * Set s maximum "skew" for <em>created</em> Signature Property (in seconds) - for detecting signatures from the "future".
         * <p>
         * A signature will be rejected if it's from the "future" - {@code created > now() + maximumSkewSeconds}
         *
         * @param maximumSkewSeconds Maximum "future" skew of verified signature n seconds
         * @return This builder
         */
        public Builder maximumSkew(int maximumSkewSeconds) {
            this.maximumSkewSeconds = maximumSkewSeconds;
            return this;
        }

        /**
         * Sets public key supplier function
         * <p>
         * The supplier should return {@link PublicKeyInfo} object for given key ID, or throw an exception in case of problems, e.g. unknown key ID.
         * Key ID is extracted from <em>Signature-Input</em> header.
         *
         * @param publicKeyGetter Function which for given <em>keyid</em> returns related public key
         * @return This builder
         * @see PublicKeyInfo
         */
        public Builder publicKeyGetter(CheckedFunction<String, PublicKeyInfo> publicKeyGetter) {
            this.publicKeyGetter = publicKeyGetter;
            return this;
        }

        /**
         * Sets label of signature to verify
         * <p>
         * <em>Signature</em> and <em>Signature-Input</em> headers will be searched for provided label. If they don't contain the label, signature verification
         * will be rejected.
         * <p>
         * If application-specific {{@link #applicationTag(String)}} is also provided then found signature must contain both the label and the tag.
         * If <em>signatureLabel</em> is not provided then only {@link #applicationTag(String)} is used to find matching signature.
         * Either <em>signatureLabel</em> or <em>applicationTag</em> must be provided in verification specs.
         *
         * @param signatureLabel Label of signature to verify
         * @return This builder
         */
        public Builder signatureLabel(String signatureLabel) {
            this.signatureLabel = signatureLabel;
            return this;
        }

        /**
         * Sets application-specific tag of signature to verify
         * <p>
         * <em>Signature-Input</em> header will be searched for provided tag. If it doesn't contain the tag, signature verification
         * will be rejected. If multiple signatures contain the tag, signature verification will be rejected.
         * <p>
         * If {{@link #signatureLabel(String)} is also provided then found signature must contain both the label and the tag.
         * (In this case multiple signatures having the tag won't lead to rejection.)
         * If <em>tag</em> is not provided then only {@link #signatureLabel(String)} is used to find matching signature.
         * Either <em>signatureLabel</em> or <em>applicationTag</em> must be provided in verification specs.
         *
         * @param tag Application-specific tag of signature to verify
         * @return This builder
         */
        public Builder applicationTag(String tag) {
            this.applicationTag = tag;
            return this;
        }

        /**
         * Constructs {@link VerificationSpec} object from this builder
         * <p>
         * All required data mentioned in {@link Builder} must be provided (signature context, public key getter, signature label).
         *
         * @return VerificationSpec object
         */
        public VerificationSpec build() {
            Objects.requireNonNull(signatureContext, "SignatureContext not provided");
            Objects.requireNonNull(publicKeyGetter, "PublicKeyGetter not provided");

            if (signatureLabel == null && applicationTag == null) {
                throw new NullPointerException("Both signatureLabel and applicationTag not provided. One of them is required.");
            }

            if (requiredComponents == null) {
                requiredComponents = SignatureComponents.builder().build();
            }

            if (requiredIfPresentComponents == null) {
                requiredIfPresentComponents = SignatureComponents.builder().build();
            }

            return new VerificationSpec(requiredParameters, forbiddenParameters, requiredComponents, requiredIfPresentComponents, signatureContext,
                    maximumAgeSeconds, maximumSkewSeconds, publicKeyGetter, signatureLabel, applicationTag);
        }
    }

    /**
     * String representation of this object. It does not contain public key getter.
     *
     * @return String representation of this VerificationSpec
     */
    @Override
    public String toString() {
        return "VerificationSpec{" +
                "requiredParameters=" + requiredParameters +
                ", forbiddenParameters=" + forbiddenParameters +
                ", requiredComponents=" + requiredComponents +
                ", signatureContext=" + signatureContext +
                ", maximumAgeSeconds=" + maximumAgeSeconds +
                ", maximumSkewSeconds=" + maximumSkewSeconds +
                ", signatureLabel='" + signatureLabel + '\'' +
                '}';
    }

    /**
     * Compares the specified object with this SignatureSpec for equality. Returns true if the given object is of the same class as this VerificationSpec,
     * and all but {@code publicKeyGetter} object fields are equal.
     *
     * @param o Object to be compared with this VerificationSpec
     * @return True is specified object is equal to this VerificationSpec
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (VerificationSpec) o;
        return requiredParameters.equals(that.requiredParameters) && forbiddenParameters.equals(that.forbiddenParameters)
                && requiredComponents.equals(that.requiredComponents) && signatureContext.equals(that.signatureContext)
                && Objects.equals(maximumAgeSeconds, that.maximumAgeSeconds) && Objects.equals(maximumSkewSeconds, that.maximumSkewSeconds)
                && signatureLabel.equals(that.signatureLabel);
    }

    /**
     * Returns hash code for this VerificationSpec, which is composed of hash codes of all but {@code publicKeyGetter} object fields
     *
     * @return The hash code for this VerificationSpec
     */
    @Override
    public int hashCode() {
        return Objects.hash(requiredParameters, forbiddenParameters, requiredComponents, signatureContext, maximumAgeSeconds, maximumSkewSeconds,
                signatureLabel);
    }
}
