import com.lagradost.cloudstream4.desktop.plugins.DexConverter;
import com.lagradost.cloudstream3.plugins.Plugin;
import com.lagradost.cloudstream3.MainAPI;
import com.lagradost.cloudstream3.MainPageRequest;
import com.lagradost.cloudstream3.MainPageData;
import com.lagradost.cloudstream3.HomePageResponse;
import com.lagradost.cloudstream3.LoadResponse;
import com.lagradost.cloudstream3.SearchResponse;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.APIHolder;
import android.content.DesktopContext;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function1;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

public class ProviderInspector {

    static class SimpleContinuation<T> implements Continuation<T> {
        private final CompletableFuture<T> future = new CompletableFuture<>();

        @Override
        public CoroutineContext getContext() {
            return EmptyCoroutineContext.INSTANCE;
        }

        @Override
        public void resumeWith(Object result) {
            if (result instanceof kotlin.Result.Failure) {
                future.completeExceptionally(((kotlin.Result.Failure) result).exception);
            } else {
                future.complete((T) result);
            }
        }

        public T get(long timeoutSec) throws Exception {
            return future.get(timeoutSec, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: ProviderInspector <plugin.cs3 or .jar>");
            return;
        }

        File file = new File(args[0]);
        File jar;
        if (file.getName().endsWith(".cs3")) {
            jar = DexConverter.INSTANCE.cs3ToJar(file);
        } else {
            jar = file;
        }

        System.out.println("=== Testing: " + file.getName() + " ===");
        String pluginClassName = null;
        try (ZipFile z = new ZipFile(file.getName().endsWith(".cs3") ? file : jar)) {
            var mEntry = z.getEntry("manifest.json");
            if (mEntry != null) {
                String manifestJson = new String(z.getInputStream(mEntry).readAllBytes(), StandardCharsets.UTF_8);
                var m = java.util.regex.Pattern.compile("\"pluginClassName\"\\s*:\\s*\"([^\"]+)\"").matcher(manifestJson);
                if (m.find()) {
                    pluginClassName = m.group(1);
                }
            }
        }

        URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()}, ProviderInspector.class.getClassLoader());
        int beforeCount = APIHolder.INSTANCE.getAllProviders().size();

        if (pluginClassName != null) {
            System.out.println("Loading plugin class: " + pluginClassName);
            try {
                Class<?> pClass = cl.loadClass(pluginClassName);
                Object plugin = pClass.getDeclaredConstructor().newInstance();
                if (plugin instanceof Plugin) {
                    ((Plugin) plugin).load(DesktopContext.INSTANCE);
                    System.out.println("Plugin.load(DesktopContext) SUCCESS!");
                }
            } catch (Throwable t) {
                System.out.println("Plugin.load FAILED: " + t);
                t.printStackTrace(System.out);
            }
        }

        List<MainAPI> allProviders = APIHolder.INSTANCE.getAllProviders();
        List<MainAPI> newProviders = new ArrayList<>();
        for (int i = beforeCount; i < allProviders.size(); i++) {
            newProviders.add(allProviders.get(i));
        }

        System.out.println("Registered providers from plugin: " + newProviders.size());
        for (MainAPI api : newProviders) {
            System.out.println("\n--- Provider: " + api.getName() + " (" + api.getClass().getName() + ") ---");
            System.out.println("Url: " + api.getMainUrl() + " | Types: " + api.getSupportedTypes());

            // Test getMainPage
            try {
                var mainPages = api.getMainPage();
                System.out.println("mainPage count: " + (mainPages != null ? mainPages.size() : 0));
                if (mainPages != null && !mainPages.isEmpty()) {
                    MainPageData first = mainPages.get(0);
                    System.out.println("Testing getMainPage(1, " + first.getName() + ", data=" + first.getData() + ")...");
                    SimpleContinuation cont = new SimpleContinuation();
                    MainPageRequest req = new MainPageRequest(first.getName(), first.getData(), false);
                    Object res = api.getMainPage(1, req, cont);
                    HomePageResponse pageResp = (res instanceof HomePageResponse) ? (HomePageResponse) res : (HomePageResponse) cont.get(20);

                    if (pageResp == null || pageResp.getItems() == null || pageResp.getItems().isEmpty()) {
                        System.out.println("getMainPage returned EMPTY or NULL!");
                    } else {
                        System.out.println("getMainPage SUCCESS: " + pageResp.getItems().size() + " sections/items");
                        SearchResponse firstItem = null;
                        for (var item : pageResp.getItems()) {
                            if (item.getList() != null && !item.getList().isEmpty()) {
                                firstItem = item.getList().get(0);
                                System.out.println("  Section: " + item.getName() + " (" + item.getList().size() + " items). First: " + firstItem.getName() + " -> " + firstItem.getUrl());
                                break;
                            }
                        }

                        if (firstItem != null) {
                            testLoadAndLinks(api, firstItem.getUrl());
                        }
                    }
                } else {
                    System.out.println("No mainPage definitions, testing search('a')...");
                    testSearch(api, "a");
                }
            } catch (Throwable t) {
                System.out.println("getMainPage FAILED: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                t.printStackTrace(System.out);
            }
        }
        System.out.println("\n=== Inspection finished ===");
        System.exit(0);
    }

