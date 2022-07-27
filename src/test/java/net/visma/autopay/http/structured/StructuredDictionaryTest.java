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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;


@SuppressWarnings("OptionalGetWithoutIsPresent")
class StructuredDictionaryTest {
    @SuppressWarnings("java:S5961")
    @Test
    void createdFromList() {
        // setup
        var objects = MapUtil.linkedMap(
                "si", StructuredInteger.of(10),
                "ii", 100,
                "il", 200L,
                "de", new BigDecimal("32.1"),
                "tr", true,
                "by", new byte[] {2},
                "to", StructuredToken.of("string"),
                "st", "second string",
                "sa", "@param",
                "fa", false,
                "li", List.of(10, StructuredToken.of("ok")),
                "is", (short) 20,
                "ib", (byte) 8,
                "dd", 18.22,
                "ls", new LinkedHashSet<>(List.of(StructuredToken.of("s1"), StructuredToken.of("s2"))));

        // execute
        var str = StructuredDictionary.of(objects);

        // verify
        assertThat(str.isEmpty()).isFalse();
        assertThat(str.serialize()).isEqualTo("si=10, ii=100, il=200, de=32.1, tr, by=:Ag==:, to=string, st=\"second string\", sa=\"@param\", fa=?0, li=(10 "
                + "ok), is=20, ib=8, dd=18.22, ls=(s1 s2)");
        assertThat(str.entrySet()).hasSize(objects.size());
        assertThat(str.values()).hasSize(objects.size());
        assertThat(str.getItem("il", StructuredInteger.class)).containsInstanceOf(StructuredInteger.class);
        assertThat(str.<StructuredInteger>getItem("il").get().parameters().isEmpty()).isTrue();
        assertThat(str.getItem("some integer", StructuredInteger.class)).isEmpty();
        assertThat(str.getItem("to", StructuredToken.class).get().stringValue()).isEqualTo("string");
        assertThat(str.getInt("si")).contains(10);
        assertThat(str.getLong("si")).contains(10L);
        assertThat(str.getDouble("si")).contains(10.0);
        assertThat(str.getInt("ii")).contains(100);
        assertThat(str.getLong("ii")).contains(100L);
        assertThat(str.getDouble("ii")).contains(100.0);
        assertThat(str.getInt("il")).contains(200);
        assertThat(str.getLong("il")).contains(200L);
        assertThat(str.getDouble("il")).contains(200.0);
        assertThat(str.getBigDecimal("de")).contains(new BigDecimal("32.1"));
        assertThat(str.getDouble("de")).contains(32.1);
        assertThat(str.getBool("tr")).contains(true);
        assertThat(str.getBytes("by")).contains(new byte[] {2});
        assertThat(str.getString("to")).contains("string");
        assertThat(str.getString("st")).contains("second string");
        assertThat(str.getString("sa")).contains("@param");
        assertThat(str.getBool("fa")).contains(false);
        assertThat(str.getList("li")).contains(List.of(StructuredInteger.of(10), StructuredToken.of("ok")));
        assertThat(str.getInt("is")).contains(20);
        assertThat(str.getLong("is")).contains(20L);
        assertThat(str.getDouble("is")).contains(20.0);
        assertThat(str.getInt("ib")).contains(8);
        assertThat(str.getLong("ib")).contains(8L);
        assertThat(str.getDouble("ib")).contains(8.0);
        assertThat(str.getBigDecimal("dd")).contains(new BigDecimal("18.22"));
        assertThat(str.getDouble("dd")).contains(18.22);
        assertThat(str.getList("ls")).contains(List.of(StructuredToken.of("s1"), StructuredToken.of("s2")));
    }

    @Test
    void createdFromObjects() {
        // setup
        var valueAp = StructuredString.of("application");
        var valueIi = StructuredInteger.of(5);
        var valuePr = StructuredString.of("program");

        // execute
        var str = StructuredDictionary.of("ap", "application", "ii", 5, "pr", "program");

        // verify
        assertThat(str.serialize()).isEqualTo("ap=\"application\", ii=5, pr=\"program\"");
        assertThat(str.itemMap().get("ap")).isInstanceOf(StructuredString.class);
        assertThat(str.itemMap().get("ii")).isInstanceOf(StructuredInteger.class);
        assertThat(str.itemMap().get("ap")).isInstanceOf(StructuredString.class);
        assertThat(str.entrySet()).hasSize(3);
        assertThat(str.entrySet()).contains(entry("ap", valueAp));
        assertThat(str.entrySet()).contains(entry("ii", valueIi));
        assertThat(str.entrySet()).contains(entry("pr", valuePr));
        assertThat(str.values()).containsExactly(valueAp, valueIi, valuePr);
    }

