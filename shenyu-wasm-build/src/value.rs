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

use crate::exception::{runtime_error, Error};
use jni::{errors::ErrorKind, objects::JObject, JNIEnv};
use std::convert::TryFrom;
use wasmer_runtime::Value as WasmValue;

/// Value wrapping the real WebAssembly value.
pub struct Value(WasmValue);

impl Value {
    pub fn inner(self) -> WasmValue {
        self.0
    }
}

pub const INT_CLASS: &str = "java/lang/Integer";
pub const LONG_CLASS: &str = "java/lang/Long";
pub const FLOAT_CLASS: &str = "java/lang/Float";
pub const DOUBLE_CLASS: &str = "java/lang/Double";

impl TryFrom<(&JNIEnv<'_>, JObject<'_>)> for Value {
    type Error = Error;

    fn try_from((env, jobject): (&JNIEnv, JObject)) -> Result<Self, Self::Error> {
        if jobject.is_null() {
            return Err(ErrorKind::NullPtr("`try_from` receives a null object").into());
        }

        Ok(Value(
            if env.is_instance_of(jobject, INT_CLASS).unwrap_or(false) {
                WasmValue::I32(env.call_method(jobject, "intValue", "()I", &[])?.i()?)
            } else if env.is_instance_of(jobject, LONG_CLASS).unwrap_or(false) {
                WasmValue::I64(env.call_method(jobject, "longValue", "()J", &[])?.j()?)
            } else if env.is_instance_of(jobject, FLOAT_CLASS).unwrap_or(false) {
                WasmValue::F32(env.call_method(jobject, "floatValue", "()F", &[])?.f()?)
            } else if env.is_instance_of(jobject, DOUBLE_CLASS).unwrap_or(false) {
                WasmValue::F64(env.call_method(jobject, "doubleValue", "()D", &[])?.d()?)
            } else {
                return Err(runtime_error(format!(
                    "Could not convert argument {:?} to a WebAssembly value.",
                    jobject
                )));
            },
        ))
    }
}
