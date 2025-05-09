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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class StructuredParametersTest {
    @Test
    void parametersSetAndRetrieved() {
        // setup
        var value = "application/json";
        var params = MapUtil.linkedMap("encoding", StructuredToken.of("ascii"), "prob", 47.48);

        // execute
        var str = StructuredToken.withParams(value, params);

        // verify
        assertThat(str.stringParam("encoding")).contains("ascii");
        assertThat(str.bigDecimalParam("prob")).contains(new BigDecimal("47.48"));
        assertThat(str.stringParam("unknown")).isEmpty();
        assertThat(str.serialize()).isEqualTo("application/json;encoding=ascii;prob=47.48").isEqualTo(str.toString());
        assertThat(str.parameters().keySet()).isEqualTo(params.keySet());
    }

    @Test
    void createdFromItemMap() {
        // setup
        var trueItem = StructuredBoolean.of(true);
        var falseItem = StructuredBoolean.of(false);
        var bytesItem = StructuredBytes.of(new byte[] {3});
        var decimalItem = StructuredDecimal.of(new BigDecimal("21.77"));
        var intItem = StructuredInteger.of(67);
        var stringItem = StructuredString.of("text");
        var tokenItem = StructuredToken.of("hello");
        //noinspection rawtypes,unchecked
        var itemMap = (Map<String, StructuredItem>) (Map) MapUtil.linkedMap(
                "tr", trueItem,
                "in", intItem,
                "fa", falseItem,
                "by", bytesItem,
                "de", decimalItem,
                "st", stringItem,
                "to", tokenItem);


        // execute
        var params = StructuredParameters.of(itemMap);
        var str = StructuredToken.withParams("token", params);

        // verify
        var expectedSerialized = ";tr;in=67;fa=?0;by=:Aw==:;de=21.77;st=\"text\";to=hello";
        assertThat(params.serialize()).isEqualTo(expectedSerialized).isEqualTo(params.toString());
        assertThat(str.serialize()).isEqualTo("token" + expectedSerialized);
        assertThat(str.boolParam("tr")).contains(true);
        assertThat(str.boolParam("fa")).contains(false);
        assertThat(str.bytesParam("by")).contains(new byte[] {3});
        assertThat(str.bigDecimalParam("de")).contains(new BigDecimal("21.77"));
        assertThat(str.doubleParam("de")).contains(21.77);
        assertThat(str.intParam("in")).contains(67);
        assertThat(str.longParam("in")).contains(67L);
        assertThat(str.doubleParam("in")).contains(67.0);
        assertThat(str.stringParam("st")).contains("text");
        assertThat(str.stringParam("to")).contains("hello");
        assertThat(str.intParam("qwe")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"øy", "må", "kąt", "9-one", "ok\tok", " ok", "hello,hi", "five>four", "Hi", "hi!", "oK"})
    void illegalCharactersInKeysAreDetected(String value) {
        assertThatThrownBy(() -> StructuredParameters.of("one", 1, value, 2)).isInstanceOf(IllegalArgumentException.class);
    }
}
