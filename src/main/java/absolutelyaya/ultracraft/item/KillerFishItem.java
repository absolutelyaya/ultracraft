package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KillerFishItem extends Item
{
	boolean wasSelected;
	
	public KillerFishItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		user.playSound(SoundRegistry.KILLERFISH_USE.value(), 1, 1);
		user.getItemCooldownManager().set(this, 10);
		return TypedActionResult.success(user.getStackInHand(hand));
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(world.isClient && !wasSelected && selected)
		{
			entity.playSound(SoundRegistry.KILLERFISH_SELECT.value(), 1, 1);
		}
		wasSelected = selected;
		super.inventoryTick(stack, world, entity, slot, selected);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		if(context.isAdvanced())
			tooltip.add(Text.translatable("item.ultracraft.killerfish-hiddenlore"));
	}
}
