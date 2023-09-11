package absolutelyaya.ultracraft.api;

import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class HeavyEntities
{
	public static final Set<EntityType<?>> heavyEntities = new HashSet<>();
	
	public static void registerEntity(EntityType<?> entity)
	{
		heavyEntities.add(entity);
	}
	
	public static boolean isHeavy(EntityType<?> entity)
	{
		return heavyEntities.contains(entity);
	}
	
	static {
		registerEntity(EntityRegistry.MALICIOUS_FACE);
		registerEntity(EntityRegistry.CERBERUS);
		registerEntity(EntityRegistry.SWORDSMACHINE);
		registerEntity(EntityRegistry.DESTINY_SWORDSMACHINE);
		registerEntity(EntityRegistry.HIDEOUS_MASS);
		
		registerEntity(EntityType.ENDER_DRAGON);
		registerEntity(EntityType.IRON_GOLEM);
		registerEntity(EntityType.RAVAGER);
		registerEntity(EntityType.ELDER_GUARDIAN);
		registerEntity(EntityType.GIANT);
		registerEntity(EntityType.HOGLIN);
		registerEntity(EntityType.ZOGLIN);
		registerEntity(EntityType.SHULKER);
		registerEntity(EntityType.WITHER);
		registerEntity(EntityType.SNIFFER);
	}
}
