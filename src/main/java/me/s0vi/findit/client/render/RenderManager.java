package me.s0vi.findit.client.render;

import me.s0vi.findit.client.FindItClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix3f;

import java.util.*;

public class RenderManager {
    private final Map<UUID, BlockSet> blockSets = new HashMap<>();

    public void renderBlock(MatrixStack matrices, VertexConsumer consumer, BlockPos pos) {
        MinecraftClient.getInstance().execute(() -> {
            WorldRenderer.drawBox(matrices, consumer, new Box(pos), 1, 1, 1, 1);
        });
    }

    public void renderBlocks(MatrixStack matrixStack, VertexConsumer consumer) {
        blockSets.forEach((uuid, blockSet) -> {
            blockSet.blocks().forEach(blockPos -> {
                FindItClient.LOGGER.info("Rendering outline for block: {}", blockPos.toShortString());
//                FindItClient.LOGGER.info(matrixStack.toString());

                /*
                Reason translating by -blockPos worked with injecting into BlockEntityRenderer was because the matrices were translates
                by blockPos which cancelled them out

                EDIT: it doesn't "cancel it out" as the matrices don't start at the blockPos

                TODO: figure out how to get the matrices where I want them. Maybe WorldRenderer Line1041 is worth a look
                 */

                VertexConsumer vertexConsumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.LINES);
                matrixStack.push();
                matrixStack.translate(blockPos.getX() * -1, blockPos.getY() * -1, blockPos.getZ() * -1);
//                matrixStack.loadIdentity();

                renderBlock(matrixStack, vertexConsumer, blockPos);
            });
        });
    }

    public boolean containsPos(BlockPos pos) {
        boolean bool = false;

        for (BlockSet set : blockSets.values()) {
            bool = set.blocks().contains(pos);

            if (bool) return true;
        }

        return bool;
    }

    public boolean hasBlocksToRender() {
        return !blockSets.isEmpty();
    }

    public Set<UUID> getRenderedSearchUuids() {
        return blockSets.keySet();
    }

    public void addBlockToRender(UUID uuid, BlockPos pos) {
        blockSets.putIfAbsent(uuid, new BlockSet(new HashSet<>(), new Random().nextInt(0xFFFFFF)));
        blockSets.get(uuid).blocks().add(pos);
    }

    public void removeBlockToRender(UUID uuid, BlockPos pos) {
        blockSets.get(uuid).blocks().remove(pos);
    }

    public void clearBlocksToRender(UUID searchUuid) {
        blockSets.remove(searchUuid);
    }

    public void clearAllBlocksToRender() {
        blockSets.clear();
    }

    public Map<UUID, BlockSet> getBlockSets() {
        return blockSets;
    }

    record BlockSet(Set<BlockPos> blocks, int color){}
}
