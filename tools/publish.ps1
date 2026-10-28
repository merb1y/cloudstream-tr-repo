# publish.ps1
#
# Depoyu GitHub'da yayınlanacak hale getirir:
#   - repo.json içindeki depo adresini GitHub adresinle değiştirir
#   - plugins.json'u o adrese göre yeniden üretir (hash + boyutlar güncellenir)
#   - sıradaki git komutlarını ekrana yazar
#
# Kullanım:
#   .\tools\publish.ps1 -User KULLANICI_ADIN
#   .\tools\publish.ps1 -User KULLANICI_ADIN -Repo benim-repom -Branch main
#
param(
    [Parameter(Mandatory = $true)][string]$User,
    [string]$Repo = "cloudstream-tr-repo",
    [string]$Branch = "main",
    [string]$RepoName = "Merby Türkçe Repo",
    [string]$RepoDescription = "Kendi CloudStream eklentilerim"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$baseUrl = "https://raw.githubusercontent.com/$User/$Repo/$Branch"
$utf8 = New-Object System.Text.UTF8Encoding($false)

# --- repo.json ---
$repoText = @"
{
  "name": "$RepoName",
  "description": "$RepoDescription",
  "manifestVersion": 1,
  "pluginLists": [
    "$baseUrl/plugins.json"
  ]
}
"@
[System.IO.File]::WriteAllText((Join-Path $root "repo.json"), $repoText, $utf8)
Write-Host "repo.json guncellendi: $baseUrl/repo.json"

# --- plugins.json + plugins/ ---
& (Join-Path $root "tools\build-repo.ps1") -BaseUrl $baseUrl

# --- ozet ---
Write-Host ""
Write-Host "Depo adresi (arkadasina verecegin adres):" -ForegroundColor Cyan
Write-Host "  $baseUrl/repo.json" -ForegroundColor Cyan
Write-Host ""
Write-Host "Sirada yapman gerekenler:" -ForegroundColor Yellow
Write-Host "  1) GitHub'da public repo olustur: $Repo"
Write-Host "  2) Bu klasorde:"
Write-Host "       git init"
Write-Host "       git add ."
Write-Host "       git commit -m `"ilk yukleme`""
Write-Host "       git branch -M $Branch"
Write-Host "       git remote add origin https://github.com/$User/$Repo.git"
Write-Host "       git push -u origin $Branch"
Write-Host ""
Write-Host "Not: git ilk push'ta kullanici adi + Personal Access Token ister (sifre degil)."
