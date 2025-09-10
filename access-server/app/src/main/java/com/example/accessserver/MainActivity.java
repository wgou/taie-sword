package com.example.accessserver;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.aslib.AccessService1;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RhinoHelper rhinoHelper;


    private  void checkAccessibility() {
        if (!isAccessibilityServiceEnabled(this.getApplicationContext(), AccessService1.class)) {
            this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }
    // 检测应用是否拥有无障碍权限
    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> serviceClass) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + serviceClass.getCanonicalName();

        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String componentName = splitter.next();
                    if (componentName.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 检查无障碍权限
        checkAccessibility();
        
        // 设置简单的布局
        setContentView(R.layout.activity_main_simple);
        
        // 初始化Rhino引擎
        initRhino();
        
        // 初始化测试按钮
        initTestButtons();
    }
    
    private void initRhino() {
        try {
            rhinoHelper = new RhinoHelper(this);
            Log.d(TAG, "Rhino引擎初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "Rhino引擎初始化失败", e);
        }
    }
    
    private void initTestButtons() {
        // 基础测试按钮
        Button testButton = findViewById(R.id.btn_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "测试按钮被点击了！开始测试修复后的功能。");
                // 测试修复后的JavaScript功能
                JSTestHelper.testBasicJS(MainActivity.this);
            }
        });
        
        // Rhino基础演示按钮
        Button rhinoBasicButton = findViewById(R.id.btn_rhino_basic);
        rhinoBasicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rhinoBasicDemo();
            }
        });
        
        // Rhino Java交互演示按钮
        Button rhinoJavaButton = findViewById(R.id.btn_rhino_java);
        rhinoJavaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rhinoJavaInteractionDemo();
            }
        });
        
        // Rhino高级演示按钮
        Button rhinoAdvancedButton = findViewById(R.id.btn_rhino_advanced);
        rhinoAdvancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rhinoAdvancedDemo();
            }
        });
    }
    
    /**
     * Rhino基础演示
     */
    private void rhinoBasicDemo() {
        Log.d(TAG, "=== Rhino基础演示开始 ===");
        
        if (rhinoHelper == null) {
            Log.e(TAG, "Rhino引擎未初始化");
            return;
        }
        
        // 基础JavaScript执行
//        rhinoHelper.executeScript("print('Hello from JavaScript!');");
//
//        // 变量和计算
//        rhinoHelper.executeScript("var x = 10; var y = 20; var sum = x + y; print('计算结果: ' + sum);");
//
//        // 函数定义和调用
//        String functionScript =
//            "function greet(name) {" +
//            "    return '你好, ' + name + '!';" +
//            "}" +
//            "var message = greet('Rhino用户');" +
//            "print(message);";
//        rhinoHelper.executeScript(functionScript);

        rhinoHelper.executeScript("print(com.example.aslib.App.accessServiceRef.get())");
        
        Log.d(TAG, "=== Rhino基础演示结束 ===");
    }
    
    /**
     * Rhino与Java交互演示
     */
    private void rhinoJavaInteractionDemo() {
        Log.d(TAG, "=== Rhino Java交互演示开始 ===");
        
        if (rhinoHelper == null) {
            Log.e(TAG, "Rhino引擎未初始化");
            return;
        }
        
        // 创建Java对象并暴露给JavaScript
        DemoData demoData = new DemoData("张三", 30);
        rhinoHelper.addJavaObject("demoData", demoData);
        
        // 分步执行JavaScript，便于调试
        
        // 步骤1: 访问Java对象基本信息
        rhinoHelper.executeScript("print('=== 访问Java对象 ===');", "step1");
        rhinoHelper.executeScript("print('姓名: ' + demoData.getName());", "step2");
        rhinoHelper.executeScript("print('年龄: ' + demoData.getAge());", "step3");
        rhinoHelper.executeScript("print('状态: ' + demoData.isActive());", "step4");
        
        // 步骤2: 调用Java方法
        rhinoHelper.executeScript("print('=== 调用Java方法 ===');", "step5");
        rhinoHelper.executeScript("demoData.sayHello();", "step6");
        rhinoHelper.executeScript("var info = demoData.getInfo(); print('对象信息: ' + info);", "step7");
        
        // 步骤3: 修改Java对象
        rhinoHelper.executeScript("print('=== 修改Java对象 ===');", "step8");
        rhinoHelper.executeScript("demoData.setName('李四');", "step9");
        rhinoHelper.executeScript("demoData.setAge(25);", "step10");
        rhinoHelper.executeScript("demoData.sayHello();", "step11");
        
        // 步骤4: JavaBean风格访问（这个可能会有问题，单独测试）
        rhinoHelper.executeScript("print('=== JavaBean风格访问 ===');", "step12");
        try {
            rhinoHelper.executeScript("demoData.name = '王五';", "step13");
            rhinoHelper.executeScript("print('修改后的姓名: ' + demoData.name);", "step14");
        } catch (Exception e) {
            Log.w(TAG, "JavaBean风格的setter可能不支持，跳过");
        }
        
        try {
            rhinoHelper.executeScript("demoData.age = 35;", "step15");
            rhinoHelper.executeScript("print('修改后的年龄: ' + demoData.age);", "step16");
        } catch (Exception e) {
            Log.w(TAG, "JavaBean风格的setter可能不支持，跳过");
        }
        
        // 调用静态方法
        rhinoHelper.executeScript("var DemoDataClass = Packages.com.example.accessserver.DemoData; DemoDataClass.staticMethod();");
        
        Log.d(TAG, "=== Rhino Java交互演示结束 ===");
    }
    
    /**
     * Rhino高级演示
     */
    private void rhinoAdvancedDemo() {
        Log.d(TAG, "=== Rhino高级演示开始 ===");
        
        if (rhinoHelper == null) {
            Log.e(TAG, "Rhino引擎未初始化");
            return;
        }
        
        // 复杂的JavaScript逻辑
        String complexScript = 
            "print('=== 数组和对象操作 ===');" +
            "var users = [" +
            "    {name: '用户1', age: 20}," +
            "    {name: '用户2', age: 25}," +
            "    {name: '用户3', age: 30}" +
            "];" +
            "" +
            "for (var i = 0; i < users.length; i++) {" +
            "    print('用户 ' + (i+1) + ': ' + users[i].name + ', 年龄: ' + users[i].age);" +
            "}" +
            "" +
            "print('=== 函数式编程风格 ===');" +
            "function filterAdults(users) {" +
            "    var adults = [];" +
            "    for (var i = 0; i < users.length; i++) {" +
            "        if (users[i].age >= 25) {" +
            "            adults.push(users[i]);" +
            "        }" +
            "    }" +
            "    return adults;" +
            "}" +
            "" +
            "var adults = filterAdults(users);" +
            "print('成年用户数量: ' + adults.length);" +
            "" +
            "print('=== 错误处理 ===');" +
            "try {" +
            "    var result = 10 / 0;" +
            "    print('除法结果: ' + result);" +
            "    throw new Error('这是一个测试错误');" +
            "} catch (e) {" +
            "    print('捕获到错误: ' + e.message);" +
            "}";
            
        rhinoHelper.executeScript(complexScript);
        
        // 演示JavaScript调用Java方法并处理返回值
        DemoData advancedDemo = new DemoData("高级演示", 28);
        rhinoHelper.addJavaObject("advancedDemo", advancedDemo);
        
        String javaCallScript = 
            "print('=== JavaScript调用Java计算 ===');" +
            "var birthYear = advancedDemo.calculateBirthYear();" +
            "print('出生年份: ' + birthYear);" +
            "print('当前年份 - 出生年份 = ' + (2024 - birthYear));";
            
        rhinoHelper.executeScript(javaCallScript);
        
        Log.d(TAG, "=== Rhino高级演示结束 ===");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rhinoHelper != null) {
            rhinoHelper.destroy();
        }
    }

}