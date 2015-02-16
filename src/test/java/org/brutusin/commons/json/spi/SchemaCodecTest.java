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

import org.brutusin.commons.json.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public abstract class SchemaCodecTest {

    public SchemaCodecTest() {
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
    public void testJsonPropertyAnnotationSupport() {
        String schemaStr = JsonCodec.getInstance().getSchemaString(TestClass.class);
        assertTrue(schemaStr.contains("\"required\":true"));
        assertTrue(schemaStr.contains("\"title\":\"a title aint\""));
        assertTrue(schemaStr.contains("\"description\":\"A string\""));
        assertTrue(schemaStr.contains("default\":\"3\""));
        assertTrue(schemaStr.contains("default\":3"));
        assertTrue(schemaStr.contains("\"enum\":[\"2\",\"4\"]"));
        assertTrue(schemaStr.contains("\"default\":[true,true]"));
        
        System.out.println(schemaStr);
    }
    
    @Test
    public void testIndexablePropertyAnnotationSupport() {
        String schemaStr = JsonCodec.getInstance().getSchemaString(TestClass.class);
        assertTrue(schemaStr.contains("\"index\":\"index\""));
    }
    
    @Test
    public void testAdditionalPropertiesSupport() {
        String schemaStr = JsonCodec.getInstance().getSchemaString(TestClass.class);
        assertTrue(schemaStr.contains("\"additionalProperties\":{\"type\":\"boolean\"}"));
    }
    
    @Test
    public void testNotSchemaReferences() throws ParseException{
        String bSchema = JsonCodec.getInstance().getSchemaString(B.class);
        String aSchema = JsonCodec.getInstance().getSchemaString(A.class);
        JsonNode bNode = JsonCodec.getInstance().parse(bSchema);
        assertTrue(aSchema.contains(bNode.get("type").toString()));
    }

    class A {
        B b;

        public B getB() {
            return b;
        }
        public void setB(B b) {
            this.b = b;
        }
    }

    class B {
        C c;
        public C getC() {
            return c;
        }
        public void setC(C c) {
            this.c = c;
        }
    }

    class C {
        String name;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
}

