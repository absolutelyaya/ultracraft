package absolutelyaya.ultracraft.command;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.components.player.IProgressionComponent;
import absolutelyaya.ultracraft.entity.machine.DestinyBondSwordsmachineEntity;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.*;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.function.TriFunction;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands
{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("ultracraft")
			.then(literal("info").requires(ServerCommandSource::isExecutedByPlayer).executes(Commands::executeInfo))
			.then(literal("config").requires(ServerCommandSource::isExecutedByPlayer).requires(source -> source.hasPermissionLevel(2)).executes(Commands::executeConfig))
			.then(literal("block").requires(ServerCommandSource::isExecutedByPlayer).then(argument("target", player()).executes(Commands::executeBlock)))
			.then(literal("unblock").requires(ServerCommandSource::isExecutedByPlayer).then(argument("target", player()).executes(Commands::executeUnblock)))
			.then(literal("time").requires(source -> source.hasPermissionLevel(2))
				.then(literal("freeze").then(argument("ticks", integer(1)).executes(Commands::executeFreeze)))
				.then(literal("unfreeze").executes(Commands::executeUnfreeze)))
			.then(literal("debug").requires(source -> source.hasPermissionLevel(2))
				.then(literal("ricoshot_warn").then(argument("pos", Vec3ArgumentType.vec3()).executes(Commands::executeDebugRicoshotWarn))))
			.then(literal("progression").requires(source -> source.hasPermissionLevel(2))
				.then(argument("list", string()).suggests(Commands::progressionListTypeProvider)
					.then(literal("list").then(argument("target", player()).executes(Commands::executeProgressionList)))
					.then(literal("grant").then(argument("target", player()).then(argument("entry", identifier()).executes(Commands::executeProgressionGrant))))
					.then(literal("revoke").then(argument("target", player()).then(argument("entry", identifier()).suggests(Commands::progressionListProvider).executes(Commands::executeProgressionRevoke)))))
				.then(literal("reset").then(argument("target", player()).executes(Commands::executeProgressionReset)))));
		dispatcher.register(literal("ultrasummon").requires(source -> source.hasPermissionLevel(2)).then(argument("type", string()).suggests((context, builder) -> CommandSource.suggestMatching(List.of("\"tundra//agony\""), builder)).then(argument("pos", Vec3ArgumentType.vec3()).then(argument("yaw", DoubleArgumentType.doubleArg()).executes(Commands::executeSpecialSpawn)))));
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
		ServerPlayNetworking.send(context.getSource().getPlayer(), PacketRegistry.OPEN_SERVER_CONFIG_MENU_PACKET_ID, buf);
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
		if(context.getSource().getWorld().getGameRules().get(GameruleRegistry.TIME_STOP).get().equals(GameruleRegistry.Setting.FORCE_OFF))
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
		ServerPlayNetworking.send(context.getSource().getPlayer(), PacketRegistry.RICOCHET_WARNING_PACKET_ID, buf);
		return Command.SINGLE_SUCCESS;
	}
	
	static int executeSpecialSpawn(CommandContext<ServerCommandSource> context)
	{
		Vec3d pos = context.getArgument("pos", PosArgument.class).toAbsolutePos(context.getSource());
		double yaw = context.getArgument("yaw", Double.class);
		
		String type = context.getArgument("type", String.class);
		TriFunction<World, Vec3d, Float, List<Entity>> spawnConsumer = switch(type)
		{
			case "tundra//agony" -> DestinyBondSwordsmachineEntity::spawn;
			default -> {
				context.getSource().sendError(Text.of("Invalid type: '" + type));
				yield null;
			}
		};
		if(spawnConsumer != null)
			spawnConsumer.apply(context.getSource().getWorld(), pos, (float)yaw);
		return Command.SINGLE_SUCCESS;
	}
	
	static CompletableFuture<Suggestions> progressionListTypeProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		return builder.suggest("unlocked").suggest("obtained").buildFuture();
	}
	
	static List<Identifier> getProgressionList(ServerPlayerEntity target, String type)
	{
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(target);
		return switch(type)
		{
			case "unlocked" -> progression.getUnlockedList();
			case "obtained" -> progression.getOwnedList();
			default -> new ArrayList<>();
		};
	}
	
	private static CompletableFuture<Suggestions> progressionListProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		String list = context.getArgument("list", String.class);
		getProgressionList(target, list).forEach(i -> builder.suggest(i.toString()));
		return builder.buildFuture();
	}
	
	private static int executeProgressionList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		String type = context.getArgument("list", String.class);
		StringBuilder builder = new StringBuilder(Text.translatable("command.ultracraft.progression.list-prefix", target.getName(), type).getString());
		List<Identifier> list = getProgressionList(target, type);
		for (int i = 0; i < list.size(); i++)
		{
			builder.append(i % 2 == 0 ? "§6" : "§e");
			builder.append(list.get(i));
			if(i < list.size() - 1)
				builder.append(", ");
		}
		if(list.size() == 0)
			builder.append(Text.translatable("command.ultracraft.progression.list-empty").getString());
		context.getSource().sendMessage(Text.of(builder.toString()));
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeProgressionGrant(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		String type = context.getArgument("list", String.class);
		Identifier entry = IdentifierArgumentType.getIdentifier(context, "entry");
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(target);
		switch(type)
		{
			case "unlocked" -> progression.unlock(entry);
			case "obtained" -> progression.obtain(entry);
			default -> {
				context.getSource().sendError(Text.translatable("command.ultracraft.progression.invalid_list"));
				return Command.SINGLE_SUCCESS;
			}
		}
		progression.sync();
		context.getSource().sendMessage(Text.translatable("command.ultracraft.progression.grant-success", entry, type, target.getName().getString()));
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeProgressionRevoke(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		String type = context.getArgument("list", String.class);
		Identifier entry = IdentifierArgumentType.getIdentifier(context, "entry");
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(target);
		switch(type)
		{
			case "unlocked" -> progression.lock(entry);
			case "obtained" -> progression.disown(entry);
			default -> {
				context.getSource().sendError(Text.translatable("command.ultracraft.progression.invalid_list"));
				return Command.SINGLE_SUCCESS;
			}
		}
		progression.sync();
		context.getSource().sendMessage(Text.translatable("command.ultracraft.progression.revoke-success", entry, type, target.getName().getString()));
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeProgressionReset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(target);
		progression.reset();
		progression.sync();
		context.getSource().sendMessage(Text.translatable("command.ultracraft.progression.reset-success", target.getName()));
		return Command.SINGLE_SUCCESS;
	}
}
