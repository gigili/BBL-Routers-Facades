package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class FacadeAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RouterFacades.MOD_ID);

    public static final Supplier<AttachmentType<BlockState>> FACADE_STATE =
            ATTACHMENT_TYPES.register("facade_state", () ->
                    AttachmentType.<BlockState>builder(() -> null)
                            .serialize(BlockState.CODEC.fieldOf("state"), s -> s != null)
                            .build()
            );

    public static void register(IEventBus modBus) { ATTACHMENT_TYPES.register(modBus); }

    private FacadeAttachments() {}
}
