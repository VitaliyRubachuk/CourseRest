package org.course.service;

import org.course.dto.ReviewCreateDTO;
import org.course.dto.ReviewDto;
import org.course.entity.Dishes;
import org.course.entity.Review;
import org.course.entity.User;
import org.course.mapper.ReviewMapper;
import org.course.repository.DishesRepository;
import org.course.repository.ReviewRepository;
import org.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final DishesRepository dishesRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ReviewMapper reviewMapper,
                         UserRepository userRepository, DishesRepository dishesRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.userRepository = userRepository;
        this.dishesRepository = dishesRepository;
    }


    public ReviewDto createReview(ReviewCreateDTO reviewCreateDTO) {
        User user = userRepository.findById(reviewCreateDTO.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dishes dish = dishesRepository.findById(reviewCreateDTO.dishId())
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        Review review = reviewMapper.toEntity(reviewCreateDTO);
        review.setUser(user);
        review.setDish(dish);

        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toDto(savedReview);
    }

    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByDish(Long dishId) {
        return reviewRepository.findByDishId(dishId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public List<ReviewDto> sortReviews(String sortBy, String order) {
        List<Review> reviews = reviewRepository.findAll();

        Comparator<Review> comparator = null;
        if ("date".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Review::getCreatedAt);
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparingInt(Review::getRating);
        }

        if (comparator != null) {
            if ("desc".equalsIgnoreCase(order)) {
                comparator = comparator.reversed();
            }
            reviews.sort(comparator);
        }

        return reviews.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
}
