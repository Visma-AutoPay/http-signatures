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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SuppressWarnings({"OptionalGetWithoutIsPresent", "java:S5961"})
class StructuredParserTest {

    @Test
    void parseDictionary() {
        // setup
        var examples = List.of(
                "en=\"Apple\", da=:w4ZibGV0w6ZydGU=:",
                "a=?0, b, c; foo=bar",
                "rating=1.5, feelings=(joy sadness)",
                "a=(1 2), b=3,c=4;aa=bb,\td=(5 6);valid",
                "foo=1 , bar=2",
                "");
        var serializedExamples = List.of(
                "en=\"Apple\", da=:w4ZibGV0w6ZydGU=:",
                "a=?0, b, c;foo=bar",
                "rating=1.5, feelings=(joy sadness)",
                "a=(1 2), b=3, c=4;aa=bb, d=(5 6);valid",
                "foo=1, bar=2",
                "");

        // execute
        var results = examples.stream().map(toUnchecked(StructuredDictionary::parse)).collect(Collectors.toList());

        // verify
        verifySerialized(results, serializedExamples);
    }

    @Test
    void parseDictionaryWithInnerList() {
        // setup
        var examples = List.of(
                "one=(\"foo\" ?1),  two=(\"baz\") ,  three=(0.0  -1.2), four=(), five=(hello 55)",
                "one=(:AwIB:;a=1;b=2);lvl=5, two=(\"bar\" \"baz\");lvl=1");
        var serializedExamples = List.of(
                "one=(\"foo\" ?1), two=(\"baz\"), three=(0.0 -1.2), four=(), five=(hello 55)",
                "one=(:AwIB:;a=1;b=2);lvl=5, two=(\"bar\" \"baz\");lvl=1");

        // execute
        var results = examples.stream().map(toUnchecked(StructuredDictionary::parse)).collect(Collectors.toList());

        // verify
        verifySerialized(results, serializedExamples);
    }

    @Test
    void parseList() {
        // setup
        var examples = List.of(
                "sugar,  tea ,  rum ",
                "\"Apple\", :w4ZibGV0w6ZydGU=:",
                "?0, b, c; foo=bar",
                "1.5,  (joy sadness)",
                "(1 2), 3,4;aa=bb,\t(5 6);valid",
                "1, 2",
                "");
        var serializedExamples = List.of(
                "sugar, tea, rum",
                "\"Apple\", :w4ZibGV0w6ZydGU=:",
                "?0, b, c;foo=bar",
                "1.5, (joy sadness)",
                "(1 2), 3, 4;aa=bb, (5 6);valid",
                "1, 2",
                "");

        // execute
        var results = examples.stream().map(toUnchecked(StructuredList::parse)).collect(Collectors.toList());

        // verify
        verifySerialized(results, serializedExamples);
    }

    @Test
    void parseListWithInnerList() {
        // setup
        var examples = List.of(
                "(\"foo\" \"bar\"), (\"baz\"), (\"bat\" \"one\"), (), (hello 55)",
                "(\"foo\"; a=1;b=2);lvl=5, (\"bar\" \"baz\");lvl=1");
        var serializedExamples = List.of(
                "(\"foo\" \"bar\"), (\"baz\"), (\"bat\" \"one\"), (), (hello 55)",
                "(\"foo\";a=1;b=2);lvl=5, (\"bar\" \"baz\");lvl=1");

        // execute
        var results = examples.stream().map(toUnchecked(StructuredList::parse)).collect(Collectors.toList());

        // verify
        verifySerialized(results, serializedExamples);
    }


    @Test
    void parseBoolean() throws StructuredException {
        assertThat(StructuredBoolean.parse("?1").boolValue()).isTrue();
        assertThat(StructuredBoolean.parse("?0").boolValue()).isFalse();
        assertThat(StructuredBoolean.parse("?1;x=y").stringParam("x")).contains("y");
    }

