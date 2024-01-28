package io.github.gaming32.signaturechanger.apply;

import io.github.gaming32.signaturechanger.tree.MemberReference;
import io.github.gaming32.signaturechanger.tree.SignatureInfo;
import io.github.gaming32.signaturechanger.tree.SigsClass;
import io.github.gaming32.signaturechanger.tree.SigsFile;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @apiNote Instances of this class are reusable, but not thread-safe.
 */
public class SignatureApplier extends ClassVisitor {
    private final SigsFile sigs;
    @Nullable
    private SigsClass classSigs = null;

    public SignatureApplier(SigsFile sigs, @Nullable ClassVisitor delegate) {
        super(Opcodes.ASM9, delegate);
        this.sigs = sigs;
    }

    public SignatureApplier(SigsFile sigs) {
        this(sigs, null);
    }

    public void setDelegate(@Nullable ClassVisitor delegate) {
        this.cv = delegate;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classSigs = sigs.classes.get(name);
        if (classSigs != null) {
            signature = classSigs.signatureInfo.apply(signature);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return super.visitField(access, name, descriptor, getSignature(name, descriptor, signature), value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, name, descriptor, getSignature(name, descriptor, signature), exceptions);
    }

    @Nullable
    private String getSignature(String name, String descriptor, @Nullable String signature) {
        if (classSigs == null) {
            return signature;
        }
        final SignatureInfo info = classSigs.members.get(new MemberReference(name, descriptor));
        if (info == null) {
            return signature;
        }
        return info.apply(signature);
    }

    @Override
    public void visitEnd() {
        classSigs = null;
        super.visitEnd();
    }
}
