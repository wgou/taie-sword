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

    public static void main(String[] args) {
        parseId("android:id/input");

    }
}
