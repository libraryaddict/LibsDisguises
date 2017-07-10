package me.libraryaddict.disguise.utilities.backwards;

import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.disguise.utilities.backwards.metadata.Version_1_10;
import me.libraryaddict.disguise.utilities.backwards.metadata.Version_1_11;
import me.libraryaddict.disguise.utilities.backwards.metadata.Version_1_9;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by libraryaddict on 8/06/2017.
 */
public class BackwardsSupport {
    public static BackwardMethods getMethods() {
        try {
            String version = ReflectionManager.getMinecraftVersion();

            Class<? extends BackwardMethods> methods = BackwardMethods.class;

            if (version.equals("1.9") || version.equals("1.9.1") || version.equals("1.9.2") || version
                    .equals("1.9.3") || version.equals("1.9.4")) {
                methods = Version_1_9.class;
            } else if (version.equals("1.10") || version.equals("1.10.1") || version.equals("1.10.2")) {
                methods = Version_1_10.class;
            } else if (version.equals("1.11") || version.equals("1.11.1") || version.equals("1.11.2")) {
                methods = Version_1_11.class;
            }

            if (methods != BackwardMethods.class) {
                if (!LibsPremium.isPremium()) {
                    System.out.println("[LibsDisguises] You must purchase the plugin to use backwards compatibility!");
                    methods = BackwardMethods.class;
                } else {
                    System.out.println("[LibsDisguises] Enabled backwards support for " + version);
                }
            }

            return setupMetadata(methods);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void getIndexes(Class backwardsClass, BackwardMethods backwards,
            HashMap<String, MetaIndex> newIndexes) throws IllegalAccessException {
        for (Field field : backwardsClass.getDeclaredFields()) {
            if (field.getType() != MetaIndex.class)
                continue;

            field.setAccessible(true);

            if (newIndexes.containsKey(field.getName()))
                continue;

            if (MetaIndex.setMetaIndex(field.getName(), (MetaIndex) field.get(backwards))) {
                newIndexes.put(field.getName(), MetaIndex.ENTITY_META);
                continue;
            }

            newIndexes.put(field.getName(), (MetaIndex) field.get(backwards));
        }

        backwardsClass = backwardsClass.getSuperclass();

        if (backwardsClass.getSimpleName().contains("Version_"))
            getIndexes(backwardsClass, backwards, newIndexes);
    }

    private static BackwardMethods setupMetadata(Class<? extends BackwardMethods> backwardsClass) {
        try {
            BackwardMethods backwards = backwardsClass.newInstance();

            HashMap<String, MetaIndex> newIndexes = new HashMap<>();

            getIndexes(backwardsClass, backwards, newIndexes);

            MetaIndex.setValues();

            HashSet<MetaIndex> indexes = new HashSet<>(newIndexes.values());
            indexes.remove(MetaIndex.ENTITY_META); // We do the hashmap stuff to prevent multiple versions
            // registering the same meta index

            MetaIndex.addMetaIndexes(indexes.toArray(new MetaIndex[0]));

            if (backwards.isOrderedIndexes()) {
                MetaIndex.eliminateBlankIndexes();
                MetaIndex.orderMetaIndexes();
            }

            backwards.doReplaceSounds();

            return backwards;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
