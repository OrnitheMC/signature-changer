package io.github.gaming32.signaturechanger.tree;

import org.objectweb.asm.Type;

public record MemberReference(String name, Type desc) {
    public MemberReference(String name, String desc) {
        this(name, Type.getType(desc));
    }
}
