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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.shenyu.wasm.exports.Function;
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
        Function sum = instance.getFunction("sum");
        
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
