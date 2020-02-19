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

import com.google.common.base.Strings;
import com.skanders.rms.def.verify.RMSVerify;

public class QueryBuilder
{
    private static final String PARAM_START = "     (";
    private static final String PARAM       = "?,";
    private static final String PARAM_END   = "?)";

    private static final String LIST_SEP    = ", \n";

    public static String paramList(int paramCount)
    {
        RMSVerify.argument(paramCount < 1, "paramCount must be greater than 0.");

        return paramList(paramCount, 1);
    }

    public static String paramList(int paramCount, int listCount)
    {
        RMSVerify.argument(paramCount < 1, "paramCount must be greater than 0.");
        RMSVerify.argument(listCount  < 1, "listCount must be greater than 0.");

        String lastParam = PARAM_START + Strings.repeat(PARAM, paramCount - 1) + PARAM_END;

        if (listCount == 1)
            return lastParam;

        String repeatParam = Strings.repeat(lastParam + LIST_SEP, listCount - 1);

        return repeatParam + lastParam;
    }

}
