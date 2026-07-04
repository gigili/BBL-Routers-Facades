package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

@EventBusSubscriber(modid = RouterFacades.MOD_ID)
public class FacadeModelBake {
    @SubscribeEvent
    public static void onModifyBaking(ModelEvent.ModifyBakingResult e) {
        ModelBakery.BakingResult result = e.getBakingResult();
        Map<BlockState, BlockStateModel> models = result.blockStateModels();
        for (var entry : models.entrySet()) {
            var state = entry.getKey();
            var original = entry.getValue();
            if (original == null) continue;

            String ns = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();

            if ("routers".equals(ns) || "bbl_routers".equals(ns)) {
                // Our router block (Facade host)
                entry.setValue(FacadeModelWrapper.wrap(original));
            } else if ("sophisticatedbackpacks".equals(ns)) {
                // Don't wrap Sophisticated Backpacks blocks.
                // Their ClientBackpackShapeProvider checks model.getClass() and
                // warns about unknown wrapper types, and these blocks don't need
                // facade neighbor awareness anyway.
                continue;
            } else {
                // Wrap other block models with NeighborModelWrapper so they can
                // see facades as their camouflaged neighbor blocks.
                entry.setValue(new NeighborModelWrapper(original));
            }
        }
    }
}
