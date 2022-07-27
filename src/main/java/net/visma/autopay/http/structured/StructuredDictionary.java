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
package net.visma.autopay.http.structured;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class representing Structured Dictionaries
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-dictionaries">Dictionaries</a>
 */
public final class StructuredDictionary implements StructuredField, StructuredMap {
    private final Map<String, StructuredItem> value;

    private StructuredDictionary(Map<String, StructuredItem> value) {
        CharacterValidator.validateKeys(value.keySet());
        this.value = value;
    }

    /**
     * Creates Structured Dictionary of given Map value.
     * <p>
     * Order of Map entries is preserved.
     * For supported value types and conversion rules see {@link StructuredList#of(Collection)}.
     *
     * @param objectMap Map to be converted to Structured Dictionary
     * @return Created Structured Dictionary
     * @throws IllegalArgumentException Invalid keys or values
     */
    public static StructuredDictionary of(Map<String, ?> objectMap) {
        var items = new LinkedHashMap<String, StructuredItem>();
        objectMap.forEach((key, value) -> items.put(key, StructuredItem.fromObject(value)));

        return new StructuredDictionary(items);
    }

    /**
     * Creates Structured Dictionary of provided keys and values.
     * <p>
     * Keys and values should be provided in alternate indices of the vararg param.
     * Order of Map entries is preserved.
     * <p>
     * Keys are created using from "even" vararg items, using {@link Object#toString()}.
     * Values are created from "odd" vararg items, using conversion described at {@link StructuredList#of(Collection)}.
     *
     * @param keysAndValues Alternating keys and values: key1, value1, key2, value2, ...
     * @return Created Structured Dictionary
     * @throws IllegalArgumentException Invalid keys or values
     */
    public static StructuredDictionary of(Object... keysAndValues) {
        return new StructuredDictionary(StructuredItem.objectsToMap(keysAndValues));
    }


    @Override
    public <T extends StructuredItem> Map<String, T> itemMap() {
        //noinspection unchecked
        return (Map<String, T>) value;
    }

    /**
     * Returns this map with Inner List values converted to {@link List} of {@link StructuredItem} objects.
     * This map must contain {@link StructuredInnerList} values only.
     *
     * @param <T> Specific class of List members, if needed and the inner lists are homogenous
     * @return Map with values converted to List of StructuredItem
     * @throws ClassCastException Thrown if this map contains non-{@link StructuredInnerList} values
     */
    public <T extends StructuredItem> Map<String, List<T>> listMap() {
        return itemMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> ((StructuredInnerList) entry.getValue()).itemList(), (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Returns the value of {@link StructuredInnerList} at requested key. Valid for {@link StructuredInnerList} Items only.
     *
     * @param key Dictionary key
     * @param <T> Specific class of Inner List members, if needed and the list is homogenous
     * @return List of StructuredItem values stored at requested key
     */
    public <T extends StructuredItem> Optional<List<T>> getList(String key) {
        return getItem(key).map(item -> ((StructuredInnerList) item).itemList());
    }

    /**
     * Serializes this Structured Dictionary to a String, according to the specification.
     *
     * @return Serialized representation of this Structured Dictionary
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-a-dictionary">Serializing a Dictionary</a>
     */
    @Override
    public String serialize() {
        var sb = new StringBuilder();
        var separator = "";

        for (var entry : value.entrySet()) {
            var item = entry.getValue();
            sb.append(separator).append(entry.getKey());
            separator = ", ";

            if (item instanceof StructuredBoolean && item.boolValue()) {
                sb.append(item.parameters().serialize());
            } else {
                sb.append('=').append(item.serialize());
            }
        }

        return sb.toString();
    }

    /**
     * Parses given string for Structured Dictionary, according to the specification
     *
     * @param httpHeader String to parse, e.g. HTTP header
     * @return Parsed Structured Dictionary
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-dictionary">Parsing a Dictionary</a>
     */
    public static StructuredDictionary parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseDictionary(httpHeader);
    }

    /**
     * Parses given HTTP header values for Structured Dictionary, according to the specification
     *
     * @param httpHeaders HTTP header values, for common header name, provided in order of occurrence in HTTP message
     * @return Parsed Structured Dictionary
     * @throws StructuredException Thrown in case of malformatted string or wrong item type
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-a-dictionary">Parsing a Dictionary</a>
     */
    public static StructuredDictionary parse(Collection<String> httpHeaders) throws StructuredException {
        return StructuredParser.parseDictionary(httpHeaders);
    }


    /**
     * Compares the specified object with this Structured Dictionary for equality. Returns true if the given object is of the same class as this Dictionary,
     * has the same value and properties.
     *
     * @param o Object to be compared with this Structured Dictionary
     * @return True is specified object is equal to this Structured Dictionary
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (StructuredDictionary) o;
        return value.equals(that.value);
    }

    /**
     * Returns hash code for this Structured Dictionary. The hash code is a combination of hash codes of Dictionary entries.
     *
     * @return The hash code for this Structured Dictionary
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Returns the string representation of this Structured Dictionary, which is the same as serialized representation.
     *
     * @return A string representation of this Structured Dictionary
     * @see #serialize()
     */
    @Override
    public String toString() {
        return serialize();
    }
}
