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



package com.skanders.rms.util.connectionpool;


import com.skanders.rms.base.config.RMSConfig;
import com.skanders.rms.def.logger.Pattern;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class PoolManager
{
    private static final Logger LOG = LoggerFactory.getLogger(PoolManager.class);

    private HikariDataSource hikariDataSource;

    private PoolManager(HikariConfig config)
    {
        LOG.trace(Pattern.ENTER, "Connection Pool Constructor");

        hikariDataSource = new HikariDataSource(config);
    }

    public static PoolManager withDriver(RMSConfig config)
    {
        LOG.trace(Pattern.ENTER, "Connection Pool Constructor from Driver");

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setDataSourceClassName(config.getDbDriver());
        hikariConfig.addDataSourceProperty("serverName",   config.getDbHostname());
        hikariConfig.addDataSourceProperty("portNumber",   config.getDbPort());
        hikariConfig.addDataSourceProperty("databaseName", config.getDbName());

        return buildPoolManager(config, hikariConfig);
    }

    public static PoolManager withJdbcUrl(RMSConfig config)
    {
        LOG.trace(Pattern.ENTER, "Connection Pool Constructor from Driver");

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(config.getDbUrl());

        return buildPoolManager(config, hikariConfig);
    }

    private static PoolManager buildPoolManager(RMSConfig config, HikariConfig hikariConfig)
    {
        hikariConfig.setUsername(config.getDbUsername());
        hikariConfig.setPassword(config.getDbPassword());
        hikariConfig.setMaxLifetime(config.getDbMaxLifetime());
        hikariConfig.setMaximumPoolSize(config.getDbMaxPoolSize());

        addDataSourceProperties(config, hikariConfig);

        return new PoolManager(hikariConfig);
    }

    private static void addDataSourceProperties(RMSConfig config, HikariConfig hikariConfig)
    {
        HashMap dbProperties = config.getDbProperties();

        if (dbProperties != null)
            for (Object key : dbProperties.keySet())
                hikariConfig.addDataSourceProperty((String) key, dbProperties.get(key));
    }

    public static PoolManager withDriver(
            String driver, String hostname, int port, String name,
            String username, String password, long maxLifetime,  int maxPoolSize)
    {
        LOG.trace(Pattern.ENTER, "Connection Pool Constructor from Driver");

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setDataSourceClassName(driver);
        hikariConfig.addDataSourceProperty("serverName",   hostname);
        hikariConfig.addDataSourceProperty("portNumber",   port);
        hikariConfig.addDataSourceProperty("databaseName", name);

        return buildPoolManager(username, password, maxLifetime, maxPoolSize, hikariConfig);
    }

    public static PoolManager withJdbcUrl(
            String url,
            String username, String password, long maxLifetime,  int maxPoolSize )
    {
        LOG.trace(Pattern.ENTER, "Connection Pool Constructor from Driver");

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(url);

        return buildPoolManager(username, password, maxLifetime, maxPoolSize, hikariConfig);
    }

    private static PoolManager buildPoolManager(
            String username, String password, long maxLifetime,  int maxPoolSize,
            HikariConfig hikariConfig)
    {
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setMaximumPoolSize(maxPoolSize);

        return new PoolManager(hikariConfig);
    }

    QueryManager createQuery(String query)
            throws SQLException
    {
        LOG.trace(Pattern.ENTER, "Request Connection");

        Connection connection = hikariDataSource.getConnection();

        PreparedStatement preparedStatement;

        try {
            preparedStatement = connection.prepareStatement(query);

        } catch (SQLException e) {
            hikariDataSource.evictConnection(connection);
            throw e;
        }

        return QueryManager.newManager(this, connection, preparedStatement);

    }

    void releaseCon(Connection connection)
    {
        hikariDataSource.evictConnection(connection);
    }
}