package com.vidal.handyWarup;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.isA;

public class FsDeepCopyTest {

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   FsDeepCopy deepCopy = new FsDeepCopy();

   @Test
   public void overwrites_single_file() throws IOException {
      Path file = folder.newFile("foo").toPath();
      write(file, "Hello world!");
      Path target = folder.newFile("bar").toPath();

      deepCopy.accept(file, target);

      assertThat(target)
         .exists()
         .hasContent("Hello world!");
   }

   @Test
   public void cannot_overwrite_nonempty_directory() throws IOException {
      Path directory = folder.newFolder("baz").toPath();
      write(
         create(new File(directory.toFile(), "foobar")).toPath(),
         "Hello world!"
      );
      Path target = folder.newFolder("qux").toPath();
      File targetFile = new File(target.toFile(), "foobar");
      write(
         create(targetFile).toPath(),
         "Bonjour le monde!"
      );

      thrown.expectCause(isA(DirectoryNotEmptyException.class));
      deepCopy.accept(directory, target);
   }

   private void write(Path path, String string) throws IOException {
      Files.write(path, string.getBytes(UTF_8));
   }

   private File create(File file) throws IOException {
      if (!file.createNewFile()) {
         Assertions.fail("Could not create file");
      }
      return file;
   }
}