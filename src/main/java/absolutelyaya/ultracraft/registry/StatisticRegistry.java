package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class StatisticRegistry
{
	public static final Identifier COLLECT_SOUL_ORB = new Identifier(Ultracraft.MOD_ID, "collect_soul_orb");
	public static final Identifier COLLECT_BLOOD_ORB = new Identifier(Ultracraft.MOD_ID, "collect_blood_orb");
	public static final Identifier DASH = new Identifier(Ultracraft.MOD_ID, "dash");
	public static final Identifier SLIDE = new Identifier(Ultracraft.MOD_ID, "slide");
	public static final Identifier SLAM = new Identifier(Ultracraft.MOD_ID, "slam");
	public static final Identifier COIN_PUNCH = new Identifier(Ultracraft.MOD_ID, "coin_punch");
	public static final Identifier PARRY = new Identifier(Ultracraft.MOD_ID, "parry");
	
	static void register(Identifier id, StatFormatter formatter)
	{
		Registry.register(Registries.CUSTOM_STAT, id.getPath(), id);
		Stats.CUSTOM.getOrCreateStat(id, formatter);
	}
	
	public static void register()
	{
		register(COLLECT_SOUL_ORB, StatFormatter.DEFAULT);
		register(COLLECT_BLOOD_ORB, StatFormatter.DEFAULT);
		register(DASH, StatFormatter.DEFAULT);
		register(SLIDE, StatFormatter.DISTANCE);
		register(SLAM, StatFormatter.DEFAULT);
		register(COIN_PUNCH, StatFormatter.DEFAULT);
		register(PARRY, StatFormatter.DEFAULT);
	}
}
