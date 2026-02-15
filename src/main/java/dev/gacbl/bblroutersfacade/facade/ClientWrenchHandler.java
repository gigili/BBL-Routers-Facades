package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import dev.gacbl.bblroutersfacade.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = RouterFacades.MOD_ID, value = Dist.CLIENT)
public class ClientWrenchHandler {
    private static boolean wasHoldingWrench = false;
    private static BlockPos lastPlayerPos = null;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        boolean isHoldingWrench = isHoldingWrench(player.getMainHandItem()) || isHoldingWrench(player.getOffhandItem());
        BlockPos currentPos = player.blockPosition();

        if (isHoldingWrench != wasHoldingWrench) {
            // Wrench state changed, refresh nearby chunks
            refreshNearbyChunks(mc, currentPos);
            wasHoldingWrench = isHoldingWrench;
            lastPlayerPos = currentPos;
        } else if (isHoldingWrench && (lastPlayerPos == null || currentPos.distSqr(lastPlayerPos) > 16)) {
            // Player moved significantly while holding wrench, refresh nearby chunks
            refreshNearbyChunks(mc, currentPos);
            lastPlayerPos = currentPos;
        }
    }

    private static boolean isHoldingWrench(ItemStack stack) {
        return stack.is(ModItems.FACADE_APPLICATOR.get());
    }

    private static void refreshNearbyChunks(Minecraft mc, BlockPos pos) {
        int r = FacadeModelWrapper.RENDER_RANGE + 16;
        mc.levelRenderer.setBlocksDirty(
                pos.getX() - r, pos.getY() - r, pos.getZ() - r,
                pos.getX() + r, pos.getY() + r, pos.getZ() + r
        );
    }
}
