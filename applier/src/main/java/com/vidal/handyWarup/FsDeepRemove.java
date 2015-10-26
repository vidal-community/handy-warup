package com.vidal.handyWarup;

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
            accept(p.toPath()); // recursive
         }
      }
      delete(path);
   }

   private void delete(Path path) {
      try {
         Files.delete(path);
      } catch (IOException e) {
         throw new RuntimeException("Could not delete " + path, e);
      }
   }
}
