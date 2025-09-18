package io.renren;

import io.renren.common.utils.PasswordRecoveryUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PasswordRecoveryExample {
    
    public static void testPasswordRecovery() {
        // 测试数据
        List<String> inputStates = Arrays.asList(
                "1","•8","••8","•••7","••••6","•••••6","••••••4","•••2•••"
        );
        
        // 基础恢复
        String basicPassword = PasswordRecoveryUtils.recoverPassword(inputStates);
        System.out.println("基础恢复: " + basicPassword);
        
        // 智能恢复（推荐）
        PasswordRecoveryUtils.PasswordRecoveryResult result = 
            PasswordRecoveryUtils.recoverPasswordSmart(inputStates);
        
        System.out.println("智能恢复结果: " + result.toString());
        System.out.println("详细信息:\n" + PasswordRecoveryUtils.getRecoveryDetails(result));
        

    }
    
    private void handleRecoveredPassword(String fieldId, String password) {
        // 处理恢复的密码
        log.info("{}", String.format("字段[%s]的密码: %s", fieldId, password));
    }

    public static void main(String[] args) {
        testPasswordRecovery();
    }
}