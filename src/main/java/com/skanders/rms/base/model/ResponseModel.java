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



package com.skanders.rms.base.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skanders.rms.def.verify.RMSVerify;
import com.skanders.rms.util.result.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public abstract class ResponseModel
{
    private static final Logger LOG = LogManager.getLogger(ResponseModel.class);

    @JsonProperty("result")
    private Result result;

    /**
     * Default constructor, sets result to UNDECLARED to ensure
     * proper creation by user
     *
     */
    public ResponseModel()
    {
        this.result = Result.UNDECLARED;
    }

    /**
     * Constructor to be called by super()
     *
     * @param result a instance of Result
     * @see Result
     */
    public ResponseModel(@NotNull Result result)
    {
        RMSVerify.checkNull(result, "result cannot be null");

        this.result = result;
    }

    /**
     *
     * @return Current Result of Response
     * @see Result
     */
    public Result getResult()
    {
        return result;
    }

    /**
     *
     * @param result sets the Current Result of the WorkFlow
     */
    public void setResult(@NotNull Result result)
    {
        RMSVerify.checkNull(result, "result cannot be null");

        this.result = result;
    }

    /**
     * Builds a response based on this ResponseModel and its result
     *
     * @return an instance of {@link Response}
     */
    @JsonIgnore
    public Response toResponse()
    {
        return responseBuilder().build();
    }

    /**
     * Builds a response based on this ResponseModel and its result
     * with the headers attached.
     *
     * @param headers headers used when creating response
     * @return an instance of {@link Response}
     */
    @JsonIgnore
    public Response toResponse(@NotNull MultivaluedHashMap<String, Object> headers)
    {
        RMSVerify.checkNull(headers, "headers cannot be null");

        ResponseBuilder builder = responseBuilder();

        for (String key : headers.keySet())
            builder = builder.header(key, headers.getFirst(key));

        return builder.build();
    }

    /**
     * Builds a response based on this ResponseModel checking for any errors in result
     *
     * @return an instance of {@link ResponseBuilder}
     */
    @JsonIgnore
    private ResponseBuilder responseBuilder()
    {
        if (result == null) {
            LOG.error("Request ending with null Result");
            return Response.status(Status.INTERNAL_SERVER_ERROR);

        } else if (result.exception() != null) {
            LOG.error("Request ending with exception: " + result.exception().getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR);

        } else if (result.status() == Status.INTERNAL_SERVER_ERROR) {
            LOG.error("Request ending with Internal Server Error");
            return Response.status(Status.INTERNAL_SERVER_ERROR);

        } else {
            return Response.status(result.status()).entity(this);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        ResponseModel that = (ResponseModel) o;

        return result == that.result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(result);
    }
}
