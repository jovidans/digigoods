package com.example.digigoods.controller;

import com.example.digigoods.dto.ReviewRequest;
import com.example.digigoods.dto.ReviewResponse;
import com.example.digigoods.exception.MissingJwtTokenException;
import com.example.digigoods.model.Product;
import com.example.digigoods.model.User;
import com.example.digigoods.service.JwtService;
import com.example.digigoods.service.ProductService;
import com.example.digigoods.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for product endpoints.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final ReviewService reviewService;
  private final JwtService jwtService;

  /**
   * Get all products endpoint.
   *
   * @return list of all products
   */
  @GetMapping
  public ResponseEntity<List<Product>> getAllProducts() {
    List<Product> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
  }

  /**
   * Creates a new review for a specific product.
   * This endpoint is nested under the product resource it belongs to.
   *
   * @param productId The ID of the product being reviewed.
   * @param reviewRequest The review content from the request body.
   * @return A ResponseEntity containing the created review data and HTTP status 201.
   */
  @PostMapping("/{productId}/reviews")
  public ResponseEntity<ReviewResponse> createProductReview(
      @PathVariable Long productId,
      @Valid @RequestBody ReviewRequest reviewRequest,
      HttpServletRequest request
  ) {

    // Extract user ID from JWT token
    String token = extractTokenFromRequest(request);
    if (token == null) {
      throw new MissingJwtTokenException();
    }
    Long authenticatedUserId = jwtService.extractUserId(token);

    // In a real-world application, the authenticated user would be retrieved
    // from the Spring Security context (e.g., @AuthenticationPrincipal).
    // For demonstration purposes, we are creating a mock user.
    User currentUser = new User(authenticatedUserId, null, null);

    ReviewResponse createdReview = reviewService
        .createReview(productId, reviewRequest, currentUser);

    // Return a 201 CREATED status with the new review in the response body.
    return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
