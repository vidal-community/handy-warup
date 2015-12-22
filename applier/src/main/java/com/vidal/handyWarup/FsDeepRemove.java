package com.vidal.handyWarup;

import com.vidal.handyWarup.errors.PathDeletionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FsDeepRemove implements Consumer<Path> {
   @Override
   public void accept(Path path) {
      File filePath = path.toFile();
      if (filePath.isDirectory()) {
         for (File p : filePath.listFiles()) {
            accept(p.toPath());
         }
      }
      delete(path);
   }

   private void delete(Path path) {
      try {
         Files.delete(path);
      } catch (IOException e) {
         throw new PathDeletionException("Could not delete " + path, e);
      }
   }
}
