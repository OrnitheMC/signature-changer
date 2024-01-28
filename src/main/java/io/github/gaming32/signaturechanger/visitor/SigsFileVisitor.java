package io.github.gaming32.signaturechanger.visitor;

import io.github.gaming32.signaturechanger.SignatureMode;
import org.jetbrains.annotations.Nullable;

public abstract class SigsFileVisitor {
    @Nullable
    private final SigsFileVisitor delegate;

    protected SigsFileVisitor(@Nullable SigsFileVisitor delegate) {
        this.delegate = delegate;
    }

    protected SigsFileVisitor() {
        this(null);
    }

    @Nullable
    public final SigsFileVisitor getDelegate() {
        return delegate;
    }

    @Nullable
    public SigsClassVisitor visitClass(String className, SignatureMode signatureMode, String signature) {
        if (delegate != null) {
            return delegate.visitClass(className, signatureMode, signature);
        }
        return null;
    }

    public void visitEnd() {
        if (delegate != null) {
            delegate.visitEnd();
        }
    }
}
