package com.petproject.todo_app.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Boolean completed = false; // Статус задачи (по умолчанию - не выполнена)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Метод для установки даты создания перед сохранением
    // @PrePersist аннотация JPA гарантирует, что метод onCreate будет вызван
    // автоматически перед тем, как новая сущность Task
    // будет впервые сохранена в базу данных
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Конструктор для удобного создания новой задачи только с описанием
    public Task(String description) {
        this.description = description;
        // completed и createdAt будут установлены по умолчанию или через @PrePersist
    }

}
