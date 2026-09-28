# Echoes of the Past

*A lore-archaeology mod for Minecraft Java Edition 26.3 (Fabric).*

The world remembers. Over centuries, the strongest moments of the past (battles, coronations, rituals, dragon
attacks) crystallize into **echo deposits** deep in the stone. Chisel one free, place it in an **Echo Projector**, and
a ghostly, holographic replay of that event plays out around you, exactly where it happened, hundreds or thousands
of years ago.

Every chunk of every world has its own history, generated deterministically from the world seed. The deeper an echo
forms, the older the memory it holds.

![Blocks and creatures of Echoes of the Past](docs/screenshots/0000_echoes-showcase.png)

![Two armies of the Age of Crowns face off in a replay](docs/screenshots/0001_echoes-replay-1.png)
![The lines break upon one another](docs/screenshots/0002_echoes-replay-2.png)
![A dragon-age memory tears open a rift](docs/screenshots/0004_echoes-replay-dragons.png)

![The Hollow King holds court in his ruined throne hall](docs/screenshots/0008_echoes-hollow-king.png)
![The Siege Colossus among the walls it once breached](docs/screenshots/0009_echoes-siege-colossus.png)

*Screenshots are captured automatically by the client game test in CI.*

## New players: the Field Guide
Everyone who joins a world for the first time receives the **Archaeologist's Field Guide**, an illustrated book
with a clickable table of contents covering every system in the mod: finding and extracting echoes, the eras,
relics, rifts, every creature and boss, keystones, weapons, armor and set bonuses. Lost it? Craft another from a book
and an echo shard.

![The Field Guide](docs/screenshots/0012_echoes-guide-book.png)

## Creative: the Codex of Echoes
The **Codex of Echoes** (creative tab) opens a menu that builds and summons everything in the mod in front of you:

* **Structures**: Projector Stage, Dig Site (with echo deposits, brushable relic caches and a stocked tent), Throne
  Hall, Siege Ruin, Wyrm Roost, Dawn Altar, a Bestiary with every creature on display, and an Armory with every
  armor set on armor stands.
* **Bosses**: summon any great echo, or build its arena and summon it there.
* **Creatures**: spawn any of the mod's mobs.
* **Replays**: build a projector stage and replay a nearby memory of any kind of event (battle, siege, dragon attack,
  market day, coronation, ritual, duel, festival, exodus, cave-in).
* **Gear**: an explorer's kit, every armor set with its weapon, every weapon, and all four keystones.

Every entry is also a command: `/echoes showcase <category> <id>` (creative mode or operators).

![The Codex of Echoes](docs/screenshots/0013_echoes-codex.png)
![The Bestiary](docs/screenshots/0005_echoes-bestiary.png)
![The Armory](docs/screenshots/0006_echoes-armory.png)
![A dig site](docs/screenshots/0007_echoes-dig-site.png)

## Gameplay

### 1. Find echoes
* **Echo Deposits** and **Deepslate Echo Deposits** generate underground in every Overworld biome. They glow
  faintly, shed motes of light and chime softly when you are near.
* Mining one with a pickaxe shatters the memory into **Echo Shards** (Fortune applies). Silk Touch takes the whole
  deposit.
* A **Resonance Compass** (compass + 4 echo shards) listens for the nearest deposit within 24 blocks and points to it.
* Feed **Echo Dust** to a wild **Memory Moth** and it will attune to you and fly to the nearest deposit, circling it.

### 2. Extract them intact
Hold right-click on a deposit with an **Archaeologist's Chisel** (iron + copper + stick) to work it free. After a few
seconds of careful tapping you get an **Echo Block**: an item that remembers *where* it formed, *how deep* it was,
and *what* it witnessed. Its tooltip names the event.

| Depth | Era | Years ago |
|---|---|---|
| y ≥ 48 | The Age of Hearths | 150 – 400 |
| 16 – 47 | The Age of Crowns | 400 – 900 |
| −16 – 15 | The Age of Iron and Ash | 900 – 1,600 |
| −48 – −17 | The Age of Dragons | 1,600 – 3,000 |
| below −48 | The Elder Dawn | 3,000 – 6,000 |

### 3. Project the memory
Place an Echo Block in an **Echo Projector** (glass, echo shards, amethyst and copper). The projector spins up, a
column of light rises, and the event is replayed by translucent, glowing figures tinted in the colors of the
factions that fought, traded or prayed there:

* **Battle**: two armies exchange volleys, charge and clash; the defeated fall and fade.
* **Siege**: defenders line the walls while a ram is carried to the gate; the sally or the fall of the keep.
* **Dragon Attack**: villagers flee as a spectral dragon circles and breathes ghost-fire; archers make their stand.
* **Market Day**: caravans arrive, merchants haggle, a juggler draws a crowd.
* **Coronation**: lords kneel as a ruler walks the aisle to be crowned (or betrayed).
* **Ritual**: a circle of mages chants until a pillar of light answers.
* **Duel**: two champions bow, circle and fight inside a ring of onlookers.
* **Festival**: dancers circle a bonfire to a fiddler's tune.
* **Exodus**: a long column of refugees leaves home forever.
* **Cave-in**: miners work a rich vein until the tunnel comes down.

