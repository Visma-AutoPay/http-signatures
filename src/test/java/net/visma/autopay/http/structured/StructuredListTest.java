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

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SuppressWarnings("unchecked")
class StructuredListTest {
    @Test
    void createdFromList() {
        // setup
        var objects = List.of(StructuredInteger.of(10), 100, 200L, new BigDecimal("32.1"), true, new byte[]{2}, StructuredToken.of("string"), "second string",
                List.of(10, StructuredToken.of("ok")), (short) 20, (byte) 10, 18.22,
                new LinkedHashSet<>(List.of(StructuredToken.of("s1"), StructuredToken.of("s2"))));

        // execute
        var str = StructuredList.of(objects);

        // verify
        var actualItems = str.itemList();
        //noinspection rawtypes
        var itemClasses = (List) actualItems.stream().map(StructuredItem::getClass).collect(Collectors.toList());
        assertThat(str.serialize()).isEqualTo("10, 100, 200, 32.1, ?1, :Ag==:, string, \"second string\", (10 ok), 20, 10, 18.22, (s1 s2)");
        assertThat(itemClasses).containsExactly(StructuredInteger.class, StructuredInteger.class, StructuredInteger.class, StructuredDecimal.class,
                StructuredBoolean.class, StructuredBytes.class, StructuredToken.class, StructuredString.class, StructuredInnerList.class,
                StructuredInteger.class, StructuredInteger.class, StructuredDecimal.class, StructuredInnerList.class);
    }

    @Test
    void createdFromObjects() {
        // execute
        var str = StructuredList.of(55, "aaa", "bb\"bb\\b", List.of(33, "cc"), TestEnum.ONE);

        // verify
        assertThat(str.serialize()).isEqualTo("55, \"aaa\", \"bb\\\"bb\\\\b\", (33 \"cc\"), one");
        assertThat(str.itemList()).containsExactly(StructuredInteger.of(55), StructuredString.of("aaa"), StructuredString.of("bb\"bb\\b"),
                StructuredInnerList.of(List.of(33L, "cc")), StructuredToken.of("one"));
    }

    @Test
    void createdFromItems() {
        // setup
        var integer = StructuredInteger.of(45);
        var integerWithParams = StructuredInteger.withParams(55, Map.of("name", StructuredToken.of("five")));
        var token = StructuredToken.of("hello");
        var tokenWithParams = StructuredToken.withParams("hi", MapUtil.linkedMap("from", StructuredToken.of("me"), "to", 4));
        var string = StructuredString.of("string");
        var items = List.of(integer, integerWithParams, token, tokenWithParams, string);

        // execute
        var str = StructuredList.of(items);

        // verify
        assertThat(str.serialize()).isEqualTo("45, 55;name=five, hello, hi;from=me;to=4, \"string\"").isEqualTo(str.toString());
    }

    @Test
    void homogeneousListMappedToRegularList() {
        // setup
        var strings = List.of("one", "two");
        var booleans = List.of(true, false);
        var integers = List.of(1, 2);
        var longs = List.of(1L, 2L);
        var doubles = List.of(1.0, 2.0);
        var decimals = List.of(new BigDecimal("1.0"), new BigDecimal("2.0"));
        var bytes = List.of(new byte[]{1}, new byte[]{2});
        var lists = List.of(List.of(1, 11), List.of(2, 22));
        var structuredLists = List.of(List.of(StructuredInteger.of(1), StructuredInteger.of(11)),
                List.of(StructuredInteger.of(2), StructuredInteger.of(22)));

        var strDict = StructuredList.of(strings);
        var boolDict = StructuredList.of(booleans);
        var intDict = StructuredList.of(integers);
        var decDict = StructuredList.of(doubles);
        var bytesDict = StructuredList.of(bytes);
        var listDict = StructuredList.of(lists);

        // execute & verify
        assertThat(strDict.stringList()).isEqualTo(strings);
        assertThat(boolDict.boolList()).isEqualTo(booleans);
        assertThat(intDict.intList()).isEqualTo(integers);
        assertThat(intDict.longList()).isEqualTo(longs);
        assertThat(intDict.doubleList()).isEqualTo(doubles);
        assertThat(decDict.bigDecimalList()).isEqualTo(decimals);
        assertThat(decDict.doubleList()).isEqualTo(doubles);
        assertThat(bytesDict.bytesList()).isEqualTo(bytes);
        assertThat(listDict.listList()).isEqualTo(structuredLists);
    }

    @Test
    void exceptionIsThrownWhenUsingUnsupportedClasses() {
        // setup
        var objects = List.of(5, Map.of(5, 10));

        // execute & verify
        assertThatThrownBy(() -> StructuredList.of(objects)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredList.of(List.of(StructuredInteger.of(45), StructuredInteger.withParams(55, Map.of("name", "five"))));
        var str2 = StructuredList.of(List.of(StructuredInteger.of(45), StructuredInteger.withParams(55, Map.of("name", "five"))));
        var str3 = StructuredList.of(List.of(StructuredInteger.of(45), StructuredInteger.withParams(55, Map.of("name", "four"))));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }

    private enum TestEnum {
        ONE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
