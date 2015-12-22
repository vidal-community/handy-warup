package com.vidal.handyWarup.errors;

public class HandyWarupException extends RuntimeException {

   public HandyWarupException(String message, Exception cause) {
      super(message, cause);
   }

   public HandyWarupException(String message) {
      super(message);
   }

   public HandyWarupException(Exception cause) {
      super(cause);
   }
}
