# Merby Türkçe CloudStream Repo

Kendi CloudStream eklenti deponuz (repo). İçinde **derlenmiş ve gerçek sitelere/yayınlara karşı doğrulanmış 20 gerçek eklenti** yer alır (4 Canlı TV, 10+ Dizi, 8+ Film, 4 Anime/Asya).

## Depodaki eklentiler

### Canlı TV (4 kaynak)
| Eklenti | Sürüm | Tür | Ne yapar |
|---|---|---|---|
| CanliTV | 1010 | Canlı TV | Ulusal ve TRT kanalları (m3u listesi) |
| AcikTV | 1001 | Canlı TV | iptv-org Türkiye canlı kanal listesi (172 kanal, Türkçe kategorili) |
| TurkiyeTV | 1001 | Canlı TV | Free-TV Türkiye canlı yayın listesi (TRT ve ulusal kanallar) |
| DunyaTV | 1001 | Canlı TV | iptv-org Türkçe yayın yapan uluslararası kanallar |

### Dizi & Belgesel
| Eklenti | Sürüm | Tür | Ne yapar |
|---|---|---|---|
| DiziPal | 1120 | Dizi, Film | Yabancı/yerli dizi + film arşivi (gömülü + OpenSubtitles altyazı) |
| DiziBox | 1025 | Dizi | Yabancı diziler (dizibox.live) |
| DiziYou | 1026 | Dizi | Yabancı dizi arşivi (Türkçe dublaj & altyazı) |
| DiziPod | 1003 | Dizi, Film | Dizi ve film içerikleri |
| Ddizi | 1024 | Dizi | Yerli diziler ve güncel televizyon bölümleri (ddizi.im) |
| DiziBal | 1001 | Dizi, Film, Anime | Yabancı dizi, film ve anime arşivi |
| DMax | 1002 | Dizi / Belgesel | DMAX Türkiye program ve belgesel bölümleri (dmax.com.tr) |
| TLCtr | 1019 | Dizi / Program | TLC Türkiye program ve bölümleri (tlctv.com.tr) |

### Film
| Eklenti | Sürüm | Tür | Ne yapar |
|---|---|---|---|
| HDFilmCehennemi | 1058 | Film, Dizi | Film + dizi arşivi (hdfilmcehennemi.nl, altyazı destekli) |
| FilmMakinesi | 1061 | Film | Film arşivi (filmmakinesi.to, CloseLoad doğrudan HLS + altyazı) |
| FilmModu | 1019 | Film | HD film arşivi (filmmodu) |
| FullHDFilm | 1036 | Film, Dizi | Full HD film ve dizi izleme sitesi |
| KultFilmler | 1016 | Film, Dizi | Kült ve sanat filmleri arşivi (kultfilmler.net) |

### Anime & Asya Dizileri
| Eklenti | Sürüm | Tür | Ne yapar |
|---|---|---|---|
| AnimeciX | 1021 | Anime | Türkçe anime izleme ve keşif platformu |
| KoreanTurk | 1006 | Asya Dizisi, Film | Kore dizileri ve filmleri (koreanturk) |
| WebdramaTurkey2 | 1001 | Asya Dizisi, Anime | Asya dizileri, filmleri ve animeleri |

Hepsi açık kaynak (GPL-3.0) `feroxx/Kekik-cloudstream` deposundan alınmıştır.
Lisans ve atıf detayları: [`LICENSE-NOTICE.md`](LICENSE-NOTICE.md)

> **Not:** MahsunSports / DomatesTV / Taraftarium24 eklentilerinin kaynak kodu kamuya
> açık değil. Bu üçü için aşağıdaki "Spor içerikleri" bölümüne bakın.

## Spor içerikleri (ayrı depo)

Spor kanalları için ayrı ve aktif bakımlı bir depo kullanmak daha sağlıklı
(bu eklentiler kapalı kaynak, kendi depomuza kopyalanamaz):

- **TurkSpor** — `https://raw.githubusercontent.com/Wiojelt/TurkSpor/main/repo.json`
  (MahsunSports, DomatesTV, Taraftarium24, SelcukSports ve ~30 spor kaynağı)
- **WioSpor** — `https://raw.githubusercontent.com/Wiojelt/WioSpor/main/repo.json`
  (düzenli canlı TV + spor kanalları)

## Yapı

```
repo.json                -> depo manifesti (CloudStream'e eklenen adres)
plugins.json             -> eklenti listesi (script üretir, elle düzenlenmez)
plugins/                 -> derlenmiş .cs3 dosyaları (script doldurur)
tools/build-repo.ps1     -> derlenenleri toplar, SHA-256 hesaplar, plugins.json yazar
tools/serve.ps1          -> depoyu localhost:8765 üzerinde yayınlar (test için)
tools/plugins-meta.json  -> isteğe bağlı override (ad, sürüm, durum)
tools/ConvertTest.java   -> .cs3 dosyalarının gerçekten yüklenebildiğini doğrular
OrnekDizi/               -> örnek eklentinin kaynak kodu (yeni eklenti için şablon)
<Modül>/                 -> her eklenti bir klasör
```

