package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.entity.machine.DestinyBondSwordsmachineEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DestinyBondSpawnEggItem extends SpawnEggItem
{
	public DestinyBondSpawnEggItem(Settings settings)
	{
		super(null, 0xffffff, 0xffffff, settings);
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		DestinyBondSwordsmachineEntity.spawn(context.getWorld(), context.getBlockPos().add(context.getSide().getVector()).toCenterPos(), context.getPlayerYaw() + 180);
		return ActionResult.CONSUME;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		return TypedActionResult.fail(user.getStackInHand(hand));
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.ultracraft.destiny_swordsmachine_spawn_egg.lore"));
	}
	
	@Override
	public FeatureSet getRequiredFeatures()
	{
		return FeatureSet.empty();
	}
}
