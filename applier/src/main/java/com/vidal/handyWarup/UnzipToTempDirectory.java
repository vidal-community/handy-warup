package com.vidal.handyWarup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.zip.ZipFile;

public class UnzipToTempDirectory implements Function<File, Path> {

   private Path toTemp(File zip) {
      Path extractDir = tempDirectory();

      try (ZipFile zipFile = new ZipFile(zip)) {
         zipFile.stream()
               .filter(zE -> !zE.isDirectory())
               .forEachOrdered(zipEntry -> {
                  try (InputStream is = zipFile.getInputStream(zipEntry)) {
                     Path target = extractDir.resolve(zipEntry.getName());
                     File file = target.toFile();
                     makeFileTree(file.getParentFile());
                     Files.copy(is, target);
                  } catch (IOException e) {
                     throw new RuntimeException(e);
                  }
               });
      } catch (IOException e) {
         throw new IllegalArgumentException("could not find diff file", e);
      }

      return extractDir;
   }

   private void makeFileTree(File file) {
      if (file.exists()) {
         return;
      }

      if (!file.mkdirs()) {
         throw new RuntimeException("Could not create parent directories");
      }
   }

   private Path tempDirectory() {
      Path extractDir;
      try {
         extractDir = Files.createTempDirectory("handy-warup");
      } catch (IOException e) {
         throw new RuntimeException(e.getMessage(), e);
      }
      return extractDir;
   }

   @Override
   public Path apply(File file) {
      return toTemp(file);
   }
}
