package com.example.digigoods.service;

import com.example.digigoods.dto.ReviewRequest;
import com.example.digigoods.dto.ReviewResponse;
import com.example.digigoods.exception.ProductNotFoundException;
import com.example.digigoods.exception.ReviewConflictException;
import com.example.digigoods.model.Product;
import com.example.digigoods.model.Review;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.ProductRepository;
import com.example.digigoods.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor // Automatically injects final fields via constructor
public class ReviewService {

  private final ProductRepository productRepository;
  private final ReviewRepository reviewRepository;

  @Transactional
  public ReviewResponse createReview(
      Long productId,
      ReviewRequest reviewRequest,
      User currentUser
  ) {
    // 1. Find the product by its ID, or throw an exception if not found
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

    // 2. CONTROL FLOW: Check if the user has already reviewed this product.
    Optional<Review> existingReview = reviewRepository
        .findByProductIdAndUserId(productId, currentUser.getId());
    if (existingReview.isPresent()) {
      // If a review exists, throw a conflict exception.
      throw new ReviewConflictException("You have already submitted a review for this product.");
    }

    // 3. Create a new Review entity if no existing review was found
    Review review = new Review();
    review.setProduct(product);
    review.setUser(currentUser); // Associate with the currently authenticated user
    review.setRating(reviewRequest.getRating());
    review.setComment(reviewRequest.getComment());

    // 4. Save the new review to the database
    Review savedReview = reviewRepository.save(review);

    // 5. Map the saved entity to a DTO and return it
    return ReviewResponse.fromEntity(savedReview);
  }
}

