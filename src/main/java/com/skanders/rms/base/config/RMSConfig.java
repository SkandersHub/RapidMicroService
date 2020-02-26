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

import com.skanders.rms.def.verify.RMSVerify;

import javax.annotation.Nonnull;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration object for RapidMicroService to set its initial state.
 * <p>
 * The values are mapped from {@link RMSProperties} and as such RMSConfig be
 * constructed by giving it an instance of RMSProperties
 * <p>
 * This should be extended when more config options are needed but when
 * extending the super MUST be called and given an the instance of RMSProperties
 * in order to set its base settings
 */
public class RMSConfig
{
    private String uriScheme;
    private String uriHostName;
    private Integer uriPort;
    private String uriPath;


    private String sslKeyStoreFile;
    private String sslKeyStorePass;
    private String sslTrustStoreFile;
    private String sslTrustStorePass;


    private String dbUrl;
    private String dbDriver;
    private String dbHostname;
    private Integer dbPort;
    private String dbName;
    private String dbUsername;
    private String dbPassword;
    private Long dbMaxLifetime;
    private Integer dbMaxPoolSize;
    private HashMap<String, Object> dbProperties;


    private String corsOrigin;
    private String corsMethods;
    private String corsHeaders;
    private String corsExposeHeaders;
    private String corsCredentials;
    private String corsMaxAge;


    private DBType dbType;
    private SSLType sslType;
    private CORSType corsType;


    /**
     * Constructor for RMSConfig
     *
     * @param prop an instance of RMSProperties
     */
    public RMSConfig(@Nonnull RMSProperties prop)
    {
        RMSVerify.checkNull(prop, "prop cannot be null");

        setUriConfigs(prop);
        setSSLEngineConfig(prop);
        setDatabaseConfig(prop);
        setCORSConfig(prop);
    }

    /**
     * Sets RMS's URI settings
     *
     * @param prop an instance of RMSProperties
     */
    private void setUriConfigs(@Nonnull RMSProperties prop)
    {
        uriScheme = prop.getReqStr("uri.scheme");
        uriHostName = prop.getReqStr("uri.hostname");
        uriPort = prop.getReqInt("uri.port");
        uriPath = prop.getReqStr("uri.path");
    }

    /**
     * Sets RMS's SSL settings
     *
     * @param prop an instance of RMSProperties
     */
    private void setSSLEngineConfig(@Nonnull RMSProperties prop)
    {
        if ((sslType = prop.getSSLType()) == SSLType.NONE)
            return;

        if (isKeyStore())
            setKeyStore(prop);

        if (isTrustStore())
            setTrustStore(prop);
    }

    /**
     * Sets the keystore values
     *
     * @param prop an instance of RMSProperties
     */
    private void setKeyStore(@Nonnull RMSProperties prop)
    {
        sslKeyStoreFile = prop.getReqStr("ssl.keyStoreFile");
        sslKeyStorePass = prop.getReqStr("ssl.keyStorePass");

        if (sslType == SSLType.FULL)
            return;

        prop.checkIgnored("ssl.trustStoreFile");
        prop.checkIgnored("ssl.trustStorePass");
    }

    /**
     * Sets the truststore values
     *
     * @param prop an instance of RMSProperties
     */
    private void setTrustStore(@Nonnull RMSProperties prop)
    {
        sslTrustStoreFile = prop.getReqStr("ssl.trustStoreFile");
        sslTrustStorePass = prop.getReqStr("ssl.trustStorePass");

        if (sslType == SSLType.FULL)
            return;

        prop.checkIgnored("ssl.keyStoreFile");
        prop.checkIgnored("ssl.keyStorePass");
    }

    /**
     * Sets RMS's Database settings
     *
     * @param prop an instance of RMSProperties
     */
    private void setDatabaseConfig(@Nonnull RMSProperties prop)
    {
        if ((dbType = prop.getDBType()) == DBType.NONE)
            return;

        if (dbType == DBType.URL)
            setDBUrl(prop);
        else
            setDbDriver(prop);

        dbUsername = prop.getReqStr("db.username");
        dbPassword = prop.getReqStr("db.password");
        dbMaxLifetime = prop.getReqLong("db.maxLifetime");
        dbMaxPoolSize = prop.getReqInt("db.maxPoolSize");

        Map<String, Object> dpProp = prop.getMap("db.properties", String.class, Object.class);

        if (dpProp != null)
            dbProperties = new HashMap<>(dpProp);
    }

    /**
     * Sets the Database Settings corresponding to a driver config.
     *
     * @param prop an instance of RMSProperties
     */
    private void setDbDriver(@Nonnull RMSProperties prop)
    {
        dbDriver = prop.getReqStr("db.driver");
        dbHostname = prop.getReqStr("db.hostname");
        dbPort = prop.getReqInt("db.port");
        dbName = prop.getReqStr("db.name");

        prop.checkIgnored("db.url");
    }

