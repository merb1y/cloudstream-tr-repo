param(
    [Parameter(Mandatory=$true)][string]$Cs3,
    [int]$Items = 3,
    [int]$LinkItems = 2,
    [int]$LinkTest = 2
)

$jdk = "C:\cs-build\tools\jdk\jdk-21.0.12.1+1\bin"
$app = "C:\cs-build\desktopApp\build\compose\binaries\main\app\CloudStream\app"
$cp = (Get-ChildItem $app -Filter *.jar | ForEach-Object { $_.FullName }) -join ';'
$out = "C:\cs-repo\tools\out"

if (-not (Test-Path "$out\ApiProbe.class")) {
    & "$jdk\javac.exe" -cp $cp -d $out "C:\cs-repo\tools\ApiProbe.java" | Out-Null
}

$env:JAVA_TOOL_OPTIONS = $null
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
& "$jdk\java.exe" -cp "$cp;$out" ApiProbe $Cs3 $Items $LinkItems $LinkTest
exit $LASTEXITCODE
