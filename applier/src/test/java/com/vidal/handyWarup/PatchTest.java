package com.vidal.handyWarup;

import static com.vidal.handyWarup.Zip.zipAndGet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class PatchTest {

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();

   @Rule
   public ExpectedException thrown = ExpectedException.none();
   private Patch patch = new Patch();


   @Test
   public void should_apply_empty_patch_on_empty_target() throws Exception {
      File diff = new File(getClass().getResource("/emptyDiff.zip").toURI());
      File target = folder.newFolder();

      File patched = patch.apply(diff, target);

      assertThat(patched).exists()
            .isDirectory();
      assertThat(patched.listFiles()).isEmpty();

   }

   @Test
   public void should_not_apply_invalid_diff() throws Exception {
      File diff = new File(getClass().getResource("/invalidDiff.zip").toURI());
      File target = folder.newFolder();

      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("could not find patch file");
      patch.apply(diff, target);
   }

   @Test
   public void should_not_apply_not_found_diff() throws Exception {
      File diff = new File("qsdfklsjdfmlj");
      File target = folder.newFolder();

      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("could not find diff file");
      patch.apply(diff, target);
   }

   @Test
   public void should_not_apply_to_not_found_target() throws Exception {
      File diff = new File(getClass().getResource("/emptyDiff.zip").toURI());
      File target = new File("foobar");


      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("could not find target to apply to");
      patch.apply(diff, target);
   }


   @Test
   public void should_not_apply_to_target_without_write_permission() throws Exception {
      File diff = new File(getClass().getResource("/emptyDiff.zip").toURI());
      File target = folder.newFolder();
      assumeTrue(target.setWritable(false));


      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("target must be writable");
      patch.apply(diff, target);
   }


   @Test
   public void should_apply_file_addition_to_empty_target() throws Exception {
      File diff = zipAndGet("/fileAdditionDiff", folder);
      File target = folder.newFolder();

      File patched = patch.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.listFiles())
            .extracting(File::getName)
            .contains("hello.txt");
      assertThat(new File(patched, "hello.txt")).hasContent("hello world!");
   }


   @Test
   public void should_apply_file_addition_to_target_with_same_filename() throws Exception {
      File diff = zipAndGet("/fileAdditionDiff", folder);
      File target = folder.newFolder();
      newFile(target, "hello.txt");

      File patched = patch.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.listFiles())
            .extracting(File::getName)
            .contains("hello.txt");
      assertThat(new File(patched, "hello.txt")).hasContent("hello world!");
   }

   @Test
   public void should_apply_directory_addition_to_target() throws Exception {
      File diff = zipAndGet("/directoryAdditionDiff", folder);
      File target = folder.newFolder();

      File patched = patch.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.toPath().resolve("new_directory/Hello.txt")).hasContent("hello world!");
   }

   @Test
   public void should_apply_file_replacement_to_target_with_same_filename() throws Exception {
      File diff = zipAndGet("/fileReplacementDiff", folder);
      File target = folder.newFolder();
      newFile(target, "hello.txt");

      File patched = patch.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.listFiles())
            .extracting(File::getName)
            .contains("hello.txt");
      assertThat(new File(patched, "hello.txt")).hasContent("hello world!");
   }

   @Test
   public void should_apply_directory_replacement_to_target() throws Exception {
      File diff = zipAndGet("/directoryReplacementDiff", folder);
      File target = folder.newFolder();

      File patched = patch.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.toPath().resolve("new_directory/Hello.txt")).hasContent("hello world!");
   }

   @Test
   public void should_apply_file_deletion_to_target() throws Exception {
      File diff = zipAndGet("/fileDeletionDiff", folder);
      File target = folder.newFolder();
      newFile(target, "hello.txt");

      File patched = patch.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.listFiles()).isEmpty();
   }

   @Test
   public void should_apply_directory_deletion_to_target() throws Exception {
      File diff = zipAndGet("/directoryDeletionDiff", folder);
      File target = folder.newFolder();

      newDirectory(target, "old_dir/very/nested");
      newFile(target, "old_dir/very/nested/hello.txt");

      newDirectory(target, "old_dir/other/very/nested");
      newFile(target, "old_dir/other/very/nested/hello.txt");

      File patched = patch.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.listFiles()).isEmpty();
   }

   private static void newDirectory(File target, String dirName) {
      if (!new File(target, dirName).mkdirs()) {
         Assertions.fail("could not create directory");
      }
   }

   private static void newFile(File target, String fileName) throws IOException {
      if (!new File(target, fileName).createNewFile()) {
         Assertions.fail("could not create file");
      }
   }


}
