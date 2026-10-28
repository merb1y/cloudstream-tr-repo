param([string]$PartsLine, [string]$HostName = "closeload.filmmakinesi.to")

function AtobB64([string]$s) {
    $str = $s.Trim()
    $pad = 4 - ($str.Length % 4)
    if ($pad -ne 4) { $str += ("=" * $pad) }
    return ([System.Text.Encoding]::GetEncoding(28591)).GetString([Convert]::FromBase64String($str))
}

function Caesar([string]$text, [int]$shift) {
    $sb = New-Object System.Text.StringBuilder
    foreach ($c in $text.ToCharArray()) {
        if ($c -ge 'A' -and $c -le 'Z') {
            [void]$sb.Append([char]((([int][char]$c - 65 + $shift) % 26 + 26) % 26 + 65))
        } elseif ($c -ge 'a' -and $c -le 'z') {
            [void]$sb.Append([char]((([int][char]$c - 97 + $shift) % 26 + 26) % 26 + 97))
        } else {
            [void]$sb.Append($c)
        }
    }
    return $sb.ToString()
}

$arr = New-Object System.Collections.ArrayList
foreach ($p in $PartsLine.Split('|')) { [void]$arr.Add($p) }

$len = $arr.Count
$h10z = $len - 2
$r6t6 = $h10z % 7
$c7z = 8 + ($h10z % 5)
$p2wf = $arr[$c7z]; $arr.RemoveAt($c7z)
$i9fb = $arr[$r6t6]; $arr.RemoveAt($r6t6)
$i9fb = $i9fb + $HostName
$xq0sh = -join $arr

if ($i9fb.Length -gt 4096) { $xq0sh = AtobB64 $xq0sh }

$hi72p = 0; $u7hv = 0
for ($i = 0; $i -lt $i9fb.Length; $i++) {
    $x7yz = [int][char]$i9fb[$i]
    $hi72p = ($hi72p * 37 + $x7yz) % 241
    $u7hv = ($u7hv + (($x7yz -shl 1) -bxor $i)) -band 255
}

$wv7 = ($hi72p * 3 + $u7hv) % 256
$vb5 = ($u7hv % 11) + 5
$hwc24 = (($u7hv * 251 + $hi72p) % 65519) + 1

for ($i = $p2wf.Length - 1; $i -ge 0; $i--) {
    $ch = $p2wf[$i]
    if ($ch -eq '7') { $xq0sh = AtobB64 $xq0sh }
    elseif ($ch -eq '3') { $ca = $xq0sh.ToCharArray(); [array]::Reverse($ca); $xq0sh = -join $ca }
    else {
        $en4e3 = (26 - (([int][char]$ch - 96) % 26)) % 26
        $xq0sh = Caesar $xq0sh $en4e3
    }
}
if ($p2wf.Length -gt 2048) { $ca = $xq0sh.ToCharArray(); [array]::Reverse($ca); $xq0sh = -join $ca }

$h = $xq0sh.Length
$o26j9 = New-Object 'int[]' ($h + 1)
for ($i = $h - 1; $i -ge 1; $i--) {
    $hwc24 = ($hwc24 * 97 + 41) % 65519
    $o26j9[$i] = $hwc24 % ($i + 1)
}
$rp = $xq0sh.ToCharArray()
for ($i = 1; $i -lt $h; $i++) {
    $j = $o26j9[$i]
    $tmp = $rp[$i]; $rp[$i] = $rp[$j]; $rp[$j] = $tmp
}
$xq0sh = -join $rp

$q6j07 = $wv7
$sb = New-Object System.Text.StringBuilder
for ($i = 0; $i -lt $xq0sh.Length; $i++) {
    $x7yz = [int][char]$xq0sh[$i]
    $q6j07 = ($q6j07 * 5 + $vb5) % 256
    [void]$sb.Append([char]($x7yz -bxor $q6j07))
    $q6j07 = ($q6j07 + $x7yz) % 256
}

Write-Output ("SONUC: " + $sb.ToString())
