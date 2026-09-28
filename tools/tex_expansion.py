"""Textures for the expansion: new creatures, bosses, weapons, armor sets, keystones and books."""
import math
import random

from pixel import Canvas, rgb, gray, mix, shade, lighten, ramp, paint_box, value_noise
from tex_items import grid_sprite, sword, armor_icon, spawn_egg, GOLD, STEEL, WOOD, CYAN, VIOLET, BRONZE
from tex_entities import humanoid, base_value

SPECTRAL = {"o": rgb("14243a"), "K": rgb("2f4f6f"), "D": rgb("4f7fa8"), "M": rgb("8fbfe0"), "L": rgb("c8e8ff"), "H": rgb("ffffff")}
CINDER = {"o": rgb("140c0a"), "K": rgb("2e1d18"), "D": rgb("4a2e26"), "M": rgb("6e4a3e"), "L": rgb("96705f"), "H": rgb("ffb070")}
IRON = {"o": rgb("111317"), "K": rgb("2a2f38"), "D": rgb("444b58"), "M": rgb("6b7484"), "L": rgb("9aa4b4"), "H": rgb("d8e0ec")}
DAWN = {"o": rgb("3a2a10"), "K": rgb("7a5a20"), "D": rgb("b88a30"), "M": rgb("e8c060"), "L": rgb("fff0b0"), "H": rgb("ffffff")}
ROYAL = {"o": rgb("1c0f2e"), "K": rgb("3a1f5e"), "D": rgb("5b3590"), "M": rgb("8457c4"), "L": rgb("b894ec"), "H": rgb("efe2ff")}
EMBER = rgb("ff8a3a")


# ------------------------------------------------------------------ materials

def spectral_plate():
    rows = [
        "................",
        "...oooooooooo...",
        "..oHLLLLLLLMMo..",
        "..oLHLLLLLLMMo..",
        "..oLLMMMMMMMDo..",
        "..oLMoLLLLoMDo..",
        "..oLMLLHLLLMDo..",
        "..oLMLLLLLLMDo..",
        "...oLMLLLLMDo...",
        "...oLMMLLMMDo...",
        "....oLMMMMDo....",
        ".....oMMMDo.....",
        "......oDDo......",
        ".......oo.......",
    ]
    c = grid_sprite(rows, SPECTRAL)
    c.map(lambda x, y, col: (col[0], col[1], col[2], 215) if col[3] and col != SPECTRAL["o"] else col)
    return c


def revenant_ash():
    c = Canvas(16, 16)
    rnd = random.Random(21)
    for y in range(7, 14):
        for x in range(2, 14):
            d = abs(x - 7.5) / 6.0 + (13 - y) / 7.0
            if d < 1.0 and rnd.random() < 0.95 - d * 0.35:
                c.set(x, y, ramp([gray(80), gray(120), gray(165)], 1 - d + rnd.uniform(-0.2, 0.2)))
    for _ in range(7):
        x, y = rnd.randrange(4, 12), rnd.randrange(8, 13)
        c.set(x, y, EMBER if rnd.random() < 0.6 else rgb("ffd070"))
    c.outline(rgb("141414"))
    return c


def ingot(pal, accent=None):
    rows = [
        "................",
        "................",
        "................",
        "................",
        ".....oooooooo...",
        "....oHLLLLLLMo..",
        "...oHLLLLLLMMDo.",
        "..oLLLLLLLMMDDo.",
        ".oMMMMMMMMMDDKo.",
        ".oDDDDDDDDDDKo..",
        ".oKKKKKKKKKKo...",
        "..oooooooooo....",
    ]
    c = grid_sprite(rows, pal)
    if accent:
        for (x, y) in [(4, 11), (5, 11), (8, 12), (9, 12), (10, 12), (6, 13)]:
            c.set(x, y, accent)
    return c


