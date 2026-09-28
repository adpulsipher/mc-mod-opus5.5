package io.github.adpulsipher.echoes.registry;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.component.EchoMemory;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModComponents {
	public static final DataComponentType<EchoMemory> ECHO_MEMORY = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			EchoesOfThePast.id("echo_memory"),
			DataComponentType.<EchoMemory>builder().persistent(EchoMemory.CODEC).networkSynchronized(EchoMemory.STREAM_CODEC).build()
	);

	private ModComponents() {
	}

	public static void init() {
	}
}
