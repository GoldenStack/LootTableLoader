package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.Trove;
import dev.goldenstack.loot.converter.ConditionalLootConverter;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

/**
 * Manages serialization for when multiple subtypes of a base class must be chosen from.
 * @param <V> the base type of object that will be converted
 */
public sealed interface LootConversionManager<V> extends TypedLootConverter<V> permits LootConversionManagerImpl {

    /**
     * Creates a new LootConversionManager builder that will convert the provided type.
     * @param convertedType the converted type
     * @return a new builder
     */
    static <V> @NotNull Builder<V> builder(@NotNull TypeToken<V> convertedType) {
        return new Builder<>(convertedType);
    }

    final class Builder<V> {

        private final @NotNull TypeToken<V> convertedType;
        private String keyLocation;
        private final @NotNull List<ConditionalLootConverter<V>> initialConverters = new ArrayList<>();

        private final @NotNull Map<String, TypedLootConverter<? extends V>> keyToConverter = new HashMap<>();
        private final @NotNull Map<TypeToken<? extends V>, TypedLootConverter<? extends V>> typeToConverter = new HashMap<>();
        private final @NotNull Map<TypeToken<? extends V>, String> typeToKey = new HashMap<>();

        private Builder(@NotNull TypeToken<V> convertedType) {
            this.convertedType = convertedType;
        }

        /**
         * Sets the location of the key that will be used to determine which typed converter to use.
         * @param keyLocation the location of the key that will be used
         * @return this, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder<V> keyLocation(@NotNull String keyLocation) {
            this.keyLocation = keyLocation;
            return this;
        }

        /**
         * Adds a conditional converter to this builder. These conditional converters are applied before any
         * typed converters are.
         * @param converter the conditional converter to add
         * @return this, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder<V> add(@NotNull ConditionalLootConverter<V> converter) {
            this.initialConverters.add(converter);
            return this;
        }

        /**
         * Adds a typed converter under a specific key to this builder. These typed converters are always applied after
         * any conditional converters are.
         * @param key the key to associate the converter with
         * @param converter the converter to be added
         * @return this, for chaining
         */
        @Contract("_, _ -> this")
        public @NotNull Builder<V> add(@NotNull String key, @NotNull TypedLootConverter<? extends V> converter) {
            if (!GenericTypeReflector.isSuperType(convertedType.getType(), converter.convertedType().getType())) {
                throw new IllegalArgumentException("Converter '" + key + "' has invalid type '" + converter.convertedType().getType() + "' as it is not a subtype of '" + convertedType.getType() + "'");
            } else if (keyToConverter.put(key, converter) != null) {
                throw new IllegalArgumentException("Converter '" + key + "' has a key that has already been registered");
            } else if (typeToConverter.put(converter.convertedType(), converter) != null || typeToKey.put(converter.convertedType(), key) != null) {
                throw new IllegalArgumentException("Converter '" + key + "' has a type '" + converter.convertedType().getType() + "' that has already been registered");
            }

            return this;
        }

        /**
         * Builds this builder into a new LootConversionManager instance.
         * @return the new loot conversion manager
         */
        @Contract(" -> new")
        public @NotNull LootConversionManager<V> build() {
            return new LootConversionManagerImpl<>(
                    convertedType,
                    Objects.requireNonNull(keyLocation, "This builder cannot be built without a key location"),
                    initialConverters,
                    keyToConverter, typeToConverter, typeToKey
            );
        }

    }

}

record LootConversionManagerImpl<V>(@NotNull TypeToken<V> convertedType, @NotNull String keyLocation,
                                    @NotNull List<ConditionalLootConverter<V>> initialConverters,
                                    @NotNull Map<String, TypedLootConverter<? extends V>> keyToConverter,
                                    @NotNull Map<TypeToken<? extends V>, TypedLootConverter<? extends V>> typeToConverter,
                                    @NotNull Map<TypeToken<? extends V>, String> typeToKey) implements LootConversionManager<V> {

    LootConversionManagerImpl {
        Objects.requireNonNull(keyLocation, "This builder cannot be built without a key location");

        initialConverters = List.copyOf(initialConverters);
        keyToConverter = Map.copyOf(keyToConverter);
        typeToConverter = Map.copyOf(typeToConverter);
        typeToKey = Map.copyOf(typeToKey);
    }

    @Override
    public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull Trove context) throws ConfigurateException {
        serialize0(input, result, context);
    }

    private <R extends V> void serialize0(@NotNull R input, @NotNull ConfigurationNode result, @NotNull Trove context) throws ConfigurateException {
        if (!initialConverters.isEmpty()) {
            for (var conditional : initialConverters) {
                if (conditional.canSerialize(input, context)) {
                    conditional.serialize(input, result, context);
                    return;
                }
            }
        }
        TypeToken<?> token = TypeToken.get(input.getClass());

        @SuppressWarnings("unchecked")
        TypedLootConverter<R> converter = (TypedLootConverter<R>) typeToConverter.get(token);
        String key = typeToKey.get(token);
        if (converter == null || key == null) {
            throw new ConfigurateException("Unknown input type '" + input.getClass() + "' for base type '" + convertedType.getType() + "'");
        }
        result.node(keyLocation).set(key);
        converter.serialize(input, result, context);
    }

    @Override
    public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull Trove context) throws ConfigurateException {
        // Initial pass with conditional converters
        for (var conditional : initialConverters) {
            if (conditional.canDeserialize(input, context)) {
                return conditional.deserialize(input, context);
            }
        }
        ConfigurationNode keyNode = input.node(keyLocation);

        String actualKey = keyNode.getString();
        if (actualKey == null) {
            throw new SerializationException(keyNode, String.class, "Expected a key");
        }

        TypedLootConverter<? extends V> converter = keyToConverter.get(actualKey);
        if (converter == null) {
            throw new ConfigurateException(keyNode, "Unknown key '" + actualKey + "' for base type '" + convertedType().getType() + "'");
        }
        return converter.deserialize(input, context);
    }

}
