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
 * Class representing Structured Booleans. Also used as "bare" Boolean in Structured parameters.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-items">Items</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-booleans">Booleans</a>
 */
public final class StructuredBoolean extends StructuredItem {
    private static final StructuredBoolean TRUE = new StructuredBoolean(true, StructuredParameters.EMPTY);
    private static final StructuredBoolean FALSE = new StructuredBoolean(false, StructuredParameters.EMPTY);

    private final boolean value;

    private StructuredBoolean(boolean value, StructuredParameters parameters) {
        super(parameters);
        this.value = value;
    }

    /**
     * Creates Structured Boolean of given value, without parameters
     *
     * @param value Item value
     * @return Created Structured Boolean
     */
    public static StructuredBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Creates Structured Boolean of given value and parameters
     *
     * @param value Item value
     * @param parameters Parameter map. For details, check {@link StructuredParameters#of(Map)}.
     * @return Created Structured Boolean
     */
    public static StructuredBoolean withParams(boolean value, Map<String, ?> parameters) {
        return new StructuredBoolean(value, StructuredParameters.of(parameters));
    }

    /**
     * Creates Structured Boolean of given value and parameters
     *
     * @param value Item value
     * @param parameters Structured Parameters
     * @return Created Structured Boolean
     */
    public static StructuredBoolean withParams(boolean value, StructuredParameters parameters) {
        return new StructuredBoolean(value, parameters);
    }

    @Override
    StructuredBoolean withParams(StructuredParameters parameters) {
        return new StructuredBoolean(value, parameters);
    }

    /**
     * Returns boolean value of this Structured Boolean
     *
     * @return Boolean value represented by this Structured Boolean
     */
    @Override
    public boolean boolValue() {
        return value;
    }

    /**
     * Returns boolean value of this Structured Boolean
     *
     * @return Boolean value represented by this Structured Boolean
     */
    @Override
    protected Boolean objectValue() {
        return value;
    }


    /**
     * Returns value serialized according to the specification, without parameters
     *
     * @return Serialized value
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-a-boolean">Serializing a Boolean</a>
     */
    @Override
    protected String serializeValue() {
        return Boolean.TRUE.equals(value) ? "?1" : "?0";
    }

    /**
     * Parses given string for Structured Boolean, according to the specification
     *
     * @param httpHeader String to parse, e.g. an HTTP header
     * @return Parsed Structured Boolean
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-item">Parsing an Item</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-boolean">Parsing a Boolean</a>
     */
    public static StructuredBoolean parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseItem(httpHeader, StructuredBoolean.class);
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
        var that = (StructuredBoolean) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
