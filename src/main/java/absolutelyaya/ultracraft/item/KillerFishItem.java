package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class KillerFishItem extends Item
{
	Random rand = new Random();
	int wasSelected;
	
	public KillerFishItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		boolean hi = rand.nextInt(500) == 0;
		boolean lo = rand.nextInt(500) == 0;
		user.playSound(SoundRegistry.KILLERFISH_USE.value(), 1, hi ? 2f : lo ? 0.5f : 1f);
		user.getItemCooldownManager().set(this, 10);
		return TypedActionResult.success(user.getStackInHand(hand));
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if(slot < 0 || slot > 9)
			return;
		if(world.isClient && wasSelected <= 0 && selected)
			ClientPlayNetworking.send(PacketRegistry.KILLER_FISH_PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
		if(selected)
			wasSelected = 12;
		else if(wasSelected > 0)
			wasSelected--;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		if(context.isAdvanced())
			tooltip.add(Text.translatable("item.ultracraft.killerfish-hiddenlore"));
	}
}
