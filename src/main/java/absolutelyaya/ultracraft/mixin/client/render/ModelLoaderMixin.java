package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin
{
	@Shadow protected abstract void addModel(ModelIdentifier modelId);
	
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelLoader;addModel(Lnet/minecraft/client/util/ModelIdentifier;)V", ordinal = 2))
	void onAddModel(BlockColors blockColors, Profiler profiler, Map jsonUnbakedModels, Map blockStates, CallbackInfo ci)
	{
		addModel(new ModelIdentifier(Ultracraft.MOD_ID, "harpoon_long", "inventory"));
		addModel(new ModelIdentifier(Ultracraft.MOD_ID, "trinkets/player_back_tank", "inventory"));
		addModel(new ModelIdentifier(Ultracraft.MOD_ID, "fake_shield", "inventory"));
		addModel(new ModelIdentifier(Ultracraft.MOD_ID, "fake_banner", "inventory"));
		addModel(new ModelIdentifier(Ultracraft.MOD_ID, "fake_terminal", "inventory"));
	}
}
