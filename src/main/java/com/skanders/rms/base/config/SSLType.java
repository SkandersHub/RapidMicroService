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

package com.skanders.rms.base.config;


import com.skanders.rms.def.exception.RMSException;

public enum SSLType
{
    NONE,
    KEYSTORE,
    TRUSTSTORE,
    FULL;

    static SSLType getType(String type)
    {
        if (type == null)
            return SSLType.NONE;

        switch (type.toLowerCase())
        {
            case "none":
                return SSLType.NONE;
            case "keystore":
                return SSLType.KEYSTORE;
            case "truststore":
                return SSLType.TRUSTSTORE;
            case "full":
                return SSLType.FULL;
            default:
                throw new RMSException("Invalid SecureType given.");
        }
    }
}
