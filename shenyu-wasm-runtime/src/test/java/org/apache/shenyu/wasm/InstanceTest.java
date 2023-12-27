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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.shenyu.wasm.exports.NativeFunction;
import org.junit.jupiter.api.Test;

class InstanceTest {
    
    private byte[] getBytes() throws Exception {
        URL url = getClass().getClassLoader().getResource("tests.wasm");
        Path modulePath = Paths.get(Objects.requireNonNull(url).toURI());
        return Files.readAllBytes(modulePath);
    }
    
    @Test
    void sum() throws Exception {
        Instance instance = new Instance(getBytes());
        NativeFunction sum = instance.getFunction("sum");
        
        assertEquals(3, sum.apply(1, 2)[0]);
        
        instance.close();
    }
    
    @Test
    void arity0() throws Exception {
        Instance instance = new Instance(getBytes());
        
        assertEquals(42, (Integer) instance.getFunction("arity_0").apply()[0]);
        
        instance.close();
    }
    
    @Test
    void i32I32() throws Exception {
        Instance instance = new Instance(getBytes());
        
        assertEquals(42, (Integer) instance.getFunction("i32_i32").apply(42)[0]);
        
        instance.close();
    }
    
    @Test
    void i64I64() throws Exception {
        Instance instance = new Instance(getBytes());
        
        assertEquals(42L, (Long) instance.getFunction("i64_i64").apply(42L)[0]);
        
        instance.close();
    }
    
    @Test
    void f32F32() throws Exception {
        Instance instance = new Instance(getBytes());
        
        assertEquals(42.0f, (Float) instance.getFunction("f32_f32").apply(42.0f)[0]);
        
        instance.close();
    }
    
    @Test
    void f64F64() throws Exception {
        Instance instance = new Instance(getBytes());
        
        assertEquals(42.0d, (Double) instance.getFunction("f64_f64").apply(42.0d)[0]);
        
        instance.close();
    }
    
    @Test
    void i32I64F32F64F64() throws Exception {
        Instance instance = new Instance(getBytes());
        
        assertEquals(10.0d, (Double) instance.getFunction("i32_i64_f32_f64_f64").apply(1, 2L, 3.0f, 4.0d)[0]);
        
        instance.close();
    }
    
    @Test
    void boolCastedToI32() throws Exception {
        Instance instance = new Instance(getBytes());
        
        assertEquals(1, (int) (Integer) instance.getFunction("bool_casted_to_i32").apply()[0]);
        
        instance.close();
    }
    
    @Test
    void string() throws Exception {
        Instance instance = new Instance(getBytes());
        Memory memory = instance.getMemory("memory");
        ByteBuffer memoryBuffer = memory.buffer();
        
        int pointer = (Integer) instance.getFunction("string").apply()[0];
        
        byte[] stringBytes = new byte[13];
        memoryBuffer.position(pointer);
        memoryBuffer.get(stringBytes);
        
        String expected = "Hello, World!";
        assertEquals(expected, new String(stringBytes));
        
        instance.close();
    }
    
    @Test
    void nothing() throws Exception {
        Instance instance = new Instance(getBytes());
        
        assertNull(instance.getFunction("void").apply());
        
        instance.close();
    }
}