def colossus_plating():
    c = Canvas(16, 16)
    for y in range(2, 14):
        for x in range(2, 14):
            light = ((14 - x) + (14 - y)) / 24
            c.set(x, y, ramp([IRON["D"], IRON["M"], IRON["L"]], 0.3 + light * 0.6))
    for x in range(2, 14):
        c.set(x, 2, IRON["L"])
        c.set(x, 13, IRON["K"])
    for y in range(2, 14):
        c.set(2, y, IRON["L"])
        c.set(13, y, IRON["K"])
    for (x, y) in [(4, 4), (11, 4), (4, 11), (11, 11)]:
        c.set(x, y, IRON["H"])
        c.set(x + 1, y + 1, IRON["K"])
    for (x, y) in [(6, 7), (7, 8), (8, 8), (9, 9)]:
        c.set(x, y, IRON["K"])  # a dent
    c.outline(IRON["o"])
    return c


def colossus_core():
    c = Canvas(16, 16)
    for y in range(2, 14):
        for x in range(2, 14):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d > 5:
                c.set(x, y, ramp([IRON["K"], IRON["M"]], 0.5 + (7.5 - y) / 12))
            else:
                glow = 1 - math.hypot(x - 7.5, y - 7.5) / 6
                col = ramp([rgb("7a1a08"), rgb("ff6a2a"), rgb("ffd070"), rgb("ffffe0")], glow)
                if x in (5, 8, 11) and d > 1:
                    col = IRON["D"]  # grate bars
                c.set(x, y, col)
    c.outline(IRON["o"])
    return c


def dawnstone():
    rows = [
        "................",
        ".......oo.......",
        "......oHHo......",
        ".....oHLLMo.....",
        "....oHLLLMMo....",
        "...oHLLLLMMDo...",
        "..oHLLLHLMMMDo..",
        "..oLLLHHHLMMDo..",
        "..oLLLLHLMMDDo..",
        "...oLLLLMMDDo...",
        "....oLLMMDDo....",
        ".....oMMDDo.....",
        "......oDDo......",
        ".......oo.......",
    ]
    return grid_sprite(rows, DAWN)


def royal_sigil():
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 6.5:
                if d > 5.2:
                    c.set(x, y, ramp([GOLD["D"], GOLD["M"], GOLD["L"]], 0.5 + ((7.5 - x) + (7.5 - y)) / 14))
                else:
                    c.set(x, y, ramp([ROYAL["K"], ROYAL["D"], ROYAL["M"]], 0.6 - d / 10 + ((7.5 - x) + (7.5 - y)) / 20))
    crown = [".H.H.H.", ".LHLHL.", ".LLLLL."]
    for j, row in enumerate(crown):
        for i, ch in enumerate(row):
            if ch != ".":
                c.set(4 + i, 6 + j, GOLD["H"] if ch == "H" else GOLD["L"])
    c.set(7, 10, GOLD["L"])
    c.outline(ROYAL["o"])
    return c


# ------------------------------------------------------------------ weapons

def ashen_cleaver():
    rows = [
        ".........oooooo.",
        "........oHLLLLMo",
        ".......oHLLLLMMo",
        "......oHLLLLMMDo",
        ".....oHLLLLMMDo.",
        "....oLLLLLMMDo..",
        "....oLMMMMMDo...",
        "....ooMMMDDo....",
        "...oGo.oDDo.....",
        "..oGGo..oo......",
        ".oGGo...........",
        "oGGo............",
        "oGo.............",
        ".o..............",
    ]
    c = grid_sprite(rows, CINDER, {"G": WOOD["D"]})
    # glowing edge that never cooled
    for (x, y) in [(9, 1), (10, 1), (11, 1), (12, 1), (13, 1), (14, 2), (14, 3), (13, 4), (12, 5)]:
        c.set(x, y + 1, EMBER)
    return c


def crownbreaker():
    c = sword(GOLD, ROYAL, ROYAL, GOLD, length=11, width=3, glow=rgb("efe2ff"))
    return c


def siegebreaker():
    rows = [
        ".....oooooo.....",
        "....oHLLLLMo....",
        "...oHLLLLLMDo...",
        "...oLLLLLLMDo...",
        "...oLMRRRMMDo...",
        "...oLLLLLLMDo...",
        "...oMMMMMMDDo...",
        "....oDDDDDDo....",
        ".......oGo......",
        ".......oGo......",
        ".......oGo......",
        ".......oBo......",
        ".......oGo......",
        ".......oGo......",
        ".......oBo......",
        "........o.......",
    ]
    c = grid_sprite(rows, IRON, {"G": WOOD["M"], "B": IRON["L"], "R": EMBER})
    return c


