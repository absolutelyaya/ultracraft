package absolutelyaya.ultracraft.entity.other;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BloodOrbEntity extends AbstractOrbEntity
{
	public BloodOrbEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	@Override
	public Identifier getTexture()
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/item/blood_orb.png");
	}
	
	@Override
	public Vec3i getGlowColor()
	{
		return new Vec3i(169, 15, 62);
	}
	
	@Override
	public void onPlayerCollision(PlayerEntity player)
	{
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 10, 4));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 3600, 4));
		super.onPlayerCollision(player);
	}
	
	@Override
	protected BlockState getParticleBlockstate()
	{
		return Blocks.REDSTONE_BLOCK.getDefaultState();
	}
}
