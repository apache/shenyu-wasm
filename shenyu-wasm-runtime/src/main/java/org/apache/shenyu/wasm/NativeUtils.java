/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.wasm;

import java.util.Locale;

/**
 * The type NativeUtils.
 */
public abstract class NativeUtils {
    
    private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    
    private static final String OPERATING_SYSTEM_ARCH = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
    
    private static final String LIB_NAME;
    
    static {
        if (OPERATING_SYSTEM_NAME.startsWith("mac") || OPERATING_SYSTEM_NAME.startsWith("darwin")) {
            LIB_NAME = "libshenyu_wasm_" + OPERATING_SYSTEM_ARCH + ".dylib";
        } else if (OPERATING_SYSTEM_NAME.startsWith("linux")) {
            LIB_NAME = "libshenyu_wasm_" + OPERATING_SYSTEM_ARCH + ".so";
        } else if (OPERATING_SYSTEM_NAME.startsWith("windows")) {
            LIB_NAME = "libshenyu_wasm_" + OPERATING_SYSTEM_ARCH + ".dll";
        } else {
            throw new ShenyuWasmInitException("Unsupported system !");
        }
    }
    
    private NativeUtils() {
    }
    
    /**
     * detect native library name.
     *
     * @return the native library name
     */
    public static String detectLibName() {
        return LIB_NAME;
    }
    
    /**
     * detect native arch.
     *
     * @return the native arch
     */
    public static String detectArch() {
        return OPERATING_SYSTEM_ARCH;
    }
}
