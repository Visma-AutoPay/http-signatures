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

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class StructuredItemTest {
    @Test
    void illegalValueGettersThrowExceptions() {
        // setup
        var item = StructuredString.of("string");
        var exceptionClass = UnsupportedOperationException.class;
        var messagePart = "StructuredString";

        // execute & verify
        assertThatThrownBy(item::boolValue).isInstanceOf(exceptionClass).hasMessageContaining(messagePart);
        assertThatThrownBy(item::intValue).isInstanceOf(exceptionClass).hasMessageContaining(messagePart);
        assertThatThrownBy(item::longValue).isInstanceOf(exceptionClass).hasMessageContaining(messagePart);
        assertThatThrownBy(item::bigDecimalValue).isInstanceOf(exceptionClass).hasMessageContaining(messagePart);
        assertThatThrownBy(item::doubleValue).isInstanceOf(exceptionClass).hasMessageContaining(messagePart);
        assertThatThrownBy(item::bytesValue).isInstanceOf(exceptionClass).hasMessageContaining(messagePart);
    }
}
