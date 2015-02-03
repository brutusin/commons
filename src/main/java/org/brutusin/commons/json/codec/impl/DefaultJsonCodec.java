/*
 * Copyright (c) 2011, DREAMgenics and/or its affiliates. All rights reserved.
 */
package org.brutusin.commons.json.codec.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.brutusin.commons.json.JsonSchema;
import org.brutusin.commons.json.ParseException;
import org.brutusin.commons.json.ValidationException;
import org.brutusin.commons.json.codec.JsonCodec;

/**
 * This JSON codec, uses Jackson, Jackson-Module-Schema and
 * com.github.fge:json-schema-validator
 *
 * @author Ignacio del Valle Alles idelvall@dreamgenics.com
 */
public class DefaultJsonCodec implements JsonCodec {

    private final ObjectMapper mapper;
    private final SchemaFactoryWrapper schemaFactory;

    public DefaultJsonCodec() {
        this(null, null);
    }

    public DefaultJsonCodec(ObjectMapper mapper, SchemaFactoryWrapper schemaFactory) {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        if (schemaFactory == null) {
            schemaFactory = new SchemaFactoryWrapper();
        }
        this.mapper = mapper;
        this.schemaFactory = schemaFactory;
    }

    public String transform(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T transform(String json, Class<T> clazz) throws ParseException {
        try {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return mapper.readValue(json, clazz);
        } catch (JsonParseException ex) {
            throw new ParseException(ex);
        } catch (JsonMappingException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public JsonSchema parseSchema(String json) throws ParseException {
        JsonNode node = parse(json);
        try {
            com.github.fge.jsonschema.main.JsonSchema jsonSchema = JsonSchemaFactory.byDefault().getJsonSchema(node);
            DefaultJsonCodec.JsonSchemaImpl ret = new JsonSchemaImpl();
            ret.setImpl(jsonSchema);
            return ret;
        } catch (ProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getSchema(Class clazz) {
        try {
            mapper.acceptJsonFormatVisitor(mapper.constructType(clazz), schemaFactory);
            com.fasterxml.jackson.module.jsonSchema.JsonSchema finalSchema = schemaFactory.finalSchema();
            return mapper.writeValueAsString(finalSchema);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void validate(JsonSchema schema, String json) throws ValidationException, ParseException {
        if (schema == null) {
            throw new IllegalArgumentException("schema cannot be null");
        }
        if (!(schema instanceof DefaultJsonCodec.JsonSchemaImpl)) {
            throw new IllegalArgumentException("schema has to be instance of " + DefaultJsonCodec.JsonSchemaImpl.class.getName());
        }
        com.github.fge.jsonschema.main.JsonSchema schemaImpl = ((DefaultJsonCodec.JsonSchemaImpl) schema).getImpl();
        ProcessingReport report = null;
        JsonNode node = parse(json);
        try {
            report = schemaImpl.validate(node);
        } catch (ProcessingException ex) {
            throw new ParseException(ex);
        }
        if (!report.isSuccess()) {
            Iterator<ProcessingMessage> iterator = report.iterator();
            List<String> messages = new ArrayList();
            while (iterator.hasNext()) {
                ProcessingMessage processingMessage = iterator.next();
                messages.add(processingMessage.getMessage());
            }
            throw new ValidationException(messages);
        }
    }

    public String quoteAsUTF8(String s) {
        return new String(JsonStringEncoder.getInstance().quoteAsUTF8(s));
    }

    private JsonNode parse(String json) throws ParseException {
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new ParseException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class JsonSchemaImpl implements JsonSchema {

        private com.github.fge.jsonschema.main.JsonSchema impl;

        public com.github.fge.jsonschema.main.JsonSchema getImpl() {
            return impl;
        }

        public void setImpl(com.github.fge.jsonschema.main.JsonSchema impl) {
            this.impl = impl;
        }
    }

}
