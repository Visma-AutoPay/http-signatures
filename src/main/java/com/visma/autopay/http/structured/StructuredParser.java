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

import com.visma.autopay.http.structured.StructuredException.ErrorCode;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Parser converting strings (HTTP header values) to Structured Fields
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-structured-fields">Parsing Structured Fields</a>
 */
final class StructuredParser {
    private final char[] input;
    private int pos;
    private static final char EOF = (char) -1;

    private StructuredParser(String input) throws StructuredException {
        if (input == null) {
            throw new StructuredException(ErrorCode.EMPTY_INPUT, "Null input for the parser");
        }

        this.input = (input + EOF).toCharArray();
        trim();
    }

    /**
     * Parses given string for Structured Dictionary, according to the specification
     *
     * @param input String to parse, e.g. HTTP header
     * @return Parsed Structured Dictionary
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-dictionary">Parsing a Dictionary</a>
     */
    static StructuredDictionary parseDictionary(String input) throws StructuredException {
        return new StructuredParser(input).parseDictionary();
    }

    /**
     * Parses given HTTP header values for Structured Dictionary, according to the specification
     *
     * @param inputLines HTTP header values, for common header name, provided in order of occurrence in HTTP message
     * @return Parsed Structured Dictionary
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-dictionary">Parsing a Dictionary</a>
     */
    static StructuredDictionary parseDictionary(Collection<String> inputLines) throws StructuredException {
        return parseDictionary(String.join(",", inputLines));
    }

    /**
     * Parses given string for Structured List, according to the specification
     *
     * @param input String to parse, e.g. HTTP header
     * @return Parsed Structured List
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-list">Parsing a List</a>
     */
    static StructuredList parseList(String input) throws StructuredException {
        return new StructuredParser(input).parseList();
    }

    /**
     * Parses given HTTP header values for Structured List, according to the specification
     *
     * @param inputLines HTTP header values, for common header name, provided in order of occurrence in HTTP message
     * @return Parsed Structured List
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-list">Parsing a List</a>
     */
    static StructuredList parseList(Collection<String> inputLines) throws StructuredException {
        return parseList(String.join(",", inputLines));
    }

    /**
     * Parses given string for Structured Item, according to the specification
     *
     * @param input         String to parse, e.g. HTTP header
     * @param expectedClass Expected item class
     * @param <T>           Expected item type
     * @return Parsed Structured Byte Sequence
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-item">Parsing an Item</a>
     */
    static <T extends StructuredItem> T parseItem(String input, Class<T> expectedClass) throws StructuredException {
        var item = parseItem(input);

        if (expectedClass == item.getClass()) {
            //noinspection unchecked
            return (T) item;
        } else {
            throw new StructuredException(ErrorCode.WRONG_ITEM_CLASS, "Impossible to parse " + expectedClass.getSimpleName() + ". "
                    + item.getClass().getSimpleName() + " detected.");
        }
    }

    /**
     * Parses given string for Structured Item, according to the specification
     * <p>
     * Class of returned value depends on parsed content, and it can be any of {@link StructuredItem}'s subclasses, excluding {@link StructuredInnerList}.
     *
     * @param input String to parse, e.g. an HTTP header
     * @return Parsed Structured Item: a subclass of {@link StructuredItem}
     * @throws StructuredException Thrown in case of malformatted string
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-item">Parsing an Item</a>
     */
    static StructuredItem parseItem(String input) throws StructuredException {
        var parser = new StructuredParser(input);
        var item = parser.parseItem();
        parser.validateTail();

        return item;
    }

    /**
     * Parses given string for Structured Field, according to the specification
     * <p>
     * Class of returned value depends on parsed content, and it can be any class implementing {@link StructuredField} interface.
     * For details, see {@link StructuredField#parse(String)}
     *
     * @param input String to parse, e.g. an HTTP header
     * @return Parsed Structured Field
     * @throws StructuredException Thrown in case of malformatted string
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-structured-fields">Parsing Structured Fields</a>
     */
    static StructuredField parseAny(String input) throws StructuredException {
        return new StructuredParser(input).parseAny();
    }

    private StructuredField parseAny() throws StructuredException {
        var insideString = false;
        var isCollection = isEmptyBeforeProcessing();
        StructuredField result;

        for (char c = current(); c != EOF; c = next()) {
            if (c == '"') {
                insideString = !insideString;
            } else if (insideString && c == '\\') {
                next();
            } else if (!insideString && c == ',') {
                isCollection = true;
                break;
            }
        }

        rewind();

        if (isCollection) {
            try {
                result = parseList();
            } catch (StructuredException e) {
                rewind();
                result = parseDictionary();
            }
        } else {
            result = parseItem();
        }

        return result;
    }

