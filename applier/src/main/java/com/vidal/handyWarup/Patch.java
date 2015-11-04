package com.vidal.handyWarup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Patch implements BiFunction<File, File, File> {

   private final HashMap<Pattern, Function<Matcher, Command>> commandFactory;

   public Patch() {
      commandFactory = new HashMap<>();
      commandFactory.put(
            Pattern.compile("(?:add|replace) --from=/?(.*) --to=/?(.*)"),
            matcher -> new AddCommand(Paths.get(matcher.group(1)), Paths.get(matcher.group(2))));
      commandFactory.put(
            Pattern.compile("rm --from=/?(.*)"),
            matcher -> new RmCommand(Paths.get(matcher.group(1))));
   }

   public static void main(String[] args) {
       if (args.length != 2) {
           throw new IllegalArgumentException(
                "Expecting diff and target paths as arguments"
           );
       }
       new Patch().apply(new File(args[0]), new File(args[1]));
   }

   @Override
   public File apply(File zippedDiff, File targetDirectory) {
      return applyPatch(zippedDiff, targetDirectory);
   }

   private File applyPatch(File zippedDiff, File targetDirectory) {

      assertTarget(targetDirectory);

      Path unzipped = new UnzipToTempDirectory().apply(zippedDiff);

      File batchFile = Arrays.asList(unzipped.toFile().listFiles()).stream()
            .filter(file -> file.getName().equals("batch.warup"))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("could not find patch file"));

      try (BufferedReader reader = new BufferedReader(new FileReader(batchFile))) {
         reader.lines()
               .map(this::parseCommandLine)
               .forEach(command -> command.accept(unzipped, targetDirectory.toPath()));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

      return targetDirectory;
   }

   private void assertTarget(File targetDirectory) {
      if (!targetDirectory.exists()) {
         throw new IllegalArgumentException("could not find target to apply to");
      }
      if (!targetDirectory.canWrite()) {
         throw new IllegalArgumentException("target must be writable");
      }
   }

   private Command parseCommandLine(String line) {
      return commandFactory.entrySet()
            .stream()
            .map(entry -> {
               Matcher matcher = entry.getKey().matcher(line);
               return new SimpleEntry<Boolean, Supplier<Command>>(
                     matcher.matches(), // matches is called here, so subsequent group() calls will work
                     () -> entry.getValue().apply(matcher));
            })
            .filter(Map.Entry::getKey)
            .map(entry -> entry.getValue().get())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Line could not be parsed: " + line));
   }
}