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
import com.skanders.rms.base.service.mapper.RMSJsonMappingException;
import com.skanders.rms.base.service.mapper.RMSJsonParseException;
import com.skanders.rms.base.service.mapper.RMSThrowable;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RMSResourceConfig extends ResourceConfig
{
    private static final String DISABLE_WADL = "jersey.config.server.wadl.disableWadl";

    public RMSResourceConfig(String ... resourcePath)
    {
        super();

        packages(resourcePath);
    }

    RMSResourceConfig withRMSSettings(RMSConfig config)
    {
        register(JacksonFeature.class);
        register(RMSJsonMappingException.class);
        register(RMSJsonParseException.class);
        register(RMSThrowable.class);

        property(DISABLE_WADL, "true");

        if (config.isCorsService())
            setCORSFilter(config);

        return this;
    }

    private void setCORSFilter(RMSConfig config)
    {
        CORSFilter.setConfig(config);
        register(CORSFilter.class);
    }
}