    private String parseKey() throws StructuredException {
        var sb = new StringBuilder();
        char c = current();

        if (!CharacterValidator.isFirstKeyChar(c)) {
            throw new StructuredException(ErrorCode.UNEXPECTED_CHARACTER, "Unexpected character when parsing key: " + c);
        }

        sb.append(c);

        while ((c = next()) != EOF) {
            if (CharacterValidator.isKeyChar(c)) {
                sb.append(c);
            } else {
                break;
            }
        }

        return sb.toString();
    }

    private StructuredToken parseToken() {
        var sb = new StringBuilder();
        char c;
        sb.append(current());

        while ((c = next()) != EOF) {
            if (!CharacterValidator.isTokenChar(c)) {
                break;
            }
            sb.append(c);
        }

        return StructuredToken.of(sb.toString());
    }

    private StructuredString parseString() throws StructuredException {
        var sb = new StringBuilder();
        char c;

        while ((c = next()) != EOF) {
            if (c == '"') {
                next();
                break;
            } else if (c == '\\') {
                c = next();
                if (c == '"' || c == '\\') {
                    sb.append(c);
                } else {
                    throw new StructuredException(ErrorCode.UNEXPECTED_CHARACTER, "Unexpected escaped character: " + c);
                }
            } else if (CharacterValidator.isStringChar(c)) {
                sb.append(c);
            } else {
                throw new StructuredException(ErrorCode.UNEXPECTED_CHARACTER, "Unexpected character when parsing string: " + c);
            }
        }

        if (c != '"') {
            throw new StructuredException(ErrorCode.MISSING_CHARACTER, "Missing closing double quote");
        }

        return StructuredString.of(sb.toString());
    }

    private StructuredBoolean parseBoolean() throws StructuredException {
        char c = next();
        next();

        if (c != '0' && c != '1') {
            throw new StructuredException(ErrorCode.UNEXPECTED_CHARACTER, "Unexpected character when parsing boolean: " + c);
        }

        return StructuredBoolean.of(c != '0');
    }

    private StructuredItem parseNumber() throws StructuredException {
        var sb = new StringBuilder();
        char c;
        var isDecimal = false;
        var digitIndex = current() == '-' ? 1 : 0;
        sb.append(current());

        while ((c = next()) != EOF) {
            if (c >= '0' && c <= '9') {
                sb.append(c);
            } else if (c == '.') {
                sb.append(c);
                isDecimal = true;
            } else {
                break;
            }
        }

        var value = sb.toString();

        if (isDecimal) {
            var dotIndex = value.indexOf('.');

            if (value.length() - dotIndex > 4) {
                throw new StructuredException(ErrorCode.WRONG_NUMBER, "Too long fractional part");
            } else if (dotIndex - digitIndex > 12) {
                throw new StructuredException(ErrorCode.WRONG_NUMBER, "Too long decimal part");
            }
        } else if (value.length() - digitIndex > 15) {
            throw new StructuredException(ErrorCode.WRONG_NUMBER, "Too long integer");
        }

        try {
            if (isDecimal) {
                return StructuredDecimal.of(value);
            } else {
                return StructuredInteger.of(Long.parseLong(value));
            }
        } catch (NumberFormatException e) {
            throw new StructuredException(ErrorCode.WRONG_NUMBER, "Numeric value out of range");
        }
    }

    private StructuredBytes parseBytes() throws StructuredException {
        var sb = new StringBuilder();
        char c;

        while ((c = next()) != EOF) {
            if (c == ':') {
                next();
                break;
            } else {
                sb.append(c);
            }
        }

        if (c != ':') {
            throw new StructuredException(ErrorCode.MISSING_CHARACTER, "Missing closing colon");
        }

        try {
            return StructuredBytes.of(Base64.getDecoder().decode(sb.toString()));
        } catch (IllegalArgumentException e) {
            throw new StructuredException(ErrorCode.INVALID_BYTES, "Invalid Base64 string");
        }
    }

