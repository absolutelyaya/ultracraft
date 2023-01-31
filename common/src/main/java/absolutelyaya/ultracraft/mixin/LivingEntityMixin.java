package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.ClientPlayerAccessor;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ClientPlayerAccessor
{
	int punchTicks, punchDuration = 6;
	boolean punching;
	float punchProgress, prevPunchProgress;
	
	@Inject(method = "tick", at = @At("HEAD"))
	void onTick(CallbackInfo ci)
	{
		punchTick();
	}
	
	void punchTick()
	{
		int i = punchDuration;
		if (punching)
		{
			++punchTicks;
			if (punchTicks >= i)
			{
				punchTicks = 0;
				punching = false;
			}
		}
		else
			punchTicks = 0;
		
		prevPunchProgress = punchProgress;
		punchProgress = (float)punchTicks / (float)i;
	}
	
	@Override
	public void Punch()
	{
		if(!punching)
		{
			punchTicks = -1;
			punching = true;
		}
	}
	
	@Override
	public float GetPunchProgress(float tickDelta)
	{
		float f = punchProgress - prevPunchProgress;
		if (f < 0.0F) {
			++f;
		}
		
		return prevPunchProgress + f * tickDelta;
	}
	
	@Override
	public boolean IsPunching()
	{
		return punching;
	}
}
