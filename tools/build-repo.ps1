# build-repo.ps1
#
# Derlenen eklentileri toplar, plugins/ klasörüne kopyalar, SHA-256 + boyut hesaplar
# ve CloudStream'in okuduğu plugins.json dosyasını üretir.
#
# Eklenti bilgileri (sürüm, açıklama, tvTypes, ikon, yazarlar) her modülün
# build.gradle.kts dosyasındaki "cloudstream { ... }" bloğundan otomatik okunur.
# tools/plugins-meta.json sadece İSTEĞE BAĞLI üzerine yazma (override) içindir.
#
# Kullanım:
#   .\tools\build-repo.ps1
#   .\tools\build-repo.ps1 -BaseUrl "https://raw.githubusercontent.com/KULLANICI/cloudstream-tr-repo/main"
#
param(
    [string]$BaseUrl = "https://raw.githubusercontent.com/KULLANICI/cloudstream-tr-repo/main"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$pluginsDir = Join-Path $root "plugins"
$outPath = Join-Path $root "plugins.json"
$metaPath = Join-Path $root "tools\plugins-meta.json"

$skipDirs = @("tools", "gradle", "plugins", "build", ".gradle", ".kotlin", ".git")

function Get-Sha256([string]$path) {
    return "sha256-" + (Get-FileHash -Path $path -Algorithm SHA256).Hash.ToLower()
}

function Get-QuotedList([string]$text, [string]$field) {
    $m = [regex]::Match($text, "$field\s*=\s*listOf\(([^)]*)\)")
    if (!$m.Success) { return @() }
    return [regex]::Matches($m.Groups[1].Value, '"((?:[^"\\]|\\.)*)"') | ForEach-Object { $_.Groups[1].Value }
}

function Get-StringField([string]$text, [string]$field) {
    $m = [regex]::Match($text, "$field\s*=\s*""((?:[^""\\]|\\.)*)""")
    if ($m.Success) { return $m.Groups[1].Value }
    return $null
}

function Get-IntField([string]$text, [string]$field) {
    $m = [regex]::Match($text, "$field\s*=\s*(\d+)")
    if ($m.Success) { return [int]$m.Groups[1].Value }
    return $null
}

# İsteğe bağlı override dosyası
$overrides = @()
if (Test-Path $metaPath) {
    $raw = [System.IO.File]::ReadAllText($metaPath, [System.Text.Encoding]::UTF8)
    if ($raw.Trim()) {
        $parsed = $raw | ConvertFrom-Json
        if ($parsed) { $overrides = @($parsed) }
    }
}

New-Item -ItemType Directory -Force -Path $pluginsDir | Out-Null
$entries = @()

foreach ($dir in (Get-ChildItem $root -Directory | Where-Object { $skipDirs -notcontains $_.Name })) {
    $gradleFile = Join-Path $dir.FullName "build.gradle.kts"
    if (!(Test-Path $gradleFile)) { continue }

    $text = [System.IO.File]::ReadAllText($gradleFile, [System.Text.Encoding]::UTF8)

    # Eklentinin .cs3 çıktısı
    $cs3 = Get-ChildItem -Path $dir.FullName -Filter "*.cs3" -Recurse -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (!$cs3) {
        Write-Warning "$($dir.Name): .cs3 bulunamadı (önce derle)"
        continue
    }

    $block = $text
    $b = [regex]::Match($text, '(?s)cloudstream\s*\{(.*?)\}')
    if ($b.Success) { $block = $b.Groups[1].Value }

    $internalName = $dir.Name
    $override = $overrides | Where-Object { $_.module -eq $internalName } | Select-Object -First 1

    $version = if ($override -and $override.PSObject.Properties["version"]) { [int]$override.version } else { Get-IntField $text "version" }
    if (!$version) { $version = 1 }

    $name = if ($override -and $override.name) { $override.name } else { $internalName }
    # status 0 (Down) da geçerli bir değer olduğu için varlık kontrolü yapılıyor
    $status = if ($override -and $override.PSObject.Properties["status"]) { [int]$override.status } else { Get-IntField $block "status" }
    if ($null -eq $status) { $status = 3 }

    $description = if ($override -and $override.description) { $override.description } else { Get-StringField $block "description" }
    $language = if ($override -and $override.language) { $override.language } else { Get-StringField $block "language" }
    $iconUrl = if ($override -and $override.iconUrl) { $override.iconUrl } else { Get-StringField $block "iconUrl" }

    $authors = if ($override -and $override.authors) { @($override.authors) } else { @(Get-QuotedList $block "authors") }
    $tvTypes = if ($override -and $override.tvTypes) { @($override.tvTypes) } else { @(Get-QuotedList $block "tvTypes") }

    $cs3Target = Join-Path $pluginsDir "$internalName.cs3"
    Copy-Item $cs3.FullName $cs3Target -Force

    $entry = [ordered]@{
        internalName  = $internalName
        name          = $name
        version       = $version
        url           = "$BaseUrl/plugins/$internalName.cs3"
        fileHash      = Get-Sha256 $cs3Target
        fileSize      = (Get-Item $cs3Target).Length
        status        = $status
        apiVersion    = 1
        language      = $language
        authors       = $authors
        description   = $description
        tvTypes       = $tvTypes
        iconUrl       = $iconUrl
        repositoryUrl = $BaseUrl
    }

    # Varsa JVM jar çıktısı (masaüstü sürüm dex dönüşümü yapmadan yükler)
    $jar = Get-ChildItem -Path $dir.FullName -Filter "$internalName.jar" -Recurse -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($jar) {
        $jarTarget = Join-Path $pluginsDir "$internalName.jar"
        Copy-Item $jar.FullName $jarTarget -Force
        $entry["jarUrl"] = "$BaseUrl/plugins/$internalName.jar"
        $entry["jarHash"] = Get-Sha256 $jarTarget
        $entry["jarFileSize"] = (Get-Item $jarTarget).Length
    }

    $entries += $entry
    Write-Host ("  {0,-18} v{1,-5} {2} ({3:N0} bayt)" -f $internalName, $version, $entry["url"], $entry["fileSize"])
}

if ($entries.Count -eq 0) {
    throw "Hiç eklenti bulunamadı. Önce derle: .\gradlew.bat make"
}

# ---------------------------------------------------------------------------
# JSON yazımı elle yapılıyor.
# Sebep: PowerShell 5.1'in ConvertTo-Json'u tek elemanlı dizileri düz metne
# çeviriyor ("authors": "x"), CloudStream ise liste bekliyor. Bu yüzden
# authors / tvTypes her zaman [...] olarak yazılmalı.
# ---------------------------------------------------------------------------
function Json-Str([string]$value) {
    if ($null -eq $value) { return "null" }
    $escapes = @{
        '"'  = '\"'
        '\'  = '\\'
        "`b" = '\b'
        "`f" = '\f'
        "`n" = '\n'
        "`r" = '\r'
        "`t" = '\t'
    }
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.Append('"')
    foreach ($ch in $value.ToCharArray()) {
        $key = [string]$ch
        if ($escapes.ContainsKey($key)) {
            [void]$sb.Append($escapes[$key])
        } elseif ([int]$ch -lt 32) {
            [void]$sb.Append(('\u{0:x4}' -f [int]$ch))
        } else {
            [void]$sb.Append($ch)
        }
    }
    [void]$sb.Append('"')
    return $sb.ToString()
}

function Json-StrList($values) {
    $parts = @()
    foreach ($v in @($values)) {
        if ($null -ne $v -and "$v" -ne "") { $parts += Json-Str "$v" }
    }
    return "[" + ($parts -join ", ") + "]"
}

$sb = New-Object System.Text.StringBuilder
[void]$sb.Append("[`n")
for ($i = 0; $i -lt $entries.Count; $i++) {
    $e = $entries[$i]
    $rows = New-Object System.Collections.Generic.List[string]
    foreach ($key in @("internalName", "name", "version", "url", "fileHash", "fileSize")) {
        $val = $e[$key]
        if ($key -in @("version", "fileSize")) { $rows.Add("    `"$key`": $val") }
        else { $rows.Add("    `"$key`": " + (Json-Str "$val")) }
    }
    if ($e.Contains("jarUrl")) {
        $rows.Add('    "jarUrl": ' + (Json-Str "$($e["jarUrl"])"))
        $rows.Add('    "jarHash": ' + (Json-Str "$($e["jarHash"])"))
        $rows.Add("    `"jarFileSize`": $($e["jarFileSize"])")
    }
    $rows.Add("    `"status`": $($e["status"])")
    $rows.Add("    `"apiVersion`": $($e["apiVersion"])")
    $rows.Add('    "language": ' + (Json-Str "$($e["language"])"))
    $rows.Add('    "authors": ' + (Json-StrList $e["authors"]))
    $rows.Add('    "description": ' + (Json-Str "$($e["description"])"))
    $rows.Add('    "tvTypes": ' + (Json-StrList $e["tvTypes"]))
    $rows.Add('    "iconUrl": ' + (Json-Str "$($e["iconUrl"])"))
    $rows.Add('    "repositoryUrl": ' + (Json-Str "$($e["repositoryUrl"])"))
    [void]$sb.Append("  {`n")
    [void]$sb.Append(($rows -join ",`n"))
    [void]$sb.Append("`n  }")
    if ($i -lt $entries.Count - 1) { [void]$sb.Append(",") }
    [void]$sb.Append("`n")
}
[void]$sb.Append("]`n")

[System.IO.File]::WriteAllText($outPath, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))

Write-Host ""
Write-Host "plugins.json yazıldı: $outPath ($($entries.Count) eklenti)"
Write-Host "Depo adresi: $BaseUrl/repo.json"
