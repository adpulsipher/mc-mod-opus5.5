package io.github.adpulsipher.echoes.registry;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	public static final SoundEvent PROJECTOR_ACTIVATE = register("block.echo_projector.activate");
	public static final SoundEvent PROJECTOR_HUM = register("block.echo_projector.hum");
	public static final SoundEvent PROJECTOR_INSERT = register("block.echo_projector.insert");
	public static final SoundEvent REPLAY_END = register("replay.end");
	public static final SoundEvent ECHO_DEPOSIT_CHIME = register("block.echo_deposit.chime");
	public static final SoundEvent CHISEL_TAP = register("item.chisel.tap");
	public static final SoundEvent ECHO_EXTRACT = register("item.chisel.extract");
	public static final SoundEvent RELIC_REVEAL = register("replay.relic_reveal");
	public static final SoundEvent RIFT_OPEN = register("replay.rift_open");
	public static final SoundEvent RIFT_WARNING = register("replay.rift_warning");
	public static final SoundEvent COMPASS_PING = register("item.resonance_compass.ping");

	public static final SoundEvent REPLAY_HORN = register("replay.horn");
	public static final SoundEvent REPLAY_CLASH = register("replay.clash");
	public static final SoundEvent REPLAY_ROAR = register("replay.roar");
	public static final SoundEvent REPLAY_CHEER = register("replay.cheer");
	public static final SoundEvent REPLAY_BELL = register("replay.bell");
	public static final SoundEvent REPLAY_CHANT = register("replay.chant");
	public static final SoundEvent REPLAY_RUMBLE = register("replay.rumble");
	public static final SoundEvent REPLAY_FIRE = register("replay.fire");
	public static final SoundEvent REPLAY_FANFARE = register("replay.fanfare");
	public static final SoundEvent REPLAY_MUSIC = register("replay.music");
	public static final SoundEvent REPLAY_WHISPER = register("replay.whisper");

	public static final SoundEvent LINGERER_AMBIENT = register("entity.lingerer.ambient");
	public static final SoundEvent LINGERER_HURT = register("entity.lingerer.hurt");
	public static final SoundEvent LINGERER_DEATH = register("entity.lingerer.death");
	public static final SoundEvent LINGERER_PHASE = register("entity.lingerer.phase");
	public static final SoundEvent MOTH_FLUTTER = register("entity.memory_moth.flutter");
	public static final SoundEvent MOTH_ATTUNE = register("entity.memory_moth.attune");
	public static final SoundEvent WYRM_ROAR = register("entity.echo_wyrm.roar");
	public static final SoundEvent WYRM_BREATH = register("entity.echo_wyrm.breath");
	public static final SoundEvent WYRM_HURT = register("entity.echo_wyrm.hurt");
	public static final SoundEvent WYRM_DEATH = register("entity.echo_wyrm.death");
	public static final SoundEvent WYRM_FLAP = register("entity.echo_wyrm.flap");
	public static final SoundEvent KING_AMBIENT = register("entity.hollow_king.ambient");
	public static final SoundEvent KING_HURT = register("entity.hollow_king.hurt");
	public static final SoundEvent KING_DEATH = register("entity.hollow_king.death");
	public static final SoundEvent KING_CLEAVE = register("entity.hollow_king.cleave");
	public static final SoundEvent KING_DECREE = register("entity.hollow_king.decree");
	public static final SoundEvent KING_ROAR = register("entity.hollow_king.roar");
	public static final SoundEvent COLOSSUS_GROAN = register("entity.siege_colossus.groan");
	public static final SoundEvent COLOSSUS_HURT = register("entity.siege_colossus.hurt");
	public static final SoundEvent COLOSSUS_DEATH = register("entity.siege_colossus.death");
	public static final SoundEvent COLOSSUS_SLAM = register("entity.siege_colossus.slam");
	public static final SoundEvent COLOSSUS_STEP = register("entity.siege_colossus.step");
	public static final SoundEvent MORTAR_IMPACT = register("entity.siege_colossus.mortar");
	public static final SoundEvent HIEROPHANT_AMBIENT = register("entity.hierophant.ambient");
	public static final SoundEvent HIEROPHANT_HURT = register("entity.hierophant.hurt");
	public static final SoundEvent HIEROPHANT_DEATH = register("entity.hierophant.death");
	public static final SoundEvent HIEROPHANT_CAST = register("entity.hierophant.cast");
	public static final SoundEvent HIEROPHANT_LANCE = register("entity.hierophant.lance");
	public static final SoundEvent HIEROPHANT_WARD_HIT = register("entity.hierophant.ward_hit");
	public static final SoundEvent HIEROPHANT_WARD_BREAK = register("entity.hierophant.ward_break");
	public static final SoundEvent STARFALL = register("entity.hierophant.starfall");
	public static final SoundEvent KNIGHT_AMBIENT = register("entity.echo_knight.ambient");
	public static final SoundEvent KNIGHT_HURT = register("entity.echo_knight.hurt");
	public static final SoundEvent KNIGHT_BLOCK = register("entity.echo_knight.block");
	public static final SoundEvent KNIGHT_CHARGE = register("entity.echo_knight.charge");
	public static final SoundEvent KNIGHT_STEP = register("entity.echo_knight.step");
	public static final SoundEvent REVENANT_AMBIENT = register("entity.ash_revenant.ambient");
	public static final SoundEvent REVENANT_HURT = register("entity.ash_revenant.hurt");
	public static final SoundEvent REVENANT_DEATH = register("entity.ash_revenant.death");
	public static final SoundEvent REVENANT_BURST = register("entity.ash_revenant.burst");
	public static final SoundEvent WISP_AMBIENT = register("entity.dawn_wisp.ambient");
	public static final SoundEvent WISP_HURT = register("entity.dawn_wisp.hurt");
	public static final SoundEvent WISP_DEATH = register("entity.dawn_wisp.death");
	public static final SoundEvent CRAWLER_AMBIENT = register("entity.shard_crawler.ambient");
	public static final SoundEvent CRAWLER_HURT = register("entity.shard_crawler.hurt");
	public static final SoundEvent CRAWLER_DEATH = register("entity.shard_crawler.death");
	public static final SoundEvent CRAWLER_STEP = register("entity.shard_crawler.step");
	public static final SoundEvent CRAWLER_BURROW = register("entity.shard_crawler.burrow");
	public static final SoundEvent BOLT_FIRE = register("combat.bolt.fire");
	public static final SoundEvent BOLT_HIT = register("combat.bolt.hit");
	public static final SoundEvent BOSS_MANIFEST = register("boss.manifest");
	public static final SoundEvent SHOWCASE_BUILD = register("showcase.build");
	public static final Holder.Reference<SoundEvent> MUSIC_DISC_ECHOES = registerForHolder("music_disc.echoes");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = EchoesOfThePast.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	private static Holder.Reference<SoundEvent> registerForHolder(String name) {
		Identifier id = EchoesOfThePast.id(name);
		return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {
	}
}
