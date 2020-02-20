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



package com.skanders.rms.util.socket;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;

public class ServiceSocket
{
    private Client client;

    private final URI uri;
    private final MediaType acceptType;
    private final String path;

    private MultivaluedHashMap<String, Object> headers;
    private MultivaluedHashMap<String, Object> queries;


    /**
     * Creates an instance of APISocket
     *
     * @param uri        URL of the target
     * @param acceptType MediaType of response from the target
     * @param path       path to the specific endpoint
     */
    ServiceSocket(URI uri, MediaType acceptType, String path, Client client)
    {
        this.uri = uri;
        this.acceptType = acceptType;
        this.path = path;

        this.headers = new MultivaluedHashMap<>();
        this.queries = new MultivaluedHashMap<>();

        this.client = client;
    }

    /**
     * Calls a GET request
     *
     * @return an instance of Response
     */
    public Response get()
    {
        return createBuilder().get();
    }

    /**
     * Calls a HEAD request
     *
     * @return an instance of Response
     */
    public Response head()
    {
        return createBuilder().head();
    }

    /**
     * Calls a POST request
     *
     * @param entity entity
     * @return an instance of Response
     */
    public Response post(Entity entity)
    {
        return createBuilder().post(entity);
    }

    /**
     * Calls a PUT request
     *
     * @param entity entity
     * @return an instance of Response
     */
    public Response put(Entity entity)
    {
        return createBuilder().put(entity);
    }

    /**
     * Calls a DELETE request
     *
     * @return an instance of Response
     */
    public Response delete()
    {
        return createBuilder().delete();
    }

    /**
     * Calls a OPTIONS request
     *
     * @return an instance of Response
     */
    public Response options()
    {
        return createBuilder().options();
    }

    /**
     * Calls a TRACE request
     *
     * @return an instance of Response
     */
    public Response trace()
    {
        return createBuilder().trace();
    }

    /**
     * Adds the headers to the socket call
     *
     * @param headerMap a MultivaluedMap instance
     * @return the object being called
     */
    public ServiceSocket headers(@NotNull MultivaluedMap<String, Object> headerMap)
    {
        headers.putAll(headerMap);

        return this;
    }

    /**
     * Adds a single header value to the socket call
     *
     * @param key key for header value
     * @param value value for header key
     * @return the object being called
     */
    public ServiceSocket header(@NotNull String key, @NotNull Object value)
    {
        headers.add(key, value);

        return this;
    }

    /**
     * Adds the queries to the socket call
     *
     * @param queryMap a MultivaluedMap instance
     * @return the object being called
     */
    public ServiceSocket queries(@NotNull MultivaluedMap<String, Object> queryMap)
    {
        queries.putAll(queryMap);

        return this;
    }

    /**
     * Adds a single query value to the socket call
     *
     * @param key key for query value
     * @param value value for query key
     * @return the object being called
     */
    public ServiceSocket query(String key, Object value)
    {
        queries.add(key, value);

        return this;
    }

    /**
     * Internal builder creator, uses the instances path, uri, queries and header to
     * construct a {@link WebTarget} to instantiate a {@link Builder}
     *
     * @return an instance of {@link Builder} to be used to query the MicroService
     */
    private Builder createBuilder()
    {
        WebTarget webTarget = client.target(uri).path(path);

        if (queries != null)
            for (String key : queries.keySet())
                webTarget = webTarget.queryParam(key, queries.getFirst(key));

        Builder builder = webTarget.request(acceptType);

        if (headers != null)
            for (String key : headers.keySet())
                builder.header(key, headers.getFirst(key));

        return builder;
    }
}
