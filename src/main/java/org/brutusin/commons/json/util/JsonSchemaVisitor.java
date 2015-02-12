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
package org.brutusin.commons.json.util;

import java.util.Iterator;
import org.brutusin.commons.json.spi.JsonNode;

/**
 * JsonSchemaVisitor instances allow implement functionality for each subschema in
 root schema, without concerns on structure navigation.
 * <br> <br>
 * Example:
 * <pre>
 *{@code
 * String schema = "{\"type\":\"object\",\"properties\":{\"map\":{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\"}},\"firstName\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"aaa\":{\"type\":\"string\"},\"bbb\":{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\"}}}}},\"middleName\":{\"type\":\"string\"},\"lastName\":{\"type\":\"string\"}},\"required\":[\"firstName\",\"lastName\"],\"additionalProperties\":false}";
 JsonNode schemaNode = JsonCodec.getInstance().parse(schema);
 JsonSchemaVisitor.accept(schemaNode, new JsonSchemaVisitor() {
     public void visit(String name, JsonNode subSchema) {
         System.out.println(name + ": " + schema.get("type").asString());
     }
 });
 }</pre>
 * <br>
 * Produces the following output:
 * <pre>
 *{@code
 * .: object
 * map: object
 * map[*]: string
 * firstName: array
 * firstName[#]: object
 * firstName[#].aaa: string
 * firstName[#].bbb: object
 * firstName[#].bbb[*]: string
 * middleName: string
 * lastName: string
 * }
 * </pre>
 * Note that "array" schemas and "object" schemas with "additionalProperties" produce names with [#] and [*] respectively, in order to represent its nature in the name itself.
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public abstract class JsonSchemaVisitor {

    /**
     * Called for each subschema in the accepted schema
     *
     * @param name
     * @param subSchema
     */
    public abstract void visit(String name, JsonNode subSchema);

    /**
     * Starts visitor notification, from the specified schema as root schema.
     *
     * @param schema
     * @param visitor
     */
    public static void accept(JsonNode rootSchema, JsonSchemaVisitor visitor) {
        accept("", rootSchema, visitor);
    }

    /**
     * Helper method for recursive processing
     *
     * @param name
     * @param schema
     * @param visitor
     */
    private static void accept(String name, JsonNode schema, JsonSchemaVisitor visitor) {
        if (schema == null) {
            return;
        }
        if (schema.getNodeType() == JsonNode.Type.OBJECT) {
            JsonNode typeNode = schema.get("type");
            if (typeNode != null && typeNode.getNodeType() == JsonNode.Type.STRING) {
                String type = typeNode.asString();
                visitor.visit((name.isEmpty() ? "." : name), schema);
                if ("object".equals(type)) {
                    JsonNode propertiesNode = schema.get("properties");
                    if (propertiesNode != null && propertiesNode.getNodeType() == JsonNode.Type.OBJECT) {
                        Iterator<String> ps = propertiesNode.getProperties();
                        while (ps.hasNext()) {
                            String prop = ps.next();
                            accept(name + (name.isEmpty() ? "" : ".") + prop, propertiesNode.get(prop), visitor);
                        }
                    }
                    JsonNode additionalPropertiesNode = schema.get("additionalProperties");
                    if (additionalPropertiesNode != null && additionalPropertiesNode.getNodeType() == JsonNode.Type.OBJECT) {
                        accept(name + "[*]", additionalPropertiesNode, visitor);
                    }
                } else if ("array".equals(type)) {
                    JsonNode itemsNode = schema.get("items");
                    if (itemsNode != null && itemsNode.getNodeType() == JsonNode.Type.OBJECT) {
                        accept(name + "[#]", itemsNode, visitor);
                    }
                }
            }
        }
    }
}
