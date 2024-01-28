package io.github.gaming32.signaturechanger.tree;

import io.github.gaming32.signaturechanger.SignatureMode;
import org.jetbrains.annotations.Nullable;

public record SignatureInfo(SignatureMode mode, @Nullable String signature) {
    public SignatureInfo {
        if (mode == SignatureMode.MODIFY) {
            if (signature == null) {
                throw new IllegalArgumentException("If mode is MODIFY, the signature cannot be null");
            }
        } else {
            signature = null;
        }
    }

    public SignatureInfo mergeWith(SignatureInfo other) {
        return other.mode == SignatureMode.KEEP ? this : other;
    }

    @Nullable
    public String apply(@Nullable String previous) {
        return switch (mode) {
            case KEEP -> previous;
            case REMOVE -> null;
            case MODIFY -> signature;
        };
    }
}
