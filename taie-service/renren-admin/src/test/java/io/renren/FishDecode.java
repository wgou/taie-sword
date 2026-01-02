package io.renren;

import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;

class Decode {
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

    public String a(byte[] bArr, byte[] bArr2) {
        return new String(b(bArr, bArr2), StandardCharsets.UTF_8);
    }
}

public class FishDecode {
    public static void main(String[] args) {
        Decode v90 = new Decode();
        JSONObject jSONObject = new JSONObject();
        jSONObject.put(v90.a(new byte[]{40, -79, 117}, new byte[]{88, -40, 17, -72, -95, 10, -20, -2}), "1111111111");
        jSONObject.put(v90.a(new byte[]{59, -41, 113, -73, 16}, new byte[]{82, -93, 8, -57, 117, 66, 68, 96}), v90.a(new byte[]{17, -120, -73, -1, -17, 45, 58, -102, 44, -112}, new byte[]{66, -28, -59, -96, -116, 65, 83, -1}));
        jSONObject.put(v90.a(new byte[]{33, -43, 82, 36}, new byte[]{82, -96, 48, 71, -95, -43, 50, 60}), v90.a(new byte[]{-65, 77, -108, -78, -84, 64, 72, Byte.MAX_VALUE, -93, 90}, new byte[]{-52, 46, -26, -41, -55, 46, 59, 23}));
        jSONObject.put(v90.a(new byte[]{71, 98, 55}, new byte[]{46, 15, 80, -126, 44, -98, -16, 55}), "222222222");
        jSONObject.put(v90.a(new byte[]{-68, -34, 96, -6}, new byte[]{-53, -77, 15, -104, 33, -122, -116, -54}), "this.h");
        jSONObject.put(v90.a(new byte[]{105, 54, 99, 87}, new byte[]{1, 91, 12, 53, -124, 89, -26, -92}), "this.i");
        System.out.println(v90.a(new byte[]{-96, 119}, new byte[]{-23, 51, 116, 20, -11, 51, 83, 82}));
        System.out.println(v90.a(new byte[]{65, -101, 27, 72, -67, -101, 82, 102}, new byte[]{5, -2, 109, 33, -34, -2, 59, 2}));
        System.out.println(jSONObject);
        System.out.println(v90.a(new byte[]{26, 38, -94, -50, 75, -10}, new byte[]{109, 79, -52, -86, 36, -127, 35, -117}));

    }
}
