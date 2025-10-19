package me.libraryaddict.disguise.utilities.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SerializerUserProfileFactory implements TypeAdapterFactory {
    private final List<Class<?>> classesToSerializeNulls;

    public SerializerUserProfileFactory(Class<?>... classesToSerializeNulls) {
        this.classesToSerializeNulls = Arrays.asList(classesToSerializeNulls);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!classesToSerializeNulls.contains(type.getRawType())) {
            return null;
        }

        final TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                boolean serializeNulls = out.getSerializeNulls();
                out.setSerializeNulls(true);
                delegateAdapter.write(out, value);
                out.setSerializeNulls(serializeNulls);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                return delegateAdapter.read(in);
            }
        };
    }
}
