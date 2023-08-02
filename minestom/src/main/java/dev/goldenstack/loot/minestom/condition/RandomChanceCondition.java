package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * A condition that returns true based on {@link #chance()}. A chance of 0 has a 0% chance to return true, and a
 * chance of 1 has a 100% chance to return true.
 * @param chance the probability of this condition returning true
 */
public record RandomChanceCondition(double chance) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:random_chance";

    /**
     * A standard map-based converter for random chance conditions.
     */
    public static final @NotNull TypedLootConverter<RandomChanceCondition> CONVERTER =
            converter(RandomChanceCondition.class,
                    field(double.class).name("chance")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        return context.random().nextDouble() < chance;
    }
}
