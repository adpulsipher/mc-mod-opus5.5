package io.github.adpulsipher.echoes.combat;

import io.github.adpulsipher.echoes.registry.ModTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Rules for what can touch a ghost. Spectral weapons, echo magic and anyone clad in full Spectral Knight plate
 * strike across time; everything else passes through a phased spirit.
 */
public final class Spectral {
	private Spectral() {
	}

	public static boolean isSpectralHit(DamageSource source) {
		ItemStack weapon = source.getWeaponItem();
		if (weapon != null && weapon.is(ModTags.SPECTRAL_WEAPONS)) {
			return true;
		}
		if (source.is(DamageTypes.INDIRECT_MAGIC)) {
			return true;
		}
		return source.getEntity() instanceof LivingEntity attacker && ArmorSets.wearsFull(attacker, ArmorSets.SPECTRAL_KNIGHT);
	}

	/** Echoes of the past do not fight one another. */
	public static boolean allied(Entity a, Entity b) {
		return a instanceof Echoborn && b instanceof Echoborn;
	}
}
