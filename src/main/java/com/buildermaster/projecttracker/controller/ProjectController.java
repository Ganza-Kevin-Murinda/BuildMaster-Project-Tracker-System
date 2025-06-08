package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.request.CreateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.ProjectResponseDTO;
import com.buildermaster.projecttracker.dto.response.ProjectSummaryDTO;
import com.buildermaster.projecttracker.model.EProjectStatus;
import com.buildermaster.projecttracker.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Management", description = "APIs for managing projects in the system")
public class ProjectController {

    private final ProjectService projectService;

    // ===== CRUD OPERATIONS =====

    @PostMapping
    @Operation(
            summary = "Create a new project",
            description = "Creates a new project with the provided details. All fields are validated according to business rules."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Project with same name already exists")
    })
    public ResponseEntity<ApiResponseDTO<ProjectResponseDTO>> createProject(
            @Valid @RequestBody CreateProjectRequestDTO createRequest) {

        log.info("Creating new project with name: {}", createRequest.getName());

        ProjectResponseDTO project = projectService.createProject(createRequest);

        log.info("Successfully created project with ID: {}", project.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(project));
    }

    @GetMapping
    @Operation(
            summary = "Get all projects with pagination",
            description = "Retrieves a paginated list of all projects with summary information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getAllProjects(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 10, sort = "createdDate") Pageable pageable) {

        log.info("Retrieving all projects with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<ProjectSummaryDTO> projects = projectService.getAllProjects(pageable);

        log.info("Retrieved {} projects from page {}", projects.getNumberOfElements(),
                pageable.getPageNumber());

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get project by ID",
            description = "Retrieves detailed information about a specific project"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid project ID format")
    })
    public ResponseEntity<ApiResponseDTO<ProjectResponseDTO>> getProjectById(
            @Parameter(description = "Project unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {

        log.info("Retrieving project with ID: {}", id);

        ProjectResponseDTO project = projectService.getProjectById(id);

        log.info("Successfully retrieved project: {}", project.getName());

        return ResponseEntity.ok(ApiResponseDTO.success(project));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update project",
            description = "Updates an existing project with new information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ApiResponseDTO<ProjectResponseDTO>> updateProject(
            @Parameter(description = "Project unique identifier")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequestDTO updateRequest) {

        log.info("Updating project with ID: {} and name: {}", id, updateRequest.getName());

        ProjectResponseDTO project = projectService.updateProject(id, updateRequest);

        log.info("Successfully updated project with ID: {}", project.getId());

        return ResponseEntity.ok(ApiResponseDTO.success("Project updated successfully", project));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete project",
            description = "Permanently deletes a project and all its associated data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project unique identifier")
            @PathVariable UUID id) {

        log.info("Deleting project with ID: {}", id);

        projectService.deleteProject(id);

        log.info("Successfully deleted project with ID: {}", id);

        return ResponseEntity.noContent().build();
    }

    // ===== SPECIALIZED ENDPOINTS =====

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get projects by status",
            description = "Retrieves projects filtered by their current status with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or pagination parameters")
    })
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getProjectsByStatus(
            @Parameter(description = "Project status to filter by", example = "IN_PROGRESS")
            @PathVariable EProjectStatus status,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10, sort = "deadline") Pageable pageable) {

        log.info("Retrieving projects with status: {}", status);

        Page<ProjectSummaryDTO> projects = projectService.getProjectsByStatus(status, pageable);

        log.info("Found {} projects with status: {}", projects.getTotalElements(), status);

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/overdue")
    @Operation(
            summary = "Get overdue projects",
            description = "Retrieves all projects that have passed their deadline"
    )
    @ApiResponse(responseCode = "200", description = "Overdue projects retrieved successfully")
    public ResponseEntity<ApiResponseDTO<List<ProjectResponseDTO>>> getOverdueProjects() {

        log.info("Retrieving overdue projects");

        List<ProjectResponseDTO> projects = projectService.getOverdueProjects();

        log.info("Found {} overdue projects", projects.size());

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/empty")
    @Operation(
            summary = "Get projects without tasks",
            description = "Retrieves all projects that don't have any tasks assigned"
    )
    @ApiResponse(responseCode = "200", description = "Projects without tasks retrieved successfully")
    public ResponseEntity<ApiResponseDTO<List<ProjectResponseDTO>>> getProjectsWithoutTasks() {

        log.info("Retrieving projects without tasks");

        List<ProjectResponseDTO> projects = projectService.getProjectsWithoutTasks();

        log.info("Found {} projects without tasks", projects.size());

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/by-task-count")
    @Operation(
            summary = "Get projects ordered by task count",
            description = "Retrieves projects ordered by the number of tasks (highest first) with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Projects ordered by task count retrieved successfully")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getProjectsOrderedByTaskCount(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("Retrieving projects ordered by task count");

        Page<ProjectSummaryDTO> projects = projectService.getProjectsOrderedByTaskCount(pageable);

        log.info("Retrieved {} projects ordered by task count", projects.getNumberOfElements());

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search projects by name",
            description = "Searches projects by name containing the specified term with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> searchProjectsByName(
            @Parameter(description = "Search term for project name", example = "ecommerce")
            @RequestParam String name,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        log.info("Searching projects by name: {}", name);

        Page<ProjectSummaryDTO> projects = projectService.searchProjectsByName(name, pageable);

        log.info("Found {} projects matching name: {}", projects.getTotalElements(), name);

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/search/description")
    @Operation(
            summary = "Search projects by description",
            description = "Searches projects by description containing the specified term with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> searchProjectsByDescription(
            @Parameter(description = "Search term for project description")
            @RequestParam String description,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        log.info("Searching projects by description: {}", description);

        Page<ProjectSummaryDTO> projects = projectService.searchProjectsByDescription(description, pageable);

        log.info("Found {} projects matching description: {}", projects.getTotalElements(), description);

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/deadline-range")
    @Operation(
            summary = "Get projects by deadline range",
            description = "Retrieves projects with deadlines between the specified start and end dates"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<ApiResponseDTO<List<ProjectResponseDTO>>> getProjectsByDeadlineRange(
            @Parameter(description = "Start date (inclusive)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Retrieving projects with deadlines between {} and {}", startDate, endDate);

        List<ProjectResponseDTO> projects = projectService.getProjectsByDeadlineRange(startDate, endDate);

        log.info("Found {} projects in deadline range", projects.size());

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/deadline/before")
    @Operation(
            summary = "Get projects with deadlines before date",
            description = "Retrieves projects with deadlines before the specified date with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getProjectsWithDeadlineBefore(
            @Parameter(description = "Cutoff date", example = "2024-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10, sort = "deadline") Pageable pageable) {

        log.info("Retrieving projects with deadlines before: {}", date);

        Page<ProjectSummaryDTO> projects = projectService.getProjectsWithDeadlineBefore(date, pageable);

        log.info("Found {} projects with deadlines before: {}", projects.getTotalElements(), date);

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/deadline/after")
    @Operation(
            summary = "Get projects with deadlines after date",
            description = "Retrieves projects with deadlines after the specified date with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getProjectsWithDeadlineAfter(
            @Parameter(description = "Cutoff date", example = "2024-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10, sort = "deadline") Pageable pageable) {

        log.info("Retrieving projects with deadlines after: {}", date);

        Page<ProjectSummaryDTO> projects = projectService.getProjectsWithDeadlineAfter(date, pageable);

        log.info("Found {} projects with deadlines after: {}", projects.getTotalElements(), date);

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/recent/created")
    @Operation(
            summary = "Get recently created projects",
            description = "Retrieves projects ordered by creation date (newest first) with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Recently created projects retrieved successfully")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getRecentlyCreatedProjects(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("Retrieving recently created projects");

        Page<ProjectSummaryDTO> projects = projectService.getRecentlyCreatedProjects(pageable);

        log.info("Retrieved {} recently created projects", projects.getNumberOfElements());

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    @GetMapping("/recent/updated")
    @Operation(
            summary = "Get recently updated projects",
            description = "Retrieves projects ordered by last update date (newest first) with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Recently updated projects retrieved successfully")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getRecentlyUpdatedProjects(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("Retrieving recently updated projects");

        Page<ProjectSummaryDTO> projects = projectService.getRecentlyUpdatedProjects(pageable);

        log.info("Retrieved {} recently updated projects", projects.getNumberOfElements());

        return ResponseEntity.ok(ApiResponseDTO.success(projects));
    }

    // ===== UTILITY ENDPOINTS =====

    @GetMapping("/exists")
    @Operation(
            summary = "Check if project exists by name",
            description = "Checks whether a project with the specified name already exists"
    )
    @ApiResponse(responseCode = "200", description = "Existence check completed")
    public ResponseEntity<ApiResponseDTO<Boolean>> existsByName(
            @Parameter(description = "Project name to check", example = "E-commerce Platform")
            @RequestParam String name) {

        log.info("Checking if project exists with name: {}", name);

        boolean exists = projectService.existsByName(name);

        log.info("Project with name '{}' exists: {}", name, exists);

        return ResponseEntity.ok(ApiResponseDTO.success(exists));
    }

    @GetMapping("/count/status/{status}")
    @Operation(
            summary = "Get project count by status",
            description = "Returns the total number of projects with the specified status"
    )
    @ApiResponse(responseCode = "200", description = "Project count retrieved successfully")
    public ResponseEntity<ApiResponseDTO<Long>> getProjectCountByStatus(
            @Parameter(description = "Project status", example = "COMPLETED")
            @PathVariable EProjectStatus status) {

        log.info("Getting project count for status: {}", status);

        Long count = projectService.getProjectCountByStatus(status);

        log.info("Found {} Projects with status: {}", count, status);

        return ResponseEntity.ok(ApiResponseDTO.success(count));
    }

    @GetMapping("/count/with-tasks")
    @Operation(
            summary = "Get count of projects with tasks",
            description = "Returns the total number of projects that have at least one task"
    )
    @ApiResponse(responseCode = "200", description = "Project count retrieved successfully")
    public ResponseEntity<ApiResponseDTO<Long>> getProjectsWithTasksCount() {

        log.info("Getting count of projects with tasks");

        Long count = projectService.getProjectsWithTasksCount();

        log.info("Found {} projects with tasks", count);

        return ResponseEntity.ok(ApiResponseDTO.success(count));
    }
}