use jni::objects::{ JClass, JObject, JString, JByteArray };
use jni::sys::{ jboolean, jstring, jint, JNI_TRUE, JNI_FALSE };
use jni::JNIEnv;
use std::ffi::c_void;
use android_logger::Config;
use log::{ info, error };
use tokio::runtime::Runtime;
use std::sync::OnceLock;

mod utils;
mod database;
mod entities;
mod services;
mod p2p;

use database::init_database;
use services::{ UserService, OrderService };
use entities::user::{ CreateUserDto, UpdateUserDto };
use entities::order::{ CreateOrderDto, UpdateOrderDto };
use p2p::{ AndroidWebRTCClient, WebRTCConfig, WebRTCCallback };

use std::sync::{ Arc, Mutex as StdMutex };
use tokio::sync::Mutex;
use std::collections::HashMap;

// 全局 Tokio 运行时
static RUNTIME: OnceLock<Runtime> = OnceLock::new();

// 全局 WebRTC 客户端管理器
static WEBRTC_CLIENTS: OnceLock<
    StdMutex<HashMap<String, Arc<Mutex<AndroidWebRTCClient>>>>
> = OnceLock::new();

// 全局连接任务管理器
static CONNECTION_TASKS: OnceLock<
    StdMutex<HashMap<String, tokio::task::JoinHandle<()>>>
> = OnceLock::new();

// Android WebRTC 回调实现
struct AndroidWebRTCCallbackImpl {
    jvm: Arc<jni::JavaVM>,
    callback_object: jni::objects::GlobalRef,
}

impl AndroidWebRTCCallbackImpl {
    fn new(jvm: Arc<jni::JavaVM>, callback_object: jni::objects::GlobalRef) -> Self {
        Self {
            jvm,
            callback_object,
        }
    }

    fn call_java_method(&self, method_name: &str, method_sig: &str, args: &[jni::objects::JValue]) {
        if let Ok(mut env) = self.jvm.attach_current_thread() {
            let _ = env.call_method(&self.callback_object, method_name, method_sig, args);
        }
    }
}

impl WebRTCCallback for AndroidWebRTCCallbackImpl {
    fn on_connected(&self, client_id: String) {
        if let Ok(env) = self.jvm.attach_current_thread() {
            if let Ok(jstring) = env.new_string(&client_id) {
                let args = &[jni::objects::JValue::Object(&jstring)];
                self.call_java_method("onConnected", "(Ljava/lang/String;)V", args);
            }
        }
    }

    fn on_user_joined(&self, user_id: String) {
        if let Ok(env) = self.jvm.attach_current_thread() {
            if let Ok(jstring) = env.new_string(&user_id) {
                let args = &[jni::objects::JValue::Object(&jstring)];
                self.call_java_method("onUserJoined", "(Ljava/lang/String;)V", args);
            }
        }
    }

    fn on_user_left(&self, user_id: String) {
        if let Ok(env) = self.jvm.attach_current_thread() {
            if let Ok(jstring) = env.new_string(&user_id) {
                let args = &[jni::objects::JValue::Object(&jstring)];
                self.call_java_method("onUserLeft", "(Ljava/lang/String;)V", args);
            }
        }
    }

    fn on_message_received(&self, sender_id: String, message: String, is_p2p: bool) {
        if let Ok(env) = self.jvm.attach_current_thread() {
            if
                let (Ok(sender_jstring), Ok(message_jstring)) = (
                    env.new_string(&sender_id),
                    env.new_string(&message),
                )
            {
                let args = &[
                    jni::objects::JValue::Object(&sender_jstring),
                    jni::objects::JValue::Object(&message_jstring),
                    jni::objects::JValue::Bool(
                        if is_p2p {
                            jni::sys::JNI_TRUE
                        } else {
                            jni::sys::JNI_FALSE
                        }
                    ),
                ];
                self.call_java_method(
                    "onMessageReceived",
                    "(Ljava/lang/String;Ljava/lang/String;Z)V",
                    args
                );
            }
        }
    }

