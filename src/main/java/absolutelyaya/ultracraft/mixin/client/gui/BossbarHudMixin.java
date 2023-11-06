package absolutelyaya.ultracraft.mixin.client.gui;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.BossBarAccessor;
import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
public abstract class BossbarHudMixin
{
	@Shadow @Final Map<UUID, ClientBossBar> bossBars;
	
	@Shadow protected abstract void renderBossBar(DrawContext context, int x, int y, BossBar bossBar);
	
	@Shadow @Final private MinecraftClient client;
	private static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/bossbar.png");
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	void onRenderBossBar(DrawContext context, CallbackInfo ci)
	{
		BossBar.Style ultrastyle = ClassTinkerers.getEnum(BossBar.Style.class, "ULTRA");
		if(bossBars.values().stream().noneMatch(i -> i.getStyle().equals(ultrastyle)))
			return;
		
		int center = context.getScaledWindowWidth() / 2;
		int x = center - 91, y = 12;
		for (BossBar bar : bossBars.values())
		{
			if(bar.getStyle().equals(ultrastyle))
			{
				context.fill(x - 2, y - 11, x + 184, y + 2, 0x88000000);
				if(bar instanceof BossBarAccessor barr)
				{
					barr.update(0.02f);
					context.drawTexture(TEXTURE, x, y -10, 0, 10, Math.round(182 * barr.getDeltaPercent()), 10);
				}
				context.drawTexture(TEXTURE, x, y -10, 0, 0, Math.round(182 * bar.getPercent()), 10);
				Text t = bar.getName();
				context.drawText(client.textRenderer, t, center - client.textRenderer.getWidth(t) / 2, y - 9, 0xffffffff, true);
				y += 13;
				if(y > context.getScaledWindowHeight() / 3)
					break;
				continue;
			}
			renderBossBar(context, x, y, bar);
			Text t = bar.getName();
			context.drawTextWithShadow(client.textRenderer, t, center - client.textRenderer.getWidth(t) / 2, y - 9, 0xffffffff);
			y += 10 + client.textRenderer.fontHeight;
			if(y > context.getScaledWindowHeight() / 3)
				break;
		}
		ci.cancel();
	}
}
