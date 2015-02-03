/*
 * Copyright (c) 2011, DREAMgenics and/or its affiliates. All rights reserved.
 */
package org.brutusin.commons.json.codec;

import org.brutusin.commons.json.ParseException;

/**
 * Decouples application logic from json parsing implementation.
 * @author Ignacio del Valle Alles idelvall@dreamgenics.com
 */
public interface JsonDataCodec {
    
    public String quoteAsUTF8(String s);

    public <T> T transform(String json, Class<T> clazz) throws ParseException;

    public String transform(Object o);
}
