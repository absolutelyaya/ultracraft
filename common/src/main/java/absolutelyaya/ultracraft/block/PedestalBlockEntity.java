package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class PedestalBlockEntity extends BlockEntity
{
	ItemStack stack;
	
	public PedestalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.PEDESTAL.get(), pos, state);
		stack = ItemStack.EMPTY;
	}
	
	public boolean onPunch(PlayerEntity player)
	{
		markDirty();
		if(player.getOffHandStack().isEmpty())
		{
			if(!stack.isEmpty())
			{
				player.getInventory().offHand.set(0, stack);
				stack = ItemStack.EMPTY;
			}
			else
				return false;
		}
		else if(stack.isEmpty())
		{
			stack = player.getOffHandStack();
			player.getInventory().offHand.set(0, ItemStack.EMPTY);
		}
		//if both stacks are not empty, do nothing, but don't count it as punching a regular block.
		return true;
	}
	
	public boolean copyItemDataRequiresOperator() {
		return true;
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if (nbt.contains("holding", 10))
			stack = ItemStack.fromNbt(nbt.getCompound("holding"));
		else
			stack = ItemStack.EMPTY;
		//TODO: send to clients
	}
	
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		if (!getStack().isEmpty())
			nbt.put("holding", getStack().writeNbt(new NbtCompound()));
	}
	
	public ItemStack getStack()
	{
		return stack;
	}
}
