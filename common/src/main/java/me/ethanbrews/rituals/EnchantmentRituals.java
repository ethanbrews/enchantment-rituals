package me.ethanbrews.rituals;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import me.ethanbrews.rituals.block.EnchantPedestalBlock;
import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;
import me.ethanbrews.rituals.block.EnchantmentPedestalBlockEntityRenderer;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import me.ethanbrews.rituals.data.EnchantmentRecipe;
import me.ethanbrews.rituals.particle.RitualConsumeParticleOptions;
import me.ethanbrews.rituals.particle.RitualConsumeParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EnchantmentRituals {
    public static final String MOD_ID = "enchantmentrituals";

    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(MOD_ID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<Block> ENCHANTMENT_PEDESTAL_BLOCK =
            BLOCKS.register("enchant_pedestal", () -> new EnchantPedestalBlock(BlockBehaviour.Properties.of()));
    public static final RegistrySupplier<BlockItem> ENCHANTMENT_PEDESTAL_BLOCK_ITEM =
            ITEMS.register("enchant_pedestal", () ->
                    new BlockItem(ENCHANTMENT_PEDESTAL_BLOCK.get(), new Item.Properties())
            );

    public static final RegistrySupplier<BlockEntityType<EnchantPedestalBlockEntity>> ENCHANTMENT_PEDESTAL_BE =
            BLOCK_ENTITY_TYPES.register("enchant_pedestal_entity",()->
                BlockEntityType.Builder.of(
                        EnchantPedestalBlockEntity::new,
                        ENCHANTMENT_PEDESTAL_BLOCK.get()).build(null));

    public static final RegistrySupplier<ParticleType<RitualConsumeParticleOptions>> RITUAL_CONSUME_PARTICLE =
            PARTICLES.register("ritual_consume", () ->
                    new ParticleType<RitualConsumeParticleOptions>(false, RitualConsumeParticleOptions.DESERIALIZER) {
                        @Override
                        public Codec<RitualConsumeParticleOptions> codec() {
                            return RitualConsumeParticleOptions.CODEC;
                        }

                        @Override
                        public ParticleOptions.@NotNull Deserializer<RitualConsumeParticleOptions> getDeserializer() {
                            return RitualConsumeParticleOptions.DESERIALIZER;
                        }
                    }
            );

    public static void init() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
        PARTICLES.register();
    }

    public static List<EnchantmentRecipe> getEnchantmentRecipes() {
        return List.of(new EnchantmentRecipe(
                "minecraft:sharpness",
                new String[][]{{"minecraft:quartz"}, {"minecraft:quartz"}, {"minecraft:quartz"}, {"minecraft:quartz"}},
                10,
                1,
                0.0f,
                20*10

        ));
    }

    public static void initClient() {
        ParticleProviderRegistry.register(
                RITUAL_CONSUME_PARTICLE.get(),
                RitualConsumeParticleProvider::new
        );

        BlockEntityRendererRegistry.register(
                ENCHANTMENT_PEDESTAL_BE.get(),
                EnchantmentPedestalBlockEntityRenderer::new
        );


    }
}
