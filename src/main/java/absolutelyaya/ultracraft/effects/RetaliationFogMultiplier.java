package absolutelyaya.ultracraft.effects;

import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.MathHelper;

public class RetaliationFogMultiplier implements BackgroundRenderer.StatusEffectFogModifier
{
	@Override
	public StatusEffect getStatusEffect()
	{
		return StatusEffectRegistry.RETALIATION;
	}
	
	@Override
	public void applyStartEndModifier(BackgroundRenderer.FogData fogData, LivingEntity entity, StatusEffectInstance effect, float viewDistance, float tickDelta)
	{
		float f = effect.isInfinite() ? 7.5f : MathHelper.lerp(Math.min(1.0f, effect.getDuration() / 60.0f), viewDistance, 7.5f);
		if (fogData.fogType == BackgroundRenderer.FogType.FOG_SKY)
		{
			fogData.fogStart = 0.0f;
			fogData.fogEnd = f * 1.6f;
		}
		else
		{
			fogData.fogStart = f * 0.25f;
			fogData.fogEnd = f * 1.5f;
		}
	}
	
	@Override
	public float applyColorModifier(LivingEntity entity, StatusEffectInstance effect, float f, float tickDelta)
	{
		return 1f;
	}
}
