package me.libraryaddict.disguise.utilities.backwards;

import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.disguise.utilities.backwards.metadata.Version_1_11;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by libraryaddict on 8/06/2017.
 */
public class BackwardsSupport {
    public static BackwardMethods getMethods() {
        try {
            String version = ReflectionManager.getBukkitVersion();
            Class<? extends BackwardMethods> methods = BackwardMethods.class;

            if (version.equals("v1_11_R1")) {
                methods = Version_1_11.class;
            }

            if (!LibsPremium.isPremium() && methods != BackwardMethods.class) {
                System.out.println("[LibsDisguises] You must purchase the plugin to use backwards compatibility!");
                methods = BackwardMethods.class;
            }

            return setupMetadata(methods);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static BackwardMethods setupMetadata(Class<? extends BackwardMethods> backwardsClass) {
        try {
            BackwardMethods backwards = backwardsClass.newInstance();
            ArrayList<MetaIndex> newIndexes = new ArrayList<>();

            for (Field field : backwards.getClass().getFields()) {
                if (field.getType() != MetaIndex.class)
                    continue;

                if (MetaIndex.setMetaIndex(field.getName(), (MetaIndex) field.get(backwards))) {
                    continue;
                }

                newIndexes.add((MetaIndex) field.get(backwards));
            }

            MetaIndex.setValues();

            MetaIndex.addMetaIndexes(newIndexes.toArray(new MetaIndex[0]));

            if (backwards.isOrderedIndexes()) {
                MetaIndex.fillInBlankIndexes();
                MetaIndex.orderMetaIndexes();
            }

            return backwards;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
