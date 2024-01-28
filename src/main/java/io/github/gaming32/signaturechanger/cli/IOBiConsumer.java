package io.github.gaming32.signaturechanger.cli;

import java.io.IOException;

@FunctionalInterface
public interface IOBiConsumer<T, U> {
    void accept(T t, U u) throws IOException;
}
