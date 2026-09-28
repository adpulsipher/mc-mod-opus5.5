#!/usr/bin/env python3
"""Generates the JSON data and asset files for Echoes of the Past.

Run from the repository root:  python3 tools/gen_data.py
Everything under src/main/resources/{assets,data} that this script owns is rewritten.
"""
import json
import os

NS = "echoes_of_the_past"
ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", NS)
DATA = os.path.join(ROOT, "data")


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def ns(path):
    return f"{NS}:{path}"


def asset(*parts):
    return os.path.join(ASSETS, *parts)


def data(namespace, *parts):
    return os.path.join(DATA, namespace, *parts)


# ---------------------------------------------------------------- block states & models

def simple_block(name, model=None):
    write(asset("blockstates", f"{name}.json"), {"variants": {"": {"model": ns(f"block/{model or name}")}}})


def emissive_ore(name, base, glow, light=12):
    faces = {d: {"texture": "#base", "cullface": d} for d in ["down", "up", "north", "south", "west", "east"]}
    glow_faces = {d: {"texture": "#glow", "cullface": d} for d in ["down", "up", "north", "south", "west", "east"]}
    write(asset("models", "block", f"{name}.json"), {
        "parent": "minecraft:block/block",
        "textures": {"particle": base, "base": base, "glow": glow},
        "elements": [
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces},
            {"from": [0, 0, 0], "to": [16, 16, 16], "light_emission": light, "faces": glow_faces},
        ],
    })


def blocks():
    simple_block("echo_deposit")
    emissive_ore("echo_deposit", ns("block/echo_deposit"), ns("block/echo_deposit_glow"))
    simple_block("deepslate_echo_deposit")
    emissive_ore("deepslate_echo_deposit", ns("block/deepslate_echo_deposit"), ns("block/deepslate_echo_deposit_glow"))

    simple_block("echo_crystal_block")
    emissive_ore("echo_crystal_block", ns("block/echo_crystal_block"), ns("block/echo_crystal_block_glow"), light=14)

    # The extracted echo block is only ever an item, but it is shown as a glowing block.
    emissive_ore("echo_block", ns("block/echo_block"), ns("block/echo_block_glow"), light=15)

    # Relic caches, brushed through four stages.
    for kind in ["soil", "stone"]:
        name = f"relic_cache_{kind}"
        write(asset("blockstates", f"{name}.json"), {
            "variants": {f"dusted={i}": {"model": ns(f"block/{name}_{i}")} for i in range(4)}
        })
        for i in range(4):
            write(asset("models", "block", f"{name}_{i}.json"), {
                "parent": "minecraft:block/cube_all",
                "textures": {"all": ns(f"block/{name}_{i}")},
            })

    # Lantern
    write(asset("blockstates", "echo_lantern.json"), {"variants": {
        "hanging=false": {"model": ns("block/echo_lantern")},
        "hanging=true": {"model": ns("block/echo_lantern_hanging")},
    }})
    write(asset("models", "block", "echo_lantern.json"), {"parent": "minecraft:block/template_lantern", "textures": {"lantern": ns("block/echo_lantern")}})
    write(asset("models", "block", "echo_lantern_hanging.json"), {"parent": "minecraft:block/template_hanging_lantern", "textures": {"lantern": ns("block/echo_lantern")}})

    projector()


def box(frm, to, faces, **extra):
    element = {"from": frm, "to": to, "faces": faces}
    element.update(extra)
    return element


def all_faces(texture, uv_for=None, cull=None):
    faces = {}
    for d in ["down", "up", "north", "south", "west", "east"]:
        face = {"texture": texture}
        if uv_for:
            face["uv"] = uv_for(d)
        if cull and d in cull:
            face["cullface"] = d
        faces[d] = face
    return faces


def projector():
    """A brass apparatus: a wide base, an engraved pedestal, four struts and a crystal lens cradle."""
    variants = {}
    rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
    for facing, y in rotations.items():
        for active in ["false", "true"]:
            entry = {"model": ns("block/echo_projector" + ("_active" if active == "true" else ""))}
            if y:
                entry["y"] = y
            variants[f"active={active},facing={facing}"] = entry
    write(asset("blockstates", "echo_projector.json"), {"variants": variants})

    def side_uv(y0, y1, x0=0, x1=16):
        def uv(d):
            if d in ("up", "down"):
                return [x0, x0, x1, x1]
            return [x0, 16 - y1, x1, 16 - y0]
        return uv

    for suffix, lens, light in [("", ns("block/echo_projector_lens"), 8), ("_active", ns("block/echo_projector_lens_active"), 15)]:
        elements = [
            # Base plinth
            box([1, 0, 1], [15, 3, 15], {
                "down": {"texture": "#bottom", "uv": [1, 1, 15, 15], "cullface": "down"},
                "up": {"texture": "#top", "uv": [1, 1, 15, 15]},
                "north": {"texture": "#base", "uv": [1, 13, 15, 16]},
                "south": {"texture": "#base", "uv": [1, 13, 15, 16]},
                "west": {"texture": "#base", "uv": [1, 13, 15, 16]},
                "east": {"texture": "#base", "uv": [1, 13, 15, 16]},
            }),
            # Pedestal
            box([4, 3, 4], [12, 8, 12], {
                "north": {"texture": "#side", "uv": [4, 8, 12, 13]},
                "south": {"texture": "#side", "uv": [4, 8, 12, 13]},
                "west": {"texture": "#side", "uv": [4, 8, 12, 13]},
                "east": {"texture": "#side", "uv": [4, 8, 12, 13]},
                "up": {"texture": "#top", "uv": [4, 4, 12, 12]},
            }),
            # Lens cradle
            box([3, 8, 3], [13, 10, 13], {
                "north": {"texture": "#base", "uv": [3, 6, 13, 8]},
                "south": {"texture": "#base", "uv": [3, 6, 13, 8]},
                "west": {"texture": "#base", "uv": [3, 6, 13, 8]},
                "east": {"texture": "#base", "uv": [3, 6, 13, 8]},
                "down": {"texture": "#bottom", "uv": [3, 3, 13, 13]},
                "up": {"texture": "#top", "uv": [3, 3, 13, 13]},
            }),
            # Crystal lens (emissive)
            box([5, 10, 5], [11, 12, 11], {
                "north": {"texture": "#lens", "uv": [5, 4, 11, 6]},
                "south": {"texture": "#lens", "uv": [5, 4, 11, 6]},
                "west": {"texture": "#lens", "uv": [5, 4, 11, 6]},
                "east": {"texture": "#lens", "uv": [5, 4, 11, 6]},
                "up": {"texture": "#lens", "uv": [5, 5, 11, 11]},
            }, light_emission=light),
        ]
        # Four struts rising from the corners of the base, each topped with a small finial
        for (x, z) in [(1.5, 1.5), (12.5, 1.5), (1.5, 12.5), (12.5, 12.5)]:
            elements.append(box([x, 3, z], [x + 2, 11, z + 2], {
                "north": {"texture": "#strut", "uv": [0, 5, 2, 13]},
                "south": {"texture": "#strut", "uv": [0, 5, 2, 13]},
                "west": {"texture": "#strut", "uv": [0, 5, 2, 13]},
                "east": {"texture": "#strut", "uv": [0, 5, 2, 13]},
                "up": {"texture": "#strut", "uv": [0, 0, 2, 2]},
            }))
            elements.append(box([x + 0.5, 11, z + 0.5], [x + 1.5, 12.5, z + 1.5], {
                "north": {"texture": "#lens", "uv": [0, 0, 1, 1.5]},
                "south": {"texture": "#lens", "uv": [0, 0, 1, 1.5]},
                "west": {"texture": "#lens", "uv": [0, 0, 1, 1.5]},
                "east": {"texture": "#lens", "uv": [0, 0, 1, 1.5]},
                "up": {"texture": "#lens", "uv": [0, 0, 1, 1]},
            }, light_emission=light))
        # A front dial that shows which way the projector faces
        elements.append(box([6, 4, 3.5], [10, 7, 4], {
            "north": {"texture": "#dial", "uv": [0, 0, 16, 16]},
        }, light_emission=6 if suffix == "" else 12))
        write(asset("models", "block", f"echo_projector{suffix}.json"), {
            "parent": "minecraft:block/block",
            "ambientocclusion": False,
            "textures": {
                "particle": ns("block/echo_projector_side"),
                "base": ns("block/echo_projector_base"),
                "side": ns("block/echo_projector_side"),
                "top": ns("block/echo_projector_top"),
                "bottom": ns("block/echo_projector_bottom"),
                "strut": ns("block/echo_projector_strut"),
                "lens": lens,
                "dial": ns("block/echo_projector_dial"),
            },
            "elements": elements,
            "display": {
                "gui": {"rotation": [30, 225, 0], "translation": [0, 1, 0], "scale": [0.7, 0.7, 0.7]},
                "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
                "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
                "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
                "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
                "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
            },
        })


