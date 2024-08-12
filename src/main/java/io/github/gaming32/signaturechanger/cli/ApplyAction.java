package io.github.gaming32.signaturechanger.cli;

import io.github.gaming32.signaturechanger.SignatureMode;
import io.github.gaming32.signaturechanger.apply.SignatureApplier;
import io.github.gaming32.signaturechanger.tree.SigsClass;
import io.github.gaming32.signaturechanger.tree.SigsFile;
import io.github.gaming32.signaturechanger.visitor.SigsReader;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ApplyAction {
    public static final String NAME = "apply";

    public static void run(Path sigs, List<Path> classes) throws IOException {
        final SigsFile sigsFile = new SigsFile();
        if (sigs.toString().equals("-")) {
            try (SigsReader reader = new SigsReader(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)))) {
                reader.accept(sigsFile);
            }
        } else {
            try (SigsReader reader = new SigsReader(Files.newBufferedReader(sigs, StandardCharsets.UTF_8))) {
                reader.accept(sigsFile);
            }
        }
        ApplyAction.run(sigsFile, classes);
    }

    public static void run(SigsFile sigsFile, List<Path> classes) throws IOException {
        final SignatureApplier applier = new SignatureApplier(sigsFile);
        SignatureChangerCli.iterateClasses(
            classes,
            origin -> {}/*System.out.println("*** Patching classes from " + origin + " ***")*/,
            origin -> {},
            (path, reader) -> {
                final SigsClass clazz = sigsFile.classes.get(reader.getClassName());
                if (clazz == null || (clazz.signatureInfo.mode() == SignatureMode.KEEP && clazz.members.isEmpty())) {
                    return;
                }
//                System.out.println("Patching " + reader.getClassName());
                final ClassWriter writer = new ClassWriter(reader, 0);
                applier.setDelegate(writer);
                try {
                    reader.accept(applier, 0);
                } finally {
                    applier.setDelegate(null);
                }
                Files.write(path, writer.toByteArray());
            }
        );
    }
}
