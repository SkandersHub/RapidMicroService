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



package com.skanders.rms.logger;


public class Log
{
    public static final String ENTER        = "[Enter] {}...";
    public static final String EXIT_EARLY   = Color.BRIGHT_YELLOW  + "[Exiting Early] {}. With Result: {}."            + Color.DEFAULT;
    public static final String EXIT_FAIL    = Color.BRIGHT_RED     + "[Exiting With Failure] {}. Caused by {}: {}."    + Color.DEFAULT;

    public static final String INIT         = "Initializing {}...";
    public static final String INIT_DONE    = "{} has been Initialized.";

    public static final String ATTEMPT      = "Attempting to {}...";
    public static final String ATTEMPT_DONE = "Successfully {}.";

    public static final String ERROR        = Color.BRIGHT_RED     + "Caused by {}: {}."  + Color.DEFAULT;

    public static final String API_ENTER    = "[{} Request Received] {}...";
    public static final String API_EXIT     = "Request Completed With Result: {}.";
}
