package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.command.UltracraftCommand;
import absolutelyaya.ultracraft.item.MarksmanRevolverItem;
import absolutelyaya.ultracraft.item.SharpshooterRevolverItem;
import absolutelyaya.ultracraft.recipe.UltraRecipeManager;
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
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
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
    public static final UltraRecipeManager RECIPE_MANAGER = new UltraRecipeManager();
    
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
        
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            UltracraftCommand.register(dispatcher);
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
                if(item instanceof MarksmanRevolverItem marksman && marksman.getCoins(stack) < 4)
                    marksman.setCoins(stack, 4);
                else if (item instanceof SharpshooterRevolverItem sharpshooter && sharpshooter.getCharges(stack) < 3)
                    sharpshooter.setCharges(stack, 3);
            });
        });
        
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((serverPlayer, lastWorld, newWorld) -> GameruleRegistry.syncAll(serverPlayer));
        ServerPlayConnectionEvents.JOIN.register((networkHandler, sender, server) -> {
            GameruleRegistry.syncAll(networkHandler.player);
            UltraRecipeManager.sync(networkHandler.player);
        });
        
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
}
