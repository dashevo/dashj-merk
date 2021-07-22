use std::os::raw::{c_char};
use std::ffi::{CString, CStr};

#[no_mangle]
pub extern fn rust_greeting(to: *const c_char) -> *mut c_char {
    let c_str = unsafe { CStr::from_ptr(to) };
    let recipient = match c_str.to_str() {
        Err(_) => "there",
        Ok(string) => string,
    };

    CString::new("Hello ".to_owned() + recipient).unwrap().into_raw()
}

#[allow(non_snake_case)]
pub mod android {
    extern crate jni;
    extern crate hex;
    use super::*;

    use self::jni::JNIEnv;
    use self::jni::objects::{JClass, JString, JObject, JMap};
    use self::jni::sys::{jstring, jbyteArray, jobject};
    use merk::{verify, execute_proof};
    use std::convert::TryInto;

    #[no_mangle]
    pub unsafe extern fn Java_org_dashj_merk_RustGreetings_greeting(env: JNIEnv, _: JClass, java_pattern: JString) -> jstring {
        // Our Java companion code might pass-in "world" as a string, hence the name.
        let world = rust_greeting(env.get_string(java_pattern).expect("invalid pattern string").as_ptr());
        // Retake pointer so that we can use it below and allow memory to be freed when it goes out of scope.
        let world_ptr = CString::from_raw(world);
        let output = env.new_string(world_ptr.to_str().unwrap()).expect("Couldn't create java string!");

        output.into_inner()
    }

    fn pop(barry: &[u8]) -> &[u8; 32] {
        barry.try_into().expect("slice with incorrect length")
    }
/*
    #[no_mangle]
    pub unsafe extern fn Java_org_dashj_merk_MerkVerifyProof_verify_old(env: JNIEnv, _: JClass, java_bytes: jbyteArray, java_key: jbyteArray, java_expected_hash: jbyteArray) -> jbyteArray {

        let bytes = env.convert_byte_array(java_bytes).expect("invalid bytes");
        let key = env.convert_byte_array(java_key).expect("invalid key");
        let expected_hash = env.convert_byte_array(java_expected_hash).expect("invalid expected hash");
        println!("Parameters passed from Kotlin that were converted to Rust:");
        println!("bytes: {}", hex::encode(bytes.clone()));
        println!("key:   {}", hex::encode(key.clone()));
        println!("hash:  {}", hex::encode(expected_hash.clone()));

        let keys: Vec<u8> = key.iter().cloned().collect();
        let output = verify_proof(bytes.as_slice(), &[keys], *pop(expected_hash.as_slice()));

        if output.is_ok() {
            let result = env.byte_array_from_slice(output.unwrap()[0].as_ref().expect("no values found").as_slice());
            result.unwrap()
        } else {
            let error = output.err();
            println!("Error decoding json: {:?}", error);

            // return array of size 0 to signal failure
            env.new_byte_array(0).unwrap()
        }
    }

    #[no_mangle]
    pub unsafe extern fn Java_org_dashj_merk_MerkVerifyProof_verify(env: JNIEnv, _: JClass, java_bytes: jbyteArray, java_expected_hash: jbyteArray) -> jbyteArray {

        let bytes = env.convert_byte_array(java_bytes).expect("invalid bytes");
        let expected_hash = env.convert_byte_array(java_expected_hash).expect("invalid expected hash");
        println!("Parameters passed from Kotlin that were converted to Rust:");
        println!("bytes: {}", hex::encode(bytes.clone()));
        println!("hash:  {}", hex::encode(expected_hash.clone()));

        let output = verify(bytes.as_slice(), *pop(expected_hash.as_slice()));

        if output.is_ok() {
            let result = env.byte_array_from_slice(output.unwrap()[0].as_ref().expect("no values found").as_slice());
            result.unwrap()
        } else {
            let error = output.err();
            println!("Error decoding json: {:?}", error);

            // return array of size 0 to signal failure
            env.new_byte_array(0).unwrap()
        }
    }
*/
    type jmap = jobject;
    #[no_mangle]
    pub unsafe extern fn Java_org_dashj_merk_MerkVerifyProof_extractProofNative(env: JNIEnv, _: JClass, java_bytes: jbyteArray, java_map: jobject) {

        let bytes = env.convert_byte_array(java_bytes).expect("invalid bytes");
        println!("Parameters passed from Kotlin that were converted to Rust:");
        println!("bytes: {}", hex::encode(bytes.clone()));

        //let keys: Vec<u8> = key.iter().cloned().collect();
        let output = execute_proof(bytes.as_slice());

        match output  {
            Err(_) => {
                let error = output.err();
                println!("Error decoding json: {:?}", error);
            }
            Ok((hash, map)) => {
                let jmap = env.get_map(java_map.into()).unwrap();

                for key_value_pair in map.all() {
                    let key = key_value_pair.0;
                    let (exists, value) = key_value_pair.1;

                    let javaKey = env.byte_array_from_slice(&*key).unwrap();
                    let javaValue = env.byte_array_from_slice(value).unwrap();

                    jmap.put(JObject::from(javaKey), JObject::from(javaValue));
                };
            }
        }
    }


}