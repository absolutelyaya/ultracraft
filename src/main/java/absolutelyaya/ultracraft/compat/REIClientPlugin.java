package absolutelyaya.ultracraft.compat;

import absolutelyaya.ultracraft.registry.ItemRegistry;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.List;

public class REIClientPlugin implements me.shedaniel.rei.api.client.plugins.REIClientPlugin
{
	@Override
	public void registerBasicEntryFiltering(BasicFilteringRule<?> rule)
	{
		me.shedaniel.rei.api.client.plugins.REIClientPlugin.super.registerBasicEntryFiltering(rule);
		rule.hide(List.of(
				EntryStacks.of(ItemRegistry.COIN.getDefaultStack()),
				EntryStacks.of(ItemRegistry.COIN),
				EntryStacks.of(ItemRegistry.FAKE_BANNER),
				EntryStacks.of(ItemRegistry.FAKE_SHIELD),
				EntryStacks.of(ItemRegistry.KILLERFISH),
				EntryStacks.of(ItemRegistry.BLOOD_RAY),
				EntryStacks.of(ItemRegistry.EJECTED_CORE)));
	}
}
