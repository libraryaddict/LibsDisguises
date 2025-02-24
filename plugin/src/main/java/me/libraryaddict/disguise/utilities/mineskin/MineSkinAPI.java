package me.libraryaddict.disguise.utilities.mineskin;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.mineskin.models.requests.MineSkinRequestFile;
import me.libraryaddict.disguise.utilities.mineskin.models.requests.MineSkinRequestPlayer;
import me.libraryaddict.disguise.utilities.mineskin.models.requests.MineSkinRequestUrl;
import me.libraryaddict.disguise.utilities.mineskin.models.requests.MineSkinSubmitQueue;
import me.libraryaddict.disguise.utilities.mineskin.models.responses.MineSkinQueueResponse;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinNotification;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.SkinVariant;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * This isn't a stanealone class
 */
public class MineSkinAPI {
    @FunctionalInterface
    interface QuadFunction {
        void apply(ByteArrayOutputStream output, PrintWriter writer, String boundary, String name, Object data) throws IOException;
    }

    private static final String CRLF = "\r\n";

    @RequiredArgsConstructor
    enum FormData {
        FILE((output, writer, boundary, name, data) -> {
            File file = (File) data;
            String type = "png";

            String[] filename = file.getName().split("\\.");

            // Expected to be of type 'png', but support other formats even if the remote will reject!
            if (filename.length > 1 && filename[filename.length - 1].length() > 1) {
                type = filename[filename.length - 1].toLowerCase(Locale.ENGLISH);
            }

            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"; filename=\"").append(file.getName())
                .append("\"").append(CRLF);
            writer.append("Content-Type: image/").append(type).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            Files.copy(file.toPath(), output);
            output.flush(); // Important before continuing with writer!
            writer.append(CRLF); // CRLF is important! It indicates end of boundary.
        }),
        STRING((output, writer, boundary, name, data) -> {
            // Make all enums lowercase
            String asString = data instanceof Enum ? ((Enum) data).name().toLowerCase(Locale.ENGLISH) : data.toString();

            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=UTF-8").append(CRLF);
            writer.append(CRLF);
            writer.append(asString);
            writer.append(CRLF);
        });
        @Getter
        private final QuadFunction function;

