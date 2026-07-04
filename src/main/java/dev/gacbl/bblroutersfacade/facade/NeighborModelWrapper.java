package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NeighborModelWrapper extends DelegateBlockStateModel {

    public NeighborModelWrapper(BlockStateModel original) {
        super(original);
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<BlockStateModelPart> parts) {
        // Pass the wrapped view to the underlying model's collectParts.
        // This allows it to see our facades as their camouflaged states.
        super.collectParts(new FacadeLevelWrapper(view), pos, state, random, parts);
    }

    @Override
    public @Nullable Object createGeometryKey(@NotNull BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        return delegate.createGeometryKey(view, pos, state, random);
    }
}
