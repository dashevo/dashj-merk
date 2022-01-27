use std::ffi::{CString};

#[allow(non_snake_case)]
pub mod android {
    extern crate jni;
    extern crate hex;
    use super::*;

    use self::jni::JNIEnv;
    use self::jni::objects::{JClass, JObject};
    use self::jni::sys::{jstring, jbyteArray, jobject, jboolean, JNI_FALSE, JNI_TRUE};
    use merk::{verify, execute_proof};
    use std::convert::TryInto;

    #[no_mangle]
    pub unsafe extern fn Java_org_dashj_merk_MerkVerifyProof_getVersion(env: JNIEnv, _: JClass) -> jstring {
        let world_ptr = CString::new("0.22-SNAPSHOT");
        let output = env.new_string(world_ptr.unwrap().to_str().unwrap()).expect("Couldn't create java string!");

        output.into_inner()
    }

    fn pop(barry: &[u8]) -> &[u8; 32] {
        barry.try_into().expect("slice with incorrect length")
    }

    #[no_mangle]
    pub unsafe extern fn Java_org_dashj_merk_MerkVerifyProof_verify(env: JNIEnv, _: JClass, java_bytes: jbyteArray, java_expected_hash: jbyteArray, java_map: jobject) -> jboolean {

        let bytes = env.convert_byte_array(java_bytes).expect("invalid bytes");
        let expected_hash = env.convert_byte_array(java_expected_hash).expect("invalid expected hash");
        println!("Parameters passed from Kotlin that were converted to Rust:");
        println!("hash:  {}", hex::encode(expected_hash.clone()));

        let output = verify(bytes.as_slice(), *pop(expected_hash.as_slice()));

        match output  {
            Err(_) => {
                let error = output.err();
                println!("Error decoding json: {:?}", error);
                JNI_FALSE
            }
            Ok(map) => {
                let jmap = env.get_map(java_map.into()).unwrap();

                for key_value_pair in map.all() {
                    let key = key_value_pair.0;
                    let (_exists, value) = key_value_pair.1;

                    let javaKey = env.byte_array_from_slice(&*key).unwrap();
                    let javaValue = env.byte_array_from_slice(value).unwrap();

                    jmap.put(JObject::from(javaKey), JObject::from(javaValue)).expect("Error adding elements to map");
                }
                JNI_TRUE
            }
        }
    }

    #[no_mangle]
    pub unsafe extern fn Java_org_dashj_merk_MerkVerifyProof_extractProofNative(env: JNIEnv, _: JClass, java_bytes: jbyteArray, java_map: jobject) -> jbyteArray{

        let bytes = env.convert_byte_array(java_bytes).expect("invalid bytes");
        println!("Parameters passed from Kotlin that were converted to Rust:");
        println!("bytes: {}", hex::encode(bytes.clone()));

        let output = execute_proof(bytes.as_slice());

        match output  {
            Err(_) => {
                let error = output.err();
                println!("Error decoding json: {:?}", error);
                return env.new_byte_array(0).unwrap()
            }
            Ok((hash, map)) => {
                let jmap = env.get_map(java_map.into()).unwrap();

                for key_value_pair in map.all() {
                    let key = key_value_pair.0;
                    let (_exists, value) = key_value_pair.1;

                    let javaKey = env.byte_array_from_slice(&*key).unwrap();
                    let javaValue = env.byte_array_from_slice(value).unwrap();

                    jmap.put(JObject::from(javaKey), JObject::from(javaValue)).expect("Error adding elements to map");
                }
                env.byte_array_from_slice(hash.as_ref()).unwrap()
            }
        }
    }
}