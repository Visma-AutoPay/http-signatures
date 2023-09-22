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

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing Structured Integers. Also used as "bare" Integer in Structured parameters.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-items">Items</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-integers">Integers</a>
 */
public final class StructuredInteger extends StructuredItem {
    private static final long MAX_VALUE = 999_999_999_999_999L;
    private static final long MIN_VALUE = -MAX_VALUE;
    private final long value;

    private StructuredInteger(long value, StructuredParameters parameters) {
        super(parameters);

        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException("Value not in allowed range");
        }

        this.value = value;
    }

    /**
     * Creates Structured Integer of given value, without parameters.
     *
     * @param value Item value
     * @return Created Structured Integer
     * @throws IllegalArgumentException When provided value is outside the allowed range, which is &plusmn;999,999,999,999,999
     */
    public static StructuredInteger of(long value) {
        return new StructuredInteger(value, StructuredParameters.EMPTY);
    }

    /**
     * Creates Structured Integer of given value, without parameters.
     *
     * @param value Item value
     * @return Created Structured Integer
     */
    public static StructuredInteger of(int value) {
        return new StructuredInteger(value, StructuredParameters.EMPTY);
    }

    /**
     * Creates Structured Integer of given value and parameters
     *
     * @param value      Item value
     * @param parameters Parameter map. For details, check {@link StructuredParameters#of(Map)}.
     * @return Created Structured Integer
     * @throws IllegalArgumentException When provided value is outside the allowed range
     */
    public static StructuredInteger withParams(long value, Map<String, ?> parameters) {
        return new StructuredInteger(value, StructuredParameters.of(parameters));
    }

    /**
     * Creates Structured Integer of given value and parameters
     *
     * @param value      Item value
     * @param parameters Structured Parameters
     * @return Created Structured DInteger
     */
    public static StructuredInteger withParams(long value, StructuredParameters parameters) {
        return new StructuredInteger(value, parameters);
    }

    @Override
    StructuredInteger withParams(StructuredParameters parameters) {
        return new StructuredInteger(value, parameters);
    }

    /**
     * Returns int value of this Structured Integer
     * <p>
     * (int) cast is used internally, which means that returned value will be incorrect for item values smaller than {@link Integer#MIN_VALUE}
     * or grater than {@link Integer#MAX_VALUE}.
     *
     * @return int value represented by this Structured Integer
     */
    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Returns long value of this Structured Integer
     *
     * @return long value represented by this Structured Integer
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns double value of this Structured Integer
     *
     * @return double value represented by this Structured Integer
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Returns {@link BigDecimal} value of this Structured Integer
     *
     * @return BigDecimal value represented by this Structured Integer
     */
    @Override
    public BigDecimal bigDecimalValue() {
        return BigDecimal.valueOf(value);
    }

    /**
     * Returns {@link Long} value of this Structured Integer
     *
     * @return Long value represented by this Structured Integer
     */
    @Override
    protected Long objectValue() {
        return value;
    }


    /**
     * Returns value serialized according to the specification, without parameters
     *
     * @return Serialized value
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-an-integer">Serializing an Integer</a>
     */
    @Override
    protected String serializeValue() {
        return Long.toString(value);
    }

    /**
     * Parses given string for Structured Integer, according to the specification
     *
     * @param httpHeader String to parse, e.g. HTTP header
     * @return Parsed Structured Integer
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-item">Parsing an Item</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-integer-or-decim">Parsing an Integer or Decimal</a>
     */
    public static StructuredInteger parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseItem(httpHeader, StructuredInteger.class);
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
        var that = (StructuredInteger) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
