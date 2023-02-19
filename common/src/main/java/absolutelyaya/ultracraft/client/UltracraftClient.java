package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.block.entity.PedestalBlockEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.HellBulletRenderer;
import absolutelyaya.ultracraft.client.rendering.item.PierceRevolverRenderer;
import absolutelyaya.ultracraft.particle.MaliciousChargeParticle;
import absolutelyaya.ultracraft.registry.*;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class UltracraftClient
{
	public static ClientHitscanHandler HITSCAN_HANDLER;
	static boolean FreezeOption = true;
	static boolean HiVelMode = true;
	
	public static void init()
	{
		ParticleRegistry.init();
		
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		//Particles
		ParticleProviderRegistry.register(ParticleRegistry.MALICIOUS_CHARGE.get(), MaliciousChargeParticle.MaliciousChargeParticleFactory::new);
		
		BlockEntityRendererRegistry.register(BlockEntityRegistry.PEDESTAL.get(), PedestalBlockEntityRenderer::new);
		
		ModelPredicateRegistry.registerModels();
		
		HITSCAN_HANDLER = new ClientHitscanHandler();
		
		TickEvent.PLAYER_POST.register((client) -> HITSCAN_HANDLER.tick());
		
		GeoItemRenderer.registerItemRenderer(ItemRegistry.PIERCE_REVOLVER.get(), new PierceRevolverRenderer());
	}
	
	//if no Server override return client setting
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
		else
			MinecraftClient.getInstance().player.sendMessage(
					Text.translatable("message.ultracraft.hi-vel-forced",
							Ultracraft.HiVelOption.equals(Ultracraft.Option.FORCE_ON) ? "Enabled" : "Disabled"), true);
	}
}
