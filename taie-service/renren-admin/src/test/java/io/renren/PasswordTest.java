package io.renren;

import org.apache.commons.codec.binary.Base32;

import java.security.SecureRandom;

public class PasswordTest {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        String secretKey = base32.encodeToString(bytes);
        System.out.println(secretKey.toLowerCase().replaceAll("(.{4})(?=.{4})", "$1 "));
    }
}
