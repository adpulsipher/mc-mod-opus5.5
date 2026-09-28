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
HANDHELD_ITEMS = ["archaeologist_chisel", "legionnaire_blade", "echoing_blade"]
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

    write(t(NS, "item", "spectral_weapons.json"), {"values": [ns("echoing_blade"), ns("legionnaire_blade")]})
    write(t(NS, "item", "repairs_wyrmscale.json"), {"values": [ns("wyrmscale")]})
    write(t(NS, "item", "repairs_relic.json"), {"values": [ns("ancient_coin")]})
    write(t(NS, "item", "crowns.json"), {"values": [ns("tarnished_crown")]})
    write(t("minecraft", "item", "swords.json"), {"replace": False, "values": [ns("legionnaire_blade"), ns("echoing_blade")]})
    write(t("minecraft", "item", "head_armor.json"), {"replace": False, "values": [ns("wyrmscale_helmet"), ns("tarnished_crown")]})
    write(t("minecraft", "item", "chest_armor.json"), {"replace": False, "values": [ns("wyrmscale_chestplate")]})
    write(t("minecraft", "item", "leg_armor.json"), {"replace": False, "values": [ns("wyrmscale_leggings")]})
    write(t("minecraft", "item", "foot_armor.json"), {"replace": False, "values": [ns("wyrmscale_boots")]})

    write(t("minecraft", "entity_type", "undead.json"), {"replace": False, "values": [ns("lingerer")]})
    write(t("minecraft", "entity_type", "fall_damage_immune.json"), {"replace": False, "values": [ns("memory_moth"), ns("echo_wyrm"), ns("echo_figure")]})


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
        "length_in_seconds": 96.0,
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
    adv = {
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
