package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Sprite;
import absolutelyaya.ultracraft.client.gui.terminal.elements.SpriteListElement;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
import absolutelyaya.ultracraft.components.player.IProgressionComponent;
import absolutelyaya.ultracraft.recipe.UltraRecipe;
import absolutelyaya.ultracraft.recipe.UltraRecipeManager;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.util.InventoryUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	static final Identifier[] NAILGUNS = new Identifier[]{
			new Identifier(Ultracraft.MOD_ID, "attractor_nailgun")
	};
	
	Button returnButton = new Button(Button.RETURN_LABEL,
			new Vector2i(-6 - textRenderer.getWidth(Text.translatable(Button.RETURN_LABEL).getString()), 96 - textRenderer.fontHeight),
			"mainmenu", 0, false);
	Button craftButton = new Button("terminal.craft", new Vector2i(150, 96 - textRenderer.fontHeight), "craft", 0, true);
	final String[] weaponCategories = new String[] { "revolver", "shotgun", "nailgun", "railcannon", "rocket_launcher" };
	Button blueWeapon = new Button(new Sprite(TEXTURE, new Vector2i(0, 0), 0.001f, new Vector2i(48, 32), new Vector2i(0, 0), new Vector2i(192, 192)),
			new Vector2i(50, 23), "weapon", 0, true);
	Button greenWeapon = new Button(new Sprite(TEXTURE, new Vector2i(0, 0), 0.001f, new Vector2i(48, 32), new Vector2i(48, 0), new Vector2i(192, 192)),
			new Vector2i(26, 58), "weapon", 1, true);
	Button redWeapon = new Button(new Sprite(TEXTURE, new Vector2i(0, 0), 0.001f, new Vector2i(48, 32), new Vector2i(96, 0), new Vector2i(192, 192)),
			new Vector2i(75, 58), "weapon", 2, true);
	List<Button> weaponCategoryButtons = new ArrayList<>();
	int selectedCategory = -1;
	Identifier selectedWeapon;
	UltraRecipe selectedRecipe;
	SpriteListElement ingredientList = new SpriteListElement(94, 4, 16);
	
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
			boolean locked = i >= 0 && !unlockedAny(weaponCategories[i]);
			Button b = new Button(t,
					new Vector2i(-6 - textRenderer.getWidth(locked ? "???" : Text.translatable("terminal.weapon." + weaponCategories[i]).getString()),
					2 + (i * (textRenderer.fontHeight + 5))), "select", i, false);
			if(locked)
				b.setClickable(false).setColor(0xff888888).setLabel("???");
			buttons.add(b);
			weaponCategoryButtons.add(b);
		}
		buttons.add(craftButton);
		buttons.add(blueWeapon);
		buttons.add(greenWeapon);
		buttons.add(redWeapon);
		ingredientList.setSelectable(false);
		
		onButtonClicked("select", 0);
	}
	
	boolean unlockedAny(String type)
	{
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(MinecraftClient.getInstance().player);
		return switch (type)
		{
			case "revolver" -> progression.isUnlocked(REVOLVERS[0]) || progression.isUnlocked(REVOLVERS[1]) || progression.isUnlocked(REVOLVERS[2]);
			case "shotgun" -> progression.isUnlocked(SHOTGUNS[0]) || progression.isUnlocked(SHOTGUNS[1]);
			case "nailgun" -> progression.isUnlocked(NAILGUNS[0]);
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
		//ingredients
		if(selectedRecipe != null)
			GUI.drawSpriteList(buffers, matrices, 104, 2, ingredientList);
		else
		{
			String t = Text.translatable("terminal.no-recipe").getString();
			matrices.push();
			matrices.translate(0, 1, 0);
			GUI.drawText(buffers, matrices, t, 152 - textRenderer.getWidth(t) / 2, 40, 0.001f, 0xffdd0000);
			matrices.pop();
		}
	}
	
	Identifier[] getWeaponListForType(int idx)
	{
		if(idx < 0 || idx > weaponCategories.length)
			return new Identifier[]{};
		return switch(weaponCategories[idx])
		{
			case "revolver" -> REVOLVERS;
			case "shotgun" -> SHOTGUNS;
			case "nailgun" -> NAILGUNS;
			default -> new Identifier[]{};
		};
	}
	
	@Override
	public boolean onButtonClicked(String action, int value)
	{
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(MinecraftClient.getInstance().player);
		if(action.equals("select"))
		{
			updateTab(value, progression);
			return true;
		}
		
		Identifier[] selectedWeaponTypeIds = getWeaponListForType(selectedCategory);
		switch(action)
		{
			case "weapon" -> {
				return updateSelectedWeapon(selectedWeaponTypeIds, value, progression);
			}
			case "craft" -> {
				if(selectedRecipe == null)
				{
					craftButton.setClickable(false);
					return true;
				}
				PlayerEntity player = MinecraftClient.getInstance().player;
				if(progression.isOwned(selectedWeapon) && !isResultTypeHeld())
				{
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeIdentifier(selectedWeapon);
					ClientPlayNetworking.send(PacketRegistry.TERMINAL_WEAPON_DISPENSE_PACKET_ID, buf);
					player.giveItemStack(Registries.ITEM.get(selectedWeapon).getDefaultStack());
					refreshTab();
					return true;
				}
				if(selectedRecipe.canCraft(player) <= 1)
					return true;
				selectedRecipe.craft(player);
				return true;
			}
			default -> {
				return super.onButtonClicked(action, value);
			}
		}
	}
	
	boolean isResultTypeHeld()
	{
		for (Identifier weapon : getWeaponListForType(selectedCategory))
		{
			PlayerEntity player = MinecraftClient.getInstance().player;
			PlayerInventory inv = player.getInventory();
			Item outputItem = Registries.ITEM.get(weapon);
			if(inv.offHand.get(0).isOf(outputItem))
				return true;
			DefaultedList<ItemStack> invList = inv.main;
			if(outputItem != null && InventoryUtil.containsItem(invList, outputItem, 1))
				return true;
		}
		return false;
	}
	
	public void updateTab(int tab, IProgressionComponent progression)
	{
		if(selectedCategory >= 0)
			weaponCategoryButtons.get(selectedCategory).getPos().add(5, 0);
		weaponCategoryButtons.get(tab).getPos().sub(5, 0);
		selectedCategory = tab;
		
		blueWeapon.getSprite().setUv(new Vector2i(0, 32 * tab));
		greenWeapon.getSprite().setUv(new Vector2i(48, 32 * tab));
		redWeapon.getSprite().setUv(new Vector2i(96, 32 * tab));
		
		Identifier[] selectedWeaponTypeIds = getWeaponListForType(tab);
		updateWeaponButton(blueWeapon, 0, selectedWeaponTypeIds, progression);
		updateWeaponButton(greenWeapon, 1, selectedWeaponTypeIds, progression);
		updateWeaponButton(redWeapon, 2, selectedWeaponTypeIds, progression);
		updateSelectedWeapon(selectedWeaponTypeIds, 0, progression);
	}
	
	public void refreshTab()
	{
		updateTab(selectedCategory, UltraComponents.PROGRESSION.get(MinecraftClient.getInstance().player));
	}
	
	void updateWeaponButton(Button button, int idx, Identifier[] selectedWeaponTypeIds, IProgressionComponent progression)
	{
		if(selectedWeaponTypeIds.length < (idx + 1) || !progression.isUnlocked(selectedWeaponTypeIds[idx]))
		{
			button.getSprite().setUv(new Vector2i(144, 160));
			button.setClickable(false).setColor(0xff888888);
		}
		else
			button.setClickable(true).setColor(0xffffffff);
	}
	
	boolean updateSelectedWeapon(Identifier[] selectedWeaponTypeIds, int idx, IProgressionComponent progression)
	{
		if(selectedWeaponTypeIds.length < idx + 1)
			return true;
		List<Pair<Sprite, String>> ingredients = ingredientList.getElements();
		ingredients.clear();
		if(idx == -1)
		{
			selectedWeapon = null;
			selectedRecipe = null;
		}
		else
		{
			if(!progression.isUnlocked(selectedWeaponTypeIds[idx]))
			{
				refreshTab();
				craftButton.setClickable(selectedRecipe != null);
				return true;
			}
			selectedWeapon = selectedWeaponTypeIds[idx];
			selectedRecipe = UltraRecipeManager.getRecipe(selectedWeapon);
			if(selectedRecipe != null)
			{
				Map<Item, Integer> materials = selectedRecipe.getMaterials();
				materials.forEach((item, amount) -> {
					Identifier id = Registries.ITEM.getId(item);
					ingredients.add(new Pair<>(new Sprite(new Identifier(id.getNamespace(), "textures/item/" + id.getPath() + ".png"),
							new Vector2i(0, 0), 0.001f, new Vector2i(16, 16), new Vector2i(0, 0), new Vector2i(16, 16)),
							amount + " " + Text.translatable(item.getTranslationKey()).getString() + (amount > 0 ? "s" : "")));
				});
				if(isResultTypeHeld() && progression.isOwned(selectedWeapon))
					craftButton.setLabel(Text.translatable("terminal.held").getString());
				else
					craftButton.setLabel(Text.translatable("terminal." + (progression.isOwned(selectedWeapon) ? "dispense" : "craft")).getString());
			}
		}
		boolean clickable = selectedRecipe != null && !(isResultTypeHeld() && progression.isOwned(selectedWeapon));
		craftButton.setClickable(clickable).setColor(clickable ? 0xffffffff : 0xff888888);
		return true;
	}
	
	@Override
	public Vector2f getSizeOverride()
	{
		return new Vector2f(300, 100);
	}
}
