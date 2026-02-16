package me.ethanbrews.rituals.ritual;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.EnchantmentRituals;
import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;
import me.ethanbrews.rituals.recipe.EnchantmentRecipe;
import me.ethanbrews.rituals.util.RecipeHelper;
import net.minecraft.util.Tuple;
import org.slf4j.Logger;

import java.util.*;

public class Ritual {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final EnchantPedestalBlockEntity controller;
    private final List<EnchantPedestalBlockEntity> others;
    private final EnchantmentRecipe ritualRecipe;
    private final RitualEventDispatcher dispatcher;

    private Ritual(
            EnchantPedestalBlockEntity controller,
            List<EnchantPedestalBlockEntity> others,
            EnchantmentRecipe recipe
    ) {
        assert controller.getLevel() != null;
        var random = controller.getLevel().random;
        this.controller = controller;
        this.others = others;
        this.ritualRecipe = recipe;
        var willFail = random.nextFloat() < ritualRecipe.getFailureChance();
        var timeline = generateEventTimeline(recipe.getTickDuration(), this.others);
        if (willFail) {
            timeline.addEvent(RitualEvent.fail(random.nextInt(0, recipe.getTickDuration())));
        }
        this.dispatcher = new RitualEventDispatcher(timeline);
    }

    public RitualEventDispatcher getEventDispatcher() {
        return dispatcher;
    }

    public EnchantmentRecipe getRecipe() {
        return ritualRecipe;
    }

    private static RitualTimeline generateEventTimeline(
            int duration,
            List<EnchantPedestalBlockEntity> pedestals) {

        RitualTimeline events = new RitualTimeline();

        int itemCount = pedestals.size();
        int ticksPerItem = duration / itemCount;
        int consumeDuration = ticksPerItem / 2;

        // Schedule consume events for each pedestal
        for (int i = 0; i < pedestals.size(); i++) {
            EnchantPedestalBlockEntity pedestal = pedestals.get(i);

            int startTick = i * ticksPerItem;
            int endTick = startTick + consumeDuration;

            events.addEvent(RitualEvent.startConsume(startTick, pedestal));
            events.addEvent(RitualEvent.endConsume(endTick, pedestal));
        }

        // Succeed at the end
        events.addEvent(RitualEvent.succeed(duration));

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

    public static void startRitual(EnchantPedestalBlockEntity controller) throws RitualException {
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
        for (var recipe : EnchantmentRituals.getEnchantmentRecipes()) {
            if (!(Objects.requireNonNull(recipe.getEnchantment()).canEnchant(target))) { continue; }
            if (!(RecipeHelper.validateIngredients(ingredients, recipe.getIngredients()))) { continue; }
            foundRecipe = recipe;
            break;
        }

        if (foundRecipe == null) {
            throw new RitualException("No matching recipe");
        }

        var ritual = new Ritual(controller, others, foundRecipe);

        controller.beginRitual(ritual);
        for (var o : others) {
            o.beginRitual(ritual);
        }
    }
}
