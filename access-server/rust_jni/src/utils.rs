// 快速创建Java字符串的宏
#[macro_export]
macro_rules! jstring {
    ($env:expr, $str:expr) => {
        $env.new_string($str).unwrap()
    };
}

// 简化方法调用的宏
#[macro_export]
macro_rules! jni_call {
    ($env:expr, $obj:expr, $method:expr, $sig:expr, $($args:expr),*) => {
        $env.call_method($obj, $method, $sig, &[$($args.into()),*])
    };
}

// 简化静态方法调用的宏
#[macro_export]
macro_rules! jni_static_call {
    ($env:expr, $class:expr, $method:expr, $sig:expr, $($args:expr),*) => {
        $env.call_static_method($class, $method, $sig, &[$($args.into()),*])
    };
}

// 安全获取字符串的宏
#[macro_export]
macro_rules! get_string_safe {
    ($env:expr, $jstring:expr) => {
        match $env.get_string(&$jstring) {
            Ok(java_str) => java_str.to_string_lossy().to_string(),
            Err(e) => {
                info!("❌ 字符串转换失败: {}", e);
                String::from("Error")
            }
        }
    };
}

// 安全查找类
// pub fn find_class_safe<'a>(
//     env: &mut jni::JNIEnv<'a>,
//     class_name: &str
// ) -> Option<jni::objects::JClass<'a>> {
//     match env.find_class(class_name) {
//         Ok(cls) => Some(cls),
//         Err(e) => {
//             handle_jni_error(env, &format!("find class {}", class_name), e);
//             None
//         }
//     }
// }
