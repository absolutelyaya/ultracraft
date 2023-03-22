package absolutelyaya.ultracraft.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;

public class DamageSources
{
	public static final DamageSource MAURICE = new DamageSource("maurice").setUnblockable().setBypassesArmor();
	
	public static DamageSource getShockwave(Entity source)
	{
		return new EntityDamageSource("shockwave", source);
	}
}
