package absolutelyaya.ultracraft.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;

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
		return new EntityDamageSource("swordmachine", source);
	}
	public static DamageSource getPound(Entity source)
	{
		return new EntityDamageSource("pound", source);
	}
}
