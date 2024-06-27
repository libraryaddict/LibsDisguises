package me.libraryaddict.disguise.utilities.parser.params;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.manager.server.ServerManagerImpl;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoUserProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.doReturn;

public class DisguiseParamUserProfileTest {
    private final String oldProfileString =
        "{\"id\":\"a149f81b-f784-4f89-87c5-54afdd4db533\",\"name\":\"libraryaddict\",\"properties\":[{\"name\":\"textures\"," + "\"value" +
            "\":\"ewogICJ0aW1lc3RhbXAiIDogMTcxNjg3MTE0OTQyNywKICAicHJvZmlsZUlkIiA6ICJhMTQ5ZjgxYmY3ODQ0Zjg5ODdjNTU0YWZkZDRkYjUzMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJsaWJyYXJ5YWRkaWN0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q1MTk3NWM0NWJmNTZiYzM1N2UyMDdiNWEwYjBlYjViODZiZDNlY2MyZGFjNTI3NTFiYjQ4MGM4YmJjMGM2ZmMiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2EyZThkOTdlYzc5MTAwZTkwYTc1ZDM2OWQxYjNiYTgxMjczYzRmODJiYzFiNzM3ZTkzNGVlZDRhODU0YmUxYjYiCiAgICB9CiAgfQp9\",\"signature\":\"V8kiVYioSkrsCHEca12KT04+VNQSflOZnF/1nbvh+TAcxTygff+2+EpF6SoFKlJvsaKsmqCWZTv8NkdfbwiU6AL0PFz9Ef4U4X7tBAw0N3xehnxalAB6kFNQJesfCwNttAn3f7xsDIufg+BBVJ0mZtKPozHk+iLd0s6JRF6ENfx0RpyLYmZEFt1aHbpCILFNbgI1Mbp0XlI94CoFKSqlLIvMIQSB7bccTtbGpGiegjeyiQlKVO5gSwSSCGOBaEycrbZfXN8SGWj7dpGMJkLwS9hNpxo29VC5AzVYaPi3Mn1nI8LPFCWjKDRTRs4jMmX4vZyax/6spPfqzB2hcvJk8LaF6QjyxJQf08rTvrObloxspM9dwWAiteSrroT01lDTHgFHjsFfLZMxGLnvhTcRVDBDqpzbIdtKPCt4SM6Z5EzFH5HFs7Lw1byoeST/VRQNjbMMVfxaM5E1o8Sjeh41GMDZ0M3X8hZABxWHbXFvuqvNofFx0Qw8qpWMeU2+4ZTJOeiZAMDGkEofmIOMcsEw/AmrLjI+xpdDsWIdiUswgUq50D02ouAi+LGbCiMinsOJ/2SHYsCnBixmz9biTy9pQjKCECs3vTGf+m7pl8UG20dRnWHqClUxznDKZH0JDBlcPV0R2yLIqoFzZ13T0vwGIFVRxi/0hyxGahq2/I/oS8s=\"}],\"legacy\":false}";
    private final String oldOldProfileString =
        oldProfileString.replace("a149f81b-f784-4f89-87c5-54afdd4db533", "a149f81bf7844f8987c554afdd4db533");
    private final String newProfileString =
        "{\"uuid\":\"a149f81b-f784-4f89-87c5-54afdd4db533\",\"name\":\"libraryaddict\",\"textureProperties\":[{\"name\":\"textures\"," +
            "\"value" +
            "\":\"ewogICJ0aW1lc3RhbXAiIDogMTcxNjg3MTE0OTQyNywKICAicHJvZmlsZUlkIiA6ICJhMTQ5ZjgxYmY3ODQ0Zjg5ODdjNTU0YWZkZDRkYjUzMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJsaWJyYXJ5YWRkaWN0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q1MTk3NWM0NWJmNTZiYzM1N2UyMDdiNWEwYjBlYjViODZiZDNlY2MyZGFjNTI3NTFiYjQ4MGM4YmJjMGM2ZmMiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2EyZThkOTdlYzc5MTAwZTkwYTc1ZDM2OWQxYjNiYTgxMjczYzRmODJiYzFiNzM3ZTkzNGVlZDRhODU0YmUxYjYiCiAgICB9CiAgfQp9\",\"signature\":\"V8kiVYioSkrsCHEca12KT04+VNQSflOZnF/1nbvh+TAcxTygff+2+EpF6SoFKlJvsaKsmqCWZTv8NkdfbwiU6AL0PFz9Ef4U4X7tBAw0N3xehnxalAB6kFNQJesfCwNttAn3f7xsDIufg+BBVJ0mZtKPozHk+iLd0s6JRF6ENfx0RpyLYmZEFt1aHbpCILFNbgI1Mbp0XlI94CoFKSqlLIvMIQSB7bccTtbGpGiegjeyiQlKVO5gSwSSCGOBaEycrbZfXN8SGWj7dpGMJkLwS9hNpxo29VC5AzVYaPi3Mn1nI8LPFCWjKDRTRs4jMmX4vZyax/6spPfqzB2hcvJk8LaF6QjyxJQf08rTvrObloxspM9dwWAiteSrroT01lDTHgFHjsFfLZMxGLnvhTcRVDBDqpzbIdtKPCt4SM6Z5EzFH5HFs7Lw1byoeST/VRQNjbMMVfxaM5E1o8Sjeh41GMDZ0M3X8hZABxWHbXFvuqvNofFx0Qw8qpWMeU2+4ZTJOeiZAMDGkEofmIOMcsEw/AmrLjI+xpdDsWIdiUswgUq50D02ouAi+LGbCiMinsOJ/2SHYsCnBixmz9biTy9pQjKCECs3vTGf+m7pl8UG20dRnWHqClUxznDKZH0JDBlcPV0R2yLIqoFzZ13T0vwGIFVRxi/0hyxGahq2/I/oS8s=\"}]}";

