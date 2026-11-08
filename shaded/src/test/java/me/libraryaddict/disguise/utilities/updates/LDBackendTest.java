package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;

@Execution(ExecutionMode.CONCURRENT)
public class LDBackendTest {
    @RequiredArgsConstructor
    static class UpdateRequest {
        private final String branch;
    }

    private LDBackend.ReceivedData fetchUpdate(String branch) {
        try {
            return new Gson().fromJson(LDBackend.postUpdate(new UpdateRequest(branch), 1000), LDBackend.ReceivedData.class);
        } catch (IOException ex) {
            return Assumptions.abort("Can't reach the api: " + ex.getMessage());
        }
    }

    private void checkUpdate(LDBackend.ReceivedData update, String branch) {
        Assertions.assertTrue(update.isSuccess());
        Assertions.assertEquals(branch, update.getBranch());
        Assertions.assertNotEquals(0, update.getChangelog().length);
        Assertions.assertTrue(update.getDownload().startsWith("https://"), update.getDownload());
        Assertions.assertTrue(update.getDownloadName().endsWith(".jar"), update.getDownloadName());
    }

    @Test
    public void testReleaseUpdate() {
        LDBackend.ReceivedData update = fetchUpdate("releases");

        checkUpdate(update, "releases");
        Assertions.assertTrue(update.getVersion().matches("\\d+(\\.\\d+)+"), update.getVersion());
    }

    @Test
    public void testSnapshotUpdate() {
        LDBackend.ReceivedData update = fetchUpdate("snapshots");

        checkUpdate(update, "snapshots");
        Assertions.assertTrue(update.getVersion().matches("\\d+"), update.getVersion());
    }
}
