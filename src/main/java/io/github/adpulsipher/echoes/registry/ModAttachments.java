package io.github.adpulsipher.echoes.registry;

import java.util.List;

import com.mojang.serialization.Codec;
import io.github.adpulsipher.echoes.EchoesOfThePast;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
	/** On a chunk: the eras whose relics have already been unearthed here. */
	public static final AttachmentType<List<String>> UNEARTHED_ERAS = AttachmentRegistry.create(
			EchoesOfThePast.id("unearthed_eras"),
			builder -> builder.initializer(List::of).persistent(Codec.STRING.listOf())
	);

	/** On a player: every kind of event they have witnessed. */
	public static final AttachmentType<List<String>> WITNESSED_EVENTS = AttachmentRegistry.create(
			EchoesOfThePast.id("witnessed_events"),
			builder -> builder.initializer(List::of).persistent(Codec.STRING.listOf()).copyOnDeath()
	);

	/** On a player: whether they have been given the field guide. */
	public static final AttachmentType<Boolean> RECEIVED_GUIDE = AttachmentRegistry.create(
			EchoesOfThePast.id("received_guide"),
			builder -> builder.initializer(() -> false).persistent(Codec.BOOL).copyOnDeath()
	);

	private ModAttachments() {
	}

	public static void init() {
	}
}
