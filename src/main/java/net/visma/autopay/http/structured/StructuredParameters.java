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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing Structured Parameters
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parameters">Parameters</a>
 */
public final class StructuredParameters implements StructuredMap {
    private final Map<String, StructuredItem> parameterMap;
    /**
     * Object representing empty parameter map
     */
    static final StructuredParameters EMPTY = new StructuredParameters(Map.of());

    private StructuredParameters(Map<String, StructuredItem> parameterMap) {
        CharacterValidator.validateKeys(parameterMap.keySet());
        this.parameterMap = parameterMap;
    }

    /**
     * Creates Structured Parameters from given map
     * <p>
     * Order of Map entries is preserved.
     * For supported value types and conversion rules see {@link StructuredList#of(Collection)}.*
     *
     * @param parameters Parameter map
     * @return Created Structured Parameters
     * @throws IllegalArgumentException Invalid keys or values
     */
    public static StructuredParameters of(Map<String, ?> parameters) {
        var parameterMap = new LinkedHashMap<String, StructuredItem>();
        parameters.forEach((key, value) -> parameterMap.put(key, StructuredItem.fromObject(value)));

        return new StructuredParameters(parameterMap);
    }

    /**
     * Creates Structured Parameters from provided keys and values.
     * <p>
     * Keys and values should be provided in alternate indices of the vararg param.
     * Order of Map entries is preserved.
     * <p>
     * Keys are created using from "even" vararg items, using {@link Object#toString()}.
     * Values are created from "odd" vararg items, using conversion described at {@link StructuredList#of(Collection)}.
     *
     * @param keysAndValues Alternating keys and values: key1, value1, key2, value2, ...
     * @return Created Structured Parameters
     * @throws IllegalArgumentException Invalid keys or values
     */
    public static StructuredParameters of(Object... keysAndValues) {
        return new StructuredParameters(StructuredItem.objectsToMap(keysAndValues));
    }


    @Override
    public <T extends StructuredItem> Map<String, T> itemMap() {
        //noinspection unchecked
        return (Map<String, T>) parameterMap;
    }


    /**
     * Serializes these Structured Parameters to a String, according to the specification.
     *
     * @return Serialized representation of these Structured Parameters
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#ser-params">Serializing Parameters</a>
     */
    String serialize() {
        var sb = new StringBuilder();

        for (var entry : parameterMap.entrySet()) {
            var item = entry.getValue();
            sb.append(';').append(entry.getKey());

            if (!(item instanceof StructuredBoolean) || !item.boolValue()) {
                sb.append('=').append(item.serialize());
            }
        }

        return sb.toString();
    }


    /**
     * Compares the specified object with these Structured Parameters for equality. Returns true if the given object is of the same class as these Parameters,
     * underlying map has the same keys and values.
     *
     * @param o Object to be compared with these Structured Parameters
     * @return True is specified object is equal to these Structured Parameters
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (StructuredParameters) o;
        return Objects.equals(parameterMap, that.parameterMap);
    }

    /**
     * Returns hash code for these Structured Parameters. The hash code is a combination of hash codes of Parameters entries.
     *
     * @return The hash code for these Structured Parameters
     */
    @Override
    public int hashCode() {
        return Objects.hash(parameterMap);
    }

    /**
     * Returns the string representation of these Structured Parameters, which is the same as serialized representation.
     *
     * @return A string representation of these Structured Parameters
     * @see #serialize()
     */
    @Override
    public String toString() {
        return serialize();
    }
}
