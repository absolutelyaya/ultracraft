package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		if(context.isAdvanced())
		{
			if(stack.isOf(ItemRegistry.EJECTED_CORE))
				tooltip.add(Text.translatable("item.ultracraft.ejected_core.hiddenlore"));
			else
				tooltip.add(Text.translatable("item.ultracraft.hell_bullet.hiddenlore"));
		}
	}
}
