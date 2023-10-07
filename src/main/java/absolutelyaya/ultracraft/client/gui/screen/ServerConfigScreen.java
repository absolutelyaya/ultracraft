package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WidgetAccessor;
import absolutelyaya.ultracraft.client.gui.widget.GameRuleWidget;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
	CheckboxWidget simplistic;
	
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
		addRule(GameruleRegistry.PROJ_BOOST, pos, GameruleRegistry.ProjectileBoostSetting.values());
		addRule(GameruleRegistry.HIVEL_MODE, pos, GameruleRegistry.Setting.values());
		addRule(GameruleRegistry.TIME_STOP, pos, new String[] { GameruleRegistry.Setting.FORCE_ON.toString(), GameruleRegistry.Setting.FORCE_OFF.toString() });
		addRule(GameruleRegistry.DISABLE_HANDSWAP, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.HIVEL_JUMP_BOOST, pos, GameRuleWidget.ValueType.INT);
		addRule(GameruleRegistry.SLAM_STORAGE, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.HIVEL_FALLDAMAGE, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.HIVEL_DROWNING, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.BLOODHEAL, pos, GameruleRegistry.RegenSetting.values());
		addRule(GameruleRegistry.HIVEL_SPEED, pos, GameRuleWidget.ValueType.INT);
		addRule(GameruleRegistry.HIVEL_SLOWFALL, pos, GameRuleWidget.ValueType.INT);
		addRule(GameruleRegistry.EFFECTIVELY_VIOLENT, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.EXPLOSION_DAMAGE, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.SM_SAFE_LEDGES, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.PARRY_CHAINING, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.TNT_PRIMING, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.REVOLVER_DAMAGE, pos, GameRuleWidget.ValueType.INT);
		addRule(GameruleRegistry.INVINCIBILITY, pos, GameRuleWidget.ValueType.INT);
		addRule(GameruleRegistry.TERMINAL_PROT, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.GRAFFITI, pos, GameruleRegistry.GraffitiSetting.values());
		addRule(GameruleRegistry.FLAMETHROWER_GRIEF, pos, GameRuleWidget.ValueType.BOOL);
		addRule(GameruleRegistry.SHOTGUN_DAMAGE, pos, GameRuleWidget.ValueType.INT);
		addRule(GameruleRegistry.NAILGUN_DAMAGE, pos, GameRuleWidget.ValueType.INT);
		addRule(GameruleRegistry.HELL_OBSERVER_INTERVAL, pos, GameRuleWidget.ValueType.INT);
		addRule(GameruleRegistry.START_WITH_PIERCER, pos, GameRuleWidget.ValueType.BOOL);
		
		boolean b = false;
		if(simplistic != null)
			b = simplistic.isChecked();
		simplistic = addDrawableChild(new CheckboxWidget(width / 2 + 130, height - 30, 60, 20,
				Text.translatable("screen.ultracraft.server.config-menu.simplistic"), b));
	}
	
	<K extends GameRules.Key<?>> void addRule(K key, Vector2i pos, String[] values)
	{
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, key, values, ruleWidgets.size())));
	}
	
	<K extends GameRules.Key<?>> void addRule(K key, Vector2i pos, Enum<?>[] values)
	{
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, key, values, ruleWidgets.size())));
	}
	
	<K extends GameRules.Key<?>> void addRule(K key, Vector2i pos, GameRuleWidget.ValueType valueType)
	{
		ruleWidgets.add(addDrawableChild(new GameRuleWidget<>(rules, pos, key, valueType, ruleWidgets.size())));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		curScroll = MathHelper.lerp(delta / 2f, curScroll, desiredScroll);
		ruleWidgets.forEach(w -> {
			((WidgetAccessor)w).setOffset(new Vector2i(0, Math.round(curScroll)));
			w.render(context, mouseX, mouseY, delta, simplistic.isChecked());
		});
		simplistic.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xffffffff);
	}
	
	@Override
	public void renderBackground(DrawContext context)
	{
		super.renderBackground(context);
		context.fill(0, 0, width, height, 0x44000000);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
		context.drawTexture(simplistic.isChecked() ? new Identifier(Ultracraft.MOD_ID, "textures/gui/simplistic_bg.png") : OPTIONS_BACKGROUND_TEXTURE,
				width /2 - 125, 0, 0, 0.0f, 0.0f, 250, height, 32, 32);
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
