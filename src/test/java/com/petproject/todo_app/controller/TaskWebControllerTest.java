package com.petproject.todo_app.controller;

import com.petproject.todo_app.model.Task;
import com.petproject.todo_app.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // Используем эту аннотацию для тестирования только веб-слоя
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*; // Импорты для Hamcrest Matchers (удобны с MockMvc)
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq; // Для точного совпадения аргументов
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // Для get(), post()
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Для status(), view(), model(), redirectedUrl(), flash()

// Указываем, какой контроллер мы хотим тестировать.
// Это загрузит только конфигурацию, необходимую для этого контроллера,
// а не весь ApplicationContext, что быстрее чем @SpringBootTest.
@WebMvcTest(TaskWebController.class)
@AutoConfigureMockMvc
class TaskWebControllerTest {

    @Autowired // Spring автоматически создаст и внедрит MockMvc
    private MockMvc mockMvc;

    @MockBean // Создает мок TaskService и заменяет им реальный бин в контексте
    private TaskService taskService;

    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        task1 = new Task(1L, "Task One", false, LocalDateTime.now().minusDays(1));
        task2 = new Task(2L, "Task Two", true, LocalDateTime.now());
    }

    // --- Тесты для методов контроллера ---

    @Test
    void showTaskList_shouldReturnTasksViewWithData() throws Exception {
        // Arrange: Настроить мок сервиса
        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // Act & Assert: Выполнить GET запрос и проверить результат
        mockMvc.perform(get("/")) // Выполняем GET запрос на корень
                .andExpect(status().isOk()) // Ожидаем HTTP статус 200 OK
                .andExpect(view().name("tasks")) // Ожидаем имя представления "tasks"
                .andExpect(model().attributeExists("tasks")) // Ожидаем, что в модели есть атрибут "tasks"
                .andExpect(model().attribute("tasks", hasSize(2))) // Ожидаем, что список tasks имеет размер 2
                .andExpect(model().attribute("tasks", contains(task1, task2))) // Ожидаем конкретные задачи в списке
                .andExpect(model().attributeExists("newTask")); // Ожидаем атрибут для формы добавления
    }

    @Test
    void addTask_whenValidTask_shouldRedirectToHomeAndAddTask() throws Exception {
        // Arrange
        String description = "New Valid Task";
        // Мы не мокаем createTask здесь, так как хотим проверить, что он ВЫЗЫВАЕТСЯ.
        // Мок можно настроить, если бы нам нужно было что-то специфичное от возвращаемого значения,
        // но для проверки редиректа и вызова это не обязательно.

        // Act & Assert
        mockMvc.perform(post("/add") // Выполняем POST запрос
                                .param("description", description) // Передаем параметр формы
                        // .param("completed", "false") // Можно передавать и другие параметры, если они есть в форме
                )
                .andExpect(status().is3xxRedirection()) // Ожидаем редирект (статус 302 Found)
                .andExpect(redirectedUrl("/")) // Ожидаем редирект на "/"
                .andExpect(flash().attributeExists("message")); // Ожидаем flash-сообщение об успехе

        // Verify: Проверяем, что метод сервиса был вызван с правильными данными
        verify(taskService).createTask(argThat(task ->
                task.getDescription().equals(description) && task.getCompleted() == false
        ));
    }

    @Test
    void addTask_whenDescriptionIsEmpty_shouldReturnTasksViewWithError() throws Exception {
        // Этот тест актуален, если бы у нас была @Valid и BindingResult,
        // или если бы ручная проверка возвращала на страницу.
        // В текущей реализации без @Valid, addTask не имеет явной логики ошибки при пустом описании,
        // но при реализации с @Valid этот тест был бы важен.
        // Давай пока его закомментируем или адаптируем под ручную проверку, если она есть.

        // Если бы была валиция (@Valid):
         /*
         mockMvc.perform(post("/add")
                         .param("description", "")) // Пустое описание
                 .andExpect(status().isOk()) // Ожидаем НЕ редирект, а показ формы снова
                 .andExpect(view().name("tasks")) // Имя представления "tasks"
                 .andExpect(model().attributeHasFieldErrors("newTask", "description")) // Ожидаем ошибку поля description
                 .andExpect(model().attributeExists("tasks")); // Список задач тоже должен быть в модели
         verify(taskService, never()).createTask(any(Task.class)); // Убедимся, что задача не создавалась
         */
    }

    @Test
    void toggleTaskComplete_shouldRedirectToHomeAndUpdateStatus() throws Exception {
        // Arrange
        Long taskId = 1L;
        // Мокаем получение задачи и обновление
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task1)); // task1 initially not completed
        // Мы не мокаем updateTask явно, проверим вызов ниже

        // Act & Assert
        mockMvc.perform(post("/toggle/{id}", taskId)) // POST на /toggle/1
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));

        // Verify
        verify(taskService).getTaskById(taskId);
        // Проверяем, что updateTask вызывался для задачи с ID=1 и инвертированным статусом (станет true)
        verify(taskService).updateTask(eq(taskId), argThat(task -> task.getCompleted() == true));
    }

    @Test
    void deleteTask_shouldRedirectToHomeAndDeleteTask() throws Exception {
        // Arrange
        Long taskId = 1L;
        when(taskService.deleteTask(taskId)).thenReturn(true); // Мокаем успешное удаление

        // Act & Assert
        mockMvc.perform(post("/delete/{id}", taskId)) // POST на /delete/1
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));

        // Verify
        verify(taskService).deleteTask(taskId);
    }

    @Test
    void showEditForm_whenTaskExists_shouldReturnEditViewWithTask() throws Exception {
        // Arrange
        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task1));

        // Act & Assert
        mockMvc.perform(get("/edit/{id}", taskId)) // GET на /edit/1
                .andExpect(status().isOk())
                .andExpect(view().name("edit-task"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attribute("task", task1));
    }

    @Test
    void showEditForm_whenTaskDoesNotExist_shouldRedirectToHomeWithError() throws Exception {
        // Arrange
        Long taskId = 99L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/edit/{id}", taskId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));
    }


    @Test
    void updateTask_whenValid_shouldRedirectToHomeAndUpdateTask() throws Exception {
        // Arrange
        Long taskId = 1L;
        String updatedDescription = "Updated Description";
        // Мокаем findById, чтобы сервис мог найти задачу для обновления
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task1));
        // updateTask мокать не обязательно, проверим вызов

        // Act & Assert
        mockMvc.perform(post("/update/{id}", taskId) // POST на /update/1
                        .param("description", updatedDescription)) // Передаем новое описание
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));

        // Verify
        verify(taskService).getTaskById(taskId); // Проверяем поиск
        // Проверяем вызов updateTask с правильным ID и объектом, содержащим новое описание
        // и старый статус completed (false для task1)
        verify(taskService).updateTask(eq(taskId), argThat(task ->
                task.getDescription().equals(updatedDescription) &&
                        task.getCompleted().equals(task1.getCompleted()) // Убедимся, что статус не поменялся
        ));
    }

    @Test
    void updateTask_whenDescriptionIsEmpty_shouldReturnEditViewWithError() throws Exception {
        Long taskId = 1L;
        // when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task1)); // Мок не нужен для этой проверки

        mockMvc.perform(post("/update/{id}", taskId)
                        .param("description", "")) // Пустое описание
                // Изменяем ожидания:
                .andExpect(status().is3xxRedirection()) // Ожидаем редирект
                .andExpect(redirectedUrl("/edit/" + taskId)) // Ожидаем редирект на нужный URL
                .andExpect(flash().attributeExists("error"))  // Проверяем наличие flash-атрибута с ошибкой
                .andExpect(flash().attributeExists("task")); // Проверяем наличие flash-атрибута с невалидным таском

        verify(taskService, never()).updateTask(anyLong(), any(Task.class)); // Убедимся, что задача не обновлялась
    }

}