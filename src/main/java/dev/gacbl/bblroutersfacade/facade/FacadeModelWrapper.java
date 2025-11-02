package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class FacadeModelWrapper extends BakedModelWrapper<BakedModel> {
    private final Function<BlockState, BakedModel> lookup;

    public FacadeModelWrapper(BakedModel original, Function<BlockState, BakedModel> lookup) {
        super(original);
        this.lookup = lookup;
    }

    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData existing) {
        var be = view.getBlockEntity(pos);
        if (be != null) {
            var camo = be.getData(FacadeAttachments.FACADE_STATE.get());
            if (camo != null) return existing.derive().with(FacadeModelData.FACADE, camo).build();
        }
        return existing;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, ModelData data, @Nullable RenderType layer) {
        var camo = data.get(FacadeModelData.FACADE);
        if (camo != null) {
            var model = lookup.apply(camo);
            return model.getQuads(camo, side, rand, ModelData.EMPTY, layer);
        }
        return super.getQuads(state, side, rand, data, layer);
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, ModelData data) {
        var camo = data.get(FacadeModelData.FACADE);
        if (camo != null) {
            var model = lookup.apply(camo);
            return model.getRenderTypes(camo, rand, ModelData.EMPTY);
        }
        return super.getRenderTypes(state, rand, data);
    }

    private static BakedModel bakedFor(BlockState s) {
        ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(s);
        return Minecraft.getInstance().getModelManager().getModel(mrl);
    }

    public static FacadeModelWrapper wrap(BakedModel original) {
        return new FacadeModelWrapper(original, FacadeModelWrapper::bakedFor);
    }
}
