package dev.goldenstack.loot.entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.JsonHelper;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract class for an entry that may contain multiple "child" entries.
 */
public abstract class CombinedEntry extends LootEntry {

    private final @NotNull ImmutableList<LootEntry> children;

    /**
     * Initialize a new CombinedEntry with the provided conditions, functions, weight, quality, and children.
     */
    public CombinedEntry(@NotNull ImmutableList<LootCondition> conditions, @NotNull ImmutableList<LootFunction> functions,
                         int weight, int quality, @NotNull ImmutableList<LootEntry> children){
        super(conditions, functions, weight, quality);
        this.children = children;
    }

    /**
     * Returns this CombinedEntry's children
     */
    public final @NotNull ImmutableList<LootEntry> children(){
        return this.children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        super.serialize(object, loader);
        if (this.children.size() > 0) {
            object.add("children", JsonHelper.serializeJsonArray(this.children, loader.getLootEntryManager()::serialize));
        }
    }

    @Override
    public String toString() {
        return "CombinedEntry["  + LootEntry.partialToString(this) + ", children=" + children + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CombinedEntry that = (CombinedEntry) o;
        return children.equals(that.children);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + children.hashCode();
    }

    /**
     * Utility method that generates part of the result that should be returned via {@code toString()}.
     * @param entry The entry to generate the string for
     * @return The string
     */
    protected static @NotNull String partialToString(@NotNull CombinedEntry entry){
        return LootEntry.partialToString(entry) + ", children=" + entry.children;
    }

    /**
     * Utility method for getting an immutable list of the children (loot entries) from the JsonObject.<br>
     * This should be called in a similar manner to: <br>
     * {@code ImmutableList<LootEntry> children = CombinedEntry.deserializeChildren(json, loader);}
     */
    public static @NotNull ImmutableList<LootEntry> deserializeChildren(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        JsonElement children = json.get("children");
        if (JsonHelper.isNull(children)){
            return ImmutableList.of();
        }
        return JsonHelper.deserializeJsonArray(children, "children", loader.getLootEntryManager()::deserialize);
    }
}