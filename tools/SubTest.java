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
 * Bir .cs3 içindeki OrtakAltyazi nesnesini doğrudan çalıştırır.
 * Kullanım: java -cp "<app jars>;tools\out" SubTest <dosya.cs3> <imdbId|-> <baslik|-> <yil|-> <sezon|-> <bolum|->
 */
public class SubTest {

    public static void main(String[] args) throws Exception {
        File cs3 = new File(args[0]);
        String imdb = args[1].equals("-") ? null : args[1];
        String baslik = args[2].equals("-") ? null : args[2];
        Integer yil = args[3].equals("-") ? null : Integer.valueOf(args[3]);
        Integer sezon = args[4].equals("-") ? null : Integer.valueOf(args[4]);
        Integer bolum = args[5].equals("-") ? null : Integer.valueOf(args[5]);

        File jar = DexConverter.INSTANCE.cs3ToJar(cs3);
        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()}, SubTest.class.getClassLoader())) {
            Class<?> cls = cl.loadClass("com.keyiflerolsun.OrtakAltyazi");
            Object nesne = cls.getField("INSTANCE").get(null);

            CountDownLatch latch = new CountDownLatch(1);
            final int[] subs = {0};

            Function1<Object, Unit> subCb = s -> {
                subs[0]++;
                System.out.println("  [altyazi] " + s);
                return Unit.INSTANCE;
            };

            Continuation<Object> cont = new Continuation<>() {
                @Override
                public kotlin.coroutines.CoroutineContext getContext() {
                    return EmptyCoroutineContext.INSTANCE;
                }

                @Override
                public void resumeWith(Object result) {
                    System.out.println("  donus: " + result);
                    latch.countDown();
                }
            };

            var method = cls.getMethod("ekle", String.class, String.class, Integer.class,
                    Integer.class, Integer.class, Function1.class, Continuation.class);
            method.invoke(nesne, imdb, baslik, yil, sezon, bolum, subCb, cont);
            if (!latch.await(90, TimeUnit.SECONDS)) {
                System.out.println("  ZAMAN ASIMI");
            }
            System.out.println("SONUC: " + subs[0] + " altyazi");
        }
    }
}
