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

package com.skanders.rms.base.result;

import com.skanders.rms.def.logger.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public interface ResultBank
{
    Logger LOG = LoggerFactory.getLogger(ResultBank.class);

    class Bank
    {
        private final ConcurrentHashMap<Integer, Result> SAFE = new ConcurrentHashMap<>();

        public Result get(Integer code)
        {
            Result result = SAFE.get(code);

            return result == null ? Result.exception("Could not find code: " + code) : result;
        }


        public <T> void load(Class<T> className)
        {
            try {
                T instance = className.getDeclaredConstructor().newInstance();

                for (Field field : className.getDeclaredFields())
                    if (isResult(field)) {
                        Result result = (Result) field.get(instance);
                        Result oldResult = SAFE.put(result.code(), result);

                        if (oldResult != null)
                            LOG.warn(warnMsg(oldResult));
                    }

            } catch (NoSuchMethodException | InvocationTargetException |
                    InstantiationException | IllegalAccessException e) {
                LOG.error("Unable to load bank, classes must have public no args constructor, and public Result values.");
                LOG.error(Pattern.ERROR, e.getCause(), e.getMessage());
                e.printStackTrace();

            }
        }

        private boolean isResult(Field field)
        {
            return field.getType() == Result.class;
        }

        private String warnMsg(Result result)
        {
            return "Result '" + result + "' has been overwritten in bank, code '" + result.code() + "'is overlapping";
        }
    }

    Bank BANK = new Bank();
}
