/*
 * Copyright 2015 brutusin.org
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
package org.brutusin.commons.json;


import org.brutusin.commons.json.spi.JsonSchema;
import org.brutusin.commons.json.spi.JsonNode;
import org.brutusin.commons.json.spi.JsonCodec;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class JsonHelper {

    private static final JsonHelper instance = new JsonHelper();

    private final JsonCodec codec;
    private final SchemaHelper schemaHelper = new SchemaHelper();
    private final DataHelper dataHelper = new DataHelper();

    public static JsonHelper getDefaultInstance() {
        return instance;
    }

    public JsonHelper() {
        this(null);
    }

    public JsonHelper(JsonCodec codec) {
        if (codec == null) {
            codec = JsonCodec.getInstance();
        }
        this.codec = codec;
    }

    public SchemaHelper getSchemaHelper() {
        return schemaHelper;
    }

    public DataHelper getDataHelper() {
        return dataHelper;
    }

    public class SchemaHelper {

        public String getSchemaString(Class<?> clazz) {
            return getSchemaString(clazz, null, null);
        }

        public String getSchemaString(Class<?> clazz, String title, String description) {
            String ret = codec.getSchema(clazz);
            if (ret != null && (title != null || description != null)) {
                StringBuilder sb = new StringBuilder(ret.trim());
                if (description != null) {
                    sb.insert(1, "\"description\":\"" + codec.quoteAsUTF8(description) + "\",");
                }
                if (title != null) {
                    sb.insert(1, "\"title\":\"" + codec.quoteAsUTF8(title) + "\",");
                }
                ret = sb.toString();
            }
            return ret;
        }
        
        public void validate(JsonSchema schema, String json) throws ValidationException, ParseException {
            JsonNode node = dataHelper.parse(json);
            schema.validate(node);
        }

        private String addDraftv3(String jsonSchema) {
            if (!jsonSchema.contains("\"$schema\"")) {
                if (jsonSchema.startsWith("{\"type\":")) {
                    StringBuilder sb = new StringBuilder(jsonSchema);
                    sb.insert(1, "\"$schema\":\"http://json-schema.org/draft-03/schema#\",");
                    return sb.toString();
                }
            }
            return jsonSchema;
        }

        public JsonSchema getSchemaD3(String json) throws ParseException {
            return getSchema(addDraftv3(json));
        }

        public JsonSchema getSchema(String json) throws ParseException {
            return codec.parseSchema(json);
        }
    }

    public class DataHelper {

        public String transform(Object entity) {
            return codec.transform(entity);
        }

        public <E> E parse(String json, Class<E> clazz) throws ParseException{
            return codec.parse(json, clazz);
        }
        
        public JsonNode parse(String json) throws ParseException{
            return codec.parse(json);
        }
    }
}
