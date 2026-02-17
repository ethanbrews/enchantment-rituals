package me.ethanbrews.rituals.particle;

import com.mojang.serialization.Codec;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.ethanbrews.rituals.EnchantmentRituals;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

public class EnchantmentRitualParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(EnchantmentRituals.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<ParticleType<RitualConsumeParticleOptions>> RITUAL_CONSUME_PARTICLE =
            PARTICLES.register("ritual_consume", () ->
                    new ParticleType<RitualConsumeParticleOptions>(false, RitualConsumeParticleOptions.DESERIALIZER) {
                        @Override
                        public @NotNull Codec<RitualConsumeParticleOptions> codec() {
                            return RitualConsumeParticleOptions.CODEC;
                        }

                        @Override
                        public ParticleOptions.@NotNull Deserializer<RitualConsumeParticleOptions> getDeserializer() {
                            return RitualConsumeParticleOptions.DESERIALIZER;
                        }
                    }
            );

    public static void register() {
        PARTICLES.register();
    }

    public static void registerClient() {
        ParticleProviderRegistry.register(
                EnchantmentRitualParticles.RITUAL_CONSUME_PARTICLE.get(),
                RitualConsumeParticleProvider::new
        );
    }
}
