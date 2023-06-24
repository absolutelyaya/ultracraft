package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.advancement.CoinPunchCriterion;
import net.minecraft.advancement.criterion.Criteria;

public class CriteriaRegistry extends Criteria
{
	public static CoinPunchCriterion COIN_PUNCH = register(new CoinPunchCriterion());
	
	public static void register()
	{
	
	}
}
