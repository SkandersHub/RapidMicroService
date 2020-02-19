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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;

public class SQLResult implements AutoCloseable
{
    private static final Logger LOG = LogManager.getLogger(SQLResult.class);

    private QueryManager queryManager;
    private ResultSet resultSet;

    private SQLResult(QueryManager queryManager, ResultSet resultSet)
    {
        this.queryManager = queryManager;
        this.resultSet = resultSet;
    }

    static SQLResult newInstance(@NotNull QueryManager queryManager, @NotNull ResultSet resultSet)
    {
        RMSVerify.checkNull(queryManager, "queryManager cannot be null");
        RMSVerify.checkNull(resultSet,    "resultSet cannot be null");

        return new SQLResult(queryManager, resultSet);
    }

    public ResultSet getResultSet()
    {
        return resultSet;
    }

    @Override
    public void close()
    {
        queryManager.close();
    }
}

