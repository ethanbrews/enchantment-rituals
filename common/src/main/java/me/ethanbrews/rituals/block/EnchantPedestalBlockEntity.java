package me.ethanbrews.rituals.block;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.ritual.Ritual;
import me.ethanbrews.rituals.ritual.RitualException;
import me.ethanbrews.rituals.ritual.RitualEventHandler;
import me.ethanbrews.rituals.ritual.RitualTier;
import me.ethanbrews.rituals.util.SingleItemContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EnchantPedestalBlockEntity extends BlockEntity implements RitualEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SingleItemContainer container;
    private Ritual ritual = null;
    private final RitualTier tier;

    // Client Side synced
    private boolean _isConsuming = false;
    private boolean _isRitualController = false;
    private boolean _isRitualParticipant = false;
    private BlockPos _controllerPos = null;

    public EnchantPedestalBlockEntity(BlockPos blockPos, BlockState blockState, RitualTier tier) {
        super(EnchantmentRitualBlocks.ENCHANTMENT_PEDESTAL_BE.get(), blockPos, blockState);
        this.tier = tier;
        container = new SingleItemContainer(this::onInventoryChanged);
    }

    public RitualTier getTier() {
        return this.tier;
    }

    public boolean isConsumingItem() {
        return _isConsuming;
    }

    public boolean isRitualController() {
        return _isRitualController;
    }

    public boolean isRitualParticipant() {
        return _isRitualParticipant;
    }

    public Ritual getRitual() {
        return ritual;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (ritual != null && ritual.getController() == this) {
            ritual.tick();
        }
    }

    /**
     * Begin a ritual, this should be called by a [Ritual] object.
     * @param ritual the new ritual
     * @throws RitualException
     */
    public void beginRitual(Ritual ritual) throws RitualException {
        if (isRitualParticipant()) {
            throw new RitualException("Pedestal is participating in an ongoing ritual");
        }
        this.ritual = ritual;
        this.ritual.getEventDispatcher().subscribe(this);
        this._isRitualController = this.ritual.getController() == this;
        this._isRitualParticipant = true;
        notifyClients();
    }

    private void endRitual() {
        if (this.ritual == null) { return; }
        this._isConsuming = false;
        this._isRitualController = false;
        this.ritual.getEventDispatcher().unsubscribe(this);
        this.ritual = null;
        this._isRitualParticipant = false;
        notifyClients();
    }

    private void onInventoryChanged() {
        notifyClients();
        if (this.level != null && !(this.level.isClientSide) && this._isConsuming) {
            this.ritual.notifyFailure(this);
        }
    }

    public void drops() {
        Containers.dropContents(this.getLevel(), this.getBlockPos(), this.container.getContainer());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        container.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        container.load(tag);
        _isConsuming = tag.getBoolean("isConsuming");
        _isRitualController = tag.getBoolean("isRitualController");
        _isRitualParticipant = tag.getBoolean("isRitualParticipant");
        if (tag.contains("controllerXYZ")) {
            var arr = tag.getIntArray("controllerXYZ");
            _controllerPos = new BlockPos(arr[0], arr[1], arr[2]);
        } else {
            _controllerPos = null;
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        container.save(tag);
        tag.putBoolean("isConsuming", _isConsuming);
        tag.putBoolean("isRitualController", _isRitualController);
        tag.putBoolean("isRitualParticipant", _isRitualParticipant);

        if (this.ritual != null) {
            var controller = this.ritual.getController();
            if (controller != null) {
                var pos = controller.getBlockPos();
                tag.putIntArray("controllerXYZ", new int[]{
                        pos.getX(),
                        pos.getY(),
                        pos.getZ()
                });
            }
        }

        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getItemStack() {
        return container.peek();
    }

    public void setItemStack(ItemStack stack) {
        container.set(stack);
    }

    private void notifyClients() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void startItemConsumePhase(@NotNull EnchantPedestalBlockEntity e, @NotNull Item expectItem) {
        if (e != this) {
            return;
        }
        if (!(this.getItemStack().is(expectItem))) {
            this.ritual.notifyFailure(this);
            return;
        }
        _isConsuming = true;
        notifyClients();
    }

    @Override
    public void endItemConsumePhase(@NotNull EnchantPedestalBlockEntity e) {
        if (e != this) {
            return;
        }
        _isConsuming = false;
        container.set(ItemStack.EMPTY); // Avoid using the setter to avoid an event.
        notifyClients();
    }

    @Override
    public void success() {
        if (this.ritual.getController() == this) {
            var itemStack = getItemStack();
            if (itemStack == null || itemStack.isEmpty()) {
                LOGGER.error("Item was removed before ritual ended!");
                // TODO: Fail here!
            } else if (!(this.ritual.getRecipe().canUpgrade(itemStack))) {
                LOGGER.error("Item has become invalid during ritual!");
                // TODO: Fail here!
            } else {
                try {
                    itemStack = this.ritual.getRecipe().applyRecipeOutput(itemStack);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                }
            }

            setItemStack(itemStack);
        }

        if ((this.ritual.getController() == this) && (level instanceof ServerLevel serverLevel)) {
            BlockPos pos = getBlockPos();

            // Portal particle explosion
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    100, // Particle count
                    0.4, 0.4, 0.4, // Spread
                    0.3 // Speed
            );

            // Reverse portal for inward burst
            serverLevel.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    50,
                    0.3, 0.3, 0.3,
                    0.1
            );

            // Enchanted hit sparkles
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    30,
                    0.3, 0.3, 0.3,
                    0.2
            );

            // Sound
            level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        endRitual();
        notifyClients();
    }

    @Override
    public void failure(@Nullable EnchantPedestalBlockEntity e) {
        if (e == this) {
            spawnFailureParticles(this.level, this.getBlockPos());
        }
        endRitual();
        notifyClients();
    }

    @Override
    public void consumeXp(int amount) {}

    public BlockPos getControllerPos() {
        return _controllerPos;
    }

    public void spawnFailureParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Center of the ritual
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Red smoke
        serverLevel.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                x, y, z,
                15,
                0.3, 0.3, 0.3,
                0.05
        );

        // Red dust particles for extra effect
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2;
            double offsetY = (level.random.nextDouble() - 0.5) * 2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2;

            serverLevel.sendParticles(
                    new DustParticleOptions(
                            new Vector3f(1.0f, 0.0f, 0.0f),  // Red color (RGB)
                            1.5f  // Size
                    ),
                    x + offsetX, y + offsetY, z + offsetZ,
                    1,
                    0, 0, 0,
                    0.02
            );
        }
    }

    public List<BrainInAJarBlockEntity> findBrainJarsNearby() {
        List<BrainInAJarBlockEntity> jars = new ArrayList<>();
        if (level == null) { return jars; }

        int horizontalRadius = 9;
        int verticalRadius = 3;

        BlockPos min = this.getBlockPos().offset(-horizontalRadius, -verticalRadius, -horizontalRadius);
        BlockPos max = this.getBlockPos().offset(horizontalRadius, verticalRadius, horizontalRadius);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            // Check horizontal distance (ignore Y)
            int dx = pos.getX() - this.getBlockPos().getX();
            int dz = pos.getZ() - this.getBlockPos().getZ();
            double horizontalDistSq = dx * dx + dz * dz;

            if (horizontalDistSq <= horizontalRadius * horizontalRadius) {
                if (level.getBlockState(pos).getBlock() instanceof BrainInAJarBlock) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof BrainInAJarBlockEntity jar) {
                        jars.add(jar);
                    }
                }
            }
        }

        return jars;
    }

}
