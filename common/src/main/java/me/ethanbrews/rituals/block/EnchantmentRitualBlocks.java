package me.ethanbrews.rituals.block;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.ethanbrews.rituals.EnchantmentRituals;
import me.ethanbrews.rituals.client.BrainInAJarBlockEntityRenderer;
import me.ethanbrews.rituals.client.EnchantmentPedestalBlockEntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class EnchantmentRitualBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(EnchantmentRituals.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(EnchantmentRituals.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    public static final RegistrySupplier<Block> ENCHANTMENT_PEDESTAL_BLOCK =
            BLOCKS.register("enchant_pedestal", EnchantPedestalBlock::new);
    public static final RegistrySupplier<Block> GLASS_JAR_BLOCK =
            BLOCKS.register("glass_jar", GlassJarBlock::new);
    public static final RegistrySupplier<Block> BRAIN_IN_A_JAR_BLOCK =
            BLOCKS.register("brain_in_a_jar", BrainInAJarBlock::new);
    public static final RegistrySupplier<BlockEntityType<EnchantPedestalBlockEntity>> ENCHANTMENT_PEDESTAL_BE =
            BLOCK_ENTITY_TYPES.register("enchant_pedestal_entity",()->
                BlockEntityType.Builder.of(
                        EnchantPedestalBlockEntity::new,
                        ENCHANTMENT_PEDESTAL_BLOCK.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<BrainInAJarBlockEntity>> BRAIN_IN_A_JAR_BLOCK_BE =
            BLOCK_ENTITY_TYPES.register("brain_in_a_jar_entity",()->
                    BlockEntityType.Builder.of(
                            BrainInAJarBlockEntity::new,
                            BRAIN_IN_A_JAR_BLOCK.get()).build(null));

    public static void register() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
    }

    public static void registerClient() {
        BlockEntityRendererRegistry.register(
                EnchantmentRitualBlocks.ENCHANTMENT_PEDESTAL_BE.get(),
                EnchantmentPedestalBlockEntityRenderer::new
        );
        BlockEntityRendererRegistry.register(
                BRAIN_IN_A_JAR_BLOCK_BE.get(),
                BrainInAJarBlockEntityRenderer::new
        );
        RenderTypeRegistry.register(
                RenderType.translucent(),
                GLASS_JAR_BLOCK.get()
        );
        RenderTypeRegistry.register(
                RenderType.translucent(),
                BRAIN_IN_A_JAR_BLOCK.get()
        );
    }
}
