/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.pipeline.task;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.test.common.StroomPipelineTestFileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

class TestFullTranslationTaskAndStepping extends TranslationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestFullTranslationTaskAndStepping.class);

    @Override
    protected boolean onAfterSetup() {
        testTranslationTask(false, false);
        return true;
    }

    @Test
    void testBOMXMLEvents() throws IOException {
        testStepping("BOM_XML-EVENTS");
    }

    @Test
    void testBOMAndReplaceXMLEvents() throws IOException {
        testStepping("BOM_AND_REPLACE_XML-EVENTS");
    }

    @Test
    void testDataSplitterEvents() throws IOException {
        testStepping("DATA_SPLITTER-EVENTS");
    }

    @Test
    void testFileToLocationReference() throws IOException {
        testStepping("FILENO_TO_LOCATION-REFERENCE");
    }

    @Test
    void testJSONEvents() throws IOException {
        testStepping("JSON-EVENTS");
    }

    @Test
    void testRawStreamingEvents() throws IOException {
        testStepping("RAW_STREAMING-EVENTS");
    }

    @Test
    void testRawStreamingForkEvents() throws IOException {
        testStepping("RAW_STREAMING_FORK-EVENTS");
    }

    @Test
    void testXMLFragmentEvents() throws IOException {
        testStepping("XML_FRAGMENT-EVENTS");
    }

    @Test
    void testXMLEvents() throws IOException {
        testStepping("XML-EVENTS");
    }

    @Test
    void testXMLReaderEvents() throws IOException {
        testStepping("XML_READER-EVENTS");
    }

    @Test
    void testZipTestDataSplitterEvents() throws IOException {
        testStepping("ZIP_TEST-DATA_SPLITTER-EVENTS");
    }

    private void testStepping(final String feedName) throws IOException {
        final Path outDir = StroomPipelineTestFileUtil.getTestResourcesDir().resolve("TestFullTranslationTaskAndStepping");

        final long time = System.currentTimeMillis();
        testSteppingTask(feedName, outDir);
        final long steppingTime = System.currentTimeMillis() - time;

        LOGGER.info(feedName + " TRANSLATION STEPPING TOOK: " + Duration.ofMillis(steppingTime).toString());
    }
}
