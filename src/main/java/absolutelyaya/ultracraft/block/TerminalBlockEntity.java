package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.api.terminal.GlobalButtonActions;
import absolutelyaya.ultracraft.client.gui.terminal.DefaultTabs;
import absolutelyaya.ultracraft.client.gui.terminal.elements.*;
import absolutelyaya.ultracraft.client.gui.terminal.elements.ListElement;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.client.ClientGraffitiManager;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.*;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.*;

public class TerminalBlockEntity extends BlockEntity implements GeoBlockEntity
{
	//Persistent
	Base base = Base.YELLOW;
	UUID owner = null;
	java.util.List<String> lines = new ArrayList<>() {
		{
			add("+--------------+");
			add("   Tip of the Day");
			add("");
			add("     be there or");
			add("      be square");
			add("");
			add("");
			add("");
			add("");
			add("");
			add("+--------------+");
		}
	};
	int textColor = 0xffffffff;
	java.util.List<Integer> palette = new ArrayList<>() {
		{
			//0 is reserved for transparency
			add(0xffd9e7ff);
			add(0xffa5b2b9);
			add(0xff5d6569);
			add(0xff250c30);
			add(0xff78434d);
			add(0xffa80300);
			add(0xffd74b0f);
			add(0xffd7c00f);
			add(0xff77c62d);
			add(0xff1e8725);
			add(0xff2b73cb);
			add(0xff3129cb);
			add(0xff8f35cb);
			add(0xffcb0acb);
			add(0xffdf95da);
		}
	};
	ByteArrayList graffiti = new ByteArrayList();
	UUID terminalID;
	java.util.List<Button> mainMenuButtons = new ArrayList<>() {
		{
			add(new Button("terminal.customize", new Vector2i(48, 50),
					"customize", 0, true).setHide(true));
			add(new Button("terminal.bestiary", new Vector2i(48, 36),
					"bestiary", 0, true));
			add(new Button("terminal.weapons", new Vector2i(48, 22),
					"weapons", 0, true));
		}
	};
	String mainMenuTitle = "terminal.main-menu";
	//Don't save all this
	float displayVisibility = 0f, inactivity = 600f, caretTimer = 0f, graffitiCamRotation = 0f;
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	java.util.List<WingedPlayerEntity> focusedPlayers = new ArrayList<>();
	int colorOverride = -1, graffitiRevision = 0;
	Vector2d cursor = new Vector2d();
	Vector2i caret = new Vector2i();
	Element lastHovered;
	TextBox focusedTextbox;
	Tab tab = new DefaultTabs.MainMenu();
	Vector2f normalWindowSize = new Vector2f(100f, 100f), curWindowSize = new Vector2f(normalWindowSize), sizeOverride = null;
	Identifier graffitiTexture;
	int redstoneTimer, redstoneStrength;
	
