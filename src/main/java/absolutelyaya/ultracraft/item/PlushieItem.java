package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.PlushieRenderer;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlushieItem extends Item implements GeoItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	RawAnimation POSE_SIT = RawAnimation.begin().thenPlay("sit");
	RawAnimation POSE_HAKITA = RawAnimation.begin().thenPlay("hakita");
	RawAnimation POSE_SLIDE = RawAnimation.begin().thenPlay("slide");
	RawAnimation BREAKDANCE = RawAnimation.begin().thenPlay("breakdance");
	
	public PlushieItem(Settings settings)
	{
		super(settings);
	}
	
	public ItemStack getDefaultStack(String type)
	{
		ItemStack stack = type.equals("pitr") ? new ItemStack(ItemRegistry.PITR) : new ItemStack(this);
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.put("type", NbtString.of(type));
		stack.setNbt(nbt);
		return stack;
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private PlushieRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new PlushieRenderer();
				
				return renderer;
			}
		});
	}
	
	public Type getType()
	{
		return getType(((PlushieRenderer)((RenderProvider)getRenderProvider().get()).getCustomRenderer()).getCurrentItemStack());
	}
	
	public Type getType(ItemStack stack)
	{
		if(stack == null)
			return Type.V1;
		NbtCompound nbt = stack.getNbt();
		if(nbt == null || !nbt.contains("type"))
			return Type.V1;
		return switch(nbt.getString("type"))
		{
			default -> Type.V1;
			case "yaya" -> Type.YAYA;
			case "hakita" -> Type.HAKITA;
			case "pitr" -> Type.PITR;
		};
	}
	
	@Override
	public Text getName(ItemStack stack)
	{
		return Text.translatable("item.ultracraft.plushie");
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		Type type = getType(stack);
		tooltip.add(Text.translatable("item.ultracraft.plushie.lore", type.name));
		tooltip.add(Text.translatable(type.lore));
	}
	
	protected <E extends GeoItem> PlayState predicate(AnimationState<E> event)
	{
		AnimationController<?> controller = event.getController();
		
		controller.setAnimationSpeed(1f);
		switch (getType())
		{
			case YAYA -> controller.setAnimation(POSE_SIT);
			case HAKITA -> controller.setAnimation(POSE_HAKITA);
			case PITR ->
			{
				controller.setAnimationSpeed(2f);
				controller.setAnimation(BREAKDANCE);
			}
			case V1 -> controller.setAnimation(POSE_SLIDE);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "plushie", 0, this::predicate));
	}
	
	@Override
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	public enum Type
	{
		YAYA("Yaya", "item.ultracraft.plushie.yaya-lore"),
		HAKITA("Hakita", "item.ultracraft.plushie.hakita-lore"),
		PITR("PITR", "item.ultracraft.plushie.pitr-lore"),
		V1("V1", "item.ultracraft.plushie.v1-lore");
		
		Type(String name, String lore)
		{
			this.name = name;
			this.lore = lore;
		}
		public final String name;
		public final String lore;
	}
}