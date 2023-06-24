package absolutelyaya.ultracraft.advancement;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class Advancements implements Consumer<Consumer<Advancement>>
{
	@Override
	public void accept(Consumer<Advancement> consumer)
	{
		Advancement root = Advancement.Builder.create().display(ItemRegistry.BLUE_SKULL, Text.translatable("advancements.ultracraft.root.title"), Text.translatable("advancements.ultracraft.root.description"), new Identifier("textures/block/stone_bricks.png"), AdvancementFrame.TASK, false, false, false).criterion("tick", TickCriterion.Conditions.createTick()).build(consumer, Ultracraft.MOD_ID + "/root");
		Advancement coinpunch_easy = Advancement.Builder.create().parent(root).display(ItemRegistry.COIN, Text.translatable("advancements.ultracraft.coinpunch-easy.title"), Text.translatable("advancements.ultracraft.coinpunch-easy.description"), null, AdvancementFrame.TASK, true, true, false).criterion("punch", CoinPunchCriterion.Conditions.create(5)).build(consumer, Ultracraft.MOD_ID + "/coinpunch_easy");
		Advancement coinpunch_medium = Advancement.Builder.create().parent(coinpunch_easy).display(ItemRegistry.COIN, Text.translatable("advancements.ultracraft.coinpunch-medium.title"), Text.translatable("advancements.ultracraft.coinpunch-medium.description"), null, AdvancementFrame.GOAL, true, true, false).criterion("punch", CoinPunchCriterion.Conditions.create(25)).build(consumer, Ultracraft.MOD_ID + "/coinpunch_medium");
		Advancement.Builder.create().parent(coinpunch_medium).display(ItemRegistry.COIN, Text.translatable("advancements.ultracraft.coinpunch-hard.title"), Text.translatable("advancements.ultracraft.coinpunch-hard.description"), null, AdvancementFrame.CHALLENGE, true, true, false).criterion("punch", CoinPunchCriterion.Conditions.create(50)).build(consumer, Ultracraft.MOD_ID + "/coinpunch_hard");
	}
}
