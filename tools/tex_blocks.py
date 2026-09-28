"""Block textures."""
import math
import random

from pixel import Canvas, rgb, gray, mix, shade, lighten, ramp, value_noise

STONE = [rgb("5a5a5c"), rgb("69696b"), rgb("777779"), rgb("848486"), rgb("939395")]
DEEPSLATE = [rgb("2c2c31"), rgb("37373d"), rgb("414148"), rgb("4c4c53"), rgb("5a5a61")]
CRYSTAL = [rgb("0d3b4f"), rgb("1b6f8a"), rgb("2fb3cf"), rgb("6fe6ff"), rgb("c8fbff"), rgb("ffffff")]
CRYSTAL_DEEP = [rgb("1c1640"), rgb("3a2f86"), rgb("6a5ad0"), rgb("a79bff"), rgb("e2dcff"), rgb("ffffff")]
BRASS = [rgb("4a2c12"), rgb("7a4d1f"), rgb("a8702e"), rgb("c9903f"), rgb("e2b25c"), rgb("f7dc93")]
PATINA = [rgb("1e4d45"), rgb("2f7466"), rgb("4ea48d"), rgb("7fd0b4")]
IRON = [rgb("1f2025"), rgb("2e3038"), rgb("41444f"), rgb("5a5e6b"), rgb("7d8291")]
DIRT = [rgb("3f2a1b"), rgb("573b26"), rgb("6b4a31"), rgb("7f5b3d"), rgb("94704e")]
TRANSPARENT = (0, 0, 0, 0)


def rock(seed, palette, stretch_x=1.0, speckle=0.18):
    """A vanilla-like rock surface from layered noise with a little per-pixel grit."""
    c = Canvas(16, 16)
    n = value_noise(seed, 16, 16, scale=3.0, octaves=3)
    n2 = value_noise(seed + 7, 16, 16, scale=8.0, octaves=1)
    rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            sx = int(x / stretch_x) % 16
            v = n[y][sx] * 0.75 + n2[y][x] * 0.25
            if rnd.random() < speckle:
                v += rnd.choice((-0.22, 0.2))
            idx = max(0, min(len(palette) - 1, int(v * len(palette))))
            c.set(x, y, palette[idx])
    return c


def deepslate(seed):
    c = rock(seed, DEEPSLATE, stretch_x=1.0, speckle=0.12)
    rnd = random.Random(seed + 3)
    # horizontal cleavage lines typical of deepslate
    for _ in range(5):
        y = rnd.randrange(16)
        x0 = rnd.randrange(16)
        length = rnd.randint(4, 9)
        for i in range(length):
            x = (x0 + i) % 16
            c.set(x, y, shade(c.get(x, y), 0.72))
            if rnd.random() < 0.5:
                c.set(x, (y + 1) % 16, lighten(c.get(x, (y + 1) % 16), 0.08))
    return c


