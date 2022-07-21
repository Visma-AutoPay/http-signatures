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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for Structured Items, Inner Lists and Parameter values
 * <p>
 * Items and Inner List have common base class because they both can have parameters and can be members of Structured List and Structured Dictionary.
 * Parameter values do not have "inner" parameters and cannot be Inner Lists.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-items">Items</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-inner-lists">Inner Lists</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parameters">Parameter values</a>
 */
public abstract class StructuredItem implements StructuredField {
    private final StructuredParameters parameters;

    /**
     * Constructs an Item with given parameters
     *
     * @param parameters Item's parameters
     */
    protected StructuredItem(StructuredParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
    }

    /**
     * Creates a copy of this Structured Item, with provided parameters. Existing this Item's parameters are not included - only the value is copied.
     *
     * @param parameters Parameters for newly created Item
     * @return Newly created Structured Item
     */
    abstract StructuredItem withParams(StructuredParameters parameters);

    /**
     * Serializes this Structured Item to a String, according to the specification.
     *
     * @return Serialized representation of this Structured Item
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-an-item">Serializing an Item</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#ser-innerlist">Serializing an Inner List</a>
     */
    public String serialize() {
        return serializeValue() + parameters.serialize();
    }

    /**
     * Returns this Item's value serialized according to the specification, without parameters
     * @return Serialized value
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-an-item">Serializing an Item</a>
     */
    protected abstract String serializeValue();

