package com.vidal.handyWarup;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.rules.TemporaryFolder;

public class Zip {
   public static File zipAndGet(String name, TemporaryFolder folder) {
      Path sourceDirectory = getSourceDirectory(name);
      try {
         File archive = folder.newFile();
         try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(archive.toPath()))) {
            Files.walk(sourceDirectory)
                  .filter(path -> !Files.isDirectory(path))
                  .forEach(path -> {
                     try {
                        File newFile = sourceDirectory.relativize(path).toFile();
                        ZipEntry entry = new ZipEntry(newFile.getPath());
                        outputStream.putNextEntry(entry);
                        outputStream.write(Files.readAllBytes(path));
                        outputStream.closeEntry();
                     } catch (IOException e) {
                        throw new RuntimeException(e.getMessage(), e);
                     }
                  });
         }
         return archive;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private static Path getSourceDirectory(String name) {
      try {
         return new File(Zip.class.getResource(name).toURI()).toPath();
      } catch (URISyntaxException e) {
         throw new RuntimeException("resource " + name + " not found");
      }
   }
}