def dawn_staff():
    c = Canvas(16, 16)
    for i in range(11):
        x, y = 2 + i, 14 - i
        c.set(x, y, WOOD["L"] if i % 3 else GOLD["M"])
        c.set(x + 1, y, WOOD["D"])
    # the sun at its head
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 12.0, y - 3.5)
            if d <= 2.6:
                c.set(x, y, ramp([DAWN["M"], DAWN["L"], DAWN["H"]], 1 - d / 2.6))
    for (x, y) in [(12, 0), (15, 3), (9, 4), (12, 7), (14, 1), (10, 1), (14, 6)]:
        if 0 <= x < 16:
            c.set(x, y, DAWN["L"])
    c.outline(DAWN["o"])
    return c


def hollow_crown():
    rows = [
        ".o...o...o...o..",
        "oHo.oHo.oHo.oHo.",
        "oLo.oLo.oLo.oLo.",
        "oLLooLLoLLoLLMo.",
        "oLGLLLLPLLLLGMo.",
        "oLLLLLLLLLLLLMo.",
        "oMMMMMMMMMMMMDo.",
        "oDPDDDDPDDDDPDo.",
        ".oooooooooooooo.",
    ]
    c = grid_sprite(rows, GOLD, {"P": ROYAL["M"], "G": rgb("efe2ff")})
    return c


# ------------------------------------------------------------------ armor icons

def armor_set_icon(piece, pal, trim=None, gem=None):
    c = armor_icon(piece, pal, scales=False)
    if trim:
        def fn(x, y, col):
            if col == pal["D"] and (x + y) % 5 == 0:
                return trim
            return col
        c.map(fn)
    if gem:
        spots = {"helmet": (7, 5), "chestplate": (7, 7), "leggings": (7, 4), "boots": (5, 9)}
        x, y = spots[piece]
        c.set(x, y, gem)
    return c


# ------------------------------------------------------------------ keystones & books

def keystone(pal, rune):
    c = Canvas(16, 16)
    stone = [gray(60), gray(92), gray(128), gray(165)]
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) + abs(y - 7.5) * 0.85
            if d <= 7.2:
                light = ((7.5 - x) + (7.5 - y)) / 16
                c.set(x, y, ramp(stone, 0.45 + light))
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) + abs(y - 7.5)
            if d <= 3.2:
                c.set(x, y, ramp([pal["D"], pal["M"], pal["L"], pal["H"]], 1 - d / 3.2))
    for (x, y) in rune:
        c.set(x, y, pal["L"])
    c.outline(rgb("16161c"))
    return c


def book(cover, spine, emblem, emblem2):
    c = Canvas(16, 16)
    for y in range(2, 15):
        for x in range(3, 14):
            if x <= 4:
                col = spine
            else:
                col = ramp([shade(cover, 0.7), cover, lighten(cover, 0.15)], 0.5 + (13 - x) / 20 + (14 - y) / 30)
            c.set(x, y, col)
    for y in range(3, 14):
        c.set(13, y, gray(235) if y % 2 else gray(210))  # pages
    for (x, y) in [(8, 6), (9, 6), (8, 7), (9, 7), (7, 8), (10, 8), (8, 9), (9, 9), (8, 10), (9, 10)]:
        c.set(x, y, emblem)
    c.set(8, 5, emblem2)
    c.set(9, 5, emblem2)
    c.set(8, 11, emblem2)
    c.set(9, 11, emblem2)
    for y in (4, 12):
        for x in range(5, 13):
            c.set(x, y, emblem2)
    c.outline(shade(cover, 0.3))
    return c


# ------------------------------------------------------------------ entity skins

