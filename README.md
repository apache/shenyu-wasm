# shenyu-wasm

This repository is forked from [wasmer-java](https://github.com/wasmerio/wasmer-java), which simplifies dependencies by directly packing the dylib into the jar, and decompressing and loading the dylib at runtime, ready to use out of the box.

If you find anything unreasonable, please send email `dev@shenyu.apache.org` or `zhangzicheng@apache.org`.

## Note: Only 64-bit CPU architecture is supported now !
If shenyu-wasm does not support your CPU architecture or system, you need to build dylib yourself, check `Build dylib on my own` below.

## How to use this library ?
### step1 add dependency
maven
```xml
<dependency>
    <groupId>org.apache.shenyu</groupId>
    <artifactId>shenyu-wasm-runtime</artifactId>
    <version>${x.y.z}</version>
</dependency>
```
gradle
```groovy
compile "org.apache.shenyu:shenyu-wasm-runtime:${x.y.z}"
```
### step2 write native source code
Below is a example of rust(any other language that compiles to WebAssembly can be used here):
```rust
#[no_mangle]
pub extern fn sum(x: i32, y: i32) -> i32 {
    x + y
}
```
### step3 compile source code to a wasm file

### step4 execute wasm in java
```java
class Example {
    public static void main(String[] args) {
        // `simple.wasm` is located at `tests/resources/`.
        Path wasmPath = Paths.get(Example.class.getClassLoader().getResource("simple.wasm").getPath());
        // Reads the WebAssembly module as bytes.
        byte[] wasmBytes = Files.readAllBytes(wasmPath);
        // Instantiates the WebAssembly module.
        Instance instance = new Instance(wasmBytes);
        // Calls an exported function, and returns an object array.
        Object[] results = instance.exports.getFunction("sum").apply(5, 37);
        System.out.println(results[0]); // 42
        // Drops an instance object pointer which is stored in Rust.
        instance.close();
    }
}
```

## Build dylib on my own
### step1 install rust
### step2 build dylib
```shell
cd ~
git clone https://github.com/apache/shenyu-wasm.git
cd shenyu-wasm/shenyu-wasm-build/
cargo build --release
```
In macos, you will get the `dylib` in `~/shenyu-wasm/shenyu-wasm-build/target/libshenyu_wasm.dylib`;

In linux, you will get the `so` in `~/shenyu-wasm/shenyu-wasm-build/target/libshenyu_wasm.so`;

In windows, you will get the `dll` in `~/shenyu-wasm/shenyu-wasm-build/target/libshenyu_wasm.dll`;

### step3 use dylib
Renaming the dylib we built in the previous step, with a naming format of `libshenyu_wasm_$(architecture).$(os_cdylib_suffix)`(e.g. libshenyu_wasm_x86_64.dylib), then put it in your own project module(the full path should like `${your_java_project_name}/${your_module_name}/src/main/resources/libshenyu_wasm_$(architecture).$(os_cdylib_suffix)`).

Finally, at runtime, `native-lib-loader` will load it.
