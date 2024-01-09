/*
 * Copyright (c) 2022-2024 Visma Autopay AS
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
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing Structured Decimals. Also used as "bare" Decimal in Structured parameters.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-items">Items</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-decimals">Decimals</a>
 */
public final class StructuredDecimal extends StructuredItem {
    private static final BigDecimal MAX_VALUE = new BigDecimal("999999999999.999");
    private static final BigDecimal MIN_VALUE = MAX_VALUE.negate();
    private final BigDecimal value;

    private StructuredDecimal(BigDecimal value, StructuredParameters parameters) {
        super(parameters);
        Objects.requireNonNull(value);

        if (value.compareTo(MIN_VALUE) < 0 || value.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException("Value not in allowed range");
        }

        if (value.scale() > 0) {
            value = value.stripTrailingZeros();
        }

        if (value.scale() <= 0) {
            value = value.setScale(1, RoundingMode.HALF_EVEN);
        }

        this.value = value;
    }

    /**
     * Creates Structured Decimal of given value, without parameters.
     * <p>
     * Stored value is modified version of provided value:
     * <ul>
     *     <li>Scale is set to 1 (one "fractional" zero) if there is no fractional part</li>
     *     <li>Extra trailing "fractional" zeros are removed</li>
     *     <li>If the "fractional" part is longer than 3 digits it is preserved in the stored value and rounded when serializing</li>
     * </ul>
     *
     * @param value Item value
     * @return Created Structured Decimal
     * @throws IllegalArgumentException When provided value is outside the allowed range, which is &plusmn;999,999,999,999.999
     */
    public static StructuredDecimal of(BigDecimal value) {
        return new StructuredDecimal(value, StructuredParameters.EMPTY);
    }

    /**
     * Creates Structured Decimal of given value, without parameters.
     *
     * @param value Item value
     * @return Created Structured Decimal
     * @throws IllegalArgumentException When provided value is outside the allowed range
     * @see #of(BigDecimal)
     */
    public static StructuredDecimal of(double value) {
        return new StructuredDecimal(BigDecimal.valueOf(value), StructuredParameters.EMPTY);
    }

    /**
     * Creates Structured Decimal of given value, without parameters.
     *
     * @param value Item value
     * @return Created Structured Decimal
     * @throws IllegalArgumentException When provided value is outside the allowed range
     * @see #of(BigDecimal)
     */
    public static StructuredDecimal of(String value) {
        return new StructuredDecimal(new BigDecimal(value), StructuredParameters.EMPTY);
    }

    /**
     * Creates Structured Decimal of given value and parameters
     *
     * @param value      Item value
     * @param parameters Parameter map. For details, check {@link StructuredParameters#of(Map)}.
     * @return Created Structured Decimal
     * @throws IllegalArgumentException When provided value is outside the allowed range
     * @see #of(BigDecimal)
     */
    public static StructuredDecimal withParams(BigDecimal value, Map<String, ?> parameters) {
        return new StructuredDecimal(value, StructuredParameters.of(parameters));
    }

    /**
     * Creates Structured Decimal of given value and parameters
     *
     * @param value      Item value
     * @param parameters Structured Parameters
     * @return Created Structured Decimal
     * @throws IllegalArgumentException When provided value is outside the allowed range
     * @see #of(BigDecimal)
     */
    public static StructuredDecimal withParams(BigDecimal value, StructuredParameters parameters) {
        return new StructuredDecimal(value, parameters);
    }

    @Override
    StructuredDecimal withParams(StructuredParameters parameters) {
        return new StructuredDecimal(value, parameters);
    }

    /**
     * Returns {@link BigDecimal} value of this Structured Decimal
     *
     * @return BigDecimal value represented by this Structured Decimal
     */
    @Override
    public BigDecimal bigDecimalValue() {
        return value;
    }

    /**
     * Returns {@link Double} value of this Structured Decimal
     *
     * @return Double value represented by this Structured Decimal
     */
    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    /**
     * Returns {@link BigDecimal} value of this Structured Decimal
     *
     * @return BigDecimal value represented by this Structured Decimal
     */
    @Override
    protected BigDecimal objectValue() {
        return value;
    }


    /**
     * Returns value serialized according to the specification, without parameters
     *
     * @return Serialized value
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-a-decimal">Serializing a Decimal</a>
     */
    @Override
    protected String serializeValue() {
        var scale = value.scale();

        if (scale <= 3) {
            return value.toString();
        } else {
            return value.setScale(3, RoundingMode.HALF_EVEN).toString();
        }
    }

    /**
     * Parses given string for Structured Decimal, according to the specification
     *
     * @param httpHeader String to parse, e.g. HTTP header
     * @return Parsed Structured Decimal
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-item">Parsing an Item</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-integer-or-decim">Parsing an Integer or Decimal</a>
     */
    public static StructuredDecimal parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseItem(httpHeader, StructuredDecimal.class);
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
        var that = (StructuredDecimal) o;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
