package me.ethanbrews.rituals.block;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.EnchantmentRituals;
import me.ethanbrews.rituals.Ritual;
import me.ethanbrews.rituals.RitualException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EnchantPedestalBlock extends Block implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    public EnchantPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof EnchantPedestalBlockEntity pedestalBlockEntity) {
                pedestalBlockEntity.drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.or(
                Block.box(6, 0, 6, 10, 10.08, 10),
                Block.box(0, 0, 0, 16, 1, 16),
                Block.box(3, 11.08, 3, 13, 15.08, 13)
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EnchantPedestalBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Only tick on the server side
        if (level.isClientSide) {
            return null;
        }

        // Make sure the type matches your block entity type
        return type == EnchantmentRituals.ENCHANTMENT_PEDESTAL_BE.get() ?
                (lvl, pos, st, be) -> ((EnchantPedestalBlockEntity) be).tick(lvl, pos, st) :
                null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof EnchantPedestalBlockEntity pedestalEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        ItemStack storedItem = pedestalEntity.getItem();

        if (heldItem.isEmpty() && player.isCrouching()) {
            try {
                Ritual.startRitual(pedestalEntity);
            } catch (RitualException e) {
                LOGGER.info("Could not start ritual", e);
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        }

        // Item add or retrieve logic
        if (!storedItem.isEmpty()) {
            if (!player.getInventory().add(storedItem)) {
                player.drop(storedItem, false);
            }
            pedestalEntity.setItem(ItemStack.EMPTY);
            pedestalEntity.setChanged();
            return InteractionResult.SUCCESS;
        }

        if (!heldItem.isEmpty()) {
            ItemStack toStore = heldItem.split(1);
            pedestalEntity.setItem(toStore);
            pedestalEntity.setChanged();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