# ---------------------------------------------------------------- items

GENERATED_ITEMS = [
    "echo_shard", "echo_dust", "ancient_coin", "wyrmscale", "wyrm_heart", "heirloom_locket", "festival_charm",
    "tarnished_crown", "wyrmscale_helmet", "wyrmscale_chestplate", "wyrmscale_leggings", "wyrmscale_boots",
    "music_disc_echoes", "lingerer_spawn_egg", "memory_moth_spawn_egg", "echo_wyrm_spawn_egg", "echo_lantern",
]
SETS = {
    "spectral_knight": ["helmet", "chestplate", "leggings", "boots"],
    "cindersteel": ["helmet", "chestplate", "leggings", "boots"],
    "colossus": ["helmet", "chestplate", "leggings", "boots"],
    "dawnweave": ["hood", "robe", "leggings", "slippers"],
}
ARMOR_PIECES = [f"{k}_{p}" for k, v in SETS.items() for p in v]
NEW_MOBS = ["echo_knight", "spectral_archer", "ash_revenant", "dawn_wisp", "shard_crawler", "hollow_king", "siege_colossus", "hierophant"]
KEYSTONES = ["keystone_of_crowns", "keystone_of_iron", "keystone_of_dragons", "keystone_of_dawn"]
GENERATED_ITEMS += [
    "spectral_plate", "revenant_ash", "cindersteel_ingot", "colossus_plating", "colossus_core", "dawnstone", "royal_sigil",
    "hollow_crown", "guide_book", "showcase_book",
] + ARMOR_PIECES + KEYSTONES + [f"{m}_spawn_egg" for m in NEW_MOBS]
HANDHELD_ITEMS = ["archaeologist_chisel", "legionnaire_blade", "echoing_blade", "spectral_longsword", "ashen_cleaver", "crownbreaker", "siegebreaker", "dawn_staff"]
BLOCK_ITEMS = ["echo_deposit", "deepslate_echo_deposit", "echo_crystal_block", "echo_projector"]


def items():
    for name in GENERATED_ITEMS:
        write(asset("models", "item", f"{name}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": ns(f"item/{name}")}})
        write(asset("items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": ns(f"item/{name}")}})
    for name in HANDHELD_ITEMS:
        write(asset("models", "item", f"{name}.json"), {"parent": "minecraft:item/handheld", "textures": {"layer0": ns(f"item/{name}")}})
        write(asset("items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": ns(f"item/{name}")}})
    for name in BLOCK_ITEMS:
        write(asset("items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": ns(f"block/{name}")}})
    for name in ["relic_cache_soil", "relic_cache_stone"]:
        write(asset("items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": ns(f"block/{name}_0")}})
    write(asset("items", "echo_block.json"), {"model": {"type": "minecraft:model", "model": ns("block/echo_block")}})

    # The resonance compass spins until it hears a deposit, then points at it.
    frames = []
    for i in range(32):
        write(asset("models", "item", f"resonance_compass_{i:02d}.json"), {
            "parent": "minecraft:item/generated", "textures": {"layer0": ns(f"item/resonance_compass_{i:02d}")}})
    order = [16 + i if 16 + i < 32 else i - 16 for i in range(32)]
    for idx, frame in enumerate(order):
        frames.append({"model": {"type": "minecraft:model", "model": ns(f"item/resonance_compass_{frame:02d}")}, "threshold": 0.0 if idx == 0 else idx - 0.5})
    frames.append({"model": {"type": "minecraft:model", "model": ns("item/resonance_compass_16")}, "threshold": 31.5})
    write(asset("items", "resonance_compass.json"), {"model": {
        "type": "minecraft:condition",
        "property": "minecraft:has_component",
        "component": "minecraft:lodestone_tracker",
        "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:compass", "scale": 32.0, "target": "lodestone", "entries": frames},
        "on_false": {"type": "minecraft:range_dispatch", "property": "minecraft:compass", "scale": 32.0, "target": "none", "entries": frames},
    }})

    # Equipment assets
    write(asset("equipment", "wyrmscale.json"), {"layers": {
        "humanoid": [{"texture": ns("wyrmscale")}],
        "humanoid_leggings": [{"texture": ns("wyrmscale")}],
    }})
    write(asset("equipment", "tarnished_crown.json"), {"layers": {
        "humanoid": [{"texture": ns("tarnished_crown")}],
    }})
    for name in SETS:
        write(asset("equipment", f"{name}.json"), {"layers": {
            "humanoid": [{"texture": ns(name)}],
            "humanoid_leggings": [{"texture": ns(name)}],
        }})
    write(asset("equipment", "hollow_crown.json"), {"layers": {
        "humanoid": [{"texture": ns("hollow_crown")}],
    }})


# ---------------------------------------------------------------- sounds

def vanilla(name, pitch=1.0, volume=1.0, **extra):
    entry = {"name": name}
    if pitch != 1.0:
        entry["pitch"] = pitch
    if volume != 1.0:
        entry["volume"] = volume
    entry.update(extra)
    return entry


def custom(name, **extra):
    entry = {"name": ns(name)}
    entry.update(extra)
    return entry


