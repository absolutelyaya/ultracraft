package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.entity.other.StainedGlassWindow;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class StainedGlassWindowItem extends DecorationItem
{
	
	public StainedGlassWindowItem(EntityType<? extends AbstractDecorationEntity> type, Settings settings)
	{
		super(type, settings);
	}
	
	public static ItemStack getStack(boolean reinforced)
	{
		ItemStack stack = new ItemStack(ItemRegistry.STAINED_GLASS_WINDOW);
		if(reinforced)
			stack.getOrCreateNbt().putBoolean("reinforced", reinforced);
		return stack;
	}
	
	@Override
	public Text getName(ItemStack stack)
	{
		return isReinforced(stack) ? Text.translatable("entity.ultracraft.reinforce_stained_glass_window") : super.getName(stack);
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		StainedGlassWindow window;
		Direction direction = context.getSide();
		BlockPos pos = context.getBlockPos().offset(direction);
		PlayerEntity player = context.getPlayer();
		ItemStack stack = context.getStack();
		if (player != null && !canPlaceOn(player, direction, stack, pos))
			return ActionResult.FAIL;
		World world = context.getWorld();
		window = StainedGlassWindow.place(world, pos, direction);
		if (window == null)
			return ActionResult.CONSUME;
		if (window.canStayAttached())
		{
			if (!world.isClient)
			{
				window.onPlace();
				window.setReinforced(isReinforced(stack));
				world.emitGameEvent(player, GameEvent.ENTITY_PLACE, window.getPos());
				world.spawnEntity(window);
			}
			stack.decrement(1);
			return ActionResult.success(world.isClient);
		}
		return ActionResult.CONSUME;
	}
	
	boolean isReinforced(ItemStack stack)
	{
		if(!stack.hasNbt() || !stack.getNbt().contains("reinforced", NbtElement.BYTE_TYPE))
			return false;
		return stack.getNbt().getBoolean("reinforced");
	}
}
