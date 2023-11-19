package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.rendering.item.TerminalItemRenderer;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TerminalItem extends BlockItem implements GeoItem
{
	AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
	final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	
	public TerminalItem(Block block, Settings settings)
	{
		super(block, settings);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public ItemStack getDefaultStack()
	{
		return getStack(TerminalBlockEntity.Base.YELLOW);
	}
	
	public static ItemStack getStack(TerminalBlockEntity.Base base)
	{
		ItemStack stack = new ItemStack(ItemRegistry.TERMINAL);
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putInt("base", base.ordinal());
		nbt.putInt("base-clr", base.getColor());
		nbt.putByte("graffiti-version", (byte)1);
		stack.setNbt(nbt);
		return stack;
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider()
		{
			final TerminalItemRenderer renderer = new TerminalItemRenderer();
			@Override
			public BuiltinModelItemRenderer getCustomRenderer()
			{
				return renderer;
			}
		});
	}
	
	@Override
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	public static TerminalBlockEntity.Base getBase(ItemStack stack)
	{
		if(!stack.isOf(ItemRegistry.TERMINAL) || !stack.hasNbt() || !stack.getNbt().contains("base", NbtElement.INT_TYPE))
			return TerminalBlockEntity.Base.YELLOW;
		int i = stack.getNbt().getInt("base");
		if(i >= 0 && i < TerminalBlockEntity.Base.values().length)
			return TerminalBlockEntity.Base.values()[i];
		else
			return TerminalBlockEntity.Base.YELLOW;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.ultracraft.terminal.color", Text.translatable(getBase(stack).translationKey())));
		if(context.isAdvanced())
			tooltip.add(Text.translatable("item.ultracraft.terminal.flavor",
					Text.translatable("item.ultracraft.terminal.flavor." + getBase(stack).name().toLowerCase()))
								.fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
	}
}
