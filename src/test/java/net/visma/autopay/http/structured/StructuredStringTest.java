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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredStringTest {
    @Test
    void created() {
        // setup
        var value = "example \"quoted\" and sla\\\\ed";

        // execute
        var str = StructuredString.of(value);

        // verify
        assertThat(str.stringValue()).isEqualTo(value);
        assertThat(str.serialize()).isEqualTo("\"example \\\"quoted\\\" and sla\\\\\\\\ed\"");
    }

    @Test
    void createdWithStringParams() {
        // setup
        var value = "example";
        var paramMap = MapUtil.linkedMap("spaced", "my example", "specials", "a\\\"b", "normal", "plain");
        var params = StructuredParameters.of(paramMap);

        // execute
        // execute
        var str = StructuredString.withParams(value, params);

        // verify
        assertThat(str.stringValue()).isEqualTo(value);
        assertThat(str.serialize()).isEqualTo("\"example\";spaced=\"my example\";specials=\"a\\\\\\\"b\";normal=\"plain\"").isEqualTo(str.toString());
    }

    @Test
    void createdWithStringAndTokenParams() {
        // setup
        var value = "example";
        var params = MapUtil.linkedMap("spaced", "my example", "specials", "a\\\"b", "normal", StructuredToken.of("plain"), "quoted", "\"quoted\"");

        // execute
        var str = StructuredString.withParams(value, params);

        // verify
        assertThat(str.stringValue()).isEqualTo(value);
        assertThat(str.serialize()).isEqualTo("\"example\";spaced=\"my example\";specials=\"a\\\\\\\"b\";normal=plain;quoted=\"\\\"quoted\\\"\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"øy", "kąt", "ok\tok"})
    void illegalCharactersAreDetected(String value) {
        assertThatThrownBy(() -> StructuredString.of(value)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredString.withParams("ab", Map.of("a", "b"));
        var str2 = StructuredString.withParams("ab", Map.of("a", "b"));
        var str3 = StructuredString.withParams("ab", Map.of("a", "c"));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }
}
