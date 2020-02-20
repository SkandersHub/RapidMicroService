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

package com.skanders.rms.base.service.mapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.skanders.rms.base.model.ResponseModel;
import com.skanders.rms.util.result.RMSResult;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class RMSJsonParseException implements ExceptionMapper<JsonParseException>
{
    private static final Logger LOG = LoggerFactory.getLogger(RMSJsonParseException.class);

    private static class JsonParseResult extends ResponseModel
    {
        JsonParseResult()
        {
            super(RMSResult.JSON_PARSE_EXCEPT);
        }
    }

    @Override
    public Response toResponse(JsonParseException e)
    {
        LOG.error("Incoming Request raised an '{}' exception, caused by '{}'.", e.getClass(), e.getMessage());
        return Response.status(Status.BAD_REQUEST).entity(new JsonParseResult()).build();
    }
}