def sounds():
    s = {
        "block.echo_projector.activate": [custom("projector/activate")],
        "block.echo_projector.hum": [custom("projector/hum", volume=0.6)],
        "block.echo_projector.insert": [vanilla("block/amethyst/place1", 0.7), vanilla("block/amethyst/place2", 0.7)],
        "replay.end": [custom("replay/end")],
        "block.echo_deposit.chime": [custom("echo/chime1", volume=0.5), custom("echo/chime2", volume=0.5), custom("echo/chime3", volume=0.5)],
        "item.chisel.tap": [vanilla("block/amethyst/step1", 1.6, 0.8), vanilla("block/amethyst/step2", 1.6, 0.8), vanilla("block/amethyst/step3", 1.5, 0.8)],
        "item.chisel.extract": [custom("echo/extract")],
        "replay.relic_reveal": [custom("replay/relic_reveal")],
        "replay.rift_open": [custom("replay/rift_open")],
        "replay.rift_warning": [custom("replay/rift_warning")],
        "item.resonance_compass.ping": [custom("echo/ping")],
        "replay.horn": [custom("replay/horn")],
        "replay.clash": [vanilla("random/anvil_land", 1.8, 0.35), vanilla("item/shield/block1", 1.2, 0.6), vanilla("item/shield/block2", 1.3, 0.6)],
        "replay.roar": [vanilla("mob/enderdragon/growl1", 0.7, 0.7), vanilla("mob/enderdragon/growl2", 0.75, 0.7), vanilla("mob/enderdragon/growl3", 0.7, 0.7)],
        "replay.cheer": [custom("replay/cheer")],
        "replay.bell": [vanilla("block/bell/bell_use01", 0.8, 0.7), vanilla("block/bell/bell_use02", 0.8, 0.7)],
        "replay.chant": [custom("replay/chant")],
        "replay.rumble": [vanilla("ambient/cave/cave13", 0.6), vanilla("random/explode1", 0.5, 0.4)],
        "replay.fire": [vanilla("mob/ghast/fireball4", 0.6, 0.6), vanilla("fire/ignite", 0.7, 0.8)],
        "replay.fanfare": [custom("replay/fanfare")],
        "replay.music": [custom("replay/music")],
        "replay.whisper": [custom("replay/whisper1"), custom("replay/whisper2")],
        "entity.lingerer.ambient": [custom("lingerer/ambient1"), custom("lingerer/ambient2")],
        "entity.lingerer.hurt": [vanilla("mob/zombie/hurt1", 1.4, 0.7), vanilla("mob/zombie/hurt2", 1.4, 0.7)],
        "entity.lingerer.death": [custom("lingerer/death")],
        "entity.lingerer.phase": [custom("lingerer/phase")],
        "entity.memory_moth.flutter": [vanilla("mob/allay/idle_without_item1", 1.6, 0.4), vanilla("mob/allay/idle_without_item2", 1.7, 0.4)],
        "entity.memory_moth.attune": [custom("echo/attune")],
        "entity.echo_wyrm.roar": [custom("wyrm/roar")],
        "entity.echo_wyrm.breath": [vanilla("mob/enderdragon/growl4", 1.3, 0.5), vanilla("fire/fire", 0.6, 1.0)],
        "entity.echo_wyrm.hurt": [vanilla("mob/enderdragon/hit1", 0.8), vanilla("mob/enderdragon/hit2", 0.8), vanilla("mob/enderdragon/hit3", 0.8)],
        "entity.echo_wyrm.death": [custom("wyrm/death")],
        "entity.echo_wyrm.flap": [vanilla("mob/enderdragon/wings1", 1.1, 0.7), vanilla("mob/enderdragon/wings3", 1.1, 0.7)],
        "music_disc.echoes": [custom("music/echoes", stream=True)],
        "entity.hollow_king.ambient": [custom("lingerer/ambient1", pitch=0.55), custom("lingerer/ambient2", pitch=0.55)],
        "entity.hollow_king.hurt": [vanilla("mob/zombie/hurt1", 0.6), vanilla("mob/zombie/hurt2", 0.6)],
        "entity.hollow_king.death": [custom("lingerer/death", pitch=0.55)],
        "entity.hollow_king.cleave": [vanilla("entity/player/attack/sweep1", 0.6), vanilla("entity/player/attack/sweep2", 0.6)],
        "entity.hollow_king.decree": [custom("boss/king_decree")],
        "entity.hollow_king.roar": [custom("wyrm/roar", pitch=1.4)],
        "entity.siege_colossus.groan": [custom("boss/colossus_groan")],
        "entity.siege_colossus.hurt": [vanilla("mob/irongolem/hit1", 0.55), vanilla("mob/irongolem/hit2", 0.55), vanilla("mob/irongolem/hit3", 0.55)],
        "entity.siege_colossus.death": [vanilla("mob/irongolem/death", 0.45)],
        "entity.siege_colossus.slam": [custom("boss/colossus_slam")],
        "entity.siege_colossus.step": [vanilla("mob/irongolem/walk1", 0.55), vanilla("mob/irongolem/walk2", 0.55)],
        "entity.siege_colossus.mortar": [vanilla("random/explode1", 0.8, 0.8), vanilla("random/explode2", 0.8, 0.8)],
        "entity.hierophant.ambient": [custom("replay/chant", pitch=1.3, volume=0.5)],
        "entity.hierophant.hurt": [vanilla("mob/allay/hurt1", 0.6), vanilla("mob/allay/hurt2", 0.6)],
        "entity.hierophant.death": [custom("boss/hierophant_cast", pitch=0.5)],
        "entity.hierophant.cast": [custom("boss/hierophant_cast")],
        "entity.hierophant.lance": [custom("boss/hierophant_lance")],
        "entity.hierophant.ward_hit": [vanilla("block/amethyst/place1", 1.6), vanilla("block/amethyst/place2", 1.6)],
        "entity.hierophant.ward_break": [vanilla("random/glass1", 0.8), vanilla("random/glass2", 0.8)],
        "entity.hierophant.starfall": [custom("boss/starfall")],
        "entity.echo_knight.ambient": [custom("lingerer/ambient2", pitch=0.8)],
        "entity.echo_knight.hurt": [vanilla("mob/zombie/hurt1", 1.1, 0.7)],
        "entity.echo_knight.block": [vanilla("item/shield/block1", 1.1), vanilla("item/shield/block2", 1.1)],
        "entity.echo_knight.charge": [custom("replay/horn", pitch=1.5, volume=0.4)],
        "entity.echo_knight.step": [vanilla("mob/irongolem/walk3", 1.8, 0.3)],
        "entity.ash_revenant.ambient": [vanilla("mob/blaze/breathe1", 0.7), vanilla("mob/blaze/breathe2", 0.7)],
        "entity.ash_revenant.hurt": [vanilla("mob/blaze/hit1", 0.8), vanilla("mob/blaze/hit2", 0.8)],
        "entity.ash_revenant.death": [vanilla("mob/blaze/death", 0.7)],
        "entity.ash_revenant.burst": [vanilla("mob/ghast/fireball4", 0.8)],
        "entity.dawn_wisp.ambient": [vanilla("mob/allay/idle_without_item1", 0.8, 0.6), vanilla("mob/allay/idle_without_item2", 0.8, 0.6)],
        "entity.dawn_wisp.hurt": [vanilla("mob/allay/hurt1", 1.2)],
        "entity.dawn_wisp.death": [vanilla("mob/allay/death1", 1.0)],
        "entity.shard_crawler.ambient": [vanilla("mob/silverfish/say1", 0.7), vanilla("mob/silverfish/say2", 0.7)],
        "entity.shard_crawler.hurt": [vanilla("mob/silverfish/hit1", 0.7), vanilla("mob/silverfish/hit2", 0.7)],
        "entity.shard_crawler.death": [vanilla("mob/silverfish/kill", 0.7)],
        "entity.shard_crawler.step": [vanilla("mob/silverfish/step1", 0.7), vanilla("mob/silverfish/step2", 0.7)],
        "entity.shard_crawler.burrow": [vanilla("dig/stone1", 0.7), vanilla("dig/stone2", 0.7)],
        "combat.bolt.fire": [custom("combat/bolt_fire")],
        "combat.bolt.hit": [vanilla("block/amethyst/place1", 1.4), vanilla("block/amethyst/place2", 1.4)],
        "boss.manifest": [custom("boss/manifest")],
        "showcase.build": [custom("showcase/build")],
    }
    out = {}
    for key, entries in s.items():
        out[key] = {"sounds": entries, "subtitle": f"subtitles.{NS}.{key}"}
    write(asset("sounds.json"), out)


# ---------------------------------------------------------------- loot tables

def item_entry(name, count=None, weight=None, extra_modifiers=None):
    entry = {"type": "minecraft:item", "name": name}
    modifiers = []
    if count is not None:
        if isinstance(count, tuple):
            modifiers.append({"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": count[0], "max": count[1]}})
        else:
            modifiers.append({"type": "minecraft:set_count", "count": count})
    if extra_modifiers:
        modifiers.extend(extra_modifiers)
    if modifiers:
        entry["modifier"] = modifiers
    if weight is not None:
        entry["weight"] = weight
    return entry


