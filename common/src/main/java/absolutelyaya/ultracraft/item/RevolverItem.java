package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
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
	public void onPrimaryFire(World world, PlayerEntity user)
	{
		HitResult hit = user.raycast(32.0, 0f, false);
		if(world.isClient)
			UltracraftClient.HITSCAN_HANDLER.addEntry(user.getPos().add(
					new Vec3d(-0.5f, 1.5f, 0.2f).rotateY(-(float)Math.toRadians(user.getYaw()))), hit.getPos(), user.getPitch(), (byte)0);
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
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		if(!world.isClient && remainingUseTicks <= 0)
		{
			System.out.println("Secondary Fire!");
			if(user instanceof PlayerEntity player)
			{
				approxCooldown = 50;
				player.getItemCooldownManager().set(this, 50);
			}
		}
		HitResult hit = user.raycast(32.0, 0f, false);
		if(world.isClient && remainingUseTicks <= 0)
			UltracraftClient.HITSCAN_HANDLER.addEntry(user.getPos().add(
					new Vec3d(-0.5f, 1.5f, 0.2f).rotateY(-(float)Math.toRadians(user.getYaw()))), hit.getPos(), user.getPitch(), (byte)1);
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
