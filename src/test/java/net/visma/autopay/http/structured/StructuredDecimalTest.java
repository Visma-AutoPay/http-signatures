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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class StructuredDecimalTest {
    @Test
    void created() {
        // setup
        var value = new BigDecimal("123.55");

        // execute
        var str = StructuredDecimal.of(value);

        // verify
        assertThat(str.bigDecimalValue()).isEqualTo(value);
        assertThat(str.doubleValue()).isEqualTo(123.55);
        assertThat(str.serialize()).isEqualTo("123.55");
    }

    @Test
    void createdInteger() {
        // setup
        var value = new BigDecimal("50");

        // execute
        var str = StructuredDecimal.of(value);

        // verify
        assertThat(str.bigDecimalValue()).isEqualTo(new BigDecimal("50.0"));
        assertThat(str.doubleValue()).isEqualTo(50.0);
        assertThat(str.serialize()).isEqualTo("50.0");
    }

    @Test
    void createdFromDouble() {
        // setup
        var doubleValue = 88.8;
        var decimalValue = new BigDecimal("88.8");

        // execute
        var str = StructuredDecimal.of(doubleValue);

        // verify
        assertThat(str.bigDecimalValue()).isEqualByComparingTo(decimalValue);
        assertThat(str.doubleValue()).isEqualTo(doubleValue);
        assertThat(str.serialize()).isEqualTo("88.8");
    }

    @Test
    void createdFromString() {
        // setup
        var stringValue = "-67";
        var decimalValue = new BigDecimal(stringValue);

        // execute
        var str = StructuredDecimal.of(stringValue);

        // verify
        assertThat(str.bigDecimalValue()).isEqualByComparingTo(decimalValue);
        assertThat(str.doubleValue()).isEqualTo(-67.0);
        assertThat(str.serialize()).isEqualTo("-67.0");
    }

    @Test
    void createdWithParametersMap() {
        // setup
        var value = new BigDecimal("-77.13");
        var params = MapUtil.linkedMap("currency", StructuredToken.of("EUR"), "exchange", new BigDecimal("2.445"));

        // execute
        var str = StructuredDecimal.withParams(value, params);

        // verify
        assertThat(str.bigDecimalValue()).isEqualTo(value);
        assertThat(str.doubleValue()).isEqualTo(-77.13);
        assertThat(str.serialize()).isEqualTo("-77.13;currency=EUR;exchange=2.445").isEqualTo(str.toString());
    }

    @Test
    void createdWithStructuredParameters() {
        // setup
        var value = new BigDecimal("-77.13");
        var paramMap = MapUtil.linkedMap("currency", "EUR", "exchange", new BigDecimal("2.445"));
        var params = StructuredParameters.of(paramMap);

        // execute
        var str = StructuredDecimal.withParams(value, params);

        // verify
        assertThat(str.bigDecimalValue()).isEqualTo(value);
        assertThat(str.doubleValue()).isEqualTo(-77.13);
        assertThat(str.serialize()).isEqualTo("-77.13;currency=\"EUR\";exchange=2.445");
    }

    @Test
    void serializedValuesAreEvenRounded() {
        assertThat(StructuredDecimal.of("12.1234").serialize()).isEqualTo("12.123");
        assertThat(StructuredDecimal.of("12.1236").serialize()).isEqualTo("12.124");
        assertThat(StructuredDecimal.of("12.1235").serialize()).isEqualTo("12.124");
        assertThat(StructuredDecimal.of("12.1245").serialize()).isEqualTo("12.124");
    }

    @Test
    void bigValuesAreParsedAndSerialized() throws Exception {
        // setup
        var bigNegative = "-999999999999.999";
        var bigPositive = "999999999999.999";

        // execute & verify
        assertThat(StructuredDecimal.parse(bigNegative).serialize()).isEqualTo(bigNegative);
        assertThat(StructuredDecimal.parse(bigPositive).serialize()).isEqualTo(bigPositive);
    }

    @Test
    void valuesOutOfRangeDetected() {
        assertThatThrownBy(() -> StructuredDecimal.of(-1_000_000_000_000L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StructuredDecimal.of("1000000000000")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredDecimal.withParams(new BigDecimal("10.3"), Map.of("a", "b"));
        var str2 = StructuredDecimal.withParams(new BigDecimal("10.3"), Map.of("a", "b"));
        var str3 = StructuredDecimal.withParams(new BigDecimal("10.3"), Map.of("a", "c"));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }
}
