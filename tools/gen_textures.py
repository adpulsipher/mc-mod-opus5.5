#!/usr/bin/env python3
"""Paints every texture for Echoes of the Past.

Run from the repository root:  python3 tools/gen_textures.py [--preview DIR]
"""
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))

import tex_blocks  # noqa: E402

TEXTURES = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "echoes_of_the_past", "textures")
MOD_ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "echoes_of_the_past")

written = []


def write(path, canvas):
    full = os.path.join(TEXTURES, path + ".png")
    canvas.save(full)
    written.append((path, canvas.img))


def write_raw(relative, canvas):
    full = os.path.join(MOD_ROOT, relative)
    canvas.save(full)
    written.append((relative, canvas.img))


def preview(directory, prefix_filter=None, scale=8, columns=8, name="preview.png"):
    items = [(p, img) for p, img in written if prefix_filter is None or p.startswith(prefix_filter)]
    if not items:
        return
    cell = max(max(img.width, img.height) for _, img in items) * scale // max(1, max(max(img.width, img.height) for _, img in items) // 16) if False else None
    tiles = []
    for p, img in items:
        s = scale if max(img.width, img.height) <= 16 else max(1, (16 * scale) // max(img.width, img.height) * 2)
        tiles.append((p, img.resize((img.width * s, img.height * s), Image.NEAREST)))
    cw = max(t.width for _, t in tiles) + 8
    ch = max(t.height for _, t in tiles) + 8
    rows = (len(tiles) + columns - 1) // columns
    sheet = Image.new("RGBA", (cw * columns, ch * rows), (40, 44, 52, 255))
    for i, (_, t) in enumerate(tiles):
        x = (i % columns) * cw + 4
        y = (i // columns) * ch + 4
        checker = Image.new("RGBA", t.size, (70, 74, 82, 255))
        sheet.alpha_composite(checker, (x, y))
        sheet.alpha_composite(t, (x, y))
    os.makedirs(directory, exist_ok=True)
    sheet.save(os.path.join(directory, name))


if __name__ == "__main__":
    tex_blocks.generate(write)
    for module_name in ("tex_items", "tex_entities", "tex_expansion"):
        try:
            module = __import__(module_name)
        except ModuleNotFoundError:
            continue
        module.generate(write, write_raw)
    if len(sys.argv) > 2 and sys.argv[1] == "--preview":
        out = sys.argv[2]
        preview(out, "block/", name="blocks.png")
        preview(out, "item/", name="items.png", columns=10)
        preview(out, "entity/", scale=4, columns=4, name="entities.png")
    print(f"Wrote {len(written)} textures.")
