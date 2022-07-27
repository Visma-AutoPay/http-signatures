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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class StructuredBytesTest {
    @Test
    void created() {
        // setup
        var value = new byte[] {1, 2, 3};

        // execute
        var str = StructuredBytes.of(value);

        // verify
        assertThat(str.bytesValue()).isEqualTo(value);
        assertThat(str.stringValue()).isEqualTo("AQID");
        assertThat(str.serialize()).isEqualTo(":AQID:");
    }

    @Test
    void empty() {
        // setup
        var value = new byte[] {};

        // execute
        var str = StructuredBytes.of(value);

        // verify
        assertThat(str.bytesValue()).isEqualTo(value);
        assertThat(str.stringValue()).isEmpty();
        assertThat(str.serialize()).isEqualTo("::");
    }

    @Test
    void createdWithParametersMap() {
        // setup
        var value = new byte[] {3, 2, 1};
        var params = MapUtil.linkedMap("encoding", "ascii", "attachment", new byte[] {8, 4, 2});

        // execute
        var str = StructuredBytes.withParams(value, params);

        // verify
        assertThat(str.bytesValue()).isEqualTo(value);
        assertThat(str.stringValue()).isEqualTo("AwIB");
        assertThat(str.serialize()).isEqualTo(":AwIB:;encoding=\"ascii\";attachment=:CAQC:").isEqualTo(str.toString());
    }

    @Test
    void createdWithStructuredParameters() {
        // setup
        var value = new byte[] {3, 2, 1};
        var params = StructuredParameters.of("encoding", StructuredToken.of("ascii"), "attachment", new byte[] {8, 4, 2});

        // execute
        var str = StructuredBytes.withParams(value, params);

        // verify
        assertThat(str.bytesValue()).isEqualTo(value);
        assertThat(str.stringValue()).isEqualTo("AwIB");
        assertThat(str.serialize()).isEqualTo(":AwIB:;encoding=ascii;attachment=:CAQC:");
    }

    @Test
    void equalsAndHashCode() {
        // setup
        var str1 = StructuredBytes.withParams(new byte[] {2, 4}, Map.of("a", "b"));
        var str2 = StructuredBytes.withParams(new byte[] {2, 4}, Map.of("a", "b"));
        var str3 = StructuredBytes.withParams(new byte[] {2, 4}, Map.of("a", "c"));

        // execute & verify
        assertThat(str1).isEqualTo(str2).hasSameHashCodeAs(str2);
        assertThat(str2).isNotEqualTo(str3);
        assertThat(str2.hashCode()).isNotEqualTo(str3.hashCode());
    }
}
