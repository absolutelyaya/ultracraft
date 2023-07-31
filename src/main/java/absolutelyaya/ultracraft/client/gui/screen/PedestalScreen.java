package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.LockButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class PedestalScreen extends HandledScreen<PedestalScreenHandler>
{
	private static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/container/pedestal.png");
	LockButtonWidget lock;
	
	public PedestalScreen(PedestalScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, Text.translatable("screen.ultracraft.pedestal.title"));
	}
	
	protected void init()
	{
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		lock = addDrawableChild(new LockButtonWidget(x + 78, y + 54, b -> {
			LockButtonWidget lock = (LockButtonWidget)b;
			lock.setLocked(!lock.isLocked());
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			HitResult hit = MinecraftClient.getInstance().crosshairTarget;
			buf.writeBlockPos(hit.getType().equals(HitResult.Type.BLOCK) ? ((BlockHitResult)hit).getBlockPos() : BlockPos.ORIGIN);
			buf.writeBoolean(lock.isLocked());
			ClientPlayNetworking.send(PacketRegistry.LOCK_PEDESTAL_ID, buf);
		}));
		lock.setTooltip(Tooltip.of(Text.translatable("screen.ultracraft.pedestal.lock")));
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		int x = width / 2;
		int y = (height - backgroundHeight) / 2;
		Text key = Text.translatable("screen.ultracraft.pedestal.key");
		int keyWidth = textRenderer.getWidth(key);
		context.drawText(textRenderer, key, x - 9 - keyWidth, y + 21, 0x3f3f3f, false);
		this.drawMouseoverTooltip(context, mouseX, mouseY);
	}
	
	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		context.drawTexture(TEXTURE, (width - backgroundWidth) / 2, (height - backgroundHeight) / 2, 0, 0, backgroundWidth, backgroundHeight);
	}
}
