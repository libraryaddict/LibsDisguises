package me.libraryaddict.disguise.utilities.reflection.asm;

import me.libraryaddict.disguise.utilities.reflection.ClassGetter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Created by libraryaddict on 20/05/2021.
 */
public class LibsDisguisesCompat extends JavaPlugin {
    @Override
    public void onLoad() {
        ClassGetter.getClassesForPackage("me.libraryaddict.disguise.disguisetypes");
    }
}
