package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = RouterFacades.MOD_ID)
public class FacadeModelBake {
    @SubscribeEvent
    public static void onModifyBaking(ModelEvent.ModifyBakingResult e) {
        var models = e.getModels();
        for (var key : models.keySet()) {
            if (key instanceof ModelResourceLocation mrl) {
                String ns = mrl.id().getNamespace();
                var original = models.get(mrl);
                if (original == null) continue;

                if ("routers".equals(ns) || "bbl_routers".equals(ns)) {
                    // Our router block (Facade host)
                    models.put(mrl, FacadeModelWrapper.wrap(original));
                } else if ("chipped".equals(ns) || "rechiseled".equals(ns) || "connectedglass".equals(ns)) {
                    // CRITICAL FIX: Wrap Connected Glass models with NeighborModelWrapper
                    // This allows them to see the facade as a connected block.
                    models.put(mrl, new NeighborModelWrapper(original));
                }
            }
        }
    }
}
