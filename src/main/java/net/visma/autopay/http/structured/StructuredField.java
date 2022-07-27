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


/**
 * Common interface for all Structured Fields
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html">Structured Fields</a>
 */
public interface StructuredField {
    /**
     * Serializes this item to String according to the specification
     * <p>
     * Both value and parameters are serialized
     *
     * @return Serialized representation of this Structured Field
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-serializing-structured-fiel">Serializing Structured Fields</a>
     */
    String serialize();

    /**
     * Parses given string for Structured Field, according to the specification
     * <p>
     * Class of returned value depends on parsed content, and it can be any class implementing {@link StructuredField} interface.
     * In case of ambiguity, the simplest implementation is returned (Item then List then Dictionary).
     * For example:
     * <ul>
     *     <li>
     *         <em>ok</em> is a valid Token and single-element List of Tokens and single-element Dictionary with <em>ok</em> key and <em>true</em> value.
     *         Here, {@link StructuredToken} will be returned.
     *      </li>
     *      <li>
     *          <em>ok, not</em> is a valid List of Tokens and a Dictionary with <em>true</em> values. Here, {@link StructuredList} will be returned.
     *      </li>
     * </ul>
     * To avoid ambiguity, use parse() methods in concrete classes
     *
     * @param httpHeader String to parse, e.g. an HTTP header
     * @return Parsed Structured Field
     * @throws StructuredException Thrown in case of malformatted string
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8941.html#name-parsing-structured-fields">Parsing Structured Fields</a>
     */
    static StructuredField parse(String httpHeader) throws StructuredException {
        return StructuredParser.parseAny(httpHeader);
    }
}