def loot():
    lt = lambda *p: data(NS, "loot_table", *p)
    for ore in ["echo_deposit", "deepslate_echo_deposit"]:
        write(lt("blocks", f"{ore}.json"), {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "entries": [{
                "type": "minecraft:alternatives",
                "children": [
                    {"type": "minecraft:item", "condition": "minecraft:tool/can_silk_touch", "name": ns(ore)},
                    {"type": "minecraft:item", "name": ns("echo_shard"), "modifier": [
                        {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}},
                        {"type": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
                        {"type": "minecraft:explosion_decay"},
                    ]},
                ],
            }]}],
            "random_sequence": ns(f"blocks/{ore}"),
        })
    for block in ["echo_crystal_block", "echo_projector", "echo_lantern"]:
        write(lt("blocks", f"{block}.json"), {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": ns(block)}]}],
            "random_sequence": ns(f"blocks/{block}"),
        })

    looting = {"type": "minecraft:enchanted_count_increase", "count": {"type": "minecraft:uniform", "min": 0.0, "max": 1.0}, "enchantment": "minecraft:looting"}
    killed_by_player = {"type": "minecraft:killed_by_player"}
    write(lt("entities", "lingerer.json"), {
        "type": "minecraft:entity",
        "pools": [
            {"rolls": 1, "entries": [item_entry(ns("echo_dust"), (0, 2), extra_modifiers=[looting])]},
            {"rolls": 1, "condition": {"type": "minecraft:all_of", "terms": [killed_by_player, {"type": "minecraft:random_chance", "chance": 0.12}]},
             "entries": [item_entry(ns("ancient_coin"), (1, 2))]},
            {"rolls": 1, "condition": {"type": "minecraft:all_of", "terms": [killed_by_player, {"type": "minecraft:random_chance", "chance": 0.03}]},
             "entries": [item_entry(ns("heirloom_locket"))]},
        ],
        "random_sequence": ns("entities/lingerer"),
    })
    write(lt("entities", "memory_moth.json"), {
        "type": "minecraft:entity",
        "pools": [{"rolls": 1, "entries": [item_entry(ns("echo_dust"), (0, 1))]}],
        "random_sequence": ns("entities/memory_moth"),
    })
    write(lt("entities", "echo_wyrm.json"), {
        "type": "minecraft:entity",
        "pools": [
            {"rolls": 1, "entries": [item_entry(ns("wyrm_heart"))]},
            {"rolls": 1, "entries": [item_entry(ns("wyrmscale"), (6, 10), extra_modifiers=[looting])]},
            {"rolls": 1, "entries": [item_entry(ns("echo_shard"), (4, 8))]},
            {"rolls": 1, "entries": [item_entry(ns("music_disc_echoes"))]},
        ],
        "random_sequence": ns("entities/echo_wyrm"),
    })
    write(lt("entities", "echo_figure.json"), {"type": "minecraft:entity", "pools": []})

    def entity_table(name, pools):
        write(lt("entities", f"{name}.json"), {"type": "minecraft:entity", "pools": pools, "random_sequence": ns(f"entities/{name}")})

    def chance(p):
        return {"type": "minecraft:all_of", "terms": [killed_by_player, {"type": "minecraft:random_chance", "chance": p}]}

    entity_table("echo_knight", [
        {"rolls": 1, "condition": killed_by_player, "entries": [item_entry(ns("spectral_plate"), (0, 2), extra_modifiers=[looting])]},
        {"rolls": 1, "entries": [item_entry(ns("echo_dust"), (0, 1))]},
        {"rolls": 1, "condition": chance(0.04), "entries": [item_entry(ns("spectral_longsword"))]},
    ])
    entity_table("spectral_archer", [
        {"rolls": 1, "entries": [item_entry(ns("echo_dust"), (0, 2), extra_modifiers=[looting])]},
        {"rolls": 1, "entries": [item_entry("minecraft:arrow", (0, 3))]},
        {"rolls": 1, "condition": chance(0.3), "entries": [item_entry(ns("spectral_plate"))]},
    ])
    entity_table("ash_revenant", [
        {"rolls": 1, "entries": [item_entry(ns("revenant_ash"), (1, 3), extra_modifiers=[looting])]},
        {"rolls": 1, "entries": [item_entry("minecraft:coal", (0, 2))]},
        {"rolls": 1, "condition": chance(0.05), "entries": [item_entry(ns("cindersteel_ingot"))]},
    ])
    entity_table("dawn_wisp", [
        {"rolls": 1, "entries": [item_entry(ns("echo_dust"), (0, 2))]},
        {"rolls": 1, "entries": [item_entry("minecraft:glowstone_dust", (0, 2), extra_modifiers=[looting])]},
        {"rolls": 1, "condition": chance(0.06), "entries": [item_entry(ns("dawnstone"))]},
    ])
    entity_table("shard_crawler", [
        {"rolls": 1, "entries": [item_entry(ns("echo_shard"), (1, 2), extra_modifiers=[looting])]},
    ])
    entity_table("hollow_king", [
        {"rolls": 1, "entries": [item_entry(ns("crownbreaker"))]},
        {"rolls": 1, "entries": [item_entry(ns("hollow_crown"))]},
        {"rolls": 1, "entries": [item_entry(ns("royal_sigil"))]},
        {"rolls": 1, "entries": [item_entry(ns("ancient_coin"), (8, 16), extra_modifiers=[looting])]},
        {"rolls": 1, "entries": [item_entry(ns("spectral_plate"), (6, 10))]},
        {"rolls": 1, "entries": [item_entry("minecraft:gold_ingot", (4, 8))]},
    ])
    entity_table("siege_colossus", [
        {"rolls": 1, "entries": [item_entry(ns("siegebreaker"))]},
        {"rolls": 1, "entries": [item_entry(ns("colossus_core"))]},
        {"rolls": 1, "entries": [item_entry(ns("colossus_plating"), (16, 24), extra_modifiers=[looting])]},
        {"rolls": 1, "entries": [item_entry(ns("cindersteel_ingot"), (4, 8))]},
        {"rolls": 1, "entries": [item_entry("minecraft:iron_block", (2, 4))]},
    ])
    entity_table("hierophant", [
        {"rolls": 1, "entries": [item_entry(ns("dawn_staff"))]},
        {"rolls": 1, "entries": [item_entry(ns("dawnstone"), (10, 14), extra_modifiers=[looting])]},
        {"rolls": 1, "entries": [item_entry(ns("echo_crystal_block"), (2, 4))]},
        {"rolls": 1, "entries": [item_entry("minecraft:experience_bottle", (4, 8))]},
    ])

    # Relics, one table per kind of event. Brushing a cache yields a single roll.
    coin = ns("ancient_coin")
    relics = {
        "battle": [(ns("legionnaire_blade"), 3, None), (coin, 6, (2, 5)), ("minecraft:iron_sword", 2, None), ("minecraft:shield", 2, None), (ns("heirloom_locket"), 2, None), ("minecraft:arrow", 3, (4, 12))],
        "siege": [(coin, 6, (3, 7)), (ns("legionnaire_blade"), 2, None), ("minecraft:gold_ingot", 3, (1, 3)), ("minecraft:crossbow", 1, None), (ns("heirloom_locket"), 2, None)],
        "dragon_attack": [(ns("wyrmscale"), 6, (1, 3)), ("minecraft:bone", 3, (2, 5)), (coin, 3, (1, 3)), ("minecraft:gold_nugget", 3, (3, 9)), (ns("echo_shard"), 2, (1, 2))],
        "market_day": [(coin, 10, (3, 8)), ("minecraft:emerald", 5, (1, 4)), (ns("festival_charm"), 2, None), ("minecraft:glass_bottle", 2, (1, 3)), ("minecraft:bundle", 1, None)],
        "coronation": [(ns("tarnished_crown"), 2, None), (coin, 6, (3, 8)), ("minecraft:gold_ingot", 4, (1, 4)), ("minecraft:emerald", 3, (1, 3)), ("minecraft:amethyst_shard", 3, (1, 4))],
        "ritual": [(ns("echo_shard"), 5, (2, 4)), ("minecraft:amethyst_shard", 4, (2, 5)), ("minecraft:ender_pearl", 2, None), ("minecraft:experience_bottle", 3, (1, 3)), (coin, 2, (1, 3))],
        "duel": [(ns("legionnaire_blade"), 4, None), (coin, 5, (1, 4)), ("minecraft:iron_ingot", 3, (1, 3)), (ns("heirloom_locket"), 1, None)],
        "festival": [(ns("festival_charm"), 6, (1, 2)), (coin, 4, (1, 4)), ("minecraft:cake", 1, None), ("minecraft:firework_rocket", 3, (2, 5)), ("minecraft:note_block", 1, None)],
        "exodus": [(ns("heirloom_locket"), 6, None), (coin, 6, (2, 6)), ("minecraft:lantern", 2, None), ("minecraft:iron_ingot", 2, (1, 2)), ("minecraft:bread", 2, (1, 3))],
        "cave_in": [("minecraft:raw_iron", 4, (2, 5)), ("minecraft:raw_gold", 3, (1, 4)), ("minecraft:iron_pickaxe", 2, None), (coin, 3, (1, 3)), ("minecraft:lantern", 2, None), (ns("echo_shard"), 2, (1, 2))],
    }
    for kind, entries in relics.items():
        write(lt("relics", f"{kind}.json"), {
            "type": "minecraft:archaeology",
            "pools": [{"rolls": 1, "entries": [item_entry(name, count, weight) for name, weight, count in entries]}],
            "random_sequence": ns(f"relics/{kind}"),
        })


# ---------------------------------------------------------------- recipes

def shaped(name, pattern, key, result, count=1, category="misc"):
    obj = {"type": "minecraft:crafting_shaped", "category": category, "key": key, "pattern": pattern, "result": {"id": result}}
    if count != 1:
        obj["result"]["count"] = count
    write(data(NS, "recipe", f"{name}.json"), obj)


def shapeless(name, ingredients, result, count=1, category="misc"):
    obj = {"type": "minecraft:crafting_shapeless", "category": category, "ingredients": ingredients, "result": {"id": result}}
    if count != 1:
        obj["result"]["count"] = count
    write(data(NS, "recipe", f"{name}.json"), obj)


RECIPES = []


