package absolutelyaya.ultracraft.registry;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

public class GameruleRegistry
{
	public static final GameRules.Key<GameRules.BooleanRule> ALLOW_PROJ_BOOST_THROWABLE =
			GameRuleRegistry.register("ultra-projBoostThrowable", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
	public static final GameRules.Key<EnumRule<Option>> HI_VEL_MODE =
			GameRuleRegistry.register("ultra-hiVelMode", GameRules.Category.PLAYER, GameRuleFactory.createEnumRule(Option.FREE,
					(server, rule) -> {
						if(server.isRemote() && rule.get().equals(Option.FORCE_ON))
						{
							server.getPlayerManager().getPlayerList().forEach(p -> {
								if(p.hasPermissionLevel(2))
									p.sendMessage(Text.translatable("message.ultracraft.server.freeze-enable-warning"));
							});
						}
						OnChanged(server, (byte)(10 + rule.get().ordinal()));
					}));
	public static final GameRules.Key<EnumRule<Option>> TIME_STOP =
			GameRuleRegistry.register("ultra-timeStopEffect", GameRules.Category.PLAYER,
				GameRuleFactory.createEnumRule(Option.FORCE_OFF, new Option[] { Option.FORCE_ON, Option.FORCE_OFF },
					(server, rule) -> OnChanged(server, (byte)(20 + rule.get().ordinal()))));
	public static final GameRules.Key<GameRules.BooleanRule> DISABLE_HANDSWAP =
			GameRuleRegistry.register("ultra-disableHandSwap", GameRules.Category.PLAYER,
				GameRuleFactory.createBooleanRule(false,
					(server, rule) -> OnChanged(server, (byte)(30 + (rule.get() ? 1 : 0)))));
	public static final GameRules.Key<GameRules.IntRule> HIVEL_JUMP_BOOST =
			GameRuleRegistry.register("ultra-hivelJumpBoost", GameRules.Category.PLAYER,
					GameRuleFactory.createIntRule(2,
							(server, rule) -> OnChanged(server, (byte)40, rule.get())));
	public static final GameRules.Key<GameRules.BooleanRule> SLAM_STORAGE =
			GameRuleRegistry.register("ultra-allowSlamStorage", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true,
							(server, rule) -> OnChanged(server, (byte) ((byte)50 + (rule.get() ? 1 : 0)))));
	public static final GameRules.Key<GameRules.BooleanRule> HIVEL_FALLDAMAGE =
			GameRuleRegistry.register("ultra-hivelFallDamage", GameRules.Category.PLAYER,
			GameRuleFactory.createBooleanRule(false,
					(server, rule) -> OnChanged(server, (byte) ((byte)60 + (rule.get() ? 1 : 0)))));
	
	public static void OnChanged(MinecraftServer server, byte b)
	{
		server.getPlayerManager().getPlayerList().forEach(p -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(b);
			ServerPlayNetworking.send(p, PacketRegistry.SYNC_RULE, buf);
		});
	}
	
	public static void OnChanged(MinecraftServer server, byte b, int val)
	{
		server.getPlayerManager().getPlayerList().forEach(p -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(b);
			buf.writeInt(val);
			ServerPlayNetworking.send(p, PacketRegistry.SYNC_RULE, buf);
		});
	}
	
	public static void OnChanged(ServerPlayerEntity player, byte b)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(b);
		ServerPlayNetworking.send(player, PacketRegistry.SYNC_RULE, buf);
	}
	
	public static void OnChanged(ServerPlayerEntity player, byte b, int val)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(b);
		buf.writeInt(val);
		ServerPlayNetworking.send(player, PacketRegistry.SYNC_RULE, buf);
	}
	
	public static void SyncAll(ServerPlayerEntity player)
	{
		OnChanged(player, (byte)(10 + player.server.getGameRules().get(HI_VEL_MODE).get().ordinal()));
		OnChanged(player, (byte)(20 + player.server.getGameRules().get(TIME_STOP).get().ordinal()));
		OnChanged(player, (byte)(30 + (player.server.getGameRules().getBoolean(DISABLE_HANDSWAP) ? 1 : 0)));
		OnChanged(player, (byte)40, player.server.getGameRules().getInt(HIVEL_JUMP_BOOST));
		OnChanged(player, (byte)(50 + (player.server.getGameRules().getBoolean(SLAM_STORAGE) ? 1 : 0)));
		OnChanged(player, (byte)(60 + (player.server.getGameRules().getBoolean(HIVEL_FALLDAMAGE) ? 1 : 0)));
	}
	
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