    @BeforeAll
    public static void beforeAll() {
        PacketEvents.setAPI(Mockito.spy(SpigotPacketEventsBuilder.build(null)));

        ServerManager impl = Mockito.spy(new ServerManagerImpl());
        doReturn(impl).when(PacketEvents.getAPI()).getServerManager();
        doReturn(ServerVersion.getLatest()).when(impl).getVersion();

        DisguiseUtilities.recreateGsonSerializer();
    }

    @SneakyThrows
    private void equals(UserProfile expected, String toParse, String asSerialized) {
        ParamInfoUserProfile parser = (ParamInfoUserProfile) ParamInfoManager.getParamInfo(UserProfile.class);

        Assertions.assertEquals(asSerialized, parser.toString(expected));

        UserProfile parsed = parser.fromString(toParse);

        Assertions.assertEquals(expected.getName(), parsed.getName());
        Assertions.assertEquals(expected.getUUID(), parsed.getUUID());
        Assertions.assertEquals(expected.getTextureProperties().size(), parsed.getTextureProperties().size());

        for (int i = 0; i < expected.getTextureProperties().size(); i++) {
            TextureProperty p1 = expected.getTextureProperties().get(i);
            TextureProperty p2 = parsed.getTextureProperties().get(i);

            Assertions.assertEquals(p1.getName(), p2.getName());
            Assertions.assertEquals(p1.getValue(), p2.getValue());
            Assertions.assertEquals(p1.getSignature(), p2.getSignature());
        }
    }

    @Test
    public void doTest() {
        UserProfile modernProfile = new UserProfile(UUID.fromString("a149f81b-f784-4f89-87c5-54afdd4db533"), "libraryaddict");
        modernProfile.getTextureProperties().add(new TextureProperty("textures",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxNjg3MTE0OTQyNywKICAicHJvZmlsZUlkIiA6ICJhMTQ5ZjgxYmY3ODQ0Zjg5ODdjNTU0YWZkZDRkYjUzMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJsaWJyYXJ5YWRkaWN0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q1MTk3NWM0NWJmNTZiYzM1N2UyMDdiNWEwYjBlYjViODZiZDNlY2MyZGFjNTI3NTFiYjQ4MGM4YmJjMGM2ZmMiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2EyZThkOTdlYzc5MTAwZTkwYTc1ZDM2OWQxYjNiYTgxMjczYzRmODJiYzFiNzM3ZTkzNGVlZDRhODU0YmUxYjYiCiAgICB9CiAgfQp9",
            "V8kiVYioSkrsCHEca12KT04+VNQSflOZnF/1nbvh+TAcxTygff+2" +
                "+EpF6SoFKlJvsaKsmqCWZTv8NkdfbwiU6AL0PFz9Ef4U4X7tBAw0N3xehnxalAB6kFNQJesfCwNttAn3f7xsDIufg+BBVJ0mZtKPozHk" +
                "+iLd0s6JRF6ENfx0RpyLYmZEFt1aHbpCILFNbgI1Mbp0XlI94CoFKSqlLIvMIQSB7bccTtbGpGiegjeyiQlKVO5gSwSSCGOBaEycrbZfXN8SGWj7dpGMJkLwS9hNpxo29VC5AzVYaPi3Mn1nI8LPFCWjKDRTRs4jMmX4vZyax/6spPfqzB2hcvJk8LaF6QjyxJQf08rTvrObloxspM9dwWAiteSrroT01lDTHgFHjsFfLZMxGLnvhTcRVDBDqpzbIdtKPCt4SM6Z5EzFH5HFs7Lw1byoeST/VRQNjbMMVfxaM5E1o8Sjeh41GMDZ0M3X8hZABxWHbXFvuqvNofFx0Qw8qpWMeU2+4ZTJOeiZAMDGkEofmIOMcsEw/AmrLjI+xpdDsWIdiUswgUq50D02ouAi+LGbCiMinsOJ/2SHYsCnBixmz9biTy9pQjKCECs3vTGf+m7pl8UG20dRnWHqClUxznDKZH0JDBlcPV0R2yLIqoFzZ13T0vwGIFVRxi/0hyxGahq2/I/oS8s="));

        equals(modernProfile, oldProfileString, newProfileString);
        equals(modernProfile, oldOldProfileString, newProfileString);
        equals(modernProfile, newProfileString, newProfileString);
    }
}