    fn on_connection_state_changed(&self, state: String) {
        if let Ok(env) = self.jvm.attach_current_thread() {
            if let Ok(jstring) = env.new_string(&state) {
                let args = &[jni::objects::JValue::Object(&jstring)];
                self.call_java_method("onConnectionStateChanged", "(Ljava/lang/String;)V", args);
            }
        }
    }

    fn on_error(&self, error: String) {
        if let Ok(env) = self.jvm.attach_current_thread() {
            if let Ok(jstring) = env.new_string(&error) {
                let args = &[jni::objects::JValue::Object(&jstring)];
                self.call_java_method("onError", "(Ljava/lang/String;)V", args);
            }
        }
    }

    fn on_binary_data_received(&self, sender_id: String, data: Vec<u8>, is_p2p: bool) {
        if let Ok(env) = self.jvm.attach_current_thread() {
            if let Ok(sender_jstring) = env.new_string(&sender_id) {
                // 创建字节数组
                if let Ok(byte_array) = env.new_byte_array(data.len() as i32) {
                    let data_i8: Vec<i8> = data
                        .iter()
                        .map(|&b| b as i8)
                        .collect();
                    if env.set_byte_array_region(&byte_array, 0, &data_i8).is_ok() {
                        let args = &[
                            jni::objects::JValue::Object(&sender_jstring),
                            jni::objects::JValue::Object(&byte_array),
                            jni::objects::JValue::Bool(
                                if is_p2p {
                                    jni::sys::JNI_TRUE
                                } else {
                                    jni::sys::JNI_FALSE
                                }
                            ),
                        ];
                        self.call_java_method(
                            "onBinaryDataReceived",
                            "(Ljava/lang/String;[BZ)V",
                            args
                        );
                    }
                }
            }
        }
    }
}

/// JNI_OnLoad - 在库加载时自动调用
#[no_mangle]
pub extern "C" fn JNI_OnLoad(_vm: *mut jni::sys::JavaVM, _reserved: *mut c_void) -> jni::sys::jint {
    // 初始化日志
    android_logger::init_once(
        Config::default().with_max_level(log::LevelFilter::Info).with_tag("f_rust_jni")
    );
    info!("🚀 JNI_OnLoad");

    jni::sys::JNI_VERSION_1_6
}

#[no_mangle]
pub extern "C" fn Java_com_example_lib_Bootstrap_soInit(
    mut env: JNIEnv,
    _class: JClass,
    context: JObject
) {
    if let Ok(result) = env.call_method(&context, "getPackageName", "()Ljava/lang/String;", &[]) {
        let jstring = JString::from(result.l().unwrap());
        let package_name = get_string_safe!(env, jstring);
        info!("📦 getPackageName: {}", package_name);
    } else {
        info!("📦 getPackageName error");
    }
}

/// 获取或创建 Tokio 运行时
fn get_runtime() -> &'static Runtime {
    RUNTIME.get_or_init(|| { Runtime::new().expect("Failed to create Tokio runtime") })
}

/// 获取或创建 WebRTC 客户端管理器
fn get_webrtc_clients() -> &'static StdMutex<HashMap<String, Arc<Mutex<AndroidWebRTCClient>>>> {
    WEBRTC_CLIENTS.get_or_init(|| StdMutex::new(HashMap::new()))
}

/// 获取或创建连接任务管理器
fn get_connection_tasks() -> &'static StdMutex<HashMap<String, tokio::task::JoinHandle<()>>> {
    CONNECTION_TASKS.get_or_init(|| StdMutex::new(HashMap::new()))
}

/// 生成客户端ID
fn generate_client_id(room_id: &str) -> String {
    use std::time::{ SystemTime, UNIX_EPOCH };
    let timestamp = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_millis();
    format!("{}_{}", room_id, timestamp)
}

