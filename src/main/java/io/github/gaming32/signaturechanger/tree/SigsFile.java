package io.github.gaming32.signaturechanger.tree;

import io.github.gaming32.signaturechanger.SignatureMode;
import io.github.gaming32.signaturechanger.visitor.SigsClassVisitor;
import io.github.gaming32.signaturechanger.visitor.SigsFileVisitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SigsFile extends SigsFileVisitor {
    public final Map<String, SigsClass> classes = new LinkedHashMap<>();

    @Override
    public SigsClass visitClass(String className, SignatureMode signatureMode, String signature) {
        final SigsClass clazz = classes.computeIfAbsent(className, k -> new SigsClass());
        clazz.signatureInfo = clazz.signatureInfo.mergeWith(new SignatureInfo(signatureMode, signature));
        return clazz;
    }

    public void accept(SigsFileVisitor visitor) {
        classes.forEach((name, clazz) -> {
            final SigsClassVisitor classVisitor = visitor.visitClass(
                name, clazz.signatureInfo.mode(), Objects.requireNonNullElse(clazz.signatureInfo.signature(), "")
            );
            if (classVisitor != null) {
                clazz.accept(classVisitor);
            }
        });
    }
}
