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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredTokenTest {
    @Test
    void created() {
        // setup
        var value = "application/json";

        // execute
        var str = StructuredToken.of(value);

        // verify
        assertThat(str.stringValue()).isEqualTo(value);
        assertThat(str.serialize()).isEqualTo(value);
    }

    @Test
    void createdWithParamsMap() {
        // setup
        var value = "example";
        var params = MapUtil.linkedMap("charset", StructuredToken.of("utf8"), "q", new BigDecimal("0.4"), "ok", true);

        // execute
        var str = StructuredToken.withParams(value, params);

        // verify
        assertThat(str.stringValue()).isEqualTo(value);
        assertThat(str.serialize()).isEqualTo("example;charset=utf8;q=0.4;ok").isEqualTo(str.toString());
    }

    @Test
    void createdWithStructuredParameters() {
        // setup
        var value = "example";
        var params = StructuredParameters.of("charset", StructuredToken.of("utf8"), "q", new BigDecimal("0.4"), "ok", true);

        // execute
        var str = StructuredToken.withParams(value, params);

        // verify
        assertThat(str.stringValue()).isEqualTo(value);
        assertThat(str.serialize()).isEqualTo("example;charset=utf8;q=0.4;ok");
    }

    @ParameterizedTest
    @ValueSource(strings = {"øy", "må", "kąt", "9-one", "ok\tok", " ok", "hello,hi", "five>four", ""})
    void illegalCharactersAreDetected(String value) {
        assertThatThrownBy(() -> StructuredToken.of(value)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredToken.withParams("ab", Map.of("a", "b"));
        var str2 = StructuredToken.withParams("ab", Map.of("a", "b"));
        var str3 = StructuredToken.withParams("ab", Map.of("a", "c"));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }
}
