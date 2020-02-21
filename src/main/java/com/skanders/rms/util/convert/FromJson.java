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

package com.skanders.rms.util.convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.skanders.rms.def.verify.RMSVerify;
import com.skanders.rms.util.result.RMSResult;
import com.skanders.rms.util.result.Resulted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Function;

public class FromJson
{
    private static final Logger LOG = LoggerFactory.getLogger(FromJson.class);

    private static final String PATH_DELIM = "\\.";

    public static <T> ArrayList<T> toArray(@Nonnull JsonNode node, String path, Function<JsonNode, T> getter)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        JsonNode value = toNode(node, path);

        return value == null ? null : toArray(value, getter);
    }

    public static <T> ArrayList<T> toArray(@Nonnull JsonNode node, Function<JsonNode, T> getter)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        ArrayList<T> list = new ArrayList<>();
        for (JsonNode item : node)
            list.add(getter.apply(item));

        if (list.isEmpty())
            LOG.trace("Could not convert to ArrayList");

        return list;
    }

    public static Resulted<String> toSafeString(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        Resulted<JsonNode> pathNode = toSafeNode(node, path);
        if (pathNode.notValid())
            return Resulted.inResulted(pathNode);

        String converted = toString(pathNode.value());

        return (converted == null) ?
                Resulted.inResult(RMSResult.FROM_JSON_NO_VALUE_FOUND) :
                Resulted.inValue(converted);
    }


    public static Resulted<Integer> toSafeInteger(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        Resulted<JsonNode> pathNode = toSafeNode(node, path);
        if (pathNode.notValid())
            return Resulted.inResulted(pathNode);

        Integer converted = toInteger(pathNode.value());

        return (converted == null) ?
                Resulted.inResult(RMSResult.FROM_JSON_NO_VALUE_FOUND) :
                Resulted.inValue(converted);
    }

    public static Resulted<Long> toSafeLong(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        Resulted<JsonNode> pathNode = toSafeNode(node, path);
        if (pathNode.notValid())
            return Resulted.inResulted(pathNode);

        Long converted = toLong(pathNode.value());

        return (converted == null) ?
                Resulted.inResult(RMSResult.FROM_JSON_NO_VALUE_FOUND) :
                Resulted.inValue(converted);
    }


    public static Resulted<Double> toSafeDouble(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        Resulted<JsonNode> pathNode = toSafeNode(node, path);
        if (pathNode.notValid())
            return Resulted.inResulted(pathNode);

        Double converted = toDouble(pathNode.value());

        return (converted == null) ?
                Resulted.inResult(RMSResult.FROM_JSON_NO_VALUE_FOUND) :
                Resulted.inValue(converted);
    }

    public static Resulted<BigDecimal> toSafeBigDecimal(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        Resulted<JsonNode> value = toSafeNode(node, path);
        if (value.notValid())
            return Resulted.inResulted(value);

        BigDecimal converted = toBigDecimal(value.value());

        return (converted == null) ?
                Resulted.inResult(RMSResult.FROM_JSON_NO_VALUE_FOUND) :
                Resulted.inValue(converted);
    }


    public static String toString(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        JsonNode value = toNode(node, path);

        return value == null ? null : toString(value);
    }


    public static Integer toInteger(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        JsonNode value = toNode(node, path);

        return value == null ? null : toInteger(value);
    }

    public static Long toLong(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        JsonNode value = toNode(node, path);

        return value == null ? null : toLong(value);
    }


    public static Double toDouble(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        JsonNode value = toNode(node, path);

        return value == null ? null : toDouble(value);
    }

    public static BigDecimal toBigDecimal(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        JsonNode value = toNode(node, path);

        return value == null ? null : toBigDecimal(value);
    }

    public static String toString(@Nonnull JsonNode node)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        String converted = node.isValueNode() ? node.asText() : null;

        if (converted == null)
            LOG.trace("Could not convert to String");

        return converted;
    }

    public static Integer toInteger(@Nonnull JsonNode node)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        Integer converted = node.isNumber() ? node.asInt() : null;

        if (converted == null)
            LOG.trace("Could not convert to Integer");

        return converted;
    }

    public static Long toLong(@Nonnull JsonNode node)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        Long converted = node.isNumber() ? node.asLong() : null;

        if (converted == null)
            LOG.trace("Could not convert to Long");

        return converted;
    }

    public static Double toDouble(@Nonnull JsonNode node)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        Double converted = node.isNumber() ? node.asDouble() : null;

        if (converted == null)
            LOG.trace("Could not convert to Double");

        return converted;
    }

    public static BigDecimal toBigDecimal(@Nonnull JsonNode node)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        BigDecimal converted = node.isNumber() ? BigDecimal.valueOf(node.asDouble()) : null;

        if (converted == null)
            LOG.trace("Could not convert to BigDecimal");

        return converted;
    }

    public static JsonNode toNode(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        String[] paths = path.split(PATH_DELIM);

        JsonNode target = node;

        for (String nodePath : paths) {
            target = target.path(nodePath);

            if (target.isMissingNode()) {
                LOG.trace("Could not find \'" + path + "\'");
                return null;
            }
        }

        return target;
    }

    public static Resulted<JsonNode> toSafeNode(@Nonnull JsonNode node, String path)
    {
        RMSVerify.checkNull(node, "node cannot be null");

        String[] paths = path.split(PATH_DELIM);

        JsonNode target = node;

        for (String nodePath : paths) {
            target = target.path(nodePath);

            if (target.isMissingNode()) {
                LOG.trace("Could not find \'" + path + "\'");
                return Resulted.inResult(RMSResult.FROM_JSON_NO_VALUE_FOUND);
            }
        }

        return Resulted.inValue(target);
    }
}
