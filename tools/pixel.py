"""A tiny pixel-art toolkit used to paint the mod's textures deterministically."""
import math
import random

from PIL import Image


def rgb(hex_color, a=255):
    hex_color = hex_color.lstrip("#")
    return (int(hex_color[0:2], 16), int(hex_color[2:4], 16), int(hex_color[4:6], 16), a)


def gray(v, a=255):
    v = max(0, min(255, int(v)))
    return (v, v, v, a)


def mix(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(round(c1[i] + (c2[i] - c1[i]) * t)) for i in range(4))


def shade(c, factor):
    return (max(0, min(255, int(c[0] * factor))), max(0, min(255, int(c[1] * factor))), max(0, min(255, int(c[2] * factor))), c[3])


def lighten(c, amount):
    return mix(c, (255, 255, 255, c[3]), amount)


def ramp(colors, t):
    """Samples a multi-stop color ramp at t in [0, 1]."""
    t = max(0.0, min(1.0, t))
    if len(colors) == 1:
        return colors[0]
    pos = t * (len(colors) - 1)
    i = min(int(pos), len(colors) - 2)
    return mix(colors[i], colors[i + 1], pos - i)


class Canvas:
    def __init__(self, w, h, fill=(0, 0, 0, 0)):
        self.img = Image.new("RGBA", (w, h), fill)
        self.px = self.img.load()
        self.w = w
        self.h = h

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h and c is not None:
            self.px[x, y] = tuple(int(v) for v in c)

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[x, y]
        return (0, 0, 0, 0)

    def rect(self, x0, y0, w, h, c):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                self.set(x, y, c)

    def fill(self, x0, y0, w, h, fn):
        """fn(x, y, local_x, local_y) -> color"""
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                self.set(x, y, fn(x, y, x - x0, y - y0))

    def grid(self, rows, palette, x0=0, y0=0):
        """Paints a character grid. '.' and ' ' are transparent/untouched."""
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch in ". ":
                    continue
                self.set(x0 + i, y0 + j, palette[ch])

    def outline(self, color, only_empty=True):
        """Adds a 1px outline around opaque pixels (items read better with a dark edge)."""
        coords = []
        for y in range(self.h):
            for x in range(self.w):
                if self.px[x, y][3] > 0:
                    continue
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if self.get(x + dx, y + dy)[3] > 0 and self.get(x + dx, y + dy) != color:
                        coords.append((x, y))
                        break
        for x, y in coords:
            self.set(x, y, color)

    def map(self, fn):
        for y in range(self.h):
            for x in range(self.w):
                c = self.px[x, y]
                if c[3] > 0:
                    self.px[x, y] = tuple(int(v) for v in fn(x, y, c))

    def paste(self, other, x0, y0):
        self.img.alpha_composite(other.img, (x0, y0))

    def save(self, path):
        import os
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.img.save(path)


def value_noise(seed, w, h, scale=4.0, octaves=3):
    """Tileable-ish smooth value noise in [0,1]."""
    rnd = random.Random(seed)
    layers = []
    for o in range(octaves):
        cells = max(1, int(scale * (2 ** o)))
        grid = [[rnd.random() for _ in range(cells + 1)] for _ in range(cells + 1)]
        for i in range(cells + 1):
            grid[i][cells] = grid[i][0]
            grid[cells][i] = grid[0][i]
        layers.append((cells, grid))
    out = [[0.0] * w for _ in range(h)]
    total = sum(0.5 ** o for o in range(octaves))
    for y in range(h):
        for x in range(w):
            v = 0.0
            for o, (cells, grid) in enumerate(layers):
                fx = x / w * cells
                fy = y / h * cells
                ix, iy = int(fx), int(fy)
                tx, ty = fx - ix, fy - iy
                tx = tx * tx * (3 - 2 * tx)
                ty = ty * ty * (3 - 2 * ty)
                a = grid[iy][ix] * (1 - tx) + grid[iy][ix + 1] * tx
                b = grid[iy + 1][ix] * (1 - tx) + grid[iy + 1][ix + 1] * tx
                v += (a * (1 - ty) + b * ty) * (0.5 ** o)
            out[y][x] = v / total
    return out


def box_faces(u, v, w, h, d):
    """Returns the texture rectangles of a model box: {face: (x, y, width, height)}."""
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }


def paint_box(canvas, u, v, w, h, d, painter):
    """painter(face, lx, ly, fw, fh) -> color or None"""
    for face, (x0, y0, fw, fh) in box_faces(u, v, w, h, d).items():
        for ly in range(fh):
            for lx in range(fw):
                c = painter(face, lx, ly, fw, fh)
                if c is not None:
                    canvas.set(x0 + lx, y0 + ly, c)


def dist(x0, y0, x1, y1):
    return math.hypot(x1 - x0, y1 - y0)
