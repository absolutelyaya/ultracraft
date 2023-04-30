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
	
	PlayerEntity owner;
	HashMap<Item, AtomicInteger[]> cooldowns = new HashMap<>();
	
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
			for (int j = 0; j < cooldowns.get(key).length; j++)
			{
				AtomicInteger cd = cooldowns.get(key)[i];
				if(cd.get() > 0)
					cd.getAndDecrement();
				if(cd.get() <= 0)
					remove.add(key);
			}
		}
		remove.forEach(i -> cooldowns.remove(i));
	}
	
	public void setCooldown(Item item, int ticks, int idx)
	{
		if(!owner.world.isClient)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeItemStack(item.getDefaultStack());
			buf.writeInt(ticks);
			buf.writeInt(idx);
			ServerPlayNetworking.send((ServerPlayerEntity)owner, PacketRegistry.SET_GUNCD_PACKET_ID, buf);
		}
		if(!cooldowns.containsKey(item))
		{
			List<AtomicInteger> list = new ArrayList<>();
			for (int i = 0; i < 2; i++)
			{
				if(i == idx)
					list.add(new AtomicInteger(ticks));
				else
					list.add(new AtomicInteger(0));
			}
			cooldowns.put(item, list.toArray(new AtomicInteger[2]));
		}
		else
			cooldowns.get(item)[idx].set(ticks);
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
			return cooldowns.get(item)[idx].get() <= 0;
		else
			return true;
	}
}
