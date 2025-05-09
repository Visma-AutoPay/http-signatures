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

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredIntegerTest {
    @Test
    void createdWithInt() {
        // setup
        int value = 45;

        // execute
        var str = StructuredInteger.of(value);

        // verify
        assertThat(str.intValue()).isEqualTo(value);
        assertThat(str.longValue()).isEqualTo(value);
        assertThat(str.doubleValue()).isEqualTo(value);
        assertThat(str.bigDecimalValue()).isEqualTo(BigDecimal.valueOf(value));
        assertThat(str.stringValue()).isEqualTo("" + value);
        assertThat(str.serialize()).isEqualTo("" + value);
    }

    @Test
    void createdWithLong() {
        // setup
        long value = 454763;

        // execute
        var str = StructuredInteger.of(value);

        // verify
        assertThat(str.intValue()).isEqualTo(value);
        assertThat(str.longValue()).isEqualTo(value);
        assertThat(str.doubleValue()).isEqualTo((double) value);
        assertThat(str.bigDecimalValue()).isEqualTo(BigDecimal.valueOf(value));
        assertThat(str.stringValue()).isEqualTo("" + value);
        assertThat(str.serialize()).isEqualTo("" + value);
    }


    @Test
    void createdWithParametersMap() {
        // setup
        var value = 35;
        var params = MapUtil.linkedMap("long", 343L, "int", 55, "short", (short) 300, "shorter", (byte) 5);

        // execute
        var str = StructuredInteger.withParams(value, params);

        // verify
        assertThat(str.intValue()).isEqualTo(value);
        assertThat(str.longValue()).isEqualTo(value);
        assertThat(str.stringValue()).isEqualTo("" + value);
        assertThat(str.serialize()).isEqualTo("35;long=343;int=55;short=300;shorter=5").isEqualTo(str.toString());
    }

    @Test
    void createdWithStructuredParameters() {
        // setup
        var value = 35;
        var params = StructuredParameters.of(MapUtil.linkedMap("long", 343L, "int", 55, "short", (short) 300, "shorter", (byte) 5));

        // execute
        var str = StructuredInteger.withParams(value, params);

        // verify
        assertThat(str.intValue()).isEqualTo(value);
        assertThat(str.longValue()).isEqualTo(value);
        assertThat(str.stringValue()).isEqualTo("" + value);
        assertThat(str.serialize()).isEqualTo("35;long=343;int=55;short=300;shorter=5");
    }

    @Test
    void valuesOutOfRangeDetected() {
        assertThatThrownBy(() -> StructuredInteger.of(-1_000_000_000_000_000L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StructuredInteger.of(1_000_000_000_000_000L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredInteger.withParams(6, Map.of("a", "b"));
        var str2 = StructuredInteger.withParams(6, Map.of("a", "b"));
        var str3 = StructuredInteger.withParams(6, Map.of("a", "c"));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }
}
