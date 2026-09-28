"""Entity textures: hologram figures, lingerer, wyrm, moth, projector light-work, armor layers, GUI, icon."""
import math
import random

from pixel import Canvas, rgb, gray, mix, shade, lighten, ramp, paint_box, box_faces, value_noise

# ------------------------------------------------------------------ humanoid skins
# Hologram figures are painted in light greys (they are multiplied by a faction tint when rendered).

SKIN = gray(215)
SKIN_SHADE = gray(185)
HAIR = gray(120)
EYE_WHITE = gray(250)
EYE = gray(60)


def base_value(face):
    """Simple directional shading: tops lightest, backs and bottoms darker."""
    return {"top": 1.08, "front": 1.0, "right": 0.9, "left": 0.9, "back": 0.82, "bottom": 0.75}[face]


def solid(value):
    def painter(face, lx, ly, fw, fh):
        return gray(value * base_value(face))
    return painter


def head_painter(hair_value=120, beard=False, hood=False, eyes_glow=False, cap=False):
    def painter(face, lx, ly, fw, fh):
        f = base_value(face)
        if face == "top":
            return gray(hair_value * 1.1)
        if face == "bottom":
            return gray(180)
        if face == "back":
            return gray(hair_value * (0.95 if ly < 6 else 1.0))
        if face in ("left", "right"):
            if ly < 3:
                return gray(hair_value)
            if ly == 3 and lx in (0, 7):
                return gray(hair_value)
            return gray(200 * f)
        # front: face
        if ly < 2:
            return gray(hair_value)
        if ly == 2 and lx in (0, 7):
            return gray(hair_value)
        if ly == 4 and lx in (1, 2, 5, 6):
            if eyes_glow:
                return gray(255)
            return EYE_WHITE if lx in (1, 5) else EYE
        if ly == 3 and lx in (1, 2, 5, 6):
            return gray(170)  # brows
        if ly == 5 and lx in (3, 4):
            return gray(175)  # nose shadow
        if ly == 6 and lx in (3, 4):
            return gray(140)  # mouth
        if beard and ly >= 5 and lx not in (3, 4) or (beard and ly == 7):
            return gray(hair_value + 20)
        return gray(212 - (lx in (0, 7)) * 14)
    return painter


def hat_painter(kind):
    """Overlay layer on the head (inflated). Returns None where transparent."""
    def painter(face, lx, ly, fw, fh):
        if kind == "hood":
            if face == "front":
                if ly <= 1 or lx == 0 or lx == 7:
                    return gray(150 - ly * 3)
                return None
            if face == "bottom":
                return None
            return gray(150 * base_value(face) - (lx + ly) % 3 * 6)
        if kind == "cap":
            if face == "top":
                return gray(130)
            if face == "bottom":
                return None
            if ly <= 2:
                if face == "front" and ly == 1 and lx in (3, 4):
                    return gray(255)  # miner's lamp
                return gray(135 * base_value(face))
            return None
        if kind == "headband":
            if face in ("front", "back", "left", "right") and ly == 2:
                return gray(95)
            return None
        return None
    return painter


