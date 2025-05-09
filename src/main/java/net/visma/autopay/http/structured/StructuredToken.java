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
package net.visma.autopay.http.structured;

import java.util.Map;
import java.util.Objects;

/**
 * Class representing Structured Tokens. Also used as "base" Tokens in Structured parameters.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-items">Items</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-tokens">Tokens</a>
 */
public final class StructuredToken extends StructuredItem {
    private final String value;

    private StructuredToken(String value, StructuredParameters parameters) {
        super(parameters);
        CharacterValidator.validateToken(value);
        this.value = value;
    }

    /**
     * Creates Structured Token of given value, without parameters
     *
     * @param value Item value
     * @return Created Structured Token
     * @throws IllegalArgumentException Invalid characters
=     */
    public static StructuredToken of(String value) {
        return new StructuredToken(value, StructuredParameters.EMPTY);
    }

    /**
     * Creates Structured String of given value and parameters
     *
     * @param value      Item value
     * @param parameters Parameter map. For details, check {@link StructuredParameters#of(Map)}.
     * @return Created Structured Token
     * @throws IllegalArgumentException Invalid characters
     */
    public static StructuredToken withParams(String value, Map<String, ?> parameters) {
        return new StructuredToken(value, StructuredParameters.of(parameters));
    }

    /**
     * Creates Structured String of given value and parameters
     *
     * @param value      Item value
     * @param parameters Structured Parameters
     * @return Created Structured Token
     * @throws IllegalArgumentException Invalid characters
     */
    public static StructuredToken withParams(String value, StructuredParameters parameters) {
        return new StructuredToken(value, parameters);
    }

    @Override
    StructuredToken withParams(StructuredParameters parameters) {
        return new StructuredToken(value, parameters);
    }


    /**
     * Returns {@link String} value of this Structured Token
     *
     * @return String value represented by this Structured Token
     */
    @Override
    protected String objectValue() {
        return value;
    }


    /**
     * Returns value serialized according to the specification, without parameters
     *
     * @return Serialized value
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-a-token">Serializing a Token</a>
     */
    @Override
    protected String serializeValue() {
        return value;
    }

    /**
     * Parses given string for Structured Token, according to the specification
     *
     * @param httpHeader String to parse, e.g. HTTP header
     * @return Parsed Structured Token
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-item">Parsing an Item</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-token">Parsing a Token</a>
     */
    public static StructuredToken parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseItem(httpHeader, StructuredToken.class);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var that = (StructuredToken) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
