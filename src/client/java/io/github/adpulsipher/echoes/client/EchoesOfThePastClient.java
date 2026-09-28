package io.github.adpulsipher.echoes.client;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.client.model.ColossusModel;
import io.github.adpulsipher.echoes.client.model.CrawlerModel;
import io.github.adpulsipher.echoes.client.model.GhostHumanoidModel;
import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.client.model.WispModel;
import io.github.adpulsipher.echoes.client.render.EchoFigureRenderer;
import io.github.adpulsipher.echoes.client.render.EchoProjectorRenderer;
import io.github.adpulsipher.echoes.client.render.EchoWyrmRenderer;
import io.github.adpulsipher.echoes.client.render.HierophantRenderer;
import io.github.adpulsipher.echoes.client.render.HumanoidGhostRenderer;
import io.github.adpulsipher.echoes.client.render.LingererRenderer;
import io.github.adpulsipher.echoes.client.render.MemoryMothRenderer;
import io.github.adpulsipher.echoes.client.render.ModelGhostRenderer;
import io.github.adpulsipher.echoes.client.screen.ShowcaseScreen;
import io.github.adpulsipher.echoes.entity.AshRevenantEntity;
import io.github.adpulsipher.echoes.entity.DawnWispEntity;
import io.github.adpulsipher.echoes.entity.EchoKnightEntity;
import io.github.adpulsipher.echoes.entity.HierophantEntity;
import io.github.adpulsipher.echoes.entity.HollowKingEntity;
import io.github.adpulsipher.echoes.entity.ShardCrawlerEntity;
import io.github.adpulsipher.echoes.entity.SiegeColossusEntity;
import io.github.adpulsipher.echoes.entity.SpectralArcherEntity;
import io.github.adpulsipher.echoes.item.ShowcaseBookItem;
import io.github.adpulsipher.echoes.registry.ModBlockEntities;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.replay.Pose;
import io.github.adpulsipher.echoes.replay.Role;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class EchoesOfThePastClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModModelLayers.init();
		EntityRenderers.register(ModEntities.ECHO_FIGURE, EchoFigureRenderer::new);
		EntityRenderers.register(ModEntities.LINGERER, LingererRenderer::new);
		EntityRenderers.register(ModEntities.MEMORY_MOTH, MemoryMothRenderer::new);
		EntityRenderers.register(ModEntities.ECHO_WYRM, EchoWyrmRenderer::new);

		EntityRenderers.register(ModEntities.ECHO_KNIGHT, context -> new HumanoidGhostRenderer<EchoKnightEntity>(context,
				EchoesOfThePast.id("textures/entity/echo_knight.png"), 1.05f, 0.82f, (entity, state, partialTick) -> {
					state.role = Role.KNIGHT;
					state.tint = 0xB8D4FF;
					if (entity.isCharging()) {
						state.pose = Pose.RUN;
					}
				}));
		EntityRenderers.register(ModEntities.SPECTRAL_ARCHER, context -> new HumanoidGhostRenderer<SpectralArcherEntity>(context,
				EchoesOfThePast.id("textures/entity/spectral_archer.png"), 1.0f, 0.75f, (entity, state, partialTick) -> {
					state.role = Role.ARCHER;
					state.tint = 0xA8F4E8;
					state.pose = entity.isDrawing() ? Pose.SHOOT : Pose.IDLE;
				}));
		EntityRenderers.register(ModEntities.ASH_REVENANT, context -> new HumanoidGhostRenderer<AshRevenantEntity>(context,
				EchoesOfThePast.id("textures/entity/ash_revenant.png"), 1.0f, 0.9f, (entity, state, partialTick) -> {
					state.role = Role.MINER;
					state.props = GhostHumanoidModel.PROP_PICKAXE;
					state.tint = 0xFFC08A;
				}));
		EntityRenderers.register(ModEntities.HOLLOW_KING, context -> new HumanoidGhostRenderer<HollowKingEntity>(context,
				EchoesOfThePast.id("textures/entity/hollow_king.png"), 2.4f, 0.9f, (entity, state, partialTick) -> {
					state.role = Role.NOBLE;
					state.props = GhostHumanoidModel.PROP_SWORD | GhostHumanoidModel.PROP_CROWN;
					state.enraged = entity.isEnraged();
					state.tint = state.enraged ? 0xFFE6A0 : 0xE8DCFF;
					state.attackState = entity.getAttackState();
					state.poseTime = entity.attackTime(partialTick);
					state.pose = switch (entity.getAttackState()) {
						case HollowKingEntity.CLEAVE -> Pose.ATTACK;
						case HollowKingEntity.DECREE -> Pose.CAST;
						case HollowKingEntity.CHARGE -> Pose.RUN;
						case HollowKingEntity.SUMMON -> Pose.CHEER;
						default -> Pose.IDLE;
					};
				}));
		EntityRenderers.register(ModEntities.HIEROPHANT, context -> new HierophantRenderer(context,
				EchoesOfThePast.id("textures/entity/hierophant.png"), 1.8f, (entity, state, partialTick) -> {
					state.role = Role.MAGE;
					state.props = GhostHumanoidModel.PROP_STAFF | GhostHumanoidModel.PROP_CROWN;
					state.enraged = entity.isEnraged();
					state.tint = state.enraged ? 0xFFF4D0 : 0xFFEBC0;
					state.count = entity.getWardCount();
					state.attackState = entity.getAttackState();
					state.poseTime = entity.attackTime(partialTick);
					state.pose = entity.getAttackState() == HierophantEntity.DRIFT ? Pose.IDLE : Pose.CAST;
					state.strideAmount = 0.0f;
				}));
		EntityRenderers.register(ModEntities.SIEGE_COLOSSUS, context -> new ModelGhostRenderer<SiegeColossusEntity>(context,
				new ColossusModel(context.bakeLayer(ModModelLayers.SIEGE_COLOSSUS)), 2.0f, EchoesOfThePast.id("textures/entity/siege_colossus.png"),
				1.8f, 0.92f, true, (entity, state, partialTick) -> {
					state.enraged = entity.isEnraged();
					state.tint = state.enraged ? 0xFFC8A0 : 0xE4ECF6;
					state.attackState = entity.getAttackState();
					state.attackTime = entity.attackTime(partialTick);
				}));
		EntityRenderers.register(ModEntities.DAWN_WISP, context -> new ModelGhostRenderer<DawnWispEntity>(context,
				new WispModel(context.bakeLayer(ModModelLayers.DAWN_WISP)), 0.2f, EchoesOfThePast.id("textures/entity/dawn_wisp.png"),
				1.0f, 0.85f, true, (entity, state, partialTick) -> state.tint = 0xFFF0C8));
		EntityRenderers.register(ModEntities.SHARD_CRAWLER, context -> new ModelGhostRenderer<ShardCrawlerEntity>(context,
				new CrawlerModel(context.bakeLayer(ModModelLayers.SHARD_CRAWLER)), 0.4f, EchoesOfThePast.id("textures/entity/shard_crawler.png"),
				1.0f, 1.0f, false, (entity, state, partialTick) -> {
				}));

		BlockEntityRenderers.register(ModBlockEntities.ECHO_PROJECTOR, EchoProjectorRenderer::new);

		ShowcaseBookItem.openScreen = player -> io.github.adpulsipher.echoes.client.screen.Screens.open(new ShowcaseScreen());
	}
}
