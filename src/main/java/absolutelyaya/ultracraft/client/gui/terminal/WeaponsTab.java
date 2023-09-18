package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Sprite;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
import absolutelyaya.ultracraft.components.IProgressionComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class WeaponsTab extends Tab
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/weapon_icons.png");
	static final Identifier[] REVOLVERS = new Identifier[]{
			new Identifier(Ultracraft.MOD_ID, "pierce_revolver"),
			new Identifier(Ultracraft.MOD_ID, "marksman_revolver"),
			new Identifier(Ultracraft.MOD_ID, "sharpshooter_revolver")
	};
	static final Identifier[] SHOTGUNS = new Identifier[]{
			new Identifier(Ultracraft.MOD_ID, "core_shotgun"),
			new Identifier(Ultracraft.MOD_ID, "pump_shotgun")
	};
	
	Button returnButton = new Button(Button.RETURN_LABEL,
			new Vector2i(-6 - textRenderer.getWidth(Text.translatable(Button.RETURN_LABEL).getString()), 96 - textRenderer.fontHeight),
			"mainmenu", 0, false);
	Button craftButton = new Button("terminal.craft", new Vector2i(150, 96 - textRenderer.fontHeight), "craft", 0, true);
	final String[] weaponCategories = new String[] { "revolver", "shotgun", "nailgun", "railcannon", "rocket_launcher" };
	Button blueWeapon = new Button(new Sprite(TEXTURE, new Vector2i(0, 0), 0.001f, new Vector2i(48, 32), new Vector2i(0, 0), new Vector2i(192, 192)),
			new Vector2i(50, 23), "blue", 0, true);
	Button greenWeapon = new Button(new Sprite(TEXTURE, new Vector2i(0, 0), 0.001f, new Vector2i(48, 32), new Vector2i(48, 0), new Vector2i(192, 192)),
			new Vector2i(26, 58), "green", 0, true);
	Button redWeapon = new Button(new Sprite(TEXTURE, new Vector2i(0, 0), 0.001f, new Vector2i(48, 32), new Vector2i(96, 0), new Vector2i(192, 192)),
			new Vector2i(75, 58), "red", 0, true);
	List<Button> weaponCategoryButtons = new ArrayList<>();
	int selectedCategory = -1;
	Identifier selectedWeapon;
	
	public WeaponsTab()
	{
		super(WEAPONS_ID);
	}
	
	@Override
	public void init(TerminalBlockEntity terminal)
	{
		super.init(terminal);
		for (int i = 0; i < weaponCategories.length; i++)
		{
			String t = "terminal.weapon." + weaponCategories[i];
			Button b = new Button(t, new Vector2i(-6 - textRenderer.getWidth(Text.translatable("terminal.weapon." + weaponCategories[i]).getString()),
					2 + (i * (textRenderer.fontHeight + 5))), "select", i, false);
			if(i >= 0 && !unlockedAny(weaponCategories[i]))
				b.setClickable(false).setColor(0xff888888);
			buttons.add(b);
			weaponCategoryButtons.add(b);
		}
		buttons.add(craftButton);
		buttons.add(blueWeapon);
		buttons.add(greenWeapon);
		buttons.add(redWeapon);
		
		onButtonClicked("select", 0);
	}
	
	boolean unlockedAny(String type)
	{
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(MinecraftClient.getInstance().player);
		return switch (type)
		{
			case "revolver" -> progression.isUnlocked(REVOLVERS[0]) || progression.isUnlocked(REVOLVERS[1]) || progression.isUnlocked(REVOLVERS[2]);
			case "shotgun" -> progression.isUnlocked(SHOTGUNS[0]) || progression.isUnlocked(SHOTGUNS[1]);
			default -> false;
		};
	}
	
	@Override
	public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "terminal.weapons", returnButton);
		GUI.drawBox(buffers, matrices, -1, 0, 1, 100, 0xffffffff, 0.0005f);
		GUI.drawBox(buffers, matrices, 101, 0, 1, 100, 0xffffffff, 0.0005f);
		drawButtons(matrices, terminal, buffers);
		//the two arrows
		GUI.drawSprite(buffers, matrices, TEXTURE, new Vector2i(11, 36), 0.002f, new Vector2i(0, 139), new Vector2i(13, 20), new Vector2i(192, 192), terminal.getTextColor());
		GUI.drawSprite(buffers, matrices, TEXTURE, new Vector2i(76, 36), 0.002f, new Vector2i(14, 139), new Vector2i(13, 20), new Vector2i(192, 192), terminal.getTextColor());
	}
	
	Identifier[] getWeaponListForType(int idx)
	{
		if(idx < 0 || idx > weaponCategories.length)
			return new Identifier[]{};
		return switch(weaponCategories[selectedCategory])
		{
			case "revolver" -> REVOLVERS;
			case "shotgun" -> SHOTGUNS;
			default -> new Identifier[]{};
		};
	}
	
	@Override
	public boolean onButtonClicked(String action, int value)
	{
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(MinecraftClient.getInstance().player);
		if(action.equals("select"))
		{
			if(selectedCategory >= 0)
				weaponCategoryButtons.get(selectedCategory).getPos().add(5, 0);
			weaponCategoryButtons.get(value).getPos().sub(5, 0);
			selectedCategory = value;
			
			blueWeapon.getSprite().setUv(new Vector2i(0, 32 * value));
			greenWeapon.getSprite().setUv(new Vector2i(48, 32 * value));
			redWeapon.getSprite().setUv(new Vector2i(96, 32 * value));
			
			Identifier[] selectedWeaponTypeIds = getWeaponListForType(value);
			if(selectedWeaponTypeIds.length < 2 || !progression.isUnlocked(selectedWeaponTypeIds[1]))
			{
				greenWeapon.getSprite().setUv(new Vector2i(144, 160));
				greenWeapon.setClickable(false).setColor(0xff888888);
			}
			if(selectedWeaponTypeIds.length < 3 || !progression.isUnlocked(selectedWeaponTypeIds[2]))
			{
				redWeapon.getSprite().setUv(new Vector2i(144, 160));
				redWeapon.setClickable(false).setColor(0xff888888);
			}
			return true;
		}
		
		Identifier[] selectedWeaponTypeIds = getWeaponListForType(selectedCategory);
		switch(action)
		{
			case "blue" -> {
				if(selectedWeaponTypeIds.length < 1)
					return true;
				selectedWeapon = selectedWeaponTypeIds[0];
				return true;
			}
			case "green" -> {
				if(selectedWeaponTypeIds.length < 2)
					return true;
				selectedWeapon = selectedWeaponTypeIds[1];
				return true;
			}
			case "red" -> {
				if(selectedWeaponTypeIds.length < 3)
					return true;
				selectedWeapon = selectedWeaponTypeIds[2];
				return true;
			}
			case "craft" -> {
				//TODO: check if Player has Recipe Items
				//TODO: add recipe System (bonus points if data oriented / datapack support)
				//TODO: double check if weapon is unlocked
				//TODO: unlock follow-up weapons (blue -> green & red)
				//TODO: remove items from crafter
				//TODO: obtain item
				
				//TODO: if weapon has been obtained and no Variant of this Weapon is held, give it as an item
				return true;
			}
			default -> {
				return super.onButtonClicked(action, value);
			}
		}
	}
	
	@Override
	public Vector2f getSizeOverride()
	{
		return new Vector2f(300, 100);
	}
}
