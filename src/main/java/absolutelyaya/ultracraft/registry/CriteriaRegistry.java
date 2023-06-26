package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.advancement.ChargebackCriterion;
import absolutelyaya.ultracraft.advancement.CoinPunchCriterion;
import net.minecraft.advancement.criterion.Criteria;

public class CriteriaRegistry extends Criteria
{
	public static CoinPunchCriterion COIN_PUNCH = register(new CoinPunchCriterion());
	public static ChargebackCriterion CHARGEBACK = register(new ChargebackCriterion());
	
	public static void register()
	{
	
	}
}
