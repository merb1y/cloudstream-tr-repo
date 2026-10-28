# serve.ps1
# Repoyu http://localhost:8765/ adresinde yayınlar (GitHub'a atmadan test için).
# Kapatmak için: Ctrl+C

param([int]$Port = 8765)

$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$prefix = "http://localhost:$Port/"
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add($prefix)
$listener.Start()
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
            # Testte pluginLists adresini localhost'a çevir; repo.json dosyası yayın için bozulmadan kalır
            $json = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8)
            $json = [regex]::Replace($json, 'https?://[^"]*/plugins\.json', "${prefix}plugins.json")
            $bytes = (New-Object System.Text.UTF8Encoding($false)).GetBytes($json)
            $ctx.Response.ContentType = "application/json; charset=utf-8"
        } elseif ($path -ieq "plugins.json") {
            # Eklenti adreslerini de localhost'a çevir; dosya GitHub için hazır kalır
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
