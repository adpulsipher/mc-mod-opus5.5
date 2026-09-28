"""Item textures (16x16)."""
import math
import random

from pixel import Canvas, rgb, gray, mix, shade, lighten, ramp

CYAN = {"o": rgb("0b2a3a"), "K": rgb("104a63"), "D": rgb("1b6f8a"), "M": rgb("2fb3cf"), "L": rgb("6fe6ff"), "H": rgb("e8ffff")}
VIOLET = {"o": rgb("24123e"), "K": rgb("3a1f66"), "D": rgb("5b35a0"), "M": rgb("8a63d6"), "L": rgb("c3a8ff"), "H": rgb("f3ebff")}
GOLD = {"o": rgb("3d2508"), "K": rgb("6b420f"), "D": rgb("9a6418"), "M": rgb("d19a2a"), "L": rgb("f2cc5a"), "H": rgb("fff2b8")}
BRONZE = {"o": rgb("2e1a0c"), "K": rgb("5a3515"), "D": rgb("80501f"), "M": rgb("a86e2c"), "L": rgb("d49a4e"), "H": rgb("f3cf8c")}
STEEL = {"o": rgb("1f2229"), "K": rgb("3b404b"), "D": rgb("5e6573"), "M": rgb("9aa2b1"), "L": rgb("cfd5df"), "H": rgb("ffffff")}
WOOD = {"o": rgb("2a1a0d"), "K": rgb("4a2e15"), "D": rgb("6b4420"), "M": rgb("8c5d2e"), "L": rgb("b07e45"), "H": rgb("d6a86a")}


def grid_sprite(rows, palette, extra=None):
    pal = dict(palette)
    if extra:
        pal.update(extra)
    c = Canvas(16, 16)
    offset = (16 - len(rows)) // 2
    c.grid(rows, pal, 0, offset)
    return c


# ------------------------------------------------------------------ crystals & materials

def echo_shard():
    rows = [
        "................",
        "..........oo....",
        ".........oHLo...",
        "........oHLLMo..",
        ".......oHLLMMo..",
        "......oHLLMMDo..",
        ".....oHLLMMDo...",
        "....oLLLMMDo..o.",
        "...oLLMMMDo..oLo",
        "...oLMMMDo..oLMo",
        "..oLMMDDo..oLMDo",
        "..oMMDDo...oMDKo",
        "..oMDKo.....oKo.",
        "...ooo.......o..",
        "................",
        "................",
    ]
    return grid_sprite(rows, CYAN)


def echo_dust():
    c = Canvas(16, 16)
    rnd = random.Random(8)
    # a soft mound of glittering dust
    for y in range(7, 14):
        for x in range(2, 14):
            d = abs(x - 7.5) / 6.0 + (13 - y) / 7.0
            if d < 1.0 and rnd.random() < 0.95 - d * 0.35:
                t = 1 - d
                c.set(x, y, ramp([CYAN["D"], CYAN["M"], CYAN["L"]], t + rnd.uniform(-0.2, 0.2)))
    for _ in range(9):
        x, y = rnd.randrange(3, 13), rnd.randrange(3, 13)
        c.set(x, y, CYAN["H"] if rnd.random() < 0.5 else CYAN["L"])
    c.outline(CYAN["o"])
    return c


def ancient_coin():
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 6.4:
                light = ((7.5 - x) + (7.5 - y)) / 14.0
                if d > 5.2:
                    col = ramp([GOLD["K"], GOLD["D"], GOLD["M"]], 0.5 + light)
                else:
                    col = ramp([GOLD["D"], GOLD["M"], GOLD["L"]], 0.55 + light * 0.8)
                c.set(x, y, col)
    # an embossed crown on the face
    crown = [
        ".H..H..H.",
        ".LH.L.HL.",
        ".LLLLLLL.",
        ".DDDDDDD.",
    ]
    for j, row in enumerate(crown):
        for i, ch in enumerate(row):
            if ch != ".":
                c.set(4 + i, 5 + j, {"H": GOLD["H"], "L": GOLD["L"], "D": GOLD["K"]}[ch])
    for (x, y) in [(5, 10), (7, 10), (9, 10)]:
        c.set(x, y, GOLD["K"])
    # verdigris
    for (x, y) in [(3, 10), (4, 11), (11, 4), (12, 9)]:
        c.set(x, y, rgb("5f9a78"))
    c.set(4, 3, GOLD["H"])
    c.set(3, 4, GOLD["H"])
    c.outline(GOLD["o"])
    return c


