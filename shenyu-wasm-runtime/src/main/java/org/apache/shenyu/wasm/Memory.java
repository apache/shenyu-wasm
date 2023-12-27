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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.shenyu.wasm.exports.Export;

/**
 * `Memory` is a Java class that represents a WebAssembly memory.
 *
 * <p>Example:
 * <pre>{@code
 * Instance instance = new Instance(wasmBytes);
 * Memory memory = instance.exports.getMemory("memory-name");
 * ByteBuffer memoryBuffer = memory.buffer();
 *
 * // Write bytes.
 * memoryBuffer.position(0);
 * memoryBuffer.put(new byte[]{1, 2, 3, 4, 5});
 *
 * // Read bytes.
 * byte[] bytes = new byte[5];
 * memoryBuffer.position(0);
 * memoryBuffer.get(bytes);
 * }</pre>
 */
@SuppressWarnings("unused")
public final class Memory implements Export {
    
    /**
     * Represents the actual WebAssembly memory data, borrowed from the runtime (in Rust). The `setBuffer` method must
     * be used to set this attribute.
     */
    private ByteBuffer buffer;
    
    private long memoryPointer;
    
    private Memory() {
        // This object is instantiated by Rust.
    }
    
    /**
     * Return a _new_ direct byte buffer borrowing the memory data.
     *
     * @return A new direct byte buffer.
     */
    public ByteBuffer buffer() {
        this.nativeMemoryView(this, this.memoryPointer);
        
        return this.buffer;
    }
    
    /**
     * Set the `ByteBuffer` of this memory. See `Memory.buffer` to learn more.
     *
     * <p>In addition, this method correctly sets the endianess of the `ByteBuffer`.
     */
    private void setBuffer(final ByteBuffer buffer) {
        this.buffer = buffer;
        
        // Ensure the endianess matches WebAssemly specification.
        if (this.buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
    }
    
    /**
     * Grow this memory by the specified number of pages.
     *
     * @param page The number of pages to grow. 1 page size is 64KiB.
     * @return The previous number of pages.
     */
    public int grow(final int page) {
        return this.nativeMemoryGrow(this, this.memoryPointer, page);
    }
    
    private native void nativeMemoryView(Memory memory, long memoryPointer);
    
    private native int nativeMemoryGrow(Memory memory, long memoryPointer, int page);
    
}
