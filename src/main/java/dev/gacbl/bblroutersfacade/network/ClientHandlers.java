package dev.gacbl.bblroutersfacade.network;

import dev.gacbl.bblroutersfacade.facade.FacadeAttachments;
import net.minecraft.client.Minecraft;


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
        });
    }
}