def wyrmscale():
    rows = [
        ".......oo.......",
        "......oHLo......",
        ".....oHLLMo.....",
        "....oHLLMLMo....",
        "...oHLLMLMMMo...",
        "...oLLMLMMMDo...",
        "..oLLMLMMMDMDo..",
        "..oLMLMMMDMDDo..",
        "..oLLMMMDMDDKo..",
        "..oLMMMDMDDDKo..",
        "...oMMDMDDDKo...",
        "....oMDDDDKo....",
        ".....oDDDKo.....",
        "......oooo......",
    ]
    c = grid_sprite(rows, VIOLET)
    # iridescent sheen along the ridge
    for (x, y) in [(7, 3), (7, 4), (8, 5), (8, 6), (9, 7)]:
        c.set(x, y, rgb("9ff0ff"))
    return c


def wyrm_heart():
    rows = [
        "................",
        "...ooo....ooo...",
        "..oHLLo..oLMMo..",
        ".oHLLLLooLMMMDo.",
        ".oLHLLLLLMMMMDo.",
        ".oLLLLLLMMMMDDo.",
        ".oLLLLLMMMMMDDo.",
        "..oLLLMMMMMDDo..",
        "...oLLMMMMDDo...",
        "....oLMMMDDo....",
        ".....oMMDDo.....",
        "......oMDo......",
        ".......oo.......",
    ]
    heart = {"o": rgb("2a0a2e"), "K": rgb("5a1060"), "D": rgb("8a1f8f"), "M": rgb("c64bc8"), "L": rgb("f08cf0"), "H": rgb("ffe0ff")}
    c = grid_sprite(rows, heart)
    # glowing veins of memory
    for (x, y) in [(5, 5), (6, 6), (7, 7), (8, 7), (9, 6), (10, 5), (7, 8), (7, 9)]:
        c.set(x, y + 1, rgb("9ff0ff"))
    return c


# ------------------------------------------------------------------ relics

def heirloom_locket():
    rows = [
        "......oooo......",
        ".....oL..Lo.....",
        ".....oM..Mo.....",
        "......oLLo......",
        ".......oo.......",
        "......oHLo......",
        ".....oHLLMo.....",
        "....oHLLLMMo....",
        "....oLLRRMMo....",
        "....oLRRRRMo....",
        "....oLLRRMDo....",
        ".....oLMMDo.....",
        "......oDDo......",
        ".......oo.......",
    ]
    return grid_sprite(rows, GOLD, {"R": rgb("b8324a")})


def festival_charm():
    c = Canvas(16, 16)
    # wooden sun disc
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 5.5)
            if d <= 4.3:
                c.set(x, y, ramp([WOOD["D"], WOOD["M"], WOOD["L"]], 0.6 - (y - 5.5) / 8 + (7.5 - x) / 12))
    for i in range(8):
        a = i / 8 * math.tau
        c.set(int(round(7.5 + math.cos(a) * 5.3)), int(round(5.5 + math.sin(a) * 5.3)), WOOD["L"])
    # painted star
    for (x, y) in [(7, 3), (8, 3), (6, 5), (7, 5), (8, 5), (9, 5), (7, 4), (8, 4), (7, 6), (8, 6), (6, 7), (9, 7)]:
        c.set(x, y, rgb("f2c14e"))
    # ribbons
    ribbons = [(rgb("d8434e"), 6, -1), (rgb("3f7fd6"), 9, 1)]
    for color, x0, drift in ribbons:
        for j in range(6):
            x = x0 + (drift if j in (2, 3) else 0) + (drift * 2 if j >= 4 else 0) // 2
            c.set(x, 10 + j, shade(color, 1.0 - j * 0.05))
            if j % 2 == 0:
                c.set(x + drift, 10 + j, shade(color, 0.8))
    c.outline(WOOD["o"])
    return c


