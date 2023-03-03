package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class EntityRegistry
{
	public static final EntityType<FilthEntity> FILTH = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "filth"),
			EntityType.Builder.create(FilthEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8).build("filth"));
	public static final EntityType<MaliciousFaceEntity> MALICIOUS_FACE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "malicious_face"),
			EntityType.Builder.create(MaliciousFaceEntity::new, SpawnGroup.MONSTER).setDimensions(1.5F, 1.5F).maxTrackingRange(8).build("malicious_face"));
	public static final EntityType<HellBulletEntity> HELL_BULLET = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "hell_bullet"),
			EntityType.Builder.create(HellBulletEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSummon().build("hell_bullet"));
	
	@SuppressWarnings("ConstantConditions")
	public static void register()
	{
		FabricDefaultAttributeRegistry.register(FILTH, FilthEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(MALICIOUS_FACE, MaliciousFaceEntity.getDefaultAttributes());
	}
}