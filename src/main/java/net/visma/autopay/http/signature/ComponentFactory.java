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
package net.visma.autopay.http.signature;

import net.visma.autopay.http.structured.StructuredString;

/**
 * Factory to convert component represented as structured string to {@link Component} object.
 * Used when parsing <em>Signature-Input</em> header when verifying signature.
 */
final class ComponentFactory {
    /**
     * Creates {@link Component} from provided structured string - a single item from <em>Signature-Input</em> header
     *
     * @param structuredString A single item taken from <em>Signature-Input</em>
     * @return A {@link Component} object
     */
    static Component create(StructuredString structuredString) {
        var value = structuredString.stringValue();
        Component component;

        if (value.charAt(0) == '@') {
            component = createDerivedComponent(structuredString);
        } else {
            component = createHeaderComponent(structuredString);
        }

        return component;
    }

    private static Component createDerivedComponent(StructuredString structuredString) {
        return new DerivedComponent(structuredString);
    }

    private static Component createHeaderComponent(StructuredString structuredString) {
        return new HeaderComponent(structuredString);
    }

    private ComponentFactory() {
        throw new UnsupportedOperationException();
    }
}
