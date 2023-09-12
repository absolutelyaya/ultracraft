package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.api.GlobalButtonActions;
import absolutelyaya.ultracraft.client.rendering.block.entity.TerminalBlockEntityRenderer;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.client.ClientGraffitiManager;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.util.TerminalGuiRenderer;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.object.Color;

import java.util.*;

public class TerminalBlockEntity extends BlockEntity implements GeoBlockEntity
{
	//Persistent
	Base base = Base.YELLOW;
	UUID owner = null;
	List<String> lines = new ArrayList<>() {
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
	boolean locked = false;
	List<Integer> palette = new ArrayList<>() {
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
	List<Button> mainMenuButtons = new ArrayList<>() {
		{
			add(new TerminalBlockEntity.Button("screen.ultracraft.terminal.customize", new Vector2i(48, 50),
					"customize", 0, true, true));
			add(new TerminalBlockEntity.Button("screen.ultracraft.terminal.bestiary", new Vector2i(48, 36),
					"bestiary", 0, true, true));
			add(new TerminalBlockEntity.Button("screen.ultracraft.terminal.weapons", new Vector2i(48, 22),
					"weapons", 0, true, true));
		}
	};
	String mainMenuTitle = "terminal.main-menu";
	//Don't save all this
	float displayVisibility = 0f, inactivity = 600f, caretTimer = 0f, graffitiCamRotation = 0f;
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	List<WingedPlayerEntity> focusedPlayers = new ArrayList<>();
	int colorOverride = -1, graffitiRevision = 0;
	Vector2d cursor = new Vector2d();
	Vector2i caret = new Vector2i();
	String lastHovered;
	Tab tab = Tab.MAIN_MENU;
	Vector2f normalWindowSize = new Vector2f(100f, 100f), curWindowSize = new Vector2f(normalWindowSize), sizeOverride = null;
	Identifier graffitiTexture;
	
	public TerminalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.TERMINAL, pos, state);
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
	}
	
