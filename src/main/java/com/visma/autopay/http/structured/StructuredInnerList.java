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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class representing Structured Inner Lists
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-inner-lists">Inner Lists</a>
 */
public final class StructuredInnerList extends StructuredItem implements StructuredCollection {
    private final List<StructuredItem> value;

    private StructuredInnerList(List<StructuredItem> value, StructuredParameters parameters) {
        super(parameters);
        this.value = value;
    }

    /**
     * Creates Structured Inner List from given Collection elements, without parameters for the inner list
     * <p>
     * Provided objects are converted to Structured Items without parameters or used directly if they are instances of {@link StructuredItem}.
     * Strings are converted to {@link StructuredString}. For tokens, use {@link StructuredToken}.
     * For detailed conversion rules see {@link StructuredList#of(Collection)}.
     *
     * @param objects Objects to be converted to {@link StructuredInnerList} items
     * @return Created Structured Inner List
     * @throws IllegalArgumentException Invalid objects provided
     */
    public static StructuredInnerList of(Collection<?> objects) {
        var items = objects.stream()
                .map(StructuredItem::fromObject)
                .collect(Collectors.toList());
        return new StructuredInnerList(items, StructuredParameters.EMPTY);
    }

    /**
     * Creates Structured Inner List from given objects, without parameters for the inner list
     * <p>
     * Provided objects are converted to Structured Items without parameters or used directly if they are instances of {@link StructuredItem}.
     * Strings are converted to {@link StructuredString}. For tokens, use {@link StructuredToken}.
     * For detailed conversion rules see {@link StructuredList#of(Collection)}.
     *
     * @param objects Objects to be converted to {@link StructuredInnerList} items
     * @return Created Structured Inner List
     * @throws IllegalArgumentException Invalid objects provided
     */
    public static StructuredInnerList of(Object... objects) {
        return of(Arrays.asList(objects));
    }

    /**
     * Creates Structured String of given object collection and parameters
     *
     * @param objects    Objects to be converted to {@link StructuredInnerList} items. For details, see {@link #of(Collection)}.
     * @param parameters Parameter map. For details, check {@link StructuredParameters#of(Map)}.
     * @return Created Structured Inner List
     * @throws IllegalArgumentException Invalid objects provided
     */
    public static StructuredInnerList withParams(Collection<?> objects, Map<String, ?> parameters) {
        return withParams(objects, StructuredParameters.of(parameters));
    }

    /**
     * Creates Structured String of given object collection and parameters
     *
     * @param objects    Objects to be converted to {@link StructuredInnerList} items. For details, see {@link #of(Collection)}.
     * @param parameters Structured Parameter
     * @return Created Structured Inner List
     * @throws IllegalArgumentException Invalid objects provided
     */
    public static StructuredInnerList withParams(Collection<?> objects, StructuredParameters parameters) {
        var items = objects.stream()
                .map(StructuredItem::fromObject)
                .collect(Collectors.toList());
        return new StructuredInnerList(items, parameters);
    }

    @Override
    StructuredInnerList withParams(StructuredParameters parameters) {
        return new StructuredInnerList(value, parameters);
    }

    @Override
    public <T extends StructuredItem> List<T> itemList() {
        //noinspection unchecked
        return (List<T>) value;
    }

    /**
     * Returns a {@link List} containing values of this Structured Inner List elements.
     * <p>
     * Individual elements are created using {@link StructuredItem#objectValue()}.
     *
     * @return List of values of this list elements
     */
    @Override
    protected List<Object> objectValue() {
        return value.stream().map(StructuredItem::objectValue).collect(Collectors.toList());
    }


    /**
     * Returns value serialized according to the specification, without parameters
     *
     * @return Serialized value
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-an-inner-list">Serializing an Inner List</a>
     */
    @Override
    protected String serializeValue() {
        var sb = new StringBuilder();
        var separator = "";
        sb.append('(');

        for (var item : value) {
            sb.append(separator).append(item.serialize());
            separator = " ";
        }

        sb.append(')');

        return sb.toString();
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
        var that = (StructuredInnerList) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
