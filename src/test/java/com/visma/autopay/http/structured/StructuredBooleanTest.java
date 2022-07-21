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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class StructuredBooleanTest {
    @Test
    void createWithValue() {
        // execute
        var truthy = StructuredBoolean.of(true);
        var falsy = StructuredBoolean.of(false);

        // verify
        verifyTruthy(truthy);
        assertThat(truthy.serialize()).isEqualTo("?1");
        verifyFalsy(falsy);
        assertThat(falsy.serialize()).isEqualTo("?0");
    }

    @Test
    void createWithValueAndMapParameters() {
        // setup
        var parameters = MapUtil.linkedMap("int", 55, "tru", true, "token", StructuredToken.of("value"), "fal", false);

        // execute
        var str = StructuredBoolean.withParams(true, parameters);

        // verify
        verifyTruthy(str);
        assertThat(str.serialize()).isEqualTo("?1;int=55;tru;token=value;fal=?0").isEqualTo(str.toString());
    }

    @Test
    void createWithValueAndParameters() {
        // setup
        var parameters = StructuredParameters.of("int", 55, "tru", true, "token", "value", "fal", false);

        // execute
        var str = StructuredBoolean.withParams(true, parameters);

        // verify
        verifyTruthy(str);
        assertThat(str.serialize()).isEqualTo("?1;int=55;tru;token=\"value\";fal=?0");
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredBoolean.withParams(true, Map.of("a", "b"));
        var str2 = StructuredBoolean.withParams(true, Map.of("a", "b"));
        var str3 = StructuredBoolean.withParams(true, Map.of("a", "c"));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }

    private void verifyTruthy(StructuredBoolean str) {
        assertThat(str.boolValue()).isTrue();
        assertThat(str.stringValue()).isEqualTo("true");
    }

    private void verifyFalsy(StructuredBoolean str) {
        assertThat(str.boolValue()).isFalse();
        assertThat(str.stringValue()).isEqualTo("false");
    }
}
