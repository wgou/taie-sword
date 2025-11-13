package io.renren;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class StringEncryptionHelper {
    private static final String AESTYPE = "AES";
    private static final Charset defaultCharset = Charset.forName("UTF-8");

    private static byte[] encode(byte[] data, String keyStr) throws BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(encodePass(keyStr).getBytes(), AESTYPE);
            Cipher cipher = Cipher.getInstance(AESTYPE);
            cipher.init(1, keySpec);
            byte[] encrypt = cipher.doFinal(data);
            return encrypt;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private static byte[] decode(byte[] data, String keyStr) throws BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(encodePass(keyStr).getBytes(), AESTYPE);
            Cipher cipher = Cipher.getInstance(AESTYPE);
            cipher.init(2, keySpec);
            byte[] decrypt = cipher.doFinal(data);
            return decrypt;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static String encodeStr(String str, String key) throws NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return bytesToHex(encode(str.getBytes(StandardCharsets.UTF_8), key));
    }

    /* renamed from: ˑˆʻˋˋˎʻʾﾞʼـᴵˎʼיـⁱᐧᵎ, reason: contains not printable characters */
    public static String m33(String str, String key) throws NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return new String(decode(hexToByteArray(str.replace("ScKit-", "")), key), defaultCharset);
    }

    public static String encodePass(String pass) throws NoSuchAlgorithmException {
        return encodeToMD516(pass).toLowerCase();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] hexToByteArray(String inHex) {
        byte[] result;
        int hexlen = inHex.length();
        if (hexlen % 2 == 1) {
            hexlen++;
            result = new byte[hexlen / 2];
            inHex = "0" + inHex;
        } else {
            result = new byte[hexlen / 2];
        }
        int j4 = 0;
        for (int i3 = 0; i3 < hexlen; i3 += 2) {
            result[j4] = (byte) Integer.parseInt(inHex.substring(i3, i3 + 2), 16);
            j4++;
        }
        return result;
    }

    public static String encodeToMD5(String string) throws NoSuchAlgorithmException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 255);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result = result + temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
            return "";
        }
    }

    public static String encodeToMD516(String encryptStr) throws NoSuchAlgorithmException {
        return encodeToMD5(encryptStr).substring(8, 24);
    }

    public static void main(String[] args) throws NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String[] b = {StringEncryptionHelper.m33("ScKit-fd99a1900dc9ceb0a794da362f260ef5", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-460f33f7568281187c5563a9fd18427d", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-5bc3b31d914a4a39b2d0a1b26b79a376", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-8ee443383354a375955d153d393f9edfc979e59934576c5aeb598f81f61c5d12", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-87a68a884336cd9e94801d4acc33d04e", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-dc4578c35992973c7ad366d7f6249f994aa31fb5d531421ad062e96c0101ac80", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-44503389506df807a092d2b3332b0ac83e4de426d30ec747875900b50cc17ddc", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-4b3b764a313748e6641b71190b6bacc646e7b4e405d79d2f4014a1a186f6a938", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-a02e3ef80cd0d842c00141c6de04d496aa1164e9744a54452b2a2497f9713233", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-1ffcf5ef61f10edebcc7df2e1a8d9ff4aa1164e9744a54452b2a2497f9713233", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-83c5e351f5c6a5a04f59aedf162860e5", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-2677958a6bcd77de27810399c7106d6aaa1164e9744a54452b2a2497f9713233", "ScKit-4f68459f64066fa6"), StringEncryptionHelper.m33("ScKit-65061a016ba59fcce852c4353d561df610b331c9daa942f8b25cd5b99149b542fb2e0bdc9448f89ccc377d6325a4a30c", "ScKit-18936117df58e3b5"), StringEncryptionHelper.m33("ScKit-a2d9384929f7a33b239e012ba0229a6f358da3be635a9ee8c754dbcc0c577ebc", "ScKit-18936117df58e3b5"), StringEncryptionHelper.m33("ScKit-437793708d03316194436acac94fff8335924eb8e9daa852993f84a448bf4338", "ScKit-18936117df58e3b5"), StringEncryptionHelper.m33("ScKit-aad91f31f3651bb48f3409a30c7b8ef94cb80ec5272710b57899a0426152d015", "ScKit-18936117df58e3b5"), StringEncryptionHelper.m33("ScKit-517c76b8845406b5dd76d4aa44767dc5", "ScKit-0011e1d4a9408e60"), StringEncryptionHelper.m33("ScKit-9eaf9dcfbedddc44cbd6712c4bb7bb1c", "ScKit-4f68459f64066fa6")};
        for (String s : b) {
            System.out.println(s);
        }

        System.out.println(StringEncryptionHelper.m33("ScKit-c3361a949ca217d30179002a51f67274f3b90f2820c6f40185585e11c2d1b9a1", "ScKit-8f4e6f63c505923c"));
        System.out.println(StringEncryptionHelper.m33("ScKit-caffd624c881a8cd6c1367f9ddbb74a8", "ScKit-8f4e6f63c505923c"));
        System.out.println(StringEncryptionHelper.m33("ScKit-104e10070cc99ec75efcdb7e9f3c00ac", "ScKit-8f4e6f63c505923c"));
        System.out.println(StringEncryptionHelper.m33("ScKit-29725a3f19e7be003d0ed391f47588855d0d693df673bc2b0df181585bb894ff", "ScKit-79da03b9e198eb74"));
        System.out.println(StringEncryptionHelper.m33("ScKit-29725a3f19e7be003d0ed391f47588854aaaf491ada9e8dbe083f62b51450cb10ba696beba5737486ba8e8a48dc1463e", "ScKit-79da03b9e198eb74"));
        System.out.println("--------");
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(StringEncryptionHelper.m33("ScKit-9d2acc4e7119f3cb8f1a402e2373af194041117b2cb8217b83517e792197c474", "ScKit-a77bb6594de7532e"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-9d2acc4e7119f3cb8f1a402e2373af194af9daad3076fa799be8504449067caa728d0545c493f5c74e2303ae11a6850e", "ScKit-a77bb6594de7532e"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-9d2acc4e7119f3cb8f1a402e2373af19a546440a2b82095ec75bc1c71d0bbe052e712bd6149ed00ecdda890c2288230f", "ScKit-a77bb6594de7532e"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47c668016155656a2d6e6a56b729df5a1b4507798e1d02e810a6331b5708dbff2ee", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47c0d858b8f294e0eee3b1f29ced77433d49470f70ef80ae54d3d961e3ed8d7f638", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47c2d03673a6320e8169f682946e7d69372", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47cfcf1ddf2deac07e270bcd86d3ecb3d94d41ce4fe0d12962cd0a30e6fb89d377b", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47cbcb862cfc08f0b4a78be70dc29d006fc0b711676f73aa92cc2219d8da4b09ead", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47c51a7a08c485b04074f6b06069df0d3c00b711676f73aa92cc2219d8da4b09ead", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47c561640dcdbb4614cb1a2245db0e768f30b711676f73aa92cc2219d8da4b09ead", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47c9ea943560ec0e713754d27df5d05e676", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47caef4e24b39d18610a41e2436ed8c2cdd", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47c931aaac4b1f99f7e1779a07ec91b3cc872eb7e3d1da95f79ddf000c31393aea3", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47caa36603956beb00140e1c0f502fc70c91600ea9299737513e8c020638e83065f", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47cfc15fcafcb8b565a7bcb68d38b978928", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-89683d3f786cf6134aeb37d75be9f47c2a039171c16c2350ff343de21702d9e3997fb524191947a439886abd50b728d02b6411c614b7b5fdbc4f953fc3c8adc3", "ScKit-b8a2388788caa490"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-b7f00e3fd09bc376c69eb6f3dbce8230e8dc42f2c28a98aa9ecab4267f9a49eba61960de4e5c72bc81a49b4b732c01dd", "ScKit-44e073a8356ec4ca"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-b7f00e3fd09bc376c69eb6f3dbce82308f6b11339b9a609f82fcca426aed58758a90f1318c0c2d4952231ec9b55ed2be", "ScKit-44e073a8356ec4ca"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-b7f00e3fd09bc376c69eb6f3dbce8230d82a79c9a8ea41fd9e96735f3ed37ec7", "ScKit-44e073a8356ec4ca"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-b7f00e3fd09bc376c69eb6f3dbce82307f53e09c2a807fe7251360005247dd0cccd559efad5e8ed192a15e5766440fcb", "ScKit-44e073a8356ec4ca"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-b7f00e3fd09bc376c69eb6f3dbce8230534d064e173b3326ad26b2a985ebc0d6", "ScKit-44e073a8356ec4ca"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-b7f00e3fd09bc376c69eb6f3dbce8230285abe9776f06d3e0db2803a625629f5", "ScKit-44e073a8356ec4ca"));
        arrayList.add(StringEncryptionHelper.m33("ScKit-b7f00e3fd09bc376c69eb6f3dbce8230c5c529733edb327497de0c382e4152171b6c47a4ec06dc27bc823484bde8dd0d", "ScKit-44e073a8356ec4ca"));
        for (String s : arrayList) {
            System.out.println(s);
        }

        System.out.println(StringEncryptionHelper.m33("ScKit-104e10070cc99ec75efcdb7e9f3c00ac", "ScKit-8f4e6f63c505923c"));
        System.out.println(StringEncryptionHelper.m33("ScKit-c3361a949ca217d30179002a51f67274f3b90f2820c6f40185585e11c2d1b9a1", "ScKit-8f4e6f63c505923c"));
        System.out.println(StringEncryptionHelper.m33("ScKit-3dd1596dc14ea545ecedc0712d3eba0b", "ScKit-18936117df58e3b5"));


    }
}