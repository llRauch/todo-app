package com.petproject.todo_app.controller;

import com.petproject.todo_app.model.Task;
import com.petproject.todo_app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class TaskWebController {

    private final TaskService taskService;

    @GetMapping
    public String showTaskList(Model model) {
        List<Task> tasks = taskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        model.addAttribute("newTask", new Task());
        return "tasks";
    }

    @PostMapping("/add")
    public String addTask(@ModelAttribute Task newTask, RedirectAttributes redirectAttributes) {
        if (newTask.getDescription() == null || newTask.getDescription().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Описание задачи не может быть пустым!");
            return "redirect:/";
        }

        if (newTask.getCompleted() == null) {
            newTask.setCompleted(false);
        }

        taskService.createTask(newTask);
        redirectAttributes.addFlashAttribute("message", "Задача успешно добавлена!");
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isPresent()) {
            model.addAttribute("task", taskOptional.get());
            return "edit-task";
        } else {
            redirectAttributes.addFlashAttribute("error", "Задача с ID " + id + " не найдена для редактирования.");
            return "redirect:/";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleTaskComplete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var taskOptional = taskService.getTaskById(id);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            task.setCompleted(!task.getCompleted());
            taskService.updateTask(id, task);
            redirectAttributes.addFlashAttribute("message", "Статус задачи '" + task.getDescription() + "' изменен.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Задача с ID " + id + " не найдена.");
        }
        return "redirect:/";
    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute("task") Task formTask,
                             RedirectAttributes redirectAttributes) {

        if (formTask.getDescription() == null || formTask.getDescription().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Описание задачи не может быть пустым!");
            redirectAttributes.addFlashAttribute("task", formTask);
            return "redirect:/edit/" + id;
        }

        Optional<Task> existingTaskOptional = taskService.getTaskById(id);

        if (existingTaskOptional.isPresent()) {
            Task existingTask = existingTaskOptional.get();
            existingTask.setDescription(formTask.getDescription());
            taskService.updateTask(id, existingTask);
            redirectAttributes.addFlashAttribute("message", "Задача '" + existingTask.getDescription() + "' успешно обновлена!");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Не удалось найти задачу с ID " + id + " для обновления.");
            return "redirect:/";
        }
    }

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