package com.petproject.todo_app.controller;

import com.petproject.todo_app.model.Task;
import com.petproject.todo_app.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(TaskWebController.class)
@AutoConfigureMockMvc
class TaskWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        task1 = new Task(1L, "Task One", false, LocalDateTime.now().minusDays(1));
        task2 = new Task(2L, "Task Two", true, LocalDateTime.now());
    }

    @Test
    void showTaskList_shouldReturnTasksViewWithData() throws Exception {
        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskService.getAllTasks()).thenReturn(tasks);
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attribute("tasks", hasSize(2)))
                .andExpect(model().attribute("tasks", contains(task1, task2)))
                .andExpect(model().attributeExists("newTask"));
    }

    @Test
    void addTask_whenValidTask_shouldRedirectToHomeAndAddTask() throws Exception {
        String description = "New Valid Task";
        mockMvc.perform(post("/add")
                        .param("description", description)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));
        verify(taskService).createTask(argThat(task ->
                task.getDescription().equals(description) && task.getCompleted() == false
        ));
    }

    @Test
    void toggleTaskComplete_shouldRedirectToHomeAndUpdateStatus() throws Exception {
        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task1));
        mockMvc.perform(post("/toggle/{id}", taskId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));
        verify(taskService).getTaskById(taskId);
        verify(taskService).updateTask(eq(taskId), argThat(task -> task.getCompleted() == true));
    }

    @Test
    void deleteTask_shouldRedirectToHomeAndDeleteTask() throws Exception {
        Long taskId = 1L;
        when(taskService.deleteTask(taskId)).thenReturn(true);
        mockMvc.perform(post("/delete/{id}", taskId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));
        verify(taskService).deleteTask(taskId);
    }

    @Test
    void showEditForm_whenTaskExists_shouldReturnEditViewWithTask() throws Exception {
        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task1));
        mockMvc.perform(get("/edit/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-task"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attribute("task", task1));
    }

    @Test
    void showEditForm_whenTaskDoesNotExist_shouldRedirectToHomeWithError() throws Exception {
        Long taskId = 99L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/edit/{id}", taskId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));
    }


    @Test
    void updateTask_whenValid_shouldRedirectToHomeAndUpdateTask() throws Exception {
        Long taskId = 1L;
        String updatedDescription = "Updated Description";
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task1));
        mockMvc.perform(post("/update/{id}", taskId)
                        .param("description", updatedDescription))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));
        verify(taskService).getTaskById(taskId);
        verify(taskService).updateTask(eq(taskId), argThat(task ->
                task.getDescription().equals(updatedDescription) &&
                        task.getCompleted().equals(task1.getCompleted())
        ));
    }

    @Test
    void updateTask_whenDescriptionIsEmpty_shouldReturnEditViewWithError() throws Exception {
        Long taskId = 1L;
        mockMvc.perform(post("/update/{id}", taskId)
                        .param("description", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/edit/" + taskId))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attributeExists("task"));
        verify(taskService, never()).updateTask(anyLong(), any(Task.class));
    }
}