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
