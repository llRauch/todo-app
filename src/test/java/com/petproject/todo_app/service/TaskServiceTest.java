package com.petproject.todo_app.service;

import com.petproject.todo_app.model.Task;
import com.petproject.todo_app.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach; // Для JUnit 5
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension; // Для интеграции Mockito с JUnit 5

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat; // Статический импорт для AssertJ
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*; // Статический импорт для методов Mockito

@ExtendWith(MockitoExtension.class) // Говорит JUnit 5 использовать расширение Mockito
class TaskServiceTest {

    @Mock // Создает мок (подделку) для TaskRepository
    private TaskRepository taskRepository;

    @InjectMocks // Создает экземпляр TaskService и автоматически внедряет в него моки (@Mock)
    private TaskService taskService;

    // Можно добавить поля для тестовых данных, чтобы не создавать их в каждом тесте
    private Task task1;
    private Task task2;

    @BeforeEach
        // Этот метод будет выполняться перед каждым @Test методом
    void setUp() {
        // Инициализируем тестовые данные здесь
        task1 = new Task(1L, "Task One", false, LocalDateTime.now().minusDays(1));
        task2 = new Task(2L, "Task Two", true, LocalDateTime.now());
        // Обрати внимание: мы используем конструктор @AllArgsConstructor,
        // так как @PrePersist не сработает в юнит-тестах без JPA контекста.
        // Либо можно установить createdAt вручную после создания.
    }

    // --- Теперь пишем сами тесты ---

    @Test
    void getAllTasks_shouldReturnListOfTasks() {
        // Arrange (Подготовка): Настраиваем поведение мока
        // Когда будет вызван taskRepository.findAll(), вернуть заранее подготовленный список
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        // Act (Действие): Вызываем тестируемый метод
        List<Task> result = taskService.getAllTasks();

        // Assert (Проверка): Проверяем результат и взаимодействия
        assertThat(result).isNotNull(); // Проверяем, что результат не null
        assertThat(result).hasSize(2); // Проверяем размер списка
        assertThat(result).containsExactly(task1, task2); // Проверяем содержимое списка

        // Дополнительно: убедимся, что метод findAll() у репозитория был вызван ровно 1 раз
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTaskById_whenTaskExists_shouldReturnOptionalWithTask() {
        // Arrange
        Long taskId = 1L;
        // Когда будет вызван taskRepository.findById(1L), вернуть Optional с task1
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task1));

        // Act
        Optional<Task> result = taskService.getTaskById(taskId);

        // Assert
        assertThat(result).isPresent(); // Проверяем, что Optional не пустой
        assertThat(result.get()).isEqualTo(task1); // Проверяем, что внутри Optional нужный task

        // Проверяем вызов findById
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_shouldReturnEmptyOptional() {
        // Arrange
        Long taskId = 99L; // Несуществующий ID
        // Когда будет вызван taskRepository.findById(99L), вернуть пустой Optional
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskService.getTaskById(taskId);

        // Assert
        assertThat(result).isNotPresent(); // Проверяем, что Optional пустой

        // Проверяем вызов findById
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void createTask_shouldReturnSavedTask() {
        // Arrange
        Task newTask = new Task("New Task"); // Без ID и createdAt
        Task savedTask = new Task(3L, "New Task", false, LocalDateTime.now()); // Задача, которую "вернет" репозиторий

        // Когда будет вызван taskRepository.save с ЛЮБЫМ объектом Task,
        // вернуть заранее подготовленный savedTask
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        // Важно: Мы передаем newTask в сервис, но мок возвращает savedTask (с ID и датой)

        // Act
        Task result = taskService.createTask(newTask);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedTask.getId()); // Проверяем, что у результата есть ID
        assertThat(result.getDescription()).isEqualTo(newTask.getDescription()); // Описание должно совпадать
        assertThat(result.getCompleted()).isFalse(); // Проверяем дефолтный статус
        assertThat(result.getCreatedAt()).isNotNull(); // Проверяем, что дата установилась (хотя бы в моке)

        // Проверяем, что метод save был вызван 1 раз с объектом newTask (или эквивалентным ему)
        verify(taskRepository, times(1)).save(newTask);
        // Можно использовать ArgumentCaptor для проверки переданного объекта детальнее, если нужно
    }

