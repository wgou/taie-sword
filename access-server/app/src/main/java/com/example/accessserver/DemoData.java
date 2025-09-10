package com.example.accessserver;

import android.util.Log;

/**
 * 演示用的数据类，用于展示JavaScript与Java的交互
 */
public class DemoData {
    private static final String TAG = "DemoData";
    
    private String name;
    private int age;
    private boolean isActive;
    
    public DemoData() {
        this.name = "默认用户";
        this.age = 25;
        this.isActive = true;
        Log.d(TAG, "DemoData对象创建完成");
    }
    
    public DemoData(String name, int age) {
        this.name = name;
        this.age = age;
        this.isActive = true;
        Log.d(TAG, "DemoData对象创建完成: " + name + ", " + age);
    }
    
    // Getter方法
    public String getName() {
        return name;
    }
    
    public int getAge() {
        return age;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    // Setter方法
    public void setName(String name) {
        this.name = name;
        Log.d(TAG, "名称已更新: " + name);
    }
    
    public void setAge(int age) {
        this.age = age;
        Log.d(TAG, "年龄已更新: " + age);
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
        Log.d(TAG, "状态已更新: " + active);
    }
    
    // 演示方法
    public String getInfo() {
        String info = "姓名: " + name + ", 年龄: " + age + ", 状态: " + (isActive ? "活跃" : "非活跃");
        Log.d(TAG, "获取信息: " + info);
        return info;
    }
    
    public void sayHello() {
        String message = "你好，我是 " + name + "，今年 " + age + " 岁！";
        Log.i(TAG, message);
    }
    
    public int calculateBirthYear() {
        int currentYear = 2024;
        int birthYear = currentYear - age;
        Log.d(TAG, "计算出生年份: " + birthYear);
        return birthYear;
    }
    
    public static void staticMethod() {
        Log.i(TAG, "这是一个静态方法被调用了！");
    }
    
    @Override
    public String toString() {
        return "DemoData{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", isActive=" + isActive +
                '}';
    }
}