def colorize(canvas, palette, regions=()):
    """Maps a grey hologram skin onto a colour ramp; regions (x0, y0, x1, y1, ramp) override it."""
    def fn(x, y, col):
        if col[3] == 0:
            return col
        v = col[0] / 255.0
        pal = palette
        for (x0, y0, x1, y1, p) in regions:
            if x0 <= x < x1 and y0 <= y < y1:
                pal = p
                break
        if v > 0.97:
            return col  # glowing eyes and highlights stay white
        c = ramp(pal, v)
        return (c[0], c[1], c[2], col[3])
    canvas.map(fn)
    return canvas


def hollow_king_skin():
    c = humanoid("noble", eyes_glow=True, tattered=True)
    purple = [rgb("1c0f2e"), rgb("3a1f5e"), rgb("6a44a8"), rgb("a88ae0"), rgb("f0e6ff")]
    gold = [rgb("3d2508"), rgb("9a6418"), rgb("d19a2a"), rgb("f2cc5a"), rgb("fff2b8")]
    steel = [rgb("1f2229"), rgb("5e6573"), rgb("9aa2b1"), rgb("cfd5df"), rgb("ffffff")]
    colorize(c, purple, regions=[(0, 64, 48, 72, gold), (64, 0, 100, 26, steel)])
    # a pale, hollow face beneath the crown
    for x in range(1, 7):
        for y in range(9, 14):
            if c.get(x + 8, y)[3] and not (y == 12 and x in (1, 2, 5, 6)):
                c.set(x + 8, y, mix(c.get(x + 8, y), rgb("d8d0e8"), 0.5))
    return c


def hierophant_skin():
    c = humanoid("mage", eyes_glow=True)
    cream = [rgb("4a3a18"), rgb("a08040"), rgb("e0c88a"), rgb("fff0c8"), rgb("ffffff")]
    gold = [rgb("3d2508"), rgb("9a6418"), rgb("d19a2a"), rgb("f2cc5a"), rgb("fff2b8")]
    colorize(c, cream, regions=[(0, 64, 48, 72, gold), (100, 0, 116, 30, gold)])
    for x in range(20, 28):
        c.set(x, 20, rgb("ffd070"))
        c.set(x, 31, rgb("ffd070"))
    return c


def ash_revenant_skin():
    c = humanoid("miner", eyes_glow=True, tattered=True)
    ash = [rgb("141010"), rgb("3a2a24"), rgb("6a5048"), rgb("a0867a"), rgb("e0d0c0")]
    colorize(c, ash)
    rnd = random.Random(5)
    # glowing cracks of the forge-fire within
    for _ in range(40):
        x, y = rnd.randrange(0, 56), rnd.randrange(8, 64)
        if c.get(x, y)[3] and y < 64:
            c.set(x, y, EMBER if rnd.random() < 0.7 else rgb("ffe080"))
    return c


