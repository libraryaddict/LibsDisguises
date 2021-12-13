package me.libraryaddict.disguise.disguisetypes;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;

import java.lang.invoke.MethodHandle;
import java.util.Map;

/**
 * Created by libraryaddict on 19/01/2020.
 */
public class DisguiseCloneTest {

    /**
     * MetaIndex needs ProtocolLib to have initialized so.
     */
    //  @Test
    public void testCloneDisguise() {
        try {
            ReflectionManager.registerValues();
            DisguiseParser.createDefaultMethods();
            DisguiseUtilities.init();

            for (DisguiseType type : DisguiseType.values()) {
                Disguise disguise;

                if (type.isPlayer()) {
                    disguise = new PlayerDisguise("libraryaddict");
                } else if (type.isMob()) {
                    disguise = new MobDisguise(type);
                } else {
                    disguise = new MiscDisguise(type);
                }

                for (Map.Entry<WatcherMethod, Map.Entry<WatcherMethod, Object>> entry : DisguiseParser.getMethodDefaults().entrySet()) {
                    Object dValue = entry.getValue().getValue();

                    if (dValue instanceof String) {
                        dValue = "NewString";
                    } else if (dValue instanceof Float) {
                        dValue = ((float) dValue) + 1;
                    } else if (dValue instanceof Double) {
                        dValue = ((double) dValue) + 1;
                    } else if (dValue instanceof Long) {
                        dValue = ((long) dValue) + 1;
                    } else if (dValue instanceof Integer) {
                        dValue = ((int) dValue) + 1;
                    } else if (dValue instanceof Byte) {
                        dValue = ((byte) dValue) + 1;
                    } else if (dValue instanceof Short) {
                        dValue = ((short) dValue) + 1;
                    } else if (dValue instanceof ItemStack) {
                        dValue = new ItemStack(Material.DIAMOND_BLOCK);
                    } else if (dValue instanceof Boolean) {
                        dValue = !((Boolean) dValue);
                    } else if (dValue instanceof Enum) {
                        Object[] vals = dValue.getClass().getEnumConstants();

                        for (int i = 0; i < vals.length; i++) {
                            if (vals[i] == dValue) {
                                continue;
                            }

                            dValue = vals[i];
                            break;
                        }
                    } else {
                        continue;
                    }

                    WatcherMethod m = entry.getKey();

                    Object invokeWith = disguise;

                    if (FlagWatcher.class.isAssignableFrom(entry.getKey().getWatcherClass())) {
                        invokeWith = disguise.getWatcher();
                    }

                    try {
                        MethodHandle handle = entry.getKey().getMethod();
                        handle.bindTo(invokeWith);
                        handle.invoke(dValue);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                Disguise cloned = disguise.clone();
                String dString = DisguiseUtilities.getGson().toJson(disguise);
                String cString = DisguiseUtilities.getGson().toJson(cloned);

                if (!dString.equals(cString)) {
                    System.err.println(dString);
                    System.err.println(cString);
                    Assert.fail("Cloned disguise is not the same!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            if (ex.getCause() != null) {
                ex.getCause().printStackTrace();
            }

            throw ex;
        }
    }
}
