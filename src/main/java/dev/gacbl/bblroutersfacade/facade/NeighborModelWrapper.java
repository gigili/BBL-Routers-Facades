package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NeighborModelWrapper extends DelegateBlockStateModel {

    public NeighborModelWrapper(BlockStateModel original) {
        super(original);
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<BlockModelPart> parts) {
        // Pass the wrapped view to the underlying model's collectParts.
        // This allows it to see our facades as their camouflaged states.
        super.collectParts(new FacadeLevelWrapper(view), pos, state, random, parts);
    }
}
