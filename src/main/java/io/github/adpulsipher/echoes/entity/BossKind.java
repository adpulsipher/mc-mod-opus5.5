package io.github.adpulsipher.echoes.entity;

import java.util.Locale;
import java.util.function.Supplier;

import io.github.adpulsipher.echoes.history.Era;
import io.github.adpulsipher.echoes.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

/**
 * The great echoes that can tear free of the past, the era each belongs to, and the arena that suits it.
 */
public enum BossKind {
	HOLLOW_KING(Era.CROWNS, "throne_hall", 0xFFD35A, () -> ModEntities.HOLLOW_KING),
	SIEGE_COLOSSUS(Era.IRON_AND_ASH, "siege_ruin", 0xFF8A3A, () -> ModEntities.SIEGE_COLOSSUS),
	ECHO_WYRM(Era.DRAGONS, "wyrm_roost", 0xE0A8FF, () -> ModEntities.ECHO_WYRM),
	HIEROPHANT(Era.ELDER_DAWN, "dawn_altar", 0xFFE7A0, () -> ModEntities.HIEROPHANT);

	private final Era era;
	private final String arena;
	private final int color;
	private final Supplier<EntityType<? extends Mob>> type;

	BossKind(Era era, String arena, int color, Supplier<EntityType<? extends Mob>> type) {
		this.era = era;
		this.arena = arena;
		this.color = color;
		this.type = type;
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public Era era() {
		return era;
	}

	public String arena() {
		return arena;
	}

	public int color() {
		return color;
	}

	public EntityType<? extends Mob> type() {
		return type.get();
	}

	public static @Nullable BossKind byId(String id) {
		for (BossKind kind : values()) {
			if (kind.id().equals(id)) {
				return kind;
			}
		}
		return null;
	}
}
