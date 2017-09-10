/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the weather database. This class is not necessary, but keeps
 * the code organized.
 */
public class DataContract {
    public static final Uri uri = Uri.parse("content://com.google.firebase.udacity.friendlychat.data");

    /* Inner class that defines the table contents of the weather table */
    public static final class DataEntry implements BaseColumns {

        /* Used internally as the name of our weather table. */
        public static final String TABLE_NAME = "audio";


        //adsawd


        /* Pressure is stored as a float representing percentage */
        public static final String AudioId = "IDD";

        /* Wind speed is stored as a float representing wind speed in mph */
        public static final String AudioPath = "path";

        public static final String AudioLink = "link";


    }
}