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
package net.visma.autopay.http.structured;

import java.util.Objects;

/**
 * Thrown in case of problems when parsing
 */
public class StructuredException extends Exception {
    private static final long serialVersionUID = 3636071083221609710L;

    /**
     * Error code of this exception
     */
    private final ErrorCode errorCode;

    /**
     * Constructs new exception with provided error code and message
     *
     * @param errorCode Error code
     * @param message Detail message
     */
    public StructuredException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    /**
     * Returns error code
     *
     * @return Error code of this exception
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Error codes to classify Structured Exceptions
     */
    public enum ErrorCode {
        /**
         * Attempt to parse null or empty input
         */
        EMPTY_INPUT,

        /**
         * Unexpected character encountered
         */
        UNEXPECTED_CHARACTER,

        /**
         * Missing closing quote or colon or parenthesis
         */
        MISSING_CHARACTER,

        /**
         * Expected Item type is different from actual. E.g. {@link StructuredBytes#parse(String)} called with "ok" argument.
         */
        WRONG_ITEM_CLASS,

        /**
         * Wrong format for numerical values, e.g. too many digits
         */
        WRONG_NUMBER,

        /**
         * Invalid Base64 string provided when parsing {@link StructuredBytes}
         */
        INVALID_BYTES,
    }
}
