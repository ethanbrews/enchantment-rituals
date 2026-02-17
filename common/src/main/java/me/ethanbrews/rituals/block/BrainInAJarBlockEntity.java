package me.ethanbrews.rituals.block;

import me.ethanbrews.rituals.util.XpHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrainInAJarBlockEntity extends BlockEntity {
    private int storedXp;
    private final int MAX_XP = XpHelper.levelsToXp(30);

    public BrainInAJarBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(EnchantmentRitualBlocks.BRAIN_IN_A_JAR_BLOCK_BE.get(), blockPos, blockState);
        storedXp = MAX_XP / 2;
    }

    public int getStoredXp() {
        return storedXp;
    }

    public int getMaxXp() {
        return MAX_XP;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setStoredXp(int xp) {
        this.storedXp = Math.min(Math.max(0, xp), MAX_XP);
        updateClients();
    }

    public float getFillPercent() {
        return (float) storedXp / MAX_XP;
    }

    public int addXp(int amount) {
        var xpNow = storedXp;
        setStoredXp(storedXp + amount);
        updateClients();
        return storedXp - xpNow;
    }

    public int removeXp(int amount) {
        if (storedXp >= amount) {
            setStoredXp(storedXp - amount);
            updateClients();
            return amount;
        } else {
            var actualXp = storedXp;
            setStoredXp(0);
            updateClients();
            return actualXp;
        }
    }

    private void updateClients() {
        setChanged();
        // Sync to client
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedXp = tag.getInt("storedXp");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("storedXp", storedXp);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
}
