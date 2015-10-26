package com.vidal.handyWarup;

import java.nio.file.Path;

public class AddCommand implements Command {

   private final Path relativeSource;
   private final Path relativeTarget;


   public AddCommand(Path relativeSource, Path relativeTarget) {
      this.relativeSource = relativeSource;
      this.relativeTarget = relativeTarget;
   }

   @Override
   public void accept(Path sourceRoot, Path targetRoot) {
      Path source = sourceRoot.resolve(relativeSource);
      Path target = targetRoot.resolve(relativeTarget);
      new FsDeepCopy().accept(source, target);
   }
}
