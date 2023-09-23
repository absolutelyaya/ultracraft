package absolutelyaya.ultracraft.compat;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.item.TerminalItem;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class REIClientPlugin implements me.shedaniel.rei.api.client.plugins.REIClientPlugin
{
	@Override
	public void registerBasicEntryFiltering(BasicFilteringRule<?> rule)
	{
		rule.hide(List.of(
				EntryStacks.of(ItemRegistry.COIN.getDefaultStack()),
				EntryStacks.of(ItemRegistry.COIN),
				EntryStacks.of(ItemRegistry.FAKE_BANNER),
				EntryStacks.of(ItemRegistry.FAKE_SHIELD),
				EntryStacks.of(ItemRegistry.KILLERFISH),
				EntryStacks.of(ItemRegistry.BLOOD_RAY),
				EntryStacks.of(ItemRegistry.EJECTED_CORE),
				EntryStacks.of(ItemRegistry.NAIL)));
	}
	
	@Override
	public void registerCollapsibleEntries(CollapsibleEntryRegistry registry)
	{
		List<EntryStack<?>> entries = new ArrayList<>();
		for (TerminalBlockEntity.Base base : TerminalBlockEntity.Base.values())
			entries.add(EntryStacks.of(TerminalItem.getStack(base)));
		registry.group(new Identifier(Ultracraft.MOD_ID, "terminal-variants"), Text.translatable("rei-group.ultracraft.terminals"), entries);
	}
}
