package absolutelyaya.ultracraft.damage;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DamageSources
{
	public static final RegistryKey<DamageType> MAURICE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "maurice"));
	public static final RegistryKey<DamageType> GUN = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "gun"));
	public static final RegistryKey<DamageType> SHOTGUN = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "shotgun"));
	public static final RegistryKey<DamageType> SHOCKWAVE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "shockwave"));
	public static final RegistryKey<DamageType> SWORDSMACHINE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "swordsmachine"));
	public static final RegistryKey<DamageType> POUND = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "groundpound"));
	public static final RegistryKey<DamageType> PARRY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "parry"));
	public static final RegistryKey<DamageType> PARRYAOE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "parry_collateral"));
	public static final RegistryKey<DamageType> INTERRUPT = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "interrupt"));
	public static final RegistryKey<DamageType> PROJBOOST = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "projectile_boost"));
	public static final RegistryKey<DamageType> RICOCHET = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "ricochet"));
	public static final RegistryKey<DamageType> COIN_PUNCH = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "coin_punch"));
	public static final RegistryKey<DamageType> CHARGEBACK = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "chargeback"));
	public static final RegistryKey<DamageType> OVERCHARGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "overcharge"));
	public static final RegistryKey<DamageType> OVERCHARGE_SELF = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "overcharge_self"));
	public static final RegistryKey<DamageType> FLAMETHROWER = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "flamethrower"));
	public static final RegistryKey<DamageType> SHORT_CIRCUIT = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "short_circuit"));
	public static final RegistryKey<DamageType> BACK_TANK = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "back_tank"));
	public static final RegistryKey<DamageType> HARPOON = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "harpoon"));
	public static final RegistryKey<DamageType> HARPOON_RIP = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "harpoon_rip"));
	public static final RegistryKey<DamageType> SOAP = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "soap"));
	public static final RegistryKey<DamageType> NAIL = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "nail"));
	public static final RegistryKey<DamageType> MAGNET = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "magnet"));
	public static final RegistryKey<DamageType> SHARPSHOOTER = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "sharpshooter"));
	public static final RegistryKey<DamageType> RETALIATION = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "retaliation"));
	public static final RegistryKey<DamageType> KNUCKLE_BLAST = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "knuckle_blast"));
	public static final RegistryKey<DamageType> PUNCH = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "punch"));
	public static final RegistryKey<DamageType> KNUCKLE_PUNCH = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "knuckle_punch"));
	public static final RegistryKey<DamageType> CANCER = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "cancer"));
	public static final RegistryKey<DamageType> PIERCER = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "piercer"));
	public static final RegistryKey<DamageType> CORE_EJECT = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "core_eject"));
	public static final RegistryKey<DamageType> OBLITERATION = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "obliteration"));
	public static final RegistryKey<DamageType> DEVOURED = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "devoured"));
	
	public static DamageSource get(World world, RegistryKey<DamageType> type)
	{
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type));
	}
	
	public static DamageSource get(World world, RegistryKey<DamageType> type, Entity attacker)
	{
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type), attacker);
	}
	
	public static HitscanDamageSource getHitscan(World world, RegistryKey<DamageType> type, Entity attacker, ServerHitscanHandler.Hitscan hitscan)
	{
		return new HitscanDamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type), attacker, hitscan);
	}
	
	public static DamageSource get(World world, RegistryKey<DamageType> type, Entity source, Entity attacker)
	{
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type), source, attacker);
	}
	
	public static HitscanDamageSource getHitscan(World world, RegistryKey<DamageType> type, Entity source, Entity attacker, ServerHitscanHandler.Hitscan hitscan)
	{
		return new HitscanDamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type), source, attacker, hitscan);
	}
}
