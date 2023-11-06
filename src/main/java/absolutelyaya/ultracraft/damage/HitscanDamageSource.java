package absolutelyaya.ultracraft.damage;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public class HitscanDamageSource extends DamageSource
{
	public final ServerHitscanHandler.Hitscan hitscan;
	
	public HitscanDamageSource(RegistryEntry<DamageType> type, @Nullable Entity attacker, ServerHitscanHandler.Hitscan hitscan)
	{
		super(type, attacker);
		this.hitscan = hitscan;
	}
	
	public HitscanDamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker, ServerHitscanHandler.Hitscan hitscan)
	{
		super(type, source, attacker);
		this.hitscan = hitscan;
	}
}
