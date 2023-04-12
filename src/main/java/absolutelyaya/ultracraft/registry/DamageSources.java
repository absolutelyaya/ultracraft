package absolutelyaya.ultracraft.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

public class DamageSources
{
	public static final DamageSource MAURICE = new DamageSource("maurice").setUnblockable().setBypassesArmor();
	
	public static DamageSource getGun(Entity source)
	{
		return new EntityDamageSource("gun", source);
	}
	public static DamageSource getShotgun(Entity source)
	{
		return new EntityDamageSource("shotgun", source);
	}
	public static DamageSource getShockwave(Entity source)
	{
		return new EntityDamageSource("shockwave", source);
	}
	public static DamageSource getSwordmachine(Entity source)
	{
		return new EntityDamageSource("swordmachine", source).setScaledWithDifficulty();
	}
	public static DamageSource getPound(Entity source)
	{
		return new EntityDamageSource("pound", source);
	}
	public static DamageSource getParriedProjectile(PlayerEntity source, ProjectileEntity projectile)
	{
		return new ProjectileDamageSource("projectile_parried", projectile, source).setProjectile();
	}
	public static DamageSource getInterrupted(Entity source)
	{
		return new EntityDamageSource("interrupt", source);
	}
	public static DamageSource getParryCollateral(Entity parrier)
	{
		return new EntityDamageSource("parry_collateral", parrier);
	}
}
