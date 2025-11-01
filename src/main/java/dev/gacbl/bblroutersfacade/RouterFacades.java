package dev.gacbl.bblroutersfacade;

import dev.gacbl.bblroutersfacade.facade.FacadeAttachments;
import dev.gacbl.bblroutersfacade.item.ModCreativeModTabs;
import dev.gacbl.bblroutersfacade.item.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(RouterFacades.MOD_ID)
public class RouterFacades {
    public static final String MOD_ID = "bblroutersfacade";
    public static final String TARGET_NS = "routers";

    public RouterFacades(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        ModCreativeModTabs.register(modEventBus);
        ModItems.register(modEventBus);
        FacadeAttachments.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
