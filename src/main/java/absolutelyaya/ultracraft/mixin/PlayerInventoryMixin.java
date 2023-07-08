package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin
{
	@Shadow @Final private List<DefaultedList<ItemStack>> combinedInventory;
	
	@Shadow @Final public PlayerEntity player;
	
	@Shadow public int selectedSlot;
	
	@Inject(method = "updateItems", at = @At(value = "HEAD"), cancellable = true)
	void onInventoryTick(CallbackInfo ci)
	{
		int i = 0;
		for (DefaultedList<ItemStack> defaultedList : combinedInventory)
		{
			for (int ii = 0; ii < defaultedList.size(); ++ii)
			{
				ItemStack stack = defaultedList.get(ii);
				if (stack.isEmpty())
					continue;
				if(stack.getItem() instanceof AbstractWeaponItem)
					stack.inventoryTick(player.getWorld(), player, ii, selectedSlot == ii && i == 0);
				else
					stack.inventoryTick(player.getWorld(), player, ii, selectedSlot == ii);
			}
			i++;
		}
		ci.cancel();
	}
}
