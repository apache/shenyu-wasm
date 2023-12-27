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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.shenyu.wasm.exports.Export;
import org.apache.shenyu.wasm.exports.NativeFunction;

/**
 * `Exports` is a Java class that represents the set of WebAssembly exports.
 *
 * <p>Example:
 * <pre>{@code
 * Instance instance = new Instance(wasmBytes);
 *
 * // Get and run an exported function.
 * Object[] result = instance.exports.getFunction("sum").apply(1, 2);
 *
 * // Get, manually downcast, and run an exported function.
 * Export sum = instance.exports.get("sum");
 * Object[] result = ((Function) sum).apply(1, 2);
 * }</pre>
 */
@SuppressWarnings("unused")
public class Exports {
    
    private final Map<String, Export> inner;
    
    private Instance instance;
    
    /**
     * Lambda expression for currying. This takes a function name and returns the function to call WebAssembly
     * function.
     */
    private final Function<String, NativeFunction> functionWrapperGenerator
            = functionName -> arguments -> this.instance.nativeCallExportedFunction(
            this.instance.getInstancePointer(), functionName, arguments);
    
    /**
     * The constructor instantiates new exported functions.
     *
     * @param instance Instance object which holds the exports object.
     */
    protected Exports(final Instance instance) {
        this.inner = new HashMap<>();
        this.instance = instance;
    }
    
    /**
     * Return the export with the name `name`.
     *
     * @param name name of the export to return.
     * @return the export
     */
    public Export get(final String name) {
        return this.inner.get(name);
    }
    
    /**
     * Return the export with the name `name` as an exported function.
     *
     * @param name Name of the exported function.
     * @return the exported function
     * @throws ClassCastException if class cast failed
     */
    public NativeFunction getFunction(final String name) throws ClassCastException {
        return (NativeFunction) this.inner.get(name);
    }
    
    /**
     * Return the export with the name `name` as an exported memory.
     *
     * @param name Name of the exported memory.
     * @return The exported memory with the name
     * @throws ClassCastException if class cast failed
     */
    public Memory getMemory(final String name) throws ClassCastException {
        return (Memory) this.inner.get(name);
    }
    
    /**
     * Called by Rust to add a new exported function.
     */
    private void addFunction(final String name) {
        this.inner.put(name, this.generateFunctionWrapper(name));
    }
    
    /**
     * Called by Rust to add a new exported memory.
     */
    private void addMemory(final String name, final Memory memory) {
        this.inner.put(name, memory);
    }
    
    /**
     * Generate the exported function wrapper.
     */
    private NativeFunction generateFunctionWrapper(final String functionName) {
        return this.functionWrapperGenerator.apply(functionName);
    }
}
