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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private ReviewService reviewService;

  private User testUser;
  private Product testProduct;
  private ReviewRequest validReviewRequest;
  private Review savedReview;

  @BeforeEach
  void setUp() {
    // Arrange - Create test data
    testUser = new User(1L, "john.smith", "hashedPassword123");
    testProduct = new Product(100L, "Premium Wireless Headphones", new BigDecimal("299.99"), 50);

    validReviewRequest = new ReviewRequest();
    validReviewRequest.setRating(5);
    validReviewRequest.setComment("Excellent sound quality");

    savedReview = new Review();
    savedReview.setId(1L);
    savedReview.setProduct(testProduct);
    savedReview.setUser(testUser);
    savedReview.setRating(5);
    savedReview.setComment("Excellent sound quality");
    savedReview.setCreatedAt(Instant.now());
  }

  @Test
  void givenValidProductIdAndReviewRequest_whenCreatingReview_thenReturnReviewResponse() {
    // Arrange
    Long productId = 100L;
    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

    // Act
    ReviewResponse result = reviewService.createReview(productId, validReviewRequest, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(savedReview.getId(), result.getId());
    assertEquals(savedReview.getRating(), result.getRating());
    assertEquals(savedReview.getComment(), result.getComment());
    assertEquals(testUser.getUsername(), result.getUserName());
    assertEquals(testProduct.getId(), result.getProductId());
    assertEquals(savedReview.getCreatedAt(), result.getCreatedAt());

    verify(productRepository).findById(productId);
    verify(reviewRepository).findByProductIdAndUserId(productId, testUser.getId());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  void givenNonExistentProductId_whenCreatingReview_thenThrowProductNotFoundException() {
    // Arrange
    Long nonExistentProductId = 999L;
    when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

    // Act & Assert
    ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
        () -> reviewService.createReview(nonExistentProductId, validReviewRequest, testUser));

    assertEquals("Product not found with id: " + nonExistentProductId, exception.getMessage());
    verify(productRepository).findById(nonExistentProductId);
    verify(reviewRepository, never()).findByProductIdAndUserId(any(), any());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  void givenExistingReviewForSameUserAndProduct_whenCreatingReview_thenThrowReviewConflictEx() {
    // Arrange
    final Long productId = 100L;
    Review existingReview = new Review();
    existingReview.setId(2L);
    existingReview.setProduct(testProduct);
    existingReview.setUser(testUser);
    existingReview.setRating(4);
    existingReview.setComment("Previous review");

    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenReturn(Optional.of(existingReview));

    // Act & Assert
    ReviewConflictException exception = assertThrows(ReviewConflictException.class,
        () -> reviewService.createReview(productId, validReviewRequest, testUser));

    assertEquals("You have already submitted a review for this product.", exception.getMessage());
    verify(productRepository).findById(productId);
    verify(reviewRepository).findByProductIdAndUserId(productId, testUser.getId());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  @DisplayName("Given minimum rating value, when creating review, then save review successfully")
  void givenMinimumRatingValue_whenCreatingReview_thenSaveReviewSuccessfully() {
    // Arrange
    final Long productId = 100L;
    ReviewRequest minRatingRequest = new ReviewRequest();
    minRatingRequest.setRating(1);
    minRatingRequest.setComment("Poor quality product, not satisfied with purchase.");

    Review minRatingSavedReview = new Review();
    minRatingSavedReview.setId(3L);
    minRatingSavedReview.setProduct(testProduct);
    minRatingSavedReview.setUser(testUser);
    minRatingSavedReview.setRating(1);
    minRatingSavedReview.setComment("Poor quality product, not satisfied with purchase.");
    minRatingSavedReview.setCreatedAt(Instant.now());

    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(minRatingSavedReview);

    // Act
    ReviewResponse result = reviewService.createReview(productId, minRatingRequest, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getRating());
    assertEquals("Poor quality product, not satisfied with purchase.", result.getComment());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  @DisplayName("Given maximum rating value, when creating review, then save review successfully")
  void givenMaximumRatingValue_whenCreatingReview_thenSaveReviewSuccessfully() {
    // Arrange
    Long productId = 100L;
    ReviewRequest maxRatingRequest = new ReviewRequest();
    maxRatingRequest.setRating(5);
    maxRatingRequest.setComment("Outstanding product!");

    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

    // Act
    ReviewResponse result = reviewService.createReview(productId, maxRatingRequest, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(5, result.getRating());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  @DisplayName("Given null comment, when creating review, then save review successfully")
  void givenNullComment_whenCreatingReview_thenSaveReviewSuccessfully() {
    // Arrange
    final Long productId = 100L;
    ReviewRequest noCommentRequest = new ReviewRequest();
    noCommentRequest.setRating(4);
    noCommentRequest.setComment(null);

    Review noCommentSavedReview = new Review();
    noCommentSavedReview.setId(4L);
    noCommentSavedReview.setProduct(testProduct);
    noCommentSavedReview.setUser(testUser);
    noCommentSavedReview.setRating(4);
    noCommentSavedReview.setComment(null);
    noCommentSavedReview.setCreatedAt(Instant.now());

    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(noCommentSavedReview);

    // Act
    ReviewResponse result = reviewService.createReview(productId, noCommentRequest, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(4, result.getRating());
    assertNull(result.getComment());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  @DisplayName("Given empty comment, when creating review, then save review successfully")
  void givenEmptyComment_whenCreatingReview_thenSaveReviewSuccessfully() {
    // Arrange
    final Long productId = 100L;
    ReviewRequest emptyCommentRequest = new ReviewRequest();
    emptyCommentRequest.setRating(3);
    emptyCommentRequest.setComment("");

    Review emptyCommentSavedReview = new Review();
    emptyCommentSavedReview.setId(5L);
    emptyCommentSavedReview.setProduct(testProduct);
    emptyCommentSavedReview.setUser(testUser);
    emptyCommentSavedReview.setRating(3);
    emptyCommentSavedReview.setComment("");
    emptyCommentSavedReview.setCreatedAt(Instant.now());

    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(emptyCommentSavedReview);

    // Act
    ReviewResponse result = reviewService.createReview(productId, emptyCommentRequest, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(3, result.getRating());
    assertEquals("", result.getComment());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  void givenReviewCreation_whenSavingReview_thenVerifyCorrectReviewEntityIsSaved() {
    // Arrange
    Long productId = 100L;
    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

    ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);

    // Act
    reviewService.createReview(productId, validReviewRequest, testUser);

    // Assert
    verify(reviewRepository).save(reviewCaptor.capture());
    Review capturedReview = reviewCaptor.getValue();

    assertEquals(testProduct, capturedReview.getProduct());
    assertEquals(testUser, capturedReview.getUser());
    assertEquals(validReviewRequest.getRating(), capturedReview.getRating());
    assertEquals(validReviewRequest.getComment(), capturedReview.getComment());
  }

  @Test
  void givenDifferentUserForSameProduct_whenCreatingReview_thenSaveReviewSuccessfully() {
    // Arrange
    final Long productId = 100L;
    User differentUser = new User(2L, "maria.garcia", "anotherHashedPassword456");

    Review differentUserReview = new Review();
    differentUserReview.setId(6L);
    differentUserReview.setProduct(testProduct);
    differentUserReview.setUser(differentUser);
    differentUserReview.setRating(4);
    differentUserReview.setComment("Good product, fast shipping and excellent customer service.");
    differentUserReview.setCreatedAt(Instant.now());

    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, differentUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(differentUserReview);

    // Act
    ReviewResponse result = reviewService
        .createReview(productId, validReviewRequest, differentUser);

    // Assert
    assertNotNull(result);
    assertEquals(differentUser.getUsername(), result.getUserName());
    assertEquals(productId, result.getProductId());
    verify(reviewRepository).findByProductIdAndUserId(productId, differentUser.getId());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  void givenSameUserForDifferentProduct_whenCreatingReview_thenSaveReviewSuccessfully() {
    // Arrange
    final Long differentProductId = 200L;
    Product differentProduct = new Product(200L,
        "Gaming Mechanical Keyboard",
        new BigDecimal("149.99"), 25);

    Review differentProductReview = new Review();
    differentProductReview.setId(7L);
    differentProductReview.setProduct(differentProduct);
    differentProductReview.setUser(testUser);
    differentProductReview.setRating(5);
    differentProductReview
        .setComment("Amazing keyboard for gaming and typing. Great build quality!");
    differentProductReview.setCreatedAt(Instant.now());

    when(productRepository.findById(differentProductId)).thenReturn(Optional.of(differentProduct));
    when(reviewRepository
        .findByProductIdAndUserId(differentProductId, testUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(differentProductReview);

    // Act
    ReviewResponse result = reviewService
        .createReview(differentProductId, validReviewRequest, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getUsername(), result.getUserName());
    assertEquals(differentProductId, result.getProductId());
    verify(reviewRepository).findByProductIdAndUserId(differentProductId, testUser.getId());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  @DisplayName("Given null product ID, when creating review, then repository is called with null")
  void givenNullProductId_whenCreatingReview_thenRepositoryIsCalledWithNull() {
    // Arrange
    Long nullProductId = null;
    when(productRepository.findById(nullProductId)).thenReturn(Optional.empty());

    // Act & Assert
    ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
        () -> reviewService.createReview(nullProductId, validReviewRequest, testUser));

    assertEquals("Product not found with id: null", exception.getMessage());
    verify(productRepository).findById(nullProductId);
    verify(reviewRepository, never()).findByProductIdAndUserId(any(), any());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  @DisplayName("Given zero product ID, when creating review, then throw ProductNotFoundException")
  void givenZeroProductId_whenCreatingReview_thenThrowProductNotFoundException() {
    // Arrange
    Long zeroProductId = 0L;
    when(productRepository.findById(zeroProductId)).thenReturn(Optional.empty());

    // Act & Assert
    ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
        () -> reviewService.createReview(zeroProductId, validReviewRequest, testUser));

    assertEquals("Product not found with id: 0", exception.getMessage());
    verify(productRepository).findById(zeroProductId);
    verify(reviewRepository, never()).findByProductIdAndUserId(any(), any());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  void givenNegativeProductId_whenCreatingReview_thenThrowProductNotFoundException() {
    // Arrange
    Long negativeProductId = -1L;
    when(productRepository.findById(negativeProductId)).thenReturn(Optional.empty());

    // Act & Assert
    ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
        () -> reviewService.createReview(negativeProductId, validReviewRequest, testUser));

    assertEquals("Product not found with id: -1", exception.getMessage());
    verify(productRepository).findById(negativeProductId);
    verify(reviewRepository, never()).findByProductIdAndUserId(any(), any());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  @DisplayName("Given user with null ID, when creating review, then handle gracefully")
  void givenUserWithNullId_whenCreatingReview_thenHandleGracefully() {
    // Arrange
    Long productId = 100L;
    User userWithNullId = new User(null, "testuser", "password");

    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, null)).thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

    // Act
    ReviewResponse result = reviewService
        .createReview(productId, validReviewRequest, userWithNullId);

    // Assert
    assertNotNull(result);
    verify(reviewRepository).findByProductIdAndUserId(productId, null);
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  @DisplayName("Given repository save failure, when creating review, then exception propagates")
  void givenRepositorySaveFailure_whenCreatingReview_thenExceptionPropagates() {
    // Arrange
    Long productId = 100L;
    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenReturn(Optional.empty());
    when(reviewRepository.save(any(Review.class)))
        .thenThrow(new RuntimeException("Database connection failed"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> reviewService.createReview(productId, validReviewRequest, testUser));

    assertEquals("Database connection failed", exception.getMessage());
    verify(productRepository).findById(productId);
    verify(reviewRepository).findByProductIdAndUserId(productId, testUser.getId());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  @DisplayName("Given product repository failure, when creating review, then exception propagates")
  void givenProductRepositoryFailure_whenCreatingReview_thenExceptionPropagates() {
    // Arrange
    Long productId = 100L;
    when(productRepository.findById(productId)).thenThrow(new RuntimeException("Database timeout"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> reviewService.createReview(productId, validReviewRequest, testUser));

    assertEquals("Database timeout", exception.getMessage());
    verify(productRepository).findById(productId);
    verify(reviewRepository, never()).findByProductIdAndUserId(any(), any());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  void givenReviewRepositoryCheckFailure_whenCreatingReview_thenExceptionPropagates() {
    // Arrange
    Long productId = 100L;
    when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
    when(reviewRepository.findByProductIdAndUserId(productId, testUser.getId()))
        .thenThrow(new RuntimeException("Query execution failed"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> reviewService.createReview(productId, validReviewRequest, testUser));

    assertEquals("Query execution failed", exception.getMessage());
    verify(productRepository).findById(productId);
    verify(reviewRepository).findByProductIdAndUserId(productId, testUser.getId());
    verify(reviewRepository, never()).save(any());
  }
}
