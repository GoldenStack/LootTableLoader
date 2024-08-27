package net.goldenstack.loot;

import net.goldenstack.loot.util.nbt.NBTPath;
import net.goldenstack.loot.util.nbt.NBTReference;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.minestom.server.item.enchant.LevelBasedValue;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

/**
 * Generates numbers based on provided loot contexts.
 */
public interface LootNumber {

    /**
     * Generates an integer depending on the information in the provided context.<br>
     * This is an explicitly impure method—it depends on state outside the given context.
     * @param context the context object, to use if required
     * @return the integer generated by this loot number for the provided context
     */
    int getInt(@NotNull LootContext context);

    /**
     * Generates a double depending on the information in the provided context.<br>
     * This is an explicitly impure method—it depends on state outside the given context.
     * @param context the context object, to use if required
     * @return the double generated by this loot number for the provided context
     */
    double getDouble(@NotNull LootContext context);

    record Constant(@NotNull Number value) implements LootNumber {
        @Override
        public int getInt(@NotNull LootContext context) {
            return value.intValue();
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            return value.doubleValue();
        }
    }

    record Uniform(@NotNull LootNumber min, @NotNull LootNumber max) implements LootNumber {
        @Override
        public int getInt(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextInt(min().getInt(context), max().getInt(context) + 1);
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextDouble(min().getDouble(context), max().getDouble(context));
        }
    }

    record Binomial(@NotNull LootNumber trials, @NotNull LootNumber probability) implements LootNumber {
        @Override
        public int getInt(@NotNull LootContext context) {
            int trials = trials().getInt(context);
            double probability = probability().getDouble(context);
            Random random = context.require(LootContext.RANDOM);

            int successes = 0;
            for (int trial = 0; trial < trials; trial++) {
                if (random.nextDouble() < probability) {
                    successes++;
                }
            }
            return successes;
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            return getInt(context);
        }
    }

    record EnchantmentLevel(@NotNull LevelBasedValue value) implements LootNumber {
        @Override
        public int getInt(@NotNull LootContext context) {
            return (int) Math.round(getDouble(context));
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            return value.calc(context.require(LootContext.ENCHANTMENT_LEVEL));
        }
    }
    
    record Score(@NotNull LootScore target, @NotNull String objective, double scale) implements LootNumber {
        @Override
        public int getInt(@NotNull LootContext context) {
            return (int) Math.round(getDouble(context));
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            var score = target.apply(context).apply(objective);

            return score != null ? score * scale : 0;
        }
    }

    record CommandStorage(@NotNull NamespaceID id, @NotNull NBTPath path) implements LootNumber {
        @Override
        public int getInt(@NotNull LootContext context) {
            return get(context).intValue();
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            return get(context).doubleValue();
        }

        private NumberBinaryTag get(@NotNull LootContext context) {
            CompoundBinaryTag compound = context.require(LootContext.COMMAND_STORAGE).apply(id);

            List<NBTReference> refs = path.get(compound);
            if (refs.size() != 1) return IntBinaryTag.intBinaryTag(0);

            if (refs.getFirst().get() instanceof NumberBinaryTag number) {
                return number;
            } else {
                return IntBinaryTag.intBinaryTag(0);
            }
        }
    }

}
