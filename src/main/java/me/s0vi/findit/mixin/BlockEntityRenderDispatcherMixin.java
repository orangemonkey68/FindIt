package me.s0vi.findit.mixin;

import me.s0vi.findit.client.FindItClient;
import me.s0vi.findit.client.render.RenderManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(at = @At( "TAIL"), method  = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V")
    private static <T extends BlockEntity> void injectRenderingCallback(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        BlockPos pos = blockEntity.getPos();
        RenderManager renderManager = FindItClient.INSTANCE.getRenderManager();

        if(renderManager.hasBlocksToRender()) {
            if(renderManager.containsPos(pos)) {
                FindItClient.LOGGER.info("Rendering Block");
//
//                FindItClient.LOGGER.info("BLOCK ENTITY RENDERER");
//                FindItClient.LOGGER.info("BEFORE TRANSLATE");
//                FindItClient.LOGGER.info(matrices.peek().getModel().toString());
//                FindItClient.LOGGER.info(matrices.peek().getNormal().toString());
//                matrices.translate(pos.getX() * -1, pos.getY() * -1, pos.getZ() * -1);
//                FindItClient.LOGGER.info("AFTER TRANSLATE");
//                FindItClient.LOGGER.info(matrices.peek().getModel().toString());
//                FindItClient.LOGGER.info(matrices.peek().getNormal().toString());
//                renderManager.renderBlock(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), pos);
            }
        }
    }
}