	public TerminalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.TERMINAL, pos, state);
		tab.init(this);
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
	}
	
	public void onHit(int mouseButton)
	{
		inactivity = 0f;
		boolean hovering = lastHovered != null;
		tab.onClicked(getCursor(), hovering, mouseButton);
		if(!hovering)
			return;
		if(lastHovered instanceof Button b)
			handleButtonPress(b, mouseButton);
		if(lastHovered instanceof TextBox box)
		{
			if(focusedTextbox != null)
				focusedTextbox.unfocus();
			setFocusedTextbox(box);
		}
		else if(focusedTextbox != null)
		{
			focusedTextbox.unfocus();
			setFocusedTextbox(null);
		}
		if(lastHovered instanceof ListElement list && list.getLastHovered() != -1)
			list.select(list.getLastHovered());
			
	}
	
	public void scroll(double amount)
	{
		tab.onScroll(amount);
		if(lastHovered instanceof ListElement list)
			list.onScroll(amount);
	}
	
	void handleButtonPress(Button element, int mouseButton)
	{
		String action = element.getAction();
		int value = element.getValue();
		if(tab.onButtonClicked(action, value))
			return;
		if(!GlobalButtonActions.runAction(this, action, value))
			Ultracraft.LOGGER.error("Undefined Behavior for Terminal button '" + action + "@" + value + "'");
	}
	
	void onBlockBreak()
	{
		for (WingedPlayerEntity p : focusedPlayers)
			p.setFocusedTerminal(null);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if(nbt.contains("base", NbtElement.INT_TYPE))
			base = Base.values()[nbt.getInt("base")];
		if(nbt.contains("txt-clr", NbtElement.INT_TYPE))
			textColor = nbt.getInt("txt-clr");
		if(nbt.containsUuid("owner"))
			owner = nbt.getUuid("owner");
		if(nbt.contains("screensaver", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound screensaver = nbt.getCompound("screensaver");
			applyScreensaver(screensaver);
		}
		if(nbt.containsUuid("terminal-id"))
			terminalID = nbt.getUuid("terminal-id");
		else
			terminalID = UUID.randomUUID();
		if(nbt.contains("graffiti", NbtElement.COMPOUND_TYPE))
		{
			applyGraffiti(nbt.getCompound("graffiti"));
			if(world != null && world.isClient)
				ClientGraffitiManager.refreshGraffitiTexture(this);
		}
		if(nbt.contains("mainmenu", NbtElement.COMPOUND_TYPE))
			applyMainMenu(nbt.getCompound("mainmenu"));
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("base", base.ordinal());
		nbt.putInt("txt-clr", textColor);
		if(owner != null)
			nbt.putUuid("owner", owner);
		nbt.put("screensaver", serializeScreensaver());
		nbt.putUuid("terminal-id", Objects.requireNonNullElseGet(terminalID, () -> terminalID = UUID.randomUUID()));
		nbt.put("graffiti", serializeGraffiti());
		nbt.put("mainmenu", serializeMainMenu());
	}
	
	public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState state, T t)
	{
		if(t instanceof TerminalBlockEntity terminal)
			terminal.tickRedstoneTimer();
	}
	
	void tickRedstoneTimer()
	{
		if(redstoneTimer > 0)
			redstoneTimer--;
		else if(redstoneStrength > 0)
		{
			redstoneStrength = 0;
			world.updateNeighbors(getPos(), world.getBlockState(pos).getBlock());
			world.updateNeighbors(getPos().down(), world.getBlockState(pos.down()).getBlock());
			world.updateNeighbors(getPos().up(), world.getBlockState(pos.up()).getBlock());
		}
	}
	
	void applyScreensaver(NbtCompound screensaver)
	{
		for (int i = 0; i < 11; i++)
		{
			if(screensaver.contains("line" + i, NbtElement.STRING_TYPE))
				lines.set(i, screensaver.getString("line" + i));
			else
				lines.set(i, "");
		}
	}
	
	NbtCompound serializeScreensaver()
	{
		NbtCompound screensaver = new NbtCompound();
		for (int i = 0; i < lines.size(); i++)
			if(lines.get(i).length() > 0)
				screensaver.putString("line" + i, lines.get(i));
		return screensaver;
	}
	
	void applyGraffiti(NbtCompound nbt)
	{
		palette = Arrays.asList(ArrayUtils.toObject(nbt.getIntArray("palette")));
		String pixelString = nbt.getString("pixels");
		ByteArrayList pixels = new ByteArrayList();
		for (int i = 0; i < pixelString.length() - 1; i++)
			pixels.add((byte)Byte.valueOf(pixelString.substring(i, i + 1), 16));
		graffiti = pixels;
	}
	
	NbtCompound serializeGraffiti()
	{
		NbtCompound graffiti = new NbtCompound();
		graffiti.putIntArray("palette", ArrayUtils.toPrimitive(getPalette().toArray(new Integer[15])));
		graffiti.putString("pixels", serializePixels(getGraffiti()));
		return graffiti;
	}
	
	public static String serializePixels(java.util.List<Byte> pixelPairs)
	{
		StringBuilder out = new StringBuilder();
		for (byte b : pixelPairs)
			out.append(Integer.toHexString(b));
		return out.toString();
	}
	
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket()
	{
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	@Override
	public NbtCompound toInitialChunkDataNbt()
	{
		return createNbt();
	}
	
	public void syncCustomization()
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeInt(textColor);
		buf.writeInt(base.ordinal());
		buf.writeNbt(serializeScreensaver());
		buf.writeNbt(serializeMainMenu());
		ClientPlayNetworking.send(PacketRegistry.TERMINAL_SYNC_C2S_PACKET_ID, buf);
		markDirty();
	}
	
	public void syncGraffiti()
	{
		Integer[] palette = getPalette().toArray(new Integer[15]);
		Byte[] pixels = getGraffiti().toArray(new Byte[0]);
		graffitiRevision++;
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeIntArray(ArrayUtils.toPrimitive(palette));
		buf.writeByteArray(ArrayUtils.toPrimitive(pixels));
		buf.writeInt(getGraffitiRevision());
		ClientPlayNetworking.send(PacketRegistry.GRAFFITI_C2S_PACKET_ID, buf);
		graffitiCamRotation = 0f;
		
		ClientGraffitiManager.refreshGraffitiTexture(this);
		markDirty();
	}
	
	public void applyCustomization(int c, int base, NbtCompound screensaver, NbtCompound mainMenu)
	{
		textColor = c;
		if(base >= 0 && base < Base.values().length)
			this.base = Base.values()[base];
		applyScreensaver(screensaver);
		applyMainMenu(mainMenu);
		markDirty();
		world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
	}
	
	public float getRotation()
	{
		if(!getWorld().getBlockState(getPos()).isOf(BlockRegistry.TERMINAL_DISPLAY))
			return 0;
		return switch(getWorld().getBlockState(getPos()).get(TerminalDisplayBlock.FACING))
		{
			default -> 0;
			case EAST -> 90;
			case SOUTH -> 180;
			case WEST -> 270;
		};
	}
	
	public void setDisplayVisibility(float f)
	{
		displayVisibility = MathHelper.clamp(f, 0f, 1f);
		if(getWorld().getBlockState(getPos()).isOf(BlockRegistry.TERMINAL_DISPLAY))
			getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(TerminalDisplayBlock.GLOWS,displayVisibility > 0.25f));
		if(displayVisibility == 0f && !tab.id.equals(Tab.MAIN_MENU_ID))
			setTab(new DefaultTabs.MainMenu());
	}
	
	public float getDisplayVisibility()
	{
		return displayVisibility;
	}
	
	public java.util.List<String> getScreensaver()
	{
		return lines;
	}
	
	public int getTextColor()
	{
		if(colorOverride != -1)
			return colorOverride;
		return textColor;
	}
	
	public void setTextColor(int i)
	{
		textColor = i;
	}
	
	public void setColorOverride(int colorOverride)
	{
		this.colorOverride = colorOverride;
	}
	
	public float getInactivity()
	{
		return inactivity;
	}
	
	public void setInactivity(float f)
	{
		if(!tab.id.equals(Tab.MAIN_MENU_ID))
			f = 0f;
		inactivity = MathHelper.clamp(f, 0f, 600f);
		if(inactivity > 30f)
			lastHovered = null;
	}
	
	public boolean isFocused(WingedPlayerEntity p)
	{
		return focusedPlayers.contains(p);
	}
	
	public void unFocus(WingedPlayerEntity p)
	{
		focusedPlayers.remove(p);
	}
	
	public UUID getOwner()
	{
		return owner;
	}
	
	public void setCursor(Vector2d cursor)
	{
		float minX = -curWindowSize.x / 200f + 0.5f;
		this.cursor = new Vector2d(MathHelper.clamp(cursor.x, minX, minX + curWindowSize.x / 100f), MathHelper.clamp(cursor.y, 0, curWindowSize.y / 100f));
	}
	
	public Vector2d getCursor()
	{
		return cursor;
	}
	
	public Element getLastHovered()
	{
		return lastHovered;
	}
	
	public void setLastHovered(Element lastHovered)
	{
		this.lastHovered = lastHovered;
	}
	
	NbtCompound serializeMainMenu()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putString("title", mainMenuTitle);
		NbtList buttons = new NbtList();
		for (Button b : mainMenuButtons)
			buttons.add(b.serialize());
		nbt.put("buttons", buttons);
		return nbt;
	}
	
	void applyMainMenu(NbtCompound nbt)
	{
		mainMenuTitle = nbt.getString("title");
		mainMenuButtons = new ArrayList<>();
		NbtList list = nbt.getList("buttons", NbtElement.COMPOUND_TYPE);
		for (NbtElement button : list)
		{
			if(button instanceof NbtCompound compound)
				mainMenuButtons.add(Button.deserialize(compound));
		}
	}
	
	public void setTab(Tab tab)
	{
		if(tab != this.tab)
		{
			tab.init(this);
			this.tab.onClose(this);
		}
		this.tab = tab;
		switch(tab.id)
		{
			case Tab.MAIN_MENU_ID, Tab.CUSTOMIZATION_ID ->
			{
				colorOverride = -1;
				sizeOverride = null;
			}
			case Tab.COMING_SOON_ID -> colorOverride = 0xffffff00;
			case Tab.EDIT_SCREENSAVER_ID -> {
				caret = new Vector2i();
				sizeOverride = new Vector2f(200, 106);
			}
			default -> {
				sizeOverride = tab.getSizeOverride();
				colorOverride = tab.getColorOverride();
			}
		}
		if(focusedTextbox != null)
			setFocusedTextbox(null);
	}
	
	public Tab getTab()
	{
		return tab;
	}
	
	public Vector2f getCurWindowSize()
	{
		return curWindowSize;
	}
	
	public void setCurWindowSize(Vector2f curWindowSize)
	{
		this.curWindowSize = curWindowSize;
	}
	
	public Vector2f getSizeOverride()
	{
		return sizeOverride;
	}
	
	public Vector2f getNormalWindowSize()
	{
		return normalWindowSize;
	}
	
	public void setBase(Base base)
	{
		this.base = base;
	}
	
	public Base getBase()
	{
		return base;
	}
	
	public Vector2i getCaret()
	{
		return caret;
	}
	
	public void setCaretTimer(float f)
	{
		caretTimer = f;
	}
	
	public float getCaretTimer()
	{
		return caretTimer;
	}
	
	public void setCaret(Vector2i v)
	{
		java.util.List<String> lines = focusedTextbox.getLines();
		int y = v.y < 0 ? lines.size() - 1 : v.y % lines.size();
		int x;
		int len = lines.get(y).length();
		if(len == 0)
			x = 0;
		else
			x = v.x < 0 ? lines.get(y).length() : v.x % (lines.get(y).length() + 1);
		caret = new Vector2i(x, y);
		caretTimer = 0f;
	}
	
	public Vec3d getCamOffset()
	{
		if(tab.id.equals(Tab.GRAFFITI_ID))
			return new Vec3d(0f, 0f, -2.5f).rotateY((getRotation() + graffitiCamRotation) * -MathHelper.RADIANS_PER_DEGREE);
		return new Vec3d(0f, 0f, -1f).rotateY(getRotation() * -MathHelper.RADIANS_PER_DEGREE);
	}
	
	public ByteArrayList getGraffiti()
	{
		return graffiti;
	}
	
	public void setGraffiti(ByteArrayList pixels)
	{
		graffiti = pixels;
	}
	
	public void setPalette(java.util.List<Integer> colors)
	{
		palette = colors;
	}
	
	public void setPaletteColor(int idx, int color)
	{
		if(idx >= 0)
			palette.set(idx, color);
	}
	
	public java.util.List<Integer> getPalette()
	{
		return palette;
	}
	
	public int getPaletteColor(int i)
	{
		if(i == 0)
			return 0x0;
		return palette.get(i - 1);
	}
	
	public UUID getTerminalID()
	{
		return terminalID;
	}
	
	public int getGraffitiRevision()
	{
		return graffitiRevision;
	}
	
	public void setGraffitiRevision(int i)
	{
		graffitiRevision = i;
	}
	
	public Identifier getGraffitiTexture()
	{
		return graffitiTexture;
	}
	
	public boolean isOwner(UUID id)
	{
		if(owner == null)
			return true;
		return owner.equals(id);
	}
	
	public boolean isCannotBreak(PlayerEntity p)
	{
		return !(isOwner(p.getUuid()) || ((WingedPlayerEntity) p).isOpped());
	}
	
	public void rotateGrafittiCam(float f)
	{
		graffitiCamRotation += f;
	}
	
	public void setPixel(int x, int y, byte color)
	{
		int i = x + y * 32;
		while(graffiti.size() <= i)
			graffiti.add((byte)0);
		graffiti.set(i, color);
		ClientGraffitiManager.refreshGraffitiTexture(this);
	}
	
	public void setGraffitiTexture(Identifier identifier)
	{
		graffitiTexture = identifier;
	}
	
	public java.util.List<Button> getMainMenuButtons()
	{
		return mainMenuButtons;
	}
	
	public String getMainMenuTitle()
	{
		return mainMenuTitle;
	}
	
	public void setMainMenuTitle(String s)
	{
		mainMenuTitle = s;
	}
	
	public TextBox getFocusedTextbox()
	{
		return focusedTextbox;
	}
	
	public void setFocusedTextbox(TextBox box)
	{
		caret.set(0, 0);
		if(focusedTextbox != null)
			focusedTextbox.unfocus();
		focusedTextbox = box;
	}
	
	public void redstoneImpulse(int strength)
	{
		redstoneStrength = strength;
		redstoneTimer = 2;
		world.updateNeighbors(getPos(), world.getBlockState(pos).getBlock());
		world.updateNeighbors(getPos().down(), world.getBlockState(pos.down()).getBlock());
		world.updateNeighbors(getPos().up(), world.getBlockState(pos.up()).getBlock());
	}
	
	public int getRedstoneStrength()
	{
		return redstoneStrength;
	}
	
	public enum Base
	{
		WHITE, LIGHT_GRAY, GRAY, BLACK, BROWN, RED, ORANGE, YELLOW,
		LIME, GREEN, CYAN, LIGHT_BLUE, BLUE, PURPLE, MAGENTA, PINK;
		
		public Identifier getTexture()
		{
			return new Identifier(Ultracraft.MOD_ID, String.format("textures/block/terminal/%s.png", name().toLowerCase()));
		}
		
		public String translationKey()
		{
			return "color.minecraft." + name().toLowerCase();
		}
	}
}
