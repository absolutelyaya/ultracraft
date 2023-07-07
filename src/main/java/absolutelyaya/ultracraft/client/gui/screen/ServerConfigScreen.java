package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.accessor.WidgetAccessor;
import absolutelyaya.ultracraft.client.gui.widget.GameRuleWidget;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ServerConfigScreen extends Screen
{
	public static ServerConfigScreen INSTANCE;
	final NbtCompound rules;
	List<GameRuleWidget<?>> ruleWidgets = new ArrayList<>();
	float curScroll, desiredScroll;
	
	public ServerConfigScreen(NbtCompound rules)
	{
		super(Text.translatable("screen.ultracraft.server.config-menu.title"));
		this.rules = rules;
		INSTANCE = this;
	}
	
	@Override
	protected void init()
	{
		super.init();
		ruleWidgets.forEach(this::remove);
		ruleWidgets.clear();
		Vector2i pos = new Vector2i(width / 2 - 100, 40);
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.PROJ_BOOST, GameruleRegistry.ProjectileBoostSetting.values(), ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.HI_VEL_MODE,GameruleRegistry.Option.values(), ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.TIME_STOP, new String[] { GameruleRegistry.Option.FORCE_ON.toString(), GameruleRegistry.Option.FORCE_OFF.toString() }, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.DISABLE_HANDSWAP, GameRuleWidget.ValueType.BOOL, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.HIVEL_JUMP_BOOST, GameRuleWidget.ValueType.INT, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.SLAM_STORAGE, GameRuleWidget.ValueType.BOOL, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.HIVEL_FALLDAMAGE, GameRuleWidget.ValueType.BOOL, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.HIVEL_DROWNING, GameRuleWidget.ValueType.BOOL, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.BLOODHEAL, GameruleRegistry.RegenOption.values(), ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.HIVEL_SPEED, GameRuleWidget.ValueType.INT, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.HIVEL_SLOWFALL, GameRuleWidget.ValueType.INT, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.EFFECTIVELY_VIOLENT, GameRuleWidget.ValueType.BOOL, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.EXPLOSION_DAMAGE, GameRuleWidget.ValueType.BOOL, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.SM_SAFE_LEDGES, GameRuleWidget.ValueType.BOOL, ruleWidgets.size())));
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, GameruleRegistry.PARRY_CHAINING, GameRuleWidget.ValueType.BOOL, ruleWidgets.size())));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		curScroll = MathHelper.lerp(delta / 2f, curScroll, desiredScroll);
		ruleWidgets.forEach(w -> ((WidgetAccessor)w).setOffset(new Vector2i(0, Math.round(curScroll))));
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xffffffff);
	}
	
	@Override
	public void renderBackground(DrawContext context)
	{
		super.renderBackground(context);
		context.fill(0, 0, width, height, 0x44000000);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
		context.drawTexture(OPTIONS_BACKGROUND_TEXTURE, width /2 - 125, 0, 0, 0.0f, 0.0f, 250, height, 32, 32);
		context.fill(width / 2 - 125, -1, width / 2 - 124, height + 1, 0xaaffffff);
		context.fill(width / 2 + 125, -1, width / 2 + 124, height + 1, 0xaa000000);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		desiredScroll = MathHelper.clamp(desiredScroll + (float)amount * 15f, -36 * (ruleWidgets.size() - 3), 0);
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	public <T extends GameRules.Key<?>> void onExternalRuleUpdate(T rule, String value)
	{
		rules.putString(rule.getName(), value);
		ruleWidgets.forEach(GameRuleWidget::stateUpdate);
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
	public void close()
	{
		super.close();
		INSTANCE = null;
	}
}
