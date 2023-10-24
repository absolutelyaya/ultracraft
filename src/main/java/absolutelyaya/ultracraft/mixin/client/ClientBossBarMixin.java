package absolutelyaya.ultracraft.mixin.client;

import absolutelyaya.ultracraft.accessor.BossBarAccessor;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientBossBar.class)
public abstract class ClientBossBarMixin implements BossBarAccessor
{
	@Shadow public abstract float getPercent();
	
	float deltaPercent, timeSinceLastChange;
	
	@Inject(method = "setPercent", at = @At("TAIL"))
	void onSetPercent(float percent, CallbackInfo ci)
	{
		timeSinceLastChange = 0f;
	}
	
	@Override
	public float getDeltaPercent()
	{
		return deltaPercent;
	}
	
	@Override
	public void update(float delta)
	{
		timeSinceLastChange += delta;
		if(timeSinceLastChange > 1f)
			deltaPercent = MathHelper.lerp(delta * 20, deltaPercent, getPercent());
	}
}