def shard(base, glow, cx, cy, length, direction, palette, rim):
    """Draws a two-pixel-wide crystal shard. The body goes on the glow layer; the socket rim on the base layer."""
    dx, dy = direction
    pts = []
    for i in range(length):
        pts.append((cx + dx * i, cy + dy * i))
    body = set()
    for i, (x, y) in enumerate(pts):
        body.add((x, y))
        if 0 < i < length - 1:
            body.add((x + 1, y) if dx != 0 else (x, y + 1))
    # socket rim around the crystal on the base texture
    for (x, y) in body:
        for ox in (-1, 0, 1):
            for oy in (-1, 0, 1):
                p = (x + ox, y + oy)
                if p not in body and 0 <= p[0] < 16 and 0 <= p[1] < 16:
                    base.set(p[0], p[1], mix(base.get(p[0], p[1]), rim, 0.6))
    for i, (x, y) in enumerate(pts):
        t = i / max(1, length - 1)
        core = ramp(palette[1:5], 0.35 + 0.6 * (1 - abs(t - 0.4)))
        glow.set(x, y, core)
        base.set(x, y, palette[0])
        if 0 < i < length - 1:
            side = (x + 1, y) if dx != 0 else (x, y + 1)
            glow.set(side[0], side[1], ramp(palette[1:4], 0.3 + 0.4 * t))
            base.set(side[0], side[1], palette[0])
    # bright tip and a highlight glint
    tx, ty = pts[-1]
    glow.set(tx, ty, palette[5])
    hx, hy = pts[max(0, length // 2 - 1)]
    glow.set(hx, hy, palette[4])


GEMS = {
    "big": ["..HL..", ".HLLM.", "HLLMMD", "LLMMDK", ".MMDK.", "..DK.."],
    "tall": [".H.", "HLM", "LMD", "LMD", "MDK", ".K."],
    "small": [".H.", "HLM", ".MK"],
    "wide": [".HLL.", "HLLMM", ".MDDK"],
    "tiny": ["HM", "MK"],
}


def stamp_gem(base, glow, x0, y0, sprite, palette, rim):
    rows = GEMS[sprite]
    cells = set()
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch != ".":
                cells.add((x0 + i, y0 + j))
    for (x, y) in cells:
        for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1)):
            p = (x + ox, y + oy)
            if p not in cells and 0 <= p[0] < 16 and 0 <= p[1] < 16:
                base.set(p[0], p[1], mix(base.get(p[0], p[1]), rim, 0.55))
    lookup = {"H": palette[5], "L": palette[4], "M": palette[3], "D": palette[2], "K": palette[1]}
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch == ".":
                continue
            glow.set(x0 + i, y0 + j, lookup[ch])
            base.set(x0 + i, y0 + j, palette[0])


def echo_deposit(seed, rock_fn, palette, rim, layout):
    base = rock_fn(seed)
    glow = Canvas(16, 16)
    for (x, y, sprite) in layout:
        stamp_gem(base, glow, x, y, sprite, palette, rim)
    return base, glow


