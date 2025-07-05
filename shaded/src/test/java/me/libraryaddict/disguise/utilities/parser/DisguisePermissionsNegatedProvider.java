package me.libraryaddict.disguise.utilities.parser;

import me.libraryaddict.disguise.DisguiseConfig;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DisguisePermissionsNegatedProvider implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Stream.of(
            invocationContext(true),
            invocationContext(false)
        );
    }

    private TestTemplateInvocationContext invocationContext(boolean value) {
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return "Run with DisguiseConfig.setDisabledInvisibility(" + value + ")";
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return List.of((BeforeEachCallback) context -> DisguiseConfig.setDisabledInvisibility(value));
            }
        };
    }
}