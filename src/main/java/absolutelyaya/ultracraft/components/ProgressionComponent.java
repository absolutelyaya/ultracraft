package absolutelyaya.ultracraft.components;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.gui.terminal.WeaponsTab;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProgressionComponent implements IProgressionComponent, AutoSyncedComponent
{
	PlayerEntity provider;
	List<Identifier> unlocked = new ArrayList<>();
	List<Identifier> owned = new ArrayList<>();
	
	public ProgressionComponent(PlayerEntity provider)
	{
		this.provider = provider;
		unlocked.add(new Identifier(Ultracraft.MOD_ID, "pierce_revolver"));
	}
	
	@Override
	public void lock(Identifier id)
	{
		unlocked.remove(id);
	}
	
	@Override
	public void unlock(Identifier id)
	{
		if(!unlocked.contains(id))
			unlocked.add(id);
	}
	
	@Override
	public boolean isUnlocked(Identifier id)
	{
		if(isOwned(id) && !unlocked.contains(id))
			unlocked.add(id);
		return unlocked.contains(id);
	}
	
	@Override
	public List<Identifier> getUnlockedList()
	{
		return unlocked;
	}
	
	@Override
	public void disown(Identifier id)
	{
		owned.remove(id);
	}
	
	@Override
	public void obtain(Identifier id)
	{
		if(!owned.contains(id))
			owned.add(id);
	}
	
	@Override
	public boolean isOwned(Identifier id)
	{
		return owned.contains(id);
	}
	
	@Override
	public List<Identifier> getOwnedList()
	{
		return owned;
	}
	
	@Override
	public void reset()
	{
		unlocked = new ArrayList<>();
		owned = new ArrayList<>();
		unlocked.add(new Identifier(Ultracraft.MOD_ID, "pierce_revolver"));
	}
	
	public void sync()
	{
		UltraComponents.PROGRESSION.sync(provider);
	}
	
	@Override
	public void readFromNbt(@NotNull NbtCompound tag)
	{
		this.unlocked.clear();
		this.owned.clear();
		NbtList list = tag.getList("unlocks", NbtElement.STRING_TYPE);
		list.forEach(i -> unlocked.add(Identifier.tryParse(i.asString())));
		list = tag.getList("owned", NbtElement.STRING_TYPE);
		list.forEach(i -> unlocked.add(Identifier.tryParse(i.asString())));
	}
	
	@Override
	public void writeToNbt(@NotNull NbtCompound tag)
	{
		NbtList unlocks = new NbtList();
		for (Identifier id : this.unlocked)
			unlocks.add(NbtString.of(id.toString()));
		tag.put("unlocks", unlocks);
		NbtList owned = new NbtList();
		for (Identifier id : this.owned)
			owned.add(NbtString.of(id.toString()));
		tag.put("owned", owned);
	}
	
	@Override
	public void applySyncPacket(PacketByteBuf buf)
	{
		IProgressionComponent.super.applySyncPacket(buf);
		if(provider instanceof WingedPlayerEntity winged && winged.getFocusedTerminal() != null && winged.getFocusedTerminal().getTab() instanceof WeaponsTab weaponsTab)
			weaponsTab.refreshTab();
	}
}
