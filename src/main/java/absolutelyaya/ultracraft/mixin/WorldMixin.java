package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import absolutelyaya.ultracraft.entity.demon.HideousPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLookup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class WorldMixin
{
	@Shadow protected abstract EntityLookup<Entity> getEntityLookup();
	
	@Inject(method = "getOtherEntities", at = @At(value = "RETURN"), cancellable = true)
	void onGetOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir)
	{
		List<Entity> list = cir.getReturnValue();
		getEntityLookup().forEachIntersects(box, (entity) -> {
			if(entity instanceof HideousMassEntity mass)
			{
				for (HideousPart part : mass.getParts())
				{
					if(part.getBoundingBox().intersects(box) && entity != except && predicate.test(part))
						list.add(part);
				}
			}
		});
		cir.setReturnValue(list);
	}
	
	@Inject(method = "collectEntitiesByType(Lnet/minecraft/util/TypeFilter;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;Ljava/util/List;I)V", at = @At("RETURN"), cancellable = true)
	<T extends Entity> void onGetEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate, List<? super T> result, int limit, CallbackInfo ci)
	{
		getEntityLookup().forEachIntersects(filter, box, (entity) -> {
			if (!(entity instanceof HideousMassEntity mass))
				return LazyIterationConsumer.NextIteration.CONTINUE;
			for (HideousPart part : mass.getParts())
			{
				T e = filter.downcast(part);
				if (e == null || !predicate.test(e))
					continue;
				result.add(e);
				if (result.size() >= limit)
					return LazyIterationConsumer.NextIteration.ABORT;
			}
			return LazyIterationConsumer.NextIteration.CONTINUE;
		});
	}
}