        static void write(ByteArrayOutputStream output, PrintWriter writer, String boundary, String name, Object data) throws IOException {
            if (data == null) {
                return;
            }

            if (data instanceof File) {
                FILE.function.apply(output, writer, boundary, name, data);
            } else {
                STRING.function.apply(output, writer, boundary, name, data);
            }
        }
    }

    private long nextRequest;
    private final ReentrantLock lock = new ReentrantLock();
    @Getter
    @Setter
    private boolean debugging = LibsDisguises.getInstance() == null;
    @Getter
    @Setter
    private String apiKey;
    private static long lastErrorPage;
    private final String userAgent;
    @Getter
    private boolean mentionedApiKey = false;

    public MineSkinAPI() {
        this("third-party");
    }

    public MineSkinAPI(String extraInfo) {
        if (extraInfo == null || extraInfo.trim().isEmpty()) {
            extraInfo = "";
        } else {
            if (!extraInfo.contains("(")) {
                extraInfo = " (" + extraInfo.trim() + ")";
            } else {
                extraInfo = " " + extraInfo.trim();
            }
        }

        String version = LibsDisguises.getInstance() == null ? "Not-LD-Itself" : LibsDisguises.getInstance().getDescription().getVersion();

        userAgent = "LibsDisguises/" + version + extraInfo;
    }

    public boolean hasApiKey() {
        return StringUtils.isNotBlank(apiKey);
    }

    private void addConnectionHeaders(HttpURLConnection connection) throws IOException {
        connection.setConnectTimeout(19000);
        connection.setReadTimeout(19000);
        connection.setDoOutput(true);

        connection.setRequestProperty("User-Agent", this.userAgent);

        String key = getApiKey();

        if (!hasApiKey()) {
            return;
        }

        // Assuming they prepended 'bearer' is just asking to be made a fool of
        if (!key.toLowerCase(Locale.ENGLISH).startsWith("bearer ")) {
            key = "Bearer " + key;
        }

        connection.setRequestProperty("Authorization", key);
    }

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
    public MineSkinQueueResponse generateFromUrl(SkinUtils.SkinCallback callback, String url, SkinVariant modelType) {
        return doPost(callback, new MineSkinRequestUrl().setUrl(url).setVariant(modelType));
    }

    private void printDebug(String message) {
        if (!isDebugging()) {
            return;
        }

        if (LibsDisguises.getInstance() != null) {
            LibsDisguises.getInstance().getLogger().info("[MineSkinAPI] " + message);
        } else {
            System.err.println(message);
        }
    }

    private MineSkinQueueResponse readResponse(HttpURLConnection connection, SkinUtils.SkinCallback callback) throws IOException {
        int responseCode = connection.getResponseCode();
        printDebug("Received status code: " + responseCode);

        String response;
        boolean errored = responseCode >= 400 && responseCode < 600;

        if (errored) {
            // Get the input stream, what we receive
            try (InputStream errorStream = connection.getErrorStream()) {
                // Read it to string
                response = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));

                if (LibsDisguises.getInstance() != null) {
                    LibsDisguises.getInstance().getLogger().severe("MineSkin error: " + response);
                } else {
                    System.out.println("MineSkin error: " + response);
                }

                printDebug("Received error: " + response);
            }
        } else {
            // Get the input stream, what we receive
            try (InputStream inputStream = connection.getInputStream()) {
                // Read it to string
                response = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
                printDebug("Received: " + response);
            }
        }

        MineSkinQueueResponse mineSkinResponse;

        if (!response.startsWith("{")) {
            mineSkinResponse = new MineSkinQueueResponse();

            if (LibsDisguises.getInstance() != null) {
                LibsDisguises.getInstance().getLogger()
                    .severe("MineSkin returned malformed response with response code " + responseCode + ": " + response);
            } else {
                System.out.println("MineSkin returned malformed response with response code " + responseCode + ": " + response);
            }
        } else {
            mineSkinResponse = new Gson().fromJson(response, MineSkinQueueResponse.class);
            mineSkinResponse.setResponseCode(responseCode);
        }

        if (mineSkinResponse.getRateLimit() != null) {
            nextRequest = mineSkinResponse.getRateLimit().getNext().getAbsolute() + 300;
        } else {
            nextRequest = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(4);
        }

        String code = String.valueOf(responseCode);
        String msg = response.startsWith("{") ? "" : response;

        if (responseCode == 429) {
            callback.onError(LibsMsg.SKIN_API_FAIL_TOO_FAST);

            if (!hasApiKey() && !mentionedApiKey) {
                mentionedApiKey = true;
                callback.onInfo(LibsMsg.SKIN_API_SUGGEST_KEY);
            }
            return null;
        } else if (responseCode == 408 || responseCode == 504 || responseCode == 599) {
            // Send the timeout message, and another message if relevant.
            callback.onError(LibsMsg.SKIN_API_TIMEOUT);
        }

        if (mineSkinResponse.getErrors() != null && mineSkinResponse.getErrors().length > 0) {
            List<String> codes = new ArrayList<>();
            List<String> messages = new ArrayList<>();

            for (MineSkinNotification fail : mineSkinResponse.getErrors()) {
                codes.add(fail.getCode());
                messages.add(fail.getMessage());
            }

            callback.onError(LibsMsg.SKIN_API_FAIL_CODE, String.join(", ", codes), String.join(", ", messages));
            return null;
        } else if (errored) {
            callback.onError(LibsMsg.SKIN_API_FAIL_CODE_EXCEPTIONAL, code, response);
            return null;
        }

        if (mineSkinResponse.getJob() == null || (!mineSkinResponse.getJob().isJobRunning() && mineSkinResponse.getSkin() == null)) {
            // If we got an error that we don't know how to handle..
            callback.onError(LibsMsg.SKIN_API_FAIL);
            return null;
        }

        return mineSkinResponse;
    }

    private MineSkinQueueResponse getJobStatus(String jobId, SkinUtils.SkinCallback callback) throws IOException {
        URL url = new URL("https://api.mineskin.org/v2/queue/" + jobId);
        // Creating a connection
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        addConnectionHeaders(con);

        return this.readResponse(con, callback);
    }

    @SneakyThrows
    private void sleepUntilReady() {
        long sleep = nextRequest - System.currentTimeMillis();

        if (sleep > 0) {
            printDebug("Sleeping for " + sleep + "ms before calling the API due to a recent request");

            Thread.sleep(sleep);
        }
    }

    private void writeFormData(HttpURLConnection connection, MineSkinRequestFile requestFile) throws IOException {
        String boundary = "LD_" + Long.toHexString(System.currentTimeMillis()).toLowerCase(); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        // Contrary to normality, MineSkin doesn't believe formdatas should be allowed if it's not a file
        // So we have to use json if we're not doing a file
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
                FormData.write(output, writer, boundary, "variant", requestFile.getVariant());
                FormData.write(output, writer, boundary, "name", requestFile.getName());
                FormData.write(output, writer, boundary, "visibility", requestFile.getVisibility());
                FormData.write(output, writer, boundary, "file", requestFile.getFile());

                // End of multipart/form-data.
                writer.append("--").append(boundary).append("--").append(CRLF).flush();
            }

            byte[] formContent = output.toByteArray();

            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.addRequestProperty("Content-Length", Integer.toString(formContent.length));

            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(formContent);
                stream.flush(); // Reportably not reliable for a stream to flush before closing
            }
        }
    }

    private void writeJson(HttpURLConnection connection, MineSkinSubmitQueue request) throws IOException {
        byte[] bytes = new Gson().toJson(request).getBytes(StandardCharsets.UTF_8);

        connection.setRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("Content-Length", Integer.toString(bytes.length));

        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(bytes);
            stream.flush(); // Reportably not reliable for a stream to flush before closing
        }
    }

    private MineSkinQueueResponse doPost(SkinUtils.SkinCallback callback, MineSkinSubmitQueue mineskinRequest) {
        lock.lock();

        printDebug("Making request of type " + mineskinRequest.getClass().getSimpleName());

        if (getApiKey() != null) {
            printDebug("Using a MineSkin api key!");
        }

        sleepUntilReady();

        HttpURLConnection connection = null;
        long nextRequestIn = TimeUnit.SECONDS.toMillis(10);

        try {
            URL url = new URL("https://api.mineskin.org/v2/queue");
            // Creating a connection
            connection = (HttpURLConnection) url.openConnection();

            addConnectionHeaders(connection);

            if (mineskinRequest instanceof MineSkinRequestFile) {
                writeFormData(connection, (MineSkinRequestFile) mineskinRequest);
            } else {
                writeJson(connection, mineskinRequest);
            }

            MineSkinQueueResponse mineSkinResponse = readResponse(connection, callback);

            while (mineSkinResponse != null && mineSkinResponse.getJob() != null && mineSkinResponse.getJob().isJobRunning()) {
                sleepUntilReady();

                mineSkinResponse = getJobStatus(mineSkinResponse.getJob().getId(), callback);
            }

            return mineSkinResponse;
        } catch (SocketTimeoutException ex) {
            if (isDebugging()) {
                ex.printStackTrace();
            }

            callback.onError(
                mineskinRequest instanceof MineSkinRequestUrl ? LibsMsg.SKIN_API_TIMEOUT_ERROR : LibsMsg.SKIN_API_IMAGE_TIMEOUT);
            return null;
        } catch (Exception ex) {
            try {
                if (connection != null) {
                    try {
                        int code = connection.getResponseCode();

                        if (connection.getResponseCode() == 524 || connection.getResponseCode() == 408 ||
                            connection.getResponseCode() == 504 || connection.getResponseCode() == 599) {
                            if (getApiKey() != null && connection.getResponseCode() == 504) {
                                callback.onError(LibsMsg.SKIN_API_TIMEOUT_API_KEY_ERROR);
                            } else {
                                callback.onError(LibsMsg.SKIN_API_TIMEOUT_ERROR);
                            }

                            return null;
                        }
                    } catch (IOException ignored) {
                    }
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }

            if (LibsDisguises.getInstance() != null && LibsDisguises.getInstance().getLogger() != null) {
                LibsDisguises.getInstance().getLogger().warning("Failed to access MineSkin.org");
            }

            ex.printStackTrace();

            callback.onError(LibsMsg.SKIN_API_FAIL);
        } finally {
            nextRequest = System.currentTimeMillis() + nextRequestIn + 1000;
            lock.unlock();

            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public MineSkinQueueResponse generateFromUUID(UUID uuid, SkinVariant modelType) throws IllegalArgumentException {
        return doPost(null, new MineSkinRequestPlayer().setUser(uuid.toString()).setVariant(modelType));
    }

    /**
     * Uploads png file
     *
     * @param file
     */
    public MineSkinQueueResponse generateFromFile(SkinUtils.SkinCallback callback, File file, SkinVariant modelType) {
        return doPost(callback, new MineSkinRequestFile().setFile(file).setVariant(modelType));
    }
}
