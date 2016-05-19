import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by azarn on 5/18/16.
 */
public class JavaObfuscateStub extends ClassLoader {
    private static byte XOR_CONSTANT = -87;
    private static Map<String, String> OBFUSCATED_DATA;
    static {
        OBFUSCATED_DATA = new HashMap<>();
        OBFUSCATED_DATA.put("TestClass", "0XPc+8Tmevm910f/QGLjKCQkgIO8+RKLp7j2OcArIS/NpWoPYOtl+/EavR7f8CKgVm/G12/BPhEttkkmmzdyu6Gbll010jfaZkDaN9rWVlDco8mshrwNmIrqKodKp8/sZYC5mYrIN1ISdXX62bYvLSuh+uijonWVuRgB+XDqxyO7P+2Vy3nDH7bFEAd0csHyF2YJ962fLOvp0C/dUHlTy7xyP3KD/yo3Sh9/se/jJ0KtBrEbb3zdSIq8xtHkMoh4W7E77y3dhW7MYbDSaoLvAijcWdByQ38FVB8Ey6N6gxthIPH/WLGCpfpWfxSX23PkEu61HiZcsXuQwHRY3LzmWf1smpW+GANLrFulS3f/DBsQUiCIZs4mjzQWBatWRjMjuw+wTVritWQCEWj4BBt60RpQ31TqEH/xdtRumdfows5WeZehh+h8ypYZko0r3u0gxZUSSrx2unIpkXL/LkLA3BIfcs3xtQ/OUJYq+gi90ZhtmcC9LwSDmk0mjew+7sTzIc89Zk4lv4iQ9STvYgqeD3giqsEm1brd43vu+7nbNOeQa7pNvBqmz4dV7UutIUc6+87e4/1qYYbU7tupCHduHF3hoXhVKK0QufuhMNvALFWpAJNc++olbzTNdW0EGucCXRXQDpBa/VEjZanzdOWHXpi/y1Wyb9E1NyFqOiFtxBuruxC9NI6Q5FuAs4CqVayheRWD");
        OBFUSCATED_DATA.put("TestClass$TestInnerClass", "0XPE+eTma+m9Na5hHxnLqLhV72ytQ6imd533kTA5watIwwPHCQ0dLwePV4UkwUtotkkmmxcrK/YSLnQwNNTGTxQUFhWrkWkHoCj1o69br2WhhTOByYAsi+OrgqtCrfoC+TZRF7O722QX2kJ9lG5Gf/LTQVN06uv7XnUZG+bhnnKFxNHnse9dOEISUzE5ANdKwrTrC7j1gCtI7+xmEqiI9wTcjXs5u1ph4zHhoMXhxQuFGYwYJLQoowidhP3A1hCz6kdbMjmaCbHzmk4OvtTcANb5BBL5AqiyoMdRCJkgQK9cmmcBkADbr7WAVqbkuX64W9VX/lSSNoEZkBwkm6ZwSUhu0drjaRsLdcqvtZeqa6t2f2+j1x9e74lSsJiSvraI2CZgGzlU/b/w7tUPtpnOlotYLU3t7fWtdSu0y7ACaa4pqcM9");
    }

    public Class findClass(String name) {
        String class_data = OBFUSCATED_DATA.get(name);
        if (class_data == null) {
            return null;
        }

        byte[] data = Base64.getDecoder().decode(class_data);
        for (int i = 0; i < data.length; ++i) {
            data[i] ^= XOR_CONSTANT;
        }

        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream res = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buf);
                res.write(buf, 0, count);
            }
        } catch (DataFormatException e) {
            return null;
        }

        inflater.end();
        data = res.toByteArray();
        return defineClass(name, data, 0, data.length);
    }

    public static void main(String args[]) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        new JavaObfuscateStub().findClass("TestClass").getMethod("main", String[].class).invoke(null, (Object)args);
    }
}
