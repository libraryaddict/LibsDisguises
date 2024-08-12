package me.libraryaddict.disguise.utilities.placeholderapi;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.BlockDisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemDisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemFrameWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.OminousItemSpawnerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TNTWatcher;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PlaceholderDisguiseMaterial implements DPlaceholder {
    @Override
    public String getName() {
        return "disguise_material";
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        ItemStack toResolve = null;
        WrappedBlockState blockState = null;

        switch (disguise.getType()) {
            case DROPPED_ITEM:
                toResolve = ((DroppedItemWatcher) disguise.getWatcher()).getItemStack();
                break;
            case ITEM_DISPLAY:
                toResolve = ((ItemDisplayWatcher) disguise.getWatcher()).getItemStack();
                break;
            case ITEM_FRAME:
            case GLOW_ITEM_FRAME:
                toResolve = ((ItemFrameWatcher) disguise.getWatcher()).getItem();
                break;
            case OMINOUS_ITEM_SPAWNER:
                toResolve = ((OminousItemSpawnerWatcher) disguise.getWatcher()).getItemStack();
                break;
            case BLOCK_DISPLAY:
                blockState = ((BlockDisplayWatcher) disguise.getWatcher()).getBlockState();
                break;
            case FALLING_BLOCK:
                blockState = ((FallingBlockWatcher) disguise.getWatcher()).getBlockState();
                break;
            case PRIMED_TNT:
                if (NmsVersion.v1_20_R3.isSupported()) {
                    blockState = ((TNTWatcher) disguise.getWatcher()).getBlockData();
                } else {
                    return "???";
                }
                break;
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_TNT:
            case MINECART_MOB_SPAWNER:
                blockState = ((MinecartWatcher) disguise.getWatcher()).getBlock();
                break;
            default:
                return "???";
        }

        String toTranslate;

        if (toResolve != null) {
            toTranslate = toResolve.getType().name();
        } else {
            toTranslate = blockState.getType().getName();
        }

        return TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(ReflectionManager.toReadable(toTranslate, " "));
    }
}
