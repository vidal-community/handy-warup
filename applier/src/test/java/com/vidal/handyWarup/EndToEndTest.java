package com.vidal.handyWarup;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EndToEndTest {

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();
   private Patch patch = new Patch();

   @Test
   public void applying_the_diff_should_result_to_original_directory() throws Exception {
      File unzippedOldWar = copyInTmp("/oldWar");
      File diff = Zip.zipAndGet("/diff", folder);

      patch.apply(diff, unzippedOldWar);

      assertThatFileTreesAreEqual(unzippedOldWar, loadFileFromClasspath("/newWar"));
   }

   @Test
   public void generating_a_diff_and_applying_the_diff_should_result_to_original_directory() throws Exception {
      assumeTrue(matchesOs("linux"));

      File newWarFile = Zip.zipAndGet("/newWar", folder);
      File oldWarFile = Zip.zipAndGet("/oldWar", folder);

      int returnStatus = generateDiff(newWarFile, oldWarFile);

      assertThat(returnStatus).isZero();

      File diff = new File("handy-warup-diff.zip");
      File unzippedOldWar = unzip(oldWarFile);

      patch.apply(diff, unzippedOldWar);

      assertThatFileTreesAreEqual(unzippedOldWar, unzip(newWarFile));
   }

   private boolean matchesOs(String os) {
      return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith(os);
   }

   private File copyInTmp(String name) throws Exception {
      File copyDirectory = folder.newFolder();
      new FsDeepCopy().accept(loadFileFromClasspath(name).toPath(), copyDirectory.toPath());
      return copyDirectory;
   }

   private int generateDiff(File newWarFile, File oldWarFile) throws URISyntaxException, IOException, InterruptedException {
      File mkdiff = loadGenerator();
      return new ProcessBuilder(mkdiff.getAbsolutePath(),
            newWarFile.getAbsolutePath(),
            oldWarFile.getAbsolutePath())
            .start()
            .waitFor();
   }

    private File unzip(File newWarFile) {
      return new UnzipToTempDirectory().apply(newWarFile).toFile();
   }

   private void assertThatFileTreesAreEqual(File actual, File expected) {
      if (actual.isFile()) {
         assertThat(actual).hasSameContentAs(expected);
         return;
      }

      File[] actualFiles = actual.listFiles();
      Arrays.sort(actualFiles);
      File[] expectedFiles = expected.listFiles();
      Arrays.sort(expectedFiles);

      assertThat(actualFiles)
            .usingElementComparator((a, e) -> a.getName().compareTo(e.getName()))
            .isEqualTo(expectedFiles);

      for (int i = 0; i < actualFiles.length; i++) {
         File actualFile = actualFiles[i];
         File expectedFile = expectedFiles[i];
         assertThatFileTreesAreEqual(actualFile, expectedFile);
      }
   }

   private File loadGenerator() throws IOException {
      try (InputStream stream = EndToEndTest.class.getResourceAsStream("/mkdiff.sh")) {
          File file = folder.newFile();
          Files.copy(stream, file.toPath(), REPLACE_EXISTING);
          if (!file.setExecutable(true)) {
              Assertions.fail("Could not make generator executable!");
          }
          return file;
      }
   }

   private static File loadFileFromClasspath(String name) throws URISyntaxException {
      return new File(EndToEndTest.class.getResource(name).toURI());
   }
}
