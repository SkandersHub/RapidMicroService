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


package com.skanders.rms.util.builder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.skanders.rms.base.result.RMSResult;
import com.skanders.rms.base.result.Result;
import com.skanders.rms.base.result.Resulted;
import com.skanders.rms.def.logger.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ModelBuilder
{
    private static final Logger LOG = LoggerFactory.getLogger(ModelBuilder.class);

    private static final ObjectMapper JSON_MAPPER;
    private static final ObjectMapper YAML_MAPPER;
    private static final ObjectMapper XML_MAPPER;

    static {
        JSON_MAPPER = new ObjectMapper();
        YAML_MAPPER = new ObjectMapper(new YAMLFactory());
        XML_MAPPER = new ObjectMapper(new XmlFactory());
    }

    public static ObjectMapper getJsonMapper()
    {
        return JSON_MAPPER;
    }

    public static ObjectMapper getYamlMapper()
    {
        return YAML_MAPPER;
    }

    public static ObjectMapper getXmlMapper()
    {
        return XML_MAPPER;
    }

    public static JsonNodeFactory getJsonFactory()
    {
        return JSON_MAPPER.getNodeFactory();
    }

    public static <T> Resulted<T> fromJson(
            InputStream jsonText, Class<T> className, DeserializationFeature... feature)
    {
        return fromInputStream(JSON_MAPPER, jsonText, className, feature);
    }

    public static <T> Resulted<T> fromJson(
            File jsonFile, Class<T> className, DeserializationFeature... feature)
    {
        return fromFile(JSON_MAPPER, jsonFile, className, feature);
    }

    public static <T> Resulted<T> fromJson(
            String jsonText, Class<T> className, DeserializationFeature... feature)
    {
        return fromString(JSON_MAPPER, jsonText, className, feature);
    }

    public static <T> Resulted<T> fromXml(
            InputStream xmlText, Class<T> className, DeserializationFeature... feature)
    {
        return fromInputStream(XML_MAPPER, xmlText, className, feature);
    }

    public static <T> Resulted<T> fromXml(
            File xmlFile, Class<T> className, DeserializationFeature... feature)
    {
        return fromFile(XML_MAPPER, xmlFile, className, feature);
    }

    public static <T> Resulted<T> fromXml(
            String xmlText, Class<T> className, DeserializationFeature... feature)
    {
        return fromString(XML_MAPPER, xmlText, className, feature);
    }

    public static <T> Resulted<T> fromYaml(
            InputStream yamlText, Class<T> className, DeserializationFeature... feature)
    {
        return fromInputStream(YAML_MAPPER, yamlText, className, feature);
    }

    public static <T> Resulted<T> fromYaml(
            File yamlFile, Class<T> className, DeserializationFeature... feature)
    {
        return fromFile(YAML_MAPPER, yamlFile, className, feature);
    }

    public static <T> Resulted<T> fromYaml(
            String yamlText, Class<T> className, DeserializationFeature... feature)
    {
        return fromString(YAML_MAPPER, yamlText, className, feature);
    }

    private static <T> Resulted<T> fromInputStream(
            ObjectMapper mapper, InputStream inputStream, Class<T> className, DeserializationFeature... feature)
    {
        try {
            ObjectReader reader = (feature == null || feature.length == 0) ?
                    mapper.readerFor(className) :
                    mapper.readerFor(className).withFeatures(feature);

            return Resulted.inValue(reader.readValue(inputStream));

        } catch (IOException e) {
            LOG.error(Pattern.ERROR, e.getClass(), e.getMessage());
            return Resulted.inResult(convert(e));

        }
    }

    private static <T> Resulted<T> fromFile(
            ObjectMapper mapper, File fileName, Class<T> className, DeserializationFeature... feature)
    {
        try {
            ObjectReader reader = (feature == null || feature.length == 0) ?
                    mapper.readerFor(className) :
                    mapper.readerFor(className).withFeatures(feature);

            return Resulted.inValue(reader.readValue(fileName));

        } catch (IOException e) {
            LOG.error(Pattern.ERROR, e.getClass(), e.getMessage());
            return Resulted.inResult(convert(e));

        }
    }

    private static <T> Resulted<T> fromString(
            ObjectMapper mapper, String text, Class<T> className, DeserializationFeature... feature)
    {
        try {
            ObjectReader reader = (feature == null || feature.length == 0) ?
                    mapper.readerFor(className) :
                    mapper.readerFor(className).withFeatures(feature);

            return Resulted.inValue(reader.readValue(text));

        } catch (IOException e) {
            LOG.error(Pattern.ERROR, e.getClass(), e.getMessage());
            return Resulted.inResult(convert(e));

        }
    }

    private static Result convert(Exception e)
    {
        if (e instanceof JsonMappingException) {
            return RMSResult.JSON_MAPPING_EXCEPT;

        } else if (e instanceof JsonParseException) {
            return RMSResult.JSON_PARSE_EXCEPT;

        } else {
            return Result.exception(e);

        }
    }
}
