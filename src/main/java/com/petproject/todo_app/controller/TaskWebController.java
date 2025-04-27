package com.petproject.todo_app.controller;

import com.petproject.todo_app.model.Task;
import com.petproject.todo_app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // Используем @Controller, а не @RestController
import org.springframework.ui.Model; // Для передачи данных в шаблон
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller // Эта аннотация указывает, что класс будет обрабатывать
            // веб-запросы и возвращать имена представлений (шаблонов)
@RequestMapping("/")
@RequiredArgsConstructor
public class TaskWebController {

    private final TaskService taskService;

    // Метод для отображения главной страницы со списком всех задач
    @GetMapping
    public String showTaskList(Model model) {
        List<Task> tasks = taskService.getAllTasks(); // Получаем все задачи
        model.addAttribute("tasks", tasks); // Добавляем список задач в модель под именем "tasks"
        model.addAttribute("newTask", new Task()); // Добавляем пустой объект для формы добавления новой задачи
        return "tasks"; // Возвращаем имя HTML-шаблона (без расширения .html), который должен находиться в src/main/resources/templates/tasks.html
    }

    // Метод для обработки добавления новой задачи
    @PostMapping("/add")
    public String addTask(@ModelAttribute Task newTask, RedirectAttributes redirectAttributes) {
        // @ModelAttribute автоматически свяжет поля формы с полями объекта newTask
        // Простая валидация (можно улучшить)
        if (newTask.getDescription() == null || newTask.getDescription().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Описание задачи не может быть пустым!");
            return "redirect:/"; // Возвращаемся на главную, показываем ошибку
        }

        // Устанавливаем completed в false по умолчанию, если не передано из формы (пока формы нет)
        if (newTask.getCompleted() == null) {
            newTask.setCompleted(false);
        }

        taskService.createTask(newTask); // Сохраняем новую задачу через сервис
        redirectAttributes.addFlashAttribute("message", "Задача успешно добавлена!");
        return "redirect:/"; // Перенаправляем пользователя обратно на главную страницу (список задач)
        // Это стандартный паттерн Post-Redirect-Get (PRG) для избежания повторной отправки формы
    }

    // Метод для отображения формы редактирования задачи
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isPresent()) {
            model.addAttribute("task", taskOptional.get()); // Передаем найденную задачу в модель под именем "task"
            return "edit-task"; // Возвращаем имя шаблона для редактирования (edit-task.html)
        } else {
            redirectAttributes.addFlashAttribute("error", "Задача с ID " + id + " не найдена для редактирования.");
            return "redirect:/"; // Если задача не найдена, возвращаемся на главную
        }
    }

    // --- Место для методов обновления и удаления ---
    // (Добавим их позже)

    // Метод для обработки изменения статуса задачи (например, отметка о выполнении)
    // Используем PostMapping, чтобы избежать случайного изменения данных через GET
    @PostMapping("/toggle/{id}")
    public String toggleTaskComplete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var taskOptional = taskService.getTaskById(id);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            task.setCompleted(!task.getCompleted()); // Инвертируем статус
            taskService.updateTask(id, task); // Обновляем задачу
            redirectAttributes.addFlashAttribute("message", "Статус задачи '" + task.getDescription() + "' изменен.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Задача с ID " + id + " не найдена.");
        }
        return "redirect:/";
    }

    // Метод для обработки обновления задачи
    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute("task") Task formTask, // Получаем данные из формы
                             RedirectAttributes redirectAttributes) {

        // Простая валидация на пустое описание
        if (formTask.getDescription() == null || formTask.getDescription().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Описание задачи не может быть пустым!");
            // Нужно вернуть пользователя обратно на форму редактирования, а не на главную
            // Также нужно передать обратно введенные (невалидные) данные и ID
            redirectAttributes.addFlashAttribute("task", formTask); // Передаем невалидный объект обратно
            return "redirect:/edit/" + id; // Возвращаемся на форму редактирования для исправления
        }

        // Важно: Мы получили из формы только описание (и ID в пути).
        // Статус completed и дата createdAt в formTask будут null или стандартными.
        // Чтобы не затереть существующий статус completed, мы должны:
        // 1. Получить существующую задачу из базы.
        // 2. Обновить ТОЛЬКО те поля, которые мы редактировали (описание).
        // 3. Сохранить обновленную СУЩЕСТВУЮЩУЮ задачу.

        Optional<Task> existingTaskOptional = taskService.getTaskById(id);

        if (existingTaskOptional.isPresent()) {
            Task existingTask = existingTaskOptional.get();
            // Обновляем только описание из данных формы
            existingTask.setDescription(formTask.getDescription());

            // Вызываем метод сервиса для сохранения (он сам обработает UPDATE)
            // Передаем обновленный existingTask
            taskService.updateTask(id, existingTask); // или taskService.createTask(existingTask) если save используется и для update

            redirectAttributes.addFlashAttribute("message", "Задача '" + existingTask.getDescription() + "' успешно обновлена!");
            return "redirect:/"; // Перенаправляем на главную страницу после успешного обновления
        } else {
            redirectAttributes.addFlashAttribute("error", "Не удалось найти задачу с ID " + id + " для обновления.");
            return "redirect:/"; // Перенаправляем на главную, если задача вдруг исчезла
        }
    }

    // Метод для обработки удаления задачи
    // используем PostMapping для безопасности
    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = taskService.deleteTask(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Задача успешно удалена!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить задачу с ID " + id + ". Возможно, она уже была удалена.");
        }
        return "redirect:/";
    }
}