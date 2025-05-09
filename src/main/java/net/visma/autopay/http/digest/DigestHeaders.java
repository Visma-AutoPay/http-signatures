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
package net.visma.autopay.http.digest;


/**
 * Constants with header names defined for Digest Fields
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9530.html#name-http-field-name-registratio">HTTP Field Name Registration</a>
 */
public final class DigestHeaders {
    /**
     * <em>Content-Digest</em> header
     */
    public static final String CONTENT_DIGEST = "Content-Digest";
    /**
     * <em>Repr-Digest</em> header
     */
    public static final String REPR_DIGEST = "Repr-Digest";
    /**
     * <em>Want-Content-Digest</em> header
     */
    public static final String WANT_CONTENT_DIGEST = "Want-Content-Digest";
    /**
     * <em>Want-Repr-Digest</em> header
     */
    public static final String WANT_REPR_DIGEST = "Want-Repr-Digest";

    private DigestHeaders() {
        throw new UnsupportedOperationException();
    }
}