    /**
     * Sets the Database Settings corresponding to a url config.
     *
     * @param prop an instance of RMSProperties
     */
    private void setDBUrl(@Nonnull RMSProperties prop)
    {
        dbUrl = prop.getReqStr("db.url");

        prop.checkIgnored("db.driver");
        prop.checkIgnored("db.hostname");
        prop.checkIgnored("db.port");
        prop.checkIgnored("db.name");
    }

    /**
     * Sets RMS's CORS option
     *
     * @param prop an instance of RMSProperties
     */
    private void setCORSConfig(@Nonnull RMSProperties prop)
    {
        if ((corsType = prop.getCORSType()) == CORSType.NONE)
            return;

        corsOrigin = prop.getReqStr("cors.origin");
        corsMethods = prop.getReqStr("cors.methods");
        corsHeaders = prop.getReqStr("cors.headers");

        corsExposeHeaders = prop.getStr("cors.exposeHeaders");

        corsCredentials = prop.getStr("cors.credentials");
        corsMaxAge = prop.getStr("cors.maxAge");
    }

    /**
     * @return SSL keystore file location
     */
    public String getSslKeyStoreFile()
    {
        return sslKeyStoreFile;
    }

    /**
     * @return SSL keystore file pass
     */
    public String getSslKeyStorePass()
    {
        return sslKeyStorePass;
    }

    /**
     * @return SSL truststore file location
     */
    public String getSslTrustStoreFile()
    {
        return sslTrustStoreFile;
    }

    /**
     * @return SSL truststore file pass
     */
    public String getSslTrustStorePass()
    {
        return sslTrustStorePass;
    }

    /**
     * @return Database url
     */
    public String getDbUrl()
    {
        return dbUrl;
    }

    /**
     * @return Database driver
     */
    public String getDbDriver()
    {
        return dbDriver;
    }

    /**
     * @return Database hostname
     */
    public String getDbHostname()
    {
        return dbHostname;
    }

    /**
     * @return Database port
     */
    public Integer getDbPort()
    {
        return dbPort;
    }

    /**
     * @return Database name
     */
    public String getDbName()
    {
        return dbName;
    }

    /**
     * @return Database url
     */
    public String getDbUsername()
    {
        return dbUsername;
    }

    /**
     * @return Database password
     */
    public String getDbPassword()
    {
        return dbPassword;
    }

    /**
     * @return Database connection max lifetime for HikariCP
     */
    public Long getDbMaxLifetime()
    {
        return dbMaxLifetime;
    }

    /**
     * @return Database pool max size for HikariCP
     */
    public Integer getDbMaxPoolSize()
    {
        return dbMaxPoolSize;
    }

    /**
     * @return Database properties in a HashMap to give to HikariCP
     */
    public HashMap<String, Object> getDbProperties()
    {
        return dbProperties;
    }

    /**
     * @return CORS expose header list (', ' delimited)
     */
    public String getCorsExposeHeaders()
    {
        return corsExposeHeaders;
    }

    /**
     * @return CORS origin
     */
    public String getCorsOrigin()
    {
        return corsOrigin;
    }

    /**
     * @return CORS method list (', ' delimited)
     */
    public String getCorsMethods()
    {
        return corsMethods;
    }

    /**
     * @return CORS header list (', ' delimited)
     */
    public String getCorsHeaders()
    {
        return corsHeaders;
    }

    /**
     * @return CORS credential list (', ' delimited)
     */
    public String getCorsCredentials()
    {
        return corsCredentials;
    }

    /**
     * @return CORS max age argument
     */
    public String getCorsMaxAge()
    {
        return corsMaxAge;
    }

    /**
     * @return true if the database connection is url based
     */
    public boolean isDbTypeUrl()
    {
        return dbType == DBType.URL;
    }

    /**
     * @return true if a keystore is to be connected to Grizzly's SSLContext
     */
    public boolean isKeyStore()
    {
        return sslType == SSLType.KEYSTORE || sslType == SSLType.FULL;
    }

    /**
     * @return true if a truststore is to be connected to Grizzly's SSLContext
     */
    public boolean isTrustStore()
    {
        return sslType == SSLType.TRUSTSTORE || sslType == SSLType.FULL;
    }

    /**
     * At the moment does nothing
     *
     * @return true is CORS is wadl enabled
     */
    public boolean isWadlEnabled()
    {
        return corsType == CORSType.WADL;
    }

    /**
     * @return true if the Server is to be created with a SSLContext
     */
    public boolean isSslSecure()
    {
        return sslType != SSLType.NONE;
    }

    /**
     * @return true if there is to be a database connected to the service
     */
    public boolean isDbService()
    {
        return dbType != DBType.NONE;
    }

    /**
     * @return true if cors is to be enabled
     */
    public boolean isCorsService()
    {
        return corsType != CORSType.NONE;
    }

    /**
     * @return a URI instance built from the base values given
     */
    public URI buildServiceUri()
    {
        return UriBuilder.fromUri(uriScheme + uriHostName + uriPath).port(uriPort).build();
    }
}
