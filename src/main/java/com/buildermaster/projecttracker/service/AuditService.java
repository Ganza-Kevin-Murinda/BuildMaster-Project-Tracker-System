package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.response.AuditLogResponseDTO;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.model.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for audit operations
 * Provides methods for logging actions and retrieving audit trails
 */
public interface AuditService {

    /**
     * Log an action performed on an entity
     * @param actionType the type of action performed
     * @param entityType the type of entity affected
     * @param entityId the unique identifier of the entity
     * @param actor the name of the user who performed the action
     * @param entity the entity object (for payload creation)
     * @return the created audit log
     */
    AuditLog logAction(EActionType actionType, String entityType, UUID entityId, String actor, Object entity);

    /**
     * Get audit trail for a specific entity
     * @param entityType the type of entity
     * @param entityId the unique identifier of the entity
     * @return list of audit logs for the entity
     */
    List<AuditLogResponseDTO> getAuditTrail(String entityType, UUID entityId);

    /**
     * Get paginated audit trail for a specific entity
     * @param entityType the type of entity
     * @param entityId the unique identifier of the entity
     * @param pageable pagination information
     * @return page of audit logs for the entity
     */
    Page<AuditLogResponseDTO> getAuditTrail(String entityType, UUID entityId, Pageable pageable);

    /**
     * Get paginated user actions
     * @param actorName the name of the user
     * @param pageable pagination information
     * @return page of audit logs for the user
     */
    Page<AuditLogResponseDTO> getUserActions(String actorName, Pageable pageable);

    /**
     * Get paginated actions by type
     * @param actionType the type of action
     * @param pageable pagination information
     * @return page of audit logs with the specified action type
     */
    Page<AuditLogResponseDTO> getActionsByType(EActionType actionType, Pageable pageable);

    /**
     * Get paginated audit logs within a date range
     * @param start the start timestamp
     * @param end the end timestamp
     * @param pageable pagination information
     * @return page of audit logs within the specified date range
     */
    Page<AuditLogResponseDTO> getAuditsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Get all audit logs with pagination
     * @param pageable pagination information
     * @return page of all audit logs ordered by timestamp (newest first)
     */
    Page<AuditLogResponseDTO> getAllAudits(Pageable pageable);

    /**
     * Get audit statistics for an entity type
     * @param entityType the type of entity
     * @return count of audit logs for the entity type
     */
    Long getAuditCountByEntityType(String entityType);

    /**
     * Get audit statistics for an action type
     * @param actionType the type of action
     * @return count of audit logs for the action type
     */
    Long getAuditCountByActionType(EActionType actionType);

    /**
     * Get audit statistics for a user
     * @param actorName the name of the user
     * @return count of audit logs for the user
     */
    Long getAuditCountByActor(String actorName);

    /**
     * Clean up old audit logs
     * @param cutoffDate the date before which logs should be deleted
     * @return number of deleted audit logs
     */
    Long cleanupOldAudits(LocalDateTime cutoffDate);
}