/// 初始化数据库
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_initDatabase(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString
) -> jboolean {
    let path_str = match env.get_string(&db_path) {
        Ok(s) => s.to_string_lossy().to_string(),
        Err(e) => {
            error!("❌ 获取数据库路径失败: {}", e);
            return JNI_FALSE;
        }
    };

    info!("🗄️ 初始化数据库: {}", path_str);

    let rt = get_runtime();
    match rt.block_on(init_database(&path_str)) {
        Ok(_) => {
            info!("✅ 数据库初始化成功");
            JNI_TRUE
        }
        Err(e) => {
            error!("❌ 数据库初始化失败: {}", e);
            JNI_FALSE
        }
    }
}

/// 创建用户
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_createUser(
    mut env: JNIEnv,
    _class: JClass,
    username: JString,
    password: JString,
    age: jint
) -> jstring {
    let username_str = get_string_safe!(env, username);
    let password_str = get_string_safe!(env, password);

    let dto = CreateUserDto {
        username: username_str.clone(),
        password: password_str,
        age: age as i32,
    };

    let rt = get_runtime();
    match rt.block_on(UserService::create_user(dto)) {
        Ok(user) => {
            info!("✅ 用户创建成功: {}", username_str);
            let json = serde_json::to_string(&user).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Err(e) => {
            error!("❌ 用户创建失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 根据 ID 获取用户
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getUserById(
    env: JNIEnv,
    _class: JClass,
    id: jint
) -> jstring {
    let rt = get_runtime();
    match rt.block_on(UserService::get_user_by_id(id as i32)) {
        Ok(Some(user)) => {
            let json = serde_json::to_string(&user).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Ok(None) => { jstring!(env, r#"{"error": "用户不存在"}"#).into_raw() }
        Err(e) => {
            error!("❌ 获取用户失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 获取所有用户
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getAllUsers(
    env: JNIEnv,
    _class: JClass,
    page: jint,
    per_page: jint
) -> jstring {
    let rt = get_runtime();
    match rt.block_on(UserService::get_all_users(page as u64, per_page as u64)) {
        Ok((users, total)) => {
            let result =
                serde_json::json!({
                "users": users,
                "total": total,
                "page": page,
                "per_page": per_page
            });
            let json = serde_json::to_string(&result).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Err(e) => {
            error!("❌ 获取用户列表失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 更新用户
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_updateUser(
    mut env: JNIEnv,
    _class: JClass,
    id: jint,
    username: JString,
    password: JString,
    age: jint
) -> jstring {
    let username_str = get_string_safe!(env, username);
    let password_str = get_string_safe!(env, password);

    let dto = UpdateUserDto {
        username: if username_str.is_empty() {
            None
        } else {
            Some(username_str)
        },
        password: if password_str.is_empty() {
            None
        } else {
            Some(password_str)
        },
        age: if age < 0 {
            None
        } else {
            Some(age as i32)
        },
    };

    let rt = get_runtime();
    match rt.block_on(UserService::update_user(id as i32, dto)) {
        Ok(Some(user)) => {
            let json = serde_json::to_string(&user).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Ok(None) => { jstring!(env, r#"{"error": "用户不存在"}"#).into_raw() }
        Err(e) => {
            error!("❌ 更新用户失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 删除用户
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_deleteUser(
    _env: JNIEnv,
    _class: JClass,
    id: jint
) -> jboolean {
    let rt = get_runtime();
    match rt.block_on(UserService::delete_user(id as i32)) {
        Ok(true) => {
            info!("✅ 用户删除成功 ID: {}", id);
            JNI_TRUE
        }
        Ok(false) => {
            info!("❌ 用户不存在 ID: {}", id);
            JNI_FALSE
        }
        Err(e) => {
            error!("❌ 删除用户失败: {}", e);
            JNI_FALSE
        }
    }
}

/// 验证用户登录
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_verifyUser(
    mut env: JNIEnv,
    _class: JClass,
    username: JString,
    password: JString
) -> jstring {
    let username_str = get_string_safe!(env, username);
    let password_str = get_string_safe!(env, password);

    let rt = get_runtime();
    match rt.block_on(UserService::verify_user(&username_str, &password_str)) {
        Ok(Some(user)) => {
            let json = serde_json::to_string(&user).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Ok(None) => { jstring!(env, r#"{"error": "用户名或密码错误"}"#).into_raw() }
        Err(e) => {
            error!("❌ 用户验证失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 获取用户总数
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getUserCount(
    _env: JNIEnv,
    _class: JClass
) -> jint {
    let rt = get_runtime();
    match rt.block_on(UserService::get_user_count()) {
        Ok(count) => count as jint,
        Err(e) => {
            error!("❌ 获取用户总数失败: {}", e);
            -1
        }
    }
}

/// 测试方法
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getString(
    env: JNIEnv,
    _class: JClass
) -> jstring {
    jstring!(env, "Hello from Rust with SeaORM! 🦀").into_raw()
}

// ===== ORDER JNI 接口 =====

/// 创建订单
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_createOrder(
    mut env: JNIEnv,
    _class: JClass,
    user_id: jint,
    product_name: JString,
    quantity: jint,
    price: f64,
    notes: JString
) -> jstring {
    let product_name_str = get_string_safe!(env, product_name);
    let notes_str = get_string_safe!(env, notes);

    let dto = CreateOrderDto {
        user_id: user_id as i32,
        product_name: product_name_str,
        quantity: quantity as i32,
        price,
        notes: if notes_str.is_empty() {
            None
        } else {
            Some(notes_str)
        },
    };

    let rt = get_runtime();
    match rt.block_on(OrderService::create_order(dto)) {
        Ok(order) => {
            info!("✅ 订单创建成功: {}", order.product_name);
            let json = serde_json::to_string(&order).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Err(e) => {
            error!("❌ 订单创建失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 根据 ID 获取订单
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getOrderById(
    env: JNIEnv,
    _class: JClass,
    id: jint
) -> jstring {
    let rt = get_runtime();
    match rt.block_on(OrderService::get_order_by_id(id as i32)) {
        Ok(Some(order)) => {
            let json = serde_json::to_string(&order).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Ok(None) => { jstring!(env, r#"{"error": "订单不存在"}"#).into_raw() }
        Err(e) => {
            error!("❌ 获取订单失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 根据用户ID获取订单列表
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getOrdersByUserId(
    env: JNIEnv,
    _class: JClass,
    user_id: jint,
    page: jint,
    per_page: jint
) -> jstring {
    let rt = get_runtime();
    match
        rt.block_on(
            OrderService::get_orders_by_user_id(user_id as i32, page as u64, per_page as u64)
        )
    {
        Ok((orders, total)) => {
            let result =
                serde_json::json!({
                "orders": orders,
                "total": total,
                "page": page,
                "per_page": per_page,
                "user_id": user_id
            });
            let json = serde_json::to_string(&result).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Err(e) => {
            error!("❌ 获取用户订单列表失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 获取所有订单
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getAllOrders(
    env: JNIEnv,
    _class: JClass,
    page: jint,
    per_page: jint
) -> jstring {
    let rt = get_runtime();
    match rt.block_on(OrderService::get_all_orders(page as u64, per_page as u64)) {
        Ok((orders, total)) => {
            let result =
                serde_json::json!({
                "orders": orders,
                "total": total,
                "page": page,
                "per_page": per_page
            });
            let json = serde_json::to_string(&result).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Err(e) => {
            error!("❌ 获取订单列表失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 更新订单
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_updateOrder(
    mut env: JNIEnv,
    _class: JClass,
    id: jint,
    product_name: JString,
    quantity: jint,
    price: f64,
    status: JString,
    notes: JString
) -> jstring {
    let product_name_str = get_string_safe!(env, product_name);
    let status_str = get_string_safe!(env, status);
    let notes_str = get_string_safe!(env, notes);

    let dto = UpdateOrderDto {
        product_name: if product_name_str.is_empty() {
            None
        } else {
            Some(product_name_str)
        },
        quantity: if quantity < 0 {
            None
        } else {
            Some(quantity as i32)
        },
        price: if price < 0.0 {
            None
        } else {
            Some(price)
        },
        status: if status_str.is_empty() {
            None
        } else {
            Some(status_str)
        },
        notes: if notes_str.is_empty() {
            None
        } else {
            Some(notes_str)
        },
    };

    let rt = get_runtime();
    match rt.block_on(OrderService::update_order(id as i32, dto)) {
        Ok(Some(order)) => {
            let json = serde_json::to_string(&order).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Ok(None) => { jstring!(env, r#"{"error": "订单不存在"}"#).into_raw() }
        Err(e) => {
            error!("❌ 更新订单失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 删除订单
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_deleteOrder(
    _env: JNIEnv,
    _class: JClass,
    id: jint
) -> jboolean {
    let rt = get_runtime();
    match rt.block_on(OrderService::delete_order(id as i32)) {
        Ok(true) => {
            info!("✅ 订单删除成功 ID: {}", id);
            JNI_TRUE
        }
        Ok(false) => {
            info!("❌ 订单不存在 ID: {}", id);
            JNI_FALSE
        }
        Err(e) => {
            error!("❌ 删除订单失败: {}", e);
            JNI_FALSE
        }
    }
}

/// 根据状态获取订单
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getOrdersByStatus(
    mut env: JNIEnv,
    _class: JClass,
    status: JString,
    page: jint,
    per_page: jint
) -> jstring {
    let status_str = get_string_safe!(env, status);

    let rt = get_runtime();
    match
        rt.block_on(OrderService::get_orders_by_status(&status_str, page as u64, per_page as u64))
    {
        Ok((orders, total)) => {
            let result =
                serde_json::json!({
                "orders": orders,
                "total": total,
                "page": page,
                "per_page": per_page,
                "status": status_str
            });
            let json = serde_json::to_string(&result).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Err(e) => {
            error!("❌ 根据状态获取订单失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 更新订单状态
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_updateOrderStatus(
    mut env: JNIEnv,
    _class: JClass,
    id: jint,
    status: JString
) -> jstring {
    let status_str = get_string_safe!(env, status);

    let rt = get_runtime();
    match rt.block_on(OrderService::update_order_status(id as i32, &status_str)) {
        Ok(Some(order)) => {
            let json = serde_json::to_string(&order).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Ok(None) => { jstring!(env, r#"{"error": "订单不存在"}"#).into_raw() }
        Err(e) => {
            error!("❌ 更新订单状态失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 获取订单统计信息
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getOrderStats(
    env: JNIEnv,
    _class: JClass
) -> jstring {
    let rt = get_runtime();
    match rt.block_on(OrderService::get_order_stats()) {
        Ok(stats) => {
            let json = serde_json::to_string(&stats).unwrap_or_else(|_| "{}".to_string());
            jstring!(env, json).into_raw()
        }
        Err(e) => {
            error!("❌ 获取订单统计失败: {}", e);
            let error_json = format!(r#"{{"error": "{}"}}"#, e);
            jstring!(env, error_json).into_raw()
        }
    }
}

/// 获取订单总数
#[no_mangle]
pub extern "C" fn Java_com_example_android_1rust_MainActivity_getOrderCount(
    _env: JNIEnv,
    _class: JClass
) -> jint {
    let rt = get_runtime();
    match rt.block_on(OrderService::get_order_count()) {
        Ok(count) => count as jint,
        Err(e) => {
            error!("❌ 获取订单总数失败: {}", e);
            -1
        }
    }
}

// ===== WebRTC JNI 接口 =====

/// 创建WebRTC客户端
#[no_mangle]
pub extern "C" fn Java_com_example_aslib_p2p_RustApi_createWebRTCClient(
    mut env: JNIEnv,
    _class: JClass,
    room_id: JString,
    server_url: JString,
    debug: jboolean,
    callback: JObject
) -> jstring {
    let room_id_str = get_string_safe!(env, room_id);
    let server_url_str = get_string_safe!(env, server_url);
    let debug_bool = debug == jni::sys::JNI_TRUE;

    info!("🚀 创建WebRTC客户端 - 房间: {}, 服务器: {}", room_id_str, server_url_str);

    // 创建配置
    let config = WebRTCConfig {
        room_id: room_id_str.clone(),
        server_url: server_url_str,
        debug: debug_bool,
        stun_servers: vec![
            "stun:stun.l.google.com:19302".to_string(),
            "stun:stun1.l.google.com:19302".to_string()
        ],
    };

    // 创建客户端
    let mut client = AndroidWebRTCClient::new(config);

    // 设置回调
    if let Ok(jvm) = env.get_java_vm() {
        if let Ok(global_ref) = env.new_global_ref(callback) {
            let callback_impl = AndroidWebRTCCallbackImpl::new(Arc::new(jvm), global_ref);
            client.set_callback(Arc::new(callback_impl));
        }
    }

    // 生成客户端ID并存储
    let client_id = generate_client_id(&room_id_str);
    let client_arc = Arc::new(Mutex::new(client));

    {
        let clients = get_webrtc_clients();
        if let Ok(mut clients_map) = clients.lock() {
            clients_map.insert(client_id.clone(), client_arc);
        }
    }

    jstring!(env, client_id).into_raw()
}

/// 连接到服务器
#[no_mangle]
pub extern "C" fn Java_com_example_aslib_p2p_RustApi_connect(
    mut env: JNIEnv,
    _class: JClass,
    client_id: JString
) -> jboolean {
    let client_id_str = get_string_safe!(env, client_id);
    info!("🔗 连接到服务器 - 客户端ID: {}", client_id_str);

    let clients = get_webrtc_clients();
    if let Ok(clients_map) = clients.lock() {
        if let Some(client_arc) = clients_map.get(&client_id_str) {
            let client_clone = Arc::clone(client_arc);
            let rt = get_runtime();

            // 在后台任务中连接并启动完整的WebRTC会话
            let client_clone_for_task = Arc::clone(&client_clone);
            let connect_task = rt.spawn(async move {
                match AndroidWebRTCClient::connect_and_start(client_clone_for_task).await {
                    Ok(_) => {
                        info!("✅ WebRTC客户端连接成功并启动消息处理");
                    }
                    Err(e) => {
                        error!("❌ ERROR: WebRTC客户端连接失败: {}", e);
                    }
                }
            });

            // 将任务存储到连接任务管理器
            {
                let tasks = get_connection_tasks();
                if let Ok(mut tasks_map) = tasks.lock() {
                    tasks_map.insert(client_id_str.clone(), connect_task);
                }
            }

            // 立即返回成功，连接在后台进行
            return jni::sys::JNI_TRUE;
        } else {
            error!("❌ 未找到客户端ID: {}", client_id_str);
        }
    }

    jni::sys::JNI_FALSE
}

/// 关闭连接
#[no_mangle]
pub extern "C" fn Java_com_example_aslib_p2p_RustApi_close(
    mut env: JNIEnv,
    _class: JClass,
    client_id: JString
) -> jboolean {
    let client_id_str = get_string_safe!(env, client_id);
    info!("🔌 关闭连接 - 客户端ID: {}", client_id_str);

    let clients = get_webrtc_clients();
    if let Ok(mut clients_map) = clients.lock() {
        if let Some(client_arc) = clients_map.remove(&client_id_str) {
            let rt = get_runtime();
            rt.block_on(async {
                let mut client = client_arc.lock().await;
                client.disconnect().await;
            });
            info!("✅ WebRTC客户端断开连接成功");
            return jni::sys::JNI_TRUE;
        } else {
            error!("❌ 未找到客户端ID: {}", client_id_str);
        }
    }

    jni::sys::JNI_FALSE
}

/// 发送消息
#[no_mangle]
pub extern "C" fn Java_com_example_aslib_p2p_RustApi_sendMessage(
    mut env: JNIEnv,
    _class: JClass,
    client_id: JString,
    message: JString
) -> jboolean {
    let client_id_str = get_string_safe!(env, client_id);
    let message_str = get_string_safe!(env, message);

    info!("📤 发送消息 - 客户端ID: {}, 消息: {}", client_id_str, message_str);

    let clients = get_webrtc_clients();
    if let Ok(clients_map) = clients.lock() {
        if let Some(client_arc) = clients_map.get(&client_id_str) {
            let client_clone = Arc::clone(client_arc);
            let rt = get_runtime();

            match
                rt.block_on(async {
                    let mut client = client_clone.lock().await;
                    client.send_message(&message_str).await
                })
            {
                Ok(_) => {
                    info!("✅ 消息发送成功");
                    return jni::sys::JNI_TRUE;
                }
                Err(e) => {
                    error!("❌ 消息发送失败: {}", e);
                }
            }
        } else {
            error!("❌ 未找到客户端ID: {}", client_id_str);
        }
    }

    jni::sys::JNI_FALSE
}

/// 发送二进制数据
#[no_mangle]
pub extern "C" fn Java_com_example_aslib_p2p_RustApi_sendBinaryData(
    mut env: JNIEnv,
    _class: JClass,
    client_id: JString,
    data: JByteArray
) -> jboolean {
    let client_id_str = get_string_safe!(env, client_id);

    // 获取字节数组的长度
    let length = match env.get_array_length(&data) {
        Ok(len) => len,
        Err(e) => {
            error!("❌ 获取字节数组长度失败: {}", e);
            return jni::sys::JNI_FALSE;
        }
    };

    // 获取字节数组的内容
    let mut byte_buffer = vec![0i8; length as usize];
    if let Err(_) = env.get_byte_array_region(&data, 0, &mut byte_buffer) {
        error!("❌ 读取字节数组失败");
        return jni::sys::JNI_FALSE;
    }

    // 转换为 u8 数组
    let data_u8: Vec<u8> = byte_buffer
        .iter()
        .map(|&b| b as u8)
        .collect();

    info!("📤 发送二进制数据 - 客户端ID: {}, 数据长度: {} 字节", client_id_str, data_u8.len());

    let clients = get_webrtc_clients();
    if let Ok(clients_map) = clients.lock() {
        if let Some(client_arc) = clients_map.get(&client_id_str) {
            let client_clone = Arc::clone(client_arc);
            let rt = get_runtime();

            match
                rt.block_on(async {
                    let mut client = client_clone.lock().await;
                    client.send_binary_data(&data_u8).await
                })
            {
                Ok(_) => {
                    info!("✅ 二进制数据发送成功");
                    return jni::sys::JNI_TRUE;
                }
                Err(e) => {
                    error!("❌ 二进制数据发送失败: {}", e);
                }
            }
        } else {
            error!("❌ 未找到客户端ID: {}", client_id_str);
        }
    }

    jni::sys::JNI_FALSE
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_get_string() {
        let rt = get_runtime();
        let s = rt.block_on(init_database("test.db"));
        println!("init_database: {:?}", s);
        let dto = CreateUserDto {
            username: "test".to_string(),
            password: "test".to_string(),
            age: 18,
        };

        let user = rt.block_on(UserService::create_user(dto));
        println!("user: {:?}", user);
    }
}
