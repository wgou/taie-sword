# Rhino JavaScript引擎集成演示

## 概述

这个项目演示了如何在Android应用中集成Mozilla Rhino JavaScript引擎，并展示JavaScript与Java代码的双向交互。

## 集成内容

### 1. 依赖配置

在 `app/build.gradle` 中添加了Rhino依赖：

```gradle
implementation 'org.mozilla:rhino:1.7.14'
```

### 2. 核心组件

#### RhinoHelper.java
- Rhino引擎的封装类
- 提供JavaScript执行、Java对象绑定、变量操作等功能
- 包含错误处理和资源管理

#### DemoData.java
- 演示用的Java数据类
- 展示JavaBean模式的getter/setter方法
- 包含静态方法和实例方法

#### MainActivity.java
- 集成了三个演示功能
- 管理Rhino引擎的生命周期

### 3. 演示功能

#### 基础演示 (btn_rhino_basic)
- JavaScript基本语法
- 变量定义和计算
- 函数定义和调用
- print函数输出到Android Log

#### Java交互演示 (btn_rhino_java)
- 在JavaScript中访问Java对象
- 调用Java方法
- JavaBean风格的属性访问
- 静态方法调用

#### 高级演示 (btn_rhino_advanced)
- 复杂的JavaScript逻辑
- 数组和对象操作
- 错误处理机制
- JavaScript调用Java计算方法

## 使用方法

1. 运行应用
2. 点击相应的演示按钮
3. 在Android Studio的Logcat中查看输出
4. 过滤标签：`JavaScript`, `MainActivity`, `DemoData`

## 核心特性展示

### 1. JavaScript调用Java方法
```javascript
// 创建Java对象
var demoData = new DemoData("张三", 30);

// 调用方法
demoData.sayHello();
var info = demoData.getInfo();

// JavaBean风格访问
demoData.name = "李四";
var name = demoData.name;
```

### 2. Java对象暴露给JavaScript
```java
// Java代码
DemoData demoData = new DemoData("张三", 30);
rhinoHelper.addJavaObject("demoData", demoData);

// JavaScript代码中可以直接使用
// demoData.getName(), demoData.setAge(25) 等
```

### 3. 双向数据传递
- Java向JavaScript传递对象和数据
- JavaScript执行结果返回给Java
- 支持基本类型和复杂对象

### 4. 错误处理
- JavaScript中的异常会被捕获并记录
- 提供详细的错误信息和堆栈跟踪

## 日志输出示例

运行演示后，你会在Logcat中看到类似输出：

```
D/JavaScript: Hello from JavaScript!
D/JavaScript: 计算结果: 30
D/JavaScript: 你好, Rhino用户!
D/DemoData: DemoData对象创建完成: 张三, 30
I/DemoData: 你好，我是 张三，今年 30 岁！
D/JavaScript: 姓名: 张三
```

## 技术要点

1. **Rhino上下文管理**: 正确的Context.enter()和Context.exit()调用
2. **优化级别设置**: 设置为-1以兼容Android环境
3. **类型转换**: JavaScript和Java对象之间的自动转换
4. **生命周期管理**: 在Activity销毁时释放Rhino资源
5. **线程安全**: Rhino上下文的线程安全使用

## 扩展建议

1. 可以添加文件系统访问演示
2. 可以集成网络请求功能
3. 可以添加数据库操作演示
4. 可以实现插件系统架构

## 注意事项

1. Rhino引擎会增加APK大小（约2-3MB）
2. JavaScript执行性能不如原生代码
3. 需要注意内存管理，避免内存泄漏
4. 在生产环境中建议添加更多错误处理
