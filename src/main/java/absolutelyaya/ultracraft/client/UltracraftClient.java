package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import absolutelyaya.ultracraft.client.rendering.block.entity.PedestalBlockEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceModel;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.WingsModel;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.SchismRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.StrayRenderer;
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
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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
	public static ClientHitscanHandler HITSCAN_HANDLER;
	public static boolean REPLACE_MENU_MUSIC = true;
	static boolean FreezeOption = true;
	static boolean HiVelMode = false;
	
	static UltraHudRenderer hudRenderer;
	
	@Override
	public void onInitializeClient()
	{
		KeybindRegistry.register();
		
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.STRAY, StrayRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SCHISM, SchismRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
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
		
		BlockEntityRendererFactories.register(BlockEntityRegistry.PEDESTAL, PedestalBlockEntityRenderer::new);
		
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
			
			client.getSoundManager().play(new MovingWindSoundInstance(client.player));
		});
		
		ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
			if (entity instanceof PlayerEntity)
				MinecraftClient.getInstance().getSoundManager().play(new MovingSlideSoundInstance((PlayerEntity)entity));
		});
		
		ClientPacketRegistry.registerS2C();
		GeckoLibNetwork.registerClientReceiverPackets();
		
		ClientTickEvents.END_WORLD_TICK.register(minecraft -> Ultracraft.tickFreeze());
		
		AutoConfig.register(Ultraconfig.class, GsonConfigSerializer::new);
	}
	
	//if no Server override, return client setting
	public static boolean isFreezeEnabled()
	{
		if(Ultracraft.FreezeOption.equals(Ultracraft.Option.FREE))
			return FreezeOption;
		else
			return Ultracraft.FreezeOption.equals(Ultracraft.Option.FORCE_ON);
	}
	
	public static boolean isHiVelEnabled()
	{
		if(Ultracraft.HiVelOption.equals(Ultracraft.Option.FREE))
			return HiVelMode;
		else
			return Ultracraft.HiVelOption.equals(Ultracraft.Option.FORCE_ON);
	}
	
	public static void toggleHiVelEnabled()
	{
		if(Ultracraft.HiVelOption.equals(Ultracraft.Option.FREE))
			HiVelMode = !HiVelMode;
		else if(MinecraftClient.getInstance().player != null)
			MinecraftClient.getInstance().player.sendMessage(
					Text.translatable("message.ultracraft.hi-vel-forced",
							Ultracraft.HiVelOption.equals(Ultracraft.Option.FORCE_ON) ? "Enabled" : "Disabled"), true);
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeUuid(MinecraftClient.getInstance().player.getUuid());
		buf.writeBoolean(HiVelMode);
		ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_C2S_PACKET_ID, buf);
	}
}