    @Test
    void createdFromItems() {
        // setup
        var integer = StructuredInteger.of(45);
        var integerWithParams = StructuredInteger.withParams(55, Map.of("name", StructuredToken.of("five")));
        var token = StructuredToken.of("hello");
        var tokenWithParams = StructuredToken.withParams("hi", MapUtil.linkedMap("from", StructuredToken.of("me"), "to", 4));
        var string = StructuredString.of("string");
        var trueWithParams = StructuredBoolean.withParams(true, Map.of("ok", "1"));
        //noinspection unchecked,rawtypes
        var items = (Map<String, StructuredItem>) (Map) MapUtil.linkedMap(
                "int", integer,
                "inp", integerWithParams,
                "tok", token,
                "top", tokenWithParams,
                "str", string,
                "tru", trueWithParams);

        // execute
        var str = StructuredDictionary.of(items);

        // verify
        assertThat(str.serialize())
                .isEqualTo("int=45, inp=55;name=five, tok=hello, top=hi;from=me;to=4, str=\"string\", tru;ok=\"1\"")
                .isEqualTo(str.toString());
        assertThat(str.itemMap()).contains(entry("int", integer));
        assertThat(str.itemMap()).contains(entry("inp", integerWithParams));
        assertThat(str.itemMap()).contains(entry("tok", token));
        assertThat(str.itemMap()).contains(entry("top", tokenWithParams));
        assertThat(str.itemMap()).contains(entry("str", string));
        assertThat(str.itemMap()).contains(entry("tru", trueWithParams));
    }

    @Test
    void emptyDictionary() {
        assertThat(StructuredDictionary.of(Map.of()).itemMap()).isEmpty();
        assertThat(StructuredDictionary.of().itemMap()).isEmpty();
        assertThat(StructuredDictionary.of("a", List.of()).getList("a").get()).isEmpty();
    }

    @Test
    void homogeneousDictionaryMappedToRegularMap() {
        // setup
        var strings = MapUtil.linkedMap("first", "one", "second", "two");
        var booleans = MapUtil.linkedMap("positive", true, "negative", false);
        var integers = MapUtil.linkedMap("one", 1, "two", 2);
        var longs = MapUtil.linkedMap("one", 1L, "two", 2L);
        var doubles = MapUtil.linkedMap("one", 1.0, "two", 2.0);
        var decimals = MapUtil.linkedMap("one", new BigDecimal("1.0"), "two", new BigDecimal("2.0"));
        var bytes = MapUtil.linkedMap("one", new byte[]{1}, "two", new byte[]{2});
        var lists = MapUtil.linkedMap("one", List.of(1, 11), "two", List.of(2, 22));
        var structuredLists = MapUtil.linkedMap("one", List.of(StructuredInteger.of(1), StructuredInteger.of(11)), "two",
                List.of(StructuredInteger.of(2), StructuredInteger.of(22)));

        var strDict = StructuredDictionary.of(strings);
        var boolDict = StructuredDictionary.of(booleans);
        var intDict = StructuredDictionary.of(integers);
        var decDict = StructuredDictionary.of(doubles);
        var bytesDict = StructuredDictionary.of(bytes);
        var listDict = StructuredDictionary.of(lists);

        // execute & verify
        assertThat(strDict.stringMap()).isEqualTo(strings);
        assertThat(boolDict.boolMap()).isEqualTo(booleans);
        assertThat(intDict.intMap()).isEqualTo(integers);
        assertThat(intDict.longMap()).isEqualTo(longs);
        assertThat(intDict.doubleMap()).isEqualTo(doubles);
        assertThat(decDict.bigDecimalMap()).isEqualTo(decimals);
        assertThat(decDict.doubleMap()).isEqualTo(doubles);
        assertThat(bytesDict.bytesMap()).isEqualTo(bytes);
        assertThat(listDict.listMap()).isEqualTo(structuredLists);
        assertThat(intDict.entrySet(StructuredInteger.class)).contains(entry("one", StructuredInteger.of(1)), entry("two", StructuredInteger.of(2)));
        assertThat(strDict.values(StructuredString.class)).containsExactly(StructuredString.of("one"), StructuredString.of("two"));
    }

    @Test
    void exceptionIsThrownWhenAccessingWrongItemClass() {
        // setup
        var str = StructuredDictionary.of(Map.of("key", "item"));

        // execute & verify
        assertThatThrownBy(() -> str.getItem("key", StructuredInteger.class)).isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"øy", "må", "kąt", "9-one", "ok\tok", " ok", "hello,hi", "five>four", "Hi", "hi!", "oK", ""})
    void illegalCharactersInKeysAreDetected(String value) {
        assertThatThrownBy(() -> StructuredDictionary.of("one", 1, value, 2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredDictionary.of(Map.of("ii", StructuredInteger.of(45), "si", StructuredInteger.withParams(55, Map.of("name", "five"))));
        var str2 = StructuredDictionary.of(Map.of("ii", StructuredInteger.of(45), "si", StructuredInteger.withParams(55, Map.of("name", "five"))));
        var str3 = StructuredDictionary.of(Map.of("ii", StructuredInteger.of(45), "si", StructuredInteger.withParams(55, Map.of("name", "four"))));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }
}
