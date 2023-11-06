package absolutelyaya.ultracraft.components.level;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.world.WorldProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UltraLevelComponent implements IUltraLevelComponent
{
	final WorldProperties provider;
	
	boolean hivelWhitelistActive, graffitiWhitelistActive;
	Map<UUID, String> hivelWhitelist = new HashMap<>(), graffitiWhitelist = new HashMap<>();
	
	public UltraLevelComponent(WorldProperties properties)
	{
		provider = properties;
	}
	
	@Override
	public boolean isHivelWhitelistActive()
	{
		return hivelWhitelistActive;
	}
	
	@Override
	public void setHivelWhitelistActive(boolean v)
	{
		hivelWhitelistActive = v;
	}
	
	@Override
	public Map<UUID, String> getHivelWhitelist()
	{
		return hivelWhitelist;
	}
	
	@Override
	public boolean isPlayerAllowedToHivel(PlayerEntity player)
	{
		return !hivelWhitelistActive || hivelWhitelist.containsKey(player.getUuid());
	}
	
	@Override
	public boolean isGraffitiWhitelistActive()
	{
		return graffitiWhitelistActive;
	}
	
	@Override
	public void setGraffitiWhitelistActive(boolean v)
	{
		graffitiWhitelistActive = v;
	}
	
	@Override
	public Map<UUID, String> getGraffitiWhitelist()
	{
		return graffitiWhitelist;
	}
	
	@Override
	public boolean isPlayerAllowedToGraffiti(PlayerEntity player)
	{
		return !graffitiWhitelistActive || graffitiWhitelist.containsKey(player.getUuid());
	}
	
	@Override
	public void readFromNbt(NbtCompound tag)
	{
		if(!tag.contains("whitelists", NbtElement.COMPOUND_TYPE))
			return;
		NbtCompound whitelists = tag.getCompound("whitelists");
		if(whitelists.contains("hivel", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound hivel = whitelists.getCompound("hivel");
			if(hivel.contains("active", NbtElement.BYTE_TYPE))
				hivelWhitelistActive = hivel.getBoolean("active");
			if(hivel.contains("entries", NbtElement.LIST_TYPE))
			{
				hivel.getList("entries", NbtElement.STRING_TYPE).forEach(i -> {
					String[] segments = i.asString().split("@");
					if(segments.length < 2)
						return;
					UUID id = UUID.fromString(segments[0]);
					hivelWhitelist.put(id, segments[1]);
				});
			}
		}
		if(whitelists.contains("graffiti", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound graffiti = whitelists.getCompound("graffiti");
			if(graffiti.contains("active", NbtElement.BYTE_TYPE))
				graffitiWhitelistActive = graffiti.getBoolean("active");
			if(graffiti.contains("entries", NbtElement.LIST_TYPE))
			{
				graffiti.getList("entries", NbtElement.STRING_TYPE).forEach(i -> {
					String[] segments = i.asString().split("@");
					if(segments.length < 2)
						return;
					UUID id = UUID.fromString(segments[0]);
					graffitiWhitelist.put(id, segments[1]);
				});
			}
		}
	}
	
	@Override
	public void writeToNbt(NbtCompound tag)
	{
		NbtCompound whitelists = new NbtCompound();
		
		NbtCompound hivel = new NbtCompound();
		hivel.putBoolean("active", hivelWhitelistActive);
		NbtList hivelList = new NbtList();
		for (Map.Entry<UUID, String> entry : hivelWhitelist.entrySet())
		{
			hivelList.add(NbtString.of(entry.getKey() + "@" + entry.getValue()));
		}
		hivel.put("entries", hivelList);
		whitelists.put("hivel", hivel);
		
		NbtCompound graffiti = new NbtCompound();
		graffiti.putBoolean("active", graffitiWhitelistActive);
		NbtList graffitiList = new NbtList();
		for (Map.Entry<UUID, String> entry : graffitiWhitelist.entrySet())
		{
			graffitiList.add(NbtString.of(entry.getKey() + "@" + entry.getValue()));
		}
		graffiti.put("entries", graffitiList);
		whitelists.put("graffiti", graffiti);
		
		tag.put("whitelists", whitelists);
	}
}
