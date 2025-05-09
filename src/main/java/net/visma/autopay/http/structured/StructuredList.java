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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class representing Structured Lists
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-lists">Lists</a>
 */
public final class StructuredList implements StructuredField, StructuredCollection {
    private final List<StructuredItem> value;

    private StructuredList(List<StructuredItem> value) {
        this.value = value;
    }

    /**
     * Creates Structured List from given Collection elements.
     * <p>
     * Provided objects are converted to Structured Items without parameters or used directly if they are instances of {@link StructuredItem}.
     * Strings are converted to {@link StructuredString}. For tokens, use {@link StructuredToken}.
     * For supported value types see the table below.
     * <p>
     * If collection element is a {@link StructuredItem} then it's used directly. Otherwise, the following conversion rules apply.
     *
     * <table style="border-spacing: 0; border-collapse: collapse">
     *     <caption>Conversion rules</caption>
     *     <thead><tr>
     *         <th style="border: 1px solid; padding: 4px">Java class</th>
     *         <th style="border: 1px solid; padding: 4px">StructuredItem class</th>
     *     </tr></thead>
     *     <tbody>
     *         <tr>
     *             <td style="border: 1px solid; padding: 4px">{@link Long}<br>{@link Integer}<br>{@link Short}<br>{@link Byte}</td>
     *             <td style="border: 1px solid; padding: 4px">{@link StructuredInteger}</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid; padding: 4px">{@link BigDecimal}<br>{@link Double}<br>{@link Float}</td>
     *             <td style="border: 1px solid; padding: 4px">{@link StructuredDecimal}</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid; padding: 4px">{@link Boolean}</td>
     *             <td style="border: 1px solid; padding: 4px">{@link StructuredBoolean}</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid; padding: 4px">byte[]</td>
     *             <td style="border: 1px solid; padding: 4px">{@link StructuredBytes}</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid; padding: 4px">{@link String}</td>
     *             <td style="border: 1px solid; padding: 4px">{@link StructuredString}</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid; padding: 4px">{@link Collection}&nbsp;</td>
     *             <td style="border: 1px solid; padding: 4px">{@link StructuredInnerList}</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid; padding: 4px">{@link Enum}</td>
     *             <td style="border: 1px solid; padding: 4px">{@link StructuredToken}</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * @param objects Collection to be converted to Structured List
     * @return Created Structured List
     * @throws IllegalArgumentException Invalid elements
     */
    public static StructuredList of(Collection<?> objects) {
        var items = objects.stream()
                .map(StructuredItem::fromObject)
                .collect(Collectors.toList());
        return new StructuredList(items);
    }

    /**
     * Creates Structured List from given objects.
     * <p>
     * Provided objects are converted to Structured Items without parameters or used directly if they are instances of {@link StructuredItem}.
     * Strings are converted to {@link StructuredString}. For tokens, use {@link StructuredToken}.
     * For detailed conversion rules see {@link #of(Collection)}.
     *
     * @param objects Objects to be converted to {@link StructuredList} items
     * @return Created Structured List
     * @throws IllegalArgumentException Invalid objects provided
     */
    public static StructuredList of(Object... objects) {
        return of(Arrays.asList(objects));
    }


    @Override
    public <T extends StructuredItem> List<T> itemList() {
        //noinspection unchecked
        return (List<T>) value;
    }

    /**
     * Returns this list with Inner List values converted to {@link List} of {@link StructuredItem} objects.
     * This list must contain {@link StructuredInnerList} values only.
     *
     * @param <T> Specific class of List members, if needed and the inner lists are homogenous
     * @return List with values converted to List of StructuredItem
     * @throws ClassCastException Thrown if this list contains non-{@link StructuredInnerList} values
     */
    public <T extends StructuredItem> List<List<T>> listList() {
        return itemList().stream().map(item -> ((StructuredInnerList) item).<T>itemList()).collect(Collectors.toList());
    }

    /**
     * Serializes this Structured List to a String, according to the specification.
     *
     * @return Serialized representation of this Structured List
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-a-list">Serializing a List</a>
     */
    @Override
    public String serialize() {
        var sb = new StringBuilder();
        var separator = "";

        for (var item : value) {
            sb.append(separator).append(item.serialize());
            separator = ", ";
        }

        return sb.toString();
    }

    /**
     * Parses given string for Structured List, according to the specification
     *
     * @param httpHeader String to parse, e.g. HTTP header
     * @return Parsed Structured List
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-list">Parsing a List</a>
     */
    public static StructuredList parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseList(httpHeader);
    }

    /**
     * Parses given HTTP header values for Structured List, according to the specification
     *
     * @param httpHeaders HTTP header values, for common header name, provided in order of occurrence in HTTP message
     * @return Parsed Structured List
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-list">Parsing a List</a>
     */
    public static StructuredList parse(Collection<String> httpHeaders) throws StructuredException {
        return StructuredParser.parseList(httpHeaders);
    }


    /**
     * Compares the specified object with this Structured List for equality. Returns true if the given object is of the same class as this List,
     * has the same value and properties.
     *
     * @param o Object to be compared with this Structured List
     * @return True is specified object is equal to this Structured List
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (StructuredList) o;
        return value.equals(that.value);
    }

    /**
     * Returns hash code for this Structured List. The hash code is a combination of hash codes of List elements.
     *
     * @return The hash code for this Structured List
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Returns the string representation of this Structured List, which is the same as serialized representation.
     *
     * @return A string representation of this Structured List
     * @see #serialize()
     */
    @Override
    public String toString() {
        return serialize();
    }
}
