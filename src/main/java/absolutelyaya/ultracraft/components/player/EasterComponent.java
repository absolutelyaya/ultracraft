package absolutelyaya.ultracraft.components.player;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class EasterComponent implements IEasterComponent
{
	final PlayerEntity provider;
	int fish, plushie;
	
	public EasterComponent(PlayerEntity provider)
	{
		this.provider = provider;
	}
	
	@Override
	public int getFishes()
	{
		return fish;
	}
	
	@Override
	public void addFish()
	{
		fish++;
		sync();
	}
	
	@Override
	public int getPlushies()
	{
		return plushie;
	}
	
	@Override
	public void addPlushie()
	{
		plushie++;
		if(plushie > 24)
			provider.addStatusEffect(new StatusEffectInstance(StatusEffectRegistry.RETALIATION, -1));
	}
	
	@Override
	public void removePlushie()
	{
		plushie--;
	}
	
	@Override
	public void sync()
	{
		UltraComponents.EASTER.sync(provider);
	}
	
	@Override
	public void readFromNbt(NbtCompound tag)
	{
		fish = tag.getInt("fish");
	}
	
	@Override
	public void writeToNbt(NbtCompound tag)
	{
		tag.putInt("fish", fish);
	}
}
