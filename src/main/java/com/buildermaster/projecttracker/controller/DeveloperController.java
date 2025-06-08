package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.request.CreateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperResponseDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperStatsDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.service.DeveloperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing developers
 * Provides endpoints for CRUD operations and business-specific queries
 */
@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Developer Management", description = "APIs for managing developers and their information")
public class DeveloperController {

    private final DeveloperService developerService;

    // ===== CRUD ENDPOINTS =====

    @PostMapping
    @Operation(summary = "Create a new developer", description = "Creates a new developer with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Developer created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO<DeveloperResponseDTO>> createDeveloper(
            @Valid @RequestBody CreateDeveloperRequestDTO createRequest) {

        log.info("Creating new developer with email: {}", createRequest.getEmail());

        try {
            // Check email uniqueness
            if (developerService.existsByEmail(createRequest.getEmail())) {
                log.warn("Attempt to create developer with existing email: {}", createRequest.getEmail());
                return ResponseEntity.badRequest()
                        .body(ApiResponseDTO.error("Developer with this email already exists", 400));
            }

            DeveloperResponseDTO createdDeveloper = developerService.createDeveloper(createRequest);
            log.info("Successfully created developer with ID: {}", createdDeveloper.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.created(createdDeveloper));

        } catch (Exception e) {
            log.error("Error creating developer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to create developer", 500));
        }
    }

    @GetMapping
    @Operation(summary = "Get all developers", description = "Retrieves all developers with pagination and sorting support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO<Page<DeveloperResponseDTO>>> getAllDevelopers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (name, email, totalTaskCount)") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (ASC, DESC)") @RequestParam(defaultValue = "ASC") String sortDir) {

        log.info("Fetching developers - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDir);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<DeveloperResponseDTO> developers = developerService.getAllDevelopers(pageable);
            log.info("Successfully retrieved {} Developers", developers.getTotalElements());

            return ResponseEntity.ok(ApiResponseDTO.success("Developers retrieved successfully", developers));

        } catch (Exception e) {
            log.error("Error fetching developers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve developers", 500));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get developer by ID", description = "Retrieves a specific developer by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developer found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Developer not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO<DeveloperResponseDTO>> getDeveloperById(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID id) {

        log.info("Fetching developer with ID: {}", id);

        try {
            DeveloperResponseDTO developer = developerService.getDeveloperById(id);
            log.info("Successfully retrieved developer: {}", developer.getName());

            return ResponseEntity.ok(ApiResponseDTO.success(developer));

        } catch (Exception e) {
            log.error("Error fetching developer with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Developer not found", 404));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update developer", description = "Updates an existing developer's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developer updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Developer not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO<DeveloperResponseDTO>> updateDeveloper(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID id,
            @Valid @RequestBody UpdateDeveloperRequestDTO updateRequest) {

        log.info("Updating developer with ID: {}", id);

        try {
            // Check if developer exists
            DeveloperResponseDTO existingDeveloper = developerService.getDeveloperById(id);

            // Check email uniqueness if email is being changed
            if (!existingDeveloper.getEmail().equals(updateRequest.getEmail()) &&
                    developerService.existsByEmail(updateRequest.getEmail())) {
                log.warn("Attempt to update developer with existing email: {}", updateRequest.getEmail());
                return ResponseEntity.badRequest()
                        .body(ApiResponseDTO.error("Developer with this email already exists", 400));
            }

            DeveloperResponseDTO updatedDeveloper = developerService.updateDeveloper(id, updateRequest);
            log.info("Successfully updated developer: {}", updatedDeveloper.getName());

            return ResponseEntity.ok(ApiResponseDTO.success("Developer updated successfully", updatedDeveloper));

        } catch (Exception e) {
            log.error("Error updating developer with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Developer not found", 404));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete developer", description = "Deletes a developer by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developer deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Developer not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Developer has assigned tasks",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteDeveloper(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID id) {

        log.info("Deleting developer with ID: {}", id);

        try {
            // Check if developer exists and has tasks
            DeveloperResponseDTO developer = developerService.getDeveloperById(id);

            if (developer.getTotalTaskCount() > 0) {
                log.warn("Attempt to delete developer with assigned tasks: {}", id);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponseDTO.error("Cannot delete developer with assigned tasks. Please reassign tasks first.", 409));
            }

            developerService.deleteDeveloper(id);
            log.info("Successfully deleted developer with ID: {}", id);

            return ResponseEntity.ok(ApiResponseDTO.success("Developer deleted successfully", null));

        } catch (Exception e) {
            log.error("Error deleting developer with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Developer not found", 404));
        }
    }

    // ===== SPECIALIZED ENDPOINTS =====

    @GetMapping("/email/{email}")
    @Operation(summary = "Find developer by email", description = "Retrieves a developer by their email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developer found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Developer not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO<DeveloperResponseDTO>> getDeveloperByEmail(
            @Parameter(description = "Developer email address") @PathVariable String email) {

        log.info("Fetching developer with email: {}", email);

        try {
            DeveloperResponseDTO developer = developerService.getDeveloperByEmail(email);
            log.info("Successfully retrieved developer by email: {}", developer.getName());

            return ResponseEntity.ok(ApiResponseDTO.success(developer));

        } catch (Exception e) {
            log.error("Error fetching developer with email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Developer not found", 404));
        }
    }

    @GetMapping("/top-performers")
    @Operation(summary = "Get top performing developers", description = "Retrieves the top 5 developers by task count")
    @ApiResponse(responseCode = "200", description = "Top performers retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    public ResponseEntity<ApiResponseDTO<List<DeveloperSummaryDTO>>> getTopPerformers() {

        log.info("Fetching top 5 developers by task count");

        try {
            List<DeveloperSummaryDTO> topDevelopers = developerService.getTop5DevelopersByTaskCount();
            log.info("Successfully retrieved {} top performing developers", topDevelopers.size());

            return ResponseEntity.ok(ApiResponseDTO.success("Top performers retrieved successfully", topDevelopers));

        } catch (Exception e) {
            log.error("Error fetching top performers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve top performers", 500));
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Get available developers", description = "Retrieves developers without assigned tasks")
    @ApiResponse(responseCode = "200", description = "Available developers retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    public ResponseEntity<ApiResponseDTO<List<DeveloperSummaryDTO>>> getAvailableDevelopers() {

        log.info("Fetching developers without tasks");

        try {
            List<DeveloperSummaryDTO> availableDevelopers = developerService.getDevelopersWithoutTasks();
            log.info("Successfully retrieved {} available developers", availableDevelopers.size());

            return ResponseEntity.ok(ApiResponseDTO.success("Available developers retrieved successfully", availableDevelopers));

        } catch (Exception e) {
            log.error("Error fetching available developers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve available developers", 500));
        }
    }

    @GetMapping("/by-task-status/{status}")
    @Operation(summary = "Get developers by task status", description = "Retrieves developers who have tasks with the specified status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Developers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid task status",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO<List<DeveloperSummaryDTO>>> getDevelopersByTaskStatus(
            @Parameter(description = "Task status filter") @PathVariable ETaskStatus status) {

        log.info("Fetching developers by task status: {}", status);

        try {
            List<DeveloperSummaryDTO> developers = developerService.getDevelopersByTaskStatus(status);
            log.info("Successfully retrieved {} developers with task status: {}", developers.size(), status);

            return ResponseEntity.ok(ApiResponseDTO.success("Developers retrieved successfully", developers));

        } catch (Exception e) {
            log.error("Error fetching developers by task status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve developers by task status", 500));
        }
    }

    @GetMapping("/search/name")
    @Operation(summary = "Search developers by name", description = "Searches for developers by name with pagination support")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    public ResponseEntity<ApiResponseDTO<Page<DeveloperResponseDTO>>> searchDevelopersByName(
            @Parameter(description = "Search term for developer name") @RequestParam String name,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {

        log.info("Searching developers by name: '{}' - page: {}, size: {}", name, page, size);

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDir);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<DeveloperResponseDTO> searchResults = developerService.searchDevelopersByName(name, pageable);
            log.info("Found {} developers matching name search: '{}'", searchResults.getTotalElements(), name);

            return ResponseEntity.ok(ApiResponseDTO.success("Search completed successfully", searchResults));

        } catch (Exception e) {
            log.error("Error searching developers by name '{}': {}", name, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Search failed", 500));
        }
    }

    @GetMapping("/search/skill")
    @Operation(summary = "Search developers by skill", description = "Searches for developers by their skills with pagination support")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    public ResponseEntity<ApiResponseDTO<Page<DeveloperResponseDTO>>> searchDevelopersBySkill(
            @Parameter(description = "Search term for developer skills") @RequestParam String skill,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {

        log.info("Searching developers by skill: '{}' - page: {}, size: {}", skill, page, size);

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDir);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<DeveloperResponseDTO> searchResults = developerService.searchDevelopersBySkill(skill, pageable);
            log.info("Found {} developers matching skill search: '{}'", searchResults.getTotalElements(), skill);

            return ResponseEntity.ok(ApiResponseDTO.success("Search completed successfully", searchResults));

        } catch (Exception e) {
            log.error("Error searching developers by skill '{}': {}", skill, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Search failed", 500));
        }
    }

    // ===== UTILITY ENDPOINTS =====

    @GetMapping("/stats")
    @Operation(summary = "Get developer statistics", description = "Retrieves statistics about developers in the system")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    public ResponseEntity<ApiResponseDTO<DeveloperStatsDTO>> getDeveloperStats() {

        log.info("Fetching developer statistics");

        try {
            Long totalCount = developerService.getTotalDeveloperCount();
            Long withTasksCount = developerService.getDeveloperCountWithTasks();
            Long availableCount = totalCount - withTasksCount;

            DeveloperStatsDTO stats = DeveloperStatsDTO.builder()
                    .totalDevelopers(totalCount)
                    .developersWithTasks(withTasksCount)
                    .availableDevelopers(availableCount)
                    .build();

            log.info("Successfully retrieved developer statistics");
            return ResponseEntity.ok(ApiResponseDTO.success("Statistics retrieved successfully", stats));

        } catch (Exception e) {
            log.error("Error fetching developer statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve statistics", 500));
        }
    }

}