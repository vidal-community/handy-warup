package com.vidal.handyWarup;

import com.vidal.handyWarup.errors.TemporaryCopyException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;

public class FsDeepCopy implements BiConsumer<Path, Path> {

   @Override
   public void accept(Path source, Path target) {
      try {
         Files.walkFileTree(source, new DeepCopyVisitor(source, target));
      } catch (IOException e) {
         throw new TemporaryCopyException("Unable to deep copy " + source + " to " + target, e);
      }
   }

   private static class DeepCopyVisitor implements FileVisitor<Path> {

      private final Path source;
      private final Path target;

      public DeepCopyVisitor(Path source, Path target) {
         this.source = source;
         this.target = target;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
         copy(dir, resolve(dir));
         return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
         copy(file, resolve(file));
         return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
         throw exc;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
         return FileVisitResult.CONTINUE;
      }

      private Path resolve(Path path) {
         return target.resolve(source.relativize(path));
      }

      private void copy(Path dir, Path to) throws IOException {
         Files.copy(dir, to, StandardCopyOption.REPLACE_EXISTING);
      }
   }
}
