$ErrorActionPreference = "Stop"
$dir = "C:\Users\merby\AppData\Local\Temp\opencode\probe"
New-Item -ItemType Directory -Force -Path $dir | Out-Null

$baseKekik = "https://raw.githubusercontent.com/keyiflerolsun/Kekik-cloudstream/builds"
$baseWio = "https://raw.githubusercontent.com/Wiojelt/TurkSpor/builds"

$kekik = @(
    "Dizilla","DiziMom","DiziYou","SezonlukDizi","DiziKorea","KoreanTurk","NetflixMirror",
    "FilmModu","FullHDFilm","FullHDFilmizlesene","JetFilmizle","WebteIzle","KultFilmler",
    "SetFilmIzle","SinemaCX","SuperFilmGeldi","UgurFilm","SineWix","IzleAI","RareFilmm","Watch2Movies",
    "GolgeTV"
)
$wio = @(
    "AtomSpor","KacakSpor","DaddyLive","NewTVLive","TimStreams","Ntvs","NebulaSports","LeoTV",
    "PapazSports","StbTV","StreamEast","KoolTV","VegolTV","BetmatikTV","Crex","KoalaTV","KralSporHD",
    "MacKeyfi","NetVGold","PatronHD","SelcukSports","Streamed","TRGoals","TurkSpor","VionTV",
    "VivoXSpor","WioMulti","ZbahisTV","ArdaSpor","AslanTV","BeyazElma","BirdirbirIPTV","CiftD2M",
    "InterSporTV","JestYayin","SeyirTURK","MahsunSports","Taraftarium24","WioSpor",
    "Dexter","Warflix"
)

foreach ($n in $kekik) {
    $f = Join-Path $dir "$n.cs3"
    if (Test-Path $f) { continue }
    try {
        Invoke-WebRequest -Uri "$baseKekik/$n.cs3" -OutFile $f -UseBasicParsing -TimeoutSec 60
        Write-Host "OK   $n"
    } catch { Write-Host "FAIL $n $($_.Exception.Message)" }
}
foreach ($n in $wio) {
    $f = Join-Path $dir "$n.cs3"
    if (Test-Path $f) { continue }
    try {
        Invoke-WebRequest -Uri "$baseWio/$n.cs3?v=1" -OutFile $f -UseBasicParsing -TimeoutSec 60
        Write-Host "OK   $n"
    } catch { Write-Host "FAIL $n $($_.Exception.Message)" }
}
Write-Host "TOPLAM: $((Get-ChildItem $dir -Filter *.cs3).Count) dosya"
