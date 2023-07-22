package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.registry.EntityRegistry;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;

public class DynamicLightManager implements DynamicLightsInitializer
{
	@Override
	public void onInitializeDynamicLights()
	{
		DynamicLightHandlers.registerDynamicLightHandler(EntityRegistry.HELL_BULLET, entity -> 8);
		DynamicLightHandlers.registerDynamicLightHandler(EntityRegistry.CERBERUS_BALL, entity -> 8);
		DynamicLightHandlers.registerDynamicLightHandler(EntityRegistry.THROWN_COIN, entity -> 4);
		DynamicLightHandlers.registerDynamicLightHandler(EntityRegistry.INTERRUPTABLE_CHARGE, entity -> 6);
		DynamicLightHandlers.registerDynamicLightHandler(EntityRegistry.CERBERUS, DynamicLightHandler.makeHandler(blaze -> 10, blaze -> true));
		//TODO: Add Stalker -> 6
		//TODO: Add Ferryman -> 3
		//TODO: Add Street Cleaner -> 2
		//TODO: Add Mindflayer -> 9
		//TODO: Add Homing Projectile -> 7
		//TODO: Add Sentry -> 7
		//TODO: Add Idol -> 4
		//TODO: Add Gabriel -> 15
		//TODO: Add Cancerous Rodent -> 5
		//TODO: Add Virtue -> 13
		//Druid Knight glows as well, but I'm currently not planning on adding it
	}
}
