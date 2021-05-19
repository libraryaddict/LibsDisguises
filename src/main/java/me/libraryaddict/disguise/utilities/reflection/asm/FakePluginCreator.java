package me.libraryaddict.disguise.utilities.reflection.asm;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Created by libraryaddict on 20/05/2021.
 */
public class FakePluginCreator {
    public String getPluginYAML() {
        return "name: LibsDisguisesVersioning\nmain: " + getPluginClassPath().replace(".class", "").replace("/", ".") +
                "\ndescription: Plugin created by Libs Disguises for " +
                "compatibility with different versions\nversion: 1.0.0\nauthor: libraryaddict\napi-version: '1.13'\nsoftdepend: [ProtocolLib, LibsDisguises]";
    }

    public File getDestination() {
        return new File(LibsDisguises.getInstance().getDataFolder(), "libs/LibsDisguisesCompat.jar");
    }

    public String getPluginClassPath() {
        return "me/libraryaddict/disguise/utilities/reflection/asm/LibsDisguisesCompat.class";
    }

    public String getVersion() throws Exception {
        File dest = getDestination();

        if (!dest.exists()) {
            return null;
        }

        JarFile jarFile = new JarFile(dest);

        try (InputStream stream = jarFile.getInputStream(jarFile.getEntry("version.txt"))) {
            return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
        }
    }

    public String getOurVersion() {
        YamlConfiguration pluginYml = ReflectionManager.getPluginYAML(LibsDisguises.getInstance().getFile());

        String buildNo = StringUtils.stripToNull(pluginYml.getString("build-number"));

        return buildNo != null && buildNo.matches("[0-9]+") ? ReflectionManager.getBukkitVersion() + " " + Integer.parseInt(buildNo) :
                ReflectionManager.getBukkitVersion() + " CUSTOM";
    }

    public void createJar(String ourVersion, Map<String, byte[]> classes) throws Exception {
        File dest = getDestination();

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        if (dest.exists()) {
            dest.delete();
        }

        JarOutputStream out = new JarOutputStream(new FileOutputStream(dest));

        out.putNextEntry(new ZipEntry("plugin.yml"));
        out.write(getPluginYAML().getBytes(StandardCharsets.UTF_8));
        out.closeEntry();

        // Write our main plugin class
        try (JarFile jar = new JarFile(LibsDisguises.getInstance().getFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            String mainPath = getPluginClassPath().replace(".class", "");

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (!entry.getName().equals(mainPath) && !entry.getName().startsWith("me/libraryaddict/disguise/disguisetypes/")) {
                    continue;
                }

                if (classes.containsKey(entry.getName())) {
                    continue;
                }

                out.putNextEntry(new ZipEntry(entry.getName().equals(mainPath) ? getPluginClassPath() : entry.getName()));

                try (InputStream stream = jar.getInputStream(entry)) {
                    int nRead;
                    byte[] data = new byte[1024];
                    while ((nRead = stream.read(data, 0, data.length)) != -1) {
                        out.write(data, 0, nRead);
                    }
                }

                out.closeEntry();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
            out.putNextEntry(new ZipEntry(entry.getKey()));
            out.write(entry.getValue());
            out.closeEntry();
        }

        out.putNextEntry(new ZipEntry("version.txt"));
        out.write(ourVersion.getBytes(StandardCharsets.UTF_8));
        out.closeEntry();

        out.close();
    }
}
