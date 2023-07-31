package absolutelyaya.ultracraft.entity.other;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class SoulOrbEntity extends AbstractOrbEntity
{
	public SoulOrbEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	@Override
	public Identifier getTexture()
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/soul_orb.png");
	}
	
	@Override
	public Vec3i getGlowColor()
	{
		return new Vec3i(179, 241, 255);
	}
	
	@Override
	public void onPlayerCollision(PlayerEntity player)
	{
		player.addStatusEffect(new StatusEffectInstance(StatusEffectRegistry.INSTANT_ENERGY));
		super.onPlayerCollision(player);
	}
	
	@Override
	protected BlockState getParticleBlockstate()
	{
		return Blocks.ICE.getDefaultState();
	}
}
