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



package com.skanders.rms.def.verify;

import com.skanders.rms.def.exception.RMSException;
import com.skanders.rms.logger.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RMSVerify
{
    private static final Logger LOG = LogManager.getLogger(RMSVerify.class);

    public static void checkNull(Object ob)
    {
        if (ob == null)
            throw new RMSException();
    }

    public static void checkNull(Object ob, String message)
    {
        if (ob == null)
            throw new RMSException(message);
    }

    public static void argument(boolean arg)
    {
        if (arg)
            throw new RMSException();
    }

    public static void argument(boolean arg, String message)
    {
        if (arg)
            throw new RMSException(message);
    }

    public static <T extends AutoCloseable> void close(T closeableObject)
    {
        try {
            if (closeableObject != null)
                closeableObject.close();

        } catch (Exception e) {
            LOG.error("SEVERE ERROR: Exception when trying to close");
            LOG.error(Log.ERROR, e.getClass(), e.getMessage());

        }
    }
}
