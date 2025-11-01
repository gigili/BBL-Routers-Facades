package dev.gacbl.bblroutersfacadeaddon.facade;

import dev.gacbl.bblroutersfacadeaddon.RouterFacades;
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
                if ("routers".equals(ns) || "bbl_routers".equals(ns)) {
                    var original = models.get(mrl);
                    if (original != null) {
                        models.put(mrl, FacadeModelWrapper.wrap(original));
                    }
                }
            }
        }
    }
}
