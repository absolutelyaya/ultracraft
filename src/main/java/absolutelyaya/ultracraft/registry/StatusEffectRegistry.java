package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.effects.ChilledStatusEffect;
import absolutelyaya.ultracraft.effects.ImpaledStatusEffect;
import absolutelyaya.ultracraft.effects.InstantEnergyStatusEffect;
import absolutelyaya.ultracraft.effects.RetaliationStatusEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
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
	public static final StatusEffect IMPALED = new ImpaledStatusEffect(StatusEffectCategory.HARMFUL, 0x670005).addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "287141e7-28fa-4d2a-baf2-4327cfd9f900", -0.4f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	public static final StatusEffect RETALIATION = new RetaliationStatusEffect(StatusEffectCategory.HARMFUL, 0x670005);
	
	public static void register()
	{
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Ultracraft.MOD_ID, "chilled"), CHILLED);
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Ultracraft.MOD_ID, "instant_energy"), INSTANT_ENERGY);
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Ultracraft.MOD_ID, "impaled"), IMPALED);
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Ultracraft.MOD_ID, "retaliation"), RETALIATION);
	}
}
