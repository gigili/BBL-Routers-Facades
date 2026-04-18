package dev.gacbl.bblroutersfacade.mixin;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class BlockMixin {
    /**
     * Overrides getAppearance to spoof the facade's block state for neighbor connection checks.
     * This allows vanilla-style blocks (fences, walls, etc.) that support getAppearance
     * to "see" the facade instead of the router block.
     */
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String ns = id.getNamespace();
        if (ns.equals("routers") || ns.equals("bbl_routers")) {
            var be = level.getBlockEntity(pos);
            if (be != null) {
                BlockState facade = be.getData(dev.gacbl.bblroutersfacade.facade.FacadeAttachments.FACADE_STATE.get());
                if (facade != null) {
                    return facade;
                }
            }
        }
        return state;
    }
}
