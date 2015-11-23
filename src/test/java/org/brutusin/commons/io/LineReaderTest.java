/*
 * Copyright 2014 brutusin.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.commons.io;

import org.brutusin.commons.Bean;
import java.io.InputStream;
import org.brutusin.commons.utils.Miscellaneous;
import org.junit.Test;
import static org.junit.Assert.*;

public class LineReaderTest {

    private static final String CONTENTS
            = "11	12\n"
            + "21	22\n"
            + "31	32";

    private static InputStream createInputStream() {
        return Miscellaneous.toInputStream(CONTENTS);
    }

    @Test
    public void testRun() throws Exception {
        final StringBuilder sb = new StringBuilder();
        final Bean<Boolean> onFinishCalled = new Bean<Boolean>();
        LineReader lr = new LineReader(createInputStream()) {
            @Override
            protected void processLine(String line) throws Exception {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }

            @Override
            protected void onExceptionFound(Exception ex) {
            }

            @Override
            protected void onFinish() {
                // Asserts only called once
                assertNull(onFinishCalled.getValue());
                onFinishCalled.setValue(Boolean.TRUE);
            }
        };
        lr.run();
        // Asserts onFinish() called
        assertNotNull(onFinishCalled.getValue());
        assertEquals(CONTENTS, sb.toString());
    }

    @Test
    public void testExit() throws Exception{
        final Bean<Boolean> exited = new Bean<Boolean>();
        final Bean<Boolean> onFinishCalled = new Bean<Boolean>();
        LineReader lr = new LineReader(createInputStream()) {
            @Override
            protected void processLine(String line) throws Exception {
                // Asserts only called once
                assertNull(exited.getValue());
                exited.setValue(Boolean.TRUE);
                exit();
            }

            @Override
            protected void onExceptionFound(Exception ex) {
            }

            @Override
            protected void onFinish() {
                // Asserts only called once
                assertNull(onFinishCalled.getValue());
                onFinishCalled.setValue(Boolean.TRUE);
            }
        };
        lr.run();
        // Asserts onFinish() called
        assertNotNull(onFinishCalled.getValue());
    }

    @Test
    public void testGetLineNumber() throws Exception{
        final StringBuilder sb = new StringBuilder();
        LineReader lr = new LineReader(createInputStream()) {
            @Override
            protected void processLine(String line) throws Exception {
                sb.append(getLineNumber());
            }

            @Override
            protected void onExceptionFound(Exception ex) {
            }
        };
        lr.run();
        assertEquals("123", sb.toString());
    }

    @Test
    public void testIsLastLine() throws Exception{
        final StringBuilder sb = new StringBuilder();
        LineReader lr = new LineReader(createInputStream()) {
            @Override
            protected void processLine(String line) throws Exception {
                sb.append(isLastLine());
            }

            @Override
            protected void onExceptionFound(Exception ex) {
            }
        };
        lr.run();
        assertEquals("falsefalsetrue", sb.toString());
    }

    /**
     * Test of getBytesBuffered method, of class LineReader.
     */
    @Test
    public void testGetBytesBuffered() throws Exception{
        final int totalBytes = CONTENTS.getBytes().length;
        LineReader lr = new LineReader(createInputStream()) {
            @Override
            protected void processLine(String line) throws Exception {
                assertTrue(totalBytes >= getBytesBuffered());
            }

            @Override
            protected void onExceptionFound(Exception ex) {
            }

            @Override
            protected void onFinish() {
                assertTrue(totalBytes == getBytesBuffered());
            }

        };
        lr.run();
    }

    @Test
    public void testOnExceptionFound() throws Exception{
        final StringBuilder sb = new StringBuilder();
        final String exMessage = "exMessage";
        LineReader lr = new LineReader(createInputStream()) {
            @Override
            protected void processLine(String line) throws Exception {
                throw new Exception(exMessage);
            }

            @Override
            protected void onExceptionFound(Exception ex) {
                sb.append(getLineNumber());
                assertEquals(ex.getMessage(), exMessage);
            }
        };
        lr.run();
        assertEquals("123", sb.toString());
    }

}
