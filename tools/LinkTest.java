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
 * Bir eklentinin loadLinks fonksiyonunu gerçek siteye karşı çalıştırır.
 * Kullanım: java -cp "<app jars>;tools\out" LinkTest <dosya.cs3> <sinifAdi> <sayfaUrl>
 */
public class LinkTest {

    public static void main(String[] args) throws Exception {
        File cs3 = new File(args[0]);
        String pluginClassName = args[1];
        String pageUrl = args[2];

        File jar = DexConverter.INSTANCE.cs3ToJar(cs3);
        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()}, LinkTest.class.getClassLoader())) {
            Class<?> apiClass = cl.loadClass(pluginClassName);
            Object api = apiClass.getDeclaredConstructor().newInstance();
            System.out.println("sinif: " + apiClass.getName());
            System.out.println("sayfa: " + pageUrl);

            CountDownLatch latch = new CountDownLatch(1);
            final int[] linkCount = {0};

            Function1<Object, Unit> subtitleCb = s -> {
                System.out.println("  [altyazi] " + s);
                return Unit.INSTANCE;
            };

            Function1<Object, Unit> linkCb = l -> {
                linkCount[0]++;
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
                    System.out.println("  done: " + result);
                    latch.countDown();
                }
            };

            var method = apiClass.getMethod("loadLinks", String.class, boolean.class,
                    Function1.class, Function1.class, Continuation.class);

            Object result = method.invoke(api, pageUrl, false, subtitleCb, linkCb, cont);
            // invoke senkron bitmişse resumeWith zaten latch'i indirmiş olur.
            if (!latch.await(120, TimeUnit.SECONDS)) {
                System.out.println("  ZAMAN ASIMI");
            }
            System.out.println("  donus: " + result);

            System.out.println("SONUC: " + linkCount[0] + " link");
            if (linkCount[0] == 0) System.exit(2);
        }
    }
}
