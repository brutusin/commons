/*
 * Copyright 2016 Ignacio del Valle Alles idelvall@brutusin.org.
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
package org.brutusin.commons;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class ParameterizedType implements java.lang.reflect.ParameterizedType {

    private final Type rawType;
    private final Type[] actualTypeArguments;
    private Type ownerType;

    public ParameterizedType(Type... types) {
        if (types == null || types.length < 2) {
            throw new IllegalArgumentException("At list two types are requierd. The first for the raw type, and the rest for the actual type arguments");
        }
        this.rawType = types[0];
        this.actualTypeArguments = Arrays.copyOfRange(types, 1, types.length);
    }

    public Type[] getActualTypeArguments() {
        return this.actualTypeArguments;
    }

    public Type getRawType() {
        return this.rawType;
    }

    public Type getOwnerType() {
        return this.ownerType;
    }

    public void setOwnerType(Type ownerType) {
        this.ownerType = ownerType;
    }
}
