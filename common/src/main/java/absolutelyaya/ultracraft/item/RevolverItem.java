package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RevolverItem extends AbstractWeaponItem
{
	public RevolverItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		user.setCurrentHand(hand);
		return TypedActionResult.consume(itemStack);
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.NONE;
	}
	
	@Override
	public void onPrimaryFire(World world, PlayerEntity user)
	{
		if(!world.isClient)
		{
			ServerHitscanHandler.performHitscan(user, (byte)0, 4, false);
		}
	}
	
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		if(remainingUseTicks <= 0)
		{
			if(user instanceof PlayerEntity player) {
				approxCooldown = 50;
				player.getItemCooldownManager().set(this, 50);
			}
			if(!world.isClient)
				ServerHitscanHandler.performHitscan(user, (byte)1, 10, true);
		}
	}
	
	@Override
	public boolean isUsedOnRelease(ItemStack stack)
	{
		return true;
	}
	
	@Override
	public int getMaxUseTime(ItemStack stack)
	{
		return 15;
	}
}
