package dev.gacbl.bblroutersfacade.network;

import dev.gacbl.bblroutersfacade.facade.FacadeAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;


public final class ClientHandlers {
    public static void handleRefresh(FacadePayloads.FacadeRefresh msg) {
        var mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.level == null) return;
            var be = mc.level.getBlockEntity(msg.pos());
            if (be != null) {
                if (msg.state() == null) be.removeData(FacadeAttachments.FACADE_STATE.get());
                else be.setData(FacadeAttachments.FACADE_STATE.get(), msg.state());
                be.requestModelDataUpdate();
            }
            var bs = mc.level.getBlockState(msg.pos());
            mc.level.sendBlockUpdated(msg.pos(), bs, bs, 3);

            // Also update neighbors for connection rendering
            updateNeighborModels(mc, msg.pos());
        });
    }

    private static void updateNeighborModels(Minecraft mc, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if(mc.level != null) {
                var neighborBe = mc.level.getBlockEntity(neighborPos);
                if (neighborBe != null) {
                    neighborBe.requestModelDataUpdate();
                    var bs = mc.level.getBlockState(neighborPos);
                    mc.level.sendBlockUpdated(neighborPos, bs, bs, 2);
                }
            }
        }
    }
}
