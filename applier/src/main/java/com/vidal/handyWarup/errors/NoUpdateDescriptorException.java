package com.vidal.handyWarup.errors;

import java.io.IOException;

public class NoUpdateDescriptorException extends HandyWarupException {

   public NoUpdateDescriptorException(String message) {
      super(message);
   }

   public NoUpdateDescriptorException(IOException cause) {
      super(cause);
   }
}
