package com.vidal.handyWarup;

import com.vidal.handyWarup.errors.UpdateUnzipException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.zip.ZipFile;

public class UnzipToTempDirectory implements Function<File, Path> {

   @Override
   public Path apply(File file) {
      return toTemp(file);
   }

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
                     throw new UpdateUnzipException(e);
                  }
               });
      } catch (IOException e) {
         throw new UpdateUnzipException("could not find diff file", e);
      }

      return extractDir;
   }

   private void makeFileTree(File file) {
      if (file.exists()) {
         return;
      }
      if (!file.mkdirs()) {
         throw new UpdateUnzipException("Could not create parent directories");
      }
   }

   private Path tempDirectory() {
      try {
         return Files.createTempDirectory("handy-warup");
      } catch (IOException e) {
         throw new UpdateUnzipException(e.getMessage(), e);
      }
   }
}
