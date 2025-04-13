package com.petproject.todo_app.service;

import com.petproject.todo_app.model.Task;
import com.petproject.todo_app.repository.TaskRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true) // Транзакция только для чтения (оптимизация)
    public List<Task> getAllTasks() {
        return taskRepository.findAll(); // Используем метод из JpaRepository
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        // findById возвращает Optional, что удобно для обработки случая "не найдено"
        return taskRepository.findById(id);
    }

    // --- CREATE ---
    @Transactional // Стандартная транзакция (чтение и запись)
    public Task createTask(Task task) {
        // Можно добавить проверки перед сохранением, если нужно
        // ID будет сгенерирован автоматически при сохранении
        // createdAt установится автоматически благодаря @PrePersist в Task
        return taskRepository.save(task);
    }

    // --- UPDATE ---
    @Transactional
    public Optional<Task> updateTask(Long id, Task taskDetails) {
        // 1. Найти существующую задачу
        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isPresent()) {
            // 2. Если найдена, обновить ее поля
            Task existingTask = optionalTask.get();
            existingTask.setDescription(taskDetails.getDescription());
            existingTask.setCompleted(taskDetails.getCompleted());
            // createdAt не обновляем

            // 3. Сохранить обновленную задачу (метод save выполнит UPDATE для существующей сущности)
            return Optional.of(taskRepository.save(existingTask));
        } else {
            // 4. Если не найдена, вернуть пустой Optional
            return Optional.empty();
        }
    }

    // --- DELETE ---
    @Transactional
    public boolean deleteTask(Long id) {
        // 1. Проверить, существует ли задача
        if (taskRepository.existsById(id)) {
            // 2. Если да, удалить ее
            taskRepository.deleteById(id);
            return true; // Успешно удалено
        } else {
            // 3. Если нет, вернуть false
            return false;
        }
    }
}