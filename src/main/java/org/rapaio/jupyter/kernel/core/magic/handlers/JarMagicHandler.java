package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.File;
import java.util.List;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.OneLineMagicHandler;

public class JarMagicHandler extends MagicHandler {

    private static final String PREFIX = "%jar ";

    @Override
    public String name() {
        return "Jar magic handler";
    }

    @Override
    public List<OneLineMagicHandler> oneLineMagicHandlers() {
        return List.of(
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jar .*")
                        .syntaxHelp("%jar path_to_jar_or_folder_of_jars")
                        .syntaxPrefix("%jar ")
                        .documentation(List.of(
                                "Adds to the classpath a jar or all jar archives from a directory"
                        ))
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .completeFunction(this::completeLine)
                        .build()
        );
    }

    @Override
    public List<String> helpMessage() {
        return List.of(
                "Jar magic handler allows adding to the current classpath a local jar file or all "
                        + "the jar files from a given directory."
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        return magicSnippet.oneLine() && magicSnippet.lines().size() == 1 && magicSnippet.lines().get(0).code().startsWith(PREFIX);
    }

    private Object evalLine(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicParseException, MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new MagicParseException("JarMagicHandler", magicSnippet, "Snippet cannot be handled by this magic handler.");
        }
        String fullCode = magicSnippet.lines().get(0).code();
        String path = fullCode.substring(PREFIX.length()).trim();

        File file = new File(path);
        if (!file.exists()) {
            throw new MagicEvalException(magicSnippet, "Provided path does not exist.");
        }
        if (file.isDirectory()) {
            File[] jars = file.listFiles(f -> f.getName().endsWith(".jar"));
            if (jars == null) {
                kernel.channels().writeToStdOut(ANSI.start().bold().fgGreen().text("No jar files were found.\n").render());
                return null;
            }
            kernel.channels().writeToStdOut(ANSI.start().fgGreen().text("Found " + jars.length + " jar files.\n").render());
            for (File jar : jars) {
                kernel.javaEngine().getShell().addToClasspath(jar.getAbsolutePath());
                kernel.channels().writeToStdOut(ANSI.start().fgGreen().text("Add " + jar.getAbsolutePath() + " to classpath\n").render());
            }
        } else {
            if (!file.getName().endsWith(".jar")) {
                throw new MagicEvalException(magicSnippet, "Provided input is not a jar file.");
            }
            kernel.javaEngine().getShell().addToClasspath(file.getAbsolutePath());
            kernel.channels().writeToStdOut(ANSI.start().fgGreen().text("Add " + file.getAbsolutePath() + " to classpath").render());
        }
        return null;
    }

    private CompleteMatches completeLine(RapaioKernel kernel, MagicSnippet magicSnippet) {
        return HandlerUtils.oneLinePathComplete(PREFIX, magicSnippet,
                f -> (f.isDirectory() || f.getName().endsWith(".jar")));
    }
}
