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

import org.apache.shenyu.wasm.exports.NativeFunction;

/**
 * `Instance` is a Java class that represents a WebAssembly instance.
 *
 * <p>Example:
 * <pre>{@code
 * Instance instance = new Instance(wasmBytes);
 * }</pre>
 */
public class Instance {
    
    /**
     * All WebAssembly exports.
     */
    private final Exports exports;
    
    /**
     * The instance pointer.
     */
    private long instancePointer;
    
    /**
     * The constructor instantiates a new WebAssembly instance based on WebAssembly bytes.
     *
     * @param moduleBytes WebAssembly bytes.
     */
    public Instance(final byte[] moduleBytes) {
        // Native bindings.
        Native.init();
        this.exports = new Exports(this);
        
        long instancePointer = this.nativeInstantiate(this, moduleBytes);
        this.instancePointer = instancePointer;
        
        nativeInitializeExportedFunctions(instancePointer);
        nativeInitializeExportedMemories(instancePointer);
    }
    
    /**
     * The constructor instantiates a new WebAssembly instance.
     */
    protected Instance() {
        this.exports = new Exports(this);
    }
    
    /**
     * Delete an instance object pointer.
     */
    public void close() {
        if (this.instancePointer != 0L) {
            this.nativeDrop(this.instancePointer);
            this.instancePointer = 0L;
        }
    }
    
    /**
     * Delete an instance object pointer, which is called by the garbage collector before an object is removed from the
     * memory.
     */
    @SuppressWarnings("removal")
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
    
    /**
     * get instancePointer.
     *
     * @return the instance pointer
     */
    public long getInstancePointer() {
        return instancePointer;
    }
    
    /**
     * set instancePointer.
     *
     * @param instancePointer the instance pointer
     */
    public void setInstancePointer(final long instancePointer) {
        this.instancePointer = instancePointer;
    }
    
    /**
     * Return the export with the name `name` as an exported function.
     *
     * @param name Name of the exported function.
     * @return the exported function
     * @throws ClassCastException if class cast failed
     */
    public NativeFunction getFunction(final String name) throws ClassCastException {
        return this.exports.getFunction(name);
    }
    
    /**
     * Return the export with the name `name` as an exported memory.
     *
     * @param name Name of the exported memory.
     * @return The exported memory with the name
     * @throws ClassCastException if class cast failed
     */
    public Memory getMemory(final String name) throws ClassCastException {
        return this.exports.getMemory(name);
    }
    
    private native long nativeInstantiate(Instance self, byte[] moduleBytes);
    
    /**
     * Clean native resources.
     *
     * @param instancePointer pointer.
     */
    private native void nativeDrop(long instancePointer);
    
    /**
     * Export native call as java functions.
     *
     * @param instancePointer pointer.
     * @param exportName      name.
     * @param arguments       args.
     * @return java functions.
     */
    protected native Object[] nativeCallExportedFunction(long instancePointer, String exportName, Object[] arguments);
    
    /**
     * nativeInitializeExportedFunctions.
     *
     * @param instancePointer pointer.
     */
    protected static native void nativeInitializeExportedFunctions(long instancePointer);
    
    /**
     * nativeInitializeExportedMemories.
     *
     * @param instancePointer pointer.
     */
    protected static native void nativeInitializeExportedMemories(long instancePointer);
    
}
