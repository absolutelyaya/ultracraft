package absolutelyaya.ultracraft.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HitscanDamageSource extends DamageSource
{
	public final byte hitscanType;
	public final int bounces, maxHits;
	public final float autoAim;
	
	public HitscanDamageSource(RegistryEntry<DamageType> type, @Nullable Entity attacker, byte hitscanType, int maxHits, int bounces, float autoaim)
	{
		super(type, attacker);
		this.hitscanType = hitscanType;
		this.maxHits = maxHits;
		this.bounces = bounces;
		this.autoAim = autoaim;
	}
	
	public HitscanDamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker, byte hitscanType, int maxHits, int bounces, float autoaim)
	{
		super(type, source, attacker);
		this.hitscanType = hitscanType;
		this.maxHits = maxHits;
		this.bounces = bounces;
		this.autoAim = autoaim;
	}
	
	public HitscanDamageSource asRicochet(World world)
	{
		return new HitscanDamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageSources.RICOCHET),
				getSource(), getAttacker(), hitscanType, maxHits, bounces, autoAim);
	}
}
