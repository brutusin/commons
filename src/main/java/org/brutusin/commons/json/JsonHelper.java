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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import java.io.IOException;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class JsonHelper {
    
    private static final JsonHelper instance = new JsonHelper();

    private final ObjectMapper mapper;
    private final SchemaFactoryWrapper schemaFactory;
    private final SchemaHelper schemaHelper = new SchemaHelper();
    private final DataHelper dataHelper = new DataHelper();

    public static JsonHelper getDefaultInstance() {
        return instance;
    }

    public JsonHelper() {
        this(null, null);
    }

    public JsonHelper(ObjectMapper mapper) {
        this(mapper, null);
    }

    public JsonHelper(SchemaFactoryWrapper schemaFactory) {
        this(null, schemaFactory);
    }

    public JsonHelper(ObjectMapper mapper, SchemaFactoryWrapper schemaFactory) {
        if (mapper == null) {
            this.mapper = new ObjectMapper();
            this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        } else {
            this.mapper = mapper;
        }
        if (schemaFactory == null) {
            this.schemaFactory = new SchemaFactoryWrapper();
        } else {
            this.schemaFactory = schemaFactory;
        }
    }

    public JsonNode parse(String str) throws JsonParseException {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return mapper.readTree(str);
        } catch (JsonParseException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isStringValidJSON(String jsonString) {
        try {
            JsonNode jsonNode = parse(jsonString);
            return (jsonNode != null);
        } catch (Exception jsonEx) {
            return false;
        }
    }

    public SchemaHelper getSchemaHelper() {
        return schemaHelper;
    }

    public DataHelper getDataHelper() {
        return dataHelper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public class SchemaHelper {

        public String getSchemaString(Class<?> clazz) {
            return getSchemaString(clazz, null, null);
        }

        public String getSchemaString(Class<?> clazz, String title, String description) {
            try {
                String ret;
                mapper.acceptJsonFormatVisitor(mapper.constructType(clazz), schemaFactory);
                com.fasterxml.jackson.module.jsonSchema.JsonSchema finalSchema = schemaFactory.finalSchema();
                ret = mapper.writeValueAsString(finalSchema);
                if (ret != null && (title != null || description != null)) {
                    StringBuilder sb = new StringBuilder(ret.trim());
                    if (description != null) {
                        sb.insert(1, "\"description\":\"" + new String(JsonStringEncoder.getInstance().quoteAsUTF8(description)) + "\",");
                    }
                    if (title != null) {
                        sb.insert(1, "\"title\":\"" + new String(JsonStringEncoder.getInstance().quoteAsUTF8(title)) + "\",");
                    }
                    ret = sb.toString();
                }
                return ret;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        public void validate(String schema, String data) throws ValidationException, ProcessingException, JsonParseException {
            JsonSchema jsonSchema = getSchema(schema);
            JsonNode dataNode = parse(data);
            validate(jsonSchema, dataNode);
        }

        public void validate(JsonSchema schema, JsonNode dataNode) throws ValidationException, ProcessingException {
            ProcessingReport report = schema.validate(dataNode);
            if (!report.isSuccess()) {
                throw new ValidationException(report);
            }
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

        public JsonSchema getSchemaD3(String json) throws ProcessingException, JsonParseException {
            return getSchema(addDraftv3(json));
        }

        public JsonSchema getSchema(String json) throws ProcessingException, JsonParseException {
            return JsonSchemaFactory.byDefault().getJsonSchema(parse(json));
        }
    }

    public class DataHelper {

        public String transform(Object entity) throws JsonProcessingException {
            return transform(entity, false);
        }

        public String transform(Object entity, boolean prettyPrinted) throws JsonProcessingException {
            if (prettyPrinted) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entity);
            } else {
                return mapper.writeValueAsString(entity);
            }
        }

        public <E> E transform(String json, Class<E> clazz) throws JsonParseException, JsonMappingException {
            try {
                if (json == null || json.trim().isEmpty()) {
                    return null;
                }
                return mapper.readValue(json, clazz);
            } catch (JsonParseException ex) {
                throw ex;
            } catch (JsonMappingException ex) {
                throw ex;
            } catch (IOException ex) {
                throw new RuntimeException(json, ex);
            }
        }
    }
}