package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootNumber;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Generates a random number between (inclusive) the minimum and maximum. To be precise, for
 * {@link #getLong(LootGenerationContext)}, both the minimum and maximum are inclusive, but for
 * {@link #getDouble(LootGenerationContext)}, only the minimum is inclusive.<br>
 * The minimum should always be less than the maximum.
 * @param min the minimum value
 * @param max the maximum value
 */
public record UniformNumber(@NotNull LootNumber<ItemStack> min, @NotNull LootNumber<ItemStack> max) implements LootNumber<ItemStack> {

    /**
     * A standard map-based converter for uniform numbers.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, UniformNumber> CONVERTER = new KeyedLootConverter<>("minecraft:uniform", TypeToken.get(UniformNumber.class)) {
        @Override
        public void serialize(@NotNull UniformNumber input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("min").set(context.loader().lootNumberManager().serialize(input.min(), context));
            result.node("max").set(context.loader().lootNumberManager().serialize(input.max(), context));
        }

        @Override
        public @NotNull UniformNumber deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new UniformNumber(
                    context.loader().lootNumberManager().deserialize(input.node("min"), context),
                    context.loader().lootNumberManager().deserialize(input.node("max"), context)
            );
        }
    };

    @Override
    public long getLong(@NotNull LootGenerationContext context) {
        return context.random().nextLong(min().getLong(context), max().getLong(context) + 1);
    }

    @Override
    public double getDouble(@NotNull LootGenerationContext context) {
        return context.random().nextDouble(min().getDouble(context), max().getDouble(context));
    }
}
