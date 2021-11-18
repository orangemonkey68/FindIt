package me.s0vi.findit.client;

import me.s0vi.findit.Timer;
import me.s0vi.findit.client.render.RenderManager;
import me.s0vi.findit.client.search.SearchResultManager;
import me.s0vi.findit.network.NetworkManager;
import me.s0vi.findit.search.Search;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class FindItClient implements ClientModInitializer {
    public final static Logger LOGGER = LogManager.getLogger("FindItClient");
    public static final NetworkManager CLIENT_NETWORK_MANAGER = new NetworkManager();
    public static FindItClient INSTANCE;
    private final RenderManager renderManager;

    private final SearchResultManager searchResultManager;
    //TODO: Update RenderManager color when config changes
    public FindItClient() {
        renderManager = new RenderManager();
        searchResultManager = new SearchResultManager(renderManager);
        INSTANCE = this;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello from FindIt!");

        CLIENT_NETWORK_MANAGER.initNetworkingClient();

        WorldRenderEvents.END.register(context -> {
            if(renderManager.hasBlocksToRender()) {
                LOGGER.debug("Starting block rendering");
                Timer timer = new Timer().start();
                renderManager.renderBlocks(context.matrixStack(), new BufferBuilder(8));
                LOGGER.debug("Block rendering done in {} ns", timer.stop().getTimeNanos());
            }
        });




        KeyBinding startSearchBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.findit.start",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.findit.keys"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (startSearchBind.wasPressed()) {
                Search s = new Search(8, new Identifier("minecraft", "cobblestone"), null);

                CLIENT_NETWORK_MANAGER.sendSearchToServer(s);
            }
        });

        KeyBinding resetSearchBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.findit.reset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.findit.keys"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (resetSearchBind.wasPressed()) {
                renderManager.clearAllBlocksToRender();
            }
        });
    }

    public RenderManager getRenderManager() {
        return renderManager;
    }

    public SearchResultManager getSearchResultManager() {
        return searchResultManager;
    }
}
