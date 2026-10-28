import re
import sys

B62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"


def enc(n, a):
    if n < a:
        return B62[n] if n < 62 else chr(n + 29)
    return enc(n // a, a) + (B62[n % a] if (n % a) < 62 else chr(n % a + 29))


def unpack(p, a, c, k):
    for i in range(c - 1, -1, -1):
        if i < len(k) and k[i]:
            token = enc(i, a)
            if token:
                p = re.sub(r"\b" + re.escape(token) + r"\b", lambda _m, v=k[i]: v, p)
    return p


def extract_call(html, start):
    """'eval(' konumundan baslayip parantez dengesine gore blogu cikarir."""
    i = start + 4
    depth = 0
    quote = None
    esc = False
    while i < len(html):
        ch = html[i]
        if quote:
            if esc:
                esc = False
            elif ch == "\\":
                esc = True
            elif ch == quote:
                quote = None
        else:
            if ch in "'\"":
                quote = ch
            elif ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
                if depth == 0:
                    return html[start + 5:i]
        i += 1
    return None


def main():
    html = open(sys.argv[1], encoding="utf-8", errors="replace").read()
    count = 0
    for m in re.finditer(r"eval\(function\(p,a,c,k,e,d\)", html):
        body = extract_call(html, m.start())
        if not body:
            continue
        # body: function(p,a,c,k,e,d){...}('packed',base,count,'dict'.split('|'),0,{})
        pm = re.search(r"\}\('(.*?)',(\d+),(\d+),'(.*?)'\.split\('\|'\)", body, re.S)
        if not pm:
            continue
        packed, base, cnt, dic = pm.group(1), int(pm.group(2)), int(pm.group(3)), pm.group(4)
        code = unpack(packed, base, cnt, dic.split("|"))
        count += 1
        print(f"===== unpack #{count} (base={base}, count={cnt}) =====")
        print(code)
        print()


main()
