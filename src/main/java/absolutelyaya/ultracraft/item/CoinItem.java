package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoinItem extends Item
{
	public CoinItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public ItemStack getDefaultStack()
	{
		return getStack("Nobody", 0);
	}
	
	public static ItemStack getStack(String name, int score)
	{
		ItemStack stack = new ItemStack(ItemRegistry.COIN);
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putInt("score", score);
		nbt.putString("name", name);
		return stack;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		
		NbtCompound nbt = stack.getNbt();
		if(nbt != null && nbt.contains("score", NbtElement.INT_TYPE) && nbt.contains("name", NbtElement.STRING_TYPE))
		{
			int score = nbt.getInt("score");
			String name = nbt.getString("name");
			tooltip.add(Text.translatable("item.ultracraft.coin.lore", score, name));
		}
	}
}
