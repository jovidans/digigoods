package com.example.digigoods.dto;

import com.example.digigoods.model.Review;
import lombok.Data;
import java.time.Instant;

/**
 * Data Transfer Object for sending review data back to the client.
 */
@Data
public class ReviewResponse {
  private Long id;
  private Integer rating;
  private String comment;
  private String userName;
  private Long productId;
  private Instant createdAt;

  public static ReviewResponse fromEntity(Review review) {
    ReviewResponse dto = new ReviewResponse();
    dto.setId(review.getId());
    dto.setRating(review.getRating());
    dto.setComment(review.getComment());
    dto.setUserName(review.getUser().getUsername()); // Avoid exposing full user object
    dto.setProductId(review.getProduct().getId());
    dto.setCreatedAt(review.getCreatedAt());
    return dto;
  }
}
