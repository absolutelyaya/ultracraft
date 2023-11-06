package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.AbstractPlushieRenderer;
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
	final RawAnimation POSE_SIT = RawAnimation.begin().thenPlay("sit");
	final RawAnimation POSE_HAKITA = RawAnimation.begin().thenPlay("hakita");
	final RawAnimation POSE_SLIDE = RawAnimation.begin().thenPlay("slide");
	final RawAnimation ANIM_BOW = RawAnimation.begin().thenPlay("bow");
	final RawAnimation BREAKDANCE = RawAnimation.begin().thenPlay("breakdance");
	final RawAnimation HELL_NEOW = RawAnimation.begin().thenPlay("hellneow");
	final RawAnimation POSE_V2 = RawAnimation.begin().thenPlay("v2");
	//Swordsmachine Exclusive Animations
	final RawAnimation SM_SIT = RawAnimation.begin().thenPlay("sit");
	final RawAnimation SM_HELD = RawAnimation.begin().thenPlay("held");
	protected Type defaultType = Type.V1;
	
	public PlushieItem(Settings settings)
	{
		super(settings);
	}
	
	public ItemStack getDefaultStack(String type)
	{
		
		ItemStack stack = switch(type)
		{
			case "pitr" -> new ItemStack(ItemRegistry.PITR);
			case "pitrpoin" -> new ItemStack(ItemRegistry.PITR_POIN);
			case "talon" -> new ItemStack(ItemRegistry.TALON);
			case "v2" -> new ItemStack(ItemRegistry.V2);
			default -> new ItemStack(this);
		};
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
		return getType(((AbstractPlushieRenderer<?>)((RenderProvider)getRenderProvider().get()).getCustomRenderer()).getCurrentItemStack());
	}
	
	public Type getType(ItemStack stack)
	{
		if(stack == null)
			return defaultType;
		NbtCompound nbt = stack.getNbt();
		if(nbt == null || !nbt.contains("type"))
			return defaultType;
		return switch(nbt.getString("type"))
		{
			default -> defaultType;
			case "yaya" -> Type.YAYA;
			case "hakita" -> Type.HAKITA;
			case "pitr" -> Type.PITR;
			case "pitrpoin" -> Type.PITRPOIN;
			case "swordsmachine" -> Type.SWORDSMACHINE;
			case "tundra" -> Type.SWORDSMACHINE_TUNDRA;
			case "agony" -> Type.SWORDSMACHINE_AGONY;
			case "talon" -> Type.TALON;
			case "v2" -> Type.V2;
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
			case PITRPOIN -> controller.setAnimation(HELL_NEOW);
			case SWORDSMACHINE, SWORDSMACHINE_TUNDRA, SWORDSMACHINE_AGONY -> {
				ItemStack stack = ((AbstractPlushieRenderer<?>)((RenderProvider)getRenderProvider().get()).getCustomRenderer()).getCurrentItemStack();
				if(stack.hasNbt() && stack.getNbt().getBoolean("holding")) //scuffed, like all plushies so far lol
					controller.setAnimation(SM_HELD);
				else
					controller.setAnimation(SM_SIT);
			}
			case V1 -> controller.setAnimation(POSE_SLIDE);
			case TALON -> controller.setAnimation(ANIM_BOW);
			case V2 -> controller.setAnimation(POSE_V2);
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
		V1("V1", "item.ultracraft.plushie.v1-lore"),
		PITRPOIN("PITR (Hell-Neow Cosplay Edition)", "item.ultracraft.plushie.pitrpoin-lore"),
		SWORDSMACHINE("Swordsmachine", "item.ultracraft.plushie.swordsmachine-lore"),
		SWORDSMACHINE_TUNDRA("Tundra", "item.ultracraft.plushie.swordsmachine.tundra-lore"),
		SWORDSMACHINE_AGONY("Agony", "item.ultracraft.plushie.swordsmachine.agony-lore"),
		TALON("TalonMC", "item.ultracraft.plushie.talon-lore"),
		V2("V2", "item.ultracraft.plushie.v2-lore");
		
		Type(String name, String lore)
		{
			this.name = name;
			this.lore = lore;
		}
		public final String name;
		public final String lore;
	}
}
