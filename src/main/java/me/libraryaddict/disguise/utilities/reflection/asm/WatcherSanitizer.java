package me.libraryaddict.disguise.utilities.reflection.asm;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by libraryaddict on 17/02/2020.
 */
public class WatcherSanitizer {

    public static void init() {
        IAsm asm;

        if (NmsVersion.v1_14.isSupported()) {
            asm = new Asm14();
        } else {
            asm = new Asm13();
        }

        try (InputStream stream = LibsDisguises.getInstance().getResource("ANTI_PIRACY_ENCRYPTION")) {
            List<String> lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.toList());

            LinkedHashMap<String, ArrayList<Map.Entry<String, String>>> toRemove = new LinkedHashMap<>();

            for (String s : lines) {
                WatcherInfo info = new WatcherInfo(s);

                if (info.isSupported()) {
                    continue;
                }

                String path = "me.libraryaddict.disguise.disguisetypes." +
                        (info.getWatcher().equals("FlagWatcher") ? "" : "watchers.") + info.getWatcher();

                toRemove.putIfAbsent(path, new ArrayList<>());

                ArrayList<Map.Entry<String, String>> list = toRemove.get(path);

                list.add(new HashMap.SimpleEntry(info.getMethod(), info.getDescriptor()));
            }

            for (Map.Entry<String, ArrayList<Map.Entry<String, String>>> entry : toRemove.entrySet()) {
                Class result = asm.createClassWithoutMethods(entry.getKey(), entry.getValue());
            }
        }
        catch (IOException | NoClassDefFoundError | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
