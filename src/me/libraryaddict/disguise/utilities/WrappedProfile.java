package me.libraryaddict.disguise.utilities;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Created by libraryaddict on 15/05/2017.
 */
public class WrappedProfile implements Serializable {
    private WrappedGameProfile profile;

    public WrappedProfile(WrappedGameProfile profile) {
        this.profile = profile;
    }

    public WrappedGameProfile getProfile() {
        return profile;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(profile.getUUID());
        out.writeObject(profile.getName());
        out.writeByte(profile.getProperties().size());

        for (Map.Entry<String, WrappedSignedProperty> entry : profile.getProperties().entries()) {
            WrappedSignedProperty property = entry.getValue();

            out.writeUTF(entry.getKey());
            out.writeUTF(property.getName());
            out.writeUTF(property.getSignature());
            out.writeUTF(property.getValue());
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        profile = new WrappedGameProfile((UUID) in.readObject(), in.readUTF());

        for (int i = in.readByte(); i > 0; i--) {
            profile.getProperties().put(in.readUTF(),
                    new WrappedSignedProperty(in.readUTF(), in.readUTF(), in.readUTF()));
        }
    }
}
