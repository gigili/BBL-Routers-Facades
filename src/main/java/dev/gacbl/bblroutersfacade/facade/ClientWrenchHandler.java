package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import dev.gacbl.bblroutersfacade.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = RouterFacades.MOD_ID, value = Dist.CLIENT)
public class ClientWrenchHandler {
    private static boolean wasHoldingWrench = false;
    private static BlockPos lastWrenchPos = null;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player != event.getEntity()) return;

        boolean isHoldingWrench = isHoldingApplicator(player);
        BlockPos currentPos = player.blockPosition();

        if (isHoldingWrench != wasHoldingWrench) {
            refreshChunks(mc, currentPos);
            wasHoldingWrench = isHoldingWrench;
            lastWrenchPos = currentPos;
        } else if (isHoldingWrench && (lastWrenchPos == null || lastWrenchPos.distSqr(currentPos) > 16)) {
            refreshChunks(mc, currentPos);
            lastWrenchPos = currentPos;
        }
    }

    private static boolean isHoldingApplicator(LocalPlayer player) {
        return isApplicator(player.getMainHandItem()) || isApplicator(player.getOffhandItem());
    }

    private static boolean isApplicator(ItemStack stack) {
        return stack.is(ModItems.FACADE_APPLICATOR.get());
    }

    private static void refreshChunks(Minecraft mc, BlockPos pos) {
        if (mc.levelRenderer != null) {
            int r = 24; // Slightly larger range to cover the 12-block transparency radius comfortably
            mc.levelRenderer.setBlocksDirty(
                    pos.getX() - r, pos.getY() - r, pos.getZ() - r,
                    pos.getX() + r, pos.getY() + r, pos.getZ() + r
            );
        }
    }
}
