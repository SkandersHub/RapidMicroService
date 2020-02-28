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

import com.skanders.rms.config.RMSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.List;

public class CORSFilter implements ContainerRequestFilter, ContainerResponseFilter
{
    private static final Logger LOG = LoggerFactory.getLogger(CORSFilter.class);

    private enum CORSType
    {NON_VALID_CORS, ACTUAL, NON_VALID_PREFLIGHT, PREFLIGHT}

    private static final String ORIGIN     = "Origin";
    private static final String LIST_DELIM = ", ";
    private static final String OPTIONS    = "OPTIONS";

    private static final String REQUEST_HEADER = "Access-Control-Request-Header";
    private static final String REQUEST_METHOD = "Access-Control-Request-Method";

    private static final String EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    private static final String ALLOW_ORIGIN  = "Access-Control-Allow-Origin";
    private static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";

    private static final String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String MAX_AGE           = "Access-Control-Max-Age";


    private static String corsOrigin;
    private static String corsMethods;
    private static String corsHeaders;

    private static String corsExposeHeaders;
    private static String corsCredentials;
    private static String corsMaxAge;

    private static List<String> corsMethodsList;
    private static List<String> corsHeadersList;

    static void setConfig(RMSConfig config)
    {
        corsOrigin  = config.getCorsOrigin();
        corsMethods = config.getCorsMethods();
        corsHeaders = config.getCorsHeaders();

        corsExposeHeaders = config.getCorsExposeHeaders();
        corsCredentials   = config.getCorsCredentials();
        corsMaxAge        = config.getCorsMaxAge();

        corsMethodsList = Arrays.asList(corsMethods.split(LIST_DELIM));
        corsHeadersList = Arrays.asList(corsHeaders.split(LIST_DELIM));
    }

    @Override
    public void filter(ContainerRequestContext requestContext)
    {

        switch (getType(requestContext)) {
            case PREFLIGHT:
                requestContext.abortWith(Response.status(Status.OK).build());
                break;

            case NON_VALID_PREFLIGHT:
                requestContext.abortWith(Response.status(Status.BAD_REQUEST).build());
                break;

            default:
                // Continue
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
    {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        switch (getType(requestContext)) {
            case PREFLIGHT:
                headers.add(ALLOW_METHODS, corsMethods);
                headers.add(ALLOW_HEADERS, corsHeaders);

                if (corsMaxAge != null)
                    headers.add(MAX_AGE, corsMaxAge);

                // Fall through

            case ACTUAL:
                if (corsExposeHeaders != null)
                    headers.add(EXPOSE_HEADERS, corsExposeHeaders);

                headers.add(ALLOW_ORIGIN, corsOrigin);

                if (corsMaxAge != null)
                    headers.add(ALLOW_CREDENTIALS, corsCredentials);

                break;

            default:
                // Continue
        }

    }

    private CORSType getType(ContainerRequestContext requestContext)
    {
        CORSType type = verifyType(requestContext);

        return type != CORSType.PREFLIGHT ? type : verifyPreflight(requestContext);
    }

    private CORSType verifyType(ContainerRequestContext requestContext)
    {
        MultivaluedMap<String, String> requestHeaders = requestContext.getHeaders();

        if (requestHeaders.get(ORIGIN) == null)
            return CORSType.NON_VALID_CORS;

        if (!requestContext.getMethod().equalsIgnoreCase(OPTIONS))
            return CORSType.ACTUAL;

        if (requestHeaders.get(REQUEST_METHOD) == null)
            return CORSType.ACTUAL;
        else
            return CORSType.PREFLIGHT;

    }

    private CORSType verifyPreflight(ContainerRequestContext requestContext)
    {
        MultivaluedMap<String, String> requestHeaders = requestContext.getHeaders();

        if (!corsMethodsList.contains(requestHeaders.getFirst(REQUEST_METHOD)))
            return CORSType.NON_VALID_PREFLIGHT;

        if (requestHeaders.getFirst(REQUEST_HEADER) == null)
            return CORSType.PREFLIGHT;

        if (!corsHeadersList.contains(requestHeaders.getFirst(REQUEST_HEADER)))
            return CORSType.NON_VALID_PREFLIGHT;
        else
            return CORSType.PREFLIGHT;
    }
}
