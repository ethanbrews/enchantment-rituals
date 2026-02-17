package me.ethanbrews.rituals.ritual;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.block.BrainInAJarBlockEntity;
import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;
import me.ethanbrews.rituals.recipe.EnchantmentRecipe;
import me.ethanbrews.rituals.recipe.EnchantmentRecipeRegistry;
import me.ethanbrews.rituals.util.RecipeHelper;
import me.ethanbrews.rituals.util.XpHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class Ritual implements RitualEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final EnchantPedestalBlockEntity controller;
    private final EnchantmentRecipe ritualRecipe;
    private final RitualEventDispatcher dispatcher;
    private final List<BrainInAJarBlockEntity> brainJars;
    private final Player player;

    private Ritual(
            EnchantPedestalBlockEntity controller,
            List<EnchantPedestalBlockEntity> others,
            EnchantmentRecipe recipe,
            Player player
    ) {
        assert controller.getLevel() != null;
        var random = controller.getLevel().random;
        this.controller = controller;
        this.ritualRecipe = recipe;
        this.player = player;
        var willFail = random.nextFloat() < ritualRecipe.getFailureChance();
        var timeline = generateEventTimeline(recipe, others);
        if (willFail) {
            timeline.addEvent(RitualEvent.fail(random.nextInt(0, recipe.getTickDuration()), null));
        }
        timeline.emitTimelineDebugLogs();
        this.dispatcher = new RitualEventDispatcher(timeline);
        this.dispatcher.subscribe(this);
        this.brainJars = controller.findBrainJarsNearby();
    }

    public EnchantmentRecipe getRecipe() {
        return this.ritualRecipe;
    }

    public RitualEventDispatcher getEventDispatcher() {
        return dispatcher;
    }

    private static RitualTimeline generateEventTimeline(
            EnchantmentRecipe recipe,
            List<EnchantPedestalBlockEntity> pedestals) {

        RitualTimeline events = new RitualTimeline();

        int itemCount = pedestals.size();
        int totalDuration = recipe.getTickDuration();
        int totalXp = recipe.getXpCost();

        if (itemCount == 0) {
            events.addEvent(RitualEvent.succeed(totalDuration));
            return events;
        }

        // Divide total duration into item consumption cycles
        // Each cycle = consume time + gap
        int ticksPerCycle = totalDuration / itemCount;

        // Within each cycle: 2/3 for consuming, 1/3 for gap
        int consumeDuration = (ticksPerCycle * 2) / 3;
        int gapDuration = ticksPerCycle - consumeDuration;

        // Schedule consume events for each pedestal
        int currentTick = 0;
        for (int i = 0; i < pedestals.size(); i++) {
            EnchantPedestalBlockEntity pedestal = pedestals.get(i);

            int startTick = currentTick;
            int endTick = startTick + consumeDuration;

            events.addEvent(RitualEvent.startConsume(startTick, pedestal, pedestal.getItemStack().getItem()));
            events.addEvent(RitualEvent.endConsume(endTick, pedestal));

            // Move to next cycle (no gap after last item)
            if (i < pedestals.size() - 1) {
                currentTick = endTick + gapDuration;
            }
        }

        // Schedule XP consumption events evenly throughout the ritual
        int xpTickInterval = 5; // Consume XP every 5 ticks (4 times per second)
        int xpEventCount = totalDuration / xpTickInterval;

        if (xpEventCount > 0 && totalXp > 0) {
            int xpPerEvent = totalXp / xpEventCount;
            int xpRemainder = totalXp % xpEventCount;

            for (int i = 0; i < xpEventCount; i++) {
                int xpTick = i * xpTickInterval;
                int xpAmount = xpPerEvent;

                // Add remainder to last few events to ensure exact total
                if (i >= xpEventCount - xpRemainder) {
                    xpAmount++;
                }

                events.addEvent(RitualEvent.consumeXp(xpTick, xpAmount));
            }
        }

        // Succeed at the end
        events.addEvent(RitualEvent.succeed(totalDuration));

        return events;
    }

    public EnchantPedestalBlockEntity getController() {
        return controller;
    }

    private static Tuple<RitualPattern, List<EnchantPedestalBlockEntity>> findPattern(EnchantPedestalBlockEntity candidate) throws RitualException {
        // Try each pattern type
        for (RitualPattern patternType : RitualPattern.values()) {
            List<EnchantPedestalBlockEntity> found = patternType.findPedestals(candidate.getLevel(), candidate.getBlockPos());

            // Check if all pedestals in the pattern are present and have items
            if (found.size() == patternType.getRequiredPedestals()) {
                return new Tuple<>(patternType, found);
            }
        }

        throw new RitualException("Invalid pedestal pattern");
    }

    public void tick() {
        try {
            dispatcher.tick();
        } catch (RitualException e) {
            throw new IllegalStateException("Ritual exception throw during ticking", e);
        }
    }

    public static void startRitual(EnchantPedestalBlockEntity controller, Player player) throws RitualException {
        // Start by inspecting the world for a valid configuration of pedestals.
        var patternOthersPair = findPattern(controller);
        var others = patternOthersPair.getB();
        var target = controller.getItemStack();
        if (target.isEmpty()) {
            throw new RitualException("No target item to enchant.");
        }
        var ingredients = others.stream()
                .map(EnchantPedestalBlockEntity::getItemStack)
                .filter(s -> !(s.isEmpty()))
                .toList();

        EnchantmentRecipe foundRecipe = null;
        for (var recipe : EnchantmentRecipeRegistry.getEnchantmentRecipes()) {
            if (!(recipe.canUpgrade(target))) { continue; }
            if (!(RecipeHelper.validateIngredients(ingredients, recipe.getIngredients()))) { continue; }
            foundRecipe = recipe;
            break;
        }

        if (foundRecipe == null) {
            throw new RitualException("No matching recipe");
        }

        var ritual = new Ritual(controller, others, foundRecipe, player);

        controller.beginRitual(ritual);
        for (var o : others) {
            o.beginRitual(ritual);
        }
    }

    public void notifyFailure(@Nullable EnchantPedestalBlockEntity failureBlock) {
        LOGGER.info("Ritual failure at Block: {}", failureBlock);
        dispatcher.injectEventNow(RitualEvent.fail(0, failureBlock));
    }

    @Override
    public void startItemConsumePhase(@NotNull EnchantPedestalBlockEntity e, @NotNull Item expectItem) {

    }

    @Override
    public void endItemConsumePhase(@NotNull EnchantPedestalBlockEntity e) {

    }

    @Override
    public void consumeXp(int costAmount) {
        var playerIsValid = player.isAlive() && !(player.isRemoved());

        var amount = costAmount;
        for (var jar : brainJars) {
            amount -= jar.removeXp(amount);
            if (amount <= 0) {
                return;
            }
        }

        if (!playerIsValid) {
            LOGGER.info("Player {} is no longer valid and alive!", player.getName());
            notifyFailure(null);
            return;
        }

        if (player.isCreative()) {
            return;
        }

        if (XpHelper.getTotalXP(player) < amount) {
            LOGGER.info("Player {} has no more xp!", player.getName());
            notifyFailure(null);
            return;
        }
        player.giveExperiencePoints(-amount);
    }

    @Override
    public void success() {
        getEventDispatcher().unsubscribe(this);
    }

    @Override
    public void failure(@Nullable EnchantPedestalBlockEntity e) {
        getEventDispatcher().unsubscribe(this);
    }
}
