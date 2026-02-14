package me.ethanbrews.rituals.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.ethanbrews.rituals.EnchantmentRituals;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class RitualConsumeParticleOptions implements ParticleOptions {
    public static final Codec<RitualConsumeParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("targetX").forGetter(o -> o.targetX),
                    Codec.DOUBLE.fieldOf("targetY").forGetter(o -> o.targetY),
                    Codec.DOUBLE.fieldOf("targetZ").forGetter(o -> o.targetZ),
                    ItemStack.CODEC.fieldOf("item").forGetter(o -> o.item)
            ).apply(instance, RitualConsumeParticleOptions::new)
    );

    private final double targetX, targetY, targetZ;
    private final ItemStack item;

    public RitualConsumeParticleOptions(double targetX, double targetY, double targetZ, ItemStack item) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.item = item;
    }

    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }
    public double getTargetZ() { return targetZ; }
    public ItemStack getItem() { return item; }

    @Override
    public @NotNull ParticleType<?> getType() {
        return EnchantmentRituals.RITUAL_CONSUME_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeDouble(targetX);
        buffer.writeDouble(targetY);
        buffer.writeDouble(targetZ);
        buffer.writeItem(item);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %s",
                BuiltInRegistries.PARTICLE_TYPE.getKey(getType()),
                targetX, targetY, targetZ, item.toString());
    }

    public static final ParticleOptions.Deserializer<RitualConsumeParticleOptions> DESERIALIZER =
            new ParticleOptions.Deserializer<>() {
                @Override
                public RitualConsumeParticleOptions fromCommand(ParticleType<RitualConsumeParticleOptions> type,
                                                                StringReader reader) throws CommandSyntaxException {
                    reader.expect(' ');
                    double targetX = reader.readDouble();
                    reader.expect(' ');
                    double targetY = reader.readDouble();
                    reader.expect(' ');
                    double targetZ = reader.readDouble();
                    // For commands, just use empty itemstack
                    return new RitualConsumeParticleOptions(targetX, targetY, targetZ, ItemStack.EMPTY);
                }

                @Override
                public RitualConsumeParticleOptions fromNetwork(ParticleType<RitualConsumeParticleOptions> type,
                                                                FriendlyByteBuf buffer) {
                    return new RitualConsumeParticleOptions(
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readItem()
                    );
                }
            };
}
