import com.lagradost.cloudstream4.desktop.plugins.DexConverter;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** .cs3 icindeki siniflari ve manifest.json'u listeler. */
public class JarList {
    public static void main(String[] args) throws Exception {
        for (String a : args) {
            File cs3 = new File(a);
            System.out.println("== " + cs3.getName() + " ==");
            File jar = DexConverter.INSTANCE.cs3ToJar(cs3); System.out.println("  JAR " + jar.getAbsolutePath());
            try (ZipFile z = new ZipFile(jar)) {
                var en = z.entries();
                while (en.hasMoreElements()) {
                    ZipEntry e = en.nextElement();
                    if (e.getName().endsWith(".class")) {
                        System.out.println("  " + e.getName().replace('/', '.').replace(".class", ""));
                    }
                }
                ZipEntry m = z.getEntry("manifest.json");
                if (m != null) {
                    System.out.println("  MANIFEST " + new String(z.getInputStream(m).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).replaceAll("\\s+", " "));
                }
            }
        }
    }
}
