package com.vidal.handyWarup;

import com.vidal.handyWarup.errors.CommandParsingException;
import com.vidal.handyWarup.errors.NoUpdateDescriptorException;
import com.vidal.handyWarup.errors.TargetDirectoryPermissionException;
import com.vidal.handyWarup.errors.UpdateUnzipException;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static com.vidal.handyWarup.Zip.zipAndGet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class HandyWarupTest {

   @Rule public TemporaryFolder folder = new TemporaryFolder();
   @Rule public ExpectedException thrown = ExpectedException.none();
   private HandyWarup handyWarup = new HandyWarup();

   @Test
   public void should_apply_empty_patch_on_empty_target() throws Exception {
      File diff = new File(getClass().getResource("/emptyDiff.zip").toURI());
      File target = folder.newFolder();

      File patched = handyWarup.apply(diff, target);

      assertThat(patched).exists().isDirectory();
      assertThat(patched.listFiles()).isEmpty();

   }

   @Test
   public void should_not_apply_invalid_diff() throws Exception {
      File diff = new File(getClass().getResource("/invalidDiff.zip").toURI());
      File target = folder.newFolder();

      thrown.expect(NoUpdateDescriptorException.class);
      thrown.expectMessage("could not find patch file");
      handyWarup.apply(diff, target);
   }

   @Test
   public void should_not_apply_not_found_diff() throws Exception {
      File diff = new File("qsdfklsjdfmlj");
      File target = folder.newFolder();

      thrown.expect(UpdateUnzipException.class);
      thrown.expectMessage("could not find diff file");
      handyWarup.apply(diff, target);
   }

   @Test
   public void should_not_apply_to_not_found_target() throws Exception {
      File diff = new File(getClass().getResource("/emptyDiff.zip").toURI());
      File target = new File("foobar");


      thrown.expect(TargetDirectoryPermissionException.class);
      thrown.expectMessage("could not find target to apply to");
      handyWarup.apply(diff, target);
   }


   @Test
   public void should_not_apply_to_target_without_write_permission() throws Exception {
      File diff = new File(getClass().getResource("/emptyDiff.zip").toURI());
      File target = folder.newFolder();
      assumeTrue(target.setWritable(false));


      thrown.expect(TargetDirectoryPermissionException.class);
      thrown.expectMessage("target must be writable");
      handyWarup.apply(diff, target);
   }


   @Test
   public void should_apply_file_addition_to_empty_target() throws Exception {
      File diff = zipAndGet("/fileAdditionDiff", folder);
      File target = folder.newFolder();

      File patched = handyWarup.apply(diff, target);

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

      File patched = handyWarup.apply(diff, target);

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

      File patched = handyWarup.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.toPath().resolve("new_directory/Hello.txt")).hasContent("hello world!");
   }

   @Test
   public void should_apply_file_replacement_to_target_with_same_filename() throws Exception {
      File diff = zipAndGet("/fileReplacementDiff", folder);
      File target = folder.newFolder();
      newFile(target, "hello.txt");

      File patched = handyWarup.apply(diff, target);

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

      File patched = handyWarup.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.toPath().resolve("new_directory/Hello.txt")).hasContent("hello world!");
   }

   @Test
   public void should_apply_file_deletion_to_target() throws Exception {
      File diff = zipAndGet("/fileDeletionDiff", folder);
      File target = folder.newFolder();
      newFile(target, "hello.txt");

      File patched = handyWarup.apply(diff, target);

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

      File patched = handyWarup.apply(diff, target);

      assertThat(patched).isDirectory();
      assertThat(patched.listFiles()).isEmpty();
   }

   @Test
   public void should_preserve_target_if_batch_execution_fails() throws IOException {
      File diff = zipAndGet("/brokenDiff", folder);
      File target = folder.newFolder();
      File existingFile = new File(target, "foo.txt");
      write(existingFile.toPath(), "barbaz".getBytes(UTF_8));

      thrown.expect(CommandParsingException.class);
      thrown.expectMessage("Line could not be parsed: i'm not a parseable command!");

      handyWarup.apply(diff, target);

      assertThat(existingFile).hasContent("barbaz");
   }

   @Test
   public void should_accept_valid_handy_warup_archive() {
      File diff = zipAndGet("/acceptValidArchive", folder);

      assertThat(handyWarup.accepts(diff)).isTrue();
   }

   @Test
   public void should_not_accept_invalid_handy_warup_archive() {
      File diff = zipAndGet("/invalidArchive", folder);

      assertThat(handyWarup.accepts(diff)).isFalse();
   }

   @Test
   public void should_not_accept_handy_warup_archive_with_mislocated_batch_file() {
      File diff = zipAndGet("/invalidBatchLocationArchive", folder);

      assertThat(handyWarup.accepts(diff)).isFalse();
   }

   @Test
   public void should_not_accept_non_archive_file() throws IOException {
      File notAZip = folder.newFile();

      assertThat(handyWarup.accepts(notAZip)).isFalse();
   }

   @Test
   public void should_not_accept_non_existing_file() throws IOException {
      assertThat(handyWarup.accepts(new File("salut c'est eddy mitchell"))).isFalse();
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
