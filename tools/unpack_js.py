import re
import sys
import base64

B62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"


def to36(n):
    return B62[n]


def unpack_js(src):
    """Dean Edwards packer cozumucu."""
    m = re.search(r"eval\(function\(p,a,c,k,e,d\)\{.*?\}\('(.*?)',(\d+),(\d+),'(.*?)'\.split\('\|'\),0,\{\}\)\)", src, re.S)
    if not m:
        return None
    p, a, c, k = m.group(1), int(m.group(2)), int(m.group(3)), m.group(4).split('|')

    def e(n):
        if n < a:
            return ''
        return e(n // a) + (chr(n % a + 29) if (n % a) > 35 else to36(n % a))

    for i in range(c - 1, -1, -1):
        if i < len(k) and k[i]:
            token = e(i)
            if token:
                p = re.sub(r'\b' + re.escape(token) + r'\b', lambda _m, v=k[i]: v, p)
    # JS'te kacis dizileri cozulur
    p = p.replace("\\'", "'").replace('\\"', '"').replace("\\/", "/").replace("\\n", "\n")
    return p


def b64d(s):
    s = s.strip()
    pad = 4 - (len(s) % 4)
    if pad != 4:
        s += "=" * pad
    return base64.b64decode(s).decode('latin-1')


def caesar(text, shift):
    out = []
    for ch in text:
        o = ord(ch)
        if 65 <= o <= 90:
            out.append(chr((o - 65 + shift) % 26 + 65))
        elif 97 <= o <= 122:
            out.append(chr((o - 97 + shift) % 26 + 97))
        else:
            out.append(ch)
    return ''.join(out)


def extract_packed(html):
    start = html.find("eval(function(p,a,c,k,e,d)")
    if start < 0:
        return None
    i = start + 4  # 'eval' sonrasi
    depth = 0
    quote = None
    esc = False
    while i < len(html):
        ch = html[i]
        if quote:
            if esc:
                esc = False
            elif ch == '\\':
                esc = True
            elif ch == quote:
                quote = None
        else:
            if ch in "'\"":
                quote = ch
            elif ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
                if depth == 0:
                    return html[start:i + 1]
        i += 1
    return None


def main():
    path = sys.argv[1]
    host = sys.argv[2]
    html = open(path, encoding='utf-8', errors='replace').read()

    packed = extract_packed(html)
    if packed is None:
        print("packed JS bulunamadi")
        return

    code = unpack_js(packed)
    if code is None:
        print("unpack basarisiz")
        return

    print("=== COZULMUS JS (ilk 3000) ===")
    print(code[:3000])
    print()

    # 2) decode fonksiyonunu Python'a uyarla ve calistir
    # fonksiyon: <name>(<parts>.split('<sep>'))
    call = re.search(r"(\w+)\s*=\s*(\w+)\('([^']+)'\.split\('(.)'\)\)", code)
    if not call:
        print("decode cagrisi bulunamadi")
        return

    var_name, func_name, parts_str, sep = call.groups()
    print(f"degisken={var_name} fonksiyon={func_name} ayirici={sep!r}")
    parts = parts_str.split(sep)
    print(f"parca sayisi: {len(parts)}")

    # fonksiyon govdesini al
    fm = re.search(r"function\s+" + re.escape(func_name) + r"\s*\((\w+)\)\s*\{", code)
    if not fm:
        print("fonksiyon govdesi bulunamadi")
        return
    start = fm.end()
    depth = 1
    i = start
    while depth > 0 and i < len(code):
        if code[i] == '{':
            depth += 1
        elif code[i] == '}':
            depth -= 1
        i += 1
    body = code[start:i - 1]
    print("=== FONKSIYON GOVDESI ===")
    print(body[:4000])

    # 3) Python yorumu: c78 benzeri (splice + hostname)
    arr = list(parts)
    n = len(arr) - 2
    rem1 = n % 7
    rem2 = 8 + (n % 5)
    p2 = arr.pop(rem2)
    seed = arr.pop(rem1) + host
    data = ''.join(arr)

    if len(seed) > 4096:
        data = b64d(data)

    h1 = 0
    h2 = 0
    for idx, ch in enumerate(seed):
        o = ord(ch)
        h1 = (h1 * 37 + o) % 241
        h2 = (h2 + ((o << 1) ^ idx)) & 255

    wv = (h1 * 3 + h2) % 256
    vb = (h2 % 11) + 5
    hw = ((h2 * 251 + h1) % 65519) + 1

    for ch in reversed(p2):
        if ch == '7':
            data = b64d(data)
        elif ch == '3':
            data = data[::-1]
        else:
            shift = (26 - ((ord(ch) - 96) % 26)) % 26
            data = caesar(data, shift)

    if len(p2) > 2048:
        data = data[::-1]

    L = len(data)
    perm = [0] * (L + 1)
    for i in range(L - 1, 0, -1):
        hw = (hw * 97 + 41) % 65519
        perm[i] = hw % (i + 1)

    chars = list(data)
    for i in range(1, L):
        j = perm[i]
        chars[i], chars[j] = chars[j], chars[i]
    data = ''.join(chars)

    acc = wv
    out = []
    for ch in data:
        o = ord(ch)
        acc = (acc * 5 + vb) % 256
        out.append(chr(o ^ acc))
        acc = (acc + o) % 256

    print("=== SONUC ===")
    print(''.join(out))


main()
