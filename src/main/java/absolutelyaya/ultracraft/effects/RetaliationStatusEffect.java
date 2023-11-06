package absolutelyaya.ultracraft.effects;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.components.player.IEasterComponent;
import absolutelyaya.ultracraft.entity.demon.RetaliationEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

public class RetaliationStatusEffect extends StatusEffect
{
	public RetaliationStatusEffect(StatusEffectCategory category, int color)
	{
		super(category, color);
	}
	
	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier)
	{
		if(!(entity instanceof PlayerEntity player))
			return;
		IEasterComponent easter = UltraComponents.EASTER.get(player);
		StatusEffectInstance instance = entity.getStatusEffect(StatusEffectRegistry.RETALIATION);
		if(!instance.isInfinite())
			return;
		if(easter.getPlushies() <= 0)
		{
			entity.setStatusEffect(new StatusEffectInstance(StatusEffectRegistry.RETALIATION, 600), entity);
			return;
		}
		if(entity.getRandom().nextFloat() > 0.25f)
			return;
		Vec3d pos = entity.getPos().addRandom(entity.getRandom(), 32f);
		if(entity.getPos().distanceTo(pos) < 16f)
			return;
		RetaliationEntity retaliation = new RetaliationEntity(EntityRegistry.RETALIATION, entity.getWorld());
		int y = entity.getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, (int)pos.x, (int)pos.z);
		retaliation.setPosition(pos.x, y, pos.z);
		entity.getWorld().spawnEntity(retaliation);
		easter.removePlushie();
	}
	
	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier)
	{
		return true;
	}
}
