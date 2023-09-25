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

import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class StructuredInnerListTest {
    @Test
    void createdWithStrings() {
        // setup
        var bytes = new byte[]{5};
        var base64Bytes = Base64.getEncoder().encodeToString(bytes);
        var objects = List.<Object>of(55.23, 18.5f, "example", "with \"quotes\" and e\\tra", true, false, 88, bytes);

        // execute
        var str = StructuredInnerList.of(objects);

        // verify
        assertThat(str.isEmpty()).isFalse();
        assertThat(str.itemList()).hasSize(objects.size());
        assertThat(str.itemList()).containsExactly(StructuredDecimal.of("55.23"), StructuredDecimal.of("18.5"), StructuredString.of("example"),
                StructuredString.of("with \"quotes\" and e\\tra"), StructuredBoolean.of(true), StructuredBoolean.of(false), StructuredInteger.of(88),
                StructuredBytes.of(bytes));
        assertThat(str.stringList()).containsExactly("55.23", "18.5", "example", "with \"quotes\" and e\\tra", "true", "false", "88", base64Bytes);
        assertThat(str.serialize()).isEqualTo("(55.23 18.5 \"example\" \"with \\\"quotes\\\" and e\\\\tra\" ?1 ?0 88 :BQ==:)");
    }

    @Test
    void createdWithTokens() {
        // execute
        var str = StructuredInnerList.of(55, StructuredToken.of("example"), "with space");

        // verify
        assertThat(str.itemList()).hasSize(3);
        assertThat(str.itemList()).containsExactly(StructuredInteger.of(55), StructuredToken.of("example"), StructuredString.of("with space"));
        assertThat(str.stringList()).containsExactly("55", "example", "with space");
        assertThat(str.serialize()).isEqualTo("(55 example \"with space\")");
        assertThat(str.stringValue()).isEqualTo("[55, example, with space]");
    }

    @Test
    void createdFromItems() {
        // setup
        var integer = StructuredInteger.of(45);
        var token = StructuredToken.of("hello");
        var string = StructuredString.of("string");
        var items = List.of(integer, token, string);

        // execute
        var str = StructuredInnerList.of(items);

        // verify
        assertThat(str.itemList()).hasSize(items.size());
        assertThat(str.itemList()).containsExactly(integer, token, string);
        assertThat(str.stringList()).containsExactly("45", "hello", "string");
        assertThat(str.serialize()).isEqualTo("(45 hello \"string\")");
    }

    @Test
    void createdWithParams() {
        // setup
        var items = List.of(55, StructuredToken.of("example"), "with space");
        var params = MapUtil.linkedMap("ok", true, "size", 6);

        // execute
        var str = StructuredInnerList.withParams(StructuredInnerList.of(items).itemList(), params);

        // verify
        assertThat(str.itemList()).hasSize(items.size());
        assertThat(str.stringList()).containsExactly("55", "example", "with space");
        assertThat(str.boolParam("ok")).contains(true);
        assertThat(str.intParam("size")).contains(6);
        assertThat(str.serialize()).isEqualTo("(55 example \"with space\");ok;size=6").isEqualTo(str.toString());
        assertThat(str.stringValue()).isEqualTo("[55, example, with space]");
    }

    @Test
    void createdWithMultiParams() {
        // setup
        var item1 = StructuredInteger.withParams(55, Map.of("hi", StructuredToken.of("hello")));
        var item2 = StructuredString.withParams("string", Map.of("len", 6));
        var params = StructuredParameters.of("win", "dow");

        // execute
        var str = StructuredInnerList.withParams(List.of(item1, item2), params);

        // verify
        assertThat(str.itemList()).containsExactly(item1, item2);
        assertThat(str.stringList()).containsExactly("55", "string");
        assertThat(str.serialize()).isEqualTo("(55;hi=hello \"string\";len=6);win=\"dow\"");
        assertThat(str.stringValue()).isEqualTo("[55, string]");
    }

    @Test
    void emptyList() {
        // execute
        var str = StructuredInnerList.of(List.of());

        // verify
        assertThat(str.isEmpty()).isTrue();
        assertThat(str.itemList()).isEmpty();
        assertThat(str.stringList()).isEmpty();
        assertThat(str.serialize()).isEqualTo("()");
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredInnerList.of(List.of(StructuredInteger.of(45), StructuredInteger.withParams(55, Map.of("name", "five"))));
        var str2 = StructuredInnerList.of(List.of(StructuredInteger.of(45), StructuredInteger.withParams(55, Map.of("name", "five"))));
        var str3 = StructuredInnerList.of(List.of(StructuredInteger.of(45), StructuredInteger.withParams(55, Map.of("name", "four"))));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }
}
