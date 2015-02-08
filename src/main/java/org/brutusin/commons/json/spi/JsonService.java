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
package org.brutusin.commons.json.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Decouples application logic from JSON parsing providers.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public abstract class JsonService implements JsonDataService, JsonSchemaService {

    private static JsonService instance;

    static {
        ServiceLoader<JsonService> sl = ServiceLoader.load(JsonService.class);
        Iterator<JsonService> it = sl.iterator();
        List<JsonService> instances = new ArrayList<JsonService>();
        while (it.hasNext()) {
            instances.add(it.next());
        }
        if (instances.size() == 0) {
            throw new Error("No '" + JsonService.class.getSimpleName() + "' service provider found.");
        } else if (instances.size() > 1) {
            throw new Error("Multiple '" + JsonService.class.getSimpleName() + "' service providers found: " + instances);
        } else {
            instance = instances.get(0);
        }
    }

    public static JsonService getInstance() {
        return instance;
    }
}
