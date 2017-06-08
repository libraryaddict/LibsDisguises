package me.libraryaddict.disguise.utilities.backwards;

import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.disguise.utilities.backwards.metadata.Version_1_1;

import java.lang.reflect.Field;

/**
 * Created by libraryaddict on 8/06/2017.
 */
public class BackwardsSupport {
    public static BackwardMethods getMethods() {
        try {
            String version = ReflectionManager.getBukkitVersion();

            if (version.equals("v1_11_R1")) {
                return setupMetadata(Version_1_1.class);
            }

            return setupMetadata(BackwardMethods.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static BackwardMethods setupMetadata(Class<? extends BackwardMethods> backwardsClass) {
        try {
            BackwardMethods backwards = backwardsClass.newInstance();

            for (Field field : backwards.getClass().getFields()) {
                if (field.getType() != MetaIndex.class)
                    continue;

                MetaIndex.setMetaIndex(field.getName(), (MetaIndex) field.get(backwards));
            }

            MetaIndex.setValues();

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