    private static void testSearch(MainAPI api, String query) {
        try {
            SimpleContinuation cont = new SimpleContinuation();
            Object res = api.search(query, cont);
            List<SearchResponse> results = (res instanceof List) ? (List<SearchResponse>) res : (List<SearchResponse>) cont.get(20);
            System.out.println("search('" + query + "') returned " + (results != null ? results.size() : 0) + " items");
            if (results != null && !results.isEmpty()) {
                var item = results.get(0);
                System.out.println("  First search item: " + item.getName() + " -> " + item.getUrl());
                testLoadAndLinks(api, item.getUrl());
            }
        } catch (Throwable t) {
            System.out.println("search FAILED: " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static void testLoadAndLinks(MainAPI api, String url) {
        try {
            System.out.println("Testing load(" + url + ")...");
            SimpleContinuation cont = new SimpleContinuation();
            Object res = api.load(url, cont);
            LoadResponse loadResp = (res instanceof LoadResponse) ? (LoadResponse) res : (LoadResponse) cont.get(20);
            if (loadResp == null) {
                System.out.println("load returned NULL");
                return;
            }
            System.out.println("load SUCCESS: type=" + loadResp.getType() + ", name=" + loadResp.getName());

            String dataUrl = loadResp.getUrl();
            if (loadResp instanceof com.lagradost.cloudstream3.TvSeriesLoadResponse) {
                var eps = ((com.lagradost.cloudstream3.TvSeriesLoadResponse) loadResp).getEpisodes();
                if (eps != null && !eps.isEmpty()) {
                    var ep = eps.get(0);
                    System.out.println("  Testing episode: S" + ep.getSeason() + "E" + ep.getEpisode() + " - " + ep.getName() + " -> " + ep.getData());
                    dataUrl = ep.getData();
                }
            }

            // Test loadLinks
            System.out.println("Testing loadLinks(data=" + dataUrl + ")...");
            List<ExtractorLink> links = new ArrayList<>();
            Function1<ExtractorLink, Unit> linkCallback = link -> {
                links.add(link);
                System.out.println("  -> EXTRACTED LINK: [" + link.getName() + "] url=" + link.getUrl() + " referer=" + link.getReferer());
                return Unit.INSTANCE;
            };
            Function1<SubtitleFile, Unit> subCallback = sub -> Unit.INSTANCE;

            SimpleContinuation linksCont = new SimpleContinuation();
            Object lRes = api.loadLinks(dataUrl, false, subCallback, linkCallback, linksCont);
            Boolean ok = (lRes instanceof Boolean) ? (Boolean) lRes : (Boolean) linksCont.get(25);
            System.out.println("loadLinks returned: " + ok + ", total links extracted: " + links.size());

            for (ExtractorLink link : links) {
                checkLinkHttp(link);
            }
        } catch (Throwable t) {
            System.out.println("load/loadLinks FAILED: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    private static void checkLinkHttp(ExtractorLink link) {
        String streamUrl = link.getUrl();
        try {
            URL u = new URL(streamUrl);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            if (link.getReferer() != null && !link.getReferer().isEmpty()) {
                conn.setRequestProperty("Referer", link.getReferer());
            }
            conn.setRequestProperty("Range", "bytes=0-1024");
            int code = conn.getResponseCode();
            String contentType = conn.getContentType();
            System.out.println("    HTTP " + code + " | Content-Type: " + contentType + " for: " + streamUrl.substring(0, Math.min(streamUrl.length(), 100)));
        } catch (Exception e) {
            System.out.println("    HTTP FAILED: " + e.getMessage() + " for: " + streamUrl.substring(0, Math.min(streamUrl.length(), 100)));
        }
    }
}
