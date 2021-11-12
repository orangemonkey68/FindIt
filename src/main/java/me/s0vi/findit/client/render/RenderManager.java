package me.s0vi.findit.client.render;

import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class RenderManager {
    private final Set<BlockPos> blocksToRender = new HashSet<>();

    private int edgeColor;
    private int faceColor;

    public RenderManager(int edgeColor, int faceColor) {
        this.edgeColor = edgeColor;
        this.faceColor = faceColor;
    }

    public void renderBlock(BlockPos pos, ClientWorld world) {
        float red = ((edgeColor & 0xFF000000) >> 16) / 255.0F;
        float green = ((edgeColor & 0x00FF00) >> 8) / 255.0F;
        float blue = (edgeColor & 0x0000FF) / 255.0F;
        float alpha = 0.5F;

        DebugRenderer.drawBox(pos, 1, red, green, blue, alpha);
    }

    public void renderBlocks(ClientWorld world) {
        blocksToRender.forEach(pos -> renderBlock(pos, world));
    }

    public boolean hasBlocksToRender() {
        return !blocksToRender.isEmpty();
    }

    public boolean addBlockToRender(BlockPos pos) {
        return blocksToRender.add(pos);
    }

    public boolean removeBlockToRender(BlockPos pos) {
        return blocksToRender.remove(pos);
    }

    public void clearBlocksToRender() {
        blocksToRender.clear();
    }

    public void setEdgeColor(int edgeColor) {
        this.edgeColor = edgeColor;
    }

    public void setFaceColor(int faceColor) {
        this.faceColor = faceColor;
    }
}
