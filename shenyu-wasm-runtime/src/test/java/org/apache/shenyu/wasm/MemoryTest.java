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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class MemoryTest {
    
    private byte[] getBytes(final String filename) throws Exception {
        Path modulePath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toURI());
        return Files.readAllBytes(modulePath);
    }
    
    @Test
    void size() throws Exception {
        Instance instance = new Instance(getBytes("tests.wasm"));
        Memory memory = instance.getMemory("memory");
        
        assertEquals(1114112, memory.buffer().limit());
        
        instance.close();
    }
    
    @Test
    void readStaticallyAllocatedDataInMemory() throws Exception {
        Instance instance = new Instance(getBytes("tests.wasm"));
        
        byte[] expectedData = "Hello, World!".getBytes();
        
        int pointer = (Integer) instance.getFunction("string").apply()[0];
        Memory memory = instance.getMemory("memory");
        ByteBuffer memoryBuffer = memory.buffer();
        
        byte[] readData = new byte[expectedData.length];
        memoryBuffer.position(pointer);
        memoryBuffer.get(readData);
        
        assertArrayEquals(expectedData, readData);
        
        instance.close();
    }
    
    @Test
    void readMemory() throws Exception {
        Instance instance = new Instance(getBytes("tests.wasm"));
        
        Memory memory = instance.getMemory("memory");
        ByteBuffer memoryBuffer = memory.buffer();
        
        byte[] readData = new byte[5];
        memoryBuffer.get(readData);
        
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0}, readData);
        
        instance.close();
    }
    
    @Test
    void writeMemory() throws Exception {
        Instance instance = new Instance(getBytes("tests.wasm"));
        
        Memory memory = instance.getMemory("memory");
        ByteBuffer memoryBuffer = memory.buffer();
        
        byte[] writtenData = new byte[]{1, 2, 3, 4, 5};
        memoryBuffer.put(writtenData);
        
        byte[] readData = new byte[5];
        memoryBuffer.position(0);
        memoryBuffer.get(readData);
        
        assertArrayEquals(writtenData, readData);
        
        ByteBuffer memoryBuffer2 = memory.buffer();
        byte[] readData2 = new byte[5];
        memoryBuffer2.get(readData2);
        
        assertArrayEquals(writtenData, readData2);
        
        instance.close();
    }
    
    @Test
    void noMemory() throws Exception {
        Instance instance = new Instance(getBytes("no_memory.wasm"));
        Memory memory = instance.getMemory("memory");
        
        assertNull(memory);
        
        instance.close();
    }
    
    @Test
    void javaBorrowsRustMemory() throws Exception {
        Instance instance = new Instance(getBytes("tests.wasm"));
        
        Memory memory = instance.getMemory("memory");
        ByteBuffer memoryBuffer = memory.buffer();
        
        int pointer = (Integer) instance.getFunction("string").apply()[0];
        byte[] data = new byte[13];
        memoryBuffer.position(pointer);
        memoryBuffer.get(data);
        
        assertEquals("Hello, World!", new String(data));
        
        memoryBuffer.position(pointer);
        memoryBuffer.put(new byte[]{'A'});
        
        memory = instance.getMemory("memory");
        memoryBuffer = memory.buffer();
        
        pointer = (Integer) instance.getFunction("string").apply()[0];
        data = new byte[13];
        memoryBuffer.position(pointer);
        memoryBuffer.get(data);
        
        assertEquals("Aello, World!", new String(data));
        
        instance.close();
    }
    
    @Test
    void memoryGrow() throws Exception {
        Instance instance = new Instance(getBytes("tests.wasm"));
        Memory memory = instance.getMemory("memory");
        
        int oldSize = memory.buffer().limit();
        assertEquals(1114112, oldSize);
        
        memory.grow(1);
        
        int newSize = memory.buffer().limit();
        assertEquals(1179648, newSize);
        assertEquals(65536, newSize - oldSize);
        
        instance.close();
    }
}
