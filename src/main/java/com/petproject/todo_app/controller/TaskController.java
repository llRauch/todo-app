package com.petproject.todo_app.controller;

import com.petproject.todo_app.model.Task;
import com.petproject.todo_app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    // GET /api/v1/tasks - Получить все задачи
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
        // Spring автоматически преобразует List<Task> в JSON
    }

    // GET /api/v1/tasks/{id} - Получить задачу по ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> taskOptional = taskService.getTaskById(id);
        // Используем ResponseEntity для контроля над HTTP-ответом (статус + тело)
        return taskOptional
                .map(task -> ResponseEntity.ok(task)) // Если задача найдена, вернуть 200 OK и задачу
                .orElseGet(() -> ResponseEntity.notFound().build()); // Если не найдена, вернуть 404 Not Found
    }

    // POST /api/v1/tasks - Создать новую задачу
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        // @RequestBody говорит Spring взять данные для объекта Task из тела JSON запроса
        Task createdTask = taskService.createTask(task);
        // Возвращаем 201 Created и созданную задачу в теле ответа
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    // PUT /api/v1/tasks/{id} - Обновить задачу по ID
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        Optional<Task> updatedTaskOptional = taskService.updateTask(id, taskDetails);
        return updatedTaskOptional
                .map(task -> ResponseEntity.ok(task)) // 200 OK с обновленной задачей
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 Not Found
    }

    // DELETE /api/v1/tasks/{id} - Удалить задачу по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTask(id);
        if (deleted) {
            // Если удалено успешно, возвращаем 204 No Content (тело ответа пустое)
            return ResponseEntity.noContent().build();
        } else {
            // Если задача не найдена для удаления, возвращаем 404 Not Found
            return ResponseEntity.notFound().build();
        }

    }
}