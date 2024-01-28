package io.github.gaming32.signaturechanger.tree;

import io.github.gaming32.signaturechanger.SignatureMode;
import org.jetbrains.annotations.Nullable;

public record SignatureInfo(SignatureMode mode, @Nullable String signature) {
    public SignatureInfo {
        if (mode == SignatureMode.KEEP) {
            if (signature == null) {
                throw new IllegalArgumentException("If mode is KEEP, the signature cannot be null");
            }
        } else {
            signature = null;
        }
    }

    public SignatureInfo mergeWith(SignatureInfo other) {
        return other.mode == SignatureMode.KEEP ? this : other;
    }
}
