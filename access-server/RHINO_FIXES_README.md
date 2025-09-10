# Rhino JavaScript引擎问题修复报告

## 问题描述

在Android环境中使用Rhino JavaScript引擎时遇到以下错误：

```
org.mozilla.javascript.EcmaError: TypeError: Cannot find function d in object class android.util.Log. (print#1)
```

## 问题原因分析

1. **直接暴露Log.class的问题**: 
   - 我们试图将`android.util.Log.class`直接暴露给JavaScript
   - Rhino无法正确处理Java静态方法的调用
   - JavaScript中调用`AndroidLog.d()`时找不到方法

2. **Rhino版本兼容性问题**:
   - Rhino 1.7.14在Android上有`javax.lang.model.SourceVersion`依赖问题
   - 已降级到Rhino 1.7.13以确保Android兼容性

## 解决方案

### 1. 创建LogWrapper类

```java
public static class LogWrapper {
    public void d(String tag, String message) {
        Log.d(tag, message);
    }
    
    public void i(String tag, String message) {
        Log.i(tag, message);
    }
    
    // ... 其他日志级别方法
}
```

**优势**:
- 提供实例方法而非静态方法
- Rhino可以正确调用实例方法
- JavaScript中可以直接使用`AndroidLog.d('tag', 'message')`

### 2. 改进错误处理

```java
} catch (org.mozilla.javascript.EcmaError e) {
    Log.e(TAG, "JavaScript运行时错误:");
    Log.e(TAG, "  错误类型: " + e.getName());
    Log.e(TAG, "  错误信息: " + e.getErrorMessage());
    Log.e(TAG, "  行号: " + e.getLineNumber());
    // ...
}
```

**改进**:
- 区分不同类型的Rhino异常
- 提供详细的错误信息和定位
- 便于调试JavaScript代码问题

### 3. 分步执行复杂脚本

将长JavaScript脚本分解为多个小脚本：

```java
// 原来的方式 - 容易出错且难以调试
rhinoHelper.executeScript(longComplexScript);

// 修复后的方式 - 分步执行
rhinoHelper.executeScript("print('=== 访问Java对象 ===');", "step1");
rhinoHelper.executeScript("print('姓名: ' + demoData.getName());", "step2");
// ...
```

**优势**:
- 更容易定位错误位置
- 可以针对特定步骤进行异常处理
- 提高代码可维护性

### 4. 添加测试辅助类

创建`JSTestHelper`类用于系统性测试：

```java
public static void testBasicJS(Context context) {
    // 测试基本JavaScript功能
    // 测试Java对象交互
    // 测试JavaBean属性访问
}
```

## 修复结果

### ✅ 已解决的问题

1. **print函数正常工作**: JavaScript中可以使用`print()`输出日志
2. **Java对象交互**: 可以在JavaScript中调用Java方法
3. **错误处理改进**: 提供详细的错误信息和调试信息
4. **版本兼容性**: 使用Rhino 1.7.13确保Android兼容性

### 🔧 当前功能状态

- ✅ JavaScript基本语法执行
- ✅ 变量和函数定义
- ✅ Java对象方法调用
- ✅ getter方法访问
- ✅ setter方法调用
- ⚠️ JavaBean风格属性访问（部分支持）
- ✅ 静态方法调用

### 🧪 测试方法

1. 点击"基础测试按钮"运行基本功能测试
2. 点击"Rhino Java交互演示"测试Java集成
3. 查看Logcat输出，过滤标签：
   - `JavaScript` - JavaScript执行输出
   - `RhinoHelper` - 引擎操作日志
   - `DemoData` - Java对象操作日志
   - `JSTestHelper` - 测试过程日志

## 使用建议

### 1. 推荐的JavaScript编写模式

```javascript
// 推荐：分步执行，便于调试
print('开始处理...');
var result = javaObject.processData();
print('处理结果: ' + result);

// 避免：过长的单行脚本
// var result = javaObject.method1().method2().method3();...
```

### 2. 错误处理最佳实践

```javascript
try {
    // JavaScript代码
    var result = riskyOperation();
    print('成功: ' + result);
} catch (e) {
    print('错误: ' + e.message);
}
```

### 3. Java对象访问模式

```javascript
// 推荐：使用方法调用
var name = javaObject.getName();
javaObject.setName('newName');

// 备选：JavaBean风格（可能不完全支持）
var name = javaObject.name;  // getter
javaObject.name = 'newName'; // setter（可能不工作）
```

## 总结

通过创建LogWrapper类、改进错误处理和分步执行脚本，我们成功解决了Rhino在Android环境中的主要问题。现在JavaScript引擎可以正常工作，支持与Java代码的双向交互。

建议在实际使用中：
1. 使用Rhino 1.7.13版本
2. 采用分步执行复杂脚本
3. 充分利用改进的错误处理机制
4. 遵循推荐的JavaScript编写模式
