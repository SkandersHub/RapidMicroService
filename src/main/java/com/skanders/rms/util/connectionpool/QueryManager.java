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

import com.skanders.rms.def.verify.RMSVerify;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class QueryManager implements AutoCloseable
{
    private PoolManager poolManager;
    private Connection connection;
    private PreparedStatement preparedStatement;

    private boolean closed;

    private QueryManager(
            PoolManager poolManager, Connection connection,
            PreparedStatement preparedStatement)
    {
        this.poolManager = poolManager;
        this.connection = connection;
        this.preparedStatement = preparedStatement;
        this.closed = false;
    }

    static QueryManager newManager(
            @NotNull PoolManager poolManager, @NotNull Connection connection,
            @NotNull PreparedStatement preparedStatement)
    {
        RMSVerify.checkNull(poolManager, "poolManager Cannot be Null");
        RMSVerify.checkNull(connection,  "connection Cannot be Null");

        return new QueryManager(poolManager, connection, preparedStatement);
    }

    void setParams(List<ParamPair> paramList)
            throws SQLException
    {
        int count = 1;

        for (ParamPair paramPair : paramList)
            preparedStatement.setObject(count++, paramPair.getParam(), paramPair.getType());
    }

    Integer executeUpdate()
            throws SQLException
    {
        return preparedStatement.executeUpdate();
    }

    ResultSet executeQuery()
            throws SQLException
    {
        return preparedStatement.executeQuery();
    }

    @Override
    public void close()
    {
        if (!this.closed) {
            poolManager.releaseCon(connection);
            this.closed = true;
        }
    }
}

