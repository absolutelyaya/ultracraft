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
import absolutelyaya.ultracraft.client.rendering.entity.feature.WingsModel;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.SchismRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.StrayRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.machine.SwordmachineRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.other.InterruptableChargeModel;
import absolutelyaya.ultracraft.client.rendering.entity.other.InterruptableChargeRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.other.ShockwaveModel;
import absolutelyaya.ultracraft.client.rendering.entity.other.ShockwaveRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.CerberusBallRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.HellBulletRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.ThrownMachineSwordRenderer;
import absolutelyaya.ultracraft.client.sound.MovingMachineSwordSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingSlideSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingWindSoundInstance;
import absolutelyaya.ultracraft.entity.projectile.ThrownMachineSwordEntity;
import absolutelyaya.ultracraft.particle.*;
import absolutelyaya.ultracraft.particle.goop.GoopDropParticle;
import absolutelyaya.ultracraft.particle.goop.GoopParticle;
import absolutelyaya.ultracraft.particle.goop.GoopStringParticle;
import absolutelyaya.ultracraft.registry.*;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
	public static boolean REPLACE_MENU_MUSIC = true;
	static boolean HiVelMode = false;
	static GameruleRegistry.Option HiVelOption = GameruleRegistry.Option.FREE;
	static GameruleRegistry.Option TimeFreezeOption = GameruleRegistry.Option.FORCE_ON;
	static boolean disableHandswap = false;
	
	static UltraHudRenderer hudRenderer;
	static ConfigHolder<Ultraconfig> config;
	
	@Override
	public void onInitializeClient()
	{
		KeybindRegistry.register();
		
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.STRAY, StrayRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SCHISM, SchismRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.CERBERUS, CerberusRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SWORDMACHINE, SwordmachineRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.CERBERUS_BALL, CerberusBallRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.THROWN_MACHINE_SWORD, ThrownMachineSwordRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SHOCKWAVE, ShockwaveRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.INTERRUPTABLE_CHARGE, InterruptableChargeRenderer::new);
		//Particles
		ParticleFactoryRegistry particleRegistry = ParticleFactoryRegistry.getInstance();
		particleRegistry.register(ParticleRegistry.MALICIOUS_CHARGE, MaliciousChargeParticle.MaliciousChargeParticleFactory::new);
		particleRegistry.register(ParticleRegistry.DASH, DashParticle.DashParticleFactory::new);
		particleRegistry.register(ParticleRegistry.SLIDE, SlideParticle.SlideParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GROUND_POUND, GroundPoundParticle.GroundPoundParticleFactory::new);
		particleRegistry.register(ParticleRegistry.PARRY_INDICATOR, ParryIndicatorParticle.ParryIndicatorParticleFactory::new);
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
			
			if(UltracraftClient.getConfigHolder().get().serverJoinInfo)
			{
				GameruleRegistry.Option hivel = client.world.getGameRules().get(GameruleRegistry.HI_VEL_MODE).get();
				GameruleRegistry.Option freeze = client.world.getGameRules().get(GameruleRegistry.TIME_STOP).get();
				client.player.sendMessage(Text.translatable("message.ultracraft.join-info-header"));
				if(!hivel.equals(GameruleRegistry.Option.FREE))
					client.player.sendMessage(Text.translatable("message.ultracraft.hi-vel-forced",
							hivel.equals(GameruleRegistry.Option.FORCE_ON) ? Text.translatable("options.on") : Text.translatable("options.off")));
				else
					client.player.sendMessage(Text.translatable("message.ultracraft.hi-vel-free"));
				client.player.sendMessage(Text.translatable("message.ultracraft.freeze-forced",
						freeze.equals(GameruleRegistry.Option.FORCE_ON) ? Text.translatable("options.on") : Text.translatable("options.off")));
				client.player.sendMessage(Text.translatable("message.ultracraft.join-info"));
				client.player.sendMessage(Text.translatable("========================================="));
			}
			
			client.getSoundManager().play(new MovingWindSoundInstance(client.player));
		});
		
		ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
			if (entity instanceof PlayerEntity player)
				MinecraftClient.getInstance().getSoundManager().play(new MovingSlideSoundInstance(player));
			if (entity instanceof ThrownMachineSwordEntity sword)
				MinecraftClient.getInstance().getSoundManager().play(new MovingMachineSwordSoundInstance(sword));
		});
		
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if(type.equals(EntityRegistry.MALICIOUS_FACE))
				helper.register(new EnragedFeature<>(context.getModelLoader()));
		});
		
		WorldRenderEvents.AFTER_ENTITIES.register((ctx) -> {
			UltracraftClient.HITSCAN_HANDLER.render(ctx.matrixStack(), ctx.camera());
			UltracraftClient.TRAIL_RENDERER.render(ctx.matrixStack(), ctx.camera());
		});
		
		ClientPacketRegistry.registerS2C();
		GeckoLibNetwork.registerClientReceiverPackets();
		
		ClientTickEvents.END_WORLD_TICK.register(minecraft -> {
			Ultracraft.tickFreeze();
			UltracraftClient.TRAIL_RENDERER.tick();
		});
		
		config = AutoConfig.register(Ultraconfig.class, GsonConfigSerializer::new);
	}
	
	//if no Server override, return client setting
	public static boolean isFreezeEnabled()
	{
		GameruleRegistry.Option option = TimeFreezeOption;
		if(option.equals(GameruleRegistry.Option.FREE))
			return UltracraftClient.getConfigHolder().get().freezeVFX;
		else
			return option.equals(GameruleRegistry.Option.FORCE_ON);
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
			default -> Ultracraft.LOGGER.error("Received invalid Packet data: [rule_sync] -> " + data);
		}
	}
}
