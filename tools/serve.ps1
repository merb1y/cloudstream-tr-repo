# serve.ps1
# Repoyu http://127.0.0.1:8765/ adresinde yayınlar (GitHub'a atmadan test için).
# Not: CloudStream'de DNS-over-HTTPS (DoH) açıkken "localhost" çözümlenemez ("private hosts not resolved"),
# bu yüzden 127.0.0.1 IP adresi kullanılır ve port doluysa bir sonraki boş porta geçilir.
# Kapatmak için: Ctrl+C

param([int]$Port = 8765)

$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$listener = $null
for ($p = $Port; $p -le ($Port + 10); $p++) {
    $candidate = New-Object System.Net.HttpListener
    $prefix = "http://127.0.0.1:$p/"
    $candidate.Prefixes.Add($prefix)
    try {
        $candidate.Start()
        $listener = $candidate
        $Port = $p
        break
    } catch {
        try { $candidate.Close() } catch {}
    }
}
if (!$listener) {
    throw "Port $Port..$($Port+10) aralığında dinleyici başlatılamadı."
}

Write-Host "Repo yayında: ${prefix}repo.json"
Write-Host "CloudStream -> Eklentiler -> Depo ekle -> ${prefix}repo.json"
Write-Host "Durdurmak için Ctrl+C"
Write-Host ""

$types = @{
    ".json" = "application/json; charset=utf-8"
    ".cs3"  = "application/octet-stream"
    ".jar"  = "application/java-archive"
    ".md"   = "text/plain; charset=utf-8"
}

try {
    while ($listener.IsListening) {
        try {
            $ctx = $listener.GetContext()
        } catch {
            break
        }

        $path = $ctx.Request.Url.AbsolutePath.TrimStart("/")
        if ([string]::IsNullOrWhiteSpace($path)) { $path = "repo.json" }
        $file = Join-Path $root $path

        if ((Test-Path $file) -and !(Test-Path $file -PathType Container)) {
            if ($path -ieq "repo.json") {
                # Testte pluginLists adresini 127.0.0.1'e çevir; repo.json dosyası yayın için bozulmadan kalır
                $json = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8)
                $json = [regex]::Replace($json, 'https?://[^"]*/plugins\.json', "${prefix}plugins.json")
                $bytes = (New-Object System.Text.UTF8Encoding($false)).GetBytes($json)
                $ctx.Response.ContentType = "application/json; charset=utf-8"
            } elseif ($path -ieq "plugins.json") {
                # Eklenti adreslerini de 127.0.0.1'e çevir; dosya GitHub için hazır kalır
                $base = $prefix.TrimEnd("/")
                $json = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8)
                $json = [regex]::Replace($json, 'https?://[^"\s]*/plugins/([^"\s]+\.(?:cs3|jar))', "${prefix}plugins/`$1")
                $json = [regex]::Replace($json, '("repositoryUrl"\s*:\s*")[^"]*(")', "`${1}$base`${2}")
                $bytes = (New-Object System.Text.UTF8Encoding($false)).GetBytes($json)
                $ctx.Response.ContentType = "application/json; charset=utf-8"
            } else {
                $bytes = [System.IO.File]::ReadAllBytes($file)
                $ext = [System.IO.Path]::GetExtension($file).ToLower()
                $ctx.Response.ContentType = if ($types.ContainsKey($ext)) { $types[$ext] } else { "application/octet-stream" }
            }
            $ctx.Response.ContentLength64 = $bytes.Length
            $ctx.Response.OutputStream.Write($bytes, 0, $bytes.Length)
            Write-Host "200 $path ($($bytes.Length) bayt)"
        } else {
            $ctx.Response.StatusCode = 404
            Write-Host "404 $path"
        }
        $ctx.Response.Close()
    }
} finally {
    if ($listener) { $listener.Stop(); $listener.Close() }
}
