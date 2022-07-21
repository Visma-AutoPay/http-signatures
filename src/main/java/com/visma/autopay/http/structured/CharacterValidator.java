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
package com.visma.autopay.http.structured;

import java.util.Collection;

/**
 * Utility for checking string validity: for Structured Strings, Tokens and map keys
 */
final class CharacterValidator {

    /**
     * Checks if given character is a valid {@link StructuredString} character
     *
     * @param ch Character to check
     * @return True if valid Structured String character
     */
    static boolean isStringChar(int ch) {
        return ch >= ' ' && ch <= '~';
    }

    /**
     * Checks if given character is a valid first character of {@link StructuredDictionary} and {@link StructuredParameters} key
     *
     * @param ch Character to check
     * @return True if valid first key character
     */
    static boolean isFirstKeyChar(int ch) {
        return (ch >= 'a' && ch <= 'z') || ch == '*';
    }

    /**
     * Checks if given character is a valid character, second or subsequent, of {@link StructuredDictionary} and {@link StructuredParameters} key
     *
     * @param ch Character to check
     * @return True if valid second or subsequent key character
     */
    static boolean isKeyChar(int ch) {
        return (ch >= 'a' && ch <='z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-' || ch == '.' || ch == '*';
    }

    /**
     * Checks if given character is a valid first character of {@link StructuredToken}
     *
     * @param ch Character to check
     * @return True if valid first Structured Token character
     */
    static boolean isFirstTokenChar(int ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '*';
    }

    /**
     * Checks if given character is a valid character, second or subsequent, of {@link StructuredToken}
     *
     * @param ch Character to check
     * @return True if valid second or subsequent Structured Token character
     */
    static boolean isTokenChar(int ch) {
        return ch >= '!' && ch <= '~' && "\"(),;<=>?@[\\]{}".indexOf(ch) == -1;
    }

    /**
     * Checks if provided string is a valid value for {@link StructuredString}. Throws an exception if not.
     *
     * @param string String value to check
     * @throws IllegalArgumentException When invalid value provided
     */
    static void validateString(String string) {
        if (!string.chars().allMatch(CharacterValidator::isStringChar)) {
            throw new IllegalArgumentException("Illegal String characters: " + string);
        }
    }

    /**
     * Checks if provided string is a valid value for {@link StructuredToken}. Throws an exception if not.
     *
     * @param token Token value to check
     * @throws IllegalArgumentException When invalid value provided
     */
    static void validateToken(String token) {
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Empty token value");
        } else if (!CharacterValidator.isFirstTokenChar(token.charAt(0))) {
            throw new IllegalArgumentException("Illegal first Token character: " + token);
        } else if (!token.chars().allMatch(CharacterValidator::isTokenChar)) {
            throw new IllegalArgumentException("Illegal Token characters: " + token);
        }
    }

    /**
     * Checks if provided strings ar valid values for {@link StructuredDictionary} or {@link StructuredParameters} key. Throws an exception if not.
     *
     * @param keys Key values to check
     * @throws IllegalArgumentException When invalid value provided
     */
    static void validateKeys(Collection<String> keys) {
        for (var key : keys) {
            if (key.isEmpty()) {
                throw new IllegalArgumentException("Empty key");
            } else if (!CharacterValidator.isFirstKeyChar(key.charAt(0))) {
                throw new IllegalArgumentException("Illegal first key character: " + key);
            } else if (!key.chars().allMatch(CharacterValidator::isKeyChar)) {
                throw new IllegalArgumentException("Illegal key characters: " + key);
            }
        }
    }

    private CharacterValidator() {
        throw new UnsupportedOperationException();
    }
}
