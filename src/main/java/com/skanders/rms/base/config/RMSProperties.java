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
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.skanders.rms.def.exception.RMSException;
import com.skanders.rms.def.verify.RMSVerify;
import com.skanders.rms.util.builder.ModelBuilder;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A manager that extracts required, optional, and encrypted values from a
 * config file in YAML format.
 * <p>
 * When stating a value to 'get' from the yaml, a dot delimited string must be
 * given to state its location.
 * <p>
 * For example:
 * <pre>
 * key:
 *     value:
 *         var1: "one"
 * </pre>
 * To 'get' var one as a string the function call must be {@code
 * getStr("key.value.var1")}
 * <p>
 * Required values are marked by using the getReq...(value) functions. If a
 * required value is not found then an exception will be thrown with a message
 * indicating the missing value.
 * <p>
 * Optional values can be missing and are marked by using the get...(value)
 * functions. Since optional values can be missing a value not found will be
 * returned as null or, if marked using the get...(value, default) functions,
 * will be returned as the default value.
 * <p>
 * Encrypted values can only be strings and must be designated by having its raw
 * value start with the label 'enc='. The value will automaticly be decrypted if
 * both, the value starts with the label AND RMSProperties was created using the
 * {@link #fromEncrypted(String, String, String)} or {@link
 * #fromEncrypted(String, String, char[])} functions.
 * <p>
 * This is in a similar manner as JASYPT's encrypted properties file but made to
 * work with YAML files instead of properties files in order to allow more
 * organized config files as well as array and hashmap extraction. The
 * difference however is in the label: 'enc=' Alone designates a value as
 * encrypted and will cause the value to be given to {@link
 * StandardPBEStringEncryptor} to be decrypted.
 */
public class RMSProperties
{
    private static final Logger LOG = LoggerFactory.getLogger(RMSProperties.class);

    private static final String PATH_DELIM = "\\.";
    private static final String ENCRYPTED_VALUE_LABEL = "enc=";

    private final JsonNode properties;
    private final StandardPBEStringEncryptor encryptor;

    private final boolean encrypted;

    /**
     * Static builder that reads all yaml values as 'plain' values and will skip
     * checking for encrypted values
     *
     * @param propertiesFileName a string corresponding to the config yaml file
     *                           location
     * @return an instance of RMSProperties
     */
    public static RMSProperties fromPlain(@Nonnull String propertiesFileName)
    {
        RMSVerify.checkNull(propertiesFileName, "propertiesFileName cannot be null");

        JsonNode properties = loadConfigProps(propertiesFileName);

        return new RMSProperties(properties, null);
    }

    /**
     * Static builder that will check all strings for encrypted values and
     * automatically convert them.
     *
     * @param propertiesFileName a string corresponding to the config yaml file
     *                           location
     * @param algorithm          encryption algorithm to decrypt values
     * @param password           password to decrypt values
     * @return an instance of RMSProperties
     */
    public static RMSProperties fromEncrypted(@Nonnull String propertiesFileName, @Nonnull String algorithm, @Nonnull String password)
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

    /**
     * Static builder that will check all strings for encrypted values and
     * automatically convert them.
     *
     * @param propertiesFileName a string corresponding to the config yaml file
     *                           location
     * @param algorithm          encryption algorithm to decrypt values
     * @param passwordArr        password to decrypt values in a char array
     * @return an instance of RMSProperties
     */
    public static RMSProperties fromEncrypted(@Nonnull String propertiesFileName, @Nonnull String algorithm, @Nonnull char[] passwordArr)
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

    /**
     * Private constructor to be called from static builders
     *
     * @param properties an instance of JsonNode modeled from yaml file
     * @param encryptor  an instance of StandardPBEStringEncryptor created with
     *                   the user given algorithm and password
     */
    private RMSProperties(JsonNode properties, StandardPBEStringEncryptor encryptor)
    {
        this.properties = properties;
        this.encryptor = encryptor;

        this.encrypted = this.encryptor != null;
    }

    /**
     * Builds a JsonNode instance from the given config file
     *
     * @param propertiesFileName a string corresponding to the config yaml file
     *                           location
     * @return an instance of JsonNode modeled from yaml file
     */
    private static JsonNode loadConfigProps(String propertiesFileName)
    {
        try {
            return ModelBuilder.getYamlMapper().readValue(new File(propertiesFileName), JsonNode.class);

        } catch (FileNotFoundException e) {
            throw new RMSException("Could not find Yaml File.");

        } catch (IOException e) {
            throw new RMSException("Could not load Yaml File.");

        }
    }

    /**
     * @return an enum representing the users declared SSL Type
     */
    SSLType getSSLType()
    {
        String type = getStr("ssl.type");

        if (type == null) {
            checkIgnored("ssl");
            return SSLType.NONE;
        }

        return SSLType.getType(type);
    }

    /**
     * @return an enum representing the users declared DB Type
     */
    DBType getDBType()
    {
        String type = getStr("db.type");

        if (type == null) {
            checkIgnored("db");
            return DBType.NONE;
        }

        return DBType.getType(type);
    }

    /**
     * @return an enum representing the users declared CORS Type
     */
    CORSType getCORSType()
    {
        String type = getStr("cors.type");

        if (type == null) {
            checkIgnored("cors");
            return CORSType.NONE;
        }

        return CORSType.getType(type);
    }

    /**
     * Checks if the given path exists and issues a warning as a ignored value
     *
     * @param path dot delimited path to value in yaml
     */
    void checkIgnored(String path)
    {
        JsonNode value = getNode(path);

        if (value != null)
            LOG.warn("Presence of ignored value: " + path);
    }

    /**
     * Retrieves the optional value as a Boolean
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a Boolean, or null if not found
     */
    public Boolean getBool(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : value.asBoolean();
    }

    /**
     * Retrieves the optional value as a Integer
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a Integer, or null if not found
     */
    public Integer getInt(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : value.asInt();
    }

    /**
     * Retrieves the optional value as a Long
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a Long, or null if not found
     */
    public Long getLong(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : value.asLong();
    }

    /**
     * Retrieves the optional value as a Double
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a Double, or null if not found
     */
    public Double getDouble(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : value.asDouble();
    }

    /**
     * Retrieves the optional value as a String
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a String, or null if not found
     */
    public String getStr(String path)
    {
        JsonNode value = getNode(path);

        return value == null ? null : checkEncrypted(value.asText());
    }

    /**
     * Retrieves the optional value as a Boolean
     *
     * @param path         dot delimited path to value in yaml
     * @param defaultValue value to be returned if path is not found
     * @return the value as a Boolean, or defaultValue if not found
     */
    public Boolean getBool(String path, Boolean defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : value.asBoolean();
    }

    /**
     * Retrieves the optional value as a Integer
     *
     * @param path         dot delimited path to value in yaml
     * @param defaultValue value to be returned if path is not found
     * @return the value as a Integer, or defaultValue if not found
     */
    public Integer getInt(String path, Integer defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : value.asInt();
    }

    /**
     * Retrieves the optional value as a Long
     *
     * @param path         dot delimited path to value in yaml
     * @param defaultValue value to be returned if path is not found
     * @return the value as a Long, or defaultValue if not found
     */
    public Long getLong(String path, Long defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : value.asLong();
    }

    /**
     * Retrieves the optional value as a Double
     *
     * @param path         dot delimited path to value in yaml
     * @param defaultValue value to be returned if path is not found
     * @return the value as a Double, or defaultValue if not found
     */
    public Double getDouble(String path, Double defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : value.asDouble();
    }

    /**
     * Retrieves the optional value as a String
     *
     * @param path         dot delimited path to value in yaml
     * @param defaultValue value to be returned if path is not found
     * @return the value as a String, or defaultValue if not found
     */
    public String getStr(String path, String defaultValue)
    {
        JsonNode value = getNode(path);

        return value == null ? defaultValue : checkEncrypted(value.asText());
    }

    /**
     * Retrieves the required value as a Boolean
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a Boolean
     * @throws RMSException if the path is not found
     */
    public Boolean getReqBool(String path)
    {
        Boolean value = getBool(path);

        requiredValue(path, value);

        return value;
    }

    /**
     * Retrieves the required value as a Integer
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a Integer
     * @throws RMSException if the path is not found
     */
    public Integer getReqInt(String path)
    {
        Integer value = getInt(path);

        requiredValue(path, value);

        return value;
    }

    /**
     * Retrieves the required value as a Long
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a Long
     * @throws RMSException if the path is not found
     */
    public Long getReqLong(String path)
    {
        Long value = getLong(path);

        requiredValue(path, value);

        return value;
    }

    /**
     * Retrieves the required value as a Double
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a Double
     * @throws RMSException if the path is not found
     */
    public Double getReqDouble(String path)
    {
        Double value = getDouble(path);

        requiredValue(path, value);

        return value;
    }

    /**
     * Retrieves the required value as a String
     *
     * @param path dot delimited path to value in yaml
     * @return the value as a String
     * @throws RMSException if the path is not found
     */
    public String getReqStr(String path)
    {
        String value = getStr(path);

        requiredValue(path, value);

        return value;
    }

    /**
     * Retrieves the optional value as an ArrayList
     *
     * @param path      dot delimited path to value in yaml
     * @param classType array class type
     * @param <T>       ArrayList type to be determined by storing value
     * @return the value as an ArrayList, or null if not found
     */
    public <T> ArrayList<T> getArray(String path, Class<T> classType)
    {
        JsonNode value = getNode(path);

        if (value == null)
            return null;

        CollectionType type = ModelBuilder.getJsonMapper().getTypeFactory().constructCollectionType(ArrayList.class, classType);

        return ModelBuilder.getJsonMapper().convertValue(value, type);
    }

    /**
     * Retrieves the required value as an ArrayList
     *
     * @param path      dot delimited path to value in yaml
     * @param classType array class type
     * @param <T>       ArrayList type to be determined by storing value
     * @return the value as an ArrayList
     * @throws RMSException if the path is not found
     */
    public <T> ArrayList<T> getReqArray(String path, Class<T> classType)
    {
        ArrayList<T> value = getArray(path, classType);

        requiredValue(path, value);

        return value;
    }

    /**
     * Retrieves the optional value as a HashMap
     *
     * @param path       dot delimited path to value in yaml
     * @param keyClass   key class type
     * @param valueClass value class type
     * @param <T>        HashMap key type to be determined by storing value
     * @param <S>        HashMap value type to be determined by storing value
     * @return the value as a HashMap, or null if not found
     */
    public <T, S> HashMap<T, S> getMap(String path, Class<T> keyClass, Class<S> valueClass)
    {
        JsonNode value = getNode(path);

        if (value == null)
            return null;

        MapType type = ModelBuilder.getJsonMapper().getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);

        return ModelBuilder.getJsonMapper().convertValue(value, type);
    }

    /**
     * Retrieves the required value as a HashMap
     *
     * @param path       dot delimited path to value in yaml
     * @param keyClass   key class type
     * @param valueClass value class type
     * @param <T>        HashMap key type to be determined by storing value
     * @param <S>        HashMap value type to be determined by storing value
     * @return the value as a HashMap
     * @throws RMSException if the path is not found
     */
    public <T, S> HashMap<T, S> getReqMap(String path, Class<T> keyClass, Class<S> valueClass)
    {
        HashMap<T, S> value = getMap(path, keyClass, valueClass);

        requiredValue(path, value);

        return value;
    }

    /**
     * Retrieves the path as a JsonNode
     *
     * @param path dot delimited path to value in yaml
     * @return an instance of JsonNode, or null if not found
     */
    private JsonNode getNode(String path)
    {
        JsonNode value = properties;

        for (String key : path.split(PATH_DELIM)) {
            value = value.get(key);

            if (value == null)
                return null;
        }

        return value;
    }

    /**
     * Checks the value for encryption label. If labeled and RMSProperties is
     * labeled as a encrypted properties instance then will automatically
     * decrypt the value.
     * <p>
     * If the label is missing or if RMSProperties is not labeled as a encrypted
     * properties instance, then the value will just be returned.
     *
     * @param value value to check
     * @return the decrypted value or original value if no encryption options
     * available
     */
    private String checkEncrypted(String value)
    {
        if (value == null)
            return null;
        else if (encrypted && value.startsWith(ENCRYPTED_VALUE_LABEL))
            return encryptor.decrypt(value.substring(ENCRYPTED_VALUE_LABEL.length()));
        else
            return value;
    }

    /**
     * Checks if a value is missing, if it is a Error is thrown with a mesage
     * stating which path was not found.
     *
     * @param path dot delimited path to value in yaml
     * @param ob   object to check
     */
    private void requiredValue(String path, Object ob)
    {
        if (ob == null)
            throw new RMSException("Required path: '" + path + "' not found.");
    }
}
