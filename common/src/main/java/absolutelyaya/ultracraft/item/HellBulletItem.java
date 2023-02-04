package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class HellBulletItem extends Item
{
	public HellBulletItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
		if (!world.isClient) {
			HellBulletEntity hellBullet = HellBulletEntity.spawn(user, world);
			hellBullet.setItem(itemStack);
			hellBullet.setVelocity(user, user.getPitch(), user.getYaw(), 0f, 1f, 0f);
			world.spawnEntity(hellBullet);
		}
		
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		if (!user.getAbilities().creativeMode) {
			itemStack.decrement(1);
		}
		
		return TypedActionResult.success(itemStack, world.isClient());
	}
}
