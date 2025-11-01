package dev.gacbl.bblroutersfacade.facade;


import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

public final class FacadeCapability {
    public static final BlockCapability<IFacadeData, Void> CAP =
            BlockCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(RouterFacades.MOD_ID, "facade"),
                    IFacadeData.class
            );
    private FacadeCapability() {}
}
