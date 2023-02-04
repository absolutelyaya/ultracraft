package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
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
	public static final Supplier<EntityType<MaliciousFaceEntity>> MALICIOUS_FACE = ENTITY_TYPES.register("malicious_face",
			() -> EntityType.Builder.create(MaliciousFaceEntity::new, SpawnGroup.MONSTER).setDimensions(1.5F, 1.5F).maxTrackingRange(8).build("malicious_face"));
	public static final Supplier<EntityType<HellBulletEntity>> HELL_BULLET = ENTITY_TYPES.register("hell_bullet",
			() -> EntityType.Builder.create(HellBulletEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
						  .maxTrackingRange(5).trackingTickInterval(1).disableSummon().build("hell_bullet"));
	
	@SuppressWarnings("ConstantConditions")
	public static void register()
	{
		ENTITY_TYPES.register();
		
		EntityAttributeRegistry.register(FILTH, FilthEntity::getDefaultAttributes);
		EntityAttributeRegistry.register(MALICIOUS_FACE, MaliciousFaceEntity::getDefaultAttributes);
	}
}
