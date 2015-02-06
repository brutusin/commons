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
package org.brutusin.commons.json.codec.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchema.factories.FormatVisitorFactory;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchema.types.AnySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.BooleanSchema;
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchema.types.NullSchema;
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
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
 * @author Ignacio del Valle Alles idelvall@brutusin.org
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
            schemaFactory = new JsonsrvFactoryWrapper();
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
        if (!(schema instanceof DefaultJsonCodec.JsonSchemaImpl)) {
            throw new IllegalArgumentException("schema has to be instance of " + DefaultJsonCodec.JsonSchemaImpl.class.getName());
        }
        com.github.fge.jsonschema.main.JsonSchema schemaImpl = ((DefaultJsonCodec.JsonSchemaImpl) schema).getImpl();
        ProcessingReport report = null;
        try {
            report = schemaImpl.validate(parse(json));
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

    /**
     * @author Ignacio del Valle Alles idelvall@brutusin.org
     */
    private static class JsonSchemaImpl implements JsonSchema {

        private com.github.fge.jsonschema.main.JsonSchema impl;

        public com.github.fge.jsonschema.main.JsonSchema getImpl() {
            return impl;
        }

        public void setImpl(com.github.fge.jsonschema.main.JsonSchema impl) {
            this.impl = impl;
        }
    }

    /**
     * @author Ignacio del Valle Alles idelvall@brutusin.org
     */
    private static class JsonsrvFactoryWrapper extends SchemaFactoryWrapper {

        private final WrapperFactory wrapperFactory = new WrapperFactory() {
            @Override
            public SchemaFactoryWrapper getWrapper(SerializerProvider p) {
                return new JsonsrvFactoryWrapper(p);
            }

            @Override
            public SchemaFactoryWrapper getWrapper(SerializerProvider provider, VisitorContext rvc) {
                JsonsrvFactoryWrapper wrapper = new JsonsrvFactoryWrapper(provider);
                wrapper.setVisitorContext(rvc);
                return wrapper;
            }

        };

        public JsonsrvFactoryWrapper() {
            this(null);
        }

        public JsonsrvFactoryWrapper(SerializerProvider p) {
            super(p);
            visitorFactory = new FormatVisitorFactory(wrapperFactory);
            schemaProvider = new JsonsrvSchemaFactory();
        }

    }

    /**
     * @author Ignacio del Valle Alles idelvall@brutusin.org
     */
    private static class JsonsrvSchemaFactory extends com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory {

        void enrich(SimpleTypeSchema schema, BeanProperty beanProperty) {
            schema.setTitle(beanProperty.getName());
        }

        @Override
        public AnySchema anySchema() {
            return new AnySchema() {
                @Override
                public void enrichWithBeanProperty(BeanProperty beanProperty) {
                    enrich(this, beanProperty);
                }
            };
        }

        @Override
        public ArraySchema arraySchema() {
            return new ArraySchema() {
                @Override
                public void enrichWithBeanProperty(BeanProperty beanProperty) {
                    enrich(this, beanProperty);
                }
            };
        }

        @Override
        public BooleanSchema booleanSchema() {
            return new BooleanSchema() {
                @Override
                public void enrichWithBeanProperty(BeanProperty beanProperty) {
                    enrich(this, beanProperty);
                }
            };
        }

        @Override
        public IntegerSchema integerSchema() {
            return new IntegerSchema() {
                @Override
                public void enrichWithBeanProperty(BeanProperty beanProperty) {
                    enrich(this, beanProperty);
                }
            };
        }

        @Override
        public NullSchema nullSchema() {
            return new NullSchema() {
                @Override
                public void enrichWithBeanProperty(BeanProperty beanProperty) {
                    enrich(this, beanProperty);
                }
            };
        }

        @Override
        public NumberSchema numberSchema() {
            return new NumberSchema() {
                @Override
                public void enrichWithBeanProperty(BeanProperty beanProperty) {
                    enrich(this, beanProperty);
                }
            };
        }

        @Override
        public ObjectSchema objectSchema() {
            return new ObjectSchema() {
                @Override
                public void enrichWithBeanProperty(BeanProperty beanProperty) {
                    enrich(this, beanProperty);
                }
            };
        }

        @Override
        public StringSchema stringSchema() {
            return new StringSchema() {
                @Override
                public void enrichWithBeanProperty(BeanProperty beanProperty) {
                    enrich(this, beanProperty);
                }
            };
        }
    }

}
