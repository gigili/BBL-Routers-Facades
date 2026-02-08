package dev.gacbl.bblroutersfacade.item;

import dev.gacbl.bblroutersfacade.RouterFacades;
import dev.gacbl.bblroutersfacade.network.FacadePayloads;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class FacadeApplicatorItem extends Item {
    private static final String TAG_STATE = "picked_state";


    public FacadeApplicatorItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public @NonNull InteractionResult use(Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            clearPicked(stack);
            player.displayClientMessage(Component.translatable("item.bblroutersfacade.facade_applicator.actions.cleared_picked_facade"), true);
            return InteractionResult.CONSUME;
        }

        return super.use(level, player, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean sneaking = player.isShiftKeyDown();
        boolean isRouter = isRouterBlock(state);

        // Right-click router (sneak) -> APPLY stored pick
        if (isRouter && sneaking) {
            if (level.isClientSide()) {
                BlockState picked = getPickedState(stack);
                ClientSide.sendApplyRequest(pos, picked);
            }
            return InteractionResult.SUCCESS;
        }

        // Right-click any non-router block (no sneak) -> PICK source
        if (!sneaking && !isRouter) {
            var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (id != null) {
                if (!level.isClientSide()) {
                    setPickedState(stack, state);
                    var key = Util.makeDescriptionId("block", id);
                    player.displayClientMessage(Component.translatable("item.bblroutersfacade.facade_applicator.actions.picked_facade", Component.translatable(key)), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }


    public static boolean isRouterBlock(BlockState s) {
        var key = BuiltInRegistries.BLOCK.getKey(s.getBlock());
        return RouterFacades.TARGET_NS.equals(key.getNamespace());
    }

    private static void setPickedState(ItemStack stack, BlockState state) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        var enc = (CompoundTag) BlockState.CODEC.encodeStart(NbtOps.INSTANCE, state).getOrThrow();
        tag.put(TAG_STATE, enc);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static @org.jetbrains.annotations.Nullable BlockState getPickedState(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var root = data.copyTag();
        if (!root.contains(TAG_STATE)) return null;
        var res = BlockState.CODEC.parse(NbtOps.INSTANCE, root.get(TAG_STATE));
        return res.result().orElse(null);
    }

    private static void clearPicked(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        var root = data.copyTag();
        root.remove(TAG_STATE);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static class ClientSide {
        private static void sendApplyRequest(BlockPos pos, @Nullable BlockState picked) {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new FacadePayloads.FacadeApplyRequest(pos, picked));
        }
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context, @NonNull TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, @NonNull TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.bblroutersfacade.facade_applicator.right_click"));
        tooltipAdder.accept(Component.translatable("item.bblroutersfacade.facade_applicator.shift_right_click"));
        tooltipAdder.accept(Component.translatable("item.bblroutersfacade.facade_applicator.right_click_air"));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
