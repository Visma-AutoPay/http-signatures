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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared interface for {@link StructuredParameters} and {@link StructuredDictionary}.
 * Contains methods for converting structured dictionaries to Java {@link Map} containing Java plain objects.
 */
public interface StructuredMap {
    /**
     * Returns {@link Map} of Structured Items stored in this Dictionary or Parameters.
     * Keys or returned map correspond to Dictionary or Parameters keys. Values are {@link StructuredItem} members of Dictionary or Parameters.
     *
     * @param <T> Specific Item class if needed. No type check is performed, only simple casting.
     * @return Underlying map of {@link StructuredItem} objects
     */
    <T extends StructuredItem> Map<String, T> itemMap();

    /**
     * Returns {@link Map} of Structured Items stored in this Dictionary or Parameters.
     * <p>
     * Similar to {@link #itemMap()} ()}, but class of map values is provided as method argument rather than
     * "generic type argument", e.g. {@code myDict.itemMap(StructuredInteger.class)} instead of {@code myDict.<StructuredInteger>itemMap()}.
     *
     * @param itemClass Class of map values. No type check is performed, only simple casting.
     * @param <T>       Type of map values
     * @return Underlying map of {@link StructuredItem} objects
     */
    default <T extends StructuredItem> Map<String, T> itemMap(Class<T> itemClass) {
        return itemMap();
    }

    /**
     * Returns the key set of underlying map
     *
     * @return Map's key set
     * @see Map#keySet()
     */
    default Set<String> keySet() {
        return itemMap().keySet();
    }

    /**
     * Returns entry set of underlying map
     *
     * @param <T> Specific Item class if needed. No type check is performed, only simple casting.
     * @return Map's entry set
     * @see Map#entrySet()
     */
    default <T extends StructuredItem> Set<Map.Entry<String, T>> entrySet() {
        return this.<T>itemMap().entrySet();
    }

    /**
     * Returns entry set of underlying map
     *
     * @param itemClass Class of map values. No type check is performed, only simple casting.
     * @param <T>       Type of map values
     * @return Map's entry set
     * @see Map#entrySet()
     */
    default <T extends StructuredItem> Set<Map.Entry<String, T>> entrySet(Class<T> itemClass) {
        return itemMap(itemClass).entrySet();
    }

    /**
     * Returns values of underlying map
     * <p>
     * Contrary to {@link Map#values()}, a {@link List} is returned rather than {@link java.util.Collection}.
     * It's because order of Structured Parameters and Dictionary members is defined.
     *
     * @param <T> Specific Item class if needed. No type check is performed, only simple casting.
     * @return Map's values
     * @see Map#values()
     */
    default <T extends StructuredItem> List<T> values() {
        return List.copyOf(this.<T>itemMap().values());
    }

    /**
     * Returns values of underlying map
     * <p>
     * Contrary to {@link Map#values()}, a {@link List} is returned rather than {@link java.util.Collection}.
     * It's because order of Structured Parameters and Dictionary members is defined.
     *
     * @param itemClass Class of map values. No type check is performed, only simple casting.
     * @param <T>       Type of map values
     * @return Map's values
     * @see Map#values()
     */
    default <T extends StructuredItem> List<T> values(Class<T> itemClass) {
        return List.copyOf(itemMap(itemClass).values());
    }

    /**
     * Returns true if underlying map contains no elements
     *
     * @return True if map contains no elements
     */
    default boolean isEmpty() {
        return itemMap().isEmpty();
    }

