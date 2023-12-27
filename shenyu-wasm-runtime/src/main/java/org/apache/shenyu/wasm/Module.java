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

/**
 * `Module` is a Java class that represents a WebAssembly module.
 *
 * <p>Example:
 * <pre>{@code
 * boolean isValid = Module.validate(wasmBytes);
 *
 * Module module = new Module(wasmBytes);
 * Instance instance = module.instantiate();
 * }</pre>
 */
@SuppressWarnings("unused")
public class Module {
    
    private long modulePointer;
    
    private Module() {
        // Native bindings.
        Native.init();
    }
    
    /**
     * The constructor instantiates a new WebAssembly module based on WebAssembly bytes.
     *
     * @param moduleBytes webassembly bytes.
     */
    public Module(final byte[] moduleBytes) {
        // Native bindings.
        Native.init();
        this.modulePointer = this.nativeModuleInstantiate(this, moduleBytes);
    }
    
    /**
     * Create an original Module object from a byte array.
     *
     * @param serializedBytes serialized bytes
     * @return Module object.
     */
    public static Module deserialize(final byte[] serializedBytes) {
        Module module = new Module();
        module.modulePointer = Module.nativeDeserialize(module, serializedBytes);
        return module;
    }
    
    /**
     * Check that given bytes represent a valid WebAssembly module.
     *
     * @param moduleBytes WebAssembly bytes.
     * @return true if, and only if, given bytes are valid as a WebAssembly module.
     */
    public static boolean validate(final byte[] moduleBytes) {
        return Module.nativeValidate(moduleBytes);
    }
    
    /**
     * Delete a module object pointer.
     */
    public void close() {
        if (this.modulePointer != 0L) {
            this.nativeDrop(this.modulePointer);
            this.modulePointer = 0L;
        }
    }
    
    /**
     * Delete a module object pointer, which is called by the garbage collector before an object is removed from the
     * memory.
     */
    @Override
    public void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
    
    /**
     * Create an instance object based on a module object.
     *
     * @return Instance object.
     */
    public Instance instantiate() {
        Instance instance = new Instance();
        long instancePointer = this.nativeInstantiate(this.modulePointer, instance);
        instance.setInstancePointer(instancePointer);
        
        Instance.nativeInitializeExportedFunctions(instancePointer);
        Instance.nativeInitializeExportedMemories(instancePointer);
        return instance;
    }
    
    /**
     * Create a serialized byte array from a WebAssembly module.
     *
     * @return Serialized bytes.
     */
    public byte[] serialize() {
        return this.nativeSerialize(this.modulePointer);
    }
    
    private native long nativeModuleInstantiate(Module self, byte[] moduleBytes);
    
    private native void nativeDrop(long modulePointer);
    
    private native long nativeInstantiate(long modulePointer, Instance instance);
    
    private static native boolean nativeValidate(byte[] moduleBytes);
    
    private native byte[] nativeSerialize(long modulePointer);
    
    private static native long nativeDeserialize(Module module, byte[] serializedBytes);
    
}
