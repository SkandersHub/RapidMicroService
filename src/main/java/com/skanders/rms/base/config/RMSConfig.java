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

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;

public class RMSConfig
{
    private  String uriScheme;
    private  String uriHostName;
    private Integer uriPort;
    private  String uriPath;


    private  String sslKeyStoreFile;
    private  String sslKeyStorePass;
    private  String sslTrustStoreFile;
    private  String sslTrustStorePass;


    private  String dbUrl;
    private  String dbDriver;
    private  String dbHostname;
    private Integer dbPort;
    private  String dbName;
    private  String dbUsername;
    private  String dbPassword;
    private    Long dbMaxLifetime;
    private Integer dbMaxPoolSize;
    private HashMap dbProperties;


    private  String corsOrigin;
    private  String corsMethods;
    private  String corsHeaders;
    private  String corsExposeHeaders;
    private  String corsCredentials;
    private  String corsMaxAge;


    private DBType   dbType;
    private SSLType  sslType;
    private CORSType corsType;

    public RMSConfig(@NotNull RMSProperties prop)
    {
        RMSVerify.checkNull(prop, "prop cannot be null");

        setUriConfigs(prop);
        setSSLEngineConfig(prop);
        setDatabaseConfig(prop);
        setCORSConfig(prop);
    }

    private void setUriConfigs(@NotNull RMSProperties prop)
    {
        uriScheme   = prop.getReqStr("uri.scheme");
        uriHostName = prop.getReqStr("uri.hostname");
        uriPort     = prop.getReqInt("uri.port");
        uriPath     = prop.getReqStr("uri.path");
    }

    private void setSSLEngineConfig(@NotNull RMSProperties prop)
    {
        if ((sslType = prop.getSSLType()) == SSLType.NONE)
            return;

        if (isKeyStore())
            setKeyStore(prop);

        if (isTrustStore())
            setTrustStore(prop);
    }

    private void setKeyStore(@NotNull RMSProperties prop)
    {
        sslKeyStoreFile = prop.getReqStr("ssl.keyStoreFile");
        sslKeyStorePass = prop.getReqStr("ssl.keyStorePass");

        if (sslType == SSLType.FULL)
            return;

        prop.checkIgnored("ssl.trustStoreFile");
        prop.checkIgnored("ssl.trustStorePass");
    }

    private void setTrustStore(@NotNull RMSProperties prop)
    {
        sslTrustStoreFile = prop.getReqStr("ssl.trustStoreFile");
        sslTrustStorePass = prop.getReqStr("ssl.trustStorePass");

        if (sslType == SSLType.FULL)
            return;

        prop.checkIgnored("ssl.keyStoreFile");
        prop.checkIgnored("ssl.keyStorePass");
    }

    private void setDatabaseConfig(@NotNull RMSProperties prop)
    {
        if ((dbType = prop.getDBType()) == DBType.NONE)
            return;

        if (dbType == DBType.URL)
            setDBUrl(prop);
        else
            setDbDriver(prop);

        dbUsername    = prop.getReqStr("db.username");
        dbPassword    = prop.getReqStr("db.password");
        dbMaxLifetime = prop.getReqLong("db.maxLifetime");
        dbMaxPoolSize = prop.getReqInt("db.maxPoolSize");

        dbProperties  = prop.getMap("db.properties");
    }

    private void setDbDriver(@NotNull RMSProperties prop)
    {
        dbDriver   = prop.getReqStr("db.driver");
        dbHostname = prop.getReqStr("db.hostname");
        dbPort     = prop.getReqInt("db.port");
        dbName     = prop.getReqStr("db.name");

        prop.checkIgnored("db.url");
    }

    private void setDBUrl(@NotNull RMSProperties prop)
    {
        dbUrl = prop.getReqStr("db.url");

        prop.checkIgnored("db.driver");
        prop.checkIgnored("db.hostname");
        prop.checkIgnored("db.port");
        prop.checkIgnored("db.name");
    }

    private void setCORSConfig(@NotNull RMSProperties prop)
    {
        if ((corsType = prop.getCORSType()) == CORSType.NONE)
            return;

        corsOrigin        = prop.getReqStr("cors.origin");
        corsMethods       = prop.getReqStr("cors.methods");
        corsHeaders       = prop.getReqStr("cors.headers");

        corsExposeHeaders = prop.getStr("cors.exposeHeaders");

        corsCredentials   = prop.getStr("cors.credentials");
        corsMaxAge        = prop.getStr("cors.maxAge");
    }

    public String getSslKeyStoreFile()
    {
        return sslKeyStoreFile;
    }

    public String getSslKeyStorePass()
    {
        return sslKeyStorePass;
    }

    public String getSslTrustStoreFile()
    {
        return sslTrustStoreFile;
    }

    public String getSslTrustStorePass()
    {
        return sslTrustStorePass;
    }

    public String getDbUrl()
    {
        return dbUrl;
    }

    public String getDbDriver()
    {
        return dbDriver;
    }

    public String getDbHostname()
    {
        return dbHostname;
    }

    public Integer getDbPort()
    {
        return dbPort;
    }

    public String getDbName()
    {
        return dbName;
    }

    public String getDbUsername()
    {
        return dbUsername;
    }

    public String getDbPassword()
    {
        return dbPassword;
    }

    public Long getDbMaxLifetime()
    {
        return dbMaxLifetime;
    }

    public Integer getDbMaxPoolSize()
    {
        return dbMaxPoolSize;
    }

    public HashMap getDbProperties()
    {
        return dbProperties;
    }

    public String getCorsExposeHeaders()
    {
        return corsExposeHeaders;
    }

    public String getCorsOrigin()
    {
        return corsOrigin;
    }

    public String getCorsMethods()
    {
        return corsMethods;
    }

    public String getCorsHeaders()
    {
        return corsHeaders;
    }

    public String getCorsCredentials()
    {
        return corsCredentials;
    }

    public String getCorsMaxAge()
    {
        return corsMaxAge;
    }

    public boolean isDbTypeUrl()
    {
        return dbType == DBType.URL;
    }

    public boolean isKeyStore()
    {
        return sslType == SSLType.KEYSTORE || sslType == SSLType.FULL;
    }

    public boolean isTrustStore()
    {
        return sslType == SSLType.TRUSTSTORE || sslType == SSLType.FULL;
    }

    public boolean isWadlEnabled()
    {
        return corsType == CORSType.WADL;
    }

    public boolean isSslSecure()
    {
        return sslType != SSLType.NONE;
    }

    public boolean isDbService()
    {
        return dbType != DBType.NONE;
    }

    public boolean isCorsService()
    {
        return corsType != CORSType.NONE;
    }

    public URI buildServiceUri()
    {
        return UriBuilder.fromUri(uriScheme + uriHostName + uriPath).port(uriPort).build();
    }
}