def tarnished_crown():
    rows = [
        "..o....o....o...",
        ".oHo..oHo..oHo..",
        ".oLo..oLo..oLo..",
        ".oLLo.oLo.oLMo..",
        ".oLLLoLCLoLLMo..",
        ".oLLLLLLLLLMMo..",
        ".oLRLLLBLLLRMo..",
        ".oMMMMMMMMMMDo..",
        ".oDTDDDDTDDDDo..",
        "..oooooooooooo..",
    ]
    c = grid_sprite(rows, GOLD, {"R": rgb("c02a3c"), "B": rgb("2f7fd0"), "C": rgb("6fe6ff"), "T": rgb("6c9a70")})
    return c


def music_disc():
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 7.2:
                if d <= 1.0:
                    continue
                if d <= 3.0:
                    col = ramp([CYAN["D"], CYAN["L"]], (3.0 - d) / 2.0)
                else:
                    groove = int(d * 2) % 2 == 0
                    col = rgb("2a2c36") if groove else rgb("1d1f27")
                    if (x - y) in (-3, -2) and d > 3.5:
                        col = rgb("4a4e5e")
                c.set(x, y, col)
    c.outline(rgb("0e0f14"))
    return c


def spawn_egg(base, spots, seed):
    c = Canvas(16, 16)
    rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            # egg: wider at the bottom
            cy = 8.5
            ry = 6.5
            rx = 4.6 + (y - 3) * 0.12
            if ((x - 7.5) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0:
                light = ((7.5 - x) / 6 + (cy - y) / 8)
                c.set(x, y, lighten(base, 0.18 * light) if light > 0 else shade(base, 1 + 0.25 * light))
    placed = 0
    while placed < 7:
        x, y = rnd.randrange(4, 12), rnd.randrange(4, 15)
        if c.get(x, y)[3] > 0:
            c.set(x, y, spots)
            if rnd.random() < 0.5 and c.get(x + 1, y)[3] > 0:
                c.set(x + 1, y, spots)
            placed += 1
    c.set(6, 4, lighten(base, 0.6))
    c.set(5, 5, lighten(base, 0.4))
    c.outline(shade(base, 0.35))
    return c


def lantern_item():
    rows = [
        "......oooo......",
        ".....oMooMo.....",
        "......oooo......",
        ".....oDDDDo.....",
        "....oKMMMMKo....",
        "....oMLHHLMo....",
        "....oMHHHHMo....",
        "....oMLHHLMo....",
        "....oMLLLLMo....",
        "....oKMMMMKo....",
        ".....oDDDDo.....",
        "......oooo......",
    ]
    iron = {"o": rgb("15161a"), "K": rgb("2e3038"), "D": rgb("41444f")}
    glow = {"M": rgb("2fb3cf"), "L": rgb("6fe6ff"), "H": rgb("e8ffff")}
    return grid_sprite(rows, {**iron, **glow})


# ------------------------------------------------------------------ armor

def scale_pattern(c, pal):
    """Re-shades filled pixels with an overlapping-scales pattern."""
    def fn(x, y, col):
        if col == pal["o"]:
            return col
        row = y // 2
        phase = (x + (row % 2)) % 3
        base = pal["M"]
        if phase == 0:
            base = pal["L"]
        elif phase == 2:
            base = pal["D"]
        if y % 2 == 1 and phase == 1:
            base = pal["K"]
        if col in (pal["H"],):
            return col
        # keep the silhouette's own light/dark sides
        if col == pal["L"]:
            return mix(base, pal["H"], 0.35)
        if col in (pal["D"], pal["K"]):
            return shade(base, 0.75)
        return base
    c.map(fn)
    return c


ARMOR = {
    "helmet": [
        "................",
        "....oooooooo....",
        "...oLLLLLLMMo...",
        "..oLHLLLLLMMDo..",
        "..oLLMMMMMMDDo..",
        "..oLMooooooDDo..",
        "..oLMo....oDDo..",
        "..oMDo....oDKo..",
        "...oo......oo...",
    ],
    "chestplate": [
        "..ooo......ooo..",
        ".oLLLo....oMMDo.",
        ".oLHLLooooLMMDo.",
        ".oLLLLLLLLMMMDo.",
        "..ooLLLLLLMDoo..",
        "....oLLLMMMDo...",
        "....oLLMMMDDo...",
        "....oLMMMMDDo...",
        "....oLMMMDDDo...",
        "....oMMMDDDKo...",
        "....oooooooooo..",
    ],
    "leggings": [
        "...oooooooooo...",
        "...oLLLLMMMDo...",
        "...oLHLMMMDDo...",
        "...oLLMooMMDo...",
        "...oLMo..oMDo...",
        "...oLMo..oMDo...",
        "...oLMo..oMDo...",
        "...oLMo..oMDo...",
        "...oMDo..oDKo...",
        "...oooo..oooo...",
    ],
    "boots": [
        "...oooo..oooo...",
        "...oLMo..oLMo...",
        "...oLMo..oLMo...",
        "...oLMo..oLMo...",
        "..ooLMo..oLMoo..",
        ".oLLLMo..oLMDDo.",
        ".oMMMDo..oMDDKo.",
        ".oooooo..oooooo.",
    ],
}


def armor_icon(piece, pal, scales=True):
    c = grid_sprite(ARMOR[piece], pal)
    if scales:
        scale_pattern(c, pal)
    return c


# ------------------------------------------------------------------ tools & weapons

def sword(blade, hilt, grip, pommel, length=10, width=2, glow=None, ghost=None):
    """Draws a diagonal sword, tip at the top right."""
    c = Canvas(16, 16)
    # blade: from (4,11) toward (14,1)
    for i in range(length):
        x = 5 + i
        y = 10 - i
        t = i / max(1, length - 1)
        c.set(x, y, blade["L"] if i < length - 1 else blade["H"])
        c.set(x + 1, y, blade["M"])
        if width >= 2:
            c.set(x, y - 1, blade["H"] if i % 3 == 0 else blade["L"])
            c.set(x + 1, y + 1, blade["D"])
        if width >= 3 and 1 <= i < length - 2:
            c.set(x + 2, y + 1, blade["D"])
            c.set(x - 1, y - 1, blade["L"])
        if glow and i % 2 == 0:
            c.set(x + 1, y, glow)
    # guard, perpendicular to the blade
    for i in range(-2, 3):
        c.set(5 + i, 10 + i, hilt["M"] if abs(i) < 2 else hilt["L"])
    c.set(3, 8, hilt["D"])
    c.set(7, 12, hilt["D"])
    # grip
    for i in range(3):
        c.set(3 - i, 12 + i - 1, grip["M"] if i % 2 == 0 else grip["D"])
        c.set(4 - i, 12 + i, grip["D"])
    # pommel
    c.set(0, 14, pommel["L"])
    c.set(1, 15, pommel["M"])
    c.set(0, 15, pommel["D"])
    c.set(1, 14, pommel["H"])
    if ghost:
        # the afterimage of a second blade
        for i in range(0, length, 2):
            x, y = 7 + i, 10 - i
            if c.get(x, y)[3] == 0:
                c.set(x, y, ghost)
    c.outline(blade["o"])
    return c


def gladius():
    rows = [
        "..............oo",
        ".............oHo",
        "............oHLo",
        "...........oHLMo",
        "..........oHLMDo",
        ".........oHLMDo.",
        "........oHLMDo..",
        ".......oHLMDo...",
        "...oo.oLLMDo....",
        "...oBooLMDo.....",
        "....oBBMDo......",
        "....oCBBo.......",
        "...oGoBCBo......",
        "..oGGo.oBCo.....",
        ".oPGo...ooo.....",
        ".oPo............",
    ]
    c = Canvas(16, 16)
    pal = dict(STEEL)
    pal.update({"B": BRONZE["L"], "C": BRONZE["D"], "G": WOOD["M"], "P": BRONZE["M"]})
    c.grid(rows, pal)
    return c


def chisel():
    c = Canvas(16, 16)
    # handle
    for i in range(6):
        x, y = 2 + i, 13 - i
        c.set(x, y, WOOD["L"] if i % 2 == 0 else WOOD["M"])
        c.set(x + 1, y, WOOD["D"])
    # copper ferrule
    for (x, y) in [(8, 7), (9, 7), (8, 8), (9, 6)]:
        c.set(x, y, rgb("e07b45"))
    c.set(8, 6, rgb("f3a978"))
    # steel shank and flat tip
    for i in range(4):
        x, y = 10 + i, 5 - i
        c.set(x, y, STEEL["L"])
        c.set(x, y + 1, STEEL["D"])
    for (x, y) in [(13, 1), (14, 1), (14, 2), (12, 1)]:
        c.set(x, y, STEEL["H"])
    c.outline(rgb("1a1410"))
    return c


def compass_frame(frame):
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 6.8:
                if d > 5.6:
                    light = ((7.5 - x) + (7.5 - y)) / 14
                    c.set(x, y, ramp([GOLD["K"], GOLD["M"], GOLD["L"]], 0.45 + light))
                else:
                    c.set(x, y, ramp([rgb("0b1a2a"), rgb("15304a")], (d / 5.6)))
    for (x, y) in [(7, 2), (8, 2), (13, 7), (13, 8), (7, 13), (8, 13), (2, 7), (2, 8)]:
        c.set(x, y, GOLD["H"])
    # the crystal needle
    angle = ((frame - 16) % 32) / 32 * math.tau
    dx, dy = math.sin(angle), -math.cos(angle)
    cx, cy = 8.0, 8.0
    tail = rgb("4a5068")
    steps = 40
    for s in range(steps + 1):
        t = s / steps
        # tail
        x = math.floor(cx - dx * 3.2 * t)
        y = math.floor(cy - dy * 3.2 * t)
        c.set(x, y, tail)
    for s in range(steps + 1):
        t = s / steps
        x = math.floor(cx + dx * 5.4 * t)
        y = math.floor(cy + dy * 5.4 * t)
        c.set(x, y, CYAN["H"] if t > 0.65 else CYAN["L"])
    c.set(math.floor(cx), math.floor(cy), GOLD["L"])
    c.outline(GOLD["o"])
    return c


def generate(write, write_raw):
    write("item/echo_shard", echo_shard())
    write("item/echo_dust", echo_dust())
    write("item/ancient_coin", ancient_coin())
    write("item/wyrmscale", wyrmscale())
    write("item/wyrm_heart", wyrm_heart())
    write("item/heirloom_locket", heirloom_locket())
    write("item/festival_charm", festival_charm())
    write("item/tarnished_crown", tarnished_crown())
    write("item/music_disc_echoes", music_disc())
    write("item/lingerer_spawn_egg", spawn_egg(rgb("a9dbe6"), rgb("3b4b5c"), 1))
    write("item/memory_moth_spawn_egg", spawn_egg(rgb("eef4fa"), rgb("6fc9e6"), 2))
    write("item/echo_wyrm_spawn_egg", spawn_egg(rgb("8a63d6"), rgb("e2c8ff"), 3))
    write("item/echo_lantern", lantern_item())
    for piece in ("helmet", "chestplate", "leggings", "boots"):
        write(f"item/wyrmscale_{piece}", armor_icon(piece, VIOLET))
    write("item/archaeologist_chisel", chisel())
    write("item/legionnaire_blade", gladius())
    write("item/echoing_blade", sword(VIOLET, CYAN, VIOLET, CYAN, length=11, width=2, glow=rgb("9ff0ff"), ghost=(159, 240, 255, 110)))
    for frame in range(32):
        write(f"item/resonance_compass_{frame:02d}", compass_frame(frame))
