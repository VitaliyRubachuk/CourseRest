package org.course.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "order_dishes",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id"))
    private List<Dishes> dishes;


    @Column(name = "fullprice")
    private double fullprice;

    @Column(name = "addition")
    private String addition;

    @Column(name = "dish_ids_string")
    private String dishIdsString;

    public void updateDishIdsString() {
        if (dishes != null) {
            this.dishIdsString = dishes.stream()
                    .map(dish -> String.valueOf(dish.getId()))
                    .collect(Collectors.joining(","));
        }
    }
}
