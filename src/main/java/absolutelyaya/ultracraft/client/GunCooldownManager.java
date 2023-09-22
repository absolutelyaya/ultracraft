package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GunCooldownManager
{
	public static final int PRIMARY = 0;
	public static final int SECONDARY = 1;
	public static final int TRITARY = 2;
	
	final PlayerEntity owner;
	final HashMap<Class<? extends AbstractWeaponItem>, Entry[]> cooldowns = new HashMap<>();
	
	public GunCooldownManager(PlayerEntity owner)
	{
		this.owner = owner;
	}
	
	public void tickCooldowns()
	{
		List<Class<? extends AbstractWeaponItem>> remove = new ArrayList<>();
		Class<? extends AbstractWeaponItem>[] keys = cooldowns.keySet().toArray(new Class[]{});
		for (int i = 0; i < cooldowns.size(); i++)
		{
			Class<? extends AbstractWeaponItem> key = keys[i];
			boolean r = true;
			for (int j = 0; j < cooldowns.get(key).length; j++)
			{
				Entry cd = cooldowns.get(key)[j];
				if(cd.get() > 0)
				{
					cd.decrement();
					r = false;
				}
			}
			if(r)
				remove.add(key);
		}
		remove.forEach(cooldowns::remove);
	}
	
	public void setCooldown(AbstractWeaponItem item, int ticks, int idx)
	{
		if(!owner.getWorld().isClient)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeItemStack(item.getDefaultStack());
			buf.writeInt(ticks);
			buf.writeInt(idx);
			ServerPlayNetworking.send((ServerPlayerEntity)owner, PacketRegistry.SET_GUNCD_PACKET_ID, buf);
		}
		if(!cooldowns.containsKey(item.getCooldownClass()))
		{
			List<Entry> list = new ArrayList<>();
			for (int i = 0; i < 3; i++)
			{
				if(i == idx)
					list.add(new Entry(ticks, new AtomicInteger(ticks)));
				else
					list.add(new Entry(0, new AtomicInteger(0)));
			}
			cooldowns.put(item.getCooldownClass(), list.toArray(new Entry[3]));
		}
		else
			cooldowns.get(item.getCooldownClass())[idx].set(ticks, ticks);
	}
	
	public int getCooldown(AbstractWeaponItem item, int idx)
	{
		return getCooldown(item.getCooldownClass(), idx);
	}
	
	public int getCooldown(Class<? extends AbstractWeaponItem> item, int idx)
	{
		if(cooldowns.containsKey(item) && idx < cooldowns.get(item).length)
			return cooldowns.get(item)[idx].get();
		else
			return 0;
	}
	
	public boolean isUsable(AbstractWeaponItem item, int idx)
	{
		return isUsable(item.getCooldownClass(), idx);
	}
	
	public boolean isUsable(Class<? extends AbstractWeaponItem> item, int idx)
	{
		if(cooldowns.containsKey(item) && idx < cooldowns.get(item).length)
			return cooldowns.get(item)[idx].get() - (owner.getWorld().isClient ? 1 : 0) <= 0;
		else
			return true;
	}
	
	public float getCooldownPercent(AbstractWeaponItem item, int idx)
	{
		return getCooldownPercent(item.getCooldownClass(), idx);
	}
	
	public float getCooldownPercent(Class<? extends AbstractWeaponItem> item, int idx)
	{
		if(!cooldowns.containsKey(item) || idx > cooldowns.get(item).length)
			return 0f;
		Entry e = cooldowns.get(item)[idx];
		return e.percent();
	}
	
	public static class Entry
	{
		public int duration;
		final AtomicInteger remaining;
		
		public Entry(int duration, AtomicInteger remaining)
		{
			this.duration = duration;
			this.remaining = remaining;
		}
		
		public int get()
		{
			return remaining.get();
		}
		
		public void set(int val)
		{
			remaining.set(val);
		}
		
		public void set(int duration, int val)
		{
			this.duration = duration;
			remaining.set(val);
		}
		
		public void decrement()
		{
			remaining.getAndDecrement();
		}
		
		public float percent()
		{
			return (float)remaining.get() / (float)duration;
		}
	}
}
