package dev.gacbl.bblroutersfacade.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;


public final class ClientHandlers {
    public static void handleRefresh(FacadePayloads.FacadeRefresh msg) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.level == null || mc.levelRenderer == null) return;

            BlockPos pos = msg.pos();

            var be = mc.level.getBlockEntity(pos);
            if (be != null) {
                be.requestModelDataUpdate();
            }

            BlockState bs = mc.level.getBlockState(pos);
            mc.levelRenderer.setBlockDirty(pos, bs, bs);

            int sx = pos.getX() >> 4;
            int sy = pos.getY() >> 4;
            int sz = pos.getZ() >> 4;
            mc.levelRenderer.setSectionDirtyWithNeighbors(sx, sy, sz);

            mc.level.sendBlockUpdated(pos, bs, bs, 3);
        });
    }
}
