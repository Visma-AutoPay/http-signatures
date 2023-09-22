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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared interface for {@link StructuredInnerList} and {@link StructuredList}.
 * Contains methods for converting structured lists to Java {@link List} containing Java plain objects.
 */
public interface StructuredCollection {
    /**
     * Returns {@link List} of Structured Items stored in this Structured List
     *
     * @param <T> Specific Item class if needed. No type check is performed, only simple casting.
     * @return Underlying list of {@link StructuredItem} objects
     */
    <T extends StructuredItem> List<T> itemList();

    /**
     * Returns {@link List} of Structured Items stored in this Structured List.
     * <p>
     * Similar to {@link #itemList()}, but class of list members is provided as method argument rather than
     * "generic type argument", e.g. {@code myList.itemList(StructuredInteger.class)} instead of {@code myList.<StructuredInteger>itemList()}.
     *
     * @param itemClass Class of list members. No type check is performed, only simple casting.
     * @param <T>       Type of list members
     * @return Underlying list of {@link StructuredItem} objects
     */
    default <T extends StructuredItem> List<T> itemList(Class<T> itemClass) {
        return itemList();
    }

    /**
     * Returns true if this list contains no elements
     *
     * @return True if list contains no elements
     */
    default boolean isEmpty() {
        return itemList().isEmpty();
    }

    /**
     * Returns values of Structured Items in this Structured List converted to {@link String} objects.
     *
     * @return List of item values converted to String
     * @see StructuredItem#stringValue()
     */
    default List<String> stringList() {
        return itemList().stream().map(StructuredItem::stringValue).collect(Collectors.toList());
    }

    /**
     * Returns values of Structured Items in this Structured List converted to {@link Boolean} objects.
     * This Structured List must contain {@link StructuredBoolean} members only.
     *
     * @return List of item values converted to Boolean
     * @throws UnsupportedOperationException Thrown if this list contains non-{@link StructuredBoolean} members.
     */
    default List<Boolean> boolList() {
        return itemList().stream().map(StructuredItem::boolValue).collect(Collectors.toList());
    }

    /**
     * Returns values of Structured Items in this Structured List converted to {@link Integer} objects.
     * This Structured List must contain {@link StructuredInteger} members only.
     * <p>
     * (int) cast is used internally, which means that returned value will be wrong for item values smaller than {@link Integer#MIN_VALUE}
     * or grater than {@link Integer#MAX_VALUE}.
     *
     * @return List of item values converted to Integer
     * @throws UnsupportedOperationException Thrown if this list contains non-{@link StructuredInteger} members
     */
    default List<Integer> intList() {
        return itemList().stream().map(StructuredItem::intValue).collect(Collectors.toList());
    }

    /**
     * Returns values of Structured Items in this Structured List converted to {@link Long} objects.
     * This Structured List must contain {@link StructuredInteger} members only.
     *
     * @return List of item values converted to Long
     * @throws UnsupportedOperationException Thrown if this list contains non-{@link StructuredInteger} members
     */
    default List<Long> longList() {
        return itemList().stream().map(StructuredItem::longValue).collect(Collectors.toList());
    }

    /**
     * Returns values of Structured Items in this Structured List converted to {@link BigDecimal} objects.
     * This Structured List must contain {@link StructuredDecimal} or {@link StructuredInteger} members only.
     *
     * @return List of item values converted to BigDecimal
     * @throws UnsupportedOperationException Thrown if this list contains a member which is neither {@link StructuredDecimal} nor {@link StructuredInteger}
     */
    default List<BigDecimal> bigDecimalList() {
        return itemList().stream().map(StructuredItem::bigDecimalValue).collect(Collectors.toList());
    }

    /**
     * Returns values of Structured Items in this Structured List converted to {@link Double} objects.
     * This Structured List must contain {@link StructuredDecimal} or {@link StructuredInteger} members only.
     *
     * @return List of item values converted to Double
     * @throws UnsupportedOperationException Thrown if this list contains non-{@link StructuredDecimal} or non-{@link StructuredInteger} members.
     */
    default List<Double> doubleList() {
        return itemList().stream().map(StructuredItem::doubleValue).collect(Collectors.toList());
    }

    /**
     * Returns values of Structured Items in this Structured List converted to byte[] objects.
     * This Structured List must contain {@link StructuredBytes} members only.
     *
     * @return List of item values converted to byte[]
     * @throws UnsupportedOperationException Thrown if this list contains non-{@link StructuredBytes} members.
     */
    default List<byte[]> bytesList() {
        return itemList().stream().map(StructuredItem::bytesValue).collect(Collectors.toList());
    }
}
