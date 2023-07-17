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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModuleTest {
    
    private byte[] getBytes(String filename) throws Exception {
        Path modulePath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toURI());
        return Files.readAllBytes(modulePath);
    }
    
    @Test
    void validate() throws Exception {
        assertTrue(Module.validate(getBytes("tests.wasm")));
    }
    
    @Test
    void invalidate() throws Exception {
        assertFalse(Module.validate(getBytes("invalid.wasm")));
    }
    
    @Test
    void compile() throws Exception {
        assertTrue(new Module(getBytes("tests.wasm")) instanceof Module);
    }
    
    @Test
    void failedToCompile() {
        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            Module module = new Module(getBytes("invalid.wasm"));
        });
        
        String expected = "Failed to compile the module: Validation error: invalid leading byte in type definition";
        assertTrue(exception.getMessage().startsWith(expected));
    }
    
    @Test
    void instantiate() throws Exception {
        Module module = new Module(getBytes("tests.wasm"));
        
        Instance instance = module.instantiate();
        assertEquals(3, (Integer) instance.getFunction("sum").apply(1, 2)[0]);
        
        instance.close();
        module.close();
    }
    
    @Test
    void serialize() throws Exception {
        Module module = new Module(getBytes("tests.wasm"));
        assertTrue(module.serialize() instanceof byte[]);
        module.close();
    }
    
    @Test
    void deserialize() throws Exception {
        Module module = new Module(getBytes("tests.wasm"));
        
        byte[] serialized = module.serialize();
        module = null;
        
        Module deserializedModule = Module.deserialize(serialized);
        assertEquals(3, (Integer) deserializedModule.instantiate().getFunction("sum").apply(1, 2)[0]);
    }
}
