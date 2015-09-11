/*
 * Copyright 2015 Ignacio del Valle Alles idelvall@brutusin.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.commons.json.spi;

import org.brutusin.commons.json.ValidationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public abstract class ValidationCodecTest {

    public ValidationCodecTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test() throws Exception {
        String schemaStr = JsonCodec.getInstance().getSchemaString(TestClass.class);
        JsonSchema schema = JsonCodec.getInstance().parseSchema(schemaStr);
        JsonNode node = JsonCodec.getInstance().parse("{\"string\": \"4\", \"aint\":3}");
        schema.validate(node);
    }
    
    @Test(expected = ValidationException.class)
    public void testFail1() throws Exception {
        String schemaStr = JsonCodec.getInstance().getSchemaString(TestClass.class);
        JsonSchema schema = JsonCodec.getInstance().parseSchema(schemaStr);
        JsonNode node = JsonCodec.getInstance().parse("true");
        schema.validate(node);
    }
}

