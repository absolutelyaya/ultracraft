package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
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
import absolutelyaya.ultracraft.client.rendering.entity.other.ShockwaveModel;
import absolutelyaya.ultracraft.client.rendering.entity.other.ShockwaveRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.CerberusBallRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.HellBulletRenderer;
import absolutelyaya.ultracraft.client.sound.MovingSlideSoundInstance;
import absolutelyaya.ultracraft.client.sound.MovingWindSoundInstance;
import absolutelyaya.ultracraft.particle.DashParticle;
import absolutelyaya.ultracraft.particle.GroundPoundParticle;
import absolutelyaya.ultracraft.particle.MaliciousChargeParticle;
import absolutelyaya.ultracraft.particle.SlideParticle;
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
import net.minecraft.world.World;
import software.bernie.geckolib.network.GeckoLibNetwork;

@Environment(EnvType.CLIENT)
public class UltracraftClient implements ClientModInitializer
{
	public static final EntityModelLayer WINGS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "wings"), "main");
	public static final EntityModelLayer MALICIOUS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "malicious"), "main");
	public static final EntityModelLayer ENRAGE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "enraged"), "main");
	public static final EntityModelLayer SHOCKWAVE_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "shockwave"), "main");
	public static ClientHitscanHandler HITSCAN_HANDLER;
	public static boolean REPLACE_MENU_MUSIC = true;
	static boolean HiVelMode = false;
	
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
		EntityRendererRegistry.register(EntityRegistry.SHOCKWAVE, ShockwaveRenderer::new);
		//Particles
		ParticleFactoryRegistry particleRegistry = ParticleFactoryRegistry.getInstance();
		particleRegistry.register(ParticleRegistry.MALICIOUS_CHARGE, MaliciousChargeParticle.MaliciousChargeParticleFactory::new);
		particleRegistry.register(ParticleRegistry.DASH, DashParticle.DashParticleFactory::new);
		particleRegistry.register(ParticleRegistry.SLIDE, SlideParticle.SlideParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GROUND_POUND, GroundPoundParticle.GroundPoundParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GOOP_DROP, GoopDropParticle.GoopDropParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GOOP, GoopParticle.GoopParticleFactory::new);
		particleRegistry.register(ParticleRegistry.GOOP_STRING, GoopStringParticle.GoopStringParticleFactory::new);
		//Entity model layers
		EntityModelLayerRegistry.registerModelLayer(WINGS_LAYER, WingsModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MALICIOUS_LAYER, MaliciousFaceModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ENRAGE_LAYER, EnragedModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(SHOCKWAVE_LAYER, ShockwaveModel::getTexturedModelData);
		
		BlockEntityRendererFactories.register(BlockEntityRegistry.PEDESTAL, PedestalBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(BlockEntityRegistry.CERBERUS, context -> new CerberusBlockRenderer());
		
		ModelPredicateRegistry.registerModels();
		
		HITSCAN_HANDLER = new ClientHitscanHandler();
		
		ClientTickEvents.END_WORLD_TICK.register((client) -> HITSCAN_HANDLER.tick());
		
		hudRenderer = new UltraHudRenderer();
		WorldRenderEvents.END.register((context) -> hudRenderer.render(context.tickDelta(), context.camera()));
		
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeUuid(client.player.getUuid());
			buf.writeBoolean(HiVelMode);
			ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_C2S_PACKET_ID, buf);
			
			if(client.world != null && client.world.getServer() != null && UltracraftClient.getConfigHolder().get().serverJoinInfo)
			{
				GameruleRegistry.Option hivel = client.world.getGameRules().get(GameruleRegistry.HI_VEL_MODE).get();
				GameruleRegistry.Option freeze = client.world.getGameRules().get(GameruleRegistry.TIME_STOP).get();
				if(!hivel.equals(GameruleRegistry.Option.FREE))
					client.player.sendMessage(Text.translatable("message.ultracraft.hi-vel-forced",
							hivel.equals(GameruleRegistry.Option.FORCE_ON) ? Text.translatable("options.on") : Text.translatable("options.off")));
				client.player.sendMessage(Text.translatable("message.ultracraft.freeze-forced",
						freeze.equals(GameruleRegistry.Option.FORCE_ON) ? Text.translatable("options.on") : Text.translatable("options.off")));
				client.player.sendMessage(Text.translatable("message.ultracraft.join-info"));
			}
			
			client.getSoundManager().play(new MovingWindSoundInstance(client.player));
		});
		
		ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
			if (entity instanceof PlayerEntity)
				MinecraftClient.getInstance().getSoundManager().play(new MovingSlideSoundInstance((PlayerEntity)entity));
		});
		
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
				(type, renderer, helper, context) -> {
					if(type.equals(EntityRegistry.MALICIOUS_FACE))
						helper.register(new EnragedFeature<>(context.getModelLoader()));
				});
		
		ClientPacketRegistry.registerS2C();
		GeckoLibNetwork.registerClientReceiverPackets();
		
		ClientTickEvents.END_WORLD_TICK.register(minecraft -> Ultracraft.tickFreeze());
		
		config = AutoConfig.register(Ultraconfig.class, GsonConfigSerializer::new);
	}
	
	//if no Server override, return client setting
	public static boolean isFreezeEnabled()
	{
		World world = MinecraftClient.getInstance().world;
		if(world == null || world.getServer() != null)
			return true;
		GameruleRegistry.Option option = world.getGameRules().get(GameruleRegistry.TIME_STOP).get();
		if(option.equals(GameruleRegistry.Option.FREE))
			return UltracraftClient.getConfigHolder().get().freezeVFX;
		else
			return option.equals(GameruleRegistry.Option.FORCE_ON);
	}
	
	public static boolean isHiVelEnabled()
	{
		World world = MinecraftClient.getInstance().world;
		if(world == null)
			return true;
		GameruleRegistry.Option option = world.getGameRules().get(GameruleRegistry.HI_VEL_MODE).get();
		if(option.equals(GameruleRegistry.Option.FREE))
			return HiVelMode;
		else
			return option.equals(GameruleRegistry.Option.FORCE_ON);
	}
	
	public static void toggleHiVelEnabled()
	{
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player == null)
			return;
		//TODO: The client always thinks the gamerule is set to FREE. fix that lol
		GameruleRegistry.Option option = player.world.getGameRules().get(GameruleRegistry.HI_VEL_MODE).get();
		if(option.equals(GameruleRegistry.Option.FREE))
			HiVelMode = !HiVelMode;
		else
			player.sendMessage(
					Text.translatable("message.ultracraft.hi-vel-forced",
									option.equals(GameruleRegistry.Option.FORCE_ON) ? "options.on" : "options.off"), true);
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeUuid(MinecraftClient.getInstance().player.getUuid());
		buf.writeBoolean(HiVelMode);
		ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_C2S_PACKET_ID, buf);
	}
	
	public static ConfigHolder<Ultraconfig> getConfigHolder()
	{
		return config;
	}
}
