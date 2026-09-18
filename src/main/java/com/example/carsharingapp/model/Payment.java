package com.example.carsharingapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "status_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Status status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payment_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Type type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;
    @Column(nullable = false)
    private String sessionUrl;
    @Column(nullable = false)
    private String sessionId;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    public enum Status {
        PENDING, PAID
    }

    public enum Type {
        PAYMENT, FINE
    }
}