def crystal_block(seed):
    base = Canvas(16, 16)
    glow = Canvas(16, 16)
    # Faceted crystal: large diamonds with bright edges.
    for y in range(16):
        for x in range(16):
            u = (x + y) % 8
            v = (x - y) % 8
            facet = ((x + y) // 8 + (x - y + 16) // 8) % 3
            t = [0.35, 0.55, 0.45][facet]
            t += 0.12 * (1 - abs(u - 4) / 4)
            c = ramp(CRYSTAL[1:5], t)
            base.set(x, y, shade(c, 0.8))
            edge = u == 0 or v == 0
            if edge:
                glow.set(x, y, CRYSTAL[4])
            elif (u + v) % 7 == 3:
                glow.set(x, y, mix(c, CRYSTAL[4], 0.4))
    for (x, y) in [(3, 3), (11, 4), (6, 11), (13, 13), (1, 9)]:
        glow.set(x, y, CRYSTAL[5])
    return base, glow


def echo_block():
    """The extracted echo: a stone reliquary holding a swirling core of memory."""
    base = Canvas(16, 16)
    glow = Canvas(16, 16)
    frame = rock(901, [rgb("2b2e38"), rgb("353946"), rgb("3f4453"), rgb("4a5061"), rgb("565d70")], speckle=0.1)
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            if edge < 2:
                c = frame.get(x, y)
                if edge == 0:
                    c = shade(c, 0.8)
                base.set(x, y, c)
            else:
                base.set(x, y, rgb("0b1a2a"))
    # corner rivets
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        base.set(x, y, rgb("9aa3b8"))
    # a spiral of memory inside
    cx, cy = 7.5, 7.5
    for i in range(260):
        a = i * 0.16
        r = 0.6 + i * 0.021
        x = int(round(cx + math.cos(a) * r))
        y = int(round(cy + math.sin(a) * r))
        if 2 <= x <= 13 and 2 <= y <= 13:
            t = 1 - i / 260
            glow.set(x, y, ramp([rgb("1b6f8a"), rgb("2fb3cf"), rgb("6fe6ff"), rgb("e8ffff")], t))
    glow.set(7, 7, rgb("ffffff"))
    glow.set(8, 8, rgb("ffffff"))
    glow.set(7, 8, rgb("c8fbff"))
    glow.set(8, 7, rgb("c8fbff"))
    # rune marks on the frame
    for (x, y) in [(5, 0), (6, 0), (10, 0), (0, 5), (0, 9), (15, 6), (15, 10), (6, 15), (9, 15), (10, 15)]:
        glow.set(x, y, rgb("6fe6ff"))
    return base, glow


def relic_cache(kind, stage):
    """Disturbed earth/stone, increasingly brushed away to reveal a glint of the relic beneath."""
    if kind == "soil":
        c = rock(500 + stage, DIRT, speckle=0.25)
        rnd = random.Random(77)
        for _ in range(10):
            x, y = rnd.randrange(16), rnd.randrange(16)
            c.set(x, y, gray(rnd.choice((110, 130, 95))))
        dust = rgb("a8845c")
    else:
        c = rock(600 + stage, STONE, speckle=0.2)
        dust = rgb("b3b0a8")
        rnd = random.Random(78)
        # cracks
        x, y = 3, 2
        for _ in range(12):
            c.set(x, y, shade(c.get(x, y), 0.55))
            x += rnd.choice((0, 1))
            y += 1
    # the disturbed patch in the middle, lighter and lumpy
    for y in range(3, 13):
        for x in range(3, 13):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 5.2 - stage * 0.3:
                c.set(x, y, mix(c.get(x, y), dust, 0.35 + 0.12 * stage))
    # the glint of a relic, revealed more with each stage
    glint = [rgb("5a4520"), rgb("c89b3c"), rgb("f2d27a"), rgb("fff4c4")]
    reveal = [[(7, 8)], [(7, 8), (8, 8), (8, 7)], [(6, 8), (7, 8), (8, 8), (8, 7), (9, 7)], [(6, 9), (6, 8), (7, 8), (8, 8), (8, 7), (9, 7), (9, 6), (7, 7)]][stage]
    for i, (x, y) in enumerate(reveal):
        c.set(x, y, glint[min(3, 1 + (i % 3))])
    if stage >= 2:
        c.set(9, 6, glint[3])
    return c


def lantern():
    c = Canvas(16, 16)
    frame = IRON
    # body sides (0,2)-(6,9)
    for y in range(2, 9):
        for x in range(0, 6):
            edge = x in (0, 5) or y in (2, 8)
            if edge:
                c.set(x, y, frame[2] if y != 8 else frame[1])
            else:
                t = 1 - abs(x - 2.5) / 3
                c.set(x, y, ramp([rgb("2fb3cf"), rgb("6fe6ff"), rgb("e8ffff")], t * (1 - abs(y - 5) / 5)))
    c.set(0, 2, frame[3])
    c.set(5, 2, frame[3])
    # top/bottom (0,9)-(6,15)
    for y in range(9, 15):
        for x in range(0, 6):
            edge = x in (0, 5) or y in (9, 14)
            c.set(x, y, frame[3] if edge else frame[1])
    # lid sides (1,0)-(5,2)
    for y in range(0, 2):
        for x in range(1, 5):
            c.set(x, y, frame[3] if y == 0 else frame[2])
    # lid top (1,10)-(5,14)
    for y in range(10, 14):
        for x in range(1, 5):
            c.set(x, y, frame[2] if (x + y) % 2 else frame[3])
    # chain / handle (11,1)-(14,12)
    for y in range(1, 12):
        for x in range(11, 14):
            if (y % 3 == 0 and x == 12) or (y % 3 != 0 and x in (11, 13)):
                c.set(x, y, frame[4] if x == 11 else frame[3])
    return c


def projector_textures():
    out = {}
    # Brass band with rivets
    band = Canvas(16, 16)
    n = value_noise(41, 16, 16, scale=2.0, octaves=2)
    for y in range(16):
        for x in range(16):
            t = 0.45 + 0.25 * n[y][x] + (0.15 if y % 4 == 1 else 0.0) - (0.2 if y % 4 == 3 else 0.0)
            band.set(x, y, ramp(BRASS[1:6], t))
    for y in (6, 7, 13, 14):
        for x in range(0, 16, 4):
            band.set(x + 1, y, BRASS[5] if y in (6, 13) else BRASS[1])
    for x in range(16):
        band.set(x, 15, BRASS[0])
        band.set(x, 0, BRASS[4])
    out["echo_projector_base"] = band

    # Pedestal: fluted brass with patina in the grooves
    side = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            groove = x % 4 == 0
            t = 0.55 + 0.25 * math.sin(x * 0.8) * 0.3 - y * 0.012
            c = ramp(BRASS[1:6], t)
            if groove:
                c = ramp(PATINA, 0.4 + 0.3 * ((y * 7) % 5) / 5)
            side.set(x, y, c)
    for x in range(16):
        side.set(x, 7, BRASS[5])
        side.set(x, 8, BRASS[1])
    out["echo_projector_side"] = side

    # Top plate: concentric engraving
    top = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            ring = int(d) % 3 == 0
            t = 0.6 - d * 0.03 + (0.15 if ring else 0.0)
            c = ramp(BRASS[1:6], t)
            if int(d) == 5:
                c = ramp(PATINA, 0.5)
            if d < 2.2:
                c = rgb("0b1a2a")
            top.set(x, y, c)
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        top.set(x, y, BRASS[5])
    out["echo_projector_top"] = top

    bottom = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            bottom.set(x, y, ramp(BRASS[0:3], 0.4 + 0.3 * n[y][x]))
    out["echo_projector_bottom"] = bottom

    strut = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            t = 0.35 + (0.35 if x % 2 == 0 else 0.0) - (0.15 if y % 5 == 0 else 0.0)
            strut.set(x, y, ramp(IRON[1:5], t))
    for x in range(2):
        for y in range(2):
            strut.set(x, y, BRASS[4])
    out["echo_projector_strut"] = strut

    for name, pal, bright in [("echo_projector_lens", CRYSTAL, 0.0), ("echo_projector_lens_active", CRYSTAL, 0.35)]:
        lens = Canvas(16, 16)
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - 7.5, y - 7.5)
                facet = ((x // 2) + (y // 2)) % 2
                t = 0.75 - d * 0.05 + facet * 0.1 + bright
                lens.set(x, y, ramp(pal[1:6], t))
        lens.set(6, 6, pal[5])
        lens.set(7, 6, pal[5])
        lens.set(0, 0, pal[4])
        out[name] = lens

    dial = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > 7.2:
                dial.set(x, y, BRASS[4])
            elif d > 6.2:
                dial.set(x, y, BRASS[2])
            else:
                dial.set(x, y, rgb("0b1a2a"))
    for i in range(12):
        a = i / 12 * math.tau
        dial.set(int(round(7.5 + math.cos(a) * 5)), int(round(7.5 + math.sin(a) * 5)), rgb("6fe6ff"))
    for i in range(5):
        dial.set(8 + i // 2, 7 - i, rgb("c8fbff"))
    out["echo_projector_dial"] = dial
    return out


STONE_LAYOUT = [(1, 1, "big"), (10, 2, "small"), (12, 7, "tall"), (5, 10, "wide"), (1, 11, "tiny"), (8, 7, "tiny")]
DEEP_LAYOUT = [(9, 1, "big"), (2, 3, "tall"), (1, 11, "wide"), (11, 10, "small"), (7, 13, "tiny"), (6, 7, "tiny")]


def generate(write):
    base, glow = echo_deposit(11, lambda s: rock(s, STONE), CRYSTAL, rgb("1f262b"), STONE_LAYOUT)
    write("block/echo_deposit", base)
    write("block/echo_deposit_glow", glow)
    base, glow = echo_deposit(12, deepslate, CRYSTAL_DEEP, rgb("121218"), DEEP_LAYOUT)
    write("block/deepslate_echo_deposit", base)
    write("block/deepslate_echo_deposit_glow", glow)
    base, glow = crystal_block(13)
    write("block/echo_crystal_block", base)
    write("block/echo_crystal_block_glow", glow)
    base, glow = echo_block()
    write("block/echo_block", base)
    write("block/echo_block_glow", glow)
    for kind in ("soil", "stone"):
        for stage in range(4):
            write(f"block/relic_cache_{kind}_{stage}", relic_cache(kind, stage))
    write("block/echo_lantern", lantern())
    for name, canvas in projector_textures().items():
        write(f"block/{name}", canvas)