def recipes():
    shard = ns("echo_shard")
    shaped("echo_projector", [" G ", "SAS", "CCC"], {"G": "minecraft:glass", "S": shard, "A": "minecraft:amethyst_shard", "C": "minecraft:copper_ingot"}, ns("echo_projector"))
    shaped("archaeologist_chisel", ["I", "C", "S"], {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot", "S": "minecraft:stick"}, ns("archaeologist_chisel"), category="equipment")
    shaped("resonance_compass", [" S ", "SCS", " S "], {"S": shard, "C": "minecraft:compass"}, ns("resonance_compass"), category="equipment")
    shaped("echo_crystal_block", ["##", "##"], {"#": shard}, ns("echo_crystal_block"), category="building")
    shapeless("echo_shard_from_block", [ns("echo_crystal_block")], shard, count=4)
    shaped("echo_lantern", ["XXX", "X#X", "XXX"], {"X": "minecraft:iron_nugget", "#": shard}, ns("echo_lantern"))
    shaped("echoing_blade", ["W", "H", "S"], {"W": ns("wyrmscale"), "H": ns("wyrm_heart"), "S": "minecraft:stick"}, ns("echoing_blade"), category="equipment")
    w = {"W": ns("wyrmscale")}
    shaped("wyrmscale_helmet", ["WWW", "W W"], w, ns("wyrmscale_helmet"), category="equipment")
    shaped("wyrmscale_chestplate", ["W W", "WWW", "WWW"], w, ns("wyrmscale_chestplate"), category="equipment")
    shaped("wyrmscale_leggings", ["WWW", "W W", "W W"], w, ns("wyrmscale_leggings"), category="equipment")
    shaped("wyrmscale_boots", ["W W", "W W"], w, ns("wyrmscale_boots"), category="equipment")
    shapeless("heirloom_locket", [ns("ancient_coin"), "minecraft:gold_nugget", "minecraft:string", ns("echo_dust")], ns("heirloom_locket"))
    shapeless("festival_charm", [ns("ancient_coin"), ns("echo_dust"), "minecraft:glow_berries"], ns("festival_charm"))
    def armor_set(name, pieces, material):
        m = {"M": material}
        patterns = [["MMM", "M M"], ["M M", "MMM", "MMM"], ["MMM", "M M", "M M"], ["M M", "M M"]]
        for piece, pattern in zip(pieces, patterns):
            shaped(f"{name}_{piece}", pattern, m, ns(f"{name}_{piece}"), category="equipment")
            RECIPES.append(f"{name}_{piece}")

    armor_set("spectral_knight", SETS["spectral_knight"], ns("spectral_plate"))
    armor_set("cindersteel", SETS["cindersteel"], ns("cindersteel_ingot"))
    armor_set("colossus", SETS["colossus"], ns("colossus_plating"))
    dw = {"D": ns("dawnstone"), "W": "minecraft:white_wool"}
    shaped("dawnweave_hood", ["DWD", "W W"], dw, ns("dawnweave_hood"), category="equipment")
    shaped("dawnweave_robe", ["W W", "DWD", "WDW"], dw, ns("dawnweave_robe"), category="equipment")
    shaped("dawnweave_leggings", ["DWD", "W W", "W W"], dw, ns("dawnweave_leggings"), category="equipment")
    shaped("dawnweave_slippers", ["D D", "W W"], dw, ns("dawnweave_slippers"), category="equipment")
    shaped("spectral_longsword", ["P", "P", "S"], {"P": ns("spectral_plate"), "S": "minecraft:stick"}, ns("spectral_longsword"), category="equipment")
    shapeless("cindersteel_ingot", [ns("revenant_ash")] * 4 + ["minecraft:iron_ingot"], ns("cindersteel_ingot"))
    shaped("ashen_cleaver", ["II", "IS", " S"], {"I": ns("cindersteel_ingot"), "S": "minecraft:stick"}, ns("ashen_cleaver"), category="equipment")
    shaped("keystone_of_crowns", [" C ", "GEG", " G "], {"C": ns("tarnished_crown"), "G": "minecraft:gold_ingot", "E": ns("echo_crystal_block")}, ns("keystone_of_crowns"))
    shaped("keystone_of_iron", [" L ", "IEI", " N "], {"L": ns("legionnaire_blade"), "I": "minecraft:iron_block", "N": ns("cindersteel_ingot"), "E": ns("echo_crystal_block")}, ns("keystone_of_iron"))
    shaped("keystone_of_dragons", [" W ", "WEW", " W "], {"W": ns("wyrmscale"), "E": ns("echo_crystal_block")}, ns("keystone_of_dragons"))
    shaped("keystone_of_dawn", [" H ", "AEA", " S "], {"H": ns("wyrm_heart"), "A": "minecraft:amethyst_shard", "S": ns("spectral_plate"), "E": ns("echo_crystal_block")}, ns("keystone_of_dawn"))
    shapeless("guide_book", ["minecraft:book", shard], ns("guide_book"))
    RECIPES.extend(["dawnweave_hood", "dawnweave_robe", "dawnweave_leggings", "dawnweave_slippers", "spectral_longsword", "cindersteel_ingot",
                    "ashen_cleaver", "keystone_of_crowns", "keystone_of_iron", "keystone_of_dragons", "keystone_of_dawn", "guide_book"])
    RECIPES.extend([
        "echo_projector", "archaeologist_chisel", "resonance_compass", "echo_crystal_block", "echo_shard_from_block", "echo_lantern",
        "echoing_blade", "wyrmscale_helmet", "wyrmscale_chestplate", "wyrmscale_leggings", "wyrmscale_boots", "heirloom_locket", "festival_charm",
    ])


# ---------------------------------------------------------------- tags

def tags():
    t = lambda namespace, *p: data(namespace, "tags", *p)
    write(t(NS, "block", "echo_deposits.json"), {"values": [ns("echo_deposit"), ns("deepslate_echo_deposit")]})
    write(t("minecraft", "block", "mineable", "pickaxe.json"), {"replace": False, "values": [
        ns("echo_deposit"), ns("deepslate_echo_deposit"), ns("echo_crystal_block"), ns("echo_projector"), ns("echo_lantern"), ns("relic_cache_stone")]})
    write(t("minecraft", "block", "mineable", "shovel.json"), {"replace": False, "values": [ns("relic_cache_soil")]})
    write(t("minecraft", "block", "needs_iron_tool.json"), {"replace": False, "values": [ns("echo_deposit"), ns("deepslate_echo_deposit")]})

    write(t(NS, "item", "spectral_weapons.json"), {"values": [ns("echoing_blade"), ns("legionnaire_blade"), ns("spectral_longsword"), ns("crownbreaker"), ns("dawn_staff")]})
    write(t(NS, "item", "repairs_spectral.json"), {"values": [ns("spectral_plate")]})
    write(t(NS, "item", "repairs_cindersteel.json"), {"values": [ns("cindersteel_ingot")]})
    write(t(NS, "item", "repairs_royal.json"), {"values": ["minecraft:gold_ingot", ns("royal_sigil")]})
    write(t(NS, "item", "repairs_colossus.json"), {"values": [ns("colossus_plating")]})
    write(t(NS, "item", "repairs_dawnweave.json"), {"values": [ns("dawnstone")]})
    write(t(NS, "item", "repairs_wyrmscale.json"), {"values": [ns("wyrmscale")]})
    write(t(NS, "item", "repairs_relic.json"), {"values": [ns("ancient_coin")]})
    write(t(NS, "item", "crowns.json"), {"values": [ns("tarnished_crown"), ns("hollow_crown")]})
    write(t("minecraft", "item", "swords.json"), {"replace": False, "values": [ns(n) for n in ["legionnaire_blade", "echoing_blade", "spectral_longsword", "ashen_cleaver", "crownbreaker", "siegebreaker"]]})
    slot = lambda i: [ns(f"{k}_{v[i]}") for k, v in SETS.items()]
    write(t("minecraft", "item", "head_armor.json"), {"replace": False, "values": [ns("wyrmscale_helmet"), ns("tarnished_crown"), ns("hollow_crown")] + slot(0)})
    write(t("minecraft", "item", "chest_armor.json"), {"replace": False, "values": [ns("wyrmscale_chestplate")] + slot(1)})
    write(t("minecraft", "item", "leg_armor.json"), {"replace": False, "values": [ns("wyrmscale_leggings")] + slot(2)})
    write(t("minecraft", "item", "foot_armor.json"), {"replace": False, "values": [ns("wyrmscale_boots")] + slot(3)})

    write(t("minecraft", "entity_type", "undead.json"), {"replace": False, "values": [ns(n) for n in ["lingerer", "echo_knight", "spectral_archer", "ash_revenant", "hollow_king"]]})
    write(t("minecraft", "entity_type", "fall_damage_immune.json"), {"replace": False, "values": [ns(n) for n in ["memory_moth", "echo_wyrm", "echo_figure", "dawn_wisp", "hierophant"]]})


# ---------------------------------------------------------------- worldgen

def worldgen():
    def rule(tag):
        return {"predicate_type": "minecraft:tag_match", "tag": tag}

    write(data(NS, "worldgen", "feature", "echo_deposits.json"), {
        "type": "minecraft:ore",
        "discard_chance_on_air_exposure": 0.25,
        "size": 5,
        "targets": [
            {"state": ns("echo_deposit"), "target": rule("minecraft:stone_ore_replaceables")},
            {"state": ns("deepslate_echo_deposit"), "target": rule("minecraft:deepslate_ore_replaceables")},
        ],
    })
    write(data(NS, "worldgen", "placed_feature", "echo_deposits.json"), {
        "feature": ns("echo_deposits"),
        "placement": [
            {"type": "minecraft:count", "count": 4},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": -16}, "max_inclusive": {"absolute": 96}}},
            {"type": "minecraft:biome"},
        ],
    })
    write(data(NS, "worldgen", "placed_feature", "echo_deposits_deep.json"), {
        "feature": ns("echo_deposits"),
        "placement": [
            {"type": "minecraft:count", "count": 5},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid", "min_inclusive": {"above_bottom": 0}, "max_inclusive": {"absolute": 16}}},
            {"type": "minecraft:biome"},
        ],
    })

    write(data(NS, "jukebox_song", "echoes.json"), {
        "comparator_output": 11,
        "description": {"translate": f"jukebox_song.{NS}.echoes"},
        "length_in_seconds": 101.0,
        "sound_event": ns("music_disc.echoes"),
    })


# ---------------------------------------------------------------- advancements

def advancement(name, parent, icon, frame, criteria, requirements=None, rewards=None, hidden=False, toast=True, chat=True, background=None):
    obj = {}
    if parent:
        obj["parent"] = parent
    display = {
        "icon": {"id": icon},
        "title": {"translate": f"advancements.{NS}.{name}.title"},
        "description": {"translate": f"advancements.{NS}.{name}.description"},
        "frame": frame,
        "show_toast": toast,
        "announce_to_chat": chat,
        "hidden": hidden,
    }
    if background:
        display["background"] = background
    obj["display"] = display
    obj["criteria"] = criteria
    obj["requirements"] = requirements or [[k] for k in criteria]
    if rewards:
        obj["rewards"] = rewards
    write(data(NS, "advancement", f"{name}.json"), obj)


def impossible():
    return {"trigger": "minecraft:impossible"}


def has_items(*names):
    return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": n} for n in names]}}


def advancements():
    advancement("root", None, ns("echo_projector"), "task",
                {"has_shard": has_items(ns("echo_shard")), "has_copper": has_items("minecraft:copper_ingot")},
                requirements=[["has_shard", "has_copper"]],
                rewards={"recipes": [ns(r) for r in RECIPES if r not in ("echoing_blade",) and not r.startswith("wyrmscale")]},
                toast=False, chat=False, background=f"{NS}:gui/advancements/backgrounds/echoes")
    advancement("careful_hands", ns("root"), ns("archaeologist_chisel"), "task", {"extracted": impossible()})
    advancement("witness_replay", ns("careful_hands"), ns("echo_block"), "task", {"witnessed": impossible()})
    advancement("where_it_happened", ns("witness_replay"), "minecraft:filled_map", "goal", {"resonant": impossible()})
    advancement("unearthed", ns("where_it_happened"), "minecraft:brush", "task",
                {n: has_items(ns(n)) for n in ["legionnaire_blade", "tarnished_crown", "heirloom_locket", "festival_charm", "ancient_coin", "wyrmscale"]},
                requirements=[["legionnaire_blade", "tarnished_crown", "heirloom_locket", "festival_charm", "ancient_coin", "wyrmscale"]])
    advancement("historian", ns("witness_replay"), "minecraft:writable_book", "goal", {"witnessed_five": impossible()})
    types = ["battle", "siege", "dragon_attack", "market_day", "coronation", "ritual", "duel", "festival", "exodus", "cave_in"]
    advancement("keeper_of_ages", ns("historian"), "minecraft:written_book", "challenge", {t: impossible() for t in types},
                requirements=[[t] for t in types], rewards={"experience": 500})
    advancement("elder_dawn", ns("witness_replay"), ns("echo_crystal_block"), "goal", {"witnessed": impossible()}, hidden=True)
    advancement("moth_to_a_flame", ns("root"), ns("memory_moth_spawn_egg"), "task", {"attuned": impossible()})
    advancement("lingering_doubts", ns("root"), ns("echo_dust"), "task", {"killed": {
        "trigger": "minecraft:player_killed_entity",
        "conditions": {"entity": {"type": "minecraft:entity_properties", "entity": "this", "predicate": {"minecraft:entity_type": ns("lingerer")}}},
    }})
    advancement("laid_to_rest", ns("lingering_doubts"), ns("heirloom_locket"), "goal", {"rested": impossible()})
    advancement("wyrmslayer", ns("where_it_happened"), ns("wyrm_heart"), "challenge", {"slain": impossible()}, rewards={
        "experience": 300, "recipes": [ns("echoing_blade"), ns("wyrmscale_helmet"), ns("wyrmscale_chestplate"), ns("wyrmscale_leggings"), ns("wyrmscale_boots")]})
    advancement("echo_chamber", ns("wyrmslayer"), ns("echoing_blade"), "goal", {"has_blade": has_items(ns("echoing_blade"))})
    advancement("field_notes", ns("root"), ns("guide_book"), "task", {"has_guide": has_items(ns("guide_book"))}, toast=False, chat=False)
    advancement("key_to_the_past", ns("where_it_happened"), ns("keystone_of_crowns"), "goal",
                {k: has_items(ns(k)) for k in KEYSTONES}, requirements=[KEYSTONES])
    advancement("regicide", ns("key_to_the_past"), ns("hollow_crown"), "challenge", {"slain": impossible()}, rewards={"experience": 300})
    advancement("the_walls_fall", ns("key_to_the_past"), ns("colossus_core"), "challenge", {"slain": impossible()}, rewards={"experience": 300})
    advancement("dawnbreaker", ns("key_to_the_past"), ns("dawn_staff"), "challenge", {"slain": impossible()}, rewards={"experience": 400})
    bosses = ["hollow_king", "siege_colossus", "echo_wyrm", "hierophant"]
    advancement("echo_hunter", ns("dawnbreaker"), ns("royal_sigil"), "challenge", {b: impossible() for b in bosses},
                requirements=[[b] for b in bosses], rewards={"experience": 1000})
    advancement("crystal_clear", ns("root"), ns("echo_shard"), "task", {"killed": {
        "trigger": "minecraft:player_killed_entity",
        "conditions": {"entity": {"type": "minecraft:entity_properties", "entity": "this", "predicate": {"minecraft:entity_type": ns("shard_crawler")}}},
    }})
    advancement("full_regalia", ns("root"), ns("spectral_knight_chestplate"), "goal",
                {k: has_items(*[ns(f"{k}_{p}") for p in v]) for k, v in SETS.items()}, requirements=[list(SETS)])


# ---------------------------------------------------------------- language

def lang():
    en = {
        "itemGroup.echoes_of_the_past": "Echoes of the Past",
        "block.echoes_of_the_past.echo_deposit": "Echo Deposit",
        "block.echoes_of_the_past.deepslate_echo_deposit": "Deepslate Echo Deposit",
        "block.echoes_of_the_past.echo_crystal_block": "Block of Echo Crystal",
        "block.echoes_of_the_past.echo_projector": "Echo Projector",
        "block.echoes_of_the_past.echo_lantern": "Echo Lantern",
        "block.echoes_of_the_past.relic_cache_soil": "Disturbed Earth",
        "block.echoes_of_the_past.relic_cache_stone": "Disturbed Stone",
        "block.echoes_of_the_past.echo_projector.occupied": "The projector already holds an echo.",
        "block.echoes_of_the_past.echo_projector.busy": "The memory is still playing out…",
        "block.echoes_of_the_past.echo_projector.empty": "Place an echo block in the projector to replay its memory.",
        "item.echoes_of_the_past.echo_shard": "Echo Shard",
        "item.echoes_of_the_past.echo_dust": "Echo Dust",
        "item.echoes_of_the_past.echo_block": "Echo Block",
        "item.echoes_of_the_past.ancient_coin": "Ancient Coin",
        "item.echoes_of_the_past.wyrmscale": "Wyrmscale",
        "item.echoes_of_the_past.wyrm_heart": "Wyrm Heart",
        "item.echoes_of_the_past.archaeologist_chisel": "Archaeologist's Chisel",
        "item.echoes_of_the_past.resonance_compass": "Resonance Compass",
        "item.echoes_of_the_past.resonance_compass.found": "The needle trembles: an echo lies %s blocks away.",
        "item.echoes_of_the_past.resonance_compass.silent": "The compass hears nothing nearby.",
        "item.echoes_of_the_past.legionnaire_blade": "Legionnaire's Blade",
        "item.echoes_of_the_past.tarnished_crown": "Tarnished Crown",
        "item.echoes_of_the_past.heirloom_locket": "Heirloom Locket",
        "item.echoes_of_the_past.festival_charm": "Festival Charm",
        "item.echoes_of_the_past.echoing_blade": "Echoing Blade",
        "item.echoes_of_the_past.wyrmscale_helmet": "Wyrmscale Helm",
        "item.echoes_of_the_past.wyrmscale_chestplate": "Wyrmscale Cuirass",
        "item.echoes_of_the_past.wyrmscale_leggings": "Wyrmscale Greaves",
        "item.echoes_of_the_past.wyrmscale_boots": "Wyrmscale Boots",
        "item.echoes_of_the_past.music_disc_echoes": "Music Disc",
        "jukebox_song.echoes_of_the_past.echoes": "Echoes of the Past - The Last Chronicler",
        "item.echoes_of_the_past.lingerer_spawn_egg": "Lingerer Spawn Egg",
        "item.echoes_of_the_past.memory_moth_spawn_egg": "Memory Moth Spawn Egg",
        "item.echoes_of_the_past.echo_wyrm_spawn_egg": "Echo Wyrm Spawn Egg",
        "entity.echoes_of_the_past.echo_figure": "Echo",
        "entity.echoes_of_the_past.lingerer": "Lingerer",
        "entity.echoes_of_the_past.memory_moth": "Memory Moth",
        "entity.echoes_of_the_past.echo_wyrm": "Echo Wyrm",
        "tooltip.echoes_of_the_past.echo.era": "Formed in %s",
        "tooltip.echoes_of_the_past.echo.origin": "Remembers the land near %s, %s, %s",
        "replay.echoes_of_the_past.rift_warning": "The memory grows unstable... something is pushing through!",
        "replay.echoes_of_the_past.relic_revealed": "The echo settles into the earth. Something lies buried at %s, %s, %s.",
        "replay.echoes_of_the_past.relic_taken": "The ground here has already given up its secrets.",
        "replay.echoes_of_the_past.distant": "This memory belongs elsewhere. Replay it near %s, %s to learn what was left behind.",
        "replay.echoes_of_the_past.wyrm_rift": "%s tears free of the past!",
        "replay.echoes_of_the_past.lingerer_rift": "The dead of that day step out of the echo.",
        "command.echoes_of_the_past.history.header": "The remembered history of chunk %s, %s:",
        "command.echoes_of_the_past.give.unknown_era": "Unknown era: %s",
        "command.echoes_of_the_past.give.success": "Conjured an echo: %s",
        "command.echoes_of_the_past.replay.no_projector": "There is no idle Echo Projector there.",
    }
    en.update({
        "item.echoes_of_the_past.spectral_plate": "Spectral Plate",
        "item.echoes_of_the_past.revenant_ash": "Revenant Ash",
        "item.echoes_of_the_past.cindersteel_ingot": "Cindersteel Ingot",
        "item.echoes_of_the_past.colossus_plating": "Colossus Plating",
        "item.echoes_of_the_past.colossus_core": "Colossus Furnace Core",
        "item.echoes_of_the_past.dawnstone": "Dawnstone",
        "item.echoes_of_the_past.royal_sigil": "Royal Sigil",
        "item.echoes_of_the_past.spectral_longsword": "Spectral Longsword",
        "item.echoes_of_the_past.ashen_cleaver": "Ashen Cleaver",
        "item.echoes_of_the_past.crownbreaker": "Crownbreaker",
        "item.echoes_of_the_past.siegebreaker": "Siegebreaker",
        "item.echoes_of_the_past.dawn_staff": "Staff of the Elder Dawn",
        "item.echoes_of_the_past.hollow_crown": "Crown of the Hollow King",
        "item.echoes_of_the_past.spectral_knight_helmet": "Spectral Knight Helm",
        "item.echoes_of_the_past.spectral_knight_chestplate": "Spectral Knight Breastplate",
        "item.echoes_of_the_past.spectral_knight_leggings": "Spectral Knight Greaves",
        "item.echoes_of_the_past.spectral_knight_boots": "Spectral Knight Sabatons",
        "item.echoes_of_the_past.cindersteel_helmet": "Cindersteel Helm",
        "item.echoes_of_the_past.cindersteel_chestplate": "Cindersteel Hauberk",
        "item.echoes_of_the_past.cindersteel_leggings": "Cindersteel Leggings",
        "item.echoes_of_the_past.cindersteel_boots": "Cindersteel Boots",
        "item.echoes_of_the_past.colossus_helmet": "Colossus Helm",
        "item.echoes_of_the_past.colossus_chestplate": "Colossus Bulwark",
        "item.echoes_of_the_past.colossus_leggings": "Colossus Tassets",
        "item.echoes_of_the_past.colossus_boots": "Colossus Treads",
        "item.echoes_of_the_past.dawnweave_hood": "Dawnweave Hood",
        "item.echoes_of_the_past.dawnweave_robe": "Dawnweave Robe",
        "item.echoes_of_the_past.dawnweave_leggings": "Dawnweave Leggings",
        "item.echoes_of_the_past.dawnweave_slippers": "Dawnweave Slippers",
        "item.echoes_of_the_past.keystone_of_crowns": "Keystone of Crowns",
        "item.echoes_of_the_past.keystone_of_iron": "Keystone of Iron and Ash",
        "item.echoes_of_the_past.keystone_of_dragons": "Keystone of Dragons",
        "item.echoes_of_the_past.keystone_of_dawn": "Keystone of the Elder Dawn",
        "item.echoes_of_the_past.guide_book": "Archaeologist's Field Guide",
        "item.echoes_of_the_past.showcase_book": "Codex of Echoes",
        "entity.echoes_of_the_past.echo_knight": "Echo Knight",
        "entity.echoes_of_the_past.spectral_archer": "Spectral Archer",
        "entity.echoes_of_the_past.ash_revenant": "Ash Revenant",
        "entity.echoes_of_the_past.dawn_wisp": "Dawn Wisp",
        "entity.echoes_of_the_past.shard_crawler": "Shard Crawler",
        "entity.echoes_of_the_past.hollow_king": "The Hollow King",
        "entity.echoes_of_the_past.siege_colossus": "Siege Colossus",
        "entity.echoes_of_the_past.hierophant": "Hierophant of the Elder Dawn",
        "boss.echoes_of_the_past.enraged": "%s is enraged!",
        "boss.echoes_of_the_past.slain": "%s returns to the past.",
        "boss.echoes_of_the_past.stirs": "Something stirs in the echo...",
        "boss.echoes_of_the_past.hollow_king.decree": "\"KNEEL before your king!\"",
        "boss.echoes_of_the_past.hollow_king.summon": "\"To me, my sworn!\"",
        "boss.echoes_of_the_past.siege_colossus.barrage": "The Colossus calls down its engines!",
        "boss.echoes_of_the_past.siege_colossus.overheat": "The Colossus's furnace heart overheats!",
        "boss.echoes_of_the_past.hierophant.starfall": "The stars of the first night fall!",
        "boss.echoes_of_the_past.hierophant.conjure": "Wards of living light gather. Break them!",
        "guide.echoes_of_the_past.welcome": "You carry an Archaeologist's Field Guide. The world remembers; read on to learn how to listen.",
        "armor_set.echoes_of_the_past.wyrmscale": "Set bonus: fire immunity, slow falling",
        "armor_set.echoes_of_the_past.spectral_knight": "Set bonus: speed, spectral strikes",
        "armor_set.echoes_of_the_past.cindersteel": "Set bonus: strength, fire immunity",
        "armor_set.echoes_of_the_past.colossus": "Set bonus: resistance",
        "armor_set.echoes_of_the_past.dawnweave": "Set bonus: night vision, healing",
        "showcase.echoes_of_the_past.title": "Codex of Echoes",
        "showcase.echoes_of_the_past.subtitle": "Build, summon and replay anything the world remembers",
        "showcase.echoes_of_the_past.tab.structure": "Structures",
        "showcase.echoes_of_the_past.tab.boss": "Bosses",
        "showcase.echoes_of_the_past.tab.creature": "Creatures",
        "showcase.echoes_of_the_past.tab.replay": "Replays",
        "showcase.echoes_of_the_past.tab.gear": "Gear",
        "showcase.echoes_of_the_past.arena_button": "Build Arena + Summon",
        "showcase.echoes_of_the_past.done": "Codex: %s",
        "showcase.echoes_of_the_past.replaying": "Codex: replaying \"%s\"",
        "showcase.echoes_of_the_past.replay_missing": "No chunk nearby remembers %s.",
        "showcase.echoes_of_the_past.creative_only": "The Codex of Echoes only answers to creative mode or operators.",
        "showcase.echoes_of_the_past.unknown": "The Codex has no entry %s.",
    })
    showcase = {
        "structure": {
            "projector_stage": ("Projector Stage", "A ceremonial stage with an Echo Projector at its heart."),
            "dig_site": ("Dig Site", "An excavation with echo deposits, relic caches and a stocked tent."),
            "throne_hall": ("Throne Hall", "The ruined hall of the Hollow King."),
            "siege_ruin": ("Siege Ruin", "A broken fortress ring where the Colossus fought."),
            "wyrm_roost": ("Wyrm Roost", "A basalt crag crowned with the ribs of dragons."),
            "dawn_altar": ("Dawn Altar", "Standing stones around the altar of the Elder Dawn."),
            "bestiary": ("Bestiary", "Every creature and great echo on display."),
            "armory": ("Armory", "Every armor set and weapon on armor stands."),
        },
        "boss": {
            "hollow_king": ("Summon the Hollow King", "Crowns-era boss: cleaves, charges, issues decrees and summons knights."),
            "siege_colossus": ("Summon the Siege Colossus", "Iron and Ash boss: ground slams, mortar barrages, overheats."),
            "echo_wyrm": ("Summon the Echo Wyrm", "Dragon-age boss: swoops and breathes spectral fire."),
            "hierophant": ("Summon the Hierophant", "Elder Dawn boss: shielded by wards, lances of light, starfall."),
        },
        "arena": {
            "hollow_king": ("Throne Hall + Hollow King", "Builds the Throne Hall and summons its king."),
            "siege_colossus": ("Siege Ruin + Colossus", "Builds the Siege Ruin and summons the Colossus."),
            "echo_wyrm": ("Wyrm Roost + Echo Wyrm", "Builds the Wyrm Roost and summons the Wyrm."),
            "hierophant": ("Dawn Altar + Hierophant", "Builds the Dawn Altar and summons the Hierophant."),
        },
        "creature": {
            "lingerer": ("Lingerer", "A soldier who never left the battlefield."),
            "memory_moth": ("Memory Moth", "Feed it echo dust and it leads you to echoes."),
            "echo_knight": ("Echo Knight", "Shields its front and charges."),
            "spectral_archer": ("Spectral Archer", "Keeps its distance and fires slowing bolts."),
            "ash_revenant": ("Ash Revenant", "A burning smith of the Iron and Ash era."),
            "dawn_wisp": ("Dawn Wisp", "A floating mote of the first light."),
            "shard_crawler": ("Shard Crawler", "A shy crystal beetle; drops echo shards."),
        },
        "replay": {t: (t.replace("_", " ").title(), "Builds a projector stage and replays a nearby memory of this kind.") for t in
                   ["battle", "siege", "dragon_attack", "market_day", "coronation", "ritual", "duel", "festival", "exodus", "cave_in"]},
        "gear": {
            "explorer_kit": ("Explorer's Kit", "Guide, chisel, brush, compass, projector and three echoes of this place."),
            "wyrmscale": ("Wyrmscale Set", "Wyrmscale armor and the Echoing Blade."),
            "spectral_knight": ("Spectral Knight Set", "Spectral Knight armor and the Spectral Longsword."),
            "cindersteel": ("Cindersteel Set", "Cindersteel armor and the Ashen Cleaver."),
            "colossus": ("Colossus Set", "Colossus armor and Siegebreaker."),
            "dawnweave": ("Dawnweave Set", "Dawnweave robes and the Staff of the Elder Dawn."),
            "weapons": ("Every Weapon", "One of every weapon, and the Hollow King's crown."),
            "keystones": ("Keystones", "All four Memory Keystones and a projector to use them on."),
        },
    }
    showcase["replay"]["dragon_attack"] = ("Dragon Attack", showcase["replay"]["dragon_attack"][1])
    showcase["replay"]["market_day"] = ("Market Day", showcase["replay"]["market_day"][1])
    showcase["replay"]["cave_in"] = ("Cave-in", showcase["replay"]["cave_in"][1])
    for category, entries in showcase.items():
        for key, (title, desc) in entries.items():
            en[f"showcase.echoes_of_the_past.{category}.{key}"] = title
            en[f"showcase.echoes_of_the_past.{category}.{key}.desc"] = desc
    for mob in NEW_MOBS:
        en[f"item.echoes_of_the_past.{mob}_spawn_egg"] = en[f"entity.echoes_of_the_past.{mob}"].replace("The ", "") + " Spawn Egg"

    adv = {
        "field_notes": ("Field Notes", "Carry the Archaeologist's Field Guide"),
        "key_to_the_past": ("Key to the Past", "Craft a Memory Keystone"),
        "regicide": ("Regicide", "Defeat the Hollow King"),
        "the_walls_fall": ("The Walls Fall", "Defeat the Siege Colossus"),
        "dawnbreaker": ("Dawnbreaker", "Defeat the Hierophant of the Elder Dawn"),
        "echo_hunter": ("Hunter of Echoes", "Defeat all four great echoes"),
        "crystal_clear": ("Crystal Clear", "Defeat a Shard Crawler"),
        "full_regalia": ("Full Regalia", "Wear a complete set of echo-forged armor"),
        "root": ("Echoes of the Past", "The past is buried beneath your feet. Find an echo shard or forge some copper to begin."),
        "careful_hands": ("Careful Hands", "Chisel an intact Echo Block out of an echo deposit"),
        "witness_replay": ("Lights of Ages Past", "Watch an echo's memory replay in an Echo Projector"),
        "where_it_happened": ("Where It Happened", "Replay an echo in the very place it formed"),
        "unearthed": ("Unearthed", "Brush a relic of the past out of disturbed earth"),
        "historian": ("Historian", "Witness five different kinds of historic events"),
        "keeper_of_ages": ("Keeper of Ages", "Witness every kind of event the world remembers"),
        "elder_dawn": ("Before the Dawn", "Witness a memory from the Elder Dawn"),
        "moth_to_a_flame": ("Moth to a Flame", "Attune a Memory Moth with echo dust"),
        "lingering_doubts": ("Lingering Doubts", "Defeat a Lingerer"),
        "laid_to_rest": ("Laid to Rest", "Show a Lingerer an Heirloom Locket so it may finally rest"),
        "wyrmslayer": ("Wyrmslayer of Ages", "Defeat an Echo Wyrm that tore free of its memory"),
        "echo_chamber": ("Echo Chamber", "Forge the Echoing Blade from a Wyrm Heart"),
    }
    for key, (title, desc) in adv.items():
        en[f"advancements.{NS}.{key}.title"] = title
        en[f"advancements.{NS}.{key}.description"] = desc
    subtitles = {
        "block.echo_projector.activate": "Echo Projector whirs to life",
        "block.echo_projector.hum": "Echo Projector hums",
        "block.echo_projector.insert": "Echo placed",
        "replay.end": "Memory fades",
        "block.echo_deposit.chime": "Echo deposit chimes",
        "item.chisel.tap": "Chisel taps",
        "item.chisel.extract": "Echo freed",
        "replay.relic_reveal": "Something settles in the earth",
        "replay.rift_open": "Rift tears open",
        "replay.rift_warning": "Memory destabilizes",
        "item.resonance_compass.ping": "Compass resonates",
        "replay.horn": "Ghostly horn",
        "replay.clash": "Ghostly steel clashes",
        "replay.roar": "Ghostly roar",
        "replay.cheer": "Ghostly cheering",
        "replay.bell": "Ghostly bell",
        "replay.chant": "Ghostly chanting",
        "replay.rumble": "Distant rumble",
        "replay.fire": "Ghostly fire",
        "replay.fanfare": "Ghostly fanfare",
        "replay.music": "Ghostly music",
        "replay.whisper": "Whispers of the past",
        "entity.lingerer.ambient": "Lingerer mourns",
        "entity.lingerer.hurt": "Lingerer hurts",
        "entity.lingerer.death": "Lingerer fades",
        "entity.lingerer.phase": "Lingerer phases",
        "entity.memory_moth.flutter": "Memory Moth flutters",
        "entity.memory_moth.attune": "Memory Moth attunes",
        "entity.echo_wyrm.roar": "Echo Wyrm roars",
        "entity.echo_wyrm.breath": "Echo Wyrm breathes fire",
        "entity.echo_wyrm.hurt": "Echo Wyrm hurts",
        "entity.echo_wyrm.death": "Echo Wyrm dissolves",
        "entity.echo_wyrm.flap": "Wings beat",
        "music_disc.echoes": "Music disc plays",
        "entity.hollow_king.ambient": "Hollow King mutters",
        "entity.hollow_king.hurt": "Hollow King hurts",
        "entity.hollow_king.death": "Hollow King fades",
        "entity.hollow_king.cleave": "Greatsword cleaves",
        "entity.hollow_king.decree": "Royal decree",
        "entity.hollow_king.roar": "Hollow King roars",
        "entity.siege_colossus.groan": "Colossus groans",
        "entity.siege_colossus.hurt": "Colossus clangs",
        "entity.siege_colossus.death": "Colossus collapses",
        "entity.siege_colossus.slam": "Ground shakes",
        "entity.siege_colossus.step": "Heavy footsteps",
        "entity.siege_colossus.mortar": "Mortar impact",
        "entity.hierophant.ambient": "Hierophant chants",
        "entity.hierophant.hurt": "Hierophant hurts",
        "entity.hierophant.death": "Hierophant fades",
        "entity.hierophant.cast": "Hierophant casts",
        "entity.hierophant.lance": "Lance of light",
        "entity.hierophant.ward_hit": "Ward absorbs a blow",
        "entity.hierophant.ward_break": "Wards shatter",
        "entity.hierophant.starfall": "Star falls",
        "entity.echo_knight.ambient": "Echo Knight murmurs",
        "entity.echo_knight.hurt": "Echo Knight hurts",
        "entity.echo_knight.block": "Spectral shield blocks",
        "entity.echo_knight.charge": "Echo Knight charges",
        "entity.echo_knight.step": "Armored footsteps",
        "entity.ash_revenant.ambient": "Ash Revenant smoulders",
        "entity.ash_revenant.hurt": "Ash Revenant hurts",
        "entity.ash_revenant.death": "Ash Revenant crumbles",
        "entity.ash_revenant.burst": "Embers burst",
        "entity.dawn_wisp.ambient": "Dawn Wisp hums",
        "entity.dawn_wisp.hurt": "Dawn Wisp flickers",
        "entity.dawn_wisp.death": "Dawn Wisp gutters out",
        "entity.shard_crawler.ambient": "Shard Crawler chitters",
        "entity.shard_crawler.hurt": "Shard Crawler hurts",
        "entity.shard_crawler.death": "Shard Crawler dies",
        "entity.shard_crawler.step": "Shard Crawler scuttles",
        "entity.shard_crawler.burrow": "Shard Crawler burrows",
        "combat.bolt.fire": "Bolt of light fires",
        "combat.bolt.hit": "Bolt strikes",
        "boss.manifest": "A great echo manifests",
        "showcase.build": "Structure rises",
    }
    for key, text in subtitles.items():
        en[f"subtitles.{NS}.{key}"] = text
    write(asset("lang", "en_us.json"), en)


if __name__ == "__main__":
    blocks()
    items()
    sounds()
    loot()
    recipes()
    tags()
    worldgen()
    advancements()
    lang()
    print("Generated data and assets.")