    @Test
    void updateTask_whenTaskExists_shouldReturnUpdatedTask() {
        // Arrange
        Long taskId = 1L;
        Task taskDetailsToUpdate = new Task(); // Объект с новыми данными
        taskDetailsToUpdate.setDescription("Updated Task One");
        taskDetailsToUpdate.setCompleted(true);

        // Мок для findById: находим существующую задачу task1
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task1));

        // Мок для save: когда сохраняем обновленную task1, возвращаем ее же (или копию с изменениями)
        // Важно: метод save в JPA возвращает управляемую сущность, которая может отличаться от переданной
        // Мы ожидаем, что сервис вызовет save с task1, у которого будут изменены поля description и completed
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task taskToSave = invocation.getArgument(0);
            // Убедимся, что сохраняется нужный объект с правильными изменениями
            assertThat(taskToSave.getId()).isEqualTo(taskId);
            assertThat(taskToSave.getDescription()).isEqualTo(taskDetailsToUpdate.getDescription());
            assertThat(taskToSave.getCompleted()).isEqualTo(taskDetailsToUpdate.getCompleted());
            return taskToSave; // Возвращаем тот же объект, что пришел на сохранение
        });


        // Act
        Optional<Task> result = taskService.updateTask(taskId, taskDetailsToUpdate);

        // Assert
        assertThat(result).isPresent();
        Task updatedTask = result.get();
        assertThat(updatedTask.getId()).isEqualTo(taskId);
        assertThat(updatedTask.getDescription()).isEqualTo("Updated Task One");
        assertThat(updatedTask.getCompleted()).isTrue();
        // Дата создания не должна меняться
        assertThat(updatedTask.getCreatedAt()).isEqualTo(task1.getCreatedAt());

        // Проверяем вызовы
        verify(taskRepository, times(1)).findById(taskId);
        // Проверяем, что save был вызван с объектом, у которого ID=1, Description="Updated...", Completed=true
        verify(taskRepository, times(1)).save(argThat(savedTask ->
                savedTask.getId().equals(taskId) &&
                        savedTask.getDescription().equals("Updated Task One") &&
                        savedTask.getCompleted().equals(true)
        ));
    }

    @Test
    void updateTask_whenTaskDoesNotExist_shouldReturnEmptyOptional() {
        // Arrange
        Long taskId = 99L;
        Task taskDetailsToUpdate = new Task(); // Используем конструктор без аргументов
        taskDetailsToUpdate.setDescription("Update Non Existent");
        taskDetailsToUpdate.setCompleted(false);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskService.updateTask(taskId, taskDetailsToUpdate);

        // Assert
        assertThat(result).isNotPresent();

        // Проверяем, что findById вызывался, а save - нет
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }


    @Test
    void deleteTask_whenTaskExists_shouldReturnTrue() {
        // Arrange
        Long taskId = 1L;
        // Настроим моки: задача существует, удаление ничего не возвращает (void)
        when(taskRepository.existsById(taskId)).thenReturn(true);
        // Для void методов используется doNothing()
        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        boolean result = taskService.deleteTask(taskId);

        // Assert
        assertThat(result).isTrue();

        // Проверяем, что оба метода репозитория были вызваны
        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void deleteTask_whenTaskDoesNotExist_shouldReturnFalse() {
        // Arrange
        Long taskId = 99L;
        // Настроим мок: задача не существует
        when(taskRepository.existsById(taskId)).thenReturn(false);

        // Act
        boolean result = taskService.deleteTask(taskId);

        // Assert
        assertThat(result).isFalse();

        // Проверяем, что existsById вызывался, а deleteById - нет
        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskRepository, never()).deleteById(taskId); // Убедимся, что deleteById не вызывался
    }
}