package me.ethanbrews.rituals.util;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SingleItemContainer {
    private final SimpleContainer container = new SimpleContainer(1);
    private final Runnable onChange;

    public SingleItemContainer(@Nullable Runnable onChange) {
        this.onChange = onChange;
        if (onChange != null) {
            container.addListener(c -> onChange.run());
        }
    }

    public SingleItemContainer() {
        this(null);
    }

    // Convenience methods
    public ItemStack peek() {
        return container.getItem(0);
    }

    public ItemStack take() {
        return container.removeItem(0, 1);
    }

    public void set(ItemStack stack) {
        if (!stack.isEmpty() && stack.getCount() != 1) {
            throw new IllegalArgumentException("Stack size must be 1, got: " + stack.getCount());
        }
        container.setItem(0, stack);
    }

    public boolean hasItem() {
        return !peek().isEmpty();
    }

    public void clear() {
        container.clearContent();
    }

    // Expose the container for drops
    public SimpleContainer getContainer() {
        return container;
    }

    // NBT serialization
    public void save(CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, NonNullList.of(ItemStack.EMPTY, peek()));
    }

    public void load(CompoundTag tag) {
        NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        container.setItem(0, items.get(0));
    }
}
