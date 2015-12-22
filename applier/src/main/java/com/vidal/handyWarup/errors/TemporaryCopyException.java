package com.vidal.handyWarup.errors;

import java.io.IOException;

public class TemporaryCopyException extends HandyWarupException {
   public TemporaryCopyException(String message, IOException cause) {
      super(message, cause);
   }
}
