package me.s0vi.findit.client;

import me.s0vi.findit.client.render.RenderManager;
import me.s0vi.findit.client.search.Search;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class FindItClient implements ClientModInitializer {
    public final static Logger LOGGER = LogManager.getLogger("FindIt");

    final RenderManager renderManager;
    Search currentSearch = null;


    //TODO: Update RenderManager color when config changes
    public FindItClient() {
        renderManager = new RenderManager(0xe35959, 0xe35959);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello from FindIt!");

        //Rendering Callback
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            LOGGER.debug("Register callback started");
            if(currentSearch != null && !currentSearch.hasResults() && client.world != null && client.player != null) {
                LOGGER.debug("Starting search");
                Timer timer = new Timer().start();
                currentSearch
                        .findInventories(client.world, client.player.getBlockPos())
                        .whenComplete((results, throwable) -> {
                            LOGGER.debug("Search took {} ns", timer.getTimeNanos());
                            results.blockSet().forEach(renderManager::addBlockToRender);
                        })
                        .exceptionally(err -> {
                            LOGGER.error(err);
                            return new Search.Results(currentSearch, Set.of());
                        });
            }
        });
        WorldRenderEvents.END.register(listener -> {
            if(renderManager.hasBlocksToRender()) {
                LOGGER.debug("Starting block rendering");
                Timer timer = new Timer().start();
                renderManager.renderBlocks(listener.world());
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

                currentSearch = new Search(30, new Identifier("minecraft:cobblestone"), null);
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
                renderManager.clearBlocksToRender();
                currentSearch = null;
            }
        });
    }
}
