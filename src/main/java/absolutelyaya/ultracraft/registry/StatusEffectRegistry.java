package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.effects.ChilledStatusEffect;
import absolutelyaya.ultracraft.effects.InstantEnergyStatusEffect;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class StatusEffectRegistry
{
	public static final StatusEffect CHILLED = new ChilledStatusEffect(StatusEffectCategory.HARMFUL, 0xbef1ee);
	public static final InstantStatusEffect INSTANT_ENERGY = new InstantEnergyStatusEffect(StatusEffectCategory.BENEFICIAL, 0x3978a8);
	
	public static void register()
	{
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Ultracraft.MOD_ID, "chilled"), CHILLED);
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Ultracraft.MOD_ID, "instant_energy"), INSTANT_ENERGY);
	}
}
