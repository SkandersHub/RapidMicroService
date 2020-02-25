/*
 * Copyright (c) 2020 Alexander Iskander
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

package com.skanders.rms.base.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.Iterator;
import java.util.Map;

class JsonNodeParser
{
    private static final String ENCRYPTED_VALUE_LABEL = "enc=";

    public static void decryptNodes(JsonNode nodes, StandardPBEStringEncryptor encryptor)
    {
        switch (nodes.getNodeType()) {
            case ARRAY:
                parseArray((ArrayNode) nodes, encryptor);
                break;

            case OBJECT:
                parseObject(nodes, encryptor);
                break;

            default:
                // continue
        }
    }

    private static void parseObject(JsonNode nodes, StandardPBEStringEncryptor encryptor)
    {
        Iterator<Map.Entry<String, JsonNode>> it = nodes.fields();

        while (it.hasNext()) {
            Map.Entry<String, JsonNode> node = it.next();

            switch (node.getValue().getNodeType()) {
                case ARRAY:
                    parseArray((ArrayNode) node.getValue(), encryptor);
                    break;

                case OBJECT:
                    parseObject(node.getValue(), encryptor);
                    break;

                case STRING:
                    String value = decrypt(node.getValue(), encryptor);

                    if (value != null)
                        ((ObjectNode) node.getValue()).put(node.getKey(), value);

                    break;

                default:
                    // continue
            }
        }
    }

    private static void parseArray(ArrayNode arrayNode, StandardPBEStringEncryptor encryptor)
    {
        int i = 0;
        int size = arrayNode.size();

        while (i < size) {
            switch (arrayNode.get(i).getNodeType()) {
                case ARRAY:
                    parseArray((ArrayNode) arrayNode.get(i), encryptor);
                    break;

                case OBJECT:
                    parseObject(arrayNode.get(i), encryptor);
                    break;

                case STRING:
                    String value = decrypt(arrayNode.get(i), encryptor);

                    if (value != null) {
                        arrayNode.remove(i);
                        arrayNode.add(value);
                        size--;
                    }
                    continue;

                default:
                    //continue
            }

            i++;
        }
    }

    /**
     * Checks the value for encryption label. If labeled and RMSProperties is
     * labeled as a encrypted properties instance then will automatically
     * decrypt the value.
     * <p>
     * If the label is missing or if RMSProperties is not labeled as a encrypted
     * properties instance, then the value will just be returned.
     *
     * @param node      value to check
     * @param encryptor a StandardPBEStringEcnryptor instance
     * @return the decrypted value or original value if no encryption options
     * available
     */
    private static String decrypt(JsonNode node, StandardPBEStringEncryptor encryptor)
    {
        if (!node.isTextual())
            return null;

        String value = node.asText();

        if (value != null && value.startsWith(ENCRYPTED_VALUE_LABEL))
            return encryptor.decrypt(value.substring(ENCRYPTED_VALUE_LABEL.length()));
        else
            return null;
    }
}
