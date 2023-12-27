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

pub use jni::errors::Error;
use jni::{errors::ErrorKind, JNIEnv};
use std::thread;

pub fn runtime_error(message: String) -> Error {
    Error::from_kind(ErrorKind::Msg(message))
}

#[derive(Debug)]
pub enum JOption<T> {
    Some(T),
    None,
}

impl<T> JOption<T> {
    pub fn unwrap_or(self, default: T) -> T {
        match self {
            JOption::Some(result) => result,
            JOption::None => default,
        }
    }
}

pub fn joption_or_throw<T>(env: &JNIEnv, result: thread::Result<Result<T, Error>>) -> JOption<T> {
    match result {
        Ok(result) => match result {
            Ok(result) => JOption::Some(result),
            Err(error) => {
                if !env.exception_check().unwrap() {
                    env.throw_new("java/lang/RuntimeException", &error.to_string())
                        .expect("Cannot throw an `java/lang/RuntimeException` exception.");
                }

                JOption::None
            }
        },
        Err(ref error) => {
            env.throw_new("java/lang/RuntimeException", format!("{:?}", error))
                .expect("Cannot throw an `java/lang/RuntimeException` exception.");

            JOption::None
        }
    }
}
