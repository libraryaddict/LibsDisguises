package me.libraryaddict.disguise.utilities;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import javax.imageio.ImageIO;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.mineskin.models.responses.MineSkinQueueResponse;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.SkinVariant;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkinUtils {
    public interface SkinCallback {
        void onError(LibsMsg msg, Object... args);

        void onInfo(LibsMsg msg, Object... args);

        void onSuccess(UserProfile profile);
    }

    private static int skinsSinceLastPromotion = 0;
    private static int totalSkinsUsedSince;
    private static long timeSinceLastPromotion = 0;

    public static void attemptPromoteMineskin(CommandSender sender) {
        totalSkinsUsedSince++;

        if (skinsSinceLastPromotion++ < 10 || timeSinceLastPromotion + TimeUnit.DAYS.toMillis(2) > System.currentTimeMillis() ||
            DisguiseUtilities.getMineSkinAPI().getApiKey() != null) {
            return;
        }

        skinsSinceLastPromotion = 0;
        timeSinceLastPromotion = System.currentTimeMillis();

        String message = ChatColor.AQUA +
            "Enjoying the ability to create player skins via file & url? You're using MineSkin which is run by Haylee // inventivetalent!" +
            " If you have the time, a small donation would be appreciated to help cover server costs https://support.inventivetalent.org/";

        LibsDisguises.getInstance().getLogger().info("Promoted the support for MineSkin, " + totalSkinsUsedSince +
            " skins were requested since server startup. Opt out by setting a MineSkin api key inside LibsDisguises/configs/players.yml");

        // No opt-out!
        if (sender == null) {
            LibsDisguises.getInstance().getLogger().info(message);
        } else {
            sender.sendMessage(message);
        }
    }

    public static void handleFile(File file, SkinVariant modelType, SkinCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    MineSkinQueueResponse response = DisguiseUtilities.getMineSkinAPI().generateFromFile(callback, file, modelType);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (response == null) {
                                return;
                            } else if (!response.hasGameProfile()) {
                                callback.onError(LibsMsg.SKIN_API_FAIL);
                                return;
                            }

                            handleProfile(response.getGameProfile(), modelType, callback);
                        }
                    }.runTask(LibsDisguises.getInstance());
                } catch (Throwable e) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            callback.onError(LibsMsg.SKIN_API_BAD_FILE);
                        }
                    }.runTask(LibsDisguises.getInstance());
                }
            }
        }.runTaskAsynchronously(LibsDisguises.getInstance());
    }

    public static void handleUrl(String url, SkinVariant modelType, SkinCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                MineSkinQueueResponse response = DisguiseUtilities.getMineSkinAPI().generateFromUrl(callback, url, modelType);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (response == null) {
                            return;
                        } else if (!response.hasGameProfile()) {
                            callback.onError(LibsMsg.SKIN_API_FAIL);
                        }

                        handleProfile(response.getGameProfile(), modelType, callback);
                    }
                }.runTask(LibsDisguises.getInstance());
            }
        }.runTaskAsynchronously(LibsDisguises.getInstance());
    }

    public static UserProfile getUUID(String urlString, String name) {
        return getUUID(urlString, name, new AtomicInteger());
    }

    public static UserProfile getUUID(String urlString, String name, AtomicInteger responseCodeInteger) {
        HttpURLConnection con = null;

        try {
            String path = String.format(urlString, name);
            URL url = new URL(path);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "plugin/LibsDisguises/" + LibsDisguises.getInstance().getDescription().getVersion());

            int responseCode = con.getResponseCode();

            responseCodeInteger.set(responseCode);

            if (responseCode == 404) {
                return null;
            }

            boolean errored = responseCode >= 400 && responseCode < 600;

            try (InputStream stream = (errored ? con.getErrorStream() : con.getInputStream())) {
                // Read it to string
                String response =
                    new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                if (errored || !response.startsWith("{")) {
                    LibsDisguises.getInstance().getLogger()
                        .severe(String.format("Received error code %s when attempting to fetch %s: %s", responseCode, path, response));
                    return null;
                }

                Map<String, String> map = new Gson().fromJson(response, Map.class);

                if (!map.containsKey("id")) {
                    return null;
                }

                String id = map.get("id");

                // Conversion from old old data
                if (!id.contains("-")) {
                    id = Pattern.compile("([\\da-fA-F]{8})([\\da-fA-F]{4})([\\da-fA-F]{4})([\\da-fA-F]{4})([\\da-fA-F]+)").matcher(id)
                        .replaceFirst("$1-$2-$3-$4-$5");
                }

                return new UserProfile(UUID.fromString(id), map.get("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return null;
    }

    public static void handleName(String playerName, SkinVariant modelType, SkinCallback callback) {
        UserProfile gameProfile = DisguiseUtilities.getProfileFromMojang(playerName, gameProfile1 -> {
            // Isn't handled by callback
            if (!Pattern.matches("\\w{1,16}", playerName)) {
                return;
            }

            if (gameProfile1 == null || gameProfile1.getTextureProperties().isEmpty()) {
                callback.onError(LibsMsg.CANNOT_FIND_PLAYER_NAME, playerName);
                return;
            }

            handleProfile(gameProfile1, modelType, callback);
        });

        // Is handled in callback
        if (gameProfile == null) {
            return;
        }

        if (gameProfile.getTextureProperties().isEmpty()) {
            callback.onError(LibsMsg.CANNOT_FIND_PLAYER_NAME, playerName);
            return;
        }

        handleProfile(gameProfile, modelType, callback);
    }

    public static void handleProfile(GameProfile profile, SkinVariant modelType, SkinCallback callback) {
        handleProfile(ReflectionManager.getUserProfile(profile), modelType, callback);
    }

    public static void handleProfile(UserProfile profile, SkinVariant modelType, SkinCallback callback) {
        callback.onSuccess(profile);
    }

    public static void handleUUID(UUID uuid, SkinVariant modelType, SkinCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                UserProfile profile = ReflectionManager.getSkullBlob(new UserProfile(uuid, "AutoGenerated"));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (profile == null || profile.getTextureProperties().isEmpty()) {
                            callback.onError(LibsMsg.CANNOT_FIND_PLAYER_UUID, uuid.toString());
                            return;
                        }

                        handleProfile(profile, modelType, callback);
                    }
                }.runTask(LibsDisguises.getInstance());
            }
        }.runTaskAsynchronously(LibsDisguises.getInstance());
    }

    public static boolean isUsable() {
        return getUsableStatus() == null;
    }

    public static String getUsableStatus() {
        if (DisguiseUtilities.getMineSkinAPI().isInUse()) {
            return LibsMsg.SKIN_API_IN_USE.get();
        }

        if (DisguiseUtilities.getMineSkinAPI().nextRequestIn() > 0) {
            return LibsMsg.SKIN_API_TIMER.get(DisguiseUtilities.getMineSkinAPI().nextRequestIn());
        }

        return null;
    }

    @Deprecated
    public static void grabSkin(String param, SkinCallback callback) {
        grabSkin(null, param, callback);
    }

    public static void grabSkin(CommandSender sender, String param, SkinCallback callback) {
        SkinVariant modelType = SkinVariant.UNKNOWN;
        // If we shouldn't try to resolve this ourselves
        boolean specificModel = false;

        // Try to find ':slim', ':classic' and ':unknown'
        for (SkinVariant variant : SkinVariant.values()) {
            if (!param.toLowerCase(Locale.ENGLISH).endsWith(":" + variant.name().toLowerCase(Locale.ENGLISH))) {
                continue;
            }

            modelType = variant;
            param = param.substring(0, param.length() - (variant.name().length() + 1));
            specificModel = true;
            break;
        }

        if (param.matches("https?://.+")) {
            // Its an url
            callback.onInfo(LibsMsg.SKIN_API_USING_URL);

            handleUrl(param, modelType, callback);
            attemptPromoteMineskin(sender);
        } else {
            // Check if it contains legal file characters
            if (!param.matches("[a-zA-Z\\d -_]+(\\.png)?")) {
                callback.onError(LibsMsg.SKIN_API_INVALID_NAME);
                return;
            }

            File expectedFolder = new File(LibsDisguises.getInstance().getDataFolder(), "/Skins/");

            File file = new File(expectedFolder, param + (param.toLowerCase(Locale.ENGLISH).endsWith(".png") ? "" : ".png"));

            if (!file.exists()) {
                file = null;

                if (param.toLowerCase(Locale.ENGLISH).endsWith(".png")) {
                    callback.onError(LibsMsg.SKIN_API_BAD_FILE_NAME);
                    return;
                }
            } else if (!file.getParentFile().getAbsolutePath().equals(expectedFolder.getAbsolutePath())) {
                callback.onError(LibsMsg.SKIN_API_INVALID_NAME);
                return;
            }

            if (file != null) {
                // We're using a file!
                if (!specificModel && modelType == SkinVariant.UNKNOWN) {
                    modelType = detectSkinVariant(file);
                }

                callback.onInfo(LibsMsg.SKIN_API_USING_FILE);
                handleFile(file, modelType, callback);
                attemptPromoteMineskin(sender);
            } else {
                // We're using a player name or UUID!
                if (param.contains("-")) {
                    try {
                        UUID uuid = UUID.fromString(param);

                        callback.onInfo(LibsMsg.SKIN_API_USING_UUID);
                        handleUUID(uuid, modelType, callback);
                        attemptPromoteMineskin(sender);
                        return;
                    } catch (Exception ignored) {
                    }
                }

                UserProfile profile = DisguiseUtilities.getUserProfile(param);

                if (profile != null) {
                    callback.onInfo(LibsMsg.SKIN_API_USING_EXISTING_NAME);
                    callback.onSuccess(profile);
                    return;
                }

                callback.onInfo(LibsMsg.SKIN_API_USING_NAME);

                handleName(param, modelType, callback);
                attemptPromoteMineskin(sender);
            }
        }
    }

    /**
     * Detects if a Minecraft skin is Steve (Classic) or Alex (Slim).
     */
    public static SkinVariant detectSkinVariant(File skinFile) {
        BufferedImage image;

        try {
            image = ImageIO.read(skinFile);
        } catch (IOException e) {
            return SkinVariant.UNKNOWN;
        }

        // If image failed to load
        if (image == null) {
            return SkinVariant.UNKNOWN;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // Minecraft skins must be 64 wide, 32 or 64 high
        if (width != 64 || (height != 32 && height != 64)) {
            return SkinVariant.UNKNOWN;
        }

        if (height == 32) {
            return SkinVariant.CLASSIC;
        }

        // The pixel to check for "solid color"
        Integer pixelToCheck = image.getRGB(0, 0);
        // If the unused 8x8 is alpha, then we're checking alpha
        int firstBlockAlpha = 0;
        int firstBlockNonAlpha = 0;

        // We're checking the unused 8x8 block at the start of the image
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int pixel = image.getRGB(x, y);

                if ((((pixel) >> 24) & 0xFF) == 0) {
                    firstBlockAlpha++;
                } else {
                    firstBlockNonAlpha++;
                }

                // Check if it's all a solid color
                if (pixelToCheck != null && pixelToCheck != pixel) {
                    pixelToCheck = null;
                }
            }
        }

        if (firstBlockAlpha > 0 && firstBlockNonAlpha > 0) {
            String message = String.format(
                "The skin '%s' has a mixture of transparent and solid colors in the first 8x8 block of pixels, unknown if this is a " +
                    "classic or slim skin.", skinFile.getName());

            if (LibsDisguises.getInstance() != null) {
                LibsDisguises.getInstance().getLogger().warning(message);
            } else {
                System.out.println("[LibsDisguises] " + message);
            }

            return SkinVariant.UNKNOWN;
        }

        boolean checkingForAlpha = firstBlockNonAlpha == 0;

        // If the 8x8 has non-transparent, and is not consistent colors
        if (!checkingForAlpha && pixelToCheck == null) {
            return SkinVariant.UNKNOWN;
        }

        boolean colorReused = false;
        int rightArmAlpha = 0;
        int rightArmNonAlpha = 0;

        // We're checking the right arm
        for (int x = checkingForAlpha ? 54 : 40; x < 56 && !colorReused; x++) {
            for (int y = 20; y < 32 && !colorReused; y++) {
                int pixel = image.getRGB(x, y);

                // We're checking solid colors, we're validating to ensure a solid color isn't used in the right arm.
                if (x < 54) {
                    if (pixel == pixelToCheck) {
                        colorReused = true;
                    }

                    continue;
                }
                // If we're checking alpha
                if (firstBlockNonAlpha == 0) {
                    // If the pixel is transparent
                    if (((pixel >> 24) & 0xFF) == 0) {
                        rightArmAlpha++;
                    } else {
                        rightArmNonAlpha++;
                    }

                    continue;
                }

                // If we're not checking alpha, then any color disparity is not slim
                if (pixel != pixelToCheck) {
                    return SkinVariant.CLASSIC;
                }

                rightArmAlpha++;
            }
        }

        if (colorReused) {
            String message = String.format(
                "The skin '%s' has no transparency in the first 8x8 block, and that color is used in the skin, unknown if this is a " +
                    "classic or slim skin.", skinFile.getName());

            if (LibsDisguises.getInstance() != null) {
                LibsDisguises.getInstance().getLogger().warning(message);
            } else {
                System.out.println("[LibsDisguises] " + message);
            }

            return SkinVariant.UNKNOWN;
        }

        // If there are no transparent pixels, it's a steve skin
        if (rightArmAlpha == 0) {
            return SkinVariant.CLASSIC;
        }

        // If there are solid colors among the transparent pixels
        if (rightArmNonAlpha > 0) {
            String message = String.format(
                "The skin '%s' has a mixture of transparent and solid colors in the right arm, unknown if this is a classic or slim skin.",
                skinFile.getName());

            if (LibsDisguises.getInstance() != null) {
                LibsDisguises.getInstance().getLogger().warning(message);
            } else {
                System.out.println("[LibsDisguises] " + message);
            }

            return SkinVariant.UNKNOWN;
        }

        return SkinVariant.SLIM;
    }
}
