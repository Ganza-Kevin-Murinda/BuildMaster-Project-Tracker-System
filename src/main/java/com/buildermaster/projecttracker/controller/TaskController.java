package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.request.CreateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.TaskResponseDTO;
import com.buildermaster.projecttracker.dto.response.TaskStatsDTO;
import com.buildermaster.projecttracker.dto.response.TaskSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Management", description = "Comprehensive API for managing tasks in the project tracker system")
public class TaskController {

    private final TaskService taskService;

    // ===== CRUD OPERATIONS =====

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Project or developer not found")
    })
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> createTask(
            @Valid @RequestBody CreateTaskRequestDTO createTaskRequest) {
        log.info("Creating new task with title: {}", createTaskRequest.getTitle());

        try {
            TaskResponseDTO createdTask = taskService.createTask(createTaskRequest);
            log.info("Task created successfully with ID: {}", createdTask.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.created(createdTask));
        } catch (Exception e) {
            log.error("Error creating task: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Failed to create task: " + e.getMessage(), 400));
        }
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks with pagination and optional sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getAllTasks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching all tasks - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TaskResponseDTO> tasks = taskService.getAllTasks(pageable);
            log.info("retrieved {} Tasks", tasks.getTotalElements());

            return ResponseEntity.ok(ApiResponseDTO.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch tasks: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found and retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> getTaskById(
            @Parameter(description = "Task unique identifier") @PathVariable UUID id) {
        log.info("Fetching task with ID: {}", id);

        try {
            TaskResponseDTO task = taskService.getTaskById(id);
            log.info("Task retrieved successfully: {}", task.getTitle());

            return ResponseEntity.ok(ApiResponseDTO.success(task));
        } catch (Exception e) {
            log.error("Error fetching task with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch task statistics for project: " + e.getMessage(), 404));
        }
    }

    @GetMapping("/stats/developer/{developerId}")
    @Operation(summary = "Get task statistics by developer", description = "Retrieves task statistics for a specific developer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developer statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Developer not found")
    })
    public ResponseEntity<ApiResponseDTO<TaskStatsDTO>> getTaskStatisticsByDeveloper(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID developerId) {

        log.info("Fetching task statistics for developer: {}", developerId);

        try {
            TaskStatsDTO stats = taskService.getTaskStatisticsByDeveloper(developerId);
            log.info("Retrieved task statistics for developer {} - total tasks: {}", developerId, stats.getTotalTasks());

            return ResponseEntity.ok(ApiResponseDTO.success(stats));
        } catch (Exception e) {
            log.error("Error fetching task statistics for developer {}: {}", developerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch task statistics for developer: " + e.getMessage(), 404));
        }
    }

    @GetMapping("/count/project/{projectId}")
    @Operation(summary = "Get task count by project", description = "Retrieves the total number of tasks in a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project task count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponseDTO<Long>> getTaskCountByProject(
            @Parameter(description = "Project unique identifier") @PathVariable UUID projectId) {

        log.info("Fetching task count for project: {}", projectId);

        try {
            Long taskCount = taskService.getTaskCountByProject(projectId);
            log.info("Project {} has {} tasks", projectId, taskCount);

            return ResponseEntity.ok(ApiResponseDTO.success(taskCount));
        } catch (Exception e) {
            log.error("Error fetching task count for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch task count for project: " + e.getMessage(), 404));
        }
    }

    @GetMapping("/count/developer/{developerId}")
    @Operation(summary = "Get task count by developer", description = "Retrieves the total number of tasks assigned to a specific developer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developer task count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Developer not found")
    })
    public ResponseEntity<ApiResponseDTO<Long>> getTaskCountByDeveloper(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID developerId) {

        log.info("Fetching task count for developer: {}", developerId);

        try {
            Long taskCount = taskService.getTaskCountByDeveloper(developerId);
            log.info("Developer {} has {} assigned tasks", developerId, taskCount);

            return ResponseEntity.ok(ApiResponseDTO.success(taskCount));
        } catch (Exception e) {
            log.error("Error fetching task count for developer {}: {}", developerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch task count for developer: " + e.getMessage(), 404));
        }
    }

    // ===== SUMMARY OPERATIONS =====

    @GetMapping("/summaries/project/{projectId}")
    @Operation(summary = "Get task summaries by project", description = "Retrieves task summaries for a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project task summaries retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskSummaryDTO>>> getTaskSummariesByProject(
            @Parameter(description = "Project unique identifier") @PathVariable UUID projectId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "dueDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Fetching task summaries for project: {}", projectId);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TaskSummaryDTO> summaries = taskService.getTaskSummariesByProject(projectId, pageable);
            log.info("Retrieved {} task summaries for project {}", summaries.getTotalElements(), projectId);

            return ResponseEntity.ok(ApiResponseDTO.success(summaries));
        } catch (Exception e) {
            log.error("Error fetching task summaries for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch task summaries for project: " + e.getMessage(), 404));
        }
    }

    @GetMapping("/summaries/developer/{developerId}")
    @Operation(summary = "Get task summaries by developer", description = "Retrieves task summaries for a specific developer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developer task summaries retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Developer not found")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskSummaryDTO>>> getTaskSummariesByDeveloper(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID developerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "dueDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Fetching task summaries for developer: {}", developerId);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TaskSummaryDTO> summaries = taskService.getTaskSummariesByDeveloper(developerId, pageable);
            log.info("Retrieved {} task summaries for developer {}", summaries.getTotalElements(), developerId);

            return ResponseEntity.ok(ApiResponseDTO.success(summaries));
        } catch (Exception e) {
            log.error("Error fetching task summaries for developer {}: {}", developerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch task summaries for developer: " + e.getMessage(), 404));
        }
    }

    @GetMapping("/summaries/status/{status}")
    @Operation(summary = "Get task summaries by status", description = "Retrieves task summaries for a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status task summaries retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskSummaryDTO>>> getTaskSummariesByStatus(
            @Parameter(description = "Task status") @PathVariable ETaskStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "dueDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Fetching task summaries for status: {}", status);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TaskSummaryDTO> summaries = taskService.getTaskSummariesByStatus(status, pageable);
            log.info("Retrieved {} task summaries for status {}", summaries.getTotalElements(), status);

            return ResponseEntity.ok(ApiResponseDTO.success(summaries));
        } catch (Exception e) {
            log.error("Error fetching task summaries for status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Failed to fetch task summaries by status: " + e.getMessage(), 400));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates an existing task with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> updateTask(
            @Parameter(description = "Task unique identifier") @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequestDTO updateTaskRequest) {
        log.info("Updating task with ID: {}", id);

        try {
            TaskResponseDTO updatedTask = taskService.updateTask(id, updateTaskRequest);
            log.info("Task updated successfully: {}", updatedTask.getTitle());

            return ResponseEntity.ok(ApiResponseDTO.success("Task updated successfully", updatedTask));
        } catch (Exception e) {
            log.error("Error updating task with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Failed to update task: " + e.getMessage(), 400));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Permanently deletes a task from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteTask(
            @Parameter(description = "Task unique identifier") @PathVariable UUID id) {
        log.info("Deleting task with ID: {}", id);

        try {
            taskService.deleteTask(id);
            log.info("Task deleted successfully with ID: {}", id);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting task with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to delete task: " + e.getMessage(), 404));
        }
    }

    // ===== ASSIGNMENT OPERATIONS =====

    @PutMapping("/{taskId}/assign/{developerId}")
    @Operation(summary = "Assign task to developer", description = "Assigns a task to a specific developer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Task or developer not found")
    })
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> assignTaskToDeveloper(
            @Parameter(description = "Task unique identifier") @PathVariable UUID taskId,
            @Parameter(description = "Developer unique identifier") @PathVariable UUID developerId) {
        log.info("Assigning task {} to developer {}", taskId, developerId);

        try {
            TaskResponseDTO assignedTask = taskService.assignTaskToDeveloper(taskId, developerId);
            log.info("Task assigned successfully to developer: {}", assignedTask.getDeveloperName());

            return ResponseEntity.ok(ApiResponseDTO.success("Task assigned successfully", assignedTask));
        } catch (Exception e) {
            log.error("Error assigning task {} to developer {}: {}", taskId, developerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to assign task: " + e.getMessage(), 404));
        }
    }

    @PutMapping("/{taskId}/unassign")
    @Operation(summary = "Unassign task", description = "Removes the current developer assignment from a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task unassigned successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> unassignTask(
            @Parameter(description = "Task unique identifier") @PathVariable UUID taskId) {
        log.info("Unassigning task with ID: {}", taskId);

        try {
            TaskResponseDTO unassignedTask = taskService.unassignTask(taskId);
            log.info("Task unassigned successfully: {}", unassignedTask.getTitle());

            return ResponseEntity.ok(ApiResponseDTO.success("Task unassigned successfully", unassignedTask));
        } catch (Exception e) {
            log.error("Error unassigning task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to unassign task: " + e.getMessage(), 404));
        }
    }

    // ===== FILTERING ENDPOINTS =====

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project", description = "Retrieves all tasks belonging to a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getTasksByProject(
            @Parameter(description = "Project unique identifier") @PathVariable UUID projectId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching tasks for project ID: {}", projectId);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TaskResponseDTO> tasks = taskService.getTasksByProjectId(projectId, pageable);
            log.info("Retrieved {} tasks for project {}", tasks.getTotalElements(), projectId);

            return ResponseEntity.ok(ApiResponseDTO.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching tasks for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch tasks for project: " + e.getMessage(), 404));
        }
    }

    @GetMapping("/developer/{developerId}")
    @Operation(summary = "Get tasks by developer", description = "Retrieves all tasks assigned to a specific developer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Developer not found")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getTasksByDeveloper(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID developerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching tasks for developer ID: {}", developerId);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TaskResponseDTO> tasks = taskService.getTasksByDeveloperId(developerId, pageable);
            log.info("Retrieved {} tasks for developer {}", tasks.getTotalElements(), developerId);

            return ResponseEntity.ok(ApiResponseDTO.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching tasks for developer {}: {}", developerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch tasks for developer: " + e.getMessage(), 404));
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status", description = "Retrieves all tasks with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getTasksByStatus(
            @Parameter(description = "Task status") @PathVariable ETaskStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching tasks with status: {}", status);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TaskResponseDTO> tasks = taskService.getTasksByStatus(status, pageable);
            log.info("Retrieved {} tasks with status {}", tasks.getTotalElements(), status);

            return ResponseEntity.ok(ApiResponseDTO.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching tasks with status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Failed to fetch tasks by status: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks", description = "Retrieves all tasks that are past their due date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue tasks retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getOverdueTasks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching overdue tasks");

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dueDate"));
            Page<TaskResponseDTO> overdueTasks = taskService.getOverdueTasks(pageable);
            log.info("Retrieved {} overdue tasks", overdueTasks.getTotalElements());

            return ResponseEntity.ok(ApiResponseDTO.success(overdueTasks));
        } catch (Exception e) {
            log.error("Error fetching overdue tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch overdue tasks: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/unassigned")
    @Operation(summary = "Get unassigned tasks", description = "Retrieves all tasks that are not assigned to any developer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unassigned tasks retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getUnassignedTasks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching unassigned tasks");

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
            Page<TaskResponseDTO> unassignedTasks = taskService.getUnassignedTasks(pageable);
            log.info("Retrieved {} unassigned tasks", unassignedTasks.getTotalElements());

            return ResponseEntity.ok(ApiResponseDTO.success(unassignedTasks));
        } catch (Exception e) {
            log.error("Error fetching unassigned tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch unassigned tasks: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search tasks", description = "Searches tasks by title or description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> searchTasks(
            @Parameter(description = "Search term for title") @RequestParam(required = false) String title,
            @Parameter(description = "Search term for description") @RequestParam(required = false) String description,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Searching tasks - title: {}, description: {}", title, description);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TaskResponseDTO> searchResults;

            if (title != null && !title.trim().isEmpty()) {
                searchResults = taskService.searchTasksByTitle(title, pageable);
            } else if (description != null && !description.trim().isEmpty()) {
                searchResults = taskService.searchTasksByDescription(description, pageable);
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDTO.error("At least one search parameter (title or description) is required", 400));
            }

            log.info("Search returned {} results", searchResults.getTotalElements());
            return ResponseEntity.ok(ApiResponseDTO.success(searchResults));
        } catch (Exception e) {
            log.error("Error searching tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Search failed: " + e.getMessage(), 500));
        }
    }

    // ===== DATE-BASED FILTERING =====

    @GetMapping("/due-date-range")
    @Operation(summary = "Get tasks by due date range", description = "Retrieves tasks within a specified date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date parameters")
    })
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getTasksByDueDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching tasks with due date between {} and {}", startDate, endDate);

        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDTO.error("Start date cannot be after end date", 400));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dueDate"));
            Page<TaskResponseDTO> tasks = taskService.getTasksByDueDateRange(startDate, endDate, pageable);
            log.info("Retrieved {} tasks in date range", tasks.getTotalElements());

            return ResponseEntity.ok(ApiResponseDTO.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching tasks by date range: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Failed to fetch tasks by date range: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/due-today")
    @Operation(summary = "Get tasks due today", description = "Retrieves all tasks that are due today")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks due today retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getTasksDueToday() {
        log.info("Fetching tasks due today");

        try {
            List<TaskResponseDTO> tasksDueToday = taskService.getTasksDueToday();
            log.info("Retrieved {} tasks due today", tasksDueToday.size());

            return ResponseEntity.ok(ApiResponseDTO.success(tasksDueToday));
        } catch (Exception e) {
            log.error("Error fetching tasks due today: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch tasks due today: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/due-this-week")
    @Operation(summary = "Get tasks due this week", description = "Retrieves all tasks that are due within the current week")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks due this week retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getTasksDueThisWeek() {
        log.info("Fetching tasks due this week");

        try {
            List<TaskResponseDTO> tasksDueThisWeek = taskService.getTasksDueThisWeek();
            log.info("Retrieved {} tasks due this week", tasksDueThisWeek.size());

            return ResponseEntity.ok(ApiResponseDTO.success(tasksDueThisWeek));
        } catch (Exception e) {
            log.error("Error fetching tasks due this week: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch tasks due this week: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/recently-created")
    @Operation(summary = "Get recently created tasks", description = "Retrieves tasks created since a specified date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recently created tasks retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getRecentlyCreatedTasks(
            @Parameter(description = "Since date (yyyy-MM-dd'T'HH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sinceDate) {

        log.info("Fetching tasks created since: {}", sinceDate);

        try {
            List<TaskResponseDTO> recentTasks = taskService.getRecentlyCreatedTasks(sinceDate);
            log.info("Retrieved {} Recently created Tasks", recentTasks.size());

            return ResponseEntity.ok(ApiResponseDTO.success(recentTasks));
        } catch (Exception e) {
            log.error("Error fetching recently created tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch recently created tasks: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/recently-updated")
    @Operation(summary = "Get recently updated tasks", description = "Retrieves tasks updated since a specified date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recently updated tasks retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getRecentlyUpdatedTasks(
            @Parameter(description = "Since date (yyyy-MM-dd'T'HH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sinceDate) {

        log.info("Fetching tasks updated since: {}", sinceDate);

        try {
            List<TaskResponseDTO> recentTasks = taskService.getRecentlyUpdatedTasks(sinceDate);
            log.info("Retrieved {} recently updated task", recentTasks.size());

            return ResponseEntity.ok(ApiResponseDTO.success(recentTasks));
        } catch (Exception e) {
            log.error("Error fetching recently updated tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch recently updated tasks: " + e.getMessage(), 500));
        }
    }

    // ===== ANALYTICS AND STATISTICS =====

    @GetMapping("/stats")
    @Operation(summary = "Get task statistics", description = "Retrieves comprehensive task statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<TaskStatsDTO>> getTaskStatistics() {
        log.info("Fetching task statistics");

        try {
            TaskStatsDTO stats = taskService.getTaskStatistics();
            log.info("Retrieved task statistics - total tasks: {}", stats.getTotalTasks());

            return ResponseEntity.ok(ApiResponseDTO.success(stats));
        } catch (Exception e) {
            log.error("Error fetching task statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch task statistics: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/stats/by-status")
    @Operation(summary = "Get task count by status", description = "Retrieves task counts grouped by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status counts retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<Map<ETaskStatus, Long>>> getTaskCountsByStatus() {
        log.info("Fetching task counts by status");

        try {
            Map<ETaskStatus, Long> statusCounts = taskService.getTaskCountsByStatus();
            log.info("Retrieved task counts by status: {}", statusCounts);

            return ResponseEntity.ok(ApiResponseDTO.success(statusCounts));
        } catch (Exception e) {
            log.error("Error fetching task counts by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to fetch task counts by status: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/stats/project/{projectId}")
    @Operation(summary = "Get task statistics by project", description = "Retrieves task statistics for a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponseDTO<TaskStatsDTO>> getTaskStatisticsByProject(
            @Parameter(description = "Project unique identifier") @PathVariable UUID projectId) {

        log.info("Fetching task statistics for project: {}", projectId);

        try {
            TaskStatsDTO stats = taskService.getTaskStatisticsByProject(projectId);
            log.info("Retrieved task statistics for project {} - total tasks: {}", projectId, stats.getTotalTasks());

            return ResponseEntity.ok(ApiResponseDTO.success(stats));
        } catch (Exception e) {
            log.error("Error fetching task statistics for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Failed to fetch task statistics by project: " + e.getMessage(), 500));
        }
    }
}