    private StructuredItem parseBareItem() throws StructuredException {
        char c = current();

        if ((c >= '0' && c <= '9') || c == '-') {
            return parseNumber();
        } else if (c == '"') {
            return parseString();
        } else if (c == ':') {
            return parseBytes();
        } else if (c == '?') {
            return parseBoolean();
        } else if (CharacterValidator.isFirstTokenChar(c)) {
            return parseToken();
        } else {
            throw new StructuredException(ErrorCode.UNEXPECTED_CHARACTER, "Cannot recognize item type. Unexpected character " + c);
        }
    }

    private StructuredParameters parseParameters() throws StructuredException {
        return current() == ';' ? parseConfirmedParameters() : StructuredParameters.EMPTY;
    }

    private StructuredParameters parseConfirmedParameters() throws StructuredException {
        var parameterMap = new LinkedHashMap<String, StructuredItem>();

        while (current() == ';') {
            next();
            skipSpaces();

            var key = parseKey();
            if (current() == '=') {
                next();
                parameterMap.put(key, parseBareItem());
            } else {
                parameterMap.put(key, StructuredBoolean.of(true));
            }
        }

        return StructuredParameters.of(parameterMap);
    }

    private StructuredItem parseItem() throws StructuredException {
        if (isEmptyBeforeProcessing()) {
            throw new StructuredException(ErrorCode.EMPTY_INPUT, "Empty or blank input");
        }

        var bareItem = parseBareItem();
        var parameters = parseParameters();

        if (!parameters.isEmpty()) {
            bareItem = bareItem.withParams(parameters);
        }

        return bareItem;
    }

    private StructuredInnerList parseInnerList() throws StructuredException {
        var items = new ArrayList<StructuredItem>();
        next();

        while (current() != EOF) {
            skipSpaces();

            if (current() == ')') {
                break;
            }

            items.add(parseItem());

            if (current() != ')' && current() != ' ') {
                throw new StructuredException(ErrorCode.MISSING_CHARACTER, "Missing space or closing parenthesis");
            }
        }

        if (current() == ')') {
            next();
        } else {
            throw new StructuredException(ErrorCode.MISSING_CHARACTER, "Missing closing parenthesis");
        }

        return StructuredInnerList.withParams(items, parseParameters());
    }

    private StructuredList parseList() throws StructuredException {
        var items = new ArrayList<StructuredItem>();

        while (current() != EOF) {
            if (current() == '(') {
                items.add(parseInnerList());
            } else {
                items.add(parseItem());
            }

            skipWhitespaces();

            if (current() == ',') {
                next();
                skipWhitespaces();

                if (current() == EOF) {
                    throw new StructuredException(ErrorCode.UNEXPECTED_CHARACTER, "Trailing comma");
                }
            } else {
                break;
            }
        }

        validateTail();
        return StructuredList.of(items);
    }

    private StructuredDictionary parseDictionary() throws StructuredException {
        var items = new LinkedHashMap<String, StructuredItem>();

        while (current() != EOF) {
            var key = parseKey();
            StructuredItem item;

            if (current() == '=') {
                char c = next();

                if (c == '(') {
                    item = parseInnerList();
                } else {
                    item = parseItem();
                }
            } else {
                item = StructuredBoolean.withParams(true, parseParameters());
            }

            items.put(key, item);
            skipWhitespaces();

            if (current() == ',') {
                next();
                skipWhitespaces();

                if (current() == EOF) {
                    throw new StructuredException(ErrorCode.UNEXPECTED_CHARACTER, "Trailing comma");
                }
            } else {
                break;
            }
        }

        validateTail();
        return StructuredDictionary.of(items);
    }

    private void validateTail() throws StructuredException {
        if (current() != EOF) {
            throw new StructuredException(ErrorCode.UNEXPECTED_CHARACTER, "Unexpected character at the end: " + current());
        }
    }

    private void skipWhitespaces() {
        while (pos < input.length && (input[pos] == ' ' || input[pos] == '\t')) {
            pos++;
        }
    }

    private void skipSpaces() {
        while (pos < input.length && input[pos] == ' ') {
            pos++;
        }
    }

    private char current() {
        return input[pos];
    }

    private char next() {
        pos = Math.min(pos + 1, input.length - 1);
        return input[pos];
    }

    private void rewind() {
        pos = 0;
    }

    private boolean isEmptyBeforeProcessing() {
        return input[pos] == EOF;
    }

    private void trim() {
        for (pos = 0; input[pos] == ' '; ) {
            pos++;
        }

        for (int i = input.length - 2; i >= 0 && this.input[i] == ' '; i--) {
            this.input[i] = EOF;
        }
    }
}