    /**
     * Returns this map with Structured Item values converted to {@link String} objects.
     *
     * @return Map with values converted to String
     * @see StructuredItem#stringValue()
     */
    default Map<String, String> stringMap() {
        return itemMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stringValue(), (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Returns this map with Structured Item values converted to {@link Boolean} objects.
     * This map must contain {@link StructuredBoolean} values only.
     *
     * @return Map with values converted to Boolean
     * @throws UnsupportedOperationException Thrown if this map contains non-{@link StructuredBoolean} values
     */
    default Map<String, Boolean> boolMap() {
        return itemMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().boolValue(), (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Returns this map with Structured Item values converted to {@link Integer} objects.
     * This map must contain {@link StructuredInteger} values only.
     * <p>
     * (int) cast is used internally, which means that returned value will be wrong for item values smaller than {@link Integer#MIN_VALUE}
     * or grater than {@link Integer#MAX_VALUE}.
     *
     * @return Map with values converted to Integer
     * @throws UnsupportedOperationException Thrown if this map contains non-{@link StructuredInteger} values
     */
    default Map<String, Integer> intMap() {
        return itemMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().intValue(), (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Returns this map with Structured Item values converted to {@link Long} objects.
     * This map must contain {@link StructuredInteger} values only.
     *
     * @return Map with values converted to Long
     * @throws UnsupportedOperationException Thrown if this map contains non-{@link StructuredInteger} values
     */
    default Map<String, Long> longMap() {
        return itemMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().longValue(), (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Returns this map with Structured Item values converted to {@link BigDecimal} objects.
     * This map must contain {@link StructuredDecimal} or {@link StructuredInteger} values only.
     *
     * @return Map with values converted to BigDecimal
     * @throws UnsupportedOperationException Thrown if this map contains a value which is neither {@link StructuredDecimal} nor {@link StructuredInteger}
     */
    default Map<String, BigDecimal> bigDecimalMap() {
        return itemMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().bigDecimalValue(), (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Returns this map with Structured Item values converted to {@link Double} objects.
     * This map must contain {@link StructuredDecimal} or {@link StructuredInteger} values only.
     *
     * @return Map with values converted to Double
     * @throws UnsupportedOperationException Thrown if this map contains a value which is neither {@link StructuredDecimal} nor {@link StructuredInteger}
     */
    default Map<String, Double> doubleMap() {
        return itemMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().doubleValue(), (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Returns this map with Structured Item values converted to byte[] objects.
     * This map must contain {@link StructuredBytes} values only.
     *
     * @return Map with values converted to byte[]
     * @throws UnsupportedOperationException Thrown if this map contains non-{@link StructuredBytes} values
     */
    default Map<String, byte[]> bytesMap() {
        return itemMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().bytesValue(), (e1, e2) -> e2, LinkedHashMap::new));
    }


    /**
     * Returns {@link StructuredItem} stored at requested key. No type check is performed.
     *
     * @param key Dictionary or Parameter key
     * @param <T> Specific Item class if needed. No type check is performed, only simple casting.
     * @return Structured Item at requested key, or empty Optional if there is no value at requested key
     */
    default <T extends StructuredItem> Optional<T> getItem(String key) {
        return Optional.ofNullable(this.<T>itemMap().get(key));
    }

    /**
     * Returns {@link StructuredItem} stored at requested key. Throws an exception in case of a type mismatch.
     *
     * @param key       Dictionary or Parameter key
     * @param itemClass Class of map value
     * @param <T>       Type of map value
     * @return Structured Item at requested key, or empty Optional if there is no value at requested key
     * @throws UnsupportedOperationException When requested value is not an instance of requested itemClass
     */
    default <T extends StructuredItem> Optional<T> getItem(String key, Class<T> itemClass) {
        var optionalItem = getItem(key);

        if (optionalItem.isPresent()) {
            var actualClass = optionalItem.get().getClass();

            if (actualClass == itemClass) {
                //noinspection unchecked
                return (Optional<T>) optionalItem;
            } else {
                throw new UnsupportedOperationException(itemClass.getSimpleName() + " requested but " + actualClass.getSimpleName() + " is present");
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns value of Item stored at requested key, converted to a {@link String}.
     * Valid for all Item types: their String representation is returned.
     * <p>
     * Mostly, it should be used for String and Token Items.

     * @param key Dictionary or Parameter key
     * @return String value of the Item at requested key, or empty Optional if there is no value at requested key
     */
    default Optional<String> getString(String key) {
        return getItem(key).map(StructuredItem::stringValue);
    }

    /**
     * Returns {@link Boolean} value of the Item stored at requested key. Valid for {@link StructuredBoolean} Items only.
     *
     * @param key Dictionary or Parameter key
     * @return Boolean value of the Item at requested key, or empty Optional if there is no value at requested key
     * @throws UnsupportedOperationException When Item at requested key is not a {@link StructuredBoolean}
     */
    default Optional<Boolean> getBool(String key) {
        return getItem(key).map(StructuredItem::boolValue);
    }

    /**
     * Returns {@link Integer} value of the Item stored at requested key. Valid for {@link StructuredInteger} Items only.
     * <p>
     * (int) cast is used internally, which means that returned value will be wrong for item values smaller than {@link Integer#MIN_VALUE}
     * or grater than {@link Integer#MAX_VALUE}.
     *
     * @param key Dictionary or Parameter key
     * @return Integer value of the Item at requested key, or empty Optional if there is no value at requested key
     * @throws UnsupportedOperationException When Item at requested key is not a {@link StructuredInteger}
     */
    default Optional<Integer> getInt(String key) {
        return getItem(key).map(StructuredItem::intValue);
    }

    /**
     * Returns {@link Long} value of the Item stored at requested key. Valid for {@link StructuredInteger} Items only.
     *
     * @param key Dictionary or Parameter key
     * @return Long value of the Item at requested key, or empty Optional if there is no value at requested key
     * @throws UnsupportedOperationException When Item at requested key is not a {@link StructuredInteger}
     */
    default Optional<Long> getLong(String key) {
        return getItem(key).map(StructuredItem::longValue);
    }

    /**
     * Returns {@link BigDecimal} value of the Item stored at requested key. Valid for {@link StructuredDecimal} and {@link StructuredInteger} Items only.
     *
     * @param key Dictionary or Parameter key
     * @return BigDecimal value of the Item at requested key, or empty Optional if there is no value at requested key
     * @throws UnsupportedOperationException When Item at requested key is neither {@link StructuredDecimal} nor {@link StructuredInteger}
     */
    default Optional<BigDecimal> getBigDecimal(String key) {
        return getItem(key).map(StructuredItem::bigDecimalValue);
    }

    /**
     * Returns {@link Double} value of the Item stored at requested key. Valid for {@link StructuredDecimal} and {@link StructuredInteger} Items only.
     *
     * @param key Dictionary or Parameter key
     * @return Double value of the Item at requested key, or empty Optional if there is no value at requested key
     * @throws UnsupportedOperationException When Item at requested key is neither {@link StructuredDecimal} nor {@link StructuredInteger}
     */
    default Optional<Double> getDouble(String key) {
        return getItem(key).map(StructuredItem::doubleValue);
    }

    /**
     * Returns byte[] value of the Item stored at requested key. Valid for {@link StructuredBytes} Items only.
     *
     * @param key Dictionary or Parameter key
     * @return byte[] value of the Item at requested key, or empty Optional if there is no value at requested key
     * @throws UnsupportedOperationException When Item at requested key is not a {@link StructuredBytes}
     */
    default Optional<byte[]> getBytes(String key) {
        return getItem(key).map(StructuredItem::bytesValue);
    }
}
