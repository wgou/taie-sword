package io.renren;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseTest {
    public static void parseId(String input){
        //org.telegram.messenger.web:hint/Message
        Pattern pattern = Pattern.compile("^([^:]+):([^/]+)/(.+)$");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String part1 = matcher.group(1); // org.telegram.messenger.web
            String part2 = matcher.group(2); // hint
            String part3 = matcher.group(3); // Message

            System.out.println("Part 1: " + part1);
            System.out.println("Part 2: " + part2);
            System.out.println("Part 3: " + part3);
        }

    }

    private static final Pattern ENGLISH_ONLY_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    public static boolean isOnlyEnglishLetters(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return ENGLISH_ONLY_PATTERN.matcher(str).matches();
    }

    public static boolean isAllBullets(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // 正则表达式：^•+$ 表示从开头到结尾全是 • 字符
        return text.matches("^•+$");
    }



    public static void main(String[] args) {
//        parseId("android:id/inpu");
//        System.out.println(isOnlyEnglishLetters("android:id/input"));
        System.out.println(isAllBullets("••••••"));
        System.out.println(isAllBullets("•"));
        System.out.println(isAllBullets("•2"));
        System.out.println(isAllBullets("•2"));


    }
}
