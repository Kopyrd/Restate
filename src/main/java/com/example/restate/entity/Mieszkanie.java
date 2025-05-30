package com.example.restate.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "mieszkania")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "createdBy")
public class Mieszkanie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "developer")
    private String developer;

    @Column(name = "investment")
    private String investment;

    @Column(name = "number")
    private String number;

    @Column(name = "area", precision = 10, scale = 2)
    private BigDecimal area;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "rooms")
    private Integer rooms;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    // Dodatkowe pola dla funkcjonalności
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.AVAILABLE;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public enum Status {
        AVAILABLE,
        RESERVED,
        SOLD
    }
}