    @Test
    void parseBytes() throws StructuredException {
        assertThat(StructuredBytes.parse(":AwIB:").bytesValue()).isEqualTo(new byte[]{3, 2, 1});
        assertThat(StructuredBytes.parse("::").bytesValue()).isEqualTo(new byte[0]);
        assertThat(StructuredBytes.parse(":CAQC:; ok;pr=-0.8").boolParam("ok")).contains(true);
        assertThat(StructuredBytes.parse(":CAQC:; ok;pr=-0.8"))
                .satisfies(item -> assertThat(item.bytesValue()).isEqualTo(new byte[]{8, 4, 2}))
                .satisfies(item -> assertThat(item.boolParam("ok")).contains(true))
                .satisfies(item -> assertThat(item.doubleParam("pr")).contains(-0.8));
    }

    @Test
    void parseDecimal() throws StructuredException {
        assertThat(StructuredDecimal.parse("4.5").bigDecimalValue()).isEqualTo(new BigDecimal("4.5"));
        assertThat(StructuredDecimal.parse("-004.5").bigDecimalValue()).isEqualTo(new BigDecimal("-4.5"));
        assertThat(StructuredDecimal.parse("0.0").bigDecimalValue()).isEqualTo(new BigDecimal("0.0"));
        assertThat(StructuredDecimal.parse("0000.000").bigDecimalValue()).isEqualTo(new BigDecimal("0.0"));
        assertThat(StructuredDecimal.parse("-0.0").bigDecimalValue()).isEqualTo(new BigDecimal("0.0"));
    }

    @Test
    void parseInteger() throws StructuredException {
        assertThat(StructuredInteger.parse("42").longValue()).isEqualTo(42L);
        assertThat(StructuredInteger.parse("-42").longValue()).isEqualTo(-42L);
        assertThat(StructuredInteger.parse("0").intValue()).isZero();
        assertThat(StructuredInteger.parse("-0").intValue()).isZero();
        assertThat(StructuredInteger.parse("000").intValue()).isZero();
        assertThat(StructuredInteger.parse("00042").intValue()).isEqualTo(42);
    }

    @Test
    void parseString() throws StructuredException {
        assertThat(StructuredString.parse("\"Hello world\"").stringValue()).isEqualTo("Hello world");
        assertThat(StructuredString.parse("\"Hel\\\\lo \\\"w\\\"orld\"").stringValue()).isEqualTo("Hel\\lo \"w\"orld");
    }

    @Test
    void parseToken() throws StructuredException {
        assertThat(StructuredToken.parse("foo123/456").stringValue()).isEqualTo("foo123/456");
        assertThat(StructuredToken.parse("*").stringValue()).isEqualTo("*");
        assertThat(StructuredToken.parse("my-width").stringValue()).isEqualTo("my-width");
    }

    @Test
    void parseItem() throws StructuredException {
        assertThat(StructuredItem.parse("?1; x=y")).isInstanceOfSatisfying(StructuredBoolean.class, str -> {
            assertThat(str.boolValue()).isTrue();
            assertThat(str.stringParam("x")).contains("y");
        });
        assertThat(StructuredItem.parse("24; x=\"a \\\"y, y\"")).isInstanceOfSatisfying(StructuredInteger.class, str -> {
            assertThat(str.intValue()).isEqualTo(24);
            assertThat(str.stringParam("x")).contains("a \"y, y");
        });
        assertThat(StructuredItem.parse("foo123/456")).isInstanceOfSatisfying(StructuredToken.class, str ->
                assertThat(str.stringValue()).isEqualTo("foo123/456")
        );
    }

    @Test
    void parseAny() throws StructuredException {
        assertThat(StructuredField.parse("?1; x=y")).isInstanceOfSatisfying(StructuredBoolean.class, str -> {
            assertThat(str.boolValue()).isTrue();
            assertThat(str.stringParam("x")).contains("y");
        });
        assertThat(StructuredField.parse("24; x=\"a \\\"y, y\"")).isInstanceOfSatisfying(StructuredInteger.class, str -> {
            assertThat(str.intValue()).isEqualTo(24);
            assertThat(str.stringParam("x")).contains("a \"y, y");
        });
        assertThat(StructuredField.parse("a=?0, b, c; foo=bar")).isInstanceOfSatisfying(StructuredDictionary.class, str ->
                assertThat(str.serialize()).isEqualTo("a=?0, b, c;foo=bar"));
        assertThat(StructuredField.parse("a, b, c; foo=bar")).isInstanceOfSatisfying(StructuredList.class, str ->
                assertThat(str.serialize()).isEqualTo("a, b, c;foo=bar"));
        assertThat(StructuredField.parse("")).isInstanceOfSatisfying(StructuredList.class, str ->
                assertThat(str.serialize()).isEmpty());
    }

