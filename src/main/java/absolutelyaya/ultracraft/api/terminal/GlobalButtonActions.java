package absolutelyaya.ultracraft.api.terminal;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.DefaultTabs;
import absolutelyaya.ultracraft.client.gui.terminal.EditMainMenuTab;

import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;

public class GlobalButtonActions
{
	static final HashMap<String, BiConsumer<TerminalBlockEntity, Integer>> actions = new HashMap<>();
	
	/**
	 * Register a new Global Button Action. Buttons in any tab will be able to trigger it,<br>
	 * and it will be listed as an Option in the Main Menu Customization Screen.
	 * @param id The ID of the action. This is the string that the button uses to reference an action.
	 * @param action The Action; this is run when the button is clicked.<br>
	 *               The arguments are the Terminal entity from which the action was triggered and an int value that can be defined by the button.<br>
	 *               This int value is used to, for example, communicate which base pattern option was clicked, without needing 16 separate Buttons.
	 */
	public static void registerAction(String id, BiConsumer<TerminalBlockEntity, Integer> action)
	{
		actions.put(id, action);
	}
	
	public static Set<String> getAllActions()
	{
		return actions.keySet();
	}
	
	public static boolean runAction(TerminalBlockEntity terminal, String actionID, int value)
	{
		if(actions.containsKey(actionID))
		{
			actions.get(actionID).accept(terminal, value);
			return true;
		}
		else
			return false;
	}
	
	static {
		registerAction("customize", (t, v) -> t.setTab(new DefaultTabs.Customization()));
		registerAction("bestiary", (t, v) -> t.setTab(new Tab(Tab.COMING_SOON_ID)));
		registerAction("weapons", (t, v) -> t.setTab(new Tab(Tab.WEAPONS_ID)));
		registerAction("mainmenu", (t, v) -> t.setTab(new DefaultTabs.MainMenu()));
		registerAction("edit-screensaver", (t, v) -> t.setTab(new DefaultTabs.ScreenSaverEditor()));
		registerAction("edit-base", (t, v) -> t.setTab(new DefaultTabs.BaseSelection()));
		registerAction("graffiti", (t, v) -> t.setTab(new DefaultTabs.Graffiti()));
		registerAction("edit-mainmenu", (t, v) -> t.setTab(new EditMainMenuTab()));
		
		registerAction("set-base", (t, v) -> t.setBase(TerminalBlockEntity.Base.values()[v]));
		registerAction("force-screensaver", (t, v) -> t.setInactivity(60f));
	}
}
