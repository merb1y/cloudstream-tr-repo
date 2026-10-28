# Lisans ve Atıf Bildirimi

Bu depodaki eklentilerin bir kısmı **GPL-3.0** lisanslı açık kaynak projelerden
alınmış ve bu depoya uyarlanmıştır. GPL-3.0, türev çalışmaların da aynı lisansla
dağıtılmasını ve kaynak kodun erişilebilir olmasını şart koşar. Bu depo bu şartı
karşılar: kaynak kod bu depoda açıktır, tam lisans metni `LICENSE` dosyasındadır.

## Kaynak projeler

| Proje | Depo | Lisans |
|---|---|---|
| feroxx/Kekik-cloudstream | https://github.com/feroxx/Kekik-cloudstream | GPL-3.0 |
| keyiflerolsun/Kekik-cloudstream (orijinal) | https://github.com/keyiflerolsun/Kekik-cloudstream | GPL-3.0 |

Eklentiler `feroxx/Kekik-cloudstream` deposundan alınmıştır (bu depo, orijinal
Kekik-cloudstream projesinin GPL-3.0 lisanslı devamıdır).

## Eklenti bazında atıf

| Klasör | Sürüm | Orijinal yazarlar | Kaynak |
|---|---|---|---|
| `DiziBox` | 1025 | keyiflerolsun | feroxx/Kekik-cloudstream |
| `DiziPal` | 1117 | keyiflerolsun, muratcesmecioglu | feroxx/Kekik-cloudstream |
| `FilmMakinesi` | 1059 | keyiflerolsun, usdogu | feroxx/Kekik-cloudstream |
| `HDFilmCehennemi` | 1058 | hexated, keyiflerolsun | feroxx/Kekik-cloudstream |
| `InatBox` | 1030 | JustRelaxable, keyiflerolsun | feroxx/Kekik-cloudstream |
| `RecTV` | 1116 | keyiflerolsun, yusiqo, inatchii, JustRelaxable | feroxx/Kekik-cloudstream |
| `CanliTV` | 1010 | Adippe, keyiflerolsun | feroxx/Kekik-cloudstream |
| `OrnekDizi` | 1 | merby | Bu depo için yazıldı (GPL-3.0) |

## Sürüm numaraları hakkında

Alınan eklentilerin sürüm numaralarına **+1000** eklenmiştir (örn. `25` → `1025`).
Sebebi: CloudStream, aynı `internalName` ile kurulu bir eklentinin üzerine yazmak
için daha yüksek bir `version` değeri arar. Bu sayede bu depodan yapılan kurulum,
kullanıcıda zaten kurulu olan eski sürümün yerine geçer ve iki kopya çakışmaz.

Sürüm artırma kuralı: bir sonraki güncellemede `1025` → `1026` gibi normal artış
kullanılabilir. `internalName` **asla** değiştirilmemelidir.

## Bu depoda yapılan değişiklikler

Kaynak kodda yapılan değişiklikler doğrudan görülebilir; başlıcaları:

- Sürüm numaraları +1000 (yukarıya bakın)
- Kök `build.gradle.kts` / `settings.gradle.kts` bu depoya göre yeniden yazıldı
- `tools/` altındaki derleme ve depo üretme betikleri bu depo için yazıldı
- Eklenti mantığı (sayfa ayrıştırma, oynatıcı çözümü) değiştirilmedi

## Lisans metni

Tam GPL-3.0 metni: [`LICENSE`](LICENSE)

## Uyarı

Bu eklentiler yalnızca üçüncü taraf sitelerdeki herkese açık sayfaları ayrıştırır;
hiçbir içerik bu depoda barındırılmaz. İçeriklerin telif durumu ilgili sitelerin
sorumluluğundadır. Kullanım kendi sorumluluğunuzdadır.
