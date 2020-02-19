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

package com.skanders.rms.util.arg;

import com.skanders.rms.def.exception.RMSException;

import java.util.Hashtable;

public class Args
{
    private Hashtable<String, String> argTable;

    private static final String MISSING = new String("");
    private static final String FLAG    = new String("");
    private static final String PREFIX  = "-";

    private Args(Hashtable<String, String> argTable)
    {
        this.argTable = argTable;
    }

    /**
     * Parses argName and places the corresponding argValue in a hashtable
     *
     * -file file.txt (argValue 'file.txt' corresponding with the argName 'file')
     *
     * All argNames will automatically be prefixed with '-'. If a argName
     * is given with a '-' already none will be added.
     *
     * If a argName is placed before another argName it is considered a flag and
     * will be given a "true" value in the hashtable.
     *
     * All argNames not called in the argValues list are given a missing flag.
     *
     * @param argValues argument values to be attached to names
     * @param argNames  argument names to search for
     * @return an instance of {@link Args}
     */
    public static Args parse(String[] argValues, String ... argNames)
    {
        Hashtable<String, String> argTable = new Hashtable<>();

        for (String arg : argNames)
            if (arg.startsWith(PREFIX))
                argTable.put(arg, MISSING);
            else
                argTable.put(PREFIX + arg, MISSING);


        int length = argValues.length;

        for (int i = 0; i < length; i++)
            if (argTable.containsKey(argValues[i]))
            {
                if (i + 1 < length) {
                    if (argValues[i+1].startsWith(PREFIX))
                        argTable.put(argValues[i], FLAG);
                    else
                        argTable.put(argValues[i], argValues[++i]);
                    
                } else {
                    argTable.put(argValues[i], FLAG);

                }
            }

        return new Args(argTable);
    }

    /**
     * Checks to see if argument was given a value
     *
     * @param arg argument name to check
     * @return true if value is missing
     */
    public boolean isMissing(String arg)
    {
        // Purposely doing object reference comparison
        return MISSING == get(arg);
    }

    /**
     * Checks to see if argument was given
     *
     * @param arg argument name to check
     * @return true if value is flagged
     */
    public boolean isTrue(String arg)
    {
        // Purposely doing object reference comparison
        return FLAG == get(arg);
    }

    /**
     * Gets the corresponding argument value
     *
     * @param arg argument name
     * @return argument value
     */
    public String get(String arg)
    {
        String value = argTable.get(PREFIX + arg);

        if (value == null)
            throw new RMSException("Named Argument not given at parse.");

        return value;
    }
}