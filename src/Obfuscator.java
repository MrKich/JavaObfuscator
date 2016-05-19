import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import static java.util.zip.Deflater.BEST_COMPRESSION;

/**
 * Created by azarn on 5/18/16.
 */
public class Obfuscator {
    private static String STUB =
            "import java.io.ByteArrayOutputStream;\n" +
            "import java.lang.reflect.InvocationTargetException;\n" +
            "import java.util.Base64;\n" +
            "import java.util.zip.DataFormatException;\n" +
            "import java.util.zip.Inflater;\n" +
            "\n" +
            "/**\n" +
            " * Created by azarn on 5/18/16.\n" +
            " */\n" +
            "public class JavaObfuscateStub extends ClassLoader {\n" +
            "    private static byte XOR_CONSTANT = %d;\n" +
            "    private static String OBFUSCATED_DATA = \"%s\";\n" +
            "\n" +
            "    public Class findClass(String name) {\n" +
            "        byte[] data = Base64.getDecoder().decode(OBFUSCATED_DATA);\n" +
            "        for (int i = 0; i < data.length; ++i) {\n" +
            "            data[i] ^= XOR_CONSTANT;\n" +
            "        }\n" +
            "\n" +
            "        Inflater inflater = new Inflater();\n" +
            "        inflater.setInput(data);\n" +
            "\n" +
            "        ByteArrayOutputStream res = new ByteArrayOutputStream();\n" +
            "        try {\n" +
            "            byte[] buf = new byte[1024];\n" +
            "            while (!inflater.finished()) {\n" +
            "                int count = inflater.inflate(buf);\n" +
            "                res.write(buf, 0, count);\n" +
            "            }\n" +
            "        } catch (DataFormatException e) {\n" +
            "            return null;\n" +
            "        }\n" +
            "\n" +
            "        inflater.end();\n" +
            "        data = res.toByteArray();\n" +
            "        return defineClass(name, data, 0, data.length);\n" +
            "    }\n" +
            "\n" +
            "    public static void main(String args[]) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {\n" +
            "        new JavaObfuscateStub().findClass(null).getMethod(\"main\", String[].class).invoke(null, (Object)args);\n" +
            "    }\n" +
            "}\n";

    public static void main(String args[]) {
        if (args.length != 3) {
            System.out.println("Usage: Obfuscator <in.java> <class name> <out.java>");
            return;
        }

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("obfuscator");
        } catch (IOException e) {
            System.err.println("Cannot create temp directory: " + e.getMessage());
            return;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, args[0], "-g:none", "-implicit:none", "-d", tempDir.toString());

        byte[] data;
        try {
            data = Files.readAllBytes(tempDir.resolve(args[1] + ".class"));
        } catch (IOException e) {
            System.err.println("Error while reading compiled data: " + e.getMessage());
            return;
        }

        ByteArrayOutputStream res = new ByteArrayOutputStream();
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        deflater.setLevel(BEST_COMPRESSION);

        byte[] buf = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buf);
            res.write(buf, 0, count);
        }

        data = res.toByteArray();
        int c = new Random().nextInt(256) - 128;
        for (int i = 0; i < data.length; ++i) {
            data[i] ^= (byte) c;
        }

        try (OutputStream os = Files.newOutputStream(Paths.get(args[2]))) {
            os.write(String.format(STUB, c, Base64.getEncoder().encodeToString(data)).getBytes());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
