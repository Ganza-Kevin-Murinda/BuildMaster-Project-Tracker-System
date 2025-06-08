package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.request.CreateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.response.TaskResponseDTO;
import com.buildermaster.projecttracker.dto.response.TaskStatsDTO;
import com.buildermaster.projecttracker.dto.response.TaskSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for Task operations
 * Provides comprehensive business methods for task management
 */
public interface TaskService {

    // ===== CRUD OPERATIONS =====

    /**
     * Create a new task
     * @param createTaskRequest the task creation request
     * @return the created task response DTO
     */
    TaskResponseDTO createTask(CreateTaskRequestDTO createTaskRequest);

    /**
     * Get task by ID
     * @param taskId the task ID
     * @return the task response DTO
     */
    TaskResponseDTO getTaskById(UUID taskId);

    /**
     * Get all tasks
     * @return list of all task response DTOs
     */
    List<TaskResponseDTO> getAllTasks();

    /**
     * Get all tasks with pagination
     * @param pageable pagination information
     * @return page of task response DTOs
     */
    Page<TaskResponseDTO> getAllTasks(Pageable pageable);

    /**
     * Update an existing task
     * @param taskId the task ID to update
     * @param updateTaskRequest the task update request
     * @return the updated task response DTO
     */
    TaskResponseDTO updateTask(UUID taskId, UpdateTaskRequestDTO updateTaskRequest);

    /**
     * Delete a task
     * @param taskId the task ID to delete
     */
    void deleteTask(UUID taskId);

    // ===== BUSINESS OPERATIONS =====

    /**
     * Get tasks by project ID
     * @param projectId the project ID
     * @return list of task response DTOs for the project
     */
    List<TaskResponseDTO> getTasksByProjectId(UUID projectId);

    /**
     * Get tasks by project ID with pagination
     * @param projectId the project ID
     * @param pageable pagination information
     * @return page of task response DTOs for the project
     */
    Page<TaskResponseDTO> getTasksByProjectId(UUID projectId, Pageable pageable);

    /**
     * Get tasks by developer ID
     * @param developerId the developer ID
     * @return list of task response DTOs assigned to the developer
     */
    List<TaskResponseDTO> getTasksByDeveloperId(UUID developerId);

    /**
     * Get tasks by developer ID with pagination
     * @param developerId the developer ID
     * @param pageable pagination information
     * @return page of task response DTOs assigned to the developer
     */
    Page<TaskResponseDTO> getTasksByDeveloperId(UUID developerId, Pageable pageable);

    /**
     * Get tasks by status with pagination
     * @param status the task status
     * @param pageable pagination information
     * @return page of task response DTOs with the specified status
     */
    Page<TaskResponseDTO> getTasksByStatus(ETaskStatus status, Pageable pageable);

    /**
     * Get overdue tasks
     * @return list of overdue task response DTOs
     */
    List<TaskResponseDTO> getOverdueTasks();

    /**
     * Get overdue tasks with pagination
     * @param pageable pagination information
     * @return page of overdue task response DTOs
     */
    Page<TaskResponseDTO> getOverdueTasks(Pageable pageable);

    /**
     * Get unassigned tasks
     * @return list of unassigned task response DTOs
     */
    List<TaskResponseDTO> getUnassignedTasks();

    /**
     * Get unassigned tasks with pagination
     * @param pageable pagination information
     * @return page of unassigned task response DTOs
     */
    Page<TaskResponseDTO> getUnassignedTasks(Pageable pageable);

    /**
     * Assign task to developer
     * @param taskId the task ID
     * @param developerId the developer ID
     * @return the updated task response DTO
     */
    TaskResponseDTO assignTaskToDeveloper(UUID taskId, UUID developerId);

    /**
     * Unassign task from developer
     * @param taskId the task ID
     * @return the updated task response DTO
     */
    TaskResponseDTO unassignTask(UUID taskId);

    /**
     * Search tasks by title with pagination
     * @param title the search term for task title
     * @param pageable pagination information
     * @return page of task response DTOs matching the search criteria
     */
    Page<TaskResponseDTO> searchTasksByTitle(String title, Pageable pageable);

    /**
     * Search tasks by description with pagination
     * @param description the search term for task description
     * @param pageable pagination information
     * @return page of task response DTOs matching the search criteria
     */
    Page<TaskResponseDTO> searchTasksByDescription(String description, Pageable pageable);

    /**
     * Get tasks by due date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of task response DTOs with due dates in the specified range
     */
    List<TaskResponseDTO> getTasksByDueDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get tasks by due date range with pagination
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of task response DTOs with due dates in the specified range
     */
    Page<TaskResponseDTO> getTasksByDueDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Get tasks due today
     * @return list of task response DTOs due today
     */
    List<TaskResponseDTO> getTasksDueToday();

    /**
     * Get tasks due this week
     * @return list of task response DTOs due this week
     */
    List<TaskResponseDTO> getTasksDueThisWeek();

    /**
     * Get recently created tasks
     * @param sinceDate the date to look back from
     * @return list of recently created task response DTOs
     */
    List<TaskResponseDTO> getRecentlyCreatedTasks(LocalDateTime sinceDate);

    /**
     * Get recently updated tasks
     * @param sinceDate the date to look back from
     * @return list of recently updated task response DTOs
     */
    List<TaskResponseDTO> getRecentlyUpdatedTasks(LocalDateTime sinceDate);

    // ===== STATISTICS AND ANALYTICS =====

    /**
     * Get task counts by status
     * @return map of status to count
     */
    Map<ETaskStatus, Long> getTaskCountsByStatus();

    /**
     * Get comprehensive task statistics
     * @return task statistics DTO
     */
    TaskStatsDTO getTaskStatistics();

    /**
     * Get task statistics by project
     * @param projectId the project ID
     * @return task statistics DTO for the project
     */
    TaskStatsDTO getTaskStatisticsByProject(UUID projectId);

    /**
     * Get task statistics by developer
     * @param developerId the developer ID
     * @return task statistics DTO for the developer
     */
    TaskStatsDTO getTaskStatisticsByDeveloper(UUID developerId);

    /**
     * Get task count by project
     * @param projectId the project ID
     * @return count of tasks in the project
     */
    Long getTaskCountByProject(UUID projectId);

    /**
     * Get task count by developer
     * @param developerId the developer ID
     * @return count of tasks assigned to the developer
     */
    Long getTaskCountByDeveloper(UUID developerId);

    // ===== SUMMARY OPERATIONS =====

    /**
     * Get task summaries for a project
     * @param projectId the project ID
     * @param pageable pagination information
     * @return page of task summary DTOs
     */
    Page<TaskSummaryDTO> getTaskSummariesByProject(UUID projectId, Pageable pageable);

    /**
     * Get task summaries for a developer
     * @param developerId the developer ID
     * @param pageable pagination information
     * @return page of task summary DTOs
     */
    Page<TaskSummaryDTO> getTaskSummariesByDeveloper(UUID developerId, Pageable pageable);

    /**
     * Get task summaries by status
     * @param status the task status
     * @param pageable pagination information
     * @return page of task summary DTOs
     */
    Page<TaskSummaryDTO> getTaskSummariesByStatus(ETaskStatus status, Pageable pageable);
}