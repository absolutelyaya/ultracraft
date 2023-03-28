package absolutelyaya.ultracraft.registry;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.world.GameRules;

public class GameruleRegistry
{
	public static final GameRules.Key<GameRules.BooleanRule> ALLOW_PROJ_BOOST_THROWABLE =
			GameRuleRegistry.register("projBoosThrowable", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
	public static final GameRules.Key<EnumRule<Option>> HI_VEL_MODE =
			GameRuleRegistry.register("hiVelMode", GameRules.Category.PLAYER, GameRuleFactory.createEnumRule(Option.FREE));
	public static final GameRules.Key<EnumRule<Option>> TIME_STOP =
			GameRuleRegistry.register("timeStopEffect", GameRules.Category.PLAYER,
					GameRuleFactory.createEnumRule(Option.FORCE_OFF, new Option[] { Option.FORCE_ON, Option.FORCE_OFF}));
	
	public static void register()
	{
	
	}
	
	public enum Option
	{
		FORCE_ON,
		FORCE_OFF,
		FREE
	}
}
