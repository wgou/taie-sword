package com.example.accessserver;

import android.content.Context;
import android.util.Log;

/**
 * JavaScript测试辅助类
 */
public class JSTestHelper {
    private static final String TAG = "JSTestHelper";
    
    /**
     * 测试基本的JavaScript功能
     */
    public static void testBasicJS(Context context) {
        Log.d(TAG, "=== 开始测试基本JavaScript功能 ===");
        
        RhinoHelper rhinoHelper = new RhinoHelper(context);
        
        try {
            // 测试1: 简单的print函数
            Log.d(TAG, "测试1: print函数");
            rhinoHelper.executeScript("print('Hello from JavaScript!');", "test1");
            
            // 测试2: 变量和计算
            Log.d(TAG, "测试2: 变量和计算");
            rhinoHelper.executeScript("var x = 10; var y = 20; print('计算结果: ' + (x + y));", "test2");
            
            // 测试3: 函数定义
            Log.d(TAG, "测试3: 函数定义");
            rhinoHelper.executeScript(
                "function testFunc(name) { return 'Hello, ' + name; } " +
                "var result = testFunc('Rhino'); " +
                "print('函数调用结果: ' + result);", "test3");
            
            // 测试4: 测试Java对象交互
            Log.d(TAG, "测试4: Java对象交互");
            DemoData testData = new DemoData("测试用户", 25);
            rhinoHelper.addJavaObject("testData", testData);
            
            rhinoHelper.executeScript("print('Java对象姓名: ' + testData.getName());", "test4");
            rhinoHelper.executeScript("testData.setName('修改后的姓名'); print('修改后: ' + testData.getName());", "test4b");
            
            Log.d(TAG, "=== 基本JavaScript功能测试完成 ===");
            
        } catch (Exception e) {
            Log.e(TAG, "测试过程中发生错误", e);
        } finally {
            rhinoHelper.destroy();
        }
    }
    
    /**
     * 测试JavaBean风格的属性访问
     */
    public static void testJavaBeanAccess(Context context) {
        Log.d(TAG, "=== 开始测试JavaBean属性访问 ===");
        
        RhinoHelper rhinoHelper = new RhinoHelper(context);
        
        try {
            DemoData testData = new DemoData("Bean测试", 30);
            rhinoHelper.addJavaObject("beanData", testData);
            
            // 测试getter访问
            rhinoHelper.executeScript("print('原始姓名: ' + beanData.name);", "bean_test1");
            rhinoHelper.executeScript("print('原始年龄: ' + beanData.age);", "bean_test2");
            
            // 测试setter访问
            rhinoHelper.executeScript("beanData.name = 'JavaBean修改'; print('修改后姓名: ' + beanData.name);", "bean_test3");
            rhinoHelper.executeScript("beanData.age = 35; print('修改后年龄: ' + beanData.age);", "bean_test4");
            
            Log.d(TAG, "=== JavaBean属性访问测试完成 ===");
            
        } catch (Exception e) {
            Log.e(TAG, "JavaBean测试过程中发生错误", e);
        } finally {
            rhinoHelper.destroy();
        }
    }
}
