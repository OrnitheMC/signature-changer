package io.github.gaming32.signaturechanger.visitor;

import io.github.gaming32.signaturechanger.SignatureMode;
import org.jetbrains.annotations.Nullable;

public abstract class SigsClassVisitor {
    @Nullable
    private final SigsClassVisitor delegate;

    protected SigsClassVisitor(@Nullable SigsClassVisitor delegate) {
        this.delegate = delegate;
    }

    protected SigsClassVisitor() {
        this(null);
    }

    @Nullable
    public final SigsClassVisitor getDelegate() {
        return delegate;
    }

    public void visitMember(String name, String desc, SignatureMode signatureMode, String signature) {
        if (delegate != null) {
            delegate.visitMember(name, desc, signatureMode, signature);
        }
    }

    public void visitEnd() {
        if (delegate != null) {
            delegate.visitEnd();
        }
    }
}