	public void onHit(int button)
	{
		inactivity = 0f;
		boolean hovering = lastHovered != null;
		tab.onClicked(getCursor(), hovering, button);
		if(hovering)
		{
			String[] segments = lastHovered.split("@");
			String action = segments[0];
			int value = 0;
			if(segments.length > 1)
				value = Integer.parseInt(segments[1]);
			
			if(!Tab.isDefaultTab(tab.id))
			{
				if(tab.onButtonClicked(action, value))
					return;
			}
			if(!GlobalButtonActions.runAction(this, action, value))
				Ultracraft.LOGGER.error("Undefined Behavior for Terminal button '" + lastHovered + "'");
		}
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
		if(nbt.contains("locked", NbtElement.BYTE_TYPE))
			locked = nbt.getBoolean("locked");
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
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("base", base.ordinal());
		nbt.putInt("txt-clr", textColor);
		if(owner != null)
			nbt.putUuid("owner", owner);
		nbt.putBoolean("locked", locked);
		nbt.put("screensaver", serializeScreensaver());
		nbt.putUuid("terminal-id", Objects.requireNonNullElseGet(terminalID, () -> terminalID = UUID.randomUUID()));
		nbt.put("graffiti", serializeGraffiti());
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
	
	public static String serializePixels(List<Byte> pixelPairs)
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
	
	public void syncCustomization(int c, int base, boolean locked, NbtCompound screensaver)
	{
		textColor = c;
		if(base >= 0 && base < Base.values().length)
			this.base = Base.values()[base];
		this.locked = locked;
		applyScreensaver(screensaver);
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
		if(displayVisibility == 0f && !tab.equals(Tab.MAIN_MENU))
			setTab(Tab.MAIN_MENU);
	}
	
	public float getDisplayVisibility()
	{
		return displayVisibility;
	}
	
	public List<String> getLines()
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
		if(!tab.equals(Tab.MAIN_MENU))
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
	
	public String getLastHovered()
	{
		return lastHovered;
	}
	
	public void setLastHovered(String lastHovered)
	{
		this.lastHovered = lastHovered;
	}
	
	public void setTab(Tab tab)
	{
		if(this.tab.equals(Tab.CUSTOMIZATION) && tab.equals(Tab.MAIN_MENU))
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBlockPos(pos);
			buf.writeInt(textColor);
			buf.writeInt(base.ordinal());
			buf.writeBoolean(locked);
			buf.writeNbt(serializeScreensaver());
			ClientPlayNetworking.send(PacketRegistry.TERMINAL_SYNC_C2S_PACKET_ID, buf);
			markDirty();
		}
		if(this.tab.equals(Tab.GRAFFITI) && !tab.equals(Tab.GRAFFITI))
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
		if(tab != this.tab)
			tab.init(this);
		this.tab = tab;
		switch(tab.id)
		{
			case Tab.MAIN_MENU_ID, Tab.CUSTOMIZATION_ID ->
			{
				colorOverride = -1;
				sizeOverride = null;
			}
			case Tab.COMING_SOON_ID -> colorOverride = 0xffffff00;
			case Tab.WEAPONS_ID, Tab.BESTIARY_ID -> sizeOverride = new Vector2f(200, 100);
			case Tab.EDIT_SCREENSAVER_ID -> {
				caret = new Vector2i();
				sizeOverride = new Vector2f(200, 106);
			}
			default -> {
				sizeOverride = tab.getSizeOverride();
				colorOverride = tab.getColorOverride();
			}
		}
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
	
	public void setLocked(boolean b)
	{
		locked = b;
	}
	
	public void toggleLock()
	{
		setLocked(!locked);
	}
	
	public boolean isLocked()
	{
		return locked;
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
		if(tab.equals(Tab.GRAFFITI))
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
	
	public void setPalette(List<Integer> colors)
	{
		palette = colors;
	}
	
	public void setPaletteColor(int idx, int color)
	{
		if(idx >= 0)
			palette.set(idx, color);
	}
	
	public List<Integer> getPalette()
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
	
	public List<Button> getMainMenuButtons()
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
	
	public static class Tab
	{
		public static final TextRenderer textRenderer;
		public static final TerminalGuiRenderer GUI;
		
		public static final String MAIN_MENU_ID = "main-menu";
		public static final String COMING_SOON_ID = "placeholder";
		public static final String WEAPONS_ID = "weapons";
		public static final String BESTIARY_ID = "enemies";
		public static final String CUSTOMIZATION_ID = "customize";
		public static final String BASE_SELECT_ID = "base-select";
		public static final String EDIT_SCREENSAVER_ID = "edit-screensaver";
		public static final String GRAFFITI_ID = "graffiti";
		public static final Tab MAIN_MENU = new Tab(MAIN_MENU_ID);
		public static final Tab COMING_SOON = new Tab(COMING_SOON_ID);
		public static final Tab WEAPONS = new Tab(WEAPONS_ID);
		public static final Tab BESTIARY = new Tab(BESTIARY_ID);
		public static final Tab CUSTOMIZATION = new Tab(CUSTOMIZATION_ID);
		public static final Tab BASE_SELECT = new Tab(BASE_SELECT_ID);
		public static final Tab EDIT_SCREENSAVER = new Tab(EDIT_SCREENSAVER_ID);
		public static final Tab GRAFFITI = new Tab(GRAFFITI_ID);
		static final List<String> defaultTabs = new ArrayList<>() {
			{
				add(MAIN_MENU_ID);
				add(COMING_SOON_ID);
				add(WEAPONS_ID);
				add(BESTIARY_ID);
				add(CUSTOMIZATION_ID);
				add(BASE_SELECT_ID);
				add(EDIT_SCREENSAVER_ID);
				add(GRAFFITI_ID);
			}
		};
		
		protected static float time;
		protected final List<Button> buttons = new ArrayList<>();
		public final String id;
		
		public Tab(String id)
		{
			this.id = id;
		}
		
		@ApiStatus.Internal
		public static boolean isDefaultTab(String id)
		{
			return defaultTabs.contains(id);
		}
		
		public void init(TerminalBlockEntity terminal)
		{
			time = 0f;
		}
		
		public final void render(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
		{
			time += MinecraftClient.getInstance().getTickDelta() / 20f;
			renderCustomTab(matrices, terminal, buffers);
		}
		
		public void renderCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
		{
			GUI.drawBG(matrices, buffers);
			renderButtons(matrices, terminal, buffers);
		}
		
		public void renderButtons(MatrixStack matrices,TerminalBlockEntity terminal, VertexConsumerProvider buffers)
		{
			for (Button b : buttons)
			{
				if(!b.isHide() || terminal.isOwner(MinecraftClient.getInstance().player.getUuid()))
					GUI.drawButton(buffers, matrices, b);
			}
		}
		
		public Vector2f getSizeOverride()
		{
			return null;
		}
		
		public int getColorOverride()
		{
			return -1;
		}
		
		public void onClicked(Vector2d pos, boolean element, int button)
		{
		
		}
		
		public boolean onButtonClicked(String action, int value)
		{
			return false;
		}
		
		public boolean drawCustomCursor(MatrixStack matrices, VertexConsumerProvider buffers, Vector2d pos)
		{
			return false;
		}
		
		protected int getRainbow(float speed)
		{
			return Color.HSBtoARGB(time * speed % 1f, 1, 1);
		}
		
		static {
			textRenderer = MinecraftClient.getInstance().textRenderer;
			GUI = TerminalBlockEntityRenderer.GUI;
		}
	}
	
	public static class Button
	{
		public static final String RETURN_LABEL = "screen.ultracraft.terminal.button.back";
		
		protected String label, action;
		protected Vector2i position;
		protected int value;
		protected boolean hide, centered;
		
		public Button(String label, Vector2i position, String action, int value, boolean hide, boolean centered)
		{
			this.label = label;
			this.action = action;
			this.position = position;
			this.value = value;
			this.hide = hide;
			this.centered = centered;
		}
		
		public String getLabel()
		{
			return label;
		}
		
		public String getAction()
		{
			return action;
		}
		
		public Vector2i getPos()
		{
			return position;
		}
		
		public boolean isHide()
		{
			return hide;
		}
		
		public void toggleHide()
		{
			hide = !hide;
		}
		
		public boolean isCentered()
		{
			return centered;
		}
		
		public void toggleCentered()
		{
			centered = !centered;
		}
		
		public int getValue()
		{
			return value;
		}
		
		public NbtCompound serialize()
		{
			NbtCompound nbt = new NbtCompound();
			nbt.putString("label", label);
			nbt.putInt("x", position.x);
			nbt.putInt("y", position.y);
			nbt.putString("action", action);
			nbt.putInt("value", value);
			nbt.putBoolean("hide", hide);
			nbt.putBoolean("centered", centered);
			return nbt;
		}
		
		public static Button deserialize(NbtCompound nbt)
		{
			String label = nbt.getString("label");
			Vector2i pos = new Vector2i(nbt.getInt("x"), nbt.getInt("y"));
			String action = nbt.getString("action");
			int value = nbt.getInt("value");
			boolean hide = nbt.getBoolean("hide");
			boolean centered = nbt.getBoolean("centered");
			return new Button(label, pos, action, value, hide, centered);
		}
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