def torso(style):
    """Returns (body_painter, jacket_painter)."""
    def body(face, lx, ly, fw, fh):
        f = base_value(face)
        if face in ("top", "bottom"):
            return gray(150 * f)
        v = 160
        if style == "chain":
            v = 175 if (lx + ly) % 2 == 0 else 135
        elif style == "plate":
            v = 225 if lx in (1, 2) else 200
            if ly in (0, 5):
                v = 150
        elif style == "tunic":
            v = 190 - (ly // 4) * 6
        elif style == "robe":
            v = 175
        elif style == "leather":
            v = 150 + ((lx * 3 + ly * 5) % 7) * 3
        elif style == "overalls":
            v = 150 if lx in (1, 2, 5, 6) else 185
        if ly == 8 or ly == 9:
            v = 90 if style != "robe" else 120  # belt
            if face == "front" and lx in (3, 4) and ly == 8:
                v = 235  # buckle
        return gray(v * f)

    def jacket(face, lx, ly, fw, fh):
        f = base_value(face)
        if style == "chain":
            # a tabard with a sigil
            if face in ("front", "back") and 1 <= lx <= 6 and ly <= 11:
                if face == "front" and 2 <= ly <= 5 and lx in (3, 4):
                    return gray(250)
                if face == "front" and ly == 3 and lx in (2, 5):
                    return gray(250)
                return gray(215 * f)
            return None
        if style == "plate":
            if ly <= 1 and face != "bottom":
                return gray(240 * f)  # pauldron rim
            return None
        if style == "robe":
            if face == "top" or face == "bottom":
                return None
            if face == "front" and lx in (3, 4):
                return gray(245)  # trim
            if ly <= 1:
                return gray(235 * f)  # fur collar
            return gray((170 + (ly % 3) * 5) * f)
        if style == "leather" and face == "front":
            # quiver strap
            if lx == ly // 2 + 1 or lx == ly // 2 + 2:
                return gray(95)
            return None
        return None
    return body, jacket


def limb(style, bare=False, boots=False, gloves=False):
    def painter(face, lx, ly, fw, fh):
        f = base_value(face)
        if face == "top":
            return gray((200 if bare else 150) * f)
        if face == "bottom":
            return gray(100 if boots else 170)
        v = 160
        if style == "chain":
            v = 175 if (lx + ly) % 2 == 0 else 140
        elif style == "plate":
            v = 220 if lx == 1 else 195
            if ly in (4, 8):
                v = 150
        elif style == "cloth":
            v = 175
        elif style == "dark":
            v = 135
        elif style == "robe":
            v = 172
        if bare and ly < 6:
            v = 205
        if gloves and ly >= 9:
            v = 110
        if boots and ly >= 8:
            v = 95 if ly >= 9 else 120
        return gray(v * f)
    return painter


def overlay_limb(kind):
    def painter(face, lx, ly, fw, fh):
        if kind == "cuff" and ly in (9, 10) and face not in ("top", "bottom"):
            return gray(235)
        if kind == "sleeve" and ly <= 7 and face not in ("bottom",):
            return gray(180 * base_value(face))
        if kind == "robe_leg" and face not in ("top", "bottom"):
            return gray((172 + (ly % 4) * 4) * base_value(face))
        return None
    return painter


def paint_props(c):
    """The shared prop region (sword, pickaxe, staff, bow, helmet, crown)."""
    steel = lambda face, lx, ly, fw, fh: gray(240 if face in ("top", "front") else 200)
    paint_box(c, 64, 0, 1, 1, 4, lambda f, x, y, w, h: gray(90 + (x % 2) * 20))  # grip
    paint_box(c, 64, 6, 4, 2, 1, lambda f, x, y, w, h: gray(170 * base_value(f)))  # guard
    paint_box(c, 64, 10, 1, 2, 11, lambda f, x, y, w, h: gray(250 if y == 0 else 215))  # blade
    paint_box(c, 64, 26, 1, 1, 12, lambda f, x, y, w, h: gray(120 + (x % 3) * 10))  # pick handle
    paint_box(c, 64, 40, 1, 7, 1, lambda f, x, y, w, h: gray(215 * base_value(f)))  # pick head
    paint_box(c, 68, 40, 1, 1, 1, lambda f, x, y, w, h: gray(240))
    paint_box(c, 100, 0, 1, 26, 1, lambda f, x, y, w, h: gray(130 + (y % 4) * 8))  # staff
    paint_box(c, 106, 0, 3, 3, 3, lambda f, x, y, w, h: gray(255 if (x + y) % 2 == 0 else 225))  # orb
    paint_box(c, 120, 0, 1, 18, 1, lambda f, x, y, w, h: gray(150 + (abs(y - 9) < 2) * 40))  # bow limb
    paint_box(c, 124, 0, 0, 16, 1, lambda f, x, y, w, h: gray(240))  # bow string
    # helmet with a ridge and visor slit
    def helmet(face, lx, ly, fw, fh):
        f = base_value(face)
        if face == "front" and ly == 2 and 1 <= lx <= 7:
            return gray(70)
        if face == "bottom":
            return None
        return gray((220 if (lx + ly) % 5 else 245) * f)
    paint_box(c, 64, 48, 9, 4, 9, helmet)
    paint_box(c, 64, 61, 1, 3, 7, lambda f, x, y, w, h: gray(250 - y * 10))  # plume
    # crown with jewels
    def crown(face, lx, ly, fw, fh):
        if face == "bottom":
            return None
        if face == "front" and ly == 1 and lx in (2, 4, 6):
            return gray(255)
        return gray(235 if ly == 0 else 205)
    paint_box(c, 0, 64, 9, 2, 9, crown)
    paint_box(c, 40, 64, 1, 2, 1, lambda f, x, y, w, h: gray(240))
    paint_box(c, 44, 64, 1, 3, 1, lambda f, x, y, w, h: gray(255))


def details(c, role):
    """Role-specific touches painted directly onto the skin layout."""
    body_front = (20, 20)  # 8x12
    body_back = (32, 20)
    if role == "merchant":
        # coin pouch on the belt and a necklace of office
        for (x, y) in [(25, 29), (26, 29), (25, 30), (26, 30)]:
            c.set(x, y, gray(110))
        c.set(25, 29, gray(235))
        for x in range(21, 27):
            c.set(x, 21 + (1 if x in (22, 25) else 0) + (2 if x in (23, 24) else 0), gray(240))
    if role == "noble":
        for i, x in enumerate(range(21, 27)):
            c.set(x, 22 + min(i, 5 - i), gray(250))
        c.set(23, 25, gray(255))
        c.set(24, 25, gray(255))
    if role == "mage":
        for x in range(20, 28):
            if x % 2 == 0:
                c.set(x, 30, gray(255))  # runes along the hem
        for x in (22, 25):
            for y in (23, 26):
                c.set(x, y, gray(245))
    if role == "archer":
        # quiver on the back with arrow fletchings poking over the shoulder
        for y in range(20, 30):
            x = body_back[0] + 5 - (y - 20) // 3
            c.set(x, y, gray(95))
            c.set(x + 1, y, gray(120))
        for x in (36, 37, 38):
            c.set(x, 16 + 3, gray(250))
    if role == "peasant" or role == "child":
        for (x, y) in [(22, 25), (22, 26), (23, 25), (23, 26)]:
            c.set(x, y, gray(160))  # a patch
        c.set(22, 25, gray(140))
    if role == "knight":
        # a heraldic cross on the breastplate
        for y in range(22, 28):
            c.set(23, y, gray(250))
            c.set(24, y, gray(250))
        for x in range(21, 27):
            c.set(x, 24, gray(250))
    if role == "miner":
        for (x, y) in [(21, 23), (26, 26), (22, 28), (43, 24)]:
            c.set(x, y, gray(110))  # soot smudges


ROLE_STYLES = {
    "soldier": dict(torso="chain", arms="chain", legs="cloth", hat=None, hair=110, boots=True, gloves=True),
    "knight": dict(torso="plate", arms="plate", legs="plate", hat=None, hair=90, boots=True, gloves=True),
    "archer": dict(torso="leather", arms="dark", legs="cloth", hat="hood", hair=100, boots=True, gloves=True),
    "peasant": dict(torso="tunic", arms="cloth", legs="dark", hat=None, hair=140, bare_arms=True, boots=True),
    "merchant": dict(torso="robe", arms="robe", legs="dark", hat="headband", hair=90, beard=True, sleeve="cuff", boots=True),
    "noble": dict(torso="robe", arms="robe", legs="robe", hat=None, hair=200, sleeve="cuff", leg_overlay="robe_leg"),
    "mage": dict(torso="robe", arms="robe", legs="robe", hat="hood", hair=230, beard=True, leg_overlay="robe_leg"),
    "miner": dict(torso="overalls", arms="cloth", legs="overalls", hat="cap", hair=110, bare_arms=True, boots=True, gloves=True),
    "child": dict(torso="tunic", arms="cloth", legs="cloth", hat=None, hair=160, bare_arms=True),
    "dragon": dict(torso="tunic", arms="cloth", legs="cloth", hat=None, hair=160),
}


def humanoid(role, scanlines=True, eyes_glow=False, tattered=False):
    s = ROLE_STYLES[role]
    c = Canvas(128, 128)
    paint_box(c, 0, 0, 8, 8, 8, head_painter(s["hair"], beard=s.get("beard", False), eyes_glow=eyes_glow))
    if s["hat"]:
        paint_box(c, 32, 0, 8, 8, 8, hat_painter(s["hat"]))
    body, jacket = torso(s["torso"])
    paint_box(c, 16, 16, 8, 12, 4, body)
    paint_box(c, 16, 32, 8, 12, 4, jacket)
    arm = limb(s["arms"], bare=s.get("bare_arms", False), gloves=s.get("gloves", False))
    paint_box(c, 40, 16, 4, 12, 4, arm)
    paint_box(c, 32, 48, 4, 12, 4, arm)
    if s.get("sleeve"):
        paint_box(c, 40, 32, 4, 12, 4, overlay_limb(s["sleeve"]))
        paint_box(c, 48, 48, 4, 12, 4, overlay_limb(s["sleeve"]))
    leg_style = {"overalls": "cloth", "robe": "robe"}.get(s["legs"], s["legs"])
    leg = limb(leg_style, boots=s.get("boots", False))
    paint_box(c, 0, 16, 4, 12, 4, leg)
    paint_box(c, 16, 48, 4, 12, 4, leg)
    if s.get("leg_overlay"):
        paint_box(c, 0, 32, 4, 12, 4, overlay_limb(s["leg_overlay"]))
        paint_box(c, 0, 48, 4, 12, 4, overlay_limb(s["leg_overlay"]))
    paint_props(c)
    details(c, role)

    if tattered:
        rnd = random.Random(role)
        def tear(x, y, col):
            if x < 64 and y < 64 and rnd.random() < 0.08:
                return (col[0], col[1], col[2], 0) if y >= 32 else shade(col, 0.7)
            return col
        c.map(tear)
        # a ribcage showing through the torn tabard
        for i, ly in enumerate((22, 24, 26)):
            for lx in range(21, 27):
                if lx not in (23, 24):
                    c.set(lx, ly, gray(245))
            c.set(23, ly, gray(80))
            c.set(24, ly, gray(80))

    if scanlines:
        def lines(x, y, col):
            f = 0.86 if y % 2 else 1.0
            return (int(col[0] * f), int(col[1] * f), int(col[2] * f), col[3])
        c.map(lines)
    return c


# ------------------------------------------------------------------ wyrm

WYRM = [rgb("4a3a78"), rgb("6c58a8"), rgb("9884d6"), rgb("c7b8f2"), rgb("eee6ff")]


def wyrm_texture():
    c = Canvas(128, 128)

    def scales(value_shift=0.0, belly=False):
        def painter(face, lx, ly, fw, fh):
            f = base_value(face)
            if belly and face == "bottom":
                t = 0.75 + (ly % 2) * 0.1
                return ramp(WYRM, t)
            row = ly // 2
            phase = (lx + row % 2) % 3
            t = 0.45 + value_shift + (0.12 if phase == 0 else -0.06 if phase == 2 else 0.0)
            if ly % 2 == 1 and phase == 1:
                t -= 0.15
            return ramp(WYRM, t * f)
        return painter

    paint_box(c, 0, 0, 12, 10, 20, scales(belly=True))
    paint_box(c, 64, 0, 2, 3, 16, lambda f, x, y, w, h: ramp(WYRM, 0.85 - (y % 3) * 0.1))  # spines
    paint_box(c, 0, 30, 7, 7, 7, scales(0.03, belly=True))
    paint_box(c, 28, 30, 6, 6, 6, scales(0.05, belly=True))

    def head(face, lx, ly, fw, fh):
        f = base_value(face)
        if face == "front" and ly == 2 and lx in (1, 6):
            return rgb("9ff0ff")  # eyes
        if face in ("left", "right") and ly == 2 and lx in (1, 2):
            return rgb("9ff0ff")
        return scales(0.05)(face, lx, ly, fw, fh)
    paint_box(c, 0, 44, 8, 6, 8, head)

    def snout(face, lx, ly, fw, fh):
        if face == "front" and ly == 1 and lx in (1, 3):
            return WYRM[0]  # nostrils
        if face == "bottom" or (face in ("left", "right", "front") and ly == fh - 1):
            return gray(250)  # teeth line
        return scales(0.08)(face, lx, ly, fw, fh)
    paint_box(c, 32, 44, 5, 3, 6, snout)
    paint_box(c, 54, 44, 1, 4, 1, lambda f, x, y, w, h: ramp(WYRM, 0.95 - y * 0.12))  # horns

    def jaw(face, lx, ly, fw, fh):
        if face == "top":
            return rgb("3a1030") if lx % 3 else gray(250)  # mouth and teeth
        return scales(-0.05, belly=True)(face, lx, ly, fw, fh)
    paint_box(c, 0, 58, 5, 2, 10, jaw)

    paint_box(c, 64, 20, 18, 2, 2, lambda f, x, y, w, h: ramp(WYRM, 0.6 * base_value(f)))
    paint_box(c, 64, 24, 16, 1, 1, lambda f, x, y, w, h: ramp(WYRM, 0.6))

    def membrane(face, lx, ly, fw, fh):
        # translucent-looking membrane with veins radiating from the arm bone
        vein = (lx * 3 + ly * 2) % 9 == 0 or ly == 0
        t = 0.55 + 0.25 * (ly / max(1, fh)) - (0.2 if vein else 0.0)
        col = ramp([WYRM[1], WYRM[2], WYRM[3], rgb("b8f4ff")], t)
        if face == "bottom":
            col = shade(col, 0.85)
        return col
    for (x, y, w, d) in [(14, 72, 18, 14), (32, 72, 18, 14), (12, 86, 16, 12), (28, 86, 16, 12)]:
        for ly in range(d):
            for lx in range(w):
                c.set(x + lx, y + ly, membrane("top" if x in (14, 12) else "bottom", lx, ly, w, d))
    # ragged trailing edge of the wing membranes
    rnd = random.Random(5)
    for (x, y, w, d) in [(14, 72, 18, 14), (32, 72, 18, 14), (12, 86, 16, 12), (28, 86, 16, 12)]:
        for lx in range(w):
            for k in range(rnd.randint(0, 2)):
                c.set(x + lx, y + d - 1 - k, (0, 0, 0, 0))

    paint_box(c, 64, 30, 6, 6, 8, scales(0.0, belly=True))
    paint_box(c, 92, 30, 5, 5, 8, scales(0.02, belly=True))
    paint_box(c, 64, 44, 4, 4, 8, scales(0.04, belly=True))
    paint_box(c, 88, 44, 3, 3, 8, scales(0.06, belly=True))
    paint_box(c, 110, 44, 1, 3, 6, lambda f, x, y, w, h: rgb("b8f4ff") if y == 0 else ramp(WYRM, 0.8))
    paint_box(c, 64, 56, 3, 7, 3, lambda f, x, y, w, h: gray(245) if y == h - 1 else ramp(WYRM, 0.5 * base_value(f)))
    paint_box(c, 76, 56, 4, 8, 4, lambda f, x, y, w, h: gray(245) if y == h - 1 else ramp(WYRM, 0.5 * base_value(f)))
    return c


# ------------------------------------------------------------------ moth

def moth_texture():
    c = Canvas(32, 32)
    paint_box(c, 0, 0, 2, 2, 6, lambda f, x, y, w, h: rgb("e9eef5") if (x + y) % 2 else rgb("c9d6e6"))
    paint_box(c, 16, 0, 2, 2, 2, lambda f, x, y, w, h: rgb("0b2a3a") if f == "front" and y == 0 else rgb("dfe8f2"))
    paint_box(c, 24, 0, 0, 3, 2, lambda f, x, y, w, h: rgb("bcd3e8"))

    def wing(x0, y0, w, d, big):
        for ly in range(d):
            for lx in range(w):
                t = lx / max(1, w - 1)
                col = mix(rgb("ffffff"), rgb("d8e8f6"), t)
                # eye-spot of crystallized memory
                cx, cy = (w * 0.6, d * 0.45)
                dd = math.hypot(lx - cx, ly - cy)
                if dd < (1.6 if big else 1.1):
                    col = rgb("2fb3cf") if dd > 0.7 else rgb("e8ffff")
                if lx == w - 1 or ly in (0, d - 1):
                    col = rgb("a9c4dc")
                c.set(x0 + lx, y0 + ly, col)
    wing(6, 8, 7, 6, True)
    wing(13, 8, 7, 6, True)
    wing(5, 14, 5, 5, False)
    wing(10, 14, 5, 5, False)
    return c


# ------------------------------------------------------------------ projector light-work

def projector_hologram():
    c = Canvas(64, 64)
    paint_box(c, 0, 0, 4, 6, 4, lambda f, x, y, w, h: ramp([rgb("6fe6ff"), rgb("e8ffff")], 1 - abs(y - h / 2) / (h / 2) * 0.6 if h else 1))
    paint_box(c, 16, 0, 2, 9, 2, lambda f, x, y, w, h: rgb("ffffff"))
    ring = lambda f, x, y, w, h: rgb("c8fbff") if (x + y) % 2 else rgb("6fe6ff")
    paint_box(c, 0, 16, 12, 1, 1, ring)
    paint_box(c, 0, 18, 1, 1, 10, ring)
    paint_box(c, 0, 30, 18, 1, 1, ring)
    paint_box(c, 0, 32, 1, 1, 16, ring)

    def beam(face, lx, ly, fw, fh):
        t = ly / max(1, fh - 1)  # 0 at the top of the beam
        a = int(40 + 200 * t)
        core = lx == 1
        col = rgb("ffffff") if core else rgb("9ff0ff")
        return (col[0], col[1], col[2], a)
    paint_box(c, 48, 0, 3, 48, 3, beam)
    return c


# ------------------------------------------------------------------ armor layers

def armor_layers():
    body = Canvas(64, 32)
    pal = [rgb("2e1a52"), rgb("4a2c80"), rgb("6c4bb0"), rgb("9a7ad8"), rgb("d0bfff")]

    def scale_painter(face, lx, ly, fw, fh):
        row = ly // 2
        phase = (lx + row % 2) % 3
        t = 0.5 + (0.2 if phase == 0 else -0.1 if phase == 2 else 0.0) - (0.15 if ly % 2 and phase == 1 else 0.0)
        t *= base_value(face)
        if face == "front" and ly == 0:
            t = 0.9
        return ramp(pal, t)

    def helmet(face, lx, ly, fw, fh):
        if face == "front" and 2 <= ly <= 4 and 1 <= lx <= 6:
            return None  # open face
        if face == "bottom":
            return None
        if face == "front" and ly == 1 and lx in (3, 4):
            return rgb("9ff0ff")  # crest gem
        return scale_painter(face, lx, ly, fw, fh)
    paint_box(body, 0, 0, 8, 8, 8, helmet)
    paint_box(body, 16, 16, 8, 12, 4, scale_painter)
    paint_box(body, 40, 16, 4, 12, 4, scale_painter)
    paint_box(body, 0, 16, 4, 12, 4, lambda f, x, y, w, h: scale_painter(f, x, y, w, h) if y >= 7 else None)  # boots

    legs = Canvas(64, 32)
    paint_box(legs, 16, 16, 8, 12, 4, lambda f, x, y, w, h: scale_painter(f, x, y, w, h) if y >= 7 else None)
    paint_box(legs, 0, 16, 4, 12, 4, lambda f, x, y, w, h: scale_painter(f, x, y, w, h) if y <= 9 else None)

    crown = Canvas(64, 32)
    gold = [rgb("5a3a0c"), rgb("9a6418"), rgb("d19a2a"), rgb("f2cc5a"), rgb("fff2b8")]

    def crown_painter(face, lx, ly, fw, fh):
        if face in ("top", "bottom"):
            return None
        if ly == 0 and lx % 3 != 1:
            return None
        if ly > 2:
            return None
        if face == "front" and ly == 2 and lx in (2, 5):
            return rgb("c02a3c")
        if face == "front" and ly == 1 and lx == 4:
            return rgb("6fe6ff")
        return ramp(gold, 0.7 if ly < 2 else 0.45)
    paint_box(crown, 0, 0, 8, 8, 8, crown_painter)
    return body, legs, crown


# ------------------------------------------------------------------ GUI & icon

def advancement_background():
    c = Canvas(16, 16)
    n = value_noise(77, 16, 16, scale=3, octaves=2)
    for y in range(16):
        for x in range(16):
            c.set(x, y, ramp([rgb("15192a"), rgb("1d2338"), rgb("262e48")], n[y][x]))
    for (x, y) in [(3, 4), (11, 2), (7, 12), (13, 10)]:
        c.set(x, y, rgb("2fb3cf"))
    c.set(11, 3, rgb("1b6f8a"))
    return c


def icon():
    """The mod icon: a projector casting a ghostly knight into the night."""
    size = 128
    c = Canvas(size, size)
    for y in range(size):
        for x in range(size):
            t = y / size
            c.set(x, y, ramp([rgb("0b1024"), rgb("1a1f45"), rgb("2a2d5a")], t))
    rnd = random.Random(3)
    for _ in range(60):
        x, y = rnd.randrange(size), rnd.randrange(size // 2)
        c.set(x, y, rgb("cfe8ff") if rnd.random() < 0.7 else rgb("ffffff"))
    # beam of light
    for y in range(20, 104):
        spread = (104 - y) * 0.35 + 4
        for x in range(int(64 - spread), int(64 + spread) + 1):
            edge = abs(x - 64) / max(1.0, spread)
            a = (1 - edge) * (0.25 + 0.4 * (y - 20) / 84)
            c.set(x, y, mix(c.get(x, y), rgb("9ff0ff"), a))
    # ghostly knight silhouette in the beam (built from rectangles, pixel-art style)
    k = rgb("dffaff")
    kd = rgb("7fd6ee")
    def r(x, y, w, h, col):
        for j in range(h):
            for i in range(w):
                base = c.get(x + i, y + j)
                c.set(x + i, y + j, mix(base, col, 0.82))
    r(56, 30, 16, 14, k)  # helmet
    r(58, 36, 12, 3, kd)  # visor slit
    r(52, 45, 24, 26, k)  # torso
    r(46, 45, 6, 22, kd)  # arm
    r(76, 45, 6, 22, kd)  # arm
    r(55, 71, 8, 22, k)  # leg
    r(65, 71, 8, 22, k)  # leg
    r(82, 24, 3, 42, rgb("ffffff"))  # sword blade
    r(78, 62, 11, 3, kd)  # guard
    r(62, 26, 4, 5, rgb("ffffff"))  # plume
    for y in range(30, 94, 2):
        for x in range(44, 92):
            col = c.get(x, y)
            c.set(x, y, shade(col, 0.93))
    # projector at the bottom
    brass = [rgb("4a2c12"), rgb("7a4d1f"), rgb("a8702e"), rgb("c9903f"), rgb("e2b25c")]
    r(40, 104, 48, 10, brass[2])
    r(40, 104, 48, 2, brass[4])
    r(48, 96, 32, 8, brass[3])
    r(56, 90, 16, 6, rgb("6fe6ff"))
    r(60, 91, 8, 3, rgb("ffffff"))
    r(34, 114, 60, 6, brass[1])
    # border
    for i in range(size):
        for w in range(3):
            for (x, y) in [(i, w), (i, size - 1 - w), (w, i), (size - 1 - w, i)]:
                c.set(x, y, brass[3] if w == 1 else brass[1])
    return c


def generate(write, write_raw):
    for role in ["soldier", "knight", "archer", "peasant", "merchant", "noble", "mage", "miner", "child", "dragon"]:
        write(f"entity/figure/{role}", humanoid(role))
    write("entity/lingerer", humanoid("soldier", scanlines=True, eyes_glow=True, tattered=True))
    write("entity/echo_wyrm", wyrm_texture())
    write("entity/memory_moth", moth_texture())
    write("entity/projector_hologram", projector_hologram())
    body, legs, crown = armor_layers()
    write("entity/equipment/humanoid/wyrmscale", body)
    write("entity/equipment/humanoid_leggings/wyrmscale", legs)
    write("entity/equipment/humanoid/tarnished_crown", crown)
    write("gui/advancements/backgrounds/echoes", advancement_background())
    write_raw("icon.png", icon())
