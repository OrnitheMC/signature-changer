package io.github.gaming32.signaturechanger.tree;

import io.github.gaming32.signaturechanger.SignatureMode;
import io.github.gaming32.signaturechanger.visitor.SigsClassVisitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SigsClass extends SigsClassVisitor {
    public SignatureInfo signatureInfo = new SignatureInfo(SignatureMode.KEEP, null);
    public final Map<MemberReference, SignatureInfo> members = new LinkedHashMap<>();

    @Override
    public void visitMember(String name, String desc, SignatureMode signatureMode, String signature) {
        members.merge(
            new MemberReference(name, desc),
            new SignatureInfo(signatureMode, signature),
            SignatureInfo::mergeWith
        );
    }

    public void accept(SigsClassVisitor visitor) {
        members.forEach((ref, info) -> visitor.visitMember(
            ref.name(), ref.desc().getDescriptor(),
            info.mode(), Objects.requireNonNullElse(info.signature(), "")
        ));
    }
}
