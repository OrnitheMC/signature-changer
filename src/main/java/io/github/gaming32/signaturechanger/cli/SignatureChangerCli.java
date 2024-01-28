package io.github.gaming32.signaturechanger.cli;

import io.github.gaming32.signaturechanger.generator.SigsClassGenerator;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.ext.java7.PathArgumentType;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SignatureChangerCli {
    public static void main(String[] args) throws IOException {
        final ArgumentParser parser = ArgumentParsers.newFor("signature-changer")
            .fromFilePrefix("@")
            .build()
            .defaultHelp(true)
            .description("A CLI/library");
        final Subparsers subparsers = parser.addSubparsers()
            .dest("action")
            .help("The action to perform");

        final Subparser generate = subparsers.addParser(GenerateAction.NAME)
            .description("Generate a sigs file to stdout")
            .defaultHelp(true);
        generate.addArgument("-e", "--empty-mode")
            .type(new LowercaseEnumArgumentType<>(SigsClassGenerator.EmptySignatureMode.class))
            .setDefault(SigsClassGenerator.EmptySignatureMode.IGNORE)
            .help("What to do when a signature is absent");
        generate.addArgument("classes")
            .type(new PathArgumentType().verifyCanRead())
            .nargs("+")
            .help("The directories or jars to load classes from");

        final Subparser apply = subparsers.addParser(ApplyAction.NAME)
            .description("Applies a sigs file to the specified classes")
            .defaultHelp(true);
        apply.addArgument("sigs")
            .type(new PathArgumentType().verifyCanRead().acceptSystemIn())
            .help("The source .sigs file, or \"-\" to use stdin");
        apply.addArgument("classes")
            .type(new PathArgumentType().verifyCanRead().verifyCanWrite())
            .nargs("+")
            .help("The directories or jars to apply signatures to **in-place**");

        final var parsedArgs = new Object() {
            @Arg
            String action;

            // Generate and apply actions
            @Arg
            List<Path> classes;

            // Generate action
            @Arg(dest = "empty_mode")
            SigsClassGenerator.EmptySignatureMode emptyMode;

            // Apply action
            @Arg
            Path sigs;
        };

        try {
            parser.parseArgs(args, parsedArgs);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        switch (parsedArgs.action) {
            case GenerateAction.NAME -> GenerateAction.run(parsedArgs.classes, parsedArgs.emptyMode);
            case ApplyAction.NAME -> ApplyAction.run(parsedArgs.sigs, parsedArgs.classes);
            default -> throw new AssertionError("Unimplemented action " + parsedArgs.action);
        }
    }

    public static void iterateClasses(
        List<Path> classes, IOConsumer<Path> beforeOrigin, IOConsumer<Path> afterOrigin, IOBiConsumer<Path, ClassReader> onClass
    ) throws IOException {
        for (final Path origin : classes) {
            beforeOrigin.accept(origin);
            FileSystem fs = null;
            final Path realRoot;
            if (Files.isRegularFile(origin)) {
                fs = FileSystems.newFileSystem(origin);
                realRoot = fs.getRootDirectories().iterator().next();
            } else {
                realRoot = origin;
            }
            try (Stream<Path> stream = Files.find(
                realRoot, Integer.MAX_VALUE, (p, a) -> a.isRegularFile() && p.toString().endsWith(".class")
            )) {
                stream.forEach(path -> {
                    try {
                        final ClassReader reader;
                        try (InputStream is = Files.newInputStream(path)) {
                            reader = new ClassReader(is);
                        }
                        onClass.accept(path, reader);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            } finally {
                if (fs != null) {
                    fs.close();
                }
            }
            afterOrigin.accept(origin);
        }
    }
}
