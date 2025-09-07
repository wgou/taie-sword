package io.renren;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractCurrencyAndNumber {
    public static void main(String[] args) {
        // 示例字符串可以是美元、欧元等不同的货币符号
        String input = "$ 4,426.63".replace(" ", "");

        extractCurrencyAndNumber(input);
    }

    public static Double extractNumber(String input) {
        // 使用正则表达式匹配数字部分
        Pattern pattern = Pattern.compile("([0-9,]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String str;
            if (matcher.groupCount() == 1) {
                str = matcher.group(0);
            } else {
                str = matcher.group(1);
            }
            String numberString = str.replace(",", ""); // 提取并去掉逗号
            return Double.parseDouble(numberString);

        } else {
            return null;
        }
    }

    public static void extractCurrencyAndNumber(String input) {
        Pattern pattern = Pattern.compile("([\\p{Sc}])([0-9,]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String symbol = matcher.group(1); // 提取货币符号部分
            String numberString = matcher.group(2).replace(",", ""); // 提取数字部分并去掉逗号

            double number = Double.parseDouble(numberString); // 转换为数字
            System.out.println(symbol);
            System.out.println(number);

        } else {
        }
    }

}
