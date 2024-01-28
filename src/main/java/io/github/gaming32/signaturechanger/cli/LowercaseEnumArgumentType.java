package io.github.gaming32.signaturechanger.cli;

import net.sourceforge.argparse4j.impl.type.CaseInsensitiveEnumArgumentType;

import java.util.Locale;

public class LowercaseEnumArgumentType<T extends Enum<T>> extends CaseInsensitiveEnumArgumentType<T> {
    public LowercaseEnumArgumentType(Class<T> type) {
        super(type, Locale.ROOT);
    }

    @Override
    protected String toStringRepresentation(T t) {
        return t.name().toLowerCase(Locale.ROOT);
    }

    @Override
    protected Object[] getStringRepresentations() {
        final T[] constants = type_.getEnumConstants();
        final String[] result = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            result[i] = constants[i].name().toLowerCase(Locale.ROOT);
        }
        return result;
    }
}
