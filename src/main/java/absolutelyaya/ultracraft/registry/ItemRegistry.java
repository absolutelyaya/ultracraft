package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.CerberusBlock;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.item.SkullItem;
import absolutelyaya.ultracraft.item.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ItemRegistry
{
	//Misc
	public static final SkullItem BLUE_SKULL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "blue_skull"), new SkullItem(new FabricItemSettings()));
	public static final SkullItem RED_SKULL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "red_skull"), new SkullItem(new FabricItemSettings()));
	public static final Item HELL_BULLET = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "hell_bullet"), new HellBulletItem(new FabricItemSettings().fireproof().maxCount(25)));
	public static final Item CERBERUS_BALL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "cerberus_ball"), new HellBulletItem(new FabricItemSettings().fireproof().maxCount(21)));
	public static final Item CANCER_BULLET = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "cancer_bullet"), new HellBulletItem(new FabricItemSettings().fireproof().maxCount(7)));
	public static final Item EJECTED_CORE = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "ejected_core"), new HellBulletItem(new FabricItemSettings().maxCount(0)));
	public static final Item NAIL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "nail"), new Item(new FabricItemSettings().maxCount(0)));
	public static final Item BLOOD_BUCKET = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "blood_bucket"), new BucketItem(FluidRegistry.STILL_BLOOD,
					new FabricItemSettings().maxCount(1).recipeRemainder(Items.BUCKET)));
	public static final CoinItem COIN = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "coin"), new CoinItem(new FabricItemSettings()));
	public static final KillerFishItem KILLERFISH = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "killerfish"), new KillerFishItem(new FabricItemSettings()));
	public static final Item BLOOD_RAY = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "bloodray"), new Item(new FabricItemSettings()
						.food(new FoodComponent.Builder().hunger(4)
							  .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 500, 0), 1f).build())));
	public static final DroneMaskItem DRONE_MASK = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "drone_mask"), new DroneMaskItem(new FabricItemSettings()));
	public static final Item MINCED_MEAT = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "mincedmeat"), new SpecialItem(new FabricItemSettings()
						.food(new FoodComponent.Builder().hunger(12).saturationModifier(6f).build()))
								.putLore(new String[] { "item.ultracraft.mincedmeat.lore" }, new String[] { "item.ultracraft.mincedmeat.hiddenlore" }));
	public static final Item KNUCKLEBLASTER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "knuckleblaster"), new Item(new FabricItemSettings()));
	public static final Item HELL_MASS = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "hell_mass"), new Item(new FabricItemSettings()));
	public static final Item PLACEHOLDER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "placeholder"),
			new Item(new FabricItemSettings().food(new FoodComponent.Builder().alwaysEdible().hunger(-1).build())));
	
	//Weapons
	public static final PierceRevolverItem PIERCE_REVOLVER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "pierce_revolver"), new PierceRevolverItem(new FabricItemSettings().maxCount(1)));
	public static final MarksmanRevolverItem MARKSMAN_REVOLVER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "marksman_revolver"), new MarksmanRevolverItem(new FabricItemSettings().maxCount(1)));
	public static final SharpshooterRevolverItem SHARPSHOOTER_REVOLVER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "sharpshooter_revolver"), new SharpshooterRevolverItem(new FabricItemSettings().maxCount(1)));
	public static final CoreEjectShotgunItem CORE_SHOTGUN = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "core_shotgun"), new CoreEjectShotgunItem(new FabricItemSettings().maxCount(1)));
	public static final PumpShotgunItem PUMP_SHOTGUN = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "pump_shotgun"), new PumpShotgunItem(new FabricItemSettings().maxCount(1)));
	public static final MachineSwordItem MACHINE_SWORD = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "machinesword"), new MachineSwordItem(ToolMaterials.IRON, 4, -2.4f,
					new FabricItemSettings().maxCount(1)));
	public static final FlamethrowerItem FLAMETHROWER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "flamethrower"), new FlamethrowerItem(new FabricItemSettings().maxCount(1), 0, 0));
	public static final Item HARPOON = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "harpoon"), new HarpoonItem(6f, -2.75f,
					new FabricItemSettings().maxDamage(100)));
	public static final Item HARPOON_GUN = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "harpoon_gun"), new HarpoonGunItem(new FabricItemSettings(), 25, 0f));
	public static final SoapItem SOAP = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "soap"), new SoapItem(new FabricItemSettings().maxCount(4).rarity(Rarity.EPIC)));
	public static final AttractorNailgunItem ATTRACTOR_NAILGUN = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "attractor_nailgun"), new AttractorNailgunItem(new FabricItemSettings().maxCount(1)));
	
	//Spawn Eggs
	public static final SpawnEggItem FILTH_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "filth_spawn_egg"),
			new SpawnEggItem(EntityRegistry.FILTH, 0x717038, 0xacaa7a, new FabricItemSettings()));
	public static final SpawnEggItem STRAY_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "stray_spawn_egg"),
			new SpawnEggItem(EntityRegistry.STRAY, 0xaa6f5e, 0x922923, new FabricItemSettings()));
	public static final SpawnEggItem SCHISM_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "schism_spawn_egg"),
			new SpawnEggItem(EntityRegistry.SCHISM, 0x572f21, 0xa0938e, new FabricItemSettings()));
	public static final SpawnEggItem MALICIOUS_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "malicious_spawn_egg"),
			new SpawnEggItem(EntityRegistry.MALICIOUS_FACE, 0xa0938e, 0x5a5353, new FabricItemSettings()));
	public static final SpawnEggItem CERBERUS_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "cerberus_spawn_egg"),
			new SpawnEggItem(EntityRegistry.CERBERUS, 0xa0938e, 0x5a5353, new FabricItemSettings()));
	public static final SpecialSpawnEggItem SWORDSMACHINE_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "swordsmachine_spawn_egg"),
			new SpecialSpawnEggItem(EntityRegistry.SWORDSMACHINE, 0xf4b41b, 0x423d40, new FabricItemSettings()));
	public static final DestinyBondSpawnEggItem DESTINY_SWORDSMACHINE_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "destiny_swordsmachine_spawn_egg"),
			new DestinyBondSpawnEggItem(new FabricItemSettings()));
	public static final MultiColorSpawnEggItem DRONE_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "drone_spawn_egg"),
			new MultiColorSpawnEggItem(EntityRegistry.DRONE, new int[] {0x813ec6, 0x1b182d, 0xee42ff}, new FabricItemSettings()));
	public static final SpawnEggItem STREET_CLEANER_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "streetcleaner_spawn_egg"),
			new SpawnEggItem(EntityRegistry.STREET_CLEANER, 0xb1723a, 0x211c1b, new FabricItemSettings()));
	public static final SpecialSpawnEggItem HIDEOUS_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "hideous_spawn_egg"),
			new MultiColorSpawnEggItem(EntityRegistry.HIDEOUS_MASS, new int[] {0xffffff}, new FabricItemSettings()));
	public static final OrbItem SOUL_ORB = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "soul_orb"), new OrbItem(new FabricItemSettings(), EntityRegistry.SOUL_ORB));
	public static final OrbItem BLOOD_ORB = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "blood_orb"), new OrbItem(new FabricItemSettings(), EntityRegistry.BLOOD_ORB));
	public static final StainedGlassWindowItem STAINED_GLASS_WINDOW = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "stained_glass_window"), new StainedGlassWindowItem(EntityRegistry.STAINED_GLASS_WINDOW, new FabricItemSettings()));
	public static final SpawnEggItem V2_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "v2_spawn_egg"),
			new SpawnEggItem(EntityRegistry.V2, 0xbd2a22, 0x261e1f, new FabricItemSettings()));
	public static final SpawnEggItem RODENT_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "rodent_spawn_egg"),
			new SpawnEggItem(EntityRegistry.RODENT, 0xb6d53c, 0x71aa34, new FabricItemSettings()));
	
	//Plushies
	public static final PlushieItem PLUSHIE = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "plushie"), new PlushieItem(new FabricItemSettings()));
	public static final PlushieItem PITR = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "pitr"), new PitrItem(new FabricItemSettings()));
	public static final PlushieItem PITR_POIN = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "pitr_poin"), new PitrPoinItem(new FabricItemSettings()));
	public static final SwordsmachinePlushieItem SWORDSMACHINE = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "swordsmachine_plushie"), new SwordsmachinePlushieItem(new FabricItemSettings()));
	public static final TalonItem TALON = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "talon"), new TalonItem(new FabricItemSettings()));
	public static final V2Item V2 = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "v2"), new V2Item(new FabricItemSettings()));
	
	//Special
	public static final TerminalItem TERMINAL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "terminal"),
			new TerminalItem(BlockRegistry.TERMINAL, new FabricItemSettings()));
	public static final MusicDiscItem CLAIR_DE_LUNE_DISK = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "clair_de_lune"),
			new MusicDiscItem(15, SoundRegistry.CLAIR_DE_LUNE.value(), new FabricItemSettings(), 231));
	public static final FlorpItem FLORP = (FlorpItem)Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "florp"), new FlorpItem(new FabricItemSettings().rarity(Rarity.EPIC).maxCount(1))
																.putLore(true, new String[] { "item.ultracraft.florp.hiddenlore" }));
	
	//Animated Blocks
	public static final HellSpawnerItem HELL_SPAWNER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "hell_spawner"), new HellSpawnerItem(new FabricItemSettings()));
	
	//fakes
	public static final Item FAKE_SHIELD = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "fake_shield"), new Item(new FabricItemSettings().maxCount(0)));
	public static final Item FAKE_BANNER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "fake_banner"), new Item(new FabricItemSettings().maxCount(0)));
	public static final Item FAKE_TERMINAL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "fake_terminal"), new Item(new FabricItemSettings().maxCount(0)));
	public static final Item FAKE_CHEST = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "fake_chest"), new Item(new FabricItemSettings().maxCount(0)));
	public static final Item FAKE_ENDER_CHEST = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "fake_ender_chest"), new Item(new FabricItemSettings().maxCount(0)));
	
	public static final RegistryKey<ItemGroup> ULTRACRAFT_TAB = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Ultracraft.MOD_ID, "item"));
	
	public static void register()
	{
		Registry.register(Registries.ITEM_GROUP, ULTRACRAFT_TAB,
				FabricItemGroup.builder().displayName(Text.translatable("itemGroup.ultracraft.item")).icon(() -> new ItemStack(BLUE_SKULL)).build());
		ItemGroupEvents.modifyEntriesEvent(ULTRACRAFT_TAB).register(content -> {
			content.add(BLUE_SKULL);
			content.add(RED_SKULL);
			content.add(HELL_BULLET);
			content.add(CERBERUS_BALL);
			content.add(BlockRegistry.ELEVATOR.asItem());
			content.add(BlockRegistry.ELEVATOR_WALL.asItem());
			content.add(BlockRegistry.ELEVATOR_FLOOR.asItem());
			content.add(BlockRegistry.SECRET_ELEVATOR.asItem());
			content.add(BlockRegistry.SECRET_ELEVATOR_WALL.asItem());
			content.add(BlockRegistry.SECRET_ELEVATOR_FLOOR.asItem());
			content.add(BlockRegistry.PEDESTAL.asItem());
			content.add(BlockRegistry.CERBERUS.asItem());
			content.add(((CerberusBlock)BlockRegistry.CERBERUS).getProximityStack());
			content.add(BlockRegistry.FLESH.asItem());
			content.add(BlockRegistry.RUSTY_PIPE.asItem());
			content.add(BlockRegistry.RUSTY_MESH.asItem());
			content.add(BlockRegistry.CRACKED_STONE.asItem());
			content.add(BlockRegistry.VENT_COVER.asItem());
			content.add(BlockRegistry.MAUERWERK1.asItem());
			content.add(BlockRegistry.MAUERWERK2.asItem());
			content.add(BlockRegistry.ORNATE_WAINSCOT.asItem());
			content.add(BlockRegistry.ADORNED_RAILING.asItem());
			content.add(StainedGlassWindowItem.getStack(false));
			content.add(StainedGlassWindowItem.getStack(true));
			content.add(BLOOD_BUCKET);
			content.add(PIERCE_REVOLVER);
			content.add(MARKSMAN_REVOLVER);
			content.add(MARKSMAN_REVOLVER.getStackedMarksman());
			content.add(SHARPSHOOTER_REVOLVER);
			content.add(SHARPSHOOTER_REVOLVER.getStackedSharpshooter());
			content.add(CORE_SHOTGUN);
			content.add(PUMP_SHOTGUN);
			content.add(ATTRACTOR_NAILGUN);
			content.add(MACHINE_SWORD.getDefaultStack(MachineSwordItem.Type.NORMAL));
			content.add(MACHINE_SWORD.getDefaultStack(MachineSwordItem.Type.TUNDRA));
			content.add(MACHINE_SWORD.getDefaultStack(MachineSwordItem.Type.AGONY));
			content.add(FLAMETHROWER);
			content.add(HARPOON);
			content.add(HARPOON_GUN);
			content.add(SOAP);
			content.add(FILTH_SPAWN_EGG);
			content.add(STRAY_SPAWN_EGG);
			content.add(SCHISM_SPAWN_EGG);
			content.add(MALICIOUS_SPAWN_EGG);
			content.add(CERBERUS_SPAWN_EGG);
			content.add(SWORDSMACHINE_SPAWN_EGG);
			content.add(SpecialSpawnEggItem.putLore(SWORDSMACHINE_SPAWN_EGG.getDefaultNamedStack("item.ultracraft.swordsmachine_spawn_egg.dan", "Dan"),
					new String[] {"item.ultracraft.swordsmachine_spawn_egg.dan.lore"}, new String[] {"item.ultracraft.swordsmachine_spawn_egg.dan.hiddenlore"}));
			content.add(SWORDSMACHINE_SPAWN_EGG.getDefaultBossStack("item.ultracraft.swordsmachine_spawn_egg.unremarkable", false));
			content.add(DESTINY_SWORDSMACHINE_SPAWN_EGG);
			content.add(DRONE_SPAWN_EGG);
			content.add(STREET_CLEANER_SPAWN_EGG);
			content.add(HIDEOUS_SPAWN_EGG);
			content.add(HIDEOUS_SPAWN_EGG.getDefaultBossStack("item.ultracraft.hideous_spawn_egg.unremarkable", false));
			content.add(V2_SPAWN_EGG);
			content.add(RODENT_SPAWN_EGG);
			content.add(SOUL_ORB);
			content.add(BLOOD_ORB);
			content.add(PLUSHIE.getDefaultStack("yaya"));
			content.add(PLUSHIE.getDefaultStack("hakita"));
			content.add(PITR.getDefaultStack("pitr"));
			content.add(PITR_POIN.getDefaultStack("pitrpoin"));
			content.add(PLUSHIE.getDefaultStack("v1"));
			content.add(TALON.getDefaultStack("talon"));
			content.add(SWORDSMACHINE.getDefaultStack("swordsmachine"));
			content.add(SWORDSMACHINE.getDefaultStack("tundra"));
			content.add(SWORDSMACHINE.getDefaultStack("agony"));
			content.add(V2.getDefaultStack("v2"));
			content.add(DRONE_MASK);
			content.add(CLAIR_DE_LUNE_DISK);
			for (TerminalBlockEntity.Base b : TerminalBlockEntity.Base.values())
				content.add(TerminalItem.getStack(b));
			content.add(BlockRegistry.HELL_OBSERVER.asItem());
			content.add(BlockRegistry.HELL_SPAWNER.asItem());
			content.add(HELL_MASS);
		});
	}
}
