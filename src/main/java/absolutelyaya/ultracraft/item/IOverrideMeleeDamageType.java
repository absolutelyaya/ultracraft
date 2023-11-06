package absolutelyaya.ultracraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

public interface IOverrideMeleeDamageType
{
	DamageSource getDamageSource(World world, LivingEntity attacker);
}
