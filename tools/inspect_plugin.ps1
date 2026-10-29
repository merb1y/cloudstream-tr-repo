param(
    [Parameter(Mandatory=$true)]
    [string]$PluginPath
)

$jars = (Get-ChildItem 'C:\Users\merby\AppData\Local\CloudStream\app\*.jar' | ForEach-Object { $_.FullName })
$fullCp = ($jars + 'C:\Users\merby\cloudstream-tr-repo\tools\out') -join [System.IO.Path]::PathSeparator
$runArgs = "-cp`r`n$fullCp`r`nProviderInspector`r`n$PluginPath"
[System.IO.File]::WriteAllText('C:\Users\merby\cloudstream-tr-repo\tools\java_run.txt', $runArgs)
& 'C:\cs-build\tools\jdk\jdk-21.0.12.1+1\bin\java.exe' '@C:\Users\merby\cloudstream-tr-repo\tools\java_run.txt'
