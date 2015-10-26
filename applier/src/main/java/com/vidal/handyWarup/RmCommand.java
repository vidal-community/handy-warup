package com.vidal.handyWarup;

import java.nio.file.Path;

public class RmCommand implements Command {
   private final Path relative;

   public RmCommand(Path relative) {
      this.relative = relative;
   }

   @Override
   public void accept(Path sourceRoot, Path targetRoot) {
      new FsDeepRemove().accept(targetRoot.resolve(relative));
   }
}
