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


package com.skanders.rms.service;

import com.skanders.commons.atsql.AtSQL;
import com.skanders.commons.atsql.AtSQLFactory;
import com.skanders.commons.def.LogPattern;
import com.skanders.commons.def.Verify;
import com.skanders.rms.config.RMSConfig;
import com.skanders.rms.def.RMSException;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public abstract class RapidMicroService
{
    private static final Logger LOG = LoggerFactory.getLogger(RapidMicroService.class);

    private AtSQL      atSQL;
    private HttpServer server;

    /**
     * Constructs an instance of RapidMicroService.
     *
     * @param config       a Config instance
     * @param resourcePath package names for jersey to find components
     * @see RMSConfig
     */
    protected RapidMicroService(@Nonnull RMSConfig config, @Nonnull String... resourcePath)
    {
        Verify.notNull(config, "config cannot be null");
        Verify.notNull(resourcePath, "resourcePath cannot be null");

        LOG.info(LogPattern.INIT, "RapidMicroService");

        if (config.isDbService())
            initConnectionPool(config);

        RMSResourceConfig rmsResourceConfig = new RMSResourceConfig(resourcePath)
                .withRMSSettings(config);

        if (config.isSslSecure())
            initHTTPSecureServer(config, rmsResourceConfig);
        else
            initHTTPServer(config, rmsResourceConfig);

        LOG.info(LogPattern.INIT_DONE, "RapidMicroService");
    }

    /**
     * Constructs an instance of RapidMicroService.
     *
     * @param config         a RMSConfig instance
     * @param resourceConfig a RMSResourceConfig instance
     * @see RMSConfig
     */
    protected RapidMicroService(@Nonnull RMSConfig config, @Nonnull RMSResourceConfig resourceConfig)
    {
        Verify.notNull(config, "config cannot be null");
        Verify.notNull(resourceConfig, "resourceConfig cannot be null");

        LOG.info(LogPattern.INIT, "RapidMicroService");

        if (config.isDbService())
            initConnectionPool(config);

        RMSResourceConfig rmsResourceConfig = resourceConfig.withRMSSettings(config);

        if (config.isSslSecure())
            initHTTPSecureServer(config, rmsResourceConfig);
        else
            initHTTPServer(config, rmsResourceConfig);

        LOG.info(LogPattern.INIT_DONE, "RapidMicroService");
    }

    /**
     * Registers {@link WebSocketApplication} with the server at the given
     * contextPath and urlLogPattern
     *
     * @param contextPath the context path for the WebSocket
     * @param urlPattern  the url pattern for the WebSocket
     * @param app         an instance of WebSocketApplication
     * @see WebSocketApplication
     */
    public void registerWebSocket(
            @Nonnull String contextPath, @Nonnull String urlPattern, @Nonnull WebSocketApplication app)
    {
        Verify.notTrue(server.isStarted(), "cannot add WebSocket after server has started!");

        Verify.notNull(contextPath, "config cannot be null");
        Verify.notNull(urlPattern, "urlLogPattern cannot be null");
        Verify.notNull(app, "app cannot be null");

        LOG.info(LogPattern.INIT, "WebSocket Attachment");

        WebSocketAddOn webSocketAddOn = new WebSocketAddOn();

        for (NetworkListener networkListener : server.getListeners())
            networkListener.registerAddOn(webSocketAddOn);

        WebSocketEngine.getEngine().register(contextPath, urlPattern, app);

        LOG.info(LogPattern.INIT_DONE, "WebSocket Attachment");
    }

    /**
     * Starts the server.
     */
    public void start()
    {
        LOG.trace(LogPattern.ENTER, "Grizzly Server Start");

        try {
            server.start();

        } catch (IOException e) {
            LOG.error(LogPattern.EXIT_FAIL, "Grizzly Server Start", e.getClass(), e.getMessage());

            throw new RMSException("Server failed to start: IOException");

        }
    }

    /**
     * Shutdown server by calling {@link HttpServer#shutdown()}
     *
     * @return an instance of GrizzlyFuture
     */
    public GrizzlyFuture<HttpServer> shutdown()
    {
        LOG.trace(LogPattern.ENTER, "Grizzly Server Shutdown");

        return server.shutdown();
    }

    /**
     * Shutdown server by calling {@link HttpServer#shutdown(long, TimeUnit)}
     *
     * @param gracePeriod grace period to pass to HttpServer's shutdown
     *                    function
     * @param timeUnit    time unit to pass to HttpServer's shutdown function
     * @return an instance of GrizzlyFuture
     */
    public GrizzlyFuture<HttpServer> shutdown(long gracePeriod, TimeUnit timeUnit)
    {
        LOG.trace(LogPattern.ENTER, "Grizzly Server Shutdown");

        return server.shutdown(gracePeriod, timeUnit);
    }

    /**
     * Shutdown server by calling {@link HttpServer#shutdownNow()}
     */
    public void shutdownNow()
    {
        LOG.trace(LogPattern.ENTER, "Grizzly Server Shutdown");

        server.shutdown();
    }

    /**
     * Initializes the connection pool stored within the MicroService.
     * Initializes using the type of connection stated in RMSConfig
     *
     * @param config a RMSConfig instance
     * @see RMSConfig
     */
    private void initConnectionPool(@Nonnull RMSConfig config)
    {
        LOG.info(LogPattern.INIT, "Connection Pool");

        AtSQLFactory factory = AtSQLFactory.newInstance(
                config.getDbUsername(),
                config.getDbPassword(),
                config.getDbMaxLifetime(),
                config.getDbMaxPoolSize());

        if (config.isDbTypeUrl())
            factory = factory.withJdbcUrl(config.getDbUrl());
        else
            factory = factory.withDriver(
                    config.getDbDriver(),
                    config.getDbHostname(),
                    config.getDbPort(),
                    config.getDbName());

        if (config.isMySQLService())
            factory.withMySQLPerformanceSettings();

        if (config.getDbProperties() != null)
            factory.withDataSourceProperties(config.getDbProperties());

        atSQL = factory.build();

        LOG.info(LogPattern.INIT_DONE, "Connection Pool");
    }

    /**
     * Creates a none-secure instance of the Grizzly server setting it to use
     * Jackson and to find components in the given resourcePath
     *
     * @param config            a RMSConfig instance
     * @param rmsResourceConfig a RMSResourceConfig instance
     * @see RMSConfig
     */
    private void initHTTPServer(@Nonnull RMSConfig config, @Nonnull RMSResourceConfig rmsResourceConfig)
    {
        LOG.info(LogPattern.INIT, "HTTP Server");

        URI uri = config.buildServiceUri();
        LOG.info("HTTP Server URI: " + uri);

        server = GrizzlyHttpServerFactory.createHttpServer(uri, rmsResourceConfig, false);

        LOG.info(LogPattern.INIT_DONE, "HTTP Server");
    }

    /**
     * Creates a secure instance of the Grizzly server setting it to use Jackson
     * and to find components in the given resourcePath.
     *
     * @param config            a RMSConfig instance
     * @param rmsResourceConfig a RMSResourceConfig instance
     * @see RMSConfig
     */
    private void initHTTPSecureServer(@Nonnull RMSConfig config, @Nonnull RMSResourceConfig rmsResourceConfig)
    {
        LOG.info(LogPattern.INIT, "HTTP Secure Server");

        URI uri = config.buildServiceUri();
        LOG.info("HTTP Secure Server URI: " + uri);

        SSLEngineConfigurator sslEngineConfigurator = createSSLEngineConfigurator(config);

        server = GrizzlyHttpServerFactory.createHttpServer(uri, rmsResourceConfig, true, sslEngineConfigurator, false);

        LOG.info(LogPattern.INIT_DONE, "HTTP Secure Server");
    }

    /**
     * Creates an instance of SSLEngineConfigurator with the KeyStore and
     * TrustStore properties given in the RMSConfig
     *
     * @param config a RMSConfig instance
     * @return an SSLEngineConfigurator with the KeyStore and TrustStore file
     * and pass
     * @see RMSConfig
     */
    private SSLEngineConfigurator createSSLEngineConfigurator(@Nonnull RMSConfig config)
    {
        SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator();

        if (config.isKeyStore()) {
            sslContextConfigurator.setKeyStoreFile(config.getSslKeyStoreFile());
            sslContextConfigurator.setKeyStorePass(config.getSslKeyStorePass());
        }

        if (config.isTrustStore()) {
            sslContextConfigurator.setTrustStoreFile(config.getSslTrustStoreFile());
            sslContextConfigurator.setTrustStorePass(config.getSslTrustStorePass());
        }

        return new SSLEngineConfigurator(sslContextConfigurator.createSSLContext(true), false, false, false);

    }

    /**
     * Simple getter for AtSQL
     *
     * @return the MicroServices instance of AtSQL
     * @see AtSQL
     */
    public AtSQL getAtSQL()
    {
        Verify.notNull(atSQL, "PoolManager has not bee initialized.");

        return atSQL;
    }
}
