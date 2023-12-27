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

use crate::{
    exception::{joption_or_throw, runtime_error, Error},
    types::{jptr, Pointer},
};
use jni::{
    objects::{JClass, JObject},
    sys::jint,
    JNIEnv,
};
use std::{cell::Cell, panic, rc::Rc, slice};
use wasmer::Memory as WasmMemory;
use wasmer_runtime::memory::MemoryView;
use wasmer_runtime::units::Pages;

#[derive(Clone)]
pub struct Memory {
    pub memory: Rc<WasmMemory>,
}

impl Memory {
    pub fn new(memory: Rc<WasmMemory>) -> Self {
        Self { memory }
    }

    pub fn grow(&self, number_of_pages: u32) -> Result<u32, Error> {
        self.memory
            .grow(Pages(number_of_pages))
            .map(|previous_pages| previous_pages.0)
            .map_err(|e| runtime_error(format!("Failed to grow the memory: {}", e)))
    }
}

#[no_mangle]
pub extern "system" fn Java_org_apache_shenyu_wasm_Memory_nativeMemoryView(
    env: JNIEnv,
    _class: JClass,
    memory_object: JObject,
    memory_pointer: jptr,
) {
    let output = panic::catch_unwind(|| {
        let memory: &Memory = Into::<Pointer<Memory>>::into(memory_pointer).borrow();
        let view: MemoryView<u8> = memory.memory.view();
        let data = unsafe {
            slice::from_raw_parts_mut(view[..].as_ptr() as *mut Cell<u8> as *mut u8, view.len())
        };

        // Create a new `JByteBuffer`, aka `java.nio.ByteBuffer`,
        // borrowing the data from the WebAssembly memory.
        let byte_buffer = env.new_direct_byte_buffer(data)?;

        // Try to rewrite the `org.apache.shenyu.wasm.Memory.buffer` attribute by
        // calling the `org.apache.shenyu.wasm.Memory.setBuffer` method.
        env.call_method(
            memory_object,
            "setBuffer",
            "(Ljava/nio/ByteBuffer;)V",
            &[JObject::from(byte_buffer).into()],
        )?;

        Ok(())
    });

    joption_or_throw(&env, output);
}

#[no_mangle]
pub extern "system" fn Java_org_apache_shenyu_wasm_Memory_nativeMemoryGrow(
    env: JNIEnv,
    _class: JClass,
    memory_object: JObject,
    memory_pointer: jptr,
    number_of_pages: jint,
) -> jint {
    let output = panic::catch_unwind(|| {
        let memory: &Memory = Into::<Pointer<Memory>>::into(memory_pointer).borrow();
        let old_pages = memory.grow(number_of_pages as u32)?;

        let view: MemoryView<u8> = memory.memory.view();
        let data = unsafe {
            std::slice::from_raw_parts_mut(
                view[..].as_ptr() as *mut Cell<u8> as *mut u8,
                view.len(),
            )
        };
        // Create a new `JByteBuffer`, aka `java.nio.ByteBuffer`,
        // borrowing the data from the WebAssembly memory.
        let byte_buffer = env.new_direct_byte_buffer(data)?;

        // Try to rewrite the `org.apache.shenyu.wasm.Memory.buffer` attribute by
        // calling the `org.apache.shenyu.wasm.Memory.setBuffer` method.
        env.call_method(
            memory_object,
            "setBuffer",
            "(Ljava/nio/ByteBuffer;)V",
            &[JObject::from(byte_buffer).into()],
        )?;

        Ok(old_pages as i32)
    });

    joption_or_throw(&env, output).unwrap_or(0)
}

pub mod java {
    use crate::{
        exception::Error,
        instance::Instance,
        types::{jptr, Pointer},
    };
    use jni::{objects::JObject, JNIEnv};

    pub fn initialize_memories(env: &JNIEnv, instance: &Instance) -> Result<(), Error> {
        let exports_object: JObject = env
            .get_field(
                instance.java_instance_object.as_obj(),
                "exports",
                "Lorg/apache/shenyu/wasm/Exports;",
            )?
            .l()?;

        // Get the `org.apache.shenyu.wasm.Memory` class.
        let memory_class = env.find_class("org/apache/shenyu/wasm/Memory")?;

        for (memory_name, memory) in &instance.memories {
            // Instantiate the `Memory` class.
            let memory_object = env.new_object(memory_class, "()V", &[])?;

            // Try to set the memory pointer to the field `org.apache.shenyu.wasm.Memory.memoryPointer`.
            let memory_pointer: jptr = Pointer::new(memory.clone()).into();
            env.set_field(memory_object, "memoryPointer", "J", memory_pointer.into())?;

            // Add the newly created `org.apache.shenyu.wasm.Memory` in the
            // `org.apache.shenyu.wasm.Exports` collection.
            env.call_method(
                exports_object,
                "addMemory",
                "(Ljava/lang/String;Lorg/apache/shenyu/wasm/Memory;)V",
                &[
                    JObject::from(env.new_string(memory_name)?).into(),
                    memory_object.into(),
                ],
            )?;
        }

        Ok(())
    }
}
