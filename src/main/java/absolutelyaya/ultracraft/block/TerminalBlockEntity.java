package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.*;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerminalBlockEntity extends BlockEntity implements GeoBlockEntity
{
	//Persistent
	Base base = Base.YELLOW;
	UUID owner = UUID.fromString("4a23954b-551c-4e2b-ac52-eb2e1ccbe443");
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
	//Don't save all this
	float displayVisibility = 0f, inactivity = 600f, caretTimer = 0f;
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	List<WingedPlayerEntity> focusedPlayers = new ArrayList<>();
	int colorOverride = -1;
	Vector2d cursor = new Vector2d();
	Vector2i caret = new Vector2i();
	String lastHovered;
	Tab tab = Tab.MAIN_MENU;
	Vector2f normalWindowSize = new Vector2f(100f, 100f), curWindowSize = new Vector2f(normalWindowSize), sizeOverride = null;
	
	public TerminalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.TERMINAL, pos, state);
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
	}
	
	public void onHit()
	{
		inactivity = 0f;
		if(lastHovered != null)
		{
			String[] segments = lastHovered.split("@");
			String action = segments[0];
			int value = 0;
			if(segments.length > 1)
				value = Integer.parseInt(segments[1]);
			
			switch(action)
			{
				case "customize" -> setTab(Tab.CUSTOMIZATION);
				case "bestiary" -> setTab(Tab.COMING_SOON);
				case "weapons" -> setTab(Tab.WEAPONS);
				case "mainmenu" -> setTab(Tab.MAIN_MENU);
				case "edit-screensaver" -> setTab(Tab.EDIT_SCREENSAVER);
				case "edit-base" -> setTab(Tab.BASE_SELECT);
				case "graffiti" -> setTab(Tab.COMING_SOON);
				
				case "set-base" -> base = Base.values()[value];
				case "toggle-lock" -> setLocked(!locked);
				default -> Ultracraft.LOGGER.error("Undefined Behavior for Terminal button '" + lastHovered + "'");
			}
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
		if(nbt.contains("locked", NbtElement.INT_TYPE))
			locked = nbt.getBoolean("locked");
		if(nbt.contains("screensaver", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound screensaver = nbt.getCompound("screensaver");
			applyScreensaver(screensaver);
		}
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("base", base.ordinal());
		nbt.putInt("txt-clr", textColor);
		nbt.putUuid("owner", owner);
		nbt.putBoolean("locked", locked);
		nbt.put("screensaver", serializeScreensaver());
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
		}
		this.tab = tab;
		switch(tab)
		{
			case MAIN_MENU, CUSTOMIZATION ->
			{
				colorOverride = -1;
				sizeOverride = null;
			}
			case COMING_SOON -> colorOverride = 0xffffff00;
			case WEAPONS, BESTIARY -> sizeOverride = new Vector2f(200, 100);
			case EDIT_SCREENSAVER -> {
				caret = new Vector2i();
				sizeOverride = new Vector2f(200, 106);
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
	
	public Base getBase()
	{
		return base;
	}
	
	public void setLocked(boolean b)
	{
		locked = b;
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
	
	public enum Tab
	{
		MAIN_MENU,
		COMING_SOON,
		WEAPONS,
		BESTIARY,
		CUSTOMIZATION,
		BASE_SELECT,
		EDIT_SCREENSAVER
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
