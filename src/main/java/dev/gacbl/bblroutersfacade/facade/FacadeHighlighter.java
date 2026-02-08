package dev.gacbl.bblroutersfacade.facade;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.gacbl.bblroutersfacade.RouterFacades;
import dev.gacbl.bblroutersfacade.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = RouterFacades.MOD_ID, value = Dist.CLIENT)
public final class FacadeHighlighter {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterEntities e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!isHoldingApplicator(mc.player.getMainHandItem()) && !isHoldingApplicator(mc.player.getOffhandItem()))
            return;

        PoseStack pose = e.getPoseStack();
        Vec3 cam = mc.getEntityRenderDispatcher().camera.position();
        var buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderTypes.lines());

        pose.pushPose();

        int r = 32;
        BlockPos c = mc.player.blockPosition();
        BlockPos.betweenClosedStream(c.offset(-r, -r, -r), c.offset(r, r, r)).forEach(p -> {
            var be = mc.level.getBlockEntity(p);
            if (be == null) return;
            if (be.getData(FacadeAttachments.FACADE_STATE.get()) == null) return;

            pose.pushPose();
            pose.translate(p.getX() - cam.x, p.getY() - cam.y, p.getZ() - cam.z);
            var box = new AABB(0, 0, 0, 1, 1, 1).inflate(0.002);
            ShapeRenderer.renderShape(pose, lines, Shapes.create(box), 0, 0, 0, 0x3399FF, 1.0f);
            pose.popPose();
        });

        pose.popPose();
        buffers.endBatch(RenderTypes.lines());
    }

    private static boolean isHoldingApplicator(ItemStack stack) {
        return stack.is(ModItems.FACADE_APPLICATOR.get());
    }
}
