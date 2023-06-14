package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.TrailRenderer;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import absolutelyaya.ultracraft.client.rendering.block.entity.CerberusBlockRenderer;
import absolutelyaya.ultracraft.client.rendering.block.entity.PedestalBlockEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.CerberusRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceModel;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.EnragedFeature;
import absolutelyaya.ultracraft.client.rendering.entity.feature.EnragedModel;
import absolutelyaya.ultracraft.client.rendering.entity.feature.WingsFeature;
import absolutelyaya.ultracraft.client.rendering.entity.feature.WingsModel;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.SchismRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.StrayRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.SwordsmachineRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.other.InterruptableChargeModel;
import absolutelyaya.ultracraft.client.rendering.entity.other.InterruptableChargeRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.other.ShockwaveModel;
import absolutelyaya.ultracraft.client.rendering.entity.other.ShockwaveRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.*;
import absolutelyaya.ultracraft.client.sound.MovingMachineSwordSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingSlideSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingSwordsmachineSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingWindSoundInstance;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import absolutelyaya.ultracraft.entity.projectile.ThrownMachineSwordEntity;
import absolutelyaya.ultracraft.particle.*;
import absolutelyaya.ultracraft.particle.goop.GoopDropParticle;
import absolutelyaya.ultracraft.particle.goop.GoopParticle;
import absolutelyaya.ultracraft.particle.goop.GoopStringParticle;
import absolutelyaya.ultracraft.registry.*;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.WaterBubbleParticle;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import software.bernie.geckolib.network.GeckoLibNetwork;

