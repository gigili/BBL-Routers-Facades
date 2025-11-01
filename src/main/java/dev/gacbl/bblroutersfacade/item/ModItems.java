package dev.gacbl.bblroutersfacade.item;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(RouterFacades.MOD_ID);

    public static final Supplier<Item> FACADE_APPLICATOR =
            ITEMS.register("facade_applicator",
                    () -> new FacadeApplicatorItem(new Item.Properties().stacksTo(1))
            );

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
