package dev.gacbl.bblroutersfacade.network;

import dev.gacbl.bblroutersfacade.facade.FacadeOps;
import dev.gacbl.bblroutersfacade.item.FacadeApplicatorItem;
import dev.gacbl.bblroutersfacade.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerHandlers {
    public static void handleApplyRequest(FacadePayloads.FacadeApplyRequest msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player == null) return;

            // Re-verify distance for security
            if (player.distanceToSqr(msg.pos().getX() + 0.5, msg.pos().getY() + 0.5, msg.pos().getZ() + 0.5) > 64)
                return;

            // Check if player has the applicator item
            if (player.getMainHandItem().getItem() != ModItems.FACADE_APPLICATOR.get() &&
                    player.getOffhandItem().getItem() != ModItems.FACADE_APPLICATOR.get()) return;

            // Verify it's a router block
            BlockState targetState = player.level().getBlockState(msg.pos());
            if (!FacadeApplicatorItem.isRouterBlock(targetState)) return;

            if (msg.state() == null) {
                FacadeOps.clear(player.level(), msg.pos());
                player.displayClientMessage(Component.translatable("item.bblroutersfacade.facade_applicator.actions.cleared_facade"), true);
            } else {
                FacadeOps.apply(player.level(), msg.pos(), msg.state());
                player.displayClientMessage(Component.translatable("item.bblroutersfacade.facade_applicator.actions.applied_facade", msg.state().getBlock().getName()), true);
            }
        });
    }
}
