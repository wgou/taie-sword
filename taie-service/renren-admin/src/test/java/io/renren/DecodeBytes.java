package io.renren;

import java.nio.charset.StandardCharsets;

public class DecodeBytes {

    private static byte[] b(byte[] bArr, byte[] bArr2) {
        int length = bArr.length;
        int length2 = bArr2.length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            if (i2 >= length2) {
                i2 = 0;
            }
            bArr[i] = (byte) (bArr[i] ^ bArr2[i2]);
            i++;
            i2++;
        }
        return bArr;
    }

    public static String decode(byte[] bArr, byte[] bArr2) {
        return new String(b(bArr, bArr2), StandardCharsets.UTF_8);
    }


    public static void main(String[] args) {
        System.out.println(decode(new byte[]{17, 21}, new byte[]{89, 87, -91, -75, 69, 114, 12, 86}));
        System.out.println(decode(new byte[]{25}, new byte[]{101, 74, -42, 11, 82, -108, -2, 110}));
        System.out.println(decode(new byte[]{61, -109}, new byte[]{97, -17, -69, 52, -55, -74, 47, 0}));
        System.out.println(decode(new byte[]{-51, 75, 16, 82, 16, 114, -6}, new byte[]{-111, 16, 44, 33, 46, 46, -89, -124}));
        System.out.println(decode(new byte[]{-71, -101}, new byte[]{-15, -39, -25, -31, 34, -17, -19, -29}));
        System.out.println(decode(new byte[]{70, 99, 120, -29, -54}, new byte[]{35, 14, 8, -105, -77, 56, -38, 79}));
        System.out.println(decode(new byte[]{57, -8, 0, -78, 65}, new byte[]{92, -107, 112, -58, 56, -72, 35, 37}));
        System.out.println(decode(new byte[]{97, -101, -61, 61}, new byte[]{21, -30, -77, 88, -100, -60, 65, -103}));
        System.out.println(decode(new byte[]{-69, 75, 91, 68, -46}, new byte[]{-47, 46, 56, 48, -95, 73, 13, -115}));
        System.out.println(decode(new byte[]{-14, -96, 76}, new byte[]{-111, -43, 54, 34, -100, 18, -6, 89}));
        System.out.println(decode(new byte[]{84}, new byte[]{50, 2, 107, 99, 96, 57, -30, -46}));
        System.out.println(decode(new byte[]{84, -27, 118}, new byte[]{58, Byte.MIN_VALUE, 18, 6, 105, 0, -87, -83}));
        System.out.println(decode(new byte[]{-42}, new byte[]{-20, -8, 8, 82, -16, -52, -113, -34}));
    }
}
