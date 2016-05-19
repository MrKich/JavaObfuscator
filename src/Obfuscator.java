import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
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
    private static String MAP_ENTRY = "        OBFUSCATED_DATA.put(\"%s\", \"%s\");\n";
    private static String STUB =
            "import java.io.ByteArrayOutputStream;\n" +
            "import java.lang.reflect.InvocationTargetException;\n" +
            "import java.util.Base64;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.Map;\n" +
            "import java.util.zip.DataFormatException;\n" +
            "import java.util.zip.Inflater;\n" +
            "\n" +
            "/**\n" +
            " * Created by azarn on 5/18/16.\n" +
            " */\n" +
            "public class JavaObfuscateStub extends ClassLoader {\n" +
            "    private static byte XOR_CONSTANT = %d;\n" +
            "    private static Map<String, String> OBFUSCATED_DATA;\n" +
            "    static {\n" +
            "        OBFUSCATED_DATA = new HashMap<>();\n" +
            "%s" +
            "    }\n" +
            "\n" +
            "    public Class findClass(String name) {\n" +
            "        String class_data = OBFUSCATED_DATA.get(name);\n" +
            "        if (class_data == null) {\n" +
            "            return null;\n" +
            "        }\n" +
            "\n" +
            "        byte[] data = Base64.getDecoder().decode(class_data);\n" +
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
            "        new JavaObfuscateStub().findClass(\"%s\").getMethod(\"main\", String[].class).invoke(null, (Object)args);\n" +
            "    }\n" +
            "}\n";

    public static String obfuscate(Path file, byte xorConstant) throws IOException {
        byte[] data = Files.readAllBytes(file);

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
        for (int i = 0; i < data.length; ++i) {
            data[i] ^= xorConstant;
        }

        return Base64.getEncoder().encodeToString(data);
    }

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

        int c = new Random().nextInt(256) - 128;
        StringBuilder sb = new StringBuilder();
        try {
            Files.list(tempDir).filter(path -> path.getFileName().toString().endsWith(".class")).forEach(path -> {
                String obfs;
                try {
                    obfs = obfuscate(path, (byte) c);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                String classname = path.getFileName().toString();
                sb.append(String.format(MAP_ENTRY, classname.substring(0, classname.length() - 6), obfs));
            });
        } catch (IOException | UncheckedIOException e) {
            System.err.println("Cannot obfuscate file, error: " + e.getMessage());
            return;
        }

        try (OutputStream os = Files.newOutputStream(Paths.get(args[2]))) {
            os.write(String.format(STUB, c, sb.toString(), args[1]).getBytes());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
