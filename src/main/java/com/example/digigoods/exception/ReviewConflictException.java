package com.example.digigoods.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to handle cases where a user tries to review a product
 * more than once. Responds with HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ReviewConflictException extends RuntimeException {
  public ReviewConflictException(String message) {
    super(message);
  }
}
