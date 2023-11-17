package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.command.Commands;
import absolutelyaya.ultracraft.command.WhitelistCommand;
import absolutelyaya.ultracraft.components.player.IWingDataComponent;
import absolutelyaya.ultracraft.data.TerminalScreensaverManager;
import absolutelyaya.ultracraft.item.AbstractNailgunItem;
import absolutelyaya.ultracraft.item.MarksmanRevolverItem;
import absolutelyaya.ultracraft.item.SharpshooterRevolverItem;
import absolutelyaya.ultracraft.recipe.RecipeSerializers;
import absolutelyaya.ultracraft.data.UltraRecipeManager;
import absolutelyaya.ultracraft.registry.*;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Ultracraft implements ModInitializer
{
    public static final String MOD_ID = "ultracraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    static final String SUPPORTER_LIST = "https://raw.githubusercontent.com/absolutelyaya/absolutelyaya/main/cool-people.json";
    public static String VERSION;
	public static boolean DYN_LIGHTS;
	static int freezeTicks;
    static Map<UUID, Integer> supporterCache = new HashMap<>(), supporterCacheAdditions = new HashMap<>();
    
    @Override
    public void onInitialize()
    {
        ParticleRegistry.init();
        EntityRegistry.register();
        BlockRegistry.registerBlocks();
        FluidRegistry.register();
        BlockEntityRegistry.register();
        ItemRegistry.register();
        PacketRegistry.registerC2S();
        TagRegistry.register();
        SoundRegistry.register();
        GameruleRegistry.register();
        RecipeSerializers.register();
        CriteriaRegistry.register();
        StatusEffectRegistry.register();
        ScreenHandlerRegistry.registerServer();
        StatisticRegistry.register();
        new UltraRecipeManager();
        new TerminalScreensaverManager();
        
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Commands.register(dispatcher);
            WhitelistCommand.register(dispatcher);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickFreeze();
            ServerHitscanHandler.tickSchedule();
            supporterCache.putAll(supporterCacheAdditions);
            supporterCacheAdditions.clear();
            supporterCache.forEach((uuid, i) -> {
                if(i > 0)
                    supporterCache.put(uuid, i - 1);
            });
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            newPlayer.getInventory().main.forEach(stack -> {
                Item item = stack.getItem();
                if(item instanceof MarksmanRevolverItem marksman && marksman.getNbt(stack, "coins") < 4)
                    marksman.setNbt(stack, "coins", 4);
                else if (item instanceof SharpshooterRevolverItem sharpshooter && sharpshooter.getNbt(stack, "charges") < 3)
                    sharpshooter.setNbt(stack, "charges", 3);
                else if (item instanceof AbstractNailgunItem nailgun && nailgun.getNbt(stack, "nails") < 100)
                    nailgun.setNbt(stack, "nails", 100);
            });
        });
        
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((serverPlayer, lastWorld, newWorld) -> GameruleRegistry.syncAll(serverPlayer));
        ServerPlayConnectionEvents.JOIN.register((networkHandler, sender, server) -> {
            ServerPlayerEntity player = networkHandler.player;
            GameruleRegistry.syncAll(player);
            UltraRecipeManager.sync(player);
            GameruleRegistry.Setting hivel = player.getWorld().getGameRules().get(GameruleRegistry.HIVEL_MODE).get();
            if(!hivel.equals(GameruleRegistry.Setting.FREE))
            {
                IWingDataComponent wings = UltraComponents.WING_DATA.get(player);
                wings.setVisible(hivel.equals(GameruleRegistry.Setting.FORCE_ON));
                wings.sync();
            }
        });
        ServerPlayConnectionEvents.INIT.register(((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            //detect first spawn; probably a scuffed way to do this, but hey, it works :3
            if(player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0)
                if(player.getWorld().getGameRules().getBoolean(GameruleRegistry.START_WITH_PIERCER))
                    player.giveItemStack(ItemRegistry.PIERCE_REVOLVER.getDefaultStack());
        }));
        
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> VERSION = modContainer.getMetadata().getVersion().getFriendlyString());
        FabricLoader.getInstance().getModContainer("lambdynlights").ifPresent(container -> DYN_LIGHTS = true);
        LOGGER.info("Ultracraft initialized.");
    }
    
    public static boolean isTimeFrozen()
    {
        return freezeTicks > 0;
    }
    
    public static void freeze(ServerPlayerEntity player, int ticks)
    {
        if(player != null)
        {
            boolean freezeDisabled = player.getServer().isRemote() &&
                                             player.getWorld().getGameRules().get(GameruleRegistry.TIME_STOP).get().equals(GameruleRegistry.Setting.FORCE_OFF);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(ticks);
            buf.writeBoolean(freezeDisabled);
            if(freezeDisabled)
            {
                ServerPlayNetworking.send(player, PacketRegistry.FREEZE_PACKET_ID, buf);
                return;
            }
            for (ServerPlayerEntity p : ((ServerWorld)player.getWorld()).getPlayers())
                ServerPlayNetworking.send(p, PacketRegistry.FREEZE_PACKET_ID, buf);
        }
        freezeTicks += ticks;
        LOGGER.info("Stopping time for " + ticks + " ticks.");
    }
    
    public static void freeze(ServerWorld world, int ticks)
    {
        if(world != null)
        {
            boolean freezeDisabled = world.getServer().isRemote() &&
                                             world.getGameRules().get(GameruleRegistry.TIME_STOP).get().equals(GameruleRegistry.Setting.FORCE_OFF);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(ticks);
            buf.writeBoolean(freezeDisabled);
            if(freezeDisabled)
                return;
            for (ServerPlayerEntity p : world.getPlayers())
                ServerPlayNetworking.send(p, PacketRegistry.FREEZE_PACKET_ID, buf);
        }
        freezeTicks += ticks;
        LOGGER.info("Stopping time for " + ticks + " ticks.");
    }
    
    public static void cancelFreeze(ServerWorld world)
    {
        freezeTicks = 0;
        LOGGER.info("Forcefully Unstopped time.");
    }
    
    public static void tickFreeze()
    {
        if(freezeTicks > 0)
            freezeTicks--;
    }
    
    public static boolean checkSupporter(UUID uuid, boolean client)
    {
        int i;
        if(supporterCache.containsKey(uuid) && (i = supporterCache.get(uuid)) != 0)
            return i == -1;
        boolean supporter = false;
        try
        {
            URL url = new URL(SUPPORTER_LIST);
            JsonObject json = JsonHelper.deserialize(new InputStreamReader(url.openStream()));
            supporter = JsonHelper.hasElement(json, uuid.toString());
            if(supporter && client)
            {
                Ultracraft.LOGGER.info("[ULTRACRAFT] " + uuid + " has been verified as a Supporter!");
                supporterCacheAdditions.put(uuid, -1);
            }
            else
                supporterCacheAdditions.put(uuid, 600); //if not a supporter, only check again after 30 seconds
        }
        catch (IOException e)
        {
            Ultracraft.LOGGER.error("[ULTRACRAFT] Failed to fetch Supporters.", e);
        }
        return supporter;
    }
    
    public static void positionKnuckleBlast(MatrixStack matrices, float equipProgress, boolean flipped)
    {
        if (!(MinecraftClient.getInstance().player instanceof LivingEntityAccessor living))
            return;
        float blast = living.getKnuckleBlastProgress(MinecraftClient.getInstance().getTickDelta());
        int flip = flipped ? 1 : -1;
        float swing = MathHelper.sqrt(blast);
        float x = 0.1f * Math.min(MathHelper.sin(swing * (float)Math.PI + 0.25f), 0.5f) * 2f;
        float y = -0.05f * (MathHelper.sin(swing * (float)Math.PI + 0.25f));
        float z = 0.4f * Math.min(MathHelper.sin(blast * (float)Math.PI + 0.25f), 0.5f) * 2f;
        matrices.translate(flip * (x + 0.64000005f), y - 0.6f + equipProgress * -0.6f, z - 0.71999997f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * 45.0f));
        float roll = MathHelper.sin(blast * blast * (float)Math.PI) / 2f;
        float yaw = MathHelper.sin(swing * (float)Math.PI) / 1.5f;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * yaw * 30.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flip * roll * -20.0f));
        matrices.translate(flip * -1.0f, 3.6f, 3.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flip * 120.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * -135.0f));
        matrices.translate(flip * 5.6f, 0.0f, 0.0f);
    }
}
