package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.SwordsmachinePlushieRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Arm;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.object.PlayState;

import java.util.function.Consumer;

//scuffed, like all plushies so far lol
public class SwordsmachinePlushieItem extends PlushieItem
{
	AnimatableInstanceCache cache = new ContextBasedAnimatableInstanceCache(this);
	
	public SwordsmachinePlushieItem(Settings settings)
	{
		super(settings);
		defaultType = Type.SWORDSMACHINE;
	}
	
	public ItemStack getDefaultStack(String type)
	{
		ItemStack stack = new ItemStack(this);
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.put("type", NbtString.of(type));
		stack.setNbt(nbt);
		return stack;
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private SwordsmachinePlushieRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new SwordsmachinePlushieRenderer();
				
				return renderer;
			}
		});
	}
	
	protected <E extends GeoItem> PlayState predicate(AnimationState<E> event)
	{
		AnimationController<?> controller = event.getController();
		Arm main = MinecraftClient.getInstance().player.getMainArm();
		switch (event.getData(DataTickets.ITEM_RENDER_PERSPECTIVE))
		{
			case FIRST_PERSON_LEFT_HAND -> controller.setAnimation(main.equals(Arm.LEFT) ? SM_HELD : SM_SIT);
			case FIRST_PERSON_RIGHT_HAND -> controller.setAnimation(main.equals(Arm.RIGHT) ? SM_HELD : SM_SIT);
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> controller.setAnimation(SM_HELD);
			default -> controller.setAnimation(SM_SIT);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "swordsmachine", 0, this::predicate));
	}
}