def colossus_texture():
    c = Canvas(128, 128)
    n = value_noise(41, 128, 128, scale=6, octaves=3)
    stone = [rgb("2a2c30"), rgb("4a4d54"), rgb("6c707a"), rgb("949aa4"), rgb("c4c8d0")]
    iron = [rgb("1a1c22"), rgb("3a3f4a"), rgb("5e6674"), rgb("8a94a4"), rgb("c8d0dc")]

    def plated(material, bands=4):
        def painter(face, lx, ly, fw, fh):
            t = 0.45 * base_value(face) + n[(ly * 7 + lx) % 128][(lx * 3 + ly) % 128] * 0.25
            if fh > 4 and ly % max(3, fh // bands) == 0:
                t -= 0.15  # seams between plates
            if (lx % 5 == 1 and ly % max(3, fh // bands) == 1):
                t += 0.3  # rivets
            return ramp(material, t)
        return painter

    def torso(face, lx, ly, fw, fh):
        col = plated(iron, 5)(face, lx, ly, fw, fh)
        if face == "back" and 2 <= ly <= 6:
            col = ramp(stone, 0.4)
        return col

    def grate(face, lx, ly, fw, fh):
        if face in ("front",):
            if lx % 3 == 0 or ly in (0, fh - 1):
                return iron[1]
            glow = 1 - abs(ly - fh / 2) / (fh / 2)
            return ramp([rgb("8a2008"), rgb("ff6a2a"), rgb("ffd070"), rgb("fff6d0")], 0.4 + glow * 0.6)
        return iron[1]

    def head(face, lx, ly, fw, fh):
        col = plated(iron, 3)(face, lx, ly, fw, fh)
        if face == "front":
            if ly == 4 and lx in (2, 3, 6, 7):
                return rgb("ffb070") if lx in (3, 6) else rgb("ff6a2a")
            if ly == 4:
                return iron[0]
            if ly == 7 and 2 <= lx <= 7:
                return iron[0]
        return col

    def tower(face, lx, ly, fw, fh):
        t = 0.45 * base_value(face) + n[ly % 128][(lx * 5) % 128] * 0.3
        brick = (ly % 3 == 0) or ((lx + (ly // 3) * 2) % 4 == 0)
        col = ramp(stone, t - (0.18 if brick else 0))
        if face in ("front", "back") and 3 <= ly <= 5 and lx in (3, 4, 11, 12):
            return rgb("1a1210")  # arrow slits
        return col

    paint_box(c, 0, 0, 24, 18, 14, torso)
    paint_box(c, 0, 32, 20, 6, 12, plated(iron, 2))
    paint_box(c, 76, 0, 10, 8, 1, grate)
    paint_box(c, 64, 32, 10, 9, 10, head)
    paint_box(c, 0, 50, 16, 10, 8, tower)
    paint_box(c, 48, 50, 4, 3, 8, tower)
    paint_box(c, 0, 68, 10, 24, 10, plated(stone, 5))
    paint_box(c, 40, 68, 12, 8, 12, plated(iron, 2))
    paint_box(c, 88, 68, 8, 18, 8, plated(stone, 4))
    return c


def wisp_texture():
    c = Canvas(64, 64)
    paint_box(c, 0, 0, 4, 4, 4, lambda f, x, y, w, h: rgb("ffffff") if (x + y) % 3 else rgb("fff6d0"))
    paint_box(c, 16, 0, 6, 6, 6, lambda f, x, y, w, h: (255, 231, 160, 110 if (x + y) % 2 else 70))

    def ring(size):
        def painter(face, lx, ly, fw, fh):
            if face not in ("top", "bottom"):
                return None
            d = max(abs(lx - (fw - 1) / 2), abs(ly - (fh - 1) / 2))
            if d >= (fw - 1) / 2 - 0.1:
                return rgb("fff0b0") if (lx + ly) % 2 else rgb("e8c060")
            if d >= (fw - 1) / 2 - 1.1:
                return (255, 231, 160, 90)
            return None
        return painter
    paint_box(c, 0, 12, 10, 0, 10, ring(10))
    paint_box(c, 0, 22, 12, 0, 12, ring(12))
    return c


def crawler_texture():
    c = Canvas(64, 32)
    shell = [rgb("1a1c24"), rgb("2e3240"), rgb("454a5c"), rgb("646a80"), rgb("8a90a6")]

    def carapace(face, lx, ly, fw, fh):
        t = 0.45 * base_value(face)
        if face == "top":
            t += 0.12 if lx in (1, fw - 2) else 0
            if lx == fw // 2 or lx == fw // 2 - 1:
                t -= 0.12  # the seam between wing cases
            if (lx * 7 + ly * 3) % 11 == 0:
                return rgb("6fe6ff")  # crystal flecks
        return ramp(shell, t)

    def head(face, lx, ly, fw, fh):
        if face == "front" and ly == 1 and lx in (1, fw - 2):
            return rgb("9ff0ff")
        return ramp(shell, 0.35 * base_value(face))

    crystal = lambda f, x, y, w, h: ramp([CYAN["D"], CYAN["M"], CYAN["L"], CYAN["H"]], 0.4 + (1 - y / max(1, h)) * 0.5 + (0.1 if f == "front" else 0))
    paint_box(c, 0, 0, 8, 4, 12, carapace)
    paint_box(c, 40, 0, 6, 4, 4, head)
    paint_box(c, 40, 8, 2, 4, 2, crystal)
    paint_box(c, 48, 8, 2, 3, 2, crystal)
    paint_box(c, 56, 8, 2, 6, 2, crystal)
    paint_box(c, 0, 16, 1, 6, 1, lambda f, x, y, w, h: ramp(shell, 0.3 + (0.2 if y == 0 else 0)))
    paint_box(c, 0, 24, 1, 1, 3, lambda f, x, y, w, h: ramp(shell, 0.6))
    return c


# ------------------------------------------------------------------ armor layers

def plate_painter(pal, bands=3, rivets=True, trim=None):
    ramp_colors = [pal["K"], pal["D"], pal["M"], pal["L"], pal["H"]]

    def painter(face, lx, ly, fw, fh):
        t = 0.5 * base_value(face)
        if fh >= 6 and ly % max(3, fh // bands) == 0:
            t -= 0.15
        if rivets and lx % 4 == 1 and ly % max(3, fh // bands) == 1:
            t += 0.3
        if trim and (ly == 0 or ly == fh - 1) and face != "top":
            return trim
        return ramp(ramp_colors, t + 0.1)
    return painter


def robe_painter(pal, trim):
    ramp_colors = [pal["K"], pal["D"], pal["M"], pal["L"], pal["H"]]

    def painter(face, lx, ly, fw, fh):
        t = 0.55 * base_value(face) + (0.05 if (lx + ly) % 4 == 0 else 0)
        if face == "front" and lx in (fw // 2 - 1, fw // 2):
            return trim
        if ly == fh - 1:
            return trim
        return ramp(ramp_colors, t)
    return painter


def armor_layer(painter, helmet_open=True, visor=None):
    body = Canvas(64, 32)

    def helmet(face, lx, ly, fw, fh):
        if face == "bottom":
            return None
        if helmet_open and face == "front" and 2 <= ly <= 5 and 1 <= lx <= 6:
            if visor and ly == 3:
                return visor
            return None
        return painter(face, lx, ly, fw, fh)
    paint_box(body, 0, 0, 8, 8, 8, helmet)
    paint_box(body, 16, 16, 8, 12, 4, painter)
    paint_box(body, 40, 16, 4, 12, 4, painter)
    paint_box(body, 0, 16, 4, 12, 4, lambda f, x, y, w, h: painter(f, x, y, w, h) if y >= 7 else None)

    legs = Canvas(64, 32)
    paint_box(legs, 16, 16, 8, 12, 4, lambda f, x, y, w, h: painter(f, x, y, w, h) if y >= 7 else None)
    paint_box(legs, 0, 16, 4, 12, 4, lambda f, x, y, w, h: painter(f, x, y, w, h) if y <= 9 else None)
    return body, legs


def hollow_crown_layer():
    crown = Canvas(64, 32)

    def painter(face, lx, ly, fw, fh):
        if face in ("top", "bottom"):
            return None
        if ly == 0 and lx % 2 == 1:
            return None
        if ly > 2:
            return None
        if face == "front" and ly == 2 and lx in (1, 3, 4, 6):
            return ROYAL["L"] if lx in (3, 4) else ROYAL["M"]
        return ramp([GOLD["D"], GOLD["M"], GOLD["L"], GOLD["H"]], 0.75 if ly < 2 else 0.5)
    paint_box(crown, 0, 0, 8, 8, 8, painter)
    return crown


# ------------------------------------------------------------------ generate

KEY_RUNES = {
    "crowns": [(6, 2), (7, 1), (8, 2), (9, 1)],
    "iron": [(3, 7), (3, 8), (12, 7), (12, 8)],
    "dragons": [(5, 12), (7, 13), (9, 12), (11, 13)],
    "dawn": [(7, 1), (8, 1), (7, 14), (8, 14), (1, 7), (14, 8)],
}


def generate(write, write_raw):
    # materials
    write("item/spectral_plate", spectral_plate())
    write("item/revenant_ash", revenant_ash())
    write("item/cindersteel_ingot", ingot(CINDER, EMBER))
    write("item/colossus_plating", colossus_plating())
    write("item/colossus_core", colossus_core())
    write("item/dawnstone", dawnstone())
    write("item/royal_sigil", royal_sigil())
    # weapons
    write("item/spectral_longsword", sword(SPECTRAL, STEEL, SPECTRAL, SPECTRAL, length=11, width=2, glow=rgb("c8e8ff"), ghost=(200, 232, 255, 110)))
    write("item/ashen_cleaver", ashen_cleaver())
    write("item/crownbreaker", crownbreaker())
    write("item/siegebreaker", siegebreaker())
    write("item/dawn_staff", dawn_staff())
    write("item/hollow_crown", hollow_crown())
    # armor icons
    sets = {
        "spectral_knight": (SPECTRAL, rgb("ffffff"), rgb("6fe6ff")),
        "cindersteel": (CINDER, EMBER, rgb("ffd070")),
        "colossus": (IRON, IRON["H"], EMBER),
        "dawnweave": (DAWN, rgb("ffffff"), rgb("6fe6ff")),
    }
    names = {"dawnweave": {"helmet": "hood", "chestplate": "robe", "leggings": "leggings", "boots": "slippers"}}
    for set_name, (pal, trim, gem) in sets.items():
        for piece in ("helmet", "chestplate", "leggings", "boots"):
            item_name = names.get(set_name, {}).get(piece, piece)
            write(f"item/{set_name}_{item_name}", armor_set_icon(piece, pal, trim=trim, gem=gem))
    # keystones
    write("item/keystone_of_crowns", keystone(ROYAL, KEY_RUNES["crowns"]))
    write("item/keystone_of_iron", keystone({"D": rgb("7a1a08"), "M": rgb("ff6a2a"), "L": rgb("ffd070"), "H": rgb("fff6d0")}, KEY_RUNES["iron"]))
    write("item/keystone_of_dragons", keystone(VIOLET, KEY_RUNES["dragons"]))
    write("item/keystone_of_dawn", keystone(DAWN, KEY_RUNES["dawn"]))
    # books
    write("item/guide_book", book(rgb("6b4420"), rgb("4a2e15"), CYAN["L"], GOLD["M"]))
    write("item/showcase_book", book(rgb("3a1f66"), rgb("24123e"), rgb("fff0b0"), GOLD["L"]))
    # spawn eggs
    eggs = {
        "echo_knight": (rgb("b8d4ff"), rgb("3a4a70")),
        "spectral_archer": (rgb("a8f4e8"), rgb("2a5a50")),
        "ash_revenant": (rgb("5a4038"), rgb("ff8a3a")),
        "dawn_wisp": (rgb("fff0c8"), rgb("e8a830")),
        "shard_crawler": (rgb("3a3e4e"), rgb("6fe6ff")),
        "hollow_king": (rgb("5b3590"), rgb("f2cc5a")),
        "siege_colossus": (rgb("6b7484"), rgb("ff6a2a")),
        "hierophant": (rgb("fff0c8"), rgb("8457c4")),
    }
    for i, (name, (base, spots)) in enumerate(eggs.items()):
        write(f"item/{name}_spawn_egg", spawn_egg(base, spots, 40 + i))

    # creatures
    write("entity/echo_knight", humanoid("knight", eyes_glow=True))
    write("entity/spectral_archer", humanoid("archer", eyes_glow=True, tattered=True))
    write("entity/ash_revenant", ash_revenant_skin())
    write("entity/hollow_king", hollow_king_skin())
    write("entity/hierophant", hierophant_skin())
    write("entity/siege_colossus", colossus_texture())
    write("entity/dawn_wisp", wisp_texture())
    write("entity/shard_crawler", crawler_texture())

    # armor layers
    layers = {
        "spectral_knight": armor_layer(plate_painter(SPECTRAL, trim=rgb("ffffff")), visor=rgb("6fe6ff")),
        "cindersteel": armor_layer(plate_painter(CINDER, trim=EMBER)),
        "colossus": armor_layer(plate_painter(IRON, bands=4), helmet_open=True, visor=EMBER),
        "dawnweave": armor_layer(robe_painter(DAWN, GOLD["L"])),
    }
    for name, (body, legs) in layers.items():
        write(f"entity/equipment/humanoid/{name}", body)
        write(f"entity/equipment/humanoid_leggings/{name}", legs)
    write("entity/equipment/humanoid/hollow_crown", hollow_crown_layer())
