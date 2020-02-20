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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.skanders.rms.def.exception.RMSException;
import com.skanders.rms.def.verify.RMSVerify;
import com.skanders.rms.util.builder.ModelBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class RMSProperties
{
    private static final Logger LOG = LoggerFactory.getLogger(RMSProperties.class);

    private static final String PATH_DELIM = "\\.";
    private static final String ENCRYPTED_VALUE_LABEL = "enc=";

    private final JsonNode properties;
    private final StandardPBEStringEncryptor encryptor;

    private final boolean encrypted;

    private RMSProperties(JsonNode properties, StandardPBEStringEncryptor encryptor)
    {
        this.properties = properties;
        this.encryptor = encryptor;

        this.encrypted = this.encryptor != null;
    }

    public static RMSProperties fromPlain(@NotNull String propertiesFileName)
    {
        RMSVerify.checkNull(propertiesFileName, "propertiesFileName cannot be null");

        JsonNode properties = loadConfigProps(propertiesFileName);

        return new RMSProperties(properties, null);
    }

    public static RMSProperties fromEncrypted(@NotNull String propertiesFileName, @NotNull String algorithm, @NotNull String password)
    {
        RMSVerify.checkNull(propertiesFileName, "propertiesFileName cannot be null");
        RMSVerify.checkNull(algorithm, "algorithm cannot be null");
        RMSVerify.checkNull(password, "password cannot be null");

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        encryptor.setAlgorithm(algorithm);
        encryptor.setIvGenerator(new RandomIvGenerator());

        JsonNode properties = loadConfigProps(propertiesFileName);

        return new RMSProperties(properties, encryptor);
    }

    public static RMSProperties fromEncrypted(@NotNull String propertiesFileName, @NotNull String algorithm, @NotNull char[] passwordArr)
    {
        RMSVerify.checkNull(propertiesFileName, "propertiesFileName cannot be null");
        RMSVerify.checkNull(algorithm, "algorithm cannot be null");
        RMSVerify.checkNull(passwordArr, "passwordArr cannot be null");

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPasswordCharArray(passwordArr);
        encryptor.setAlgorithm(algorithm);
        encryptor.setIvGenerator(new RandomIvGenerator());

        JsonNode properties = loadConfigProps(propertiesFileName);

        return new RMSProperties(properties, encryptor);
    }

    private static JsonNode loadConfigProps(String propertiesFileName)
    {
        try {
            return ModelBuilder.getYamlMapper().readValue(new File(propertiesFileName), JsonNode.class);

        } catch (FileNotFoundException e) {
            throw new RMSException("Could not find Yaml File.");

        } catch (IOException e){
            throw new RMSException("Could not load Yaml File.");

        }
    }

    SSLType getSSLType()
    {
        String type = getStr("ssl.type");

        if (type == null) {
            checkIgnored("ssl");
            return SSLType.NONE;
        }

        return SSLType.getType(type);
    }

    DBType getDBType()
    {
        String type = getStr("db.type");

        if (type == null) {
            checkIgnored("db");
            return DBType.NONE;
        }

        return DBType.getType(type);
    }

    CORSType getCORSType()
    {
        String type = getStr("cors.type");

        if (type == null) {
            checkIgnored("cors");
            return CORSType.NONE;
        }

        return CORSType.getType(type);
    }

    private JsonNode getNode(String path)
    {
        JsonNode value = properties;

        for (String key : path.split(PATH_DELIM))
        {
            value = value.get(key);

            if (value == null)
                return null;
        }

        return value;
    }

    public Boolean getBool(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : value.asBoolean();
    }

    public Integer getInt(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : value.asInt();
    }

    public Long getLong(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : value.asLong();
    }

    public Double getDouble(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : value.asDouble();
    }

    public String getStr(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : checkEncrypted(value.asText());
    }

    public <T> ArrayList<T> getArray(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : ModelBuilder.getJsonMapper().convertValue(value, new TypeReference<ArrayList<T>>(){});
    }

    public <T,S> HashMap<T,S> getMap(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : ModelBuilder.getJsonMapper().convertValue(value, new TypeReference<HashMap<T,S>>(){});
    }

    public Boolean getBool(String path, Boolean defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : value.asBoolean();
    }

    public Integer getInt(String path, Integer defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : value.asInt();
    }

    public Long getLong(String path, Long defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : value.asLong();
    }

    public Double getDouble(String path, Double defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : value.asDouble();
    }

    public String getStr(String path, String defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : checkEncrypted(value.asText());
    }

    public Boolean getReqBool(String path)
    {
        Boolean value = getBool(path);

        requiredValue(path, value);

        return value;
    }

    public Integer getReqInt(String path)
    {
        Integer value = getInt(path);

        requiredValue(path, value);

        return value;
    }

    public Long getReqLong(String path)
    {
        Long value = getLong(path);

        requiredValue(path, value);

        return value;
    }

    public Double getReqDouble(String path)
    {
        Double value = getDouble(path);

        requiredValue(path, value);

        return value;
    }

    public String getReqStr(String path)
    {
        String value = getStr(path);

        requiredValue(path, value);

        return value;
    }

    public <T> ArrayList<T> getReqArray(String path)
    {
        ArrayList<T> value = getArray(path);

        requiredValue(path, value);

        return value;
    }

    public <T,S> HashMap<T,S> getReqMap(String path)
    {
        HashMap<T,S> value = getMap(path);

        requiredValue(path, value);

        return value;
    }

    private void requiredValue(String path, Object ob)
    {
        if (ob == null)
            throw new RMSException("Required path: '" + path + "' not found.");
    }


    private String checkEncrypted(String value)
    {
        if (value == null)
            return null;
        else if (encrypted && value.startsWith(ENCRYPTED_VALUE_LABEL))
            return encryptor.decrypt(value.substring(ENCRYPTED_VALUE_LABEL.length()));
        else
            return value;
    }

    void checkIgnored(String path)
    {
        JsonNode value = getNode(path);

        if (value != null)
            LOG.warn("Presence of ignored value: " + path);
    }
}
