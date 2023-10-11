package absolutelyaya.ultracraft.command;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.components.level.IUltraLevelComponent;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.GameProfileArgumentType.gameProfile;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WhitelistCommand
{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("ultrawhitelist").requires(source -> source.hasPermissionLevel(2)).requires(ServerCommandSource::isExecutedByPlayer)
									.then(argument("list", string()).suggests(WhitelistCommand::whitelistProvider).executes(WhitelistCommand::executeWhitelistCheck)
												  .then(argument("state", string()).suggests(WhitelistCommand::whitelistToggleProvider).executes(WhitelistCommand::executeWhitelistToggle))
												  .then(literal("list").executes(WhitelistCommand::executeWhitelistList))
												  .then(literal("add").then(argument("target", gameProfile()).executes(WhitelistCommand::executeWhitelistAdd)))
												  .then(literal("remove").then(argument("target", gameProfile()).suggests(WhitelistCommand::whitelistEntryProvider).executes(WhitelistCommand::executeWhitelistRemove)))));
	}
	
	static CompletableFuture<Suggestions> whitelistProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		return builder.suggest("hivel").suggest("graffiti").buildFuture();
	}
	
	private static int executeWhitelistCheck(CommandContext<ServerCommandSource> context)
	{
		IUltraLevelComponent global = UltraComponents.GLOBAL.get(context.getSource().getWorld().getLevelProperties());
		String list = context.getArgument("list", String.class);
		boolean active = switch (list)
		{
			case "hivel" -> global.isHivelWhitelistActive();
			case "graffiti" -> global.isGraffitiWhitelistActive();
			default -> false;
		};
		context.getSource().sendMessage(Text.translatable("command.ultracraft.whitelist.check", list,
				Text.translatable("command.ultracraft.whitelist." + (active ? "active" : "inactive"))));
		
		return Command.SINGLE_SUCCESS;
	}
	
	static CompletableFuture<Suggestions> whitelistToggleProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		IUltraLevelComponent global = UltraComponents.GLOBAL.get(context.getSource().getWorld().getLevelProperties());
		builder.suggest(isWhitelistActive(global, context.getArgument("list", String.class)) ? "disable" : "enable");
		return builder.buildFuture();
	}
	
	static boolean isWhitelistActive(IUltraLevelComponent global, String list)
	{
		return switch (list)
		{
			case "hivel" -> global.isHivelWhitelistActive();
			case "graffiti" -> global.isGraffitiWhitelistActive();
			default -> false;
		};
	}
	
	static Map<UUID, String> getWhitelist(IUltraLevelComponent global, String list)
	{
		return switch(list)
		{
			case "hivel" -> global.getHivelWhitelist();
			case "graffiti" -> global.getGraffitiWhitelist();
			default -> null;
		};
	}
	
	private static int executeWhitelistToggle(CommandContext<ServerCommandSource> context)
	{
		IUltraLevelComponent global = UltraComponents.GLOBAL.get(context.getSource().getWorld().getLevelProperties());
		String list = context.getArgument("list", String.class);
		switch (list)
		{
			case "hivel" -> global.setHivelWhitelistActive(!isWhitelistActive(global, list));
			case "graffiti" -> global.setGraffitiWhitelistActive(!isWhitelistActive(global, list));
		}
		context.getSource().sendMessage(Text.translatable("command.ultracraft.whitelist.toggle", list,
				Text.translatable("command.ultracraft.whitelist." + (isWhitelistActive(global, list) ? "active" : "inactive"))));
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeWhitelistList(CommandContext<ServerCommandSource> context)
	{
		IUltraLevelComponent global = UltraComponents.GLOBAL.get(context.getSource().getWorld().getLevelProperties());
		String list = context.getArgument("list", String.class);
		Map<UUID, String> whitelist = getWhitelist(global, list);
		if(whitelist == null)
		{
			context.getSource().sendError(Text.translatable("command.ultracraft.whitelist.invalid"));
			return Command.SINGLE_SUCCESS;
		}
		StringBuilder builder = new StringBuilder(Text.translatable("command.ultracraft.whitelist.list-prefix", list).getString());
		String[] entries = whitelist.values().toArray(new String[]{});
		for (int i = 0; i < entries.length; i++)
		{
			builder.append(i % 2 == 0 ? "§6" : "§e");
			builder.append(entries[i]);
			if(i < entries.length - 1)
				builder.append(", ");
		}
		context.getSource().sendMessage(Text.of(builder.toString()));
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeWhitelistAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		IUltraLevelComponent global = UltraComponents.GLOBAL.get(context.getSource().getWorld().getLevelProperties());
		Collection<GameProfile> target = GameProfileArgumentType.getProfileArgument(context, "target");
		String list = context.getArgument("list", String.class);
		Map<UUID, String> whitelist = getWhitelist(global, list);
		for (GameProfile profile : target)
		{
			whitelist.put(profile.getId(), profile.getName());
			context.getSource().sendMessage(Text.translatable("command.ultracraft.whitelist.add", profile.getName(), list));
		}
		return Command.SINGLE_SUCCESS;
	}
	
	static CompletableFuture<Suggestions> whitelistEntryProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		IUltraLevelComponent global = UltraComponents.GLOBAL.get(context.getSource().getWorld().getLevelProperties());
		String list = context.getArgument("list", String.class);
		Map<UUID, String> whitelist = getWhitelist(global, list);
		for (String name : whitelist.values())
			builder.suggest(name);
		return builder.buildFuture();
	}
	
	private static int executeWhitelistRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		IUltraLevelComponent global = UltraComponents.GLOBAL.get(context.getSource().getWorld().getLevelProperties());
		Collection<GameProfile> target = GameProfileArgumentType.getProfileArgument(context, "target");
		String list = context.getArgument("list", String.class);
		Map<UUID, String> whitelist = getWhitelist(global, list);
		for (GameProfile profile : target)
		{
			whitelist.remove(profile.getId());
			context.getSource().sendMessage(Text.translatable("command.ultracraft.whitelist.remove", profile.getName(), list));
		}
		return Command.SINGLE_SUCCESS;
	}
}
