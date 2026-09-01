package dev.gacbl.bblroutersfacade.mixin;

import dev.gacbl.bblroutersfacade.facade.FacadeLevelWrapper;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    /**
     * Lets models see a facade's camouflaged state when they inspect neighboring
     * blocks, without replacing the model object or changing its concrete type.
     */
    @ModifyArg(
            method = "tesselateBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;collectParts(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Ljava/util/List;)V"
            ),
            index = 0
    )
    private BlockAndTintGetter bblRoutersFacade$wrapLevel(BlockAndTintGetter level) {
        return level instanceof FacadeLevelWrapper ? level : new FacadeLevelWrapper(level);
    }
}
