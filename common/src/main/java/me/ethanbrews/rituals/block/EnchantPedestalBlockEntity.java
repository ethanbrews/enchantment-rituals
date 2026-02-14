package me.ethanbrews.rituals.block;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.EnchantmentRituals;
import me.ethanbrews.rituals.Ritual;
import me.ethanbrews.rituals.RitualException;
import me.ethanbrews.rituals.data.RitualEventHandler;
import me.ethanbrews.rituals.util.SingleItemContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class EnchantPedestalBlockEntity extends BlockEntity implements RitualEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SingleItemContainer container;
    private Ritual ritual = null;

    // Client Side synced
    private boolean _isConsuming = false;
    private boolean _isRitualController = false;
    private boolean _isRitualParticipant = false;
    private BlockPos _controllerPos = null;

    public EnchantPedestalBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(EnchantmentRituals.ENCHANTMENT_PEDESTAL_BE.get(), blockPos, blockState);
        container = new SingleItemContainer(this::onInventoryChanged);
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
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

    public ItemStack getItem() {
        return container.peek();
    }

    public void setItem(ItemStack stack) {
        container.set(stack);
    }

    private void notifyClients() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void startItemConsumePhase(EnchantPedestalBlockEntity e) {
        if (e != this) {
            return;
        }
        _isConsuming = true;
        notifyClients();
    }

    @Override
    public void endItemConsumePhase(EnchantPedestalBlockEntity e) {
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
            var ench = this.ritual.getRecipe().getEnchantment();
            var level = this.ritual.getRecipe().level();
            assert ench != null;
            var item = getItem();
            if (item.isEmpty() || !ench.canEnchant(item)) {
                LOGGER.error("Item was removed before ritual ended!");
                // TODO: Fail here
            } else {
                item.enchant(ench, level);
            }
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
    public void failure() {
        endRitual();
        notifyClients();
    }

    public BlockPos getControllerPos() {
        return _controllerPos;
    }
}