Eklenti bilgileri (sürüm, açıklama, tür, yazarlar, ikon) her modülün
`build.gradle.kts` dosyasındaki `cloudstream { ... }` bloğundan **otomatik** okunur.

## Derleme

```powershell
$env:JAVA_HOME="C:\cs-build\tools\jdk\jdk-21.0.12.1+1"
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"

cd C:\cs-repo
.\gradlew.bat make                       # hepsini derle
.\gradlew.bat :DiziBox:make              # tek modül
.\tools\build-repo.ps1                   # plugins/ + plugins.json üret
```

Gerekenler: JDK 21, Android SDK, Gradle wrapper (depoda var).
Yol **ASCII** olmalı (Android Gradle Plugin Türkçe karakterli yollarda hata verir).

## Yerel test (GitHub'a atmadan)

```powershell
.\tools\serve.ps1
```

CloudStream → Ayarlar → Eklentiler → Depo ekle → `http://localhost:8765/repo.json`

`serve.ps1` çalışırken `repo.json` içindeki `pluginLists` adresini otomatik olarak
localhost'a çevirir; dosya yayın için bozulmadan kalır.

## Yayınlama (GitHub)

Tek komut, kullanıcı adını ver yeter:

```powershell
cd C:\cs-repo
.\tools\publish.ps1 -User KULLANICI_ADIN
```

Bu komut `repo.json` ve `plugins.json` içindeki tüm adresleri GitHub adresine
göre yeniden yazar, hash/boyutları günceller ve sıradaki git komutlarını ekrana basar.

Sonra GitHub'da `cloudstream-tr-repo` adında public repo aç ve:

```powershell
git init
git add .
git commit -m "ilk yukleme"
git branch -M main
git remote add origin https://github.com/KULLANICI_ADIN/cloudstream-tr-repo.git
git push -u origin main
```

Arkadaşına vereceğin adres:
`https://raw.githubusercontent.com/KULLANICI_ADIN/cloudstream-tr-repo/main/repo.json`

> Push sırasında şifre değil **Personal Access Token (PAT)** istenir:
> GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens

> `raw.githubusercontent.com` Türkiye'de zaman zaman yavaş/engelli olabilir.
> Alternatif: aynı dosyaları jsDelivr veya Cloudflare Pages üzerinden yayınlayıp
> `publish.ps1` yerine `build-repo.ps1 -BaseUrl <adres>` kullanmak.

> `.gitignore` derleme çıktılarını ve `local.properties` dosyasını hariç tutar;
> `plugins/` klasörü **dahil** olmalı (yoksa eklenti indirilemez).

## Güncelleme mantığı

- `internalName` **asla** değişmez (klasör adı = internalName).
- Her yeni derlemede `version` artırılmalı, yoksa uygulama güncellemeyi görmez.
- `fileHash` elle yazılmaz; `build-repo.ps1` hesaplar.
- Alınan GPL eklentilerin sürümleri **+1000** ile başlatıldı; böylece kullanıcıda
  kurulu eski sürümün üzerine yazılır (aynı `internalName` çakışmasın diye).

## Yeni eklenti eklemek

1. `OrnekDizi` klasörünü kopyala, adını değiştir (örn. `DiziX`).
2. `DiziX/build.gradle.kts` içindeki `version`, `description`, `tvTypes`, `iconUrl` alanlarını düzenle.
3. `DiziX/src/main/kotlin/.../` altındaki sınıfı hedef siteye göre yaz:
   - `getMainPage` → ana sayfa listeleri (Jsoup ile HTML ayrıştırma)
   - `search` → site arama sayfası
   - `load` + `loadLinks` → detay sayfası ve player/iframe çözümü
4. `.\gradlew.bat :DiziX:make` ve `.\tools\build-repo.ps1`

## Site adresi değişince

Bu eklentilerin çoğu sitenin alan adını kod içinde sabit tutar. Site adres
değiştirdiğinde eklenti "içerik yok" verir. Yapılacak:

1. Modülün `*.kt` dosyasındaki `mainUrl` satırını yeni adresle güncelle.
2. `build.gradle.kts` içindeki `version` değerini 1 artır.
3. `.\gradlew.bat :<Modül>:make` + `.\tools\build-repo.ps1` + push.

## Doğrulama

`.cs3` dosyalarının yüklenebildiğini test etmek için `tools/ConvertTest.java`
kullanılır: uygulamanın kendi `DexConverter`'ını çağırır, `.cs3` → `.jar` dönüşümünü,
`manifest.json` içindeki `pluginClassName` sınıfının yüklenmesini ve sınıfın
`Plugin`'den türediğini doğrular.

## Notlar

- `.cs3` = Android (dex) derlemesi; masaüstü sürüm bunu otomatik jar'a çevirir.
- Eklentiler CloudStream API'sine (`com.lagradost:cloudstream3:pre-release`) karşı derlenir.
- Kısa linkler (cutt.ly vb.) masaüstünde genelde çalışmaz; tam URL kullanın.
