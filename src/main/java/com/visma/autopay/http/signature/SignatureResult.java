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
package com.visma.autopay.http.signature;

import java.util.Objects;

/**
 * A result of signature calculation
 *
 * @see SignatureSpec#sign()
 */
public final class SignatureResult {
    private final String signatureInput;
    private final String signature;
    private final String signatureBase;

    /**
     * Creates Signature Result object
     *
     * @param signatureInput Signature Input to be copied to <em>Signature-Input</em> header
     * @param signature      Signature to be copied to <em>Signature</em> header
     * @param signatureBase  Used signature base which can be used for logging or debugging
     */
    public SignatureResult(String signatureInput, String signature, String signatureBase) {
        this.signatureInput = signatureInput;
        this.signature = signature;
        this.signatureBase = signatureBase;
    }

    /**
     * Returns Signature Input to be copied to <em>Signature-Input</em> header
     *
     * @return Signature Input
     */
    public String getSignatureInput() {
        return signatureInput;
    }

    /**
     * Returns Signature to be copied to <em>Signature</em> header
     *
     * @return Signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Returns signature base which can be used for logging or debugging
     *
     * @return Signature Base
     * @see <a href="https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html#name-creating-the-signature-base">
     *      Creating the Signature Base</a>
     */
    public String getSignatureBase() {
        return signatureBase;
    }

    /**
     * Compares the specified object with this SignatureResult for equality. Returns true if the given object is of the same class as this SignatureResult,
     * and all object fields are equal.
     *
     * @param obj Object to be compared with this SignatureResult
     * @return True is specified object is equal to this SignatureResult
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SignatureResult) obj;
        return Objects.equals(this.signatureInput, that.signatureInput) &&
                Objects.equals(this.signature, that.signature) &&
                Objects.equals(this.signatureBase, that.signatureBase);
    }

    /**
     * Returns hash code for this SignatureResult, which is composed of hash codes of all object fields
     *
     * @return The hash code for this SignatureResult
     */
    @Override
    public int hashCode() {
        return Objects.hash(signatureInput, signature, signatureBase);
    }

    /**
     * String representation of this object
     *
     * @return String representation of this SignatureResult
     */
    @Override
    public String toString() {
        return "SignatureResult[" +
                "signatureInput=" + signatureInput + ", " +
                "signature=" + signature + ", " +
                "signatureBase=" + signatureBase + ']';
    }

}
