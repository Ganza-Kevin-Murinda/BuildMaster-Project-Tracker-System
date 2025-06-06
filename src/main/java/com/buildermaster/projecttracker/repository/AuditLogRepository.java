package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.audit.AuditLog;
import com.buildermaster.projecttracker.model.EActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AuditLog MongoDB document
 * Provides CRUD operations and custom query methods for audit log management
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    // ===== DERIVED QUERY METHODS =====

    /**
     * Find audit logs for a specific entity
     * @param entityType the type of entity (e.g., "Project", "Task", "Developer")
     * @param entityId the unique identifier of the entity
     * @return list of audit logs for the specified entity
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    /**
     * Find audit logs for a specific entity with pagination
     * @param entityType the type of entity
     * @param entityId the unique identifier of the entity
     * @param pageable pagination information
     * @return page of audit logs for the specified entity
     */
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    /**
     * Find user actions ordered by timestamp (newest first)
     * @param actorName the name of the user who performed actions
     * @return list of audit logs for the specified user, ordered by timestamp descending
     */
    List<AuditLog> findByActorNameOrderByTimestampDesc(String actorName);

    /**
     * Find user actions with pagination, ordered by timestamp (newest first)
     * @param actorName the name of the user who performed actions
     * @param pageable pagination information
     * @return page of audit logs for the specified user, ordered by timestamp descending
     */
    Page<AuditLog> findByActorNameOrderByTimestampDesc(String actorName, Pageable pageable);

    /**
     * Find audit logs by action type
     * @param actionType the type of action performed
     * @return list of audit logs with the specified action type
     */
    List<AuditLog> findByActionType(EActionType actionType);

    /**
     * Find audit logs by entity type
     * @param entityType the type of entity
     * @return list of audit logs for the specified entity type
     */
    List<AuditLog> findByEntityType(String entityType);

    /**
     * Find audit logs within a timestamp range
     * @param start the start timestamp
     * @param end the end timestamp
     * @return list of audit logs within the specified time range
     */
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // ===== CUSTOM @QUERY METHODS WITH MONGODB SYNTAX =====

    /**
     * Find audit logs by entity type and action type using MongoDB query
     * @param entityType the type of entity
     * @param actionType the type of action
     * @return list of audit logs matching both criteria
     */
    @Query("{ 'entityType': ?0, 'actionType': ?1 }")
    List<AuditLog> findByEntityTypeAndActionType(String entityType, EActionType actionType);


    /**
     * Find audit logs by actor and entity type
     * @param actorName the name of the user
     * @param entityType the type of entity
     * @return list of audit logs matching both criteria
     */
    @Query("{ 'actorName': ?0, 'entityType': ?1 }")
    List<AuditLog> findByActorNameAndEntityType(String actorName, String entityType);

    /**
     * Find audit logs by actor and action type
     * @param actorName the name of the user
     * @param actionType the type of action
     * @return list of audit logs matching both criteria
     */
    @Query("{ 'actorName': ?0, 'actionType': ?1 }")
    List<AuditLog> findByActorNameAndActionType(String actorName, EActionType actionType);

    /**
     * Find recent audit logs (within last N hours)
     * @param hoursAgo number of hours to look back from current time
     * @return list of recent audit logs
     */
    @Query("{ 'timestamp': { $gte: ?0 } }")
    List<AuditLog> findRecentAuditLogs(LocalDateTime hoursAgo);


    // ===== ADDITIONAL USEFUL QUERY METHODS =====

    /**
     * Find all audit logs ordered by timestamp (newest first)
     * @param pageable pagination information
     * @return page of audit logs ordered by timestamp descending
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find audit logs by actor name (case-insensitive)
     * @param actorName the actor name to search for
     * @return list of audit logs for the specified actor
     */
    List<AuditLog> findByActorNameIgnoreCase(String actorName);

    /**
     * Count audit logs by entity type
     * @param entityType the type of entity
     * @return count of audit logs for the specified entity type
     */
    Long countByEntityType(String entityType);

    /**
     * Count audit logs by action type
     * @param actionType the type of action
     * @return count of audit logs for the specified action type
     */
    Long countByActionType(EActionType actionType);

    /**
     * Count audit logs by actor
     * @param actorName the name of the actor
     * @return count of audit logs for the specified actor
     */
    Long countByActorName(String actorName);

    /**
     * Find audit logs with specific payload field value
     * @param fieldName the name of the payload field
     * @param fieldValue the value to search for
     * @return list of audit logs containing the specified payload field and value
     */
    @Query("{ 'payload.?0': ?1 }")
    List<AuditLog> findByPayloadField(String fieldName, Object fieldValue);

    /**
     * Find audit logs by timestamp after (newer than)
     * @param timestamp the cutoff timestamp
     * @return list of audit logs newer than the specified timestamp
     */
    List<AuditLog> findByTimestampAfter(LocalDateTime timestamp);

    /**
     * Find audit logs by timestamp before (older than)
     * @param timestamp the cutoff timestamp
     * @return list of audit logs older than the specified timestamp
     */
    List<AuditLog> findByTimestampBefore(LocalDateTime timestamp);

    /**
     * Delete audit logs older than specified timestamp (for cleanup)
     * @param timestamp the cutoff timestamp
     * @return number of deleted documents
     */
    Long deleteByTimestampBefore(LocalDateTime timestamp);
}