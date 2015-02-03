/*
 * Copyright (c) 2011, DREAMgenics and/or its affiliates. All rights reserved.
 */
package org.brutusin.commons.json.codec;

import org.brutusin.commons.json.JsonSchema;
import org.brutusin.commons.json.ParseException;
import org.brutusin.commons.json.ValidationException;

/**
 * Decouples application logic from json parsing implementation.
 * @author Ignacio del Valle Alles idelvall@dreamgenics.com
 */
public interface JsonSchemaCodec {
    
    public String getSchema(Class clazz);
    
    public JsonSchema parseSchema(String json) throws ParseException;

    public void validate(JsonSchema schema, String data) throws ValidationException, ParseException;
}
