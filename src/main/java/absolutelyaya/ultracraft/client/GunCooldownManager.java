package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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
	
	final PlayerEntity owner;
	final HashMap<Item, Entry[]> cooldowns = new HashMap<>();
	
	public GunCooldownManager(PlayerEntity owner)
	{
		this.owner = owner;
	}
	
	public void tickCooldowns()
	{
		List<Item> remove = new ArrayList<>();
		Object[] keys = cooldowns.keySet().toArray();
		for (int i = 0; i < cooldowns.size(); i++)
		{
			Item key = (Item)keys[i];
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
	
	public void setCooldown(Item item, int ticks, int idx)
	{
		if(!owner.getWorld().isClient)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeItemStack(item.getDefaultStack());
			buf.writeInt(ticks);
			buf.writeInt(idx);
			ServerPlayNetworking.send((ServerPlayerEntity)owner, PacketRegistry.SET_GUNCD_PACKET_ID, buf);
		}
		if(!cooldowns.containsKey(item))
		{
			List<Entry> list = new ArrayList<>();
			for (int i = 0; i < 2; i++)
			{
				if(i == idx)
					list.add(new Entry(ticks, new AtomicInteger(ticks)));
				else
					list.add(new Entry(0, new AtomicInteger(0)));
			}
			cooldowns.put(item, list.toArray(new Entry[2]));
		}
		else
			cooldowns.get(item)[idx].set(ticks, ticks);
	}
	
	public int getCooldown(Item item, int idx)
	{
		if(cooldowns.containsKey(item) && idx < cooldowns.get(item).length)
			return cooldowns.get(item)[idx].get();
		else
			return 0;
	}
	
	public boolean isUsable(Item item, int idx)
	{
		if(cooldowns.containsKey(item) && idx < cooldowns.get(item).length)
			return cooldowns.get(item)[idx].get() - (owner.getWorld().isClient ? 1 : 0) <= 0;
		else
			return true;
	}
	
	public float getCooldownPercent(Item item, int idx)
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
