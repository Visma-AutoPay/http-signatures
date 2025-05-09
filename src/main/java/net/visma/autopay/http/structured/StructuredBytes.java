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

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing Structured Byte Sequences.  Also used as "bare" Bytes in Structured parameters.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-items">Items</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-byte-sequences">Byte Sequences</a>
 */
public final class StructuredBytes extends StructuredItem {
    private final byte[] value;

    private StructuredBytes(byte[] value, StructuredParameters parameters) {
        super(parameters);
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Creates Structured Byte Sequence of given value, without parameters
     *
     * @param value Item value
     * @return Created Structured Byte Sequence
     */
    public static StructuredBytes of(byte[] value) {
        return new StructuredBytes(value, StructuredParameters.EMPTY);
    }

    /**
     * Creates Structured Byte Sequence of given value and parameters
     *
     * @param value Item value
     * @param parameters Parameter map. For details, check {@link StructuredParameters#of(Map)}.
     * @return Created Structured Byte Sequence
     */
    public static StructuredBytes withParams(byte[] value, Map<String, ?> parameters) {
        return new StructuredBytes(value, StructuredParameters.of(parameters));
    }

    /**
     * Creates Structured Byte Sequence of given value and parameters
     *
     * @param value Item value
     * @param parameters Structured Parameters
     * @return Created Structured Byte Sequence
     */
    public static StructuredBytes withParams(byte[] value, StructuredParameters parameters) {
        return new StructuredBytes(value, parameters);
    }

    @Override
    StructuredBytes withParams(StructuredParameters parameters) {
        return new StructuredBytes(value, parameters);
    }


    /**
     * Returns bytes[] value of this Structured Byte Sequence
     *
     * @return bytes[] value represented by this Structured Byte Sequence
     */
    @Override
    public byte[] bytesValue() {
        return value;
    }

    /**
     * Returns string representation of value of this Structured Byte Sequence, which is a Base64-encoded byte[] value
     *
     * @return String value represented by this Structured Byte Sequence
     */
    @Override
    public String stringValue() {
        return Base64.getEncoder().encodeToString(value);
    }

    /**
     * Returns bytes[] value of this Structured Byte Sequence
     *
     * @return bytes[] value represented by this Structured Byte Sequence
     */
    @Override
    protected byte[] objectValue() {
        return value;
    }


    /**
     * Returns value serialized according to the specifications, without parameters
     *
     * @return Serialized value
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-a-byte-sequence">Serializing a Byte Sequence</a>
     */
    @Override
    protected String serializeValue() {
        return ":" + Base64.getEncoder().encodeToString(value) + ":";
    }

    /**
     * Parses given string for Structured Byte Sequence, according to the specification
     *
     * @param httpHeader String to parse, e.g. HTTP header
     * @return Parsed Structured Byte Sequence
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-item">Parsing an Item</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-byte-sequence">Parsing a Byte Sequence</a>
     */
    public static StructuredBytes parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseItem(httpHeader, StructuredBytes.class);
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
        var that = (StructuredBytes) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
}
