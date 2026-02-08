package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = RouterFacades.MOD_ID)
public class FacadeModelBake {
    @SubscribeEvent
    public static void onModifyBaking(ModelEvent.ModifyBakingResult e) {
        var models = e.getBakingResult().blockStateModels();
        for (var entry : models.entrySet()) {
            var state = entry.getKey();
            var original = entry.getValue();
            if (original == null) continue;

            String ns = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();

            if ("routers".equals(ns) || "bbl_routers".equals(ns)) {
                // Our router block (Facade host)
                entry.setValue(FacadeModelWrapper.wrap(original));
            } else if ("chipped".equals(ns) || "rechiseled".equals(ns) || "connectedglass".equals(ns)) {
                // CRITICAL FIX: Wrap Connected Glass models with NeighborModelWrapper
                // This allows them to see the facade as a connected block.
                entry.setValue(new NeighborModelWrapper(original));
            }
        }
    }
}
