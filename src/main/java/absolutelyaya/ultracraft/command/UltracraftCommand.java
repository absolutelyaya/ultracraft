package absolutelyaya.ultracraft.command;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class UltracraftCommand
{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("ultracraft").requires(ServerCommandSource::isExecutedByPlayer)
			.then(literal("info").executes(UltracraftCommand::executeInfo))
			.then(literal("config").requires(source -> source.hasPermissionLevel(2)).executes(UltracraftCommand::executeConfig))
			.then(literal("block").then(argument("target", player()).executes(UltracraftCommand::executeBlock)))
			.then(literal("unblock").then(argument("target", player()).executes(UltracraftCommand::executeUnblock)))
			.then(literal("time").requires(source -> source.hasPermissionLevel(2))
				.then(literal("freeze").then(argument("ticks", integer(1)).executes(UltracraftCommand::executeFreeze)))
				.then(literal("unfreeze").executes(UltracraftCommand::executeUnfreeze)))
			.then(literal("debug").requires(source -> source.hasPermissionLevel(2))
				.then(literal("ricoshot_warn").then(argument("pos", Vec3ArgumentType.vec3()).executes(UltracraftCommand::executeDebugRicoshotWarn)))));
	}
	
	static int executeInfo(CommandContext<ServerCommandSource> context)
	{
		ServerPlayNetworking.send(context.getSource().getPlayer(), PacketRegistry.WORLD_INFO_PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
		return Command.SINGLE_SUCCESS;
	}
	
	static int executeConfig(CommandContext<ServerCommandSource> context)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeNbt(context.getSource().getWorld().getGameRules().toNbt());
		ServerPlayNetworking.send(context.getSource().getPlayer(), PacketRegistry.OPEN_SERVER_CONFIG_MENU, buf);
		return Command.SINGLE_SUCCESS;
	}
	
	static int executeBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		if(context.getSource().getPlayer().equals(target))
		{
			context.getSource().sendFeedback(() -> Text.translatable("command.ultracraft.block.self"), false);
			return Command.SINGLE_SUCCESS;
		}
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeUuid(target.getUuid());
		ServerPlayNetworking.send(context.getSource().getPlayer(), PacketRegistry.BLOCK_PLAYER_PACKET_ID, buf);
		return Command.SINGLE_SUCCESS;
	}
	
	static int executeUnblock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		if(context.getSource().getPlayer().equals(target))
		{
			context.getSource().sendFeedback(() -> Text.translatable("command.ultracraft.block.self"), false);
			return Command.SINGLE_SUCCESS;
		}
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeUuid(target.getUuid());
		ServerPlayNetworking.send(context.getSource().getPlayer(), PacketRegistry.UNBLOCK_PLAYER_PACKET_ID, buf);
		return Command.SINGLE_SUCCESS;
	}
	
	static int executeFreeze(CommandContext<ServerCommandSource> context)
	{
		int ticks = context.getArgument("ticks", Integer.class);
		String senderName = context.getSource().getPlayer().getName().getString();
		if(context.getSource().getWorld().getGameRules().get(GameruleRegistry.TIME_STOP).get().equals(GameruleRegistry.Option.FORCE_OFF))
			context.getSource().sendFeedback(() -> Text.translatable("command.ultracraft.time-freeze.fail"), false);
		else
		{
			Ultracraft.freeze(context.getSource().getWorld(), ticks);
			context.getSource().sendFeedback(() -> Text.translatable("command.ultracraft.time-freeze.success", senderName, ticks), true);
		}
		return Command.SINGLE_SUCCESS;
	}
	
	static int executeUnfreeze(CommandContext<ServerCommandSource> context)
	{
		String senderName = context.getSource().getPlayer().getName().getString();
		if(Ultracraft.isTimeFrozen())
		{
			Ultracraft.cancelFreeze(context.getSource().getWorld());
			context.getSource().sendFeedback(() -> Text.translatable("command.ultracraft.time-unfreeze.success", senderName), true);
		}
		else
			context.getSource().sendFeedback(() -> Text.translatable("command.ultracraft.time-unfreeze.fail"), false);
		return Command.SINGLE_SUCCESS;
	}
	
	static int executeDebugRicoshotWarn(CommandContext<ServerCommandSource> context)
	{
		Vec3d v = context.getArgument("pos", PosArgument.class).toAbsolutePos(context.getSource());
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeVector3f(v.toVector3f());
		buf.writeUuid(context.getSource().getPlayer().getUuid());
		ServerPlayNetworking.send(context.getSource().getPlayer(), PacketRegistry.RICOCHET_WARNING, buf);
		return Command.SINGLE_SUCCESS;
	}
}
