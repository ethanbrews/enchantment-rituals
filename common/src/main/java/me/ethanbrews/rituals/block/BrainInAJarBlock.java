package me.ethanbrews.rituals.block;

import me.ethanbrews.rituals.util.XpHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BrainInAJarBlock extends GlassJarBlock implements EntityBlock {

    public BrainInAJarBlock() {
        super();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BrainInAJarBlockEntity brainEntity)) {
            return InteractionResult.PASS;
        }

        if (player.isCrouching()) {
            // Withdraw XP to complete current level (or get one full level if already at exact level)
            int currentTotal = XpHelper.getTotalXP(player);
            int nextLevelTotal = XpHelper.levelsToXp(player.experienceLevel + 1);
            int xpNeeded = nextLevelTotal - currentTotal;

            int withdrawn = brainEntity.removeXp(xpNeeded);
            if (withdrawn > 0) {
                player.giveExperiencePoints(withdrawn);
            }
        } else {
            // Deposit XP from current level
            int currentLevelStart = XpHelper.levelsToXp(player.experienceLevel);
            int currentTotal = XpHelper.getTotalXP(player);
            int xpInCurrentLevel = currentTotal - currentLevelStart;

            // If at exact level (no progress), deposit the entire previous level instead
            if (xpInCurrentLevel == 0 && player.experienceLevel > 0) {
                int previousLevelStart = XpHelper.levelsToXp(player.experienceLevel - 1);
                xpInCurrentLevel = currentTotal - previousLevelStart; // Full previous level
            }

            if (xpInCurrentLevel > 0) {
                int deposited = brainEntity.addXp(xpInCurrentLevel);
                if (deposited > 0) {
                    player.giveExperiencePoints(-deposited);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BrainInAJarBlockEntity(blockPos, blockState);
    }
}