Each replay is narrated on screen with the event's title, its date and five lines of story. Settlement names are
consistent across neighbouring chunks, factions have names and heraldry, and heroes, villains and dragons have
names and epithets.

Everyone watching receives an **Echo Transcript**: a written book chronicling the event in full.

### 4. Dig up what was left behind
Replay an echo **in the chunk where it formed** and the memory settles into the earth: a **Disturbed Earth** or
**Disturbed Stone** block appears where relics of the event lie buried, at a depth matching its era. Brush it with a
vanilla brush to recover relics themed to the event:

* **Legionnaire's Blade**: a sturdy relic sword that can strike phased spirits.
* **Tarnished Crown**: a helmet; Lingerers will not raise a blade against anyone who wears a crown.
* **Heirloom Locket**: show it to a Lingerer and it finally rests (and leaves gifts).
* **Festival Charm**: a consumable good-luck charm (Regeneration, Luck and Speed).
* **Ancient Coins**, **Wyrmscales**, and period loot from battles, markets, rituals and mines.

A replay played far from home still teaches its story, but the transcript tells you where to take the echo to find its
relics.

### 5. Beware what follows the memory back
Violent memories are unstable. Near the end of a battle, siege, ritual or dragon attack the replay may warn that it is
**destabilizing**, and when it ends a rift can open:

* **Lingerers**, the ghosts of soldiers who never left the battlefield, step out of the echo. They periodically
  *phase* out of time, becoming immune to ordinary weapons and blinking toward their foe. They also haunt deep
  caves naturally, and fade away in daylight.
* A dragon-attack memory from the **Age of Dragons** or the **Elder Dawn** can let the **Echo Wyrm** itself tear free:
  a flying boss with a boss bar that circles its old hunting ground, dives at intruders, breathes spectral fire and
  calls its fallen soldiers to its side at half health.

Defeat the Wyrm for its **Wyrm Heart**, **Wyrmscales** and the music disc **"The Last Chronicler"**. Forge the
**Echoing Blade**, whose every blow is struck again a moment later by a ghostly afterimage, and a set of
**Wyrmscale armor**.

### 6. The great echoes
Four memories are strong enough to walk in the present. Each has a boss bar, a hand-written attack pattern and a
second phase at half health. They can tear free at the end of an unstable replay from their era, or be called on
demand by setting a **Memory Keystone** into an idle Echo Projector.

| Boss | Era | Fight | Drops |
|---|---|---|---|
| **The Hollow King** | Age of Crowns | Cleaves the ground before him, charges fleeing foes, issues decrees that slow and weaken everything nearby, and calls his Echo Knights back from the dead. | Crownbreaker, Crown of the Hollow King, Royal Sigil |
| **Siege Colossus** | Iron and Ash | Slams with both fists, stomps anyone underfoot, and calls down telegraphed mortar barrages. Arrows glance off its plating. Its furnace heart overheats at half health. | Siegebreaker, Colossus Furnace Core, Colossus Plating |
| **Echo Wyrm** | Age of Dragons | Circles, swoops and breathes spectral fire; summons Lingerers. | Wyrm Heart, Wyrmscale, music disc |
| **Hierophant of the Elder Dawn** | Elder Dawn | Floats above its altar behind orbiting wards of light: destroy its Dawn Wisps before your blows can land. Spears you with lances of dawn, calls down starfall and flashes across the altar in novas. | Staff of the Elder Dawn, Dawnstone |

| Keystone | Recipe |
|---|---|
| Keystone of Crowns | Tarnished Crown, gold ingots, Block of Echo Crystal |
| Keystone of Iron and Ash | Legionnaire's Blade, iron blocks, Cindersteel Ingot, Block of Echo Crystal |
| Keystone of Dragons | Wyrmscales, Block of Echo Crystal |
| Keystone of the Elder Dawn | Wyrm Heart, amethyst, Spectral Plate, Block of Echo Crystal |

Rifts are themed by era too: Crowns memories release Echo Knights and Spectral Archers, Iron and Ash memories Ash
Revenants, and the Elder Dawn releases Dawn Wisps.

### 7. Creatures of the deep past
* **Echo Knight** (below y 16): raises a spectral shield against blows from the front, and charges with its lance.
  Drops Spectral Plate.
