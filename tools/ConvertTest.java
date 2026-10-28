import com.lagradost.cloudstream4.desktop.plugins.DexConverter;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipFile;

/**
 * .cs3 -> .jar dönüşümünü ve plugin sınıfının yüklenebildiğini doğrular.
 * Kullanım: java -cp "<app jars>;tools\out" ConvertTest <dosya.cs3> [dosya2.cs3 ...]
 */
public class ConvertTest {
    public static void main(String[] args) throws Exception {
        int ok = 0, fail = 0;
        for (String arg : args) {
            System.out.println("== " + new File(arg).getName() + " ==");
            try {
                File cs3 = new File(arg);
                String manifest = readManifest(cs3);
                File jar = DexConverter.INSTANCE.cs3ToJar(cs3);
                try (ZipFile z = new ZipFile(jar)) {
                    long classes = z.stream().filter(e -> e.getName().endsWith(".class")).count();
                    System.out.println("  jar: " + jar.length() + " bayt, " + classes + " sinif");
                }
                try (URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()}, ConvertTest.class.getClassLoader())) {
                    String className = manifest;
                    Class<?> c = cl.loadClass(className);
                    Object o = c.getDeclaredConstructor().newInstance();
                    System.out.println("  plugin: " + className + " yuklendi, ust sinif: " + c.getSuperclass().getSimpleName()
                            + ", ornek: " + o.getClass().getSimpleName());
                }
                ok++;
            } catch (Exception e) {
                fail++;
                System.out.println("  HATA: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        System.out.println();
        System.out.println("SONUC: " + ok + " basarili, " + fail + " hatali");
        if (fail > 0) System.exit(1);
    }

    /** .cs3 içindeki manifest.json dosyasından pluginClassName alanını okur. */
    private static String readManifest(File cs3) throws Exception {
        try (ZipFile z = new ZipFile(cs3)) {
            var entry = z.getEntry("manifest.json");
            if (entry == null) throw new IllegalStateException("manifest.json yok");
            String text = new String(z.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
            var m = java.util.regex.Pattern.compile("\"pluginClassName\"\\s*:\\s*\"([^\"]+)\"").matcher(text);
            if (!m.find()) throw new IllegalStateException("pluginClassName yok: " + text);
            return m.group(1);
        }
    }
}