@Environment(EnvType.CLIENT)
public class UltracraftClient implements ClientModInitializer
{
	public static final EntityModelLayer WINGS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "wings"), "main");
	public static final EntityModelLayer MALICIOUS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "malicious"), "main");
	public static final EntityModelLayer ENRAGE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "enraged"), "main");
	public static final EntityModelLayer SHOCKWAVE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "shockwave"), "main");
	public static final EntityModelLayer INTERRUPTABLE_CHARGE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "interruptable_charge"), "main");
	public static ClientHitscanHandler HITSCAN_HANDLER;
	public static TrailRenderer TRAIL_RENDERER;
	public static boolean REPLACE_MENU_MUSIC = true, applyEntityPoses;
	static boolean HiVelMode = false;
	static GameruleRegistry.Option HiVelOption = GameruleRegistry.Option.FREE;
	static GameruleRegistry.Option TimeFreezeOption = GameruleRegistry.Option.FORCE_ON;
	static GameruleRegistry.RegenOption BloodRegen = GameruleRegistry.RegenOption.ALWAYS;
	static boolean disableHandswap = false, slamStorage = true, fallDamage = false, drowning = false, effectivelyViolent = false, wasMovementSoundsEnabled;
	public static int jumpBoost, speed, gravityReduction;
	static float screenblood;
	
	static UltraHudRenderer hudRenderer;
	static ConfigHolder<Ultraconfig> config;
	
	@Override
	public void onInitializeClient()
	{
		config = AutoConfig.register(Ultraconfig.class, GsonConfigSerializer::new);
		KeybindRegistry.register();
		
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.STRAY, StrayRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SCHISM, SchismRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.CERBERUS, CerberusRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SWORDSMACHINE, SwordsmachineRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.CERBERUS_BALL, CerberusBallRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SHOTGUN_PELLET, ShotgunPelletRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.EJECTED_CORE, EjectedCoreRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.THROWN_MACHINE_SWORD, ThrownMachineSwordRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SHOCKWAVE, ShockwaveRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.INTERRUPTABLE_CHARGE, InterruptableChargeRenderer::new);
		//Particles
		ParticleFactoryRegistry particleRegistry = ParticleFactoryRegistry.getInstance();
		particleRegistry.register(ParticleRegistry.MALICIOUS_CHARGE, MaliciousChargeParticle.MaliciousChargeParticleFactory::new);
		particleRegistry.register(ParticleRegistry.DASH, DashParticle.DashParticleFactory::new);
		particleRegistry.register(ParticleRegistry.SLIDE, SlideParticle.SlideParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GROUND_POUND, GroundPoundParticle.GroundPoundParticleFactory::new);
		particleRegistry.register(ParticleRegistry.EJECTED_CORE_FLASH, EjectedCoreFlashParticle.EjectedCoreFlashParticleFactory::new);
		particleRegistry.register(ParticleRegistry.BLOOD_SPLASH, WaterSplashParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.BLOOD_BUBBLE, WaterBubbleParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.RIPPLE, RippleParticle.RippleFactory::new);
		particleRegistry.register(ParticleRegistry.PARRY_INDICATOR, ParryIndicatorParticle.ParryIndicatorParticleFactory::new);
		particleRegistry.register(ParticleRegistry.TELEPORT, TeleportParticle.TeleportParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GOOP_DROP, GoopDropParticle.GoopDropParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GOOP, GoopParticle.GoopParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GOOP_STRING, GoopStringParticle.GoopStringParticleFactory::new);
		//Entity model layers
		EntityModelLayerRegistry.registerModelLayer(WINGS_LAYER, WingsModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MALICIOUS_LAYER, MaliciousFaceModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ENRAGE_LAYER, EnragedModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(SHOCKWAVE_LAYER, ShockwaveModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(INTERRUPTABLE_CHARGE_LAYER, InterruptableChargeModel::getTexturedModelData);
		
		BlockEntityRendererFactories.register(BlockEntityRegistry.PEDESTAL, PedestalBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(BlockEntityRegistry.CERBERUS, context -> new CerberusBlockRenderer());
		
		ModelPredicateRegistry.registerModels();
		
		HITSCAN_HANDLER = new ClientHitscanHandler();
		TRAIL_RENDERER = new TrailRenderer();
		
		ClientTickEvents.END_WORLD_TICK.register((client) -> HITSCAN_HANDLER.tick());
		
		hudRenderer = new UltraHudRenderer();
		WorldRenderEvents.END.register((context) -> hudRenderer.render(context.tickDelta(), context.camera()));
		
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(isHiVelEnabled());
			ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_C2S_PACKET_ID, buf);
			
			if(config.get().serverJoinInfo)
			{
				GameruleRegistry.Option hivel = client.world.getGameRules().get(GameruleRegistry.HI_VEL_MODE).get();
				GameruleRegistry.Option freeze = client.world.getGameRules().get(GameruleRegistry.TIME_STOP).get();
				client.player.sendMessage(Text.translatable("message.ultracraft.join-info-header"));
				if(!hivel.equals(GameruleRegistry.Option.FREE))
					client.player.sendMessage(Text.translatable("message.ultracraft.hi-vel-forced",
							hivel.equals(GameruleRegistry.Option.FORCE_ON) ? Text.translatable("options.on") : Text.translatable("options.off")));
				else
					client.player.sendMessage(Text.translatable("message.ultracraft.hi-vel-free"));
				if(client.getServer() != null && client.getServer().isRemote())
					client.player.sendMessage(Text.translatable("message.ultracraft.freeze-forced",
							freeze.equals(GameruleRegistry.Option.FORCE_ON) ? Text.translatable("options.on") : Text.translatable("options.off")));
				client.player.sendMessage(Text.translatable("message.ultracraft.jump-boost", jumpBoost));
				if(fallDamage)
					client.player.sendMessage(Text.translatable("message.ultracraft.fall-damage"));
				client.player.sendMessage(Text.translatable("message.ultracraft.join-info"));
				client.player.sendMessage(Text.translatable("========================================="));
			}
		});
		
		ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
			if (entity instanceof PlayerEntity player)
			{
				if(config.get().movementSounds && player.getUuid().equals(MinecraftClient.getInstance().player.getUuid()))
				{
					SoundManager sound = MinecraftClient.getInstance().getSoundManager();
					sound.play(new MovingSlideSoundInstance(player));
					sound.play(new MovingWindSoundInstance(player));
				}
				if(player.getUuid().equals(MinecraftClient.getInstance().player.getUuid()))
					return;
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeUuid(player.getUuid());
				ClientPlayNetworking.send(PacketRegistry.REQUEST_HIVEL_PACKET_ID, buf);
			}
			else if (entity instanceof ThrownMachineSwordEntity sword)
				MinecraftClient.getInstance().getSoundManager().play(new MovingMachineSwordSoundInstance(sword));
			else if (entity instanceof SwordsmachineEntity sm)
				MinecraftClient.getInstance().getSoundManager().play(new MovingSwordsmachineSoundInstance(sm));
		});
		
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if(type.equals(EntityRegistry.MALICIOUS_FACE))
				helper.register(new EnragedFeature<>(context.getModelLoader()));
			if(type.equals(EntityType.PLAYER))
				helper.register(new WingsFeature<>((PlayerEntityRenderer)renderer, context.getModelLoader()));
		});
		
		WorldRenderEvents.BEFORE_ENTITIES.register((ctx) -> applyEntityPoses = true);
		
		WorldRenderEvents.AFTER_ENTITIES.register((ctx) -> {
			UltracraftClient.HITSCAN_HANDLER.render(ctx.matrixStack(), ctx.camera());
			UltracraftClient.TRAIL_RENDERER.render(ctx.matrixStack(), ctx.camera());
			applyEntityPoses = false;
		});
		
		HudRenderCallback.EVENT.register((matrices, delta) -> {
			if(config.getConfig().bloodOverlay)
			{
				RenderSystem.enableBlend();
				String bloodName = config.get().danganronpa ? "textures/misc/blood_overlay_c" : "textures/misc/blood_overlay";
				MinecraftClient.getInstance().inGameHud.renderOverlay(matrices, new Identifier(Ultracraft.MOD_ID, bloodName + "3.png"),
						Math.min(screenblood - 1.25f, 0.75f));
				MinecraftClient.getInstance().inGameHud.renderOverlay(matrices, new Identifier(Ultracraft.MOD_ID, bloodName + "2.png"),
						Math.min(screenblood - 0.25f, Math.max(0.6f - Math.min(screenblood - 0.75f, 0.6f), 0f)));
				MinecraftClient.getInstance().inGameHud.renderOverlay(matrices, new Identifier(Ultracraft.MOD_ID, bloodName + "1.png"),
						Math.min(screenblood - 0.75f, 0.6f));
				screenblood = Math.max(0f, screenblood - delta / 120);
				
				if(Ultracraft.isTimeFrozen())
					MinecraftClient.getInstance().inGameHud.renderOverlay(matrices, new Identifier(Ultracraft.MOD_ID, "textures/misc/time_freeze_overlay.png"),
						0.25f);
			}
		});
		
		ClientPacketRegistry.registerS2C();
		GeckoLibNetwork.registerClientReceiverPackets();
		
		ClientTickEvents.END_WORLD_TICK.register(minecraft -> {
			Ultracraft.tickFreeze();
			UltracraftClient.TRAIL_RENDERER.tick();
			if(!wasMovementSoundsEnabled && config.get().movementSounds)
			{
				PlayerEntity player = MinecraftClient.getInstance().player;
				if(player == null)
					return;
				SoundManager sound = MinecraftClient.getInstance().getSoundManager();
				sound.play(new MovingSlideSoundInstance(player));
				sound.play(new MovingWindSoundInstance(player));
			}
			wasMovementSoundsEnabled = config.get().movementSounds;
		});
		ClientSendMessageEvents.CHAT.register(message -> {
			PlayerEntity player = MinecraftClient.getInstance().player;
			if(player == null)
				return;
			if(message.equals("Press alt to throw it back"))
				player.world.getEntitiesByType(EntityRegistry.FILTH, player.getBoundingBox().expand(128.0), entity -> true)
						.forEach(FilthEntity::throwback);
		});
		FluidRenderHandlerRegistry.INSTANCE.register(FluidRegistry.STILL_BLOOD, FluidRegistry.Flowing_BLOOD,
				new SimpleFluidRenderHandler(new Identifier(Ultracraft.MOD_ID, "block/blood_still"), new Identifier(Ultracraft.MOD_ID, "block/blood_flow")));
		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getSolid(), FluidRegistry.STILL_BLOOD, FluidRegistry.Flowing_BLOOD);
	}
	
	public static void addBlood(float f)
	{
		screenblood = Math.min(3.5f, screenblood + f);
	}
	
	public static void clearBlood()
	{
		screenblood = 0f;
	}
	
	//if no Server override, return client setting
	public static boolean isFreezeEnabled()
	{
		if(MinecraftClient.getInstance().isInSingleplayer())
			return config.get().freezeVFX;
		else
			return TimeFreezeOption.equals(GameruleRegistry.Option.FORCE_ON);
	}
	
	public static boolean isHiVelEnabled()
	{
		GameruleRegistry.Option option = HiVelOption;
		if(option.equals(GameruleRegistry.Option.FREE))
			return HiVelMode;
		else
			return option.equals(GameruleRegistry.Option.FORCE_ON);
	}
	
	public static boolean isHandSwapEnabled()
	{
		return disableHandswap;
	}
	
	public static void toggleHiVelEnabled()
	{
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player == null)
			return;
		GameruleRegistry.Option option = HiVelOption;
		if(option.equals(GameruleRegistry.Option.FREE))
		{
			HiVelMode = !HiVelMode;
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeUuid(player.getUuid());
			buf.writeBoolean(HiVelMode);
			ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_C2S_PACKET_ID, buf);
		}
		else
			player.sendMessage(
					Text.translatable("message.ultracraft.hi-vel-forced",
									Text.translatable(option.equals(GameruleRegistry.Option.FORCE_ON) ? "options.on" : "options.off")), true);
	}
	
	public static boolean isSlamStorageEnabled()
	{
		return slamStorage;
	}
	
	public static boolean isViolentFeaturesEnabled(World world)
	{
		return world.getDifficulty() == Difficulty.HARD || effectivelyViolent;
	}
	
	public static void setHighVel(boolean b, boolean fromServer)
	{
		PlayerEntity player = MinecraftClient.getInstance().player;
		HiVelMode = b;
		if(!fromServer && player != null)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(HiVelMode);
			ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_C2S_PACKET_ID, buf);
		}
	}
	
	public static ConfigHolder<Ultraconfig> getConfigHolder()
	{
		return config;
	}
	
	public static void syncGameRule(byte data)
	{
		int rule = data / 10;
		int value = data - rule * 10;
		switch (rule)
		{
			case 1 ->
			{
				HiVelOption = GameruleRegistry.Option.values()[value];
				if(HiVelOption != GameruleRegistry.Option.FREE)
					setHighVel(HiVelOption == GameruleRegistry.Option.FORCE_ON, false);
			}
			case 2 -> TimeFreezeOption = GameruleRegistry.Option.values()[value];
			case 3 -> disableHandswap = value == 1;
			case 5 -> slamStorage = value == 1;
			case 6 -> fallDamage = value == 1;
			case 7 -> drowning = value == 1;
			case 8 -> BloodRegen = GameruleRegistry.RegenOption.values()[value];
			case 11 -> effectivelyViolent = value == 1;
			default -> Ultracraft.LOGGER.error("Received invalid Packet data: [rule_syncB] -> " + data);
		}
	}
	
	public static void syncGameRule(byte data, int value)
	{
		int rule = data / 10;
		switch (rule)
		{
			case 4 -> jumpBoost = value;
			case 9 -> speed = value;
			case 10 -> gravityReduction = value;
			default -> Ultracraft.LOGGER.error("Received invalid Packet data: [rule_syncI] -> " + data);
		}
	}
}
