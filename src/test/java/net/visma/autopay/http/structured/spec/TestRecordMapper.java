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
package net.visma.autopay.http.structured.spec;

import net.visma.autopay.http.structured.StructuredBoolean;
import net.visma.autopay.http.structured.StructuredDecimal;
import net.visma.autopay.http.structured.StructuredItem;
import net.visma.autopay.http.structured.StructuredToken;
import net.visma.autopay.http.structured.StructuredBytes;
import net.visma.autopay.http.structured.StructuredDictionary;
import net.visma.autopay.http.structured.StructuredInnerList;
import net.visma.autopay.http.structured.StructuredInteger;
import net.visma.autopay.http.structured.StructuredList;
import net.visma.autopay.http.structured.StructuredString;
import net.visma.autopay.http.structured.StructuredField;
import org.bouncycastle.util.encoders.Base32;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


class TestRecordMapper {
    static List<?> toTestRecordFormat(StructuredField structuredField) {
        if (structuredField instanceof StructuredDictionary) {
            return mapDictionary((StructuredDictionary) structuredField);
        } else if (structuredField instanceof StructuredList) {
            return mapList((StructuredList) structuredField);
        } else {
            return mapItem((StructuredItem) structuredField);
        }
    }

    private static List<?> mapDictionary(StructuredDictionary dictionary) {
        return dictionary.entrySet().stream()
                .map(entry -> List.of(entry.getKey(), mapItem(entry.getValue())))
                .collect(Collectors.toList());
    }

    private static List<?> mapList(StructuredList list) {
        return list.itemList().stream()
                .map(TestRecordMapper::mapItem)
                .collect(Collectors.toList());
    }

    private static List<?> mapItem(StructuredItem item) {
        return (List<?>) mapItem(item, true);
    }

    private static Object mapItem(StructuredItem item, boolean isFullItem) {
        Object value;

        if (item instanceof StructuredBoolean) {
            value = mapBoolean((StructuredBoolean) item);
        } else if (item instanceof StructuredBytes) {
            value = mapBytes((StructuredBytes) item);
        } else if (item instanceof StructuredDecimal) {
            value = mapDecimal((StructuredDecimal) item);
        } else if (item instanceof StructuredInnerList) {
            value = mapInnerList((StructuredInnerList) item);
        } else if (item instanceof StructuredInteger) {
            value = mapInteger((StructuredInteger) item);
        } else if (item instanceof StructuredString) {
            value = mapString((StructuredString) item);
        } else {
            value = mapToken((StructuredToken) item);
        }

        var params = item.parameters().entrySet().stream()
                .map(entry -> List.of(entry.getKey(), mapItem(entry.getValue(), false)))
                .collect(Collectors.toList());

        return isFullItem ? List.of(value, params) : value;
    }

    private static Object mapBoolean(StructuredBoolean str) {
        return str.boolValue();
    }

    private static Object mapBytes(StructuredBytes str) {
        var map = new LinkedHashMap<String, String>();
        map.put("__type", "binary");
        map.put("value", Base32.toBase32String(str.bytesValue()));

        return map;
    }

    private static Object mapDecimal(StructuredDecimal str) {
        return str.bigDecimalValue();
    }

    private static Object mapInnerList(StructuredInnerList str) {
        return str.itemList().stream()
                .map(TestRecordMapper::mapItem)
                .collect(Collectors.toList());
    }

    private static Object mapInteger(StructuredInteger str) {
        return str.longValue();
    }

    private static Object mapString(StructuredString str) {
        return str.stringValue();
    }

    private static Object mapToken(StructuredToken str) {
        var map = new LinkedHashMap<String, String>();
        map.put("__type", "token");
        map.put("value", str.stringValue());

        return map;
    }
}
