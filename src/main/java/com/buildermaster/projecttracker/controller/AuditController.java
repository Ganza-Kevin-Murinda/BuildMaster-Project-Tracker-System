package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.AuditLogResponseDTO;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Audit Management", description = "APIs for managing and retrieving audit logs")
public class AuditController {

    private final AuditService auditService;

    @Operation(
            summary = "Get entity audit trail",
            description = "Retrieve paginated audit trail for a specific entity"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit trail retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getEntityAuditTrail(
            @Parameter(description = "Type of entity", example = "Project")
            @PathVariable String entityType,

            @Parameter(description = "Entity unique identifier")
            @PathVariable UUID entityId,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving audit trail for entity: {} with ID: {}", entityType, entityId);

        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<AuditLogResponseDTO> auditTrail = auditService.getAuditTrail(entityType, entityId, pageable);

            return ResponseEntity.ok(ApiResponseDTO.success(
                    String.format("Retrieved %d audit records for %s", auditTrail.getTotalElements(), entityType),
                    auditTrail
            ));
        } catch (Exception e) {
            log.error("Error retrieving audit trail for entity {} with ID {}: {}", entityType, entityId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve audit trail", 500));
        }
    }

    @Operation(
            summary = "Get user action history",
            description = "Retrieve paginated action history for a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User actions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/user/{actorName}")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getUserActions(
            @Parameter(description = "Username or actor name", example = "admin@example.com")
            @PathVariable String actorName,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving actions for user: {}", actorName);

        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<AuditLogResponseDTO> userActions = auditService.getUserActions(actorName, pageable);

            return ResponseEntity.ok(ApiResponseDTO.success(
                    String.format("Retrieved %d actions for user %s", userActions.getTotalElements(), actorName),
                    userActions
            ));
        } catch (Exception e) {
            log.error("Error retrieving actions for user {}: {}", actorName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve user actions", 500));
        }
    }

    @Operation(
            summary = "Get actions by type",
            description = "Retrieve paginated audit logs filtered by action type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid action type")
    })
    @GetMapping("/action/{actionType}")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getActionsByType(
            @Parameter(description = "Type of action", example = "CREATE")
            @PathVariable EActionType actionType,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving actions by type: {}", actionType);

        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<AuditLogResponseDTO> actions = auditService.getActionsByType(actionType, pageable);

            return ResponseEntity.ok(ApiResponseDTO.success(
                    String.format("Retrieved %d %s actions", actions.getTotalElements(), actionType),
                    actions
            ));
        } catch (Exception e) {
            log.error("Error retrieving actions by type {}: {}", actionType, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve actions by type", 500));
        }
    }

    @Operation(
            summary = "Get audits by date range",
            description = "Retrieve paginated audit logs within a specified date range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audits retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getAuditsByDateRange(
            @Parameter(description = "Start date and time", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,

            @Parameter(description = "End date and time", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving audits from {} to {}", start, end);

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Start date cannot be after end date", 400));
        }

        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<AuditLogResponseDTO> audits = auditService.getAuditsByDateRange(start, end, pageable);

            return ResponseEntity.ok(ApiResponseDTO.success(
                    String.format("Retrieved %d audits from %s to %s", audits.getTotalElements(), start, end),
                    audits
            ));
        } catch (Exception e) {
            log.error("Error retrieving audits by date range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve audits by date range", 500));
        }
    }

    @Operation(
            summary = "Get recent audit activities",
            description = "Retrieve recent audit activities with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent audits retrieved successfully")
    })
    @GetMapping("/recent")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getRecentAudits(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving recent audit activities");

        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<AuditLogResponseDTO> recentAudits = auditService.getAllAudits(pageable);

            return ResponseEntity.ok(ApiResponseDTO.success(
                    String.format("Retrieved %d recent audit activities", recentAudits.getTotalElements()),
                    recentAudits
            ));
        } catch (Exception e) {
            log.error("Error retrieving recent audits: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve recent audits", 500));
        }
    }
}
