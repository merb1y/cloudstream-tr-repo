import com.lagradost.cloudstream4.desktop.plugins.DexConverter;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function1;

/**
 * Bir .cs3 içindeki ExtractorApi sınıfını doğrudan çalıştırır (plugin kaydı olmadan).
 * Kullanım: java -cp "<app jars>;tools\out" ExtractTest <dosya.cs3> <sinifAdi> <videoUrl> [referer]
 */
public class ExtractTest {

    public static void main(String[] args) throws Exception {
        File cs3 = new File(args[0]);
        String className = args[1];
        String videoUrl = args[2];
        String referer = args.length > 3 ? args[3] : null;

        File jar = DexConverter.INSTANCE.cs3ToJar(cs3);
        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()}, ExtractTest.class.getClassLoader())) {
            Class<?> cls = cl.loadClass(className);
            Object api = cls.getDeclaredConstructor().newInstance();
            System.out.println("sinif: " + cls.getName());
            System.out.println("url  : " + videoUrl);

            CountDownLatch latch = new CountDownLatch(1);
            final int[] subs = {0};
            final int[] links = {0};

            Function1<Object, Unit> subCb = s -> {
                subs[0]++;
                System.out.println("  [altyazi] " + s);
                return Unit.INSTANCE;
            };
            Function1<Object, Unit> linkCb = l -> {
                links[0]++;
                System.out.println("  [link] " + l);
                return Unit.INSTANCE;
            };

            Continuation<Object> cont = new Continuation<>() {
                @Override
                public kotlin.coroutines.CoroutineContext getContext() {
                    return EmptyCoroutineContext.INSTANCE;
                }

                @Override
                public void resumeWith(Object result) {
                    if (result instanceof Throwable) {
                        System.out.println("  HATA: " + result);
                    }
                    latch.countDown();
                }
            };

            var method = cls.getMethod("getUrl", String.class, String.class,
                    Function1.class, Function1.class, Continuation.class);
            Object result = method.invoke(api, videoUrl, referer, subCb, linkCb, cont);
            if (!latch.await(120, TimeUnit.SECONDS)) {
                System.out.println("  ZAMAN ASIMI");
            }
            System.out.println("  donus: " + result);
            System.out.println("SONUC: " + links[0] + " link, " + subs[0] + " altyazi");
        }
    }
}
