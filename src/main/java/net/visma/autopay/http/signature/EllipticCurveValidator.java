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

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.interfaces.ECKey;
import java.security.spec.ECFieldFp;
import java.util.Objects;


/**
 * Validates whether elliptic curve matches signature algorithm
 * <p>
 * Such verification is needed because "SHA###withECDSA" algorithms can be used with multiple curves, but the specification narrows usage
 * to P-256 curve with SHA-256 and P-384 curve with SHA-384.
 */
final class EllipticCurveValidator {
    private static final CurveParams P_256_PARAMS = new CurveParams(
            new BigInteger(new byte[]{0, -1, -1, -1, -1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}),
            new BigInteger(new byte[]{0, -1, -1, -1, -1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -4}),
            new BigInteger(new byte[]{90, -58, 53, -40, -86, 58, -109, -25, -77, -21, -67, 85, 118, -104, -122, -68, 101, 29, 6, -80, -52, 83, -80, -10, 59, -50, 60, 62, 39, -46, 96, 75}),
            new BigInteger(new byte[]{107, 23, -47, -14, -31, 44, 66, 71, -8, -68, -26, -27, 99, -92, 64, -14, 119, 3, 125, -127, 45, -21, 51, -96, -12, -95, 57, 69, -40, -104, -62, -106}),
            new BigInteger(new byte[]{79, -29, 66, -30, -2, 26, 127, -101, -114, -25, -21, 74, 124, 15, -98, 22, 43, -50, 51, 87, 107, 49, 94, -50, -53, -74, 64, 104, 55, -65, 81, -11}),
            new BigInteger(new byte[]{0, -1, -1, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -68, -26, -6, -83, -89, 23, -98, -124, -13, -71, -54, -62, -4, 99, 37, 81}),
            1
    );

    private static final CurveParams P_384_PARAMS = new CurveParams(
            new BigInteger(new byte[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1}),
            new BigInteger(new byte[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -4}),
            new BigInteger(new byte[]{0, -77, 49, 47, -89, -30, 62, -25, -28, -104, -114, 5, 107, -29, -8, 45, 25, 24, 29, -100, 110, -2, -127, 65, 18, 3, 20, 8, -113, 80, 19, -121, 90, -58, 86, 57, -115, -118, 46, -47, -99, 42, -123, -56, -19, -45, -20, 42, -17}),
            new BigInteger(new byte[]{0, -86, -121, -54, 34, -66, -117, 5, 55, -114, -79, -57, 30, -13, 32, -83, 116, 110, 29, 59, 98, -117, -89, -101, -104, 89, -9, 65, -32, -126, 84, 42, 56, 85, 2, -14, 93, -65, 85, 41, 108, 58, 84, 94, 56, 114, 118, 10, -73}),
            new BigInteger(new byte[]{54, 23, -34, 74, -106, 38, 44, 111, 93, -98, -104, -65, -110, -110, -36, 41, -8, -12, 29, -67, 40, -102, 20, 124, -23, -38, 49, 19, -75, -16, -72, -64, 10, 96, -79, -50, 29, 126, -127, -99, 122, 67, 29, 124, -112, -22, 14, 95}),
            new BigInteger(new byte[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -57, 99, 77, -127, -12, 55, 45, -33, 88, 26, 13, -78, 72, -80, -89, 122, -20, -20, 25, 106, -52, -59, 41, 115}),
            1
    );

    /**
     * Validates if given EC key can be used with given signature algorithm
     *
     * @param ecKey EC key to check
     * @param signatureAlgorithm Signature algorithm to be used with given key
     * @throws InvalidKeyException Curve in given key does not match given signature algorithm
     */
    static void validate(ECKey ecKey, SignatureAlgorithm signatureAlgorithm) throws InvalidKeyException {
        if (signatureAlgorithm.getKeyAlgorithm() != SignatureKeyAlgorithm.EC) {
            throw new InvalidKeyException("EC key cannot be used with " + signatureAlgorithm + " algorithm");
        }

        var curveParams = new CurveParams(ecKey);
        var reference = signatureAlgorithm == SignatureAlgorithm.ECDSA_P384_SHA_384 ? P_384_PARAMS : P_256_PARAMS;

        if (!reference.equals(curveParams)) {
            var requiredCurve = signatureAlgorithm == SignatureAlgorithm.ECDSA_P384_SHA_384 ? "P-384" : "P-256";
            throw new InvalidKeyException("Used EC key does not match the algorithm. " + requiredCurve + " is required.");
        }
    }

    private static class CurveParams {
        private final BigInteger p;
        private final BigInteger a;
        private final BigInteger b;
        private final BigInteger gx;
        private final BigInteger gy;
        private final BigInteger n;
        private final int h;

        public CurveParams(BigInteger p, BigInteger a, BigInteger b, BigInteger gx, BigInteger gy, BigInteger n, int h) {
            this.p = p;
            this.a = a;
            this.b = b;
            this.gx = gx;
            this.gy = gy;
            this.n = n;
            this.h = h;
        }

        public CurveParams(ECKey key) {
            var params = key.getParams();
            var curve = params.getCurve();
            var generator = params.getGenerator();

            this.p = ((ECFieldFp) curve.getField()).getP();
            this.a = curve.getA();
            this.b = curve.getB();
            this.gx = generator.getAffineX();
            this.gy = generator.getAffineY();
            this.n = params.getOrder();
            this.h = params.getCofactor();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CurveParams that = (CurveParams) o;
            return h == that.h && p.equals(that.p) && a.equals(that.a) && b.equals(that.b) && gx.equals(that.gx) && gy.equals(that.gy) && n.equals(that.n);
        }

        @Override
        public int hashCode() {
            return Objects.hash(p, a, b, gx, gy, n, h);
        }
    }

    private EllipticCurveValidator() {
        throw new UnsupportedOperationException();
    }
}
