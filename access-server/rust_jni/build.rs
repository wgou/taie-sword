use std::env;
use std::path::PathBuf;

fn main() {
    // 获取项目根目录和目标架构
    let target = env::var("TARGET").unwrap();
    println!("cargo:warning=Target: {}", target);
    // 只在Android目标上链接shadowhook
    if target.contains("android") {
        // Android系统库
        println!("cargo:rustc-link-lib=log");
        println!("cargo:rustc-link-lib=dl");
    } else {
        println!("cargo:warning=Not building for Android, skipping shadowhook linking");
    }
}