    @Test
    void unexpectedTypeIsDetected() {
        assertThatThrownBy(() -> StructuredToken.parse("55")).isInstanceOfSatisfying(StructuredException.class, e -> {
            Assertions.assertThat(e).hasMessageContaining("StructuredToken");
            assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.WRONG_ITEM_CLASS);
        });
        assertThatThrownBy(() -> StructuredString.parse("hello")).isInstanceOfSatisfying(StructuredException.class, e -> {
            Assertions.assertThat(e).hasMessageContaining("StructuredString");
            assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.WRONG_ITEM_CLASS);
        });
        assertThatThrownBy(() -> StructuredDecimal.parse("66")).isInstanceOfSatisfying(StructuredException.class, e -> {
            Assertions.assertThat(e).hasMessageContaining("StructuredDecimal");
            assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.WRONG_ITEM_CLASS);
        });
        assertThatThrownBy(() -> StructuredInteger.parse("2.3")).isInstanceOfSatisfying(StructuredException.class, e -> {
            Assertions.assertThat(e).hasMessageContaining("StructuredInteger");
            assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.WRONG_ITEM_CLASS);
        });
    }

    @Test
    void emptyInputIsDetected() {
        assertThatThrownBy(() -> StructuredString.parse(null)).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.EMPTY_INPUT));
        assertThatThrownBy(() -> StructuredString.parse("   ")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.EMPTY_INPUT));
    }

    @Test
    void unexpectedCharacterIsDetected() {
        assertThatThrownBy(() -> StructuredToken.parse("hello=")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.UNEXPECTED_CHARACTER));
        assertThatThrownBy(() -> StructuredBoolean.parse("?x")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.UNEXPECTED_CHARACTER));
        assertThatThrownBy(() -> StructuredDictionary.parse("a =1, b=2")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.UNEXPECTED_CHARACTER));
        assertThatThrownBy(() -> StructuredDictionary.parse("a=1, b= 2")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.UNEXPECTED_CHARACTER));
        assertThatThrownBy(() -> StructuredDictionary.parse("a=1,,b=2")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.UNEXPECTED_CHARACTER));
    }

    @Test
    void missingCharacterIsDetected() {
        assertThatThrownBy(() -> StructuredString.parse("\"hello")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.MISSING_CHARACTER));
        assertThatThrownBy(() -> StructuredBytes.parse(":aGVsbG8=")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.MISSING_CHARACTER));
        assertThatThrownBy(() -> StructuredList.parse("(12 33")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.MISSING_CHARACTER));
    }

    @Test
    void wrongNumberIsDetected() {
        assertThatThrownBy(() -> StructuredDecimal.parse("1234567890123.0")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.WRONG_NUMBER));
        assertThatThrownBy(() -> StructuredInteger.parse("1234567890123456")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.WRONG_NUMBER));
    }

    @Test
    void invalidBase64StringIsDetected() {
        assertThatThrownBy(() -> StructuredBytes.parse(":hello!@#:")).isInstanceOfSatisfying(StructuredException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StructuredException.ErrorCode.INVALID_BYTES));
    }

    @Test
    void realHttpHeaders() throws StructuredException {
        assertThat(StructuredInteger.parse("86400").intValue()).isEqualTo(86400);
        assertThat(StructuredList.parse("Viewport-Width, Width"))
                .satisfies(list -> assertThat(list.stringList()).containsExactly("Viewport-Width", "Width"))
                .satisfies(list -> assertThat(list.itemList().get(0)).isInstanceOf(StructuredToken.class))
                .satisfies(list -> assertThat(list.itemList().get(1)).isInstanceOf(StructuredToken.class));
        assertThat(StructuredList.parse("deflate, gzip;q=1.0, *;q=0.5"))
                .satisfies(list -> assertThat(list.stringList()).containsExactly("deflate", "gzip", "*"))
                .satisfies(list -> assertThat(list.itemList().get(0)).isInstanceOf(StructuredToken.class))
                .satisfies(list -> assertThat(list.itemList().get(1)).isInstanceOf(StructuredToken.class))
                .satisfies(list -> assertThat(list.itemList().get(2)).isInstanceOf(StructuredToken.class))
                .satisfies(list -> assertThat(list.itemList().get(1).doubleParam("q")).contains(1.0))
                .satisfies(list -> assertThat(list.itemList().get(2).doubleParam("q")).contains(0.5));
        assertThat(StructuredToken.parse("text/example;charset=utf-8"))
                .satisfies(item -> assertThat(item.stringValue()).isEqualTo("text/example"))
                .satisfies(item -> assertThat(item.stringParam("charset")).contains("utf-8"));
        assertThat(StructuredToken.parse("*/*"))
                .satisfies(item -> assertThat(item.stringValue()).isEqualTo("*/*"));
        assertThat(StructuredToken.parse("https://developer.mozilla.org"))
                .satisfies(item -> assertThat(item.stringValue()).isEqualTo("https://developer.mozilla.org"));
        assertThat(StructuredDictionary.parse("h3-25=\":443\"; ma=3600, h2=\":444\"; ma=3601; persist=1"))
                .satisfies(map -> assertThat(map.getString("h3-25")).contains(":443"))
                .satisfies(map -> assertThat(map.getString("h2")).contains(":444"))
                .satisfies(map -> assertThat(map.getItem("h3-25").get()).isInstanceOf(StructuredString.class))
                .satisfies(map -> assertThat(map.getItem("h2").get()).isInstanceOf(StructuredString.class))
                .satisfies(map -> assertThat(map.itemMap().get("h3-25").intParam("ma")).contains(3600))
                .satisfies(map -> assertThat(map.itemMap().get("h2").intParam("ma")).contains(3601))
                .satisfies(map -> assertThat(map.itemMap().get("h2").intParam("persist")).contains(1));
        assertThat(StructuredDictionary.parse("max-age=604800, must-revalidate"))
                .satisfies(map -> assertThat(map.getLong("max-age")).contains(604800L))
                .satisfies(map -> assertThat(map.getBool("must-revalidate")).contains(true));
    }

    @Test
    void securityHttpHeaders() throws StructuredException {
        assertThat(StructuredDictionary.parse("sha-256=:4REjxQ4yrqUVicfSKYNO/cF9zNj5ANbzgDZt3/h3Qxo=:,"
                + "sha-512=:WZDPaVn/7XgHaAy8pmojAkGWoRx2UFChF41A2svX+TaPm+AbwAgBWnrIiYllu7BNNyealdVLvRwEmTHWXvJwew==:"))
                .satisfies(map -> assertThat(map.getBytes("sha-256").get()).hasSize(32))
                .satisfies(map -> assertThat(map.getBytes("sha-512").get()).hasSize(64));

        var signatureInputDict = StructuredDictionary.parse("reqres=(\"@status\" \"content-length\" \"content-type\" "
                + " \"@request-response\";key=\"sig1\");created=1618884479;keyid=\"test-key-ecc-p256\"");
        assertThat(signatureInputDict.itemMap()).hasSize(1);
        var signatureInput = signatureInputDict.getItem("reqres", StructuredInnerList.class).get();
        assertThat(signatureInput.longParam("created")).contains(1618884479L);
        assertThat(signatureInput.stringParam("keyid")).contains("test-key-ecc-p256");
        assertThat(signatureInput.stringList()).containsExactly("@status", "content-length", "content-type", "@request-response");
        assertThat(signatureInput.itemList().get(3).stringParam("key")).contains("sig1");
    }

    private void verifySerialized(List<? extends StructuredField> items, List<String> expected) {
        assertThat(items.stream().map(StructuredField::serialize).collect(Collectors.toList())).isEqualTo(expected);
    }

    private interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    private <T, R> Function<T, R> toUnchecked(CheckedFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        };
    }
}
