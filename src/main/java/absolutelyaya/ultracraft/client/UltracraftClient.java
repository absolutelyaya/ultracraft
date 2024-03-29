package absolutelyaya.ultracraft.client;

import absolutelyaya.goop.client.GoopClient;
import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.api.terminal.TerminalCodeRegistry;
import absolutelyaya.ultracraft.client.gui.screen.EpilepsyPopupScreen;
import absolutelyaya.ultracraft.client.gui.screen.ServerConfigScreen;
import absolutelyaya.ultracraft.client.gui.terminal.PetTab;
import absolutelyaya.ultracraft.client.rendering.TrailRenderer;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import absolutelyaya.ultracraft.client.rendering.block.entity.*;
import absolutelyaya.ultracraft.client.rendering.entity.demon.*;
import absolutelyaya.ultracraft.client.rendering.entity.feature.*;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.GreaterFilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.SchismRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.StrayRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.DroneEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.StreetCleanerEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.SwordsmachineRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.V2Renderer;
import absolutelyaya.ultracraft.client.rendering.entity.other.*;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.*;
import absolutelyaya.ultracraft.client.sound.MovingMachineSwordSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingSlideSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingSwordsmachineSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingWindSoundInstance;
import absolutelyaya.ultracraft.compat.PlayerAnimator;
import absolutelyaya.ultracraft.components.player.IWingDataComponent;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import absolutelyaya.ultracraft.entity.projectile.ThrownMachineSwordEntity;
import absolutelyaya.ultracraft.particle.*;
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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.particle.WaterBubbleParticle;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class UltracraftClient implements ClientModInitializer
{
	public static final EntityModelLayer WINGS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "wings"), "main");
	public static final EntityModelLayer MALICIOUS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "malicious"), "main");
	public static final EntityModelLayer ENRAGE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "enraged"), "main");
	public static final EntityModelLayer INTERRUPTABLE_CHARGE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "interruptable_charge"), "main");
	public static String wingPreset = "", wingPattern = "";
	private static ShaderProgram wingsColoredProgram, wingsColoredUIProgram, texPosFade, flesh, sky;
	public static ClientHitscanHandler HITSCAN_HANDLER;
	public static TrailRenderer TRAIL_RENDERER;
	public static boolean REPLACE_MENU_MUSIC = true, APPLY_ENTITY_POSES, GRAFFITI_WHITELISTED = true, SODIUM = true, IRIS = false;
	static GameruleRegistry.Setting HiVelOption = GameruleRegistry.Setting.FREE;
	static GameruleRegistry.Setting TimeFreezeOption = GameruleRegistry.Setting.FORCE_ON;
	static GameruleRegistry.RegenSetting bloodRegen = GameruleRegistry.RegenSetting.ALWAYS;
	static GameruleRegistry.ProjectileBoostSetting projBoost = GameruleRegistry.ProjectileBoostSetting.LIMITED;
	static GameruleRegistry.GraffitiSetting GraffitiOption = GameruleRegistry.GraffitiSetting.ALLOW_ALL;
	static boolean disableHandswap = false, slamStorage = true, fallDamage = false, drowning = false, effectivelyViolent = false, wasMovementSoundsEnabled, parryChaining, supporter = false, joinInfoPending, terminalProt;
	public static int jumpBoost, speed, gravityReduction;
	static float screenblood;
	static Vector3f[] wingColors = new Vector3f[] { new Vector3f(247f, 255f, 154f), new Vector3f(117f, 154f, 255f) };
	static final Vector3f[] defaultWingColors = new Vector3f[] { new Vector3f(247f, 255f, 154f), new Vector3f(117f, 154f, 255f) };
	static int visualFreezeTicks;
	static Optional<Boolean> forcedHivel = Optional.empty();
	
	static UltraHudRenderer hudRenderer;
	static ConfigHolder<Ultraconfig> config;
	
	@Override
	public void onInitializeClient()
	{
		SODIUM = FabricLoader.getInstance().getModContainer("sodium").isPresent();
		IRIS = FabricLoader.getInstance().getModContainer("iris").isPresent();
		
		config = AutoConfig.register(Ultraconfig.class, GsonConfigSerializer::new);
		KeybindRegistry.register();
		
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.STRAY, StrayRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SCHISM, SchismRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.CERBERUS, CerberusRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HIDEOUS_MASS, HideousMassRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.RETALIATION, RetaliationRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SWORDSMACHINE, SwordsmachineRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.DESTINY_SWORDSMACHINE, SwordsmachineRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.CERBERUS_BALL, CerberusBallRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SHOTGUN_PELLET, ShotgunPelletRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.CANCER_BULLET, CancerBulletRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.EJECTED_CORE, EjectedCoreRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.THROWN_MACHINE_SWORD, ThrownMachineSwordRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.THROWN_COIN, ThrownCoinRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.FLAME, FlameRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MORTAR, HideousMortarRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HARPOON, HarpoonEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SOAP, ThrownSoapRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MAGNET, MagnetEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.NAIL, NailEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SHOCKWAVE, ShockwaveRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.VERICAL_SHOCKWAVE, VerticalShockwaveRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.INTERRUPTABLE_CHARGE, InterruptableChargeRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SOUL_ORB, OrbRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.BLOOD_ORB, OrbRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.DRONE, DroneEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.STREET_CLEANER, StreetCleanerEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.BACK_TANK, BackTankRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.STAINED_GLASS_WINDOW, StainedGlassWindowRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.PROGRESSION_ITEM, ItemEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.V2, V2Renderer::new);
		EntityRendererRegistry.register(EntityRegistry.BEAM, BeamProjectileRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.RODENT, RodentRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.GREATER_FILTH, GreaterFilthRenderer::new);
		//Particles
		ParticleFactoryRegistry particleRegistry = ParticleFactoryRegistry.getInstance();
		particleRegistry.register(ParticleRegistry.MALICIOUS_CHARGE, MaliciousChargeParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.DASH, DashParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.SLIDE, SlideParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.GROUND_POUND, GroundPoundParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.EJECTED_CORE_FLASH, EjectedCoreFlashParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.BLOOD_SPLASH, WaterSplashParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.BLOOD_BUBBLE, WaterBubbleParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.SOAP_BUBBLE, SoapBubbleParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.RIPPLE, RippleParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.PARRY_INDICATOR, ParryIndicatorParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.TELEPORT, TeleportParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.EXPLOSION, ExplosionParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.RICOCHET_WARNING, RicochetWarningParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.BIG_CIRCLE, BigCircleParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.DRONE_CHARGE, DroneChargeParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.SHOCK, ShockParticle.Factory::new);
		//Entity model layers
		EntityModelLayerRegistry.registerModelLayer(WINGS_LAYER, WingsModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MALICIOUS_LAYER, MaliciousFaceModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ENRAGE_LAYER, EnragedModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(INTERRUPTABLE_CHARGE_LAYER, InterruptableChargeModel::getTexturedModelData);
		//BlockEntityRenderers
		BlockEntityRendererFactories.register(BlockEntityRegistry.PEDESTAL, PedestalBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(BlockEntityRegistry.CERBERUS, context -> new CerberusBlockRenderer());
		BlockEntityRendererFactories.register(BlockEntityRegistry.TERMINAL, context -> new TerminalBlockEntityRenderer());
		BlockEntityRendererFactories.register(BlockEntityRegistry.HELL_OBSERVER, context -> new HellObserverRenderer());
		BlockEntityRendererFactories.register(BlockEntityRegistry.HELL_SPAWNER, context -> new HellSpawnerBlockRenderer());
		BlockEntityRendererFactories.register(BlockEntityRegistry.SKY, context -> new SkyBlockRenderer());
		//Player Animations
		PlayerAnimator.init();
		
		ModelPredicateRegistry.registerModels();
		ScreenHandlerRegistry.registerClient();
		
		WingColorPresetManager.restoreDefaults();
		
		HITSCAN_HANDLER = new ClientHitscanHandler();
		TRAIL_RENDERER = new TrailRenderer();
		
		ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("ultracraft_non_essential"),
				FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID).orElseThrow(), Text.literal("ULTRACRAFT Non-Essential"),
				ResourcePackActivationType.DEFAULT_ENABLED);
		
		ClientTickEvents.END_WORLD_TICK.register((client) -> {
			HITSCAN_HANDLER.tick();
			if(visualFreezeTicks > 0)
				visualFreezeTicks--;
		});
		
		hudRenderer = new UltraHudRenderer();
		WorldRenderEvents.END.register((context) -> hudRenderer.render(context.tickDelta(), context.camera()));
		
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			refreshSupporter();
			IWingDataComponent wings = UltraComponents.WING_DATA.get(client.player);
			wings.setColor(wingColors[0], 0);
			wings.setColor(wingColors[1], 1);
			wings.setPattern(wingPattern);
			if(forcedHivel.isEmpty())
				wings.setVisible(config.get().hivel);
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(wings.isActive());
			buf.writeVector3f(wings.getColors()[0]);
			buf.writeVector3f(wings.getColors()[1]);
			buf.writeString(wings.getPattern());
			ClientPlayNetworking.send(PacketRegistry.SEND_WING_DATA_C2S_PACKET_ID, buf);
			buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(config.get().armVisible);
			ClientPlayNetworking.send(PacketRegistry.ARM_VISIBLE_PACKET_ID, buf);
			if(config.get().showEpilepsyWarning)
				MinecraftClient.getInstance().setScreen(new EpilepsyPopupScreen(null));
			if(config.get().serverJoinInfo)
				joinInfoPending = true;
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
				UltraComponents.WING_DATA.get(player).sync();
				if(player.getUuid().equals(MinecraftClient.getInstance().player.getUuid()))
					return;
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeUuid(player.getUuid());
				ClientPlayNetworking.send(PacketRegistry.REQUEST_WINGED_DATA_PACKET_ID, buf);
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
			{
				helper.register(new WingsFeature<>((PlayerEntityRenderer)renderer, context.getModelLoader()));
				helper.register(new PlayerBackTankFeature<>((PlayerEntityRenderer)renderer));
				helper.register(new ArmFeature<>((PlayerEntityRenderer)renderer));
			}
		});
		
		WorldRenderEvents.BEFORE_ENTITIES.register((ctx) -> APPLY_ENTITY_POSES = true);
		
		WorldRenderEvents.AFTER_ENTITIES.register((ctx) -> {
			UltracraftClient.HITSCAN_HANDLER.render(ctx.matrixStack(), ctx.camera(), ctx.tickDelta());
			UltracraftClient.TRAIL_RENDERER.render(ctx.matrixStack(), ctx.camera());
			APPLY_ENTITY_POSES = false;
		});
		
		HudRenderCallback.EVENT.register((matrices, delta) -> {
			if(config.get().safeVFX)
				return;
			if(config.get().bloodOverlay)
			{
				RenderSystem.enableBlend();
				String bloodName = GoopClient.getConfig().censorMature ? "textures/misc/blood_overlay_c" : "textures/misc/blood_overlay";
				MinecraftClient.getInstance().inGameHud.renderOverlay(matrices, new Identifier(Ultracraft.MOD_ID, bloodName + "3.png"),
						Math.min(screenblood - 1.25f, 0.75f));
				MinecraftClient.getInstance().inGameHud.renderOverlay(matrices, new Identifier(Ultracraft.MOD_ID, bloodName + "2.png"),
						Math.min(screenblood - 0.25f, Math.max(0.6f - Math.min(screenblood - 0.75f, 0.6f), 0f)));
				MinecraftClient.getInstance().inGameHud.renderOverlay(matrices, new Identifier(Ultracraft.MOD_ID, bloodName + "1.png"),
						Math.min(screenblood - 0.75f, 0.6f));
				screenblood = Math.max(0f, screenblood - delta / 120);
			}
			if(visualFreezeTicks > 0)
				MinecraftClient.getInstance().inGameHud.renderOverlay(matrices, new Identifier(Ultracraft.MOD_ID, "textures/misc/time_freeze_overlay.png"),
						0.25f);
		});
		
		WingPatterns.init();
		CoreShaderRegistrationCallback.EVENT.register((callback) -> {
			callback.register(new Identifier(Ultracraft.MOD_ID, "rendertype_wings_colored"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.getUniform("Pattern");
				program.markUniformsDirty();
				wingsColoredProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wings_colored_ui"), VertexFormats.POSITION_TEXTURE_COLOR, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.getUniform("Pattern");
				program.markUniformsDirty();
				wingsColoredUIProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "position_tex_fade"), VertexFormats.POSITION_TEXTURE_COLOR, (program) -> {
				program.getUniform("TextureSize");
				program.markUniformsDirty();
				texPosFade = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "flesh"), VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, (program) -> flesh = program);
			callback.register(new Identifier(Ultracraft.MOD_ID, "sky"), VertexFormats.POSITION_TEXTURE, (program) -> {
				program.getUniform("RotMat");
				program.markUniformsDirty();
				sky = program;
			});
		});
		
		ClientPacketRegistry.registerS2C();
		
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
		//Block Layers
		FluidRenderHandlerRegistry.INSTANCE.register(FluidRegistry.STILL_BLOOD, FluidRegistry.Flowing_BLOOD,
				new SimpleFluidRenderHandler(new Identifier(Ultracraft.MOD_ID, "block/blood_still"), new Identifier(Ultracraft.MOD_ID, "block/blood_flow")));
		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getSolid(), FluidRegistry.STILL_BLOOD, FluidRegistry.Flowing_BLOOD);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.FLESH, //prevent Sodium from crashing when trying to render Flesh Blocks
				SODIUM ? RenderLayers.getSolid() : RenderLayers.getFlesh());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.ADORNED_RAILING, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.VENT_COVER, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.SKY_BLOCK, RenderLayer.getTranslucent());
		
		TerminalCodeRegistry.registerCode("florp", t -> t.setTab(new PetTab()));
		TerminalCodeRegistry.registerCode("somethingwicked", new TerminalCodeRegistry.Result(t -> {
			t.setColorOverride(0x460006);
			MinecraftClient.getInstance().player.sendMessage(Text.of("Something Wicked this way comes"), true);
		}, SoundEvents.ENTITY_PLAYER_BREATH, 1.15f));
		
		setWingColor(config.get().wingColors[0].toVector3f(), 0);
		setWingColor(config.get().wingColors[1].toVector3f(), 1);
		wingPreset = config.get().wingPreset;
		setWingPattern(config.get().wingPattern);
		
		refreshSupporter();
	}
	
	public static void sendJoinInfo(MinecraftClient client, boolean manual)
	{
		if(client.player == null)
			return;
		client.player.sendMessage(Text.translatable("message.ultracraft.join-info-header"));
		if(!HiVelOption.equals(GameruleRegistry.Setting.FREE))
			client.player.sendMessage(Text.translatable("message.ultracraft.hi-vel-forced",
					HiVelOption.equals(GameruleRegistry.Setting.FORCE_ON) ? Text.translatable("options.on") : Text.translatable("options.off")));
		else
			client.player.sendMessage(Text.translatable("message.ultracraft.hi-vel-free"));
		if(client.getServer() != null && client.getServer().isRemote())
			client.player.sendMessage(Text.translatable("message.ultracraft.freeze-forced",
					TimeFreezeOption.equals(GameruleRegistry.Setting.FORCE_ON) ? Text.translatable("options.on") : Text.translatable("options.off")));
		client.player.sendMessage(Text.translatable("message.ultracraft.attributes", speed, jumpBoost,
				MathHelper.clamp(gravityReduction * 10, 0, 99)).append("%"));
		client.player.sendMessage(Text.translatable("message.ultracraft.blood-heal." + bloodRegen.name()));
		if(fallDamage)
			client.player.sendMessage(Text.translatable("message.ultracraft.fall-damage"));
		if(drowning)
			client.player.sendMessage(Text.translatable("message.ultracraft.drowning"));
		if(config.get().detailedJoinInfo || manual)
		{
			client.player.sendMessage(Text.translatable("message.ultracraft.projectile-boost." + projBoost.name()));
			if(disableHandswap)
				client.player.sendMessage(Text.translatable("message.ultracraft.disabled-handswap"));
			if(!slamStorage)
				client.player.sendMessage(Text.translatable("message.ultracraft.disabled-slamstorage"));
			if(effectivelyViolent)
				client.player.sendMessage(Text.translatable("message.ultracraft.effectively-violent"));
			if(parryChaining)
				client.player.sendMessage(Text.translatable("message.ultracraft.parry-chaining"));
		}
		if(!manual)
			client.player.sendMessage(Text.translatable("message.ultracraft.join-info"));
		client.player.sendMessage(Text.translatable("========================================="));
	}
	
	public static void addBlood(float f)
	{
		screenblood = Math.min(3.5f, screenblood + f);
	}
	
	public static void clearBlood()
	{
		screenblood = 0f;
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
		IWingDataComponent wings = UltraComponents.WING_DATA.get(player);
		GameruleRegistry.Setting option = HiVelOption;
		if(option.equals(GameruleRegistry.Setting.FREE))
		{
			setHiVel(!wings.isActive(), false);
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(wings.isActive());
			ClientPlayNetworking.send(PacketRegistry.SEND_WING_STATE_C2S_PACKET_ID, buf);
			config.get().hivel = wings.isActive();
			config.save();
		}
		else
			player.sendMessage(
					Text.translatable("message.ultracraft.hi-vel-forced",
									Text.translatable(option.equals(GameruleRegistry.Setting.FORCE_ON) ? "options.on" : "options.off")), true);
	}
	
	public static boolean isSlamStorageEnabled()
	{
		return slamStorage;
	}
	
	public static boolean isViolentFeaturesEnabled(World world)
	{
		return world.getDifficulty() == Difficulty.HARD || effectivelyViolent;
	}
	
	public static void setHiVel(boolean b, boolean fromServer)
	{
		if(forcedHivel.isPresent())
			b = forcedHivel.get();
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player == null)
			return;
		IWingDataComponent wings = UltraComponents.WING_DATA.get(player);
		wings.setVisible(b);
		if(!fromServer)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(b);
			ClientPlayNetworking.send(PacketRegistry.SEND_WING_STATE_C2S_PACKET_ID, buf);
		}
	}
	
	public static Ultraconfig getConfig()
	{
		return config.get();
	}
	
	public static void saveConfig()
	{
		config.save();
	}
	
	public static void syncGameRule(byte data, int value)
	{
		switch (data)
		{
			case 0 -> onExternalRuleUpdate(GameruleRegistry.PROJ_BOOST, (projBoost = GameruleRegistry.ProjectileBoostSetting.values()[value]).name());
			case 1 ->
			{
				onExternalRuleUpdate(GameruleRegistry.HIVEL_MODE, (HiVelOption = GameruleRegistry.Setting.values()[value]).name());
				if(HiVelOption != GameruleRegistry.Setting.FREE)
					forcedHivel = Optional.of(HiVelOption == GameruleRegistry.Setting.FORCE_ON);
				else
					forcedHivel = Optional.empty();
			}
			case 2 -> onExternalRuleUpdate(GameruleRegistry.TIME_STOP, (TimeFreezeOption = GameruleRegistry.Setting.values()[value]).name());
			case 3 -> onExternalRuleUpdate(GameruleRegistry.DISABLE_HANDSWAP, disableHandswap = value == 1);
			case 4 -> onExternalRuleUpdate(GameruleRegistry.HIVEL_JUMP_BOOST, jumpBoost = value);
			case 5 -> onExternalRuleUpdate(GameruleRegistry.SLAM_STORAGE, slamStorage = value == 1);
			case 6 -> onExternalRuleUpdate(GameruleRegistry.HIVEL_FALLDAMAGE, fallDamage = value == 1);
			case 7 -> onExternalRuleUpdate(GameruleRegistry.HIVEL_DROWNING, drowning = value == 1);
			case 8 -> onExternalRuleUpdate(GameruleRegistry.BLOODHEAL, (bloodRegen = GameruleRegistry.RegenSetting.values()[value]).name());
			case 9 -> onExternalRuleUpdate(GameruleRegistry.HIVEL_SPEED, speed = value);
			case 10 -> onExternalRuleUpdate(GameruleRegistry.HIVEL_SLOWFALL, gravityReduction = value);
			case 11 -> onExternalRuleUpdate(GameruleRegistry.EFFECTIVELY_VIOLENT, effectivelyViolent = value == 1);
			case 12 -> onExternalRuleUpdate(GameruleRegistry.EXPLOSION_DAMAGE, value == 1);
			case 13 -> onExternalRuleUpdate(GameruleRegistry.SM_SAFE_LEDGES, value == 1);
			case 14 -> onExternalRuleUpdate(GameruleRegistry.PARRY_CHAINING, parryChaining = value == 1);
			case 15 -> onExternalRuleUpdate(GameruleRegistry.TNT_PRIMING, value == 1);
			case 16 -> onExternalRuleUpdate(GameruleRegistry.REVOLVER_DAMAGE, value);
			case 17 -> onExternalRuleUpdate(GameruleRegistry.INVINCIBILITY, value);
			case 18 -> onExternalRuleUpdate(GameruleRegistry.TERMINAL_PROT, terminalProt = value == 1);
			case 19 -> onExternalRuleUpdate(GameruleRegistry.GRAFFITI, (GraffitiOption = GameruleRegistry.GraffitiSetting.values()[value]).name());
			case 20 -> onExternalRuleUpdate(GameruleRegistry.FLAMETHROWER_GRIEF, value == 1);
			case 21 -> onExternalRuleUpdate(GameruleRegistry.SHOTGUN_DAMAGE, value);
			case 22 -> onExternalRuleUpdate(GameruleRegistry.NAILGUN_DAMAGE, value);
			case 23 -> onExternalRuleUpdate(GameruleRegistry.HELL_OBSERVER_INTERVAL, value);
			case 24 -> onExternalRuleUpdate(GameruleRegistry.START_WITH_PIERCER, value == 1);
			case 25 -> onExternalRuleUpdate(GameruleRegistry.BLOOD_SATURATION, value == 1);
			case 127 -> gameRuleSyncFinished();
			default -> Ultracraft.LOGGER.error("Received invalid Packet data: [rule_syncB] -> " + data);
		}
	}
	
	public static void gameRuleSyncFinished()
	{
		if(joinInfoPending)
			sendJoinInfo(MinecraftClient.getInstance(), false);
		joinInfoPending = false;
	}
	
	static <V, T extends GameRules.Key<?>> void onExternalRuleUpdate(T rule, V value)
	{
		if(ServerConfigScreen.INSTANCE != null)
			ServerConfigScreen.INSTANCE.onExternalRuleUpdate(rule, String.valueOf(value));
	}
	
	public static ShaderProgram getWingsColoredShaderProgram()
	{
		return wingsColoredProgram;
	}
	
	public static ShaderProgram getWingsColoredUIShaderProgram()
	{
		return wingsColoredUIProgram;
	}
	
	public static ShaderProgram getTexPosFadeProgram()
	{
		return texPosFade;
	}
	
	public static ShaderProgram getFleshProgram()
	{
		return flesh;
	}
	
	public static ShaderProgram getDaySkyProgram()
	{
		return sky;
	}
	
	public static void setWingColor(Vector3f val, int idx)
	{
		wingColors[idx] = val;
		if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged)
			UltraComponents.WING_DATA.get(winged).setColor(val, idx);
	}
	
	public static Vector3f[] getWingColors()
	{
		return wingColors;
	}
	
	public static Vector3f[] getDefaultWingColors()
	{
		return defaultWingColors;
	}
	
	public static void setWingPattern(String id)
	{
		wingPattern = id;
		if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged)
			UltraComponents.WING_DATA.get(winged).setPattern(id);
	}
	
	public static void refreshSupporter()
	{
		MinecraftClient client = MinecraftClient.getInstance();
		UUID uuid = client.player != null ? client.player.getUuid() : Uuids.getUuidFromProfile(client.getSession().getProfile());
		supporter = Ultracraft.checkSupporter(uuid, true);
	}
	
	public static boolean isSupporter()
	{
		return supporter;
	}
	
	public static void freezeVFX(int ticks)
	{
		visualFreezeTicks += ticks;
	}
	
	public static boolean isParryVisualsActive()
	{
		return visualFreezeTicks > 0;
	}
	
	public static boolean isBlocked(UUID uuid)
	{
		return config.get().blockedPlayers.contains(uuid);
	}
	
	public static boolean isCanGraffiti()
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		ClientPlayNetworking.send(PacketRegistry.REQUEST_GRAFFITI_WHITELIST_PACKET_ID, buf);
		return switch(GraffitiOption)
		{
			case ALLOW_ALL -> GRAFFITI_WHITELISTED;
			case ONLY_ADMINS -> ((WingedPlayerEntity)MinecraftClient.getInstance().player).isOpped() && GRAFFITI_WHITELISTED;
			case DISALLOW -> false;
		};
	}
	
	public static boolean isTerminalProtEnabled()
	{
		return terminalProt;
	}
}
