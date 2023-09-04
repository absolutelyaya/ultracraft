package absolutelyaya.ultracraft.api;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class TerminalCodeRegistry
{
	static final HashMap<String, Result> codes = new HashMap<>();
	
	/**
	 * Register a Secret Code that can be entered on Terminals Main Menu to run a special Action.<br>
	 * These Actions are only run on the Client!
	 * @param code The Series of Inputs the user has to make. For simplicity, I'd recommend only using lower-case chars.
	 * @param action The Action that will be run after the code has been entered.
	 */
	public static void registerCode(String code, Consumer<TerminalBlockEntity> action)
	{
		codes.put(code, new Result(action, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1f));
	}
	
	/**
	 * Register a Secret Code that can be entered on Terminals Main Menu to run a special Action.<br>
	 * These Actions are only run on the Client!
	 * @param code The Series of Inputs the user has to make. For simplicity, I'd recommend only using lower-case chars.
	 * @param action The Action that will be run after the code has been entered.
	 */
	public static void registerCode(String code, Result action)
	{
		codes.put(code, action);
	}
	
	@ApiStatus.Internal
	public static boolean trigger(String input, TerminalBlockEntity terminal)
	{
		for (String code : codes.keySet())
		{
			if(code.equals(input))
			{
				Result r = codes.get(input);
				r.action.accept(terminal);
				MinecraftClient.getInstance().player.playSound(r.sound, SoundCategory.PLAYERS, 1f, r.pitch);
				return true;
			}
		}
		return false;
	}
	
	public record Result(Consumer<TerminalBlockEntity> action, SoundEvent sound, float pitch) {}
}
