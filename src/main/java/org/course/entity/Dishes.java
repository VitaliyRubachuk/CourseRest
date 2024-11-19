package org.course.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "orders")
public class Dishes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //IDENTITY
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private double price;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @ManyToMany(mappedBy = "dishes")
    private List<Order> orders;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;


}
