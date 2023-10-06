package absolutelyaya.ultracraft.damage;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class DamageTypeTags
{
	public static final TagKey<DamageType> HITSCAN = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "scan"));
	public static final TagKey<DamageType> ULTRACRAFT = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "all"));
	public static final TagKey<DamageType> IS_PER_TICK = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "is_per_tick"));
	public static final TagKey<DamageType> UNDODGEABLE = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "undodgeable"));
	public static final TagKey<DamageType> UNBOOSTED = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "unboosted"));
	public static final TagKey<DamageType> NO_BLEEDING = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "no_bleeding"));
	public static final TagKey<DamageType> MELEE = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "melee"));
	public static final TagKey<DamageType> EXPLODE_PLUSHIE = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Ultracraft.MOD_ID, "explode_plushie"));
}
