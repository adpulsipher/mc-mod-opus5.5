package io.github.adpulsipher.echoes.component;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.adpulsipher.echoes.history.Era;
import io.github.adpulsipher.echoes.history.EventType;
import io.github.adpulsipher.echoes.history.Terrain;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

/**
 * The memory held by an extracted echo block: where and how deep it formed, and what it remembers.
 */
public record EchoMemory(String dimension, int chunkX, int chunkZ, int depth, String terrain, String eventType, String title) implements TooltipProvider {
	public static final Codec<EchoMemory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("dimension").forGetter(EchoMemory::dimension),
			Codec.INT.fieldOf("chunk_x").forGetter(EchoMemory::chunkX),
			Codec.INT.fieldOf("chunk_z").forGetter(EchoMemory::chunkZ),
			Codec.INT.fieldOf("depth").forGetter(EchoMemory::depth),
			Codec.STRING.fieldOf("terrain").forGetter(EchoMemory::terrain),
			Codec.STRING.fieldOf("event_type").forGetter(EchoMemory::eventType),
			Codec.STRING.fieldOf("title").forGetter(EchoMemory::title)
	).apply(instance, EchoMemory::new));

	public static final StreamCodec<ByteBuf, EchoMemory> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, EchoMemory::dimension,
			ByteBufCodecs.VAR_INT, EchoMemory::chunkX,
			ByteBufCodecs.VAR_INT, EchoMemory::chunkZ,
			ByteBufCodecs.VAR_INT, EchoMemory::depth,
			ByteBufCodecs.STRING_UTF8, EchoMemory::terrain,
			ByteBufCodecs.STRING_UTF8, EchoMemory::eventType,
			ByteBufCodecs.STRING_UTF8, EchoMemory::title,
			EchoMemory::new
	);

	public Era era() {
		return Era.fromDepth(depth);
	}

	public Terrain terrainValue() {
		try {
			return Terrain.valueOf(terrain);
		} catch (IllegalArgumentException e) {
			return Terrain.PLAINS;
		}
	}

	public EventType type() {
		return EventType.byId(eventType);
	}

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
		tooltip.accept(Component.literal("“" + title + "”").withStyle(Style.EMPTY.withColor(era().tint()).withItalic(true)));
		tooltip.accept(Component.translatable("tooltip.echoes_of_the_past.echo.era", era().title()).withStyle(Style.EMPTY.withColor(0xA0A0B8)));
		tooltip.accept(Component.translatable("tooltip.echoes_of_the_past.echo.origin", chunkX * 16 + 8, depth, chunkZ * 16 + 8).withStyle(Style.EMPTY.withColor(0x707088)));
	}
}
