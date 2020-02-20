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



package com.skanders.rms.base.service;

import com.skanders.rms.base.config.RMSConfig;
import com.skanders.rms.def.exception.RMSException;
import com.skanders.rms.def.verify.RMSVerify;
import com.skanders.rms.def.logger.Pattern;
import com.skanders.rms.util.connectionpool.PoolManager;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public abstract class RapidMicroService
{
    private static final Logger LOG = LoggerFactory.getLogger(RapidMicroService.class);

    private PoolManager poolManager;
    private HttpServer server;

    /**
     * Constructs an instance of RapidMicroService.
     *
     * @param config       a Config instance
     * @param resourcePath package names for jersey to find components
     * @see   RMSConfig
     */
    protected RapidMicroService(@NotNull RMSConfig config, @NotNull String ... resourcePath)
    {
        RMSVerify.checkNull(config,       "config cannot be null");
        RMSVerify.checkNull(resourcePath, "resourcePath cannot be null");

        LOG.info(Pattern.INIT, "RapidMicroService");

        if (config.isDbService())
            initConnectionPool(config);

        RMSResourceConfig rmsResourceConfig = new RMSResourceConfig(resourcePath)
                .withRMSSettings(config);

        if (config.isSslSecure())
            initHTTPSecureServer(config, rmsResourceConfig);
        else
            initHTTPServer(config, rmsResourceConfig);

        LOG.info(Pattern.INIT_DONE, "RapidMicroService");
    }

    /**
     * Constructs an instance of RapidMicroService.
     *
     * @param config         a RMSConfig instance
     * @param resourceConfig a RMSResourceConfig instance
     * @see   RMSConfig
     */
    protected RapidMicroService(@NotNull RMSConfig config, @NotNull RMSResourceConfig resourceConfig)
    {
        RMSVerify.checkNull(config,         "config cannot be null");
        RMSVerify.checkNull(resourceConfig, "resourceConfig cannot be null");

        LOG.info(Pattern.INIT, "RapidMicroService");

        if (config.isDbService())
            initConnectionPool(config);

        RMSResourceConfig rmsResourceConfig = resourceConfig.withRMSSettings(config);

        if (config.isSslSecure())
            initHTTPSecureServer(config, rmsResourceConfig);
        else
            initHTTPServer(config, rmsResourceConfig);

        LOG.info(Pattern.INIT_DONE, "RapidMicroService");
    }

    /**
     * Registers {@link WebSocketApplication} with the server at the
     * given contextPath and urlPattern
     *
     * @param contextPath the context path for the WebSocket
     * @param urlPattern  the url pattern for the WebSocket
     * @param app         an instance of WebSocketApplication
     * @see   WebSocketApplication
     */
    public void registerWebSocket(
            @NotNull String contextPath, @NotNull String urlPattern, @NotNull WebSocketApplication app)
    {
        RMSVerify.argument(server.isStarted(), "cannot add WebSocket after server has started!");

        RMSVerify.checkNull(contextPath, "config cannot be null");
        RMSVerify.checkNull(urlPattern,  "urlPattern cannot be null");
        RMSVerify.checkNull(app,         "app cannot be null");

        LOG.info(Pattern.INIT, "WebSocket Attachment");

        WebSocketAddOn webSocketAddOn = new WebSocketAddOn();

        for (NetworkListener networkListener : server.getListeners())
            networkListener.registerAddOn(webSocketAddOn);

        WebSocketEngine.getEngine().register(contextPath, urlPattern, app);

        LOG.info(Pattern.INIT_DONE, "WebSocket Attachment");
    }

    /**
     * Starts the server.
     */
    public void start()
    {
        LOG.trace(Pattern.ENTER, "Grizzly Server Start");

        try {
            server.start();

        } catch (IOException e) {
            LOG.error(Pattern.EXIT_FAIL, "Grizzly Server Start", e.getClass(), e.getMessage());

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
        LOG.trace(Pattern.ENTER, "Grizzly Server Shutdown");

        return server.shutdown();
    }

    /**
     * Shutdown server by calling {@link HttpServer#shutdown(long, TimeUnit)}
     *
     * @param gracePeriod grace period to pass to HttpServer's shutdown function
     * @param timeUnit    time unit to pass to HttpServer's shutdown function
     * @return an instance of GrizzlyFuture
     */
    public GrizzlyFuture<HttpServer> shutdown(long gracePeriod, TimeUnit timeUnit)
    {
        LOG.trace(Pattern.ENTER, "Grizzly Server Shutdown");

        return server.shutdown(gracePeriod, timeUnit);
    }

    /**
     * Shutdown server by calling {@link HttpServer#shutdownNow()}
     *
     */
    public void shutdownNow()
    {
        LOG.trace(Pattern.ENTER, "Grizzly Server Shutdown");

        server.shutdown();
    }

    /**
     * Initializes the connection pool stored within the MicroService. Initializes
     * using the type of connection stated in RMSConfig
     *
     * @param config a RMSConfig instance
     * @see RMSConfig
     * @see PoolManager
     */
    private void initConnectionPool(@NotNull RMSConfig config)
    {
        LOG.info(Pattern.INIT, "Connection Pool");

        if (config.isDbTypeUrl())
            poolManager = PoolManager.withJdbcUrl(config);
        else
            poolManager = PoolManager.withDriver(config);

        LOG.info(Pattern.INIT_DONE, "Connection Pool");
    }

    /**
     * Creates a none-secure instance of the Grizzly server setting it to use Jackson
     * and to find components in the given resourcePath
     *
     * @param config            a RMSConfig instance
     * @param rmsResourceConfig a RMSResourceConfig instance
     * @see RMSConfig
     */
    private void initHTTPServer(@NotNull RMSConfig config, @NotNull RMSResourceConfig rmsResourceConfig)
    {
        LOG.info(Pattern.INIT, "HTTP Server");

        URI uri = config.buildServiceUri();
        LOG.info("HTTP Server URI: " + uri);

        server = GrizzlyHttpServerFactory.createHttpServer(uri, rmsResourceConfig, false);

        LOG.info(Pattern.INIT_DONE, "HTTP Server");
    }

    /**
     * Creates a secure instance of the Grizzly server setting it to use Jackson and to
     * find components in the given resourcePath.
     *
     * @param config            a RMSConfig instance
     * @param rmsResourceConfig a RMSResourceConfig instance
     * @see RMSConfig
     */
    private void initHTTPSecureServer(@NotNull RMSConfig config, @NotNull RMSResourceConfig rmsResourceConfig)
    {
        LOG.info(Pattern.INIT, "HTTP Secure Server");

        URI uri = config.buildServiceUri();
        LOG.info("HTTP Secure Server URI: " + uri);

        SSLEngineConfigurator sslEngineConfigurator = createSSLEngineConfigurator(config);

        server = GrizzlyHttpServerFactory.createHttpServer(uri, rmsResourceConfig, true, sslEngineConfigurator, false);

        LOG.info(Pattern.INIT_DONE, "HTTP Secure Server");
    }

    /**
     * Creates an instance of SSLEngineConfigurator with the KeyStore and TrustStore properties
     * given in the RMSConfig
     *
     * @param config a RMSConfig instance
     * @return an SSLEngineConfigurator with the KeyStore and TrustStore file and pass
     * @see RMSConfig
     */
    private SSLEngineConfigurator createSSLEngineConfigurator(@NotNull RMSConfig config)
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
     * Simple getter for ConnectionPool
     *
     * @return the MicroServices instance of ConnectionPool
     * @see PoolManager
     */
    public PoolManager getPoolManager()
    {
        RMSVerify.checkNull(poolManager, "PoolManager has not bee initialized.");
        return poolManager;
    }
}
