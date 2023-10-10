package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.demon.CerberusEntity;
import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.entity.demon.RetaliationEntity;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import absolutelyaya.ultracraft.entity.husk.SchismEntity;
import absolutelyaya.ultracraft.entity.husk.StrayEntity;
import absolutelyaya.ultracraft.entity.machine.DestinyBondSwordsmachineEntity;
import absolutelyaya.ultracraft.entity.machine.DroneEntity;
import absolutelyaya.ultracraft.entity.machine.StreetCleanerEntity;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import absolutelyaya.ultracraft.entity.other.*;
import absolutelyaya.ultracraft.entity.projectile.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
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
	public static final EntityType<HideousMassEntity> HIDEOUS_MASS = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "hideous_mass"),
			EntityType.Builder.create(HideousMassEntity::new, SpawnGroup.MONSTER).setDimensions(8F, 7F).maxTrackingRange(8).build("hideous_mass"));
	public static final EntityType<RetaliationEntity> RETALIATION = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "retaliation"),
			EntityType.Builder.create(RetaliationEntity::new, SpawnGroup.MONSTER).setDimensions(1F, 2F).maxTrackingRange(8).build("retaliation"));
	public static final EntityType<SwordsmachineEntity> SWORDSMACHINE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "swordsmachine"),
			EntityType.Builder.create(SwordsmachineEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 2.5F).maxTrackingRange(8).build("swordsmachine"));
	public static final EntityType<DestinyBondSwordsmachineEntity> DESTINY_SWORDSMACHINE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "destiny_swordsmachine"),
			EntityType.Builder.create(DestinyBondSwordsmachineEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 2.5F).maxTrackingRange(8).build("destiny_swordsmachine"));
	public static final EntityType<DroneEntity> DRONE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "drone"),
			EntityType.Builder.create(DroneEntity::new, SpawnGroup.MONSTER).setDimensions(0.7F, 0.8F).maxTrackingRange(8).build("drone"));
	public static final EntityType<StreetCleanerEntity> STREET_CLEANER = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "streetcleaner"),
			EntityType.Builder.create(StreetCleanerEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 2F).maxTrackingRange(8).build("streetcleaner"));
	
	public static final EntityType<HellBulletEntity> HELL_BULLET = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "hell_bullet"),
			EntityType.Builder.create(HellBulletEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSaving().build("hell_bullet"));
	public static final EntityType<CerberusBallEntity> CERBERUS_BALL = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "cerberus_ball"),
			EntityType.Builder.create(CerberusBallEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSaving().build("cerberus_ball"));
	public static final EntityType<ShotgunPelletEntity> SHOTGUN_PELLET = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "shotgun_pellet"),
			EntityType.Builder.create(ShotgunPelletEntity::new, SpawnGroup.MISC).setDimensions(0.25f, 0.25f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSummon().disableSaving().build("shotgun_pellet"));
	public static final EntityType<EjectedCoreEntity> EJECTED_CORE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "ejected_core"),
			EntityType.Builder.create(EjectedCoreEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSummon().disableSaving().build("ejected_core"));
	public static final EntityType<ThrownMachineSwordEntity> THROWN_MACHINE_SWORD = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "thrown_machinesword"),
			EntityType.Builder.create(ThrownMachineSwordEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(4).trackingTickInterval(20).disableSummon().build("thrown_machinesword"));
	public static final EntityType<ThrownCoinEntity> THROWN_COIN = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "thrown_coin"),
			EntityType.Builder.create(ThrownCoinEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSaving().build("thrown_coin"));
	public static final EntityType<FlameProjectileEntity> FLAME = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "flame"),
			EntityType.Builder.create(FlameProjectileEntity::new, SpawnGroup.MISC).setDimensions(0.25f, 0.25f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSaving().disableSummon().build("flame"));
	public static final EntityType<HideousMortarEntity> MORTAR = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "mortar"),
			EntityType.Builder.create(HideousMortarEntity::new, SpawnGroup.MISC).setDimensions(0.75f, 0.75f)
					.maxTrackingRange(5).trackingTickInterval(1).disableSaving().build("mortar"));
	public static final EntityType<HarpoonEntity> HARPOON = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "harpoon"),
			EntityType.Builder.create(HarpoonEntity::new, SpawnGroup.MISC).setDimensions(0.35f, 0.35f)
					.maxTrackingRange(5).trackingTickInterval(1).build("harpoon"));
	public static final EntityType<ThrownSoapEntity> SOAP = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "soap"),
			EntityType.Builder.create(ThrownSoapEntity::new, SpawnGroup.MISC).setDimensions(0.25f, 0.25f)
					.maxTrackingRange(5).trackingTickInterval(1).build("soap"));
	public static final EntityType<MagnetEntity> MAGNET = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "magnet"),
			EntityType.Builder.create(MagnetEntity::new, SpawnGroup.MISC).setDimensions(0.35f, 0.35f)
					.maxTrackingRange(5).trackingTickInterval(1).build("magnet"));
	//only tracking interval 3 due to the guaranteed large amount of these and it's not like they're parriable anyways
	public static final EntityType<NailEntity> NAIL = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "nail"),
			EntityType.Builder.create(NailEntity::new, SpawnGroup.MISC).setDimensions(0.125f, 0.125f)
					.maxTrackingRange(5).trackingTickInterval(3).disableSaving().build("nail"));
	
	public static final EntityType<ShockwaveEntity> SHOCKWAVE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "shockwave"),
			EntityType.Builder.create(ShockwaveEntity::new, SpawnGroup.MISC).maxTrackingRange(5).disableSaving().build("shockwave"));
	public static final EntityType<VerticalShockwaveEntity> VERICAL_SHOCKWAVE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "vertical_shockwave"),
			EntityType.Builder.create(VerticalShockwaveEntity::new, SpawnGroup.MISC).maxTrackingRange(5).disableSaving().build("vertical_shockwave"));
	public static final EntityType<InterruptableCharge> INTERRUPTABLE_CHARGE = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "interruptable_charge"),
			EntityType.Builder.create(InterruptableCharge::new, SpawnGroup.MISC).setDimensions(0.25f, 0.25f)
					.disableSummon().disableSaving().maxTrackingRange(5).build("interruptable_charge"));
	public static final EntityType<BackTank> BACK_TANK = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "back_tank"),
			EntityType.Builder.create(BackTank::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.disableSummon().disableSaving().maxTrackingRange(5).build("back_tank"));
	public static final EntityType<SoulOrbEntity> SOUL_ORB = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "soul_orb"),
			EntityType.Builder.create(SoulOrbEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).build("soul_orb"));
	public static final EntityType<BloodOrbEntity> BLOOD_ORB = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "blood_orb"),
			EntityType.Builder.create(BloodOrbEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).build("blood_orb"));
	public static final EntityType<StainedGlassWindow> STAINED_GLASS_WINDOW = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "stained_glass_window"),
			EntityType.Builder.create(StainedGlassWindow::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
					.maxTrackingRange(5).build("stained_glass_window"));
	public static final EntityType<ProgressionItemEntity> PROGRESSION_ITEM = Registry.register(Registries.ENTITY_TYPE,
			new Identifier(Ultracraft.MOD_ID, "progression_item"),
			EntityType.Builder.create(ProgressionItemEntity::new, SpawnGroup.MISC).setDimensions(0.25f, 0.25f)
					.maxTrackingRange(5).build("progression_item"));
	
	public static final TagKey<EntityType<?>> PROJBOOSTABLE = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(Ultracraft.MOD_ID, "projboostable"));
	
	public static void register()
	{
		FabricDefaultAttributeRegistry.register(FILTH, FilthEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(STRAY, StrayEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(SCHISM, SchismEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(MALICIOUS_FACE, MaliciousFaceEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(CERBERUS, CerberusEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(HIDEOUS_MASS, HideousMassEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(RETALIATION, RetaliationEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(SWORDSMACHINE, SwordsmachineEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(DESTINY_SWORDSMACHINE, DestinyBondSwordsmachineEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(DRONE, DroneEntity.getDefaultAttributes());
		FabricDefaultAttributeRegistry.register(STREET_CLEANER, StreetCleanerEntity.getDefaultAttributes());
	}
}