    /**
     * Parses given string for Structured Item, according to the specification
     * <p>
     * Class of returned value depends on parsed content, and it can be any of {@link StructuredItem}'s subclasses, excluding {@link StructuredInnerList}.
     *
     * @param httpHeader String to parse, e.g. an HTTP header
     * @return Parsed Structured Item: a subclass of {@link StructuredItem}
     * @throws StructuredException Thrown in case of malformatted string
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-an-item">Parsing an Item</a>
     */
    public static StructuredItem parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseItem(httpHeader);
    }

    /**
     * Returns {@link Object} representation of this Structured Item's value.
     * <p>
     * Properties are not included. Implementations return instances of classes closest to the value instances,
     * e.g. {@link Boolean} for {@link StructuredBoolean} or {@link Long} for {@link StructuredInteger}.
     *
     * @return Object representation of this Structure Item's value
     */
    protected abstract Object objectValue();

    /**
     * Returns string representation this Structured Item's value
     * <p>
     * It's not the same as the result of serialization - it's closer to {@link Object#toString()}.
     * Mostly, it should be used for {@link StructuredString} and {@link StructuredToken}, where underlying values are strings.
     * For remaining classes, it could be for representation only.
     * Properties are not included.
     *
     * @return String representation of this Structured Item's value
     */
    public String stringValue() {
        return objectValue().toString();
    }

    /**
     * Returns boolean value of this Structured Item. Valid for {@link StructuredBoolean} only.
     *
     * @return Boolean value represented by this Structured Item
     * @throws UnsupportedOperationException When called on non-Boolean item
     */
    protected boolean boolValue() {
        throw getUnsupportedException();
    }

    /**
     * Returns int value of this Structured Item. Valid for {@link StructuredInteger} only.
     * <p>
     * (int) cast is used internally, which means that returned value will be incorrect for item values smaller than {@link Integer#MIN_VALUE}
     * or grater than {@link Integer#MAX_VALUE}.
     *
     * @return int value represented by this Structured Item
     * @throws UnsupportedOperationException When called on non-Integer item
     */
    protected int intValue() {
        throw getUnsupportedException();
    }

    /**
     * Returns long value of this Structured Item. Valid for {@link StructuredInteger} only.
     *
     * @return long value represented by this Structured Item
     * @throws UnsupportedOperationException When called on non-Integer item
     */
    protected long longValue() {
        throw getUnsupportedException();
    }

    /**
     * Returns {@link BigDecimal} value of this Structured Item. Valid for {@link StructuredDecimal} and {@link StructuredInteger} only.
     *
     * @return BigDecimal value represented by this Structured Item
     * @throws UnsupportedOperationException When called on neither Integer nor Decimal item
     */
    protected BigDecimal bigDecimalValue() {
        throw getUnsupportedException();
    }

    /**
     * Returns value of this Structured Item. Valid for {@link StructuredDecimal} and {@link StructuredInteger} only.
     *
     * @return BigDecimal value represented by this Structured Item
     * @throws UnsupportedOperationException When called on neither Integer nor Decimal item
     */
    protected double doubleValue() {
        throw getUnsupportedException();
    }

    /**
     * Returns byte[] value of this Structured Item. Valid for {@link StructuredBytes} only.
     *
     * @return byte[] value represented by this Structured Item
     * @throws UnsupportedOperationException When called on non-Byte Sequence item
     */
    protected byte[] bytesValue() {
        throw getUnsupportedException();
    }

    /**
     * Returns parameters associated with this Item.
     * If the Item does not have any parameters, an "empty" object is returned - {@link StructuredParameters} with empty underlying map.
     *
     * @return Structured Item's parameters
     */
    public StructuredParameters parameters() {
        return parameters;
    }

    /**
     * Returns value of requested parameter converted to a {@link String}.
     * Valid for all parameter types: their String representation is returned.
     * <p>
     * Mostly, it should be used for String and Token parameters.
     *
     * @param key Parameter key
     * @return String value of requested parameter, or empty Optional if parameter with given key does not exist.
     */
    public Optional<String> stringParam(String key) {
        return parameters.getString(key);
    }

    /**
     * Returns {@link Boolean} value of requested parameter. Valid for Boolean parameters only.
     *
     * @param key Parameter key
     * @return Boolean value of requested parameter, or empty Optional if parameter with given key does not exist.
     * @throws UnsupportedOperationException When requested parameter is not a Boolean
     */
    public Optional<Boolean> boolParam(String key) {
        return parameters.getBool(key);
    }

    /**
     * Returns {@link Integer} value of requested parameter. Valid for Integer parameters only.
     * <p>
     * (int) cast is used internally, which means that returned value will be incorrect for parameter values smaller than {@link Integer#MIN_VALUE}
     * or grater than {@link Integer#MAX_VALUE}.
     *
     * @param key Parameter key
     * @return Int value of requested parameter, or empty Optional if parameter with given key does not exist.
     * @throws UnsupportedOperationException When requested parameter is not an Integer
     */
    public Optional<Integer> intParam(String key) {
        return parameters.getInt(key);
    }

    /**
     * Returns {@link Long} value of requested parameter. Valid for Integer parameters only.
     *
     * @param key Parameter key
     * @return Long value of requested parameter, or empty Optional if parameter with given key does not exist.
     * @throws UnsupportedOperationException When requested parameter is not an Integer
     */
    public Optional<Long> longParam(String key) {
        return parameters.getLong(key);
    }

    /**
     * Returns {@link BigDecimal} value of requested parameter. Valid for Decimal and Integer parameters only.
     *
     * @param key Parameter key
     * @return BigDecimal value of requested parameter, or empty Optional if parameter with given key does not exist.
     * @throws UnsupportedOperationException When requested parameter is neither Decimal nor Integer
     */
    public Optional<BigDecimal> bigDecimalParam(String key) {
        return parameters.getBigDecimal(key);
    }

    /**
     * Returns {@link Double} value of requested parameter. Valid for Decimal and Integer parameters only.
     *
     * @param key Parameter key
     * @return Double value of requested parameter, or empty Optional if parameter with given key does not exist.
     * @throws UnsupportedOperationException When requested parameter is neither Decimal nor Integer
     */
    public Optional<Double> doubleParam(String key) {
        return parameters.getDouble(key);
    }

    /**
     * Returns byte[] value of requested parameter. Valid for Byte Sequence parameters only.
     *
     * @param key Parameter key
     * @return byte[] value of requested parameter, or empty Optional if parameter with given key does not exist.
     * @throws UnsupportedOperationException When requested parameter is not a Byte Sequence
     */
    public Optional<byte[]> bytesParam(String key) {
        return parameters.getBytes(key);
    }


    private UnsupportedOperationException getUnsupportedException() {
        return new UnsupportedOperationException("Item is " + getClass().getSimpleName());
    }

    /**
     * Creates a map of {@link String} keys and {@link StructuredItem} values from keys and values provided in alternate indices of the varargs param.
     * <p>
     * Keys are created using {@link Object#toString()} on "even" vararg items.
     * Values are created using {@link #fromObject(Object)} on "odd" vararg items.
     * An "ordered" map is created - order of provided items is preserved.
     *
     * @param keysAndValues Alternating keys and values: key1, value1, key2, value2, ...
     * @return A map of String keys and Structured Item values
     * @throws IllegalArgumentException If a value cannot be converted to Structured Item - its class is not supported
     * @throws NullPointerException For null values
     */
    static Map<String, StructuredItem> objectsToMap(Object... keysAndValues) {
        var items = new LinkedHashMap<String, StructuredItem>();

        for (var i = 0; i < keysAndValues.length - 1; i += 2) {
            items.put(keysAndValues[i].toString(), StructuredItem.fromObject(keysAndValues[i+1]));
        }

        return items;
    }

    /**
     * From given object creates a "bare" structured item (without parameters).
     * <p>
     * For conversion rules see {@link StructuredList#of(Collection)}.
     *
     * @param obj Object to map to Structured Item
     * @return Structured Item created from the object
     * @throws IllegalArgumentException For unsupported classes
     * @throws NullPointerException For null values
     */
    static StructuredItem fromObject(Object obj) {
        if (obj instanceof StructuredItem) {
            return (StructuredItem) obj;
        } else if (obj instanceof Integer) {
            return StructuredInteger.of((Integer) obj);
        } else if (obj instanceof Long) {
            return StructuredInteger.of((Long) obj);
        } else if (obj instanceof BigDecimal) {
            return StructuredDecimal.of((BigDecimal) obj);
        } else if (obj instanceof Boolean) {
            return StructuredBoolean.of((Boolean) obj);
        } else if (obj instanceof byte[]) {
            return StructuredBytes.of((byte[]) obj);
        } else if (obj instanceof String) {
            return StructuredString.of((String) obj);
        } else if (obj instanceof Collection<?>) {
            return StructuredInnerList.of((Collection<?>) obj);
        } else if (obj instanceof Short) {
            return StructuredInteger.of(((Short) obj).longValue());
        } else if (obj instanceof Byte) {
            return StructuredInteger.of(((Byte) obj).longValue());
        } else if (obj instanceof Double) {
            return StructuredDecimal.of((Double) obj);
        } else if (obj instanceof Float) {
            return StructuredDecimal.of(((Float) obj).doubleValue());
        } else if (obj instanceof Enum<?>) {
            return StructuredToken.of(obj.toString());
        } else if (obj != null) {
            throw new IllegalArgumentException(obj.getClass().getName() + " not supported as parameter");
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Compares the specified object with this Structured Item for equality. Returns true if the given object is of the same class as this Item, it has the
     * same value and properties.
     *
     * @param o Object to be compared with this Structured Item
     * @return True is specified object is equal to this Structured Item
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (StructuredItem) o;
        return Objects.equals(parameters, that.parameters);
    }

    /**
     * Returns hash code for this Structured Item. The hash code is a combination of hash codes of Item parameters and Item value.
     *
     * @return The hash code for this Structured Item
     */
    @Override
    public int hashCode() {
        return Objects.hash(parameters);
    }

    /**
     * Returns the string representation of this Structured Item, which is the same as serialized representation.
     *
     * @return A string representation of this Structured Item
     * @see #serialize()
     */
    @Override
    public String toString() {
        return serialize();
    }
}
