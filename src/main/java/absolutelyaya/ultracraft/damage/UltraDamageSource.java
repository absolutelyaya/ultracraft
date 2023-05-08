package absolutelyaya.ultracraft.damage;


import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class UltraDamageSource extends EntityDamageSource
{
	public final DamageSources.Type type;
	
	public UltraDamageSource(DamageSources.Type type, Entity source)
	{
		super(type.name, source);
		this.type = type;
	}
	boolean hitscan;
	
	@Override
	public Text getDeathMessage(LivingEntity entity)
	{
		String string = "death.attack." + this.name;
		if(source instanceof PlayerEntity || (source != null && source.hasCustomName()))
			string += ".player";
		return Text.translatable(string, entity.getDisplayName(), this.source.getDisplayName());
	}
	
	public UltraDamageSource setHitscan()
	{
		hitscan = true;
		return this;
	}
	
	public boolean isHitscan()
	{
		return hitscan;
	}
	
	public boolean isOf(DamageSources.Type type)
	{
		return this.type.equals(type);
	}
}
