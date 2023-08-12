package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.client.gui.screen.PedestalScreenHandler;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity implements NamedScreenHandlerFactory
{
	Inventory inventory;
	String type;
	
	public PedestalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.PEDESTAL, pos, state);
		inventory = new SimpleInventory(2);
	}
	
	public boolean onPunch(PlayerEntity player, boolean mainHand)
	{
		markDirty();
		PlayerInventory playerInventory = player.getInventory();
		ItemStack held = inventory.getStack(0);
		if((player.getOffHandStack().isEmpty() && !mainHand) || (mainHand && player.getMainHandStack().isEmpty()))
		{
			if(!held.isEmpty())
			{
				(mainHand ? playerInventory.main : playerInventory.offHand).set(mainHand ? playerInventory.selectedSlot : 0, held);
				inventory.setStack(0, ItemStack.EMPTY);
			}
			else
				return false;
		}
		else if(held.isEmpty())
		{
			inventory.setStack(0, mainHand ? player.getMainHandStack() : player.getOffHandStack());
			if(!player.isCreative())
				(mainHand ? playerInventory.main : playerInventory.offHand).set(mainHand ? playerInventory.selectedSlot : 0, ItemStack.EMPTY);
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
		if (nbt.contains("holding", 10))
			inventory.setStack(0, ItemStack.fromNbt(nbt.getCompound("holding")));
		else
			inventory.setStack(0, ItemStack.EMPTY);
		if (nbt.contains("key", 10))
			inventory.setStack(1, ItemStack.fromNbt(nbt.getCompound("key")));
		else
			inventory.setStack(1, ItemStack.EMPTY);
		if(nbt.contains("locked", NbtElement.BYTE_TYPE))
			world.setBlockState(getPos(), getCachedState().with(PedestalBlock.LOCKED, nbt.getBoolean("locked")));
		if(nbt.contains("type", NbtElement.STRING_TYPE) && world != null)
		{
			type = nbt.getString("type");
			world.setBlockState(getPos(), getCachedState().with(PedestalBlock.TYPE, PedestalBlock.Type.valueOf(type.toUpperCase())));
		}
		else
			type = "none";
	}
	
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
		nbt.put("holding", getHeld().writeNbt(new NbtCompound()));
		nbt.put("key", getKey().writeNbt(new NbtCompound()));
		nbt.putString("type", getCachedState().get(PedestalBlock.TYPE).name);
		nbt.putBoolean("fancy", getCachedState().get(PedestalBlock.FANCY));
		nbt.putBoolean("locked", getCachedState().get(PedestalBlock.LOCKED));
	}
	
	public ItemStack getHeld()
	{
		return inventory.getStack(0);
	}
	
	public ItemStack getKey()
	{
		return inventory.getStack(1);
	}
	
	public boolean isFancy()
	{
		return getCachedState().get(PedestalBlock.FANCY);
	}
	
	@Override
	public Text getDisplayName()
	{
		return Text.translatable("screen.ultracraft.pedestal");
	}
	
	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player)
	{
		return new PedestalScreenHandler(ScreenHandlerRegistry.PEDESTAL, syncId, playerInventory, inventory, getPos());
	}
}
