package dev.goldenstack.loot.context;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents information about something that happened in a LootContext
 */
public record LootContextParameter <T> (@NotNull NamespaceID key) {

    /**
     * Represents the entity that is getting loot generated for it
     */
    public static final @NotNull LootContextParameter<Entity> THIS_ENTITY = new LootContextParameter<>("this_entity");

    /**
     * Represents the last damage that was dealt to the entity
     */
    public static final @NotNull LootContextParameter<Player> LAST_DAMAGE_PLAYER = new LootContextParameter<>("last_damage_player");

    /**
     * Represents the source of the damage
     */
    public static final @NotNull LootContextParameter<DamageType> DAMAGE_SOURCE = new LootContextParameter<>("damage_source");

    /**
     * Represents the entity that killed something. If a player shoots an arrow and kills something, this will represent
     * the player.
     */
    public static final @NotNull LootContextParameter<Entity> KILLER_ENTITY = new LootContextParameter<>("killer_entity");

    /**
     * Represents the exact entity that killed something. If a player shoots an arrow and kills something, this will
     * represent the arrow.
     */
    public static final @NotNull LootContextParameter<Entity> DIRECT_KILLER_ENTITY = new LootContextParameter<>("direct_killer_entity");

    /**
     * Represents the origin of whatever happened
     */
    public static final @NotNull LootContextParameter<Pos> ORIGIN = new LootContextParameter<>("origin");

    /**
     * Represents the block state
     */
    public static final @NotNull LootContextParameter<Block> BLOCK_STATE = new LootContextParameter<>("block_state");

    /**
     * Represents the block entity. Because Minestom handles NBT for all blocks, this is no different from
     * {@link #BLOCK_STATE}
     */
    public static final @NotNull LootContextParameter<Block> BLOCK_ENTITY = new LootContextParameter<>("block_entity");

    /**
     * Represents the tool that was used in the event
     */
    public static final @NotNull LootContextParameter<ItemStack> TOOL = new LootContextParameter<>("tool");

    /**
     * Represents the explosion radius of an explosion that occurred
     */
    public static final @NotNull LootContextParameter<Float> EXPLOSION_RADIUS = new LootContextParameter<>("explosion_radius");

    /**
     * Creates a new LootContextParameter with the provided key turned into a {@link NamespaceID}
     */
    public LootContextParameter(@NotNull String key) {
        this(NamespaceID.from(key));
    }
}
