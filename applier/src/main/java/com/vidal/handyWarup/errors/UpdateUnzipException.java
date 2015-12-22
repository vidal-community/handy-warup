package com.vidal.handyWarup.errors;

import java.io.IOException;

public class UpdateUnzipException extends HandyWarupException {

   public UpdateUnzipException(Exception cause) {
      super(cause);
   }

   public UpdateUnzipException(String message, IOException cause) {
      super(message, cause);
   }

   public UpdateUnzipException(String message) {
      super(message);
   }
}
