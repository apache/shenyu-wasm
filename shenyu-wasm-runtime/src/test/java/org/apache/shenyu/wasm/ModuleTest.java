/*
 * MIT License
 *
 * Copyright (c) 2019-present Wasmer, Inc. and its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
    
    private byte[] getBytes(final String filename) throws Exception {
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
