package me.ethanbrews.rituals.forge;

import me.ethanbrews.rituals.EnchantmentRituals;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod.EventBusSubscriber(modid = EnchantmentRituals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnchantmentRitualsReloadEventListener {
        @SubscribeEvent
        public static void onResourceReload(AddReloadListenerEvent event) {
            event.addListener(new PreparableReloadListener() {
                @Override
                public @NotNull CompletableFuture<Void> reload(
                        @NotNull PreparationBarrier stage,
                        @NotNull ResourceManager resourceManager,
                        @NotNull ProfilerFiller preparationsProfiler,
                        @NotNull ProfilerFiller reloadProfiler,
                        @NotNull Executor backgroundExecutor,
                        @NotNull Executor gameExecutor
                ) {
                    return CompletableFuture.runAsync(() -> {
                                // Load on background thread
                            }, backgroundExecutor)
                            .thenCompose(stage::wait)
                            .thenRunAsync(() -> {
                                // Apply on game thread
                                EnchantmentRituals.reload(resourceManager);
                            }, gameExecutor);
                }
            });
        }
}