* **Spectral Archer** (below y 32): keeps its distance and fires chilling bolts that slow.
* **Ash Revenant** (below y 0): sets what it strikes on fire, and bursts with embers. Drops Revenant Ash.
* **Dawn Wisp** (below y −40): a floating light that fires bolts of dawn.
* **Shard Crawler** (caves): a shy crystal beetle that grazes on echo deposits and burrows away when hurt. Drops Echo
  Shards.

### 8. Arms and armor
| Weapon | Source | Ability |
|---|---|---|
| Spectral Longsword | Spectral Plate | Strikes phased spirits |
| Ashen Cleaver | Cindersteel | Sets foes alight |
| Crownbreaker | Hollow King | Use: Royal Decree slows and weakens nearby hostiles and grants you Strength |
| Siegebreaker | Siege Colossus | Knocks foes back; use: ground slam shockwave |
| Staff of the Elder Dawn | Hierophant | Use: fires bolts of dawn |
| Echoing Blade | Echo Wyrm | Every blow is struck again by an afterimage |

| Armor set | Crafted from | Full-set bonus |
|---|---|---|
| Spectral Knight | Spectral Plate (Echo Knights) | Speed; every blow counts as spectral |
| Cindersteel | Cindersteel Ingots (Revenant Ash + iron) | Strength, fire immunity |
| Colossus | Colossus Plating | Resistance, knockback resistance |
| Dawnweave | Dawnstone + wool | Night vision, gentle healing |
| Wyrmscale | Wyrmscales | Fire immunity, slow falling |

The **Crown of the Hollow King** is a strong helmet, and like any crown it keeps Lingerers, Knights and Archers from
raising a blade against you.

## Content overview

| Blocks | Items | Mobs |
|---|---|---|
| Echo Deposit, Deepslate Echo Deposit | Echo Shard, Echo Dust, Echo Block | Lingerer, Echo Knight, Spectral Archer, Ash Revenant, Dawn Wisp (hostile) |
| Echo Projector | Archaeologist's Chisel, Resonance Compass, Field Guide, Codex of Echoes | Shard Crawler (neutral), Memory Moth (ambient guide) |
| Block of Echo Crystal, Echo Lantern | Legionnaire's Blade, Tarnished Crown, Heirloom Locket, Festival Charm, Ancient Coin | Hollow King, Siege Colossus, Echo Wyrm, Hierophant (bosses) |
| Disturbed Earth, Disturbed Stone (brushable) | 6 weapons, 5 armor sets + 2 crowns, 4 keystones, 8 boss and mob materials, Music Disc | Echoes (replay holograms, 10 roles) |

There are 21 advancements in their own tab, including **Keeper of Ages** (witness every kind of event the world
remembers) and **Hunter of Echoes** (defeat all four great echoes).

### Commands
* `/echoes history`: lists the remembered history of the chunk you stand in, one event per era.
* `/echoes give <era>` (operators): conjures the echo of your chunk from the given era.
* `/echoes replay <pos> <era>` (operators): starts a replay in the projector at `pos`.
* `/echoes showcase <category> <id>` (creative or operators): runs any entry of the Codex of Echoes.

## Building

Requires Java 25.

```sh
./gradlew build            # compile and package build/libs/echoes-of-the-past-<version>.jar
./gradlew runGameTest      # server game tests (history, projector, replays, relics, mobs, data)
./gradlew runClient        # play in a development client
```

Install the jar in `.minecraft/mods` together with **Fabric Loader 0.19.5+** and **Fabric API** for Minecraft 26.3.

### Regenerating assets
All textures, sounds and data files are generated by the scripts in `tools/`:

```sh
pip install pillow numpy scipy soundfile
python3 tools/gen_textures.py   # pixel art for blocks, items, entities, GUI and the mod icon
python3 tools/gen_sounds.py     # synthesized sound effects and the music disc
python3 tools/gen_data.py       # models, blockstates, loot tables, recipes, tags, worldgen, advancements, lang
```

## How it works

* `history/`: a pure-Java, deterministic history generator. `HistoryGenerator` derives one event per era for any
  chunk from the world seed; `Chronicler` writes its title, narration and transcript; `NameGen` names the people,
  places, factions and dragons.
* `replay/`: `Choreographer` stages each kind of event as a timed script of actors, movements, poses, effects,
  sounds and narration beats.
* `projection/`: `ReplayDirector` performs a script in the world with synced hologram entities; `ReplayOutcome`
  hands out transcripts, buries relics, grants advancements and opens rifts.
* `entity/`: the great echoes share `EchoBoss` (boss bar, synced attack state, enrage). `combat/` holds spectral
  bolts (server-simulated projectiles), telegraphed area attacks, armor set bonuses and the rules of spectral damage.
* `showcase/`: the Codex catalog, the structure builder and the actions behind `/echoes showcase`.
* Rendering uses custom entity models (a humanoid with role props, a serpentine wyrm, a moth, and the projector's
  light-work) drawn translucent and full-bright with a flickering, faction-tinted hologram look.

## License

MIT
