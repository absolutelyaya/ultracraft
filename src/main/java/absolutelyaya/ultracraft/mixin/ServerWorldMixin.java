package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ServerWorldAccessor;
import absolutelyaya.ultracraft.entity.demon.HideousPart;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements ServerWorldAccessor
{
	Int2ObjectMap<HideousPart> hideousParts = new Int2ObjectOpenHashMap<>();
	
	@Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
	void onTickEntity(Entity entity, CallbackInfo ci)
	{
		if(Ultracraft.isTimeFrozen())
			ci.cancel();
	}
	
	@Override
	public Int2ObjectMap<HideousPart> getHideousParts()
	{
		return hideousParts;
	}
}