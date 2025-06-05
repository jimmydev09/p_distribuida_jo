package com.programacion.distribuida.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "books")
public class Book {
    @Id
    @Column(length = 128)
    private String isbn;

    @OneToOne(mappedBy = "book")
    private Inventory inventory;

    @Column(length = 128)
    private String title;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    private Integer version = 1;
}