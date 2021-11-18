package me.s0vi.findit;

import me.s0vi.findit.network.NetworkManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FindIt implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("FindIt");
    public static final NetworkManager SERVER_NETWORK_MANAGER = new NetworkManager();
    public static MinecraftServer SERVER;
//    private final

    @Override
    public void onInitialize() {
        Timer initTimer = new Timer().start();
        LOGGER.info("It's around here somewhere...");
        LOGGER.info("Registering Config");
        AutoConfig.register(FindItConfig.class, GsonConfigSerializer::new);
        LOGGER.info("Config registered");
        LOGGER.info("Initializing Networking");
        SERVER_NETWORK_MANAGER.initNetworkingServer();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER = server;
        });

        LOGGER.info("Init took {} nanoseconds", initTimer.stop().getTimeNanos());
    }

    public static FindItConfig getConfig() {
        return AutoConfig.getConfigHolder(FindItConfig.class).getConfig();
    }
}
