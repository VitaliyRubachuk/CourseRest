package org.course.controller;

import jakarta.validation.Valid;
import org.course.dto.ReviewCreateDTO;
import org.course.dto.ReviewDto;
import org.course.exception.UnauthorizedReviewUpdateException;
import org.course.service.ReviewService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewCreateDTO reviewCreateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        ReviewDto updatedReview = reviewService.updateReview(id, reviewCreateDTO);
        return ResponseEntity.ok(updatedReview);
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
    public ResponseEntity<Object> addReview(@Valid @RequestBody ReviewCreateDTO reviewCreateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {

            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(errorMessages);
        }

        ReviewDto createdReview = reviewService.createReview(reviewCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok("Відгук успішно видалено.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Відгук не знайдено.");
        }
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
