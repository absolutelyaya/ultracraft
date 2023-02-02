package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class EntityRegistry
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Ultracraft.MOD_ID, Registry.ENTITY_TYPE_KEY);
	public static final Supplier<EntityType<FilthEntity>> FILTH = ENTITY_TYPES.register("filth",
			() -> EntityType.Builder.create(FilthEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8).build("filth"));
	
	@SuppressWarnings("ConstantConditions")
	public static void register()
	{
		ENTITY_TYPES.register();
		
		EntityAttributeRegistry.register(FILTH, FilthEntity::getDefaultAttributes);
	}
}
