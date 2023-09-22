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
package net.visma.autopay.http.structured.spec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.visma.autopay.http.structured.StructuredItem;
import net.visma.autopay.http.structured.StructuredDictionary;
import net.visma.autopay.http.structured.StructuredException;
import net.visma.autopay.http.structured.StructuredList;
import net.visma.autopay.http.structured.StructuredField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


// Taken from https://github.com/httpwg/structured-field-tests
class StructuredSpecificationTest {
    private static final String TEST_FOLDER = "structured-tests";
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

    @ParameterizedTest
    @MethodSource("readTestCases")
    void specificationTest(TestRecord testRecord) {
        StructuredField item = null;
        StructuredException structuredException = null;
        var inputStr = testRecord.raw.size() == 1 ? testRecord.raw.get(0) : testRecord.raw.toString();

        try {
            switch (testRecord.headerType) {
                case item:
                    item = StructuredItem.parse(inputStr);
                    break;
                case list:
                    item = StructuredList.parse(testRecord.raw);
                    break;
                case dictionary:
                    item = StructuredDictionary.parse(testRecord.raw);
                    break;
            }
        } catch (StructuredException e) {
            structuredException = e;
        }

        if (!testRecord.mustFail && (item != null || !testRecord.canFail)) {
            assertThat(structuredException).isNull();
            assertThat(item).isNotNull();

            var expectedSerialized = inputStr;
            if (testRecord.canonical != null) {
                expectedSerialized = testRecord.canonical.size() == 0 ? "" : testRecord.canonical.get(0);
            }

            assertThat(item.serialize()).isEqualTo(expectedSerialized);
            assertThat(TestRecordMapper.toTestRecordFormat(item)).hasToString(testRecord.expected.toString());
        } else {
            assertThat(structuredException).as("Exception expected. input='%s' got='%s'", inputStr, item).isNotNull();
        }
    }

    private static Stream<TestRecord> readTestCases() {
        try (var res = StructuredSpecificationTest.class.getClassLoader().getResourceAsStream(TEST_FOLDER)) {
            return Stream.of(new String(res.readAllBytes(), StandardCharsets.UTF_8).split("\n"))
                    .flatMap(fileName -> readTestFile(fileName).stream());
        } catch (IOException e) {
            throw new RuntimeException("Problem while reading test files", e);
        }
    }

    private static List<TestRecord> readTestFile(String fileName) {
        try (var is = StructuredSpecificationTest.class.getClassLoader().getResourceAsStream(TEST_FOLDER + "/" + fileName)) {
            var testCases = objectMapper.readValue(is, new TypeReference<List<TestRecord>>() {});
            testCases.forEach(tc -> tc.fileName = fileName.replace(".json", ""));
            return testCases;
        } catch (IOException e) {
            throw new RuntimeException("Problem while reading " + fileName,  e);
        }
    }
}
