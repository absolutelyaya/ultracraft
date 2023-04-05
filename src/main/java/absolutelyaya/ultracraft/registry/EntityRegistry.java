package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.projectile.ThrownMachineSwordEntity;
import absolutelyaya.ultracraft.entity.machine.SwordmachineEntity;
import absolutelyaya.ultracraft.entity.other.InterruptableCharge;
import absolutelyaya.ultracraft.entity.other.ShockwaveEntity;
import absolutelyaya.ultracraft.entity.demon.CerberusEntity;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import absolutelyaya.ultracraft.entity.husk.SchismEntity;
import absolutelyaya.ultracraft.entity.husk.StrayEntity;
import absolutelyaya.ultracraft.entity.projectile.CerberusBallEntity;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class EntityRegistry
{
	public static final EntityType<FilthEntity> FILTH = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "filth"),
			EntityType.Builder.create(FilthEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8).build("filth"));
	public static final EntityType<StrayEntity> STRAY = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "stray"),
			EntityType.Builder.create(StrayEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8).build("stray"));
	public static final EntityType<SchismEntity> SCHISM = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "schism"),
			EntityType.Builder.create(SchismEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8).build("schism"));
	public static final EntityType<MaliciousFaceEntity> MALICIOUS_FACE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "malicious_face"),
			EntityType.Builder.create(MaliciousFaceEntity::new, SpawnGroup.MONSTER).setDimensions(1.5F, 1.5F).maxTrackingRange(8).build("malicious_face"));
	public static final EntityType<CerberusEntity> CERBERUS = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "cerberus"),
			EntityType.Builder.create(CerberusEntity::new, SpawnGroup.MONSTER).setDimensions(1.75F, 4F).maxTrackingRange(8).build("cerberus"));
	public static final EntityType<SwordmachineEntity> SWORDMACHINE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "swordmachine"),
			EntityType.Builder.create(SwordmachineEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 2.5F).maxTrackingRange(8).build("swordmachine"));
	
	public static final EntityType<HellBulletEntity> HELL_BULLET = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "hell_bullet"),
			EntityType.Builder.create(HellBulletEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSummon().build("hell_bullet"));
	public static final EntityType<CerberusBallEntity> CERBERUS_BALL = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "cerberus_ball"),
			EntityType.Builder.create(CerberusBallEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSummon().build("cerberus_ball"));
	public static final EntityType<ThrownMachineSwordEntity> THROWN_MACHINE_SWORD = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "thrown_machinesword"),
			EntityType.Builder.create(ThrownMachineSwordEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(4).trackingTickInterval(20).disableSummon().build("thrown_machinesword"));
	
	public static final EntityType<ShockwaveEntity> SHOCKWAVE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "shockwave"),
			EntityType.Builder.create(ShockwaveEntity::new, SpawnGroup.MISC).maxTrackingRange(5).build("shockwave"));
	public static final EntityType<InterruptableCharge> INTERRUPTABLE_CHARGE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "interruptable_charge"),
			EntityType.Builder.create(InterruptableCharge::new, SpawnGroup.MISC).setDimensions(0.25f, 0.25f)
					.disableSummon().maxTrackingRange(5).build("interruptable_charge"));
	
	@SuppressWarnings("ConstantConditions")
	public static void register()
	{
		FabricDefaultAttributeRegistry.register(FILTH, FilthEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(STRAY, StrayEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(SCHISM, SchismEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(MALICIOUS_FACE, MaliciousFaceEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(CERBERUS, CerberusEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(SWORDMACHINE, SwordmachineEntity.getDefaultAttributes());
	}
}
