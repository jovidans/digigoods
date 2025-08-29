package com.example.digigoods.repository;

import com.example.digigoods.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
}

