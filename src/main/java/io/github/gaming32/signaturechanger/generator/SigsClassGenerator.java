package io.github.gaming32.signaturechanger.generator;

import io.github.gaming32.signaturechanger.SignatureMode;
import io.github.gaming32.signaturechanger.visitor.SigsClassVisitor;
import io.github.gaming32.signaturechanger.visitor.SigsFileVisitor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SigsClassGenerator extends ClassVisitor {
    @Nullable
    private final SigsFileVisitor visitor;
    @Nullable
    private SigsClassVisitor classVisitor;
    private final EmptySignatureMode emptyMode;

    public SigsClassGenerator(SigsFileVisitor visitor, EmptySignatureMode emptyMode) {
        super(Opcodes.ASM9);
        this.visitor = visitor;
        this.classVisitor = null;
        this.emptyMode = emptyMode;
    }

    public SigsClassGenerator(SigsClassVisitor visitor, EmptySignatureMode emptyMode) {
        super(Opcodes.ASM9);
        this.visitor = null;
        this.classVisitor = visitor;
        this.emptyMode = emptyMode;
    }

    @Override
    public void visit(int version, int access, String name, @Nullable String signature, String superName, String[] interfaces) {
        if (classVisitor == null) {
            assert visitor != null;
            if (signature == null) {
                classVisitor = visitor.visitClass(name, emptyMode.modeOrElse(SignatureMode.KEEP), "");
            } else {
                classVisitor = visitor.visitClass(name, SignatureMode.MODIFY, signature);
            }
        }
    }

    @Nullable
    @Override
    @Contract("_, _, _, _, _ -> null")
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return visitMember(name, descriptor, signature);
    }

    @Nullable
    @Override
    @Contract("_, _, _ , _, _-> null")
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return visitMember(name, descriptor, signature);
    }

    @Nullable
    @Contract("_, _, _ -> null")
    @SuppressWarnings("SameReturnValue") // It's literally the contract...
    private <T> T visitMember(String name, String descriptor, @Nullable String signature) {
        if (classVisitor == null) {
            return null;
        }
        if (signature == null) {
            if (emptyMode.classMode != null) {
                classVisitor.visitMember(name, descriptor, emptyMode.classMode, "");
            }
        } else {
            classVisitor.visitMember(name, descriptor, SignatureMode.MODIFY, signature);
        }
        return null;
    }

    @Override
    public void visitEnd() {
        if (classVisitor != null) {
            classVisitor.visitEnd();
            classVisitor = null;
        }
    }

    public enum EmptySignatureMode {
        IGNORE(null),
        STORE_REMOVE(SignatureMode.REMOVE),
        STORE_KEEP(SignatureMode.KEEP);

        @Nullable
        private final SignatureMode classMode;

        EmptySignatureMode(@Nullable SignatureMode mode) {
            this.classMode = mode;
        }

        private SignatureMode modeOrElse(SignatureMode mode) {
            return classMode == null ? mode : classMode;
        }
    }
}
