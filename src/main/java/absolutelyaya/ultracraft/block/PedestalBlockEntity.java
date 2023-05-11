package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity
{
	ItemStack stack;
	String type;
	
	public PedestalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.PEDESTAL, pos, state);
		stack = ItemStack.EMPTY;
	}
	
	public boolean onPunch(PlayerEntity player, boolean mainHand)
	{
		markDirty();
		PlayerInventory inventory = player.getInventory();
		if((player.getOffHandStack().isEmpty() && !mainHand) || (mainHand && player.getMainHandStack().isEmpty()))
		{
			if(!stack.isEmpty())
			{
				(mainHand ? inventory.main : inventory.offHand).set(mainHand ? inventory.selectedSlot : 0, stack);
				stack = ItemStack.EMPTY;
			}
			else
				return false;
		}
		else if(stack.isEmpty())
		{
			stack = mainHand ? player.getMainHandStack() : player.getOffHandStack();
			if(!player.isCreative())
				(mainHand ? inventory.main : inventory.offHand).set(mainHand ? inventory.selectedSlot : 0, ItemStack.EMPTY);
		}
		if(world != null)
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
		//if both stacks are not empty, do nothing, but don't count it as punching a regular block.
		return true;
	}
	
	public boolean copyItemDataRequiresOperator() {
		return true;
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		System.out.println("read -> " + nbt);
		if (nbt.contains("holding", 10))
			stack = ItemStack.fromNbt(nbt.getCompound("holding"));
		else
			stack = ItemStack.EMPTY;
		if(nbt.contains("type", NbtElement.STRING_TYPE))
		{
			type = nbt.getString("type");
			world.setBlockState(getPos(), getCachedState().with(PedestalBlock.TYPE, PedestalBlock.Type.valueOf(type.toUpperCase())));
		}
		else
			type = "none";
	}
	
	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket()
	{
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	@Override
	public NbtCompound toInitialChunkDataNbt()
	{
		return createNbt();
	}
	
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.put("holding", getStack().writeNbt(new NbtCompound()));
		nbt.putString("type", getCachedState().get(PedestalBlock.TYPE).name);
		System.out.println("write -> " + nbt);
	}
	
	public ItemStack getStack()
	{
		return stack;
	}
	
	public boolean isFancy()
	{
		return getCachedState().get(PedestalBlock.FANCY);
	}
}
