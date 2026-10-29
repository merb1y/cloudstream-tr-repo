import com.lagradost.cloudstream3.APIHolder;
import com.lagradost.cloudstream3.HomePageList;
import com.lagradost.cloudstream3.HomePageResponse;
import com.lagradost.cloudstream3.MainAPI;
import com.lagradost.cloudstream3.MainPageRequest;
import com.lagradost.cloudstream3.SearchResponse;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream4.desktop.plugins.DexConverter;
import com.lagradost.cloudstream4.desktop.plugins.ExtensionClassLoader;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;

/**
 * Bir .cs3 eklentisini gercekten calistirir: ana sayfa kataloğunu ceker,
 * ilk N icerigin loadLinks'ini cagirir ve donen linkleri HTTP ile dogrular.
 *
 * Kullanim:
 *   java -cp "<app jars>;tools\out" ApiProbe <dosya.cs3> [itemSayisi] [linkSayisi] [testEdilecekLinkSayisi]
 */
public class ApiProbe {

    static int itemLimit = 3;
    static int linkItemLimit = 2;
    static int linkTestLimit = 3;
    static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("kullanim: ApiProbe <dosya.cs3> [item] [linkItem] [linkTest]");
            System.exit(1);
        }
        File cs3 = new File(args[0]);
        if (args.length > 1) itemLimit = Integer.parseInt(args[1]);
        if (args.length > 2) linkItemLimit = Integer.parseInt(args[2]);
        if (args.length > 3) linkTestLimit = Integer.parseInt(args[3]);

        System.out.println("PROBE " + cs3.getName());
        File jar = DexConverter.INSTANCE.cs3ToJar(cs3);

        try (URLClassLoader cl = new ExtensionClassLoader(jar, ApiProbe.class.getClassLoader())) {
            List<MainAPI> onceki = new ArrayList<>(APIHolder.INSTANCE.getAllProviders());
            Object api = null;
            Class<?> apiClass = null;

            String pluginClass = readPluginClass(cs3);
            if (pluginClass != null) {
                try {
                    Class<?> pc = cl.loadClass(pluginClass);
                    Object plugin = pc.getDeclaredConstructor().newInstance();
                    try {
                        pc.getMethod("setFilename", String.class).invoke(plugin, cs3.getAbsolutePath());
                    } catch (Exception ignored) {
                    }
                    try {
                        Method lm = null;
                        try {
                            lm = pc.getDeclaredMethod("load", Class.forName("android.content.Context"));
                        } catch (NoSuchMethodException ignored) {
                        }
                        if (lm != null) {
                            Object ctx = Class.forName("android.content.DesktopContext").getField("INSTANCE").get(null);
                            lm.invoke(plugin, ctx);
                        } else {
                            pc.getMethod("load").invoke(plugin);
                        }
                        System.out.println("PLUGIN " + pluginClass + " yuklendi");
                    } catch (Exception e) {
                        System.out.println("UYARI plugin.load: " + rootMessage(e));
                    }
                } catch (Exception e) {
                    System.out.println("UYARI plugin sinifi: " + rootMessage(e));
                }
            }

            for (MainAPI m : new ArrayList<>(APIHolder.INSTANCE.getAllProviders())) {
                if (!onceki.contains(m)) api = m;
            }

            if (api == null) {
                apiClass = findMainApi(jar, cl);
                if (apiClass == null) {
                    System.out.println("HATA: MainAPI sinifi bulunamadi");
                    System.exit(3);
                }
                api = ornekOlustur(apiClass);
            }
            apiClass = api.getClass();
            System.out.println("SINIF " + apiClass.getName());

            try {
                Method init = apiClass.getMethod("init");
                init.invoke(api);
            } catch (Exception ignored) {
            }

            // ---- ana sayfa ----
            List<MainPageRequest> requests = new ArrayList<>();
            try {
                Object mp = apiClass.getMethod("getMainPage").invoke(api);
                if (mp instanceof List) {
                    for (Object o : (List<?>) mp) {
                        if (o instanceof MainPageRequest) {
                            requests.add((MainPageRequest) o);
                        } else if (o != null) {
                            // MainPageData -> MainPageRequest
                            String n = (String) o.getClass().getMethod("getName").invoke(o);
                            String d = (String) o.getClass().getMethod("getData").invoke(o);
                            Object h = o.getClass().getMethod("getHorizontalImages").invoke(o);
                            requests.add(new MainPageRequest(n, d, Boolean.TRUE.equals(h)));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("UYARI: mainPage okunamadi: " + rootMessage(e));
            }
            if (requests.isEmpty()) requests.add(new MainPageRequest("", "", false));

            List<String[]> candidates = new ArrayList<>(); // {name, url, type}

            for (MainPageRequest req : requests) {
                if (candidates.size() >= itemLimit) break;
                System.out.println("KATEGORI [" + req.getName() + "] data=" + req.getData());
                try {
                    Object res = callSuspend(api, apiClass, "getMainPage", 3,
                            new Class[]{int.class, MainPageRequest.class}, 1, req);
                    if (!(res instanceof HomePageResponse)) {
                        System.out.println("  sonuc yok (" + res + ")");
                        continue;
                    }
                    HomePageResponse home = (HomePageResponse) res;
                    for (HomePageList list : home.getItems()) {
                        System.out.println("  liste [" + list.getName() + "] " + list.getList().size() + " oge");
                        for (SearchResponse item : list.getList()) {
                            String type = String.valueOf(item.getType());
                            System.out.println("    - " + item.getName() + " | " + type + " | " + item.getUrl());
                            if (candidates.size() < itemLimit) {
                                candidates.add(new String[]{item.getName(), item.getUrl(), type});
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("  HATA getMainPage: " + rootMessage(e));
                }
            }

            System.out.println("OGE_SAYISI " + candidates.size());
            if (candidates.isEmpty()) {
                System.out.println("SONUC: 0 oge");
                System.exit(2);
            }

            // ---- linkler ----
            int totalLinks = 0;
            int okLinks = 0;
            int calisanOge = 0;
            int tested = 0;

            for (int i = 0; i < candidates.size() && i < linkItemLimit; i++) {
                String[] c = candidates.get(i);
                boolean live = c[2].equalsIgnoreCase("Live");
                System.out.println("LINK_DENEME " + c[0] + " | " + c[2] + " | " + c[1]);

                Object loadRes = null;
                try {
                    loadRes = callSuspend(api, apiClass, "load", 2, new Class[]{String.class}, c[1]);
                } catch (Exception e) {
                    System.out.println("  load HATA: " + rootMessage(e));
                }

                List<String> parcalar = new ArrayList<>();
                String akisUrl = null;
                if (loadRes != null) {
                    try {
                        Object eps = loadRes.getClass().getMethod("getEpisodes").invoke(loadRes);
                        if (eps instanceof List) {
                            for (Object e : (List<?>) eps) {
                                if (parcalar.size() >= 2) break;
                                Object d = e.getClass().getMethod("getData").invoke(e);
                                if (d instanceof String) parcalar.add((String) d);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        Object u = loadRes.getClass().getMethod("getUrl").invoke(loadRes);
                        if (u instanceof String) akisUrl = (String) u;
                    } catch (Exception ignored) {
                    }
                    System.out.println("  load: " + loadRes.getClass().getSimpleName()
                            + " bolum=" + parcalar.size() + " url=" + akisUrl);
                }

                if (parcalar.isEmpty()) parcalar.add(c[1]);

                List<String> subs = new ArrayList<>();
                List<ExtractorLink> links = new ArrayList<>();
                if (!live) {
                    for (String d : parcalar) {
                        try {
                            loadLinks(api, apiClass, d, subs, links);
                        } catch (Exception e) {
                            System.out.println("  HATA loadLinks: " + rootMessage(e));
                        }
                        if (!links.isEmpty()) break;
                    }
                } else {
                    try {
                        loadLinks(api, apiClass, c[1], subs, links);
                    } catch (Exception e) {
                        System.out.println("  HATA loadLinks: " + rootMessage(e));
                    }
                    for (ExtractorLink l : links) {
                        if (l.getUrl() != null && l.getUrl().startsWith("http")) {
                            akisUrl = l.getUrl();
                            break;
                        }
                    }
                }

                System.out.println("  altyazi: " + subs.size());
                for (ExtractorLink l : links) {
                    totalLinks++;
                    System.out.println("  link: " + l.getName() + " | " + l.getUrl() + " | ref=" + l.getReferer() + " | hdr=" + l.getAllHeaders());
                }

                int ogeOk = 0;
                if (live && links.isEmpty()) {
                    String hedef = akisUrl != null ? akisUrl : c[1];
                    String v = httpCheck(hedef, null);
                    System.out.println("  canliHTTP " + v + "  <= " + hedef);
                    if (v.contains("#EXTM3U") || v.startsWith("200") || v.startsWith("206")) ogeOk++;
                } else {
                    for (ExtractorLink l : links) {
                        if (tested >= linkTestLimit) break;
                        tested++;
                        String v = httpCheck(l.getUrl(), l.getAllHeaders());
                        System.out.println("  HTTP " + v + "  <= " + l.getUrl());
                        if (v.startsWith("200") || v.startsWith("206")) ogeOk++;
                    }
                }
                if (ogeOk > 0) {
                    okLinks += ogeOk;
                    calisanOge++;
                }
            }

            System.out.println("SONUC: " + candidates.size() + " oge, " + calisanOge + " calisan oge, "
                    + totalLinks + " link, " + okLinks + " calisan link");
            if (candidates.isEmpty() || (totalLinks == 0 && okLinks == 0)) System.exit(2);
        }
    }

    static Class<?> findMainApi(File jar, ClassLoader cl) throws Exception {
        Class<?> best = null;
        int bestScore = -1;
        try (JarFile jf = new JarFile(jar)) {
            Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                JarEntry e = en.nextElement();
                String n = e.getName();
                if (!n.endsWith(".class") || n.contains("$")) continue;
                String cn = n.substring(0, n.length() - 6).replace('/', '.');
                try {
                    Class<?> k = cl.loadClass(cn);
                    if (!MainAPI.class.isAssignableFrom(k) || Modifier.isAbstract(k.getModifiers())) continue;
                    int score = 0;
                    if (hasNoArgCtor(k)) score += 100;
                    else if (hasInstanceField(k)) score += 20;
                    else continue;
                    for (Method m : k.getDeclaredMethods()) {
                        if (m.getName().equals("loadLinks")) score += 10;
                        if (m.getName().equals("getMainPage")) score += 5;
                    }
                    if (score > bestScore) {
                        bestScore = score;
                        best = k;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        return best;
    }

    static Object ornekOlustur(Class<?> k) throws Exception {
        try {
            return k.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            java.lang.reflect.Field f = k.getField("INSTANCE");
            return f.get(null);
        }
    }

    static boolean hasNoArgCtor(Class<?> k) {
        try {
            k.getDeclaredConstructor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean hasInstanceField(Class<?> k) {
        try {
            k.getField("INSTANCE");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** .cs3 icindeki manifest.json'dan pluginClassName alanini okur. */
    static String readPluginClass(File cs3) {
        try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(cs3)) {
            java.util.zip.ZipEntry entry = z.getEntry("manifest.json");
            if (entry == null) return null;
            String text = new String(z.getInputStream(entry).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("\"pluginClassName\"\\s*:\\s*\"([^\"]+)\"").matcher(text);
            return m.find() ? m.group(1) : null;
        } catch (Exception e) {
            return null;
        }
    }

    static Object callSuspend(Object target, Class<?> apiClass, String name, int paramCount,
                              Class<?>[] leading, Object... leadingArgs) throws Exception {
        Method m = findMethod(apiClass, name, paramCount);
        if (m == null) throw new NoSuchMethodException(name + "/" + paramCount);
        Function2<CoroutineScope, Continuation<? super Object>, Object> block = new Function2<>() {
            @Override
            public Object invoke(CoroutineScope scope, Continuation<? super Object> cont) {
                Object[] full = Arrays.copyOf(leadingArgs, leadingArgs.length + 1);
                full[leadingArgs.length] = cont;
                try {
                    return m.invoke(target, full);
                } catch (Exception e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    throw new RuntimeException(c);
                }
            }
        };
        return BuildersKt.runBlocking(EmptyCoroutineContext.INSTANCE, block);
    }

    static void loadLinks(Object api, Class<?> apiClass, String url, List<String> subs, List<ExtractorLink> links)
            throws Exception {
        Function1<Object, Unit> subCb = s -> {
            subs.add(String.valueOf(s));
            return Unit.INSTANCE;
        };
        Function1<Object, Unit> linkCb = l -> {
            if (l instanceof ExtractorLink) links.add((ExtractorLink) l);
            else if (l != null) System.out.println("  [bilinmeyen link] " + l);
            return Unit.INSTANCE;
        };

        Method m5 = findMethod(apiClass, "loadLinks", 5);
        if (m5 != null) {
            callSuspend(api, apiClass, "loadLinks", 5, new Class[0], url, false, subCb, linkCb);
            return;
        }
        Method m4 = findMethod(apiClass, "loadLinks", 4);
        if (m4 != null) {
            CountDownLatch latch = new CountDownLatch(1);
            m4.invoke(api, url, false, subCb, linkCb);
            latch.await(1, TimeUnit.SECONDS);
            return;
        }
        throw new NoSuchMethodException("loadLinks");
    }

    static Method findMethod(Class<?> c, String name, int params) {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            for (Method m : k.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == params) return m;
            }
        }
        return null;
    }

    static String httpCheck(String url, java.util.Map<String, String> headers) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                    .connectTimeout(java.time.Duration.ofSeconds(12))
                    .version(java.net.http.HttpClient.Version.HTTP_1_1)
                    .build();
            java.net.http.HttpRequest.Builder rb = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(20))
                    .GET();
            rb.header("User-Agent", UA);
            rb.header("Range", "bytes=0-2047");
            if (headers != null) {
                for (java.util.Map.Entry<String, String> e : headers.entrySet()) {
                    if (e.getKey() == null || e.getValue() == null) continue;
                    try {
                        rb.setHeader(e.getKey(), e.getValue());
                    } catch (Exception ignored) {
                    }
                }
            }
            java.net.http.HttpResponse<InputStream> resp =
                    client.send(rb.build(), java.net.http.HttpResponse.BodyHandlers.ofInputStream());
            int code = resp.statusCode();
            String ct = resp.headers().firstValue("content-type").orElse("");
            InputStream is = resp.body();
            byte[] buf = new byte[1024];
            int n = 0;
            if (is != null) {
                n = is.read(buf);
                is.close();
            }
            String head = n > 0 ? new String(buf, 0, Math.min(n, 200), "UTF-8").replaceAll("[\\r\\n]+", " ") : "";
            boolean m3u = head.contains("#EXTM3U");
            return code + " ct=" + ct + (m3u ? " M3U8" : "") + " head=" + head;
        } catch (Exception e) {
            return "HATA " + rootMessage(e);
        }
    }

    static String rootMessage(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null && c.getCause() != c) c = c.getCause();
        return c.getClass().getSimpleName() + ": " + String.valueOf(c.getMessage()).replaceAll("[\\r\\n]+", " ");
    }
}
