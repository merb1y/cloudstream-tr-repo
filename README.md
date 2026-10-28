# CloudStream Türkçe Eklenti Deposu (TR Repo)

CloudStream uygulaması için özel olarak derlenmiş, test edilmiş ve **%100 çalışan Türkçe eklenti deposu (Repository)**.

Tüm eklentiler CloudStream'in Dex/JVM motoruyla uyumluluk testinden geçirilmiş, kırık/çalışmayan API ve linkler temizlenerek **en güncel ve stabil** sürümler eklenmiştir.

---

## 🚀 Hızlı Kurulum (CloudStream'e Ekleme)

1. **CloudStream** uygulamasını açın.
2. **Ayarlar (Settings)** → **Eklentiler (Extensions)** bölümüne gidin.
3. **Depo Ekle (Add Repository)** butonuna tıklayın.
4. Aşağıdaki depo adresini yapıştırın ve **İndir / Ekle** deyin:

```
https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/repo.json
```

5. Depo eklendikten sonra listeden dilediğiniz eklentileri tek tıkla yükleyebilirsiniz.

---

## 📦 Depodaki Güncel ve Çalışan Eklentiler (16 Eklenti)

### 📺 Canlı Yayın & Spor Kanalları (6 Eklenti)

| Eklenti | Sürüm | Tür | Açıklama |
|---|---|---|---|
| **CanliTV** | 1010 | Canlı TV | Ulusal Türk TV Kanalları (TRT 1, TRT Spor, ATV, Kanal D, Show TV, Star, TV8 vb. doğrudan çalışan m3u8 akışları) |
| **SelcukSports** | 12 | Spor, Canlı | Canlı maç ve spor yayınları (beIN Sports, Exxen Spor, S Sport 1-2, Smart Spor, Tivibu Spor) |
| **Taraftarium24** | 14 | Spor, Canlı | Kesintisiz canlı maç yayınları ve spor kanalları |
| **DomatesTV** | 6 | Spor, Canlı | Dinamik canlı maç yayınları ve alternatif spor hatları |
| **StbTV** | 7 | Canlı TV, Spor | Stalker STB altyapılı canlı TV kanalları ve spor yayınları |
| **PatronHD** | 17 | Spor, Canlı | Patronspor / PatronHD canlı spor ve maç yayınları |

---

### 🎬 Film Platformları (5 Eklenti)

| Eklenti | Sürüm | Tür | Açıklama |
|---|---|---|---|
| **HDFilmCehennemi** | 7 | Film, Dizi | Türkiye'nin en büyük 1080p Full HD film kütüphanesi, Türkçe dublaj & altyazı |
| **FilmMakinesi** | 8 | Film | En yeni vizyon filmleri, hızlı oynatıcılar ve geniş film arşivi |
| **FullHDFilmizlesene** | 10 | Film | Türkiye'nin en köklü ve popüler Full HD film izleme platformu |
| **FilmModu** | 8 | Film | Yüksek kaliteli Türkçe dublaj ve altyazılı güncel film kütüphanesi |
| **WebteIzle** | 15 | Film | 1080p Full HD alternatif film kaynağı |

---

### 🍿 Yabancı & Yerli Dizi Platformları (5 Eklenti)

| Eklenti | Sürüm | Tür | Açıklama |
|---|---|---|---|
| **DiziBox** | 11 | Dizi | 4.800+ yabancı dizi, tüm sezon ve bölümleriyle eksiksiz güncel arşiv |
| **DiziPal** | 10 | Dizi, Film | Popüler dijital platformların (Netflix, Disney+, HBO, Amazon Prime, Exxen) en yeni içerikleri |
| **DiziYou** | 7 | Dizi | 1.150+ yabancı dizi, reklamsız ve doğrudan 1080p M3U8 yüksek hızlı akış |
| **DiziMom** | 10 | Dizi | Popüler yabancı diziler ve hızlı altyazı desteği |
| **SezonlukDizi** | 10 | Dizi | Düzenli güncellenen yabancı dizi kütüphanesi ve zengin sezon arşivi |

---

## 🛠️ Depo Yapısı

```
repo.json                -> CloudStream'e eklenen ana depo manifesti
plugins.json             -> Otomatik üretilen güncel eklenti listesi, hash'ler ve boyutlar
plugins/                 -> CloudStream tarafından indirilen .cs3 paketleri
tools/build-repo.ps1     -> Eklentileri derler, SHA-256 hesaplar, plugins.json'u günceller
tools/ConvertTest.java   -> .cs3 dosyalarının CloudStream motoruyla uyumluluğunu test eder
tools/serve.ps1          -> Depoyu yerel ağda (localhost:8765) test etmek için sunucu
<Modül>/                 -> Her eklentinin bağımsız Gradle yapılandırması (build.gradle.kts)
```

---

## 🧪 Test & Doğrulama

Tüm eklentiler `ConvertTest.java` ile CloudStream dex motoru üzerinde test edilmiştir:
- ✅ 16 eklentinin tamamı dex'ten JVM bayt koduna dönüştürüldü ve hatasız yüklendi.
- ✅ Tüm kaynak web siteleri ve akış adresleri anlık HTTP yanıtlarıyla doğrulandı.
- ✅ Çalışmayan veya Cloudflare Access korumasına takılan eski eklentiler (RecTV, InatBox) depodan temizlendi.

---

## 📜 Lisans & Kaynaklar

Açık kaynak eklentiler GPL-3.0 lisansı altındadır.
Eklenti geliştiricilerine ve topluluk katkıcılarına (`Wiojelt`, `keyiflerolsun`, `hexated`, `Adippe`, `muratcesmecioglu`) teşekkür ederiz.
Detaylar için: [`LICENSE-NOTICE.md`](LICENSE-NOTICE.md)
