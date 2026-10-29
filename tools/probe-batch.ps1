param(
    [Parameter(Mandatory=$true)][string[]]$Names,
    [string]$Dir = "C:\Users\merby\AppData\Local\Temp\opencode\probe",
    [int]$Parallel = 4,
    [int]$TimeoutSec = 150,
    [int]$Items = 3,
    [int]$LinkItems = 2,
    [int]$LinkTest = 1
)

$jdk = "C:\cs-build\tools\jdk\jdk-21.0.12.1+1\bin"
$app = "C:\cs-build\desktopApp\build\compose\binaries\main\app\CloudStream\app"
$cp = (Get-ChildItem $app -Filter *.jar | ForEach-Object { $_.FullName }) -join ';'
$out = "C:\cs-repo\tools\out"
$logDir = Join-Path $Dir "logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$queue = New-Object System.Collections.Queue
foreach ($n in $Names) { $queue.Enqueue($n) }
$running = @()
$results = @()

while ($queue.Count -gt 0 -or $running.Count -gt 0) {
    while ($running.Count -lt $Parallel -and $queue.Count -gt 0) {
        $n = $queue.Dequeue()
        $log = Join-Path $logDir "$n.log"
        $errLog = Join-Path $logDir "$n.err"
        $cs3 = Join-Path $Dir "$n.cs3"
        $argList = @("-cp", "$cp;$out", "ApiProbe", $cs3, "$Items", "$LinkItems", "$LinkTest")
        $p = Start-Process -FilePath "$jdk\java.exe" -ArgumentList $argList -RedirectStandardOutput $log -RedirectStandardError $errLog -PassThru -WindowStyle Hidden
        $running += [pscustomobject]@{ Name = $n; Proc = $p; Start = (Get-Date); Log = $log }
    }
    Start-Sleep -Seconds 3
    $still = @()
    foreach ($r in $running) {
        $dur = ((Get-Date) - $r.Start).TotalSeconds
        $state = $null
        if ($r.Proc.HasExited) { $state = "done" }
        elseif ($dur -gt $TimeoutSec) {
            try { $r.Proc.Kill() } catch {}
            $state = "timeout"
        }
        if ($state) {
            $sonuc = ""
            $calisan = 0
            $oge = 0
            $link = 0
            $err = ""
            if (Test-Path $r.Log) {
                $txt = Get-Content -Raw -Path $r.Log -ErrorAction SilentlyContinue
                if ($txt) {
                    $m = [regex]::Match($txt, "SONUC: (\d+) oge, (\d+) calisan oge, (\d+) link, (\d+) calisan link")
                    if ($m.Success) {
                        $oge = [int]$m.Groups[1].Value
                        $calisan = [int]$m.Groups[2].Value
                        $link = [int]$m.Groups[3].Value
                        $sonuc = $m.Value
                    }
                }
            }
            if ($state -eq "timeout") { $sonuc = "ZAMAN ASIMI" }
            if (-not $sonuc) { $sonuc = "sonuc yok" }
            if (Test-Path $r.Log) { $err = (Get-Content $r.Log -ErrorAction SilentlyContinue | Select-String -Pattern "HATA|Exception|SINIF " | Select-Object -First 1) }
            $results += [pscustomobject]@{
                Ad = $r.Name; Durum = $state; Oge = $oge; Calisan = $calisan; Link = $link; Sonuc = $sonuc
            }
            Write-Host ("[{0,-14}] {1,-8} {2}" -f $r.Name, $state, $sonuc)
        } else {
            $still += $r
        }
    }
    $running = $still
}

Write-Host ""
Write-Host "==== OZET ===="
$results | Sort-Object -Property @{ Expression = "Calisan"; Descending = $true }, @{ Expression = "Link"; Descending = $true } |
    Format-Table Ad, Durum, Oge, Calisan, Link -AutoSize | Out-String -Width 200 | Write-Host
