# CloudStream Türkçe Eklenti Deposu (TR Repo)

CloudStream uygulaması için özel olarak optimize edilmiş, uçtan uca test edilmiş ve **%100 çalışan Türkçe eklenti deposu (Repository)**.

Tüm eklentiler CloudStream Desktop (Windows/JVM) ve Android ortamlarında **ProviderInspector** ile kapsamlı testlerden geçirilmiş; Android GUI çökmesi yaratan kütüphane hataları (`Bitmap$Config ARGB_8888`, `OnClickListener`), Cloudflare / alan adı engelleri ve bozuk oynatıcılar temizlenmiştir. Her eklentinin canlı akış / M3U8 video oynatımı doğrulanmıştır.

---

## 🚀 Hızlı Kurulum (CloudStream'e Ekleme)

1. **CloudStream** uygulamasını açın.
2. **Ayarlar (Settings)** → **Eklentiler (Extensions)** bölümüne gidin.
3. **Depo Ekle (Add Repository)** butonuna tıklayın.
4. Depo Adı: `Merby TR Repo`
5. Depo URL'si:
```
https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/repo.json
```
6. **İndir / Ekle** butonuna basın. Depo eklendikten sonra eklentileri tek tıkla yükleyebilirsiniz.

---

## 📦 Doğrulanmış ve Çalışan Eklentiler (12 Eklenti)

### 📺 Canlı TV & Spor Kanalları (4 Eklenti)

| Eklenti | Sürüm | Tür | Açıklama & Kanallar | Durum |
|---|---|---|---|---|
| **CanliTV** | 1010 | Canlı TV | 70+ Ulusal Türk TV Kanalı (TRT 1, ATV, Kanal D, Show TV, Star TV, TV8 vb.) | ✅ Aktif (HTTP 200) |
| **SporTV** | 1 | Spor, Canlı | Kesintisiz Spor Kanalları (A Spor HD, TRT Spor HD, TRT Spor Yıldız, HT Spor HD, TJK TV, TRT 1 HD Canlı Maç) | ✅ Aktif (HTTP 200) |
| **HaberTV** | 1 | Haber, Canlı | Kesintisiz Haber Kanalları (HaberTürk TV HD, Haber Global HD, TV 24 HD, TRT Haber HD) | ✅ Aktif (HTTP 200) |
| **BelgeselTV** | 1 | Belgesel, Kültür | Kesintisiz Belgesel & Müzik (TRT Belgesel HD, TRT Müzik HD, TRT Avaz HD, TBMM TV Meclis HD) | ✅ Aktif (HTTP 206) |

---

### 🎬 Film Platformları (4 Eklenti)

| Eklenti | Sürüm | Tür | Açıklama | Durum |
|---|---|---|---|---|
| **HDFilmCehennemi** | 7 | Film, Dizi | Türkiye'nin en popüler film platformu (Rapidrame CDN 1080p M3U8 akışları) | ✅ Aktif (HTTP 200) |
| **WebteIzle** | 15 | Film | 1080p Full HD film kütüphanesi (Vidmoly CDN doğrudan M3U8 akışı) | ✅ Aktif (HTTP 200) |
| **KultFilmler** | 15 | Film | Klasik, bağımsız ve ödüllü sinema filmleri arşivi (Vidpapi 1080p M3U8) | ✅ Aktif (HTTP 200) |
| **CineCat** | 2 | Film | 1080p yüksek kaliteli yerli ve yabancı film arşivi | ✅ Aktif (HTTP 206) |

---

### 🍿 Yabancı & Yerli Dizi Platformları (4 Eklenti)

| Eklenti | Sürüm | Tür | Açıklama | Durum |
|---|---|---|---|---|
| **DiziMom** | 10 | Dizi | Popüler yabancı diziler ve hızlı altyazı desteği (Pilavyerplay CDN 1080p M3U8) | ✅ Aktif (HTTP 200) |
| **DiziBox** | 11 | Dizi | 4.800+ yabancı dizi, tüm sezon ve bölümleriyle güncel arşiv (Vidmoly M3U8) | ✅ Aktif (HTTP 206) |
| **DiziYou** | 7 | Dizi | 1.150+ dizi, doğrudan hızlı M3U8 oynatıcı (`storage.diziyou.one`) | ✅ Aktif (HTTP 200) |
| **DiziRella** | 8 | Dizi | Güncel yabancı diziler ve tüm sezon bölümleri (Ksdpictures 1080p M3U8) | ✅ Aktif (HTTP 200) |

---

## 🛠️ Depo Yapısı

```
repo.json                -> CloudStream'e eklenen ana depo manifesti
plugins.json             -> Otomatik üretilen güncel eklenti listesi, hash'ler ve dosya boyutları
plugins/                 -> CloudStream tarafından indirilen doğrulanmış .cs3 paketleri
tools/build-repo.ps1     -> Eklentileri derler, SHA-256 hesaplar ve plugins.json'u günceller
tools/inspect_plugin.ps1 -> Her eklentiyi yükleyip API çağrılarını ve video akışlarını test eden harness
```

---

## 🧪 Test & Doğrulama Standartları

- ✅ **Desktop JVM & Android Uyumlu**: `android.graphics.Bitmap$Config.ARGB_8888` veya `View$OnClickListener` çağrıları içermez, Desktop CloudStream'de çökmez.
- ✅ **Video Akışı Doğrulandı**: Tüm eklentilerin kaynakları ve `loadLinks` fonksiyonları test edilmiş olup, en az bir adet HTTP 200/206 yanıtı veren geçerli M3U8/video akışı döner.
- ✅ **Kırık Eklentiler Temizlendi**: Alan adı kapanmış veya erişilemeyen eklentiler (DomatesTV, PatronHD, FilmMakinesi, FilmModu, FullHDFilmizlesene, SelcukSports, Taraftarium24, StbTV) repodan çıkarılmıştır.
