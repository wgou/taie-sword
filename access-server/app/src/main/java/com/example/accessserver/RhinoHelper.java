package com.example.accessserver;

import android.content.Context;
import android.util.Log;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Rhino JavaScript引擎帮助类
 */
public class RhinoHelper {
    private static final String TAG = "RhinoHelper";
    
    private org.mozilla.javascript.Context rhinoContext;
    private Scriptable scope;
    private android.content.Context androidContext;
    
    public RhinoHelper(android.content.Context androidContext) {
        this.androidContext = androidContext;
        initRhino();
    }
    
    /**
     * 初始化Rhino引擎
     */
    private void initRhino() {
        try {
            // 创建Rhino上下文
            rhinoContext = org.mozilla.javascript.Context.enter();
            rhinoContext.setOptimizationLevel(-1); // 禁用优化以兼容Android
            
            // 创建全局作用域
            scope = rhinoContext.initStandardObjects();
            
            // 暴露Android上下文给JavaScript
            Object wrappedContext = org.mozilla.javascript.Context.javaToJS(androidContext, scope);
            ScriptableObject.putProperty(scope, "androidContext", wrappedContext);
            
            // 暴露日志包装器给JavaScript
            LogWrapper logWrapper = new LogWrapper();
            Object wrappedLog = org.mozilla.javascript.Context.javaToJS(logWrapper, scope);
            ScriptableObject.putProperty(scope, "AndroidLog", wrappedLog);
            
            // 添加一些实用函数
            addUtilityFunctions();
            
            Log.d(TAG, "Rhino引擎初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Rhino引擎初始化失败", e);
        }
    }
    
    /**
     * 添加实用函数
     */
    private void addUtilityFunctions() {
        try {
            // 添加print函数用于日志输出
            String printFunction = 
                "function print(message) {" +
                "    AndroidLog.d('JavaScript', String(message));" +
                "}";
            rhinoContext.evaluateString(scope, printFunction, "print", 1, null);
            
            // 添加alert函数（输出到日志）
            String alertFunction = 
                "function alert(message) {" +
                "    AndroidLog.i('JavaScript-Alert', String(message));" +
                "}";
            rhinoContext.evaluateString(scope, alertFunction, "alert", 1, null);
            
        } catch (Exception e) {
            Log.e(TAG, "添加实用函数失败", e);
        }
    }
    
    /**
     * 执行JavaScript代码
     */
    public Object executeScript(String script) {
        return executeScript(script, "script");
    }
    
    /**
     * 执行JavaScript代码
     */
    public Object executeScript(String script, String sourceName) {
        try {
            if (rhinoContext == null) {
                Log.e(TAG, "Rhino上下文未初始化");
                return null;
            }
            
            // 只在调试模式下输出完整脚本
            if (script.length() > 100) {
                Log.d(TAG, "执行JavaScript: " + script.substring(0, 100) + "...");
            } else {
                Log.d(TAG, "执行JavaScript: " + script);
            }
            
            Object result = rhinoContext.evaluateString(scope, script, sourceName, 1, null);
            
            // 转换结果为Java对象
            if (result != null) {
                result = org.mozilla.javascript.Context.jsToJava(result, Object.class);
            }
            
            Log.d(TAG, "JavaScript执行成功，返回: " + (result != null ? result.toString() : "null"));
            return result;
        } catch (org.mozilla.javascript.EcmaError e) {
            Log.e(TAG, "JavaScript运行时错误:");
            Log.e(TAG, "  错误类型: " + e.getName());
            Log.e(TAG, "  错误信息: " + e.getErrorMessage());
            Log.e(TAG, "  行号: " + e.getLineNumber());
            Log.e(TAG, "  源文件: " + e.getSourceName());
            if (script.length() <= 200) {
                Log.e(TAG, "  脚本内容: " + script);
            }
            return null;
        } catch (org.mozilla.javascript.RhinoException e) {
            Log.e(TAG, "Rhino异常:");
            Log.e(TAG, "  错误信息: " + e.getMessage());
            Log.e(TAG, "  行号: " + e.lineNumber());
            Log.e(TAG, "  源文件: " + e.sourceName());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "执行JavaScript时发生未知错误", e);
            return null;
        }
    }
    
    /**
     * 调用JavaScript函数
     */
    public Object callFunction(String functionName, Object... args) {
        try {
            Object functionObj = scope.get(functionName, scope);
            if (functionObj instanceof Function) {
                Function function = (Function) functionObj;
                return function.call(rhinoContext, scope, scope, args);
            } else {
                Log.e(TAG, "函数不存在: " + functionName);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "调用JavaScript函数失败: " + functionName, e);
            return null;
        }
    }
    
    /**
     * 添加Java对象到JavaScript作用域
     */
    public void addJavaObject(String name, Object javaObject) {
        try {
            Object wrappedObject = org.mozilla.javascript.Context.javaToJS(javaObject, scope);
            ScriptableObject.putProperty(scope, name, wrappedObject);
            Log.d(TAG, "Java对象已添加到JavaScript作用域: " + name);
        } catch (Exception e) {
            Log.e(TAG, "添加Java对象失败: " + name, e);
        }
    }
    
    /**
     * 获取JavaScript变量值
     */
    public Object getVariable(String varName) {
        try {
            Object value = scope.get(varName, scope);
            return org.mozilla.javascript.Context.jsToJava(value, Object.class);
        } catch (Exception e) {
            Log.e(TAG, "获取JavaScript变量失败: " + varName, e);
            return null;
        }
    }
    
    /**
     * 设置JavaScript变量值
     */
    public void setVariable(String varName, Object value) {
        try {
            Object jsValue = org.mozilla.javascript.Context.javaToJS(value, scope);
            ScriptableObject.putProperty(scope, varName, jsValue);
        } catch (Exception e) {
            Log.e(TAG, "设置JavaScript变量失败: " + varName, e);
        }
    }
    
    /**
     * 释放资源
     */
    public void destroy() {
        try {
            if (rhinoContext != null) {
                org.mozilla.javascript.Context.exit();
                rhinoContext = null;
                scope = null;
                Log.d(TAG, "Rhino引擎资源已释放");
            }
        } catch (Exception e) {
            Log.e(TAG, "释放Rhino引擎资源失败", e);
        }
    }
    
    /**
     * 日志包装器，用于在JavaScript中调用Android Log
     */
    public static class LogWrapper {
        public void d(String tag, String message) {
            Log.d(tag, message);
        }
        
        public void i(String tag, String message) {
            Log.i(tag, message);
        }
        
        public void w(String tag, String message) {
            Log.w(tag, message);
        }
        
        public void e(String tag, String message) {
            Log.e(tag, message);
        }
        
        public void v(String tag, String message) {
            Log.v(tag, message);
        }
    }
}
