package org.course.controller;

import org.course.dto.ReviewCreateDTO;
import org.course.dto.ReviewDto;
import org.course.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    @GetMapping("/dish/{dishId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByDishId(@PathVariable Long dishId) {
        List<ReviewDto> reviews = reviewService.getReviewsByDish(dishId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUserId(@PathVariable Long userId) {
        List<ReviewDto> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }


    @PostMapping
    public ResponseEntity<ReviewDto> addReview(@RequestBody ReviewCreateDTO reviewCreateDTO) {
        ReviewDto createdReview = reviewService.createReview(reviewCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<ReviewDto> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/sort")
    public ResponseEntity<List<ReviewDto>> sortReviews(
            @RequestParam(required = false, defaultValue = "date") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order) {

        List<ReviewDto> sortedReviews = reviewService.sortReviews(sortBy, order);
        return ResponseEntity.ok(sortedReviews);
    }

}
