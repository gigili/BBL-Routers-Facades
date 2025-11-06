package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class NeighborModelWrapper extends BakedModelWrapper<BakedModel> {
    private final BakedModel original;

    public NeighborModelWrapper(BakedModel original) {
        super(original);
        this.original = original;
    }

    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData existing) {
        var wrappedView = new FacadeLevelWrapper(view);
        return original.getModelData(wrappedView, pos, state, existing);
    }
}
