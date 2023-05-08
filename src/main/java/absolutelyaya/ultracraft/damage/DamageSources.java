package absolutelyaya.ultracraft.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class DamageSources
{
	public static final UltraDamageSource MAURICE = (UltraDamageSource) new UltraDamageSource(Type.MAURICE, null).setHitscan().setUnblockable().setBypassesArmor();
	
	public static UltraDamageSource getGun(Entity source)
	{
		return new UltraDamageSource(Type.GUN, source);
	}
	public static UltraDamageSource getShotgun(Entity source)
	{
		return (UltraDamageSource) new UltraDamageSource(Type.SHOTGUN, source).setProjectile();
	}
	public static UltraDamageSource getShockwave(Entity source)
	{
		return new UltraDamageSource(Type.SHOCKWAVE, source);
	}
	public static UltraDamageSource getSwordmachine(Entity source)
	{
		return new UltraDamageSource(Type.SWORDSMACHINE, source);
	}
	public static UltraDamageSource getPound(Entity source)
	{
		return new UltraDamageSource(Type.POUND, source);
	}
	public static UltraDamageSource getParriedProjectile(PlayerEntity source)
	{
		return (UltraDamageSource) new UltraDamageSource(Type.PARRY, source).setProjectile();
	}
	public static UltraDamageSource getInterrupted(Entity source)
	{
		return (UltraDamageSource) new UltraDamageSource(Type.INTERRUPT, source).setBypassesArmor().setBypassesProtection().setUnblockable();
	}
	public static UltraDamageSource getParryCollateral(Entity parrier)
	{
		return (UltraDamageSource) new UltraDamageSource(Type.PARRYAOE, parrier).setExplosive();
	}
	
	public static UltraDamageSource getExplosion(Entity source)
	{
		return (UltraDamageSource) new UltraDamageSource(Type.EXPLOSION, source).setExplosive();
	}
	
	public static UltraDamageSource getProjectileBoost(PlayerEntity booster)
	{
		return (UltraDamageSource) new UltraDamageSource(Type.PROJBOOST, booster).setExplosive();
	}
	
	public enum Type
	{
		MAURICE("maurice"),
		GUN("gun"),
		SHOTGUN("shotgun"),
		SHOCKWAVE("shockwave"),
		SWORDSMACHINE("swordmachine"),
		POUND("pound"),
		PARRY("projectile_parried"),
		PARRYAOE("parry_collateral"),
		INTERRUPT("interrupt"),
		EXPLOSION("explosion"),
		PROJBOOST("proj_boost");
		final String name;
		
		Type(String name)
		{
			this.name = name;
		}
	}
}
