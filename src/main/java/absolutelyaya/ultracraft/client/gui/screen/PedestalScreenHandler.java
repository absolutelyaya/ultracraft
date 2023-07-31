package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.block.PedestalBlock;
import absolutelyaya.ultracraft.registry.ScreenHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PedestalScreenHandler extends ScreenHandler
{
	Inventory inventory;
	BlockPos origin;
	
	public PedestalScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, BlockPos pos)
	{
		super(type, syncId);
		this.inventory = inventory;
		playerInventory.onOpen(playerInventory.player);
		addSlot(new Slot(inventory, 1, 80, 17));
		
		int i;
		int j;
		for(i = 0; i < 3; ++i) {
			for(j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for(i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
		}
		origin = pos;
	}
	
	@Override
	public ItemStack quickMove(PlayerEntity player, int slot)
	{
		Slot itemSlot = slots.get(slot);
		if(itemSlot == null || !itemSlot.hasStack())
			return ItemStack.EMPTY;
		return itemSlot.getStack();
	}
	
	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player)
	{
		if(slotIndex < 0 || slotIndex >= slots.size())
			return;
		if(slotIndex == 0)
		{
			slots.get(0).setStack(ItemStack.EMPTY);
			return;
		}
		Slot clicked = slots.get(slotIndex);
		if(clicked.hasStack())
		{
			ItemStack stack = clicked.getStack().copy();
			stack.setCount(1);
			slots.get(0).setStack(stack);
		}
	}
	
	@Override
	public boolean canUse(PlayerEntity player)
	{
		return inventory.canPlayerUse(player);
	}
	
	@Override
	public void onClosed(PlayerEntity player)
	{
		super.onClosed(player);
		updatePedestalBlock(player);
	}
	
	public void updatePedestalBlock(PlayerEntity p)
	{
		World world = p.getWorld();
		if(!world.isClient && p.getWorld().getBlockState(origin).getBlock() instanceof PedestalBlock pedestal)
		{
			world.updateNeighbors(origin, pedestal);
			world.updateNeighbors(origin.down(), pedestal);
		}
	}
	
	public static PedestalScreenHandler createPedestalHandler(int syncId, PlayerInventory playerInventory)
	{
		return new PedestalScreenHandler(ScreenHandlerRegistry.PEDESTAL, syncId, playerInventory, new SimpleInventory(2), BlockPos.ORIGIN);
	}
}
