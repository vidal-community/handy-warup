package com.vidal.handyWarup.errors;

import java.io.IOException;

public class PathDeletionException extends HandyWarupException {

   public PathDeletionException(String message, IOException cause) {
      super(message, cause);
   }
}
