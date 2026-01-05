package me.libraryaddict.disguise.disguisetypes;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;

import static org.junit.jupiter.api.Assertions.fail;

public class DisguiseCloneTest {

    /**
     * MetaIndex needs PacketEvents to have initialized so.
     */
    //  @Test
    public void testCloneDisguise() {
        try {
            ReflectionManager.registerFlagWatchers();
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

                Disguise cloned = disguise.clone();
                String dString = DisguiseUtilities.getGson().toJson(disguise);
                String cString = DisguiseUtilities.getGson().toJson(cloned);

                if (!dString.equals(cString)) {
                    System.err.println(dString);
                    System.err.println(cString);
                    fail("Cloned disguise is not the same!");
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
