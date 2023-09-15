package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.api.terminal.TerminalCodeRegistry;
import absolutelyaya.ultracraft.client.gui.screen.EpilepsyPopupScreen;
import absolutelyaya.ultracraft.client.gui.screen.ServerConfigScreen;
import absolutelyaya.ultracraft.client.gui.terminal.PetTab;
import absolutelyaya.ultracraft.client.rendering.TrailRenderer;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import absolutelyaya.ultracraft.client.rendering.block.entity.CerberusBlockRenderer;
import absolutelyaya.ultracraft.client.rendering.block.entity.PedestalBlockEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.block.entity.TerminalBlockEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.CerberusRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.HideousMassRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceModel;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.*;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.SchismRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.StrayRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.DroneEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.StreetCleanerEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.SwordsmachineRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.other.*;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.*;
import absolutelyaya.ultracraft.client.sound.MovingMachineSwordSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingSlideSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingSwordsmachineSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingWindSoundInstance;
import absolutelyaya.ultracraft.compat.PlayerAnimator;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import software.bernie.geckolib.network.GeckoLibNetwork;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class UltracraftClient implements ClientModInitializer
{
	public static final EntityModelLayer WINGS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "wings"), "main");
	public static final EntityModelLayer MALICIOUS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "malicious"), "main");
	public static final EntityModelLayer ENRAGE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "enraged"), "main");
	public static final EntityModelLayer INTERRUPTABLE_CHARGE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "interruptable_charge"), "main");
	public static String wingPreset = "", wingPattern = "";
	private static ShaderProgram wingsColoredProgram, wingsColoredUIProgram, texPosFade, flesh;
	public static ClientHitscanHandler HITSCAN_HANDLER;
	public static TrailRenderer TRAIL_RENDERER;
	public static boolean REPLACE_MENU_MUSIC = true, applyEntityPoses;
	static boolean HiVelMode = false;
	static GameruleRegistry.Setting HiVelOption = GameruleRegistry.Setting.FREE;
	static GameruleRegistry.Setting TimeFreezeOption = GameruleRegistry.Setting.FORCE_ON;
	static GameruleRegistry.RegenSetting bloodRegen = GameruleRegistry.RegenSetting.ALWAYS;
	static GameruleRegistry.ProjectileBoostSetting projBoost = GameruleRegistry.ProjectileBoostSetting.LIMITED;
	static GameruleRegistry.GraffitiSetting GraffitiOption = GameruleRegistry.GraffitiSetting.ALLOW_ALL;
	static boolean disableHandswap = false, slamStorage = true, fallDamage = false, drowning = false, effectivelyViolent = false, wasMovementSoundsEnabled, parryChaining, supporter = false, joinInfoPending, terminalProt;
	public static int jumpBoost, speed, gravityReduction;
	static float screenblood;
	static Vec3d[] wingColors = new Vec3d[] { new Vec3d(247f, 255f, 154f), new Vec3d(117f, 154f, 255f) };
	static final Vec3d[] defaultWingColors = new Vec3d[] { new Vec3d(247f, 255f, 154f), new Vec3d(117f, 154f, 255f) };
	static int visualFreezeTicks;
	
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
		EntityRendererRegistry.register(EntityRegistry.HIDEOUS_MASS, HideousMassRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SWORDSMACHINE, SwordsmachineRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.DESTINY_SWORDSMACHINE, SwordsmachineRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.CERBERUS_BALL, CerberusBallRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SHOTGUN_PELLET, ShotgunPelletRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.EJECTED_CORE, EjectedCoreRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.THROWN_MACHINE_SWORD, ThrownMachineSwordRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.THROWN_COIN, ThrownCoinRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.FLAME, FlameRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MORTAR, HideousMortarRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HARPOON, HarpoonEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SOAP, ThrownSoapRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SHOCKWAVE, ShockwaveRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.VERICAL_SHOCKWAVE, VerticalShockwaveRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.INTERRUPTABLE_CHARGE, InterruptableChargeRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SOUL_ORB, OrbRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.BLOOD_ORB, OrbRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.DRONE, DroneEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.STREET_CLEANER, StreetCleanerEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.BACK_TANK, BackTankRenderer::new);
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
		particleRegistry.register(ParticleRegistry.GOOP_DROP, GoopDropParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.GOOP, GoopParticle.Factory::new);
		particleRegistry.register(ParticleRegistry.GOOP_STRING, GoopStringParticle.Factory::new);
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
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(isHiVelEnabled());
			buf.writeVector3f(getWingColors()[0].toVector3f());
			buf.writeVector3f(getWingColors()[1].toVector3f());
			buf.writeString(wingPattern);
			ClientPlayNetworking.send(PacketRegistry.SEND_WINGED_DATA_C2S_PACKET_ID, buf);
			
			refreshSupporter();
			WingedPlayerEntity winged = ((WingedPlayerEntity)client.player);
			winged.setWingColor(wingColors[0], 0);
			winged.setWingColor(wingColors[1], 1);
			winged.setWingPattern(wingPattern);
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
			}
		});
		
		WorldRenderEvents.BEFORE_ENTITIES.register((ctx) -> applyEntityPoses = true);
		
		WorldRenderEvents.AFTER_ENTITIES.register((ctx) -> {
			UltracraftClient.HITSCAN_HANDLER.render(ctx.matrixStack(), ctx.camera(), ctx.tickDelta());
			UltracraftClient.TRAIL_RENDERER.render(ctx.matrixStack(), ctx.camera());
			applyEntityPoses = false;
		});
		
		HudRenderCallback.EVENT.register((matrices, delta) -> {
			if(config.get().safeVFX)
				return;
			if(config.get().bloodOverlay)
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
		//Block Layers
		FluidRenderHandlerRegistry.INSTANCE.register(FluidRegistry.STILL_BLOOD, FluidRegistry.Flowing_BLOOD,
				new SimpleFluidRenderHandler(new Identifier(Ultracraft.MOD_ID, "block/blood_still"), new Identifier(Ultracraft.MOD_ID, "block/blood_flow")));
		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getSolid(), FluidRegistry.STILL_BLOOD, FluidRegistry.Flowing_BLOOD);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.FLESH, RenderLayers.getFlesh());
		
		TerminalCodeRegistry.registerCode("saiai", t -> t.setTab(new PetTab()));
		TerminalCodeRegistry.registerCode("somethingwicked", new TerminalCodeRegistry.Result(t -> {
			t.setColorOverride(0x460006);
			MinecraftClient.getInstance().player.sendMessage(Text.of("Something Wicked this way comes"), true);
		}, SoundEvents.ENTITY_PLAYER_BREATH, 1.15f));
		
		setWingColor(config.get().wingColors[0], 0);
		setWingColor(config.get().wingColors[1], 1);
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
	
	//if no Server override, return client setting
	public static boolean isFreezeEnabled()
	{
		if(MinecraftClient.getInstance().isInSingleplayer())
			return config.get().freezeVFX;
		else
			return TimeFreezeOption.equals(GameruleRegistry.Setting.FORCE_ON);
	}
	
	public static boolean isHiVelEnabled()
	{
		GameruleRegistry.Setting option = HiVelOption;
		if(option.equals(GameruleRegistry.Setting.FREE))
			return HiVelMode;
		else
			return option.equals(GameruleRegistry.Setting.FORCE_ON);
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
		GameruleRegistry.Setting option = HiVelOption;
		if(option.equals(GameruleRegistry.Setting.FREE))
		{
			HiVelMode = !HiVelMode;
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(isHiVelEnabled());
			ClientPlayNetworking.send(PacketRegistry.SEND_WING_STATE_C2S_PACKET_ID, buf);
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
		PlayerEntity player = MinecraftClient.getInstance().player;
		HiVelMode = b;
		if(!fromServer && player != null)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(isHiVelEnabled());
			ClientPlayNetworking.send(PacketRegistry.SEND_WING_STATE_C2S_PACKET_ID, buf);
		}
	}
	
	public static ConfigHolder<Ultraconfig> getConfigHolder()
	{
		return config;
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
					setHiVel(HiVelOption == GameruleRegistry.Setting.FORCE_ON, false);
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
			case 16 -> onExternalRuleUpdate(GameruleRegistry.GUN_DAMAGE, value);
			case 17 -> onExternalRuleUpdate(GameruleRegistry.INVINCIBILITY, value);
			case 18 -> onExternalRuleUpdate(GameruleRegistry.TERMINAL_PROT, terminalProt = value == 1);
			case 19 -> onExternalRuleUpdate(GameruleRegistry.GRAFFITI, (GraffitiOption = GameruleRegistry.GraffitiSetting.values()[value]).name());
			case 20 -> onExternalRuleUpdate(GameruleRegistry.FLAMETHROWER_GRIEF, value == 1);
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
	
	public static void setWingColor(Vec3d val, int idx)
	{
		wingColors[idx] = val;
		if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged)
			winged.setWingColor(val, idx);
	}
	
	public static Vec3d[] getWingColors()
	{
		return wingColors;
	}
	
	public static Vec3d[] getDefaultWingColors()
	{
		return defaultWingColors;
	}
	
	public static void setWingPattern(String id)
	{
		wingPattern = id;
		if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged)
			winged.setWingPattern(id);
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
	
	public static boolean isCanGraffiti()
	{
		return switch(GraffitiOption)
		{
			case ALLOW_ALL -> true;
			case ONLY_ADMINS -> ((WingedPlayerEntity)MinecraftClient.getInstance().player).isOpped();
			case DISALLOW -> false;
		};
	}
	
	public static boolean isTerminalProtEnabled()
	{
		return terminalProt;
	}
}
