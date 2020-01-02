package me.libraryaddict.disguise.utilities.mineskin;

import com.google.gson.Gson;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by libraryaddict on 28/12/2019.
 * <p>
 * This isn't a stanealone class
 */
public class MineSkinAPI {
    private class APIError {
        int code;
        String error;
    }

    /**
     * Time in millis until next request can be made
     */
    private long nextRequest;
    private final ReentrantLock lock = new ReentrantLock();

    public boolean isInUse() {
        return lock.isLocked();
    }

    public int nextRequestIn() {
        long timeTillNext = nextRequest - System.currentTimeMillis();

        if (timeTillNext < 0) {
            return 0;
        }

        return (int) Math.ceil(timeTillNext / 1000D);
    }

    /**
     * Fetches image from the provided url
     *
     * @param url
     */
    public MineSkinResponse generateFromUrl(SkinUtils.SkinCallback callback, String url) {
        return doPost(callback, "/generate/url", url, null);
    }

    private MineSkinResponse doPost(SkinUtils.SkinCallback callback, String path, String skinUrl, File file) {
        lock.lock();

        HttpURLConnection connection = null;

        try {
            URL url = new URL("https://api.mineskin.org" + path);
            // Creating a connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "LibsDisguises");
            connection.setConnectTimeout(19000);
            connection.setReadTimeout(19000);

            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            String charset = "UTF-8";
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            try (OutputStream output = connection.getOutputStream(); PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(output, charset), true)) {
                // Send normal param.
                writer.append("--").append(boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"visibility\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=").append(charset).append(CRLF);
                writer.append(CRLF).append("1").append(CRLF).flush();

                if (file != null) {
                    // Send binary file.
                    writer.append("--").append(boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName())
                            .append("\"").append(CRLF);
                    writer.append("Content-Type: image/png").append(CRLF);
                    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                    writer.append(CRLF).flush();
                    Files.copy(file.toPath(), output);
                    output.flush(); // Important before continuing with writer!
                    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
                } else if (skinUrl != null) {
                    // Send normal param.
                    writer.append("--").append(boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"url\"").append(CRLF);
                    writer.append(CRLF).append(skinUrl).append(CRLF).flush();
                }

                // End of multipart/form-data.
                writer.append("--").append(boundary).append("--").append(CRLF).flush();
            }

            if (connection.getResponseCode() == 500) {
                APIError error = new Gson().fromJson(IOUtils.toString(connection.getErrorStream()), APIError.class);

                if (error.code == 403) {
                    callback.onError(LibsMsg.SKIN_API_FAIL_CODE, "" + error.code, LibsMsg.SKIN_API_403.get());
                    return null;
                } else if (error.code == 404) {
                    callback.onError(LibsMsg.SKIN_API_FAIL_CODE, "" + error.code, LibsMsg.SKIN_API_404.get());
                    return null;
                } else if (error.code == 408 || error.code == 504 || error.code == 599) {
                    callback.onError(LibsMsg.SKIN_API_FAIL_CODE, "" + error.code, LibsMsg.SKIN_API_TIMEOUT.get());
                    return null;
                } else {
                    callback.onError(LibsMsg.SKIN_API_FAIL_CODE, "" + error.code,
                            "Your image has the error: " + error.error);
                    return null;
                }
            } else if (connection.getResponseCode() == 400) {
                if (skinUrl != null) {
                    callback.onError(LibsMsg.SKIN_API_BAD_URL);
                    return null;
                } else if (file != null) {
                    callback.onError(LibsMsg.SKIN_API_BAD_FILE);
                    return null;
                }
            }

            // Get the input stream, what we receive
            try (InputStream input = connection.getInputStream()) {
                // Read it to string
                String response = IOUtils.toString(input);

                MineSkinResponse skinResponse = new Gson().fromJson(response, MineSkinResponse.class);

                nextRequest = System.currentTimeMillis() + (long) (skinResponse.getNextRequest() * 1000);

                return skinResponse;
            }
        }
        catch (SocketTimeoutException ex) {
            callback.onError(skinUrl == null ? LibsMsg.SKIN_API_TIMEOUT : LibsMsg.SKIN_API_IMAGE_TIMEOUT);
            return null;
        }
        catch (Exception ex) {
            nextRequest = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);

            try {
                if (connection != null && (connection.getResponseCode() == 524 || connection.getResponseCode() == 408 ||
                        connection.getResponseCode() == 504 || connection.getResponseCode() == 599)) {
                    callback.onError(LibsMsg.SKIN_API_TIMEOUT);
                    return null;
                }
            }
            catch (IOException e) {
            }

            DisguiseUtilities.getLogger().warning("Failed to access MineSkin.org");
            ex.printStackTrace();

            callback.onError(LibsMsg.SKIN_API_FAIL);
        }
        finally {
            lock.unlock();
        }

        return null;
    }

    public MineSkinResponse generateFromUUID(UUID uuid) throws IllegalArgumentException {
        lock.lock();

        try {
            URL url = new URL("https://api.mineskin.org/generate/user/:" + uuid.toString());
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "LibsDisguises");
            // We're writing a body that contains the API access key (Not required and obsolete, but!)
            con.setDoOutput(true);

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String response = IOUtils.toString(input);

                MineSkinResponse skinResponse = new Gson().fromJson(response, MineSkinResponse.class);

                nextRequest = System.currentTimeMillis() + (long) (skinResponse.getNextRequest() * 1000);

                return skinResponse;
            }
        }
        catch (Exception ex) {
            nextRequest = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);

            if (ex.getMessage() != null &&
                    ex.getMessage().contains("Server returned HTTP response code: 400 for URL")) {
                throw new IllegalArgumentException();
            }

            DisguiseUtilities.getLogger().warning("Failed to access MineSkin.org");
            ex.printStackTrace();
        }
        finally {
            lock.unlock();
        }

        return null;
    }

    /**
     * Uploads png file
     *
     * @param file
     */
    public MineSkinResponse generateFromFile(SkinUtils.SkinCallback callback, File file) {
        return doPost(callback, "/generate/upload", null, file);
    }
}
