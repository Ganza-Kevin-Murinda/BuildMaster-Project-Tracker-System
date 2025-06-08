package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.Task;
import com.buildermaster.projecttracker.model.ETaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository interface for Task entity
 * Provides CRUD operations and custom query methods for Task management
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // ===== DERIVED QUERY METHODS =====

    /**
     * Find all tasks for a specific project
     * @param projectId the project ID
     * @return list of tasks belonging to the project
     */
    List<Task> findByProjectId(UUID projectId);

    /**
     * Find all tasks assigned to a specific developer
     * @param developerId the developer ID
     * @return list of tasks assigned to the developer
     */
    List<Task> findByDeveloperId(UUID developerId);

    /**
     * Find tasks by status
     * @param status the task status to filter by
     * @return list of tasks with the specified status
     */
    List<Task> findByStatus(ETaskStatus status);

    /**
     * Find tasks with due dates before the specified date (overdue tasks)
     * @param date the cutoff date
     * @return list of tasks due before the given date
     */
    List<Task> findByDueDateBefore(LocalDate date);

    /**
     * Find tasks with due dates between two dates (inclusive)
     * @param start the start date
     * @param end the end date
     * @return list of tasks with due dates in the specified range
     */
    List<Task> findByDueDateBetween(LocalDate start, LocalDate end);

    /**
     * Find tasks for a specific project with a specific status
     * @param projectId the project ID
     * @param status the task status
     * @return list of tasks matching both criteria
     */
    List<Task> findByProjectIdAndStatus(UUID projectId, ETaskStatus status);

    /**
     * Find tasks for a specific developer with a specific status
     * @param developerId the developer ID
     * @param status the task status
     * @return list of tasks matching both criteria
     */
    List<Task> findByDeveloperIdAndStatus(UUID developerId, ETaskStatus status);

    /**
     * Find tasks by title containing search term (case-insensitive)
     * @param title the search term for task title
     * @return list of tasks matching the search criteria
     */
    List<Task> findByTitleContainingIgnoreCase(String title);

    /**
     * Find tasks by description containing search term (case-insensitive)
     * @param description the search term for task description
     * @return list of tasks matching the search criteria
     */
    List<Task> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Count tasks by project
     * @param projectId the project ID
     * @return count of tasks in the project
     */
    Long countByProjectId(UUID projectId);

    /**
     * Count tasks by developer
     * @param developerId the developer ID
     * @return count of tasks assigned to the developer
     */
    Long countByDeveloperId(UUID developerId);

    // ===== CUSTOM @QUERY METHODS =====

    /**
     * Find overdue tasks that are not completed
     * Tasks are considered overdue if their due date has passed and status is not COMPLETED
     * @return list of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks();

    /**
     * Find tasks that are not assigned to any developer
     * @return list of unassigned tasks
     */
    @Query("SELECT t FROM Task t WHERE t.developer IS NULL")
    List<Task> findUnassignedTasks();

    /**
     * Count tasks by specific status
     * @param status the task status
     * @return count of tasks with the specified status
     */
    Long countByStatus(ETaskStatus status);

    /**
     * Count tasks grouped by status
     * @return map of status to count
     */
    @Query("SELECT t.status as status, COUNT(t) as count FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();

    /**
     * Find tasks created within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of tasks created within the specified range
     */
    @Query("SELECT t FROM Task t WHERE DATE(t.createdDate) BETWEEN :startDate AND :endDate")
    List<Task> findTasksCreatedBetween(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * Find tasks by project and due date range
     * @param projectId the project ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of tasks matching the criteria
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findByProjectIdAndDueDateBetween(@Param("projectId") UUID projectId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * Find tasks by developer and due date range
     * @param developerId the developer ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of tasks matching the criteria
     */
    @Query("SELECT t FROM Task t WHERE t.developer.id = :developerId AND t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findByDeveloperIdAndDueDateBetween(@Param("developerId") UUID developerId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Find tasks by project name (case-insensitive)
     * @param projectName the project name to search for
     * @return list of tasks belonging to projects with matching names
     */
    @Query("SELECT t FROM Task t JOIN t.project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :projectName, '%'))")
    List<Task> findByProjectName(@Param("projectName") String projectName);

    /**
     * Find tasks by developer name (case-insensitive)
     * @param developerName the developer name to search for
     * @return list of tasks assigned to developers with matching names
     */
    @Query("SELECT t FROM Task t JOIN t.developer d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :developerName, '%'))")
    List<Task> findByDeveloperName(@Param("developerName") String developerName);

    // ===== PAGINATION SUPPORT METHODS =====

    /**
     * Find tasks by project ID with pagination
     * @param projectId the project ID
     * @param pageable pagination information
     * @return page of tasks belonging to the project
     */
    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    /**
     * Find tasks by developer ID with pagination
     * @param developerId the developer ID
     * @param pageable pagination information
     * @return page of tasks assigned to the developer
     */
    Page<Task> findByDeveloperId(UUID developerId, Pageable pageable);

    /**
     * Find tasks by status with pagination
     * @param status the task status
     * @param pageable pagination information
     * @return page of tasks with the specified status
     */
    Page<Task> findByStatus(ETaskStatus status, Pageable pageable);

    /**
     * Find tasks by due date before with pagination
     * @param date the cutoff date
     * @param pageable pagination information
     * @return page of tasks due before the given date
     */
    Page<Task> findByDueDateBefore(LocalDate date, Pageable pageable);

    /**
     * Find tasks by due date after with pagination
     * @param date the cutoff date
     * @param pageable pagination information
     * @return page of tasks due after the given date
     */
    Page<Task> findByDueDateAfter(LocalDate date, Pageable pageable);

    /**
     * Find tasks by due date between with pagination
     * @param start the start date
     * @param end the end date
     * @param pageable pagination information
     * @return page of tasks with due dates in the specified range
     */
    Page<Task> findByDueDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    /**
     * Find tasks by project and status with pagination
     * @param projectId the project ID
     * @param status the task status
     * @param pageable pagination information
     * @return page of tasks matching both criteria
     */
    Page<Task> findByProjectIdAndStatus(UUID projectId, ETaskStatus status, Pageable pageable);

    /**
     * Find tasks by developer and status with pagination
     * @param developerId the developer ID
     * @param status the task status
     * @param pageable pagination information
     * @return page of tasks matching both criteria
     */
    Page<Task> findByDeveloperIdAndStatus(UUID developerId, ETaskStatus status, Pageable pageable);

    /**
     * Find tasks by title containing search term with pagination (case-insensitive)
     * @param title the search term for task title
     * @param pageable pagination information
     * @return page of tasks matching the search criteria
     */
    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Find tasks by description containing search term with pagination (case-insensitive)
     * @param description the search term for task description
     * @param pageable pagination information
     * @return page of tasks matching the search criteria
     */
    Page<Task> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * Find all tasks ordered by due date (earliest first)
     * @param pageable pagination information
     * @return page of tasks ordered by due date ascending
     */
    Page<Task> findAllByOrderByDueDateAsc(Pageable pageable);

    /**
     * Find all tasks ordered by creation date (newest first)
     * @param pageable pagination information
     * @return page of tasks ordered by creation date descending
     */
    Page<Task> findAllByOrderByCreatedDateDesc(Pageable pageable);

    // ===== ADDITIONAL USEFUL QUERY METHODS =====

    /**
     * Find tasks due today
     * @return list of tasks due today
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate = CURRENT_DATE")
    List<Task> findTasksDueToday();

    /**
     * Find tasks due this week
     * @return list of tasks due within the current week
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :now AND :nextWeek")
    List<Task> findTasksDueThisWeek(@Param("now") LocalDate now, @Param("nextWeek") LocalDate nextWeek);


    /**
     * Find recently created tasks (within last N days)
     * @param sinceDate number of days to look back
     * @return list of recently created tasks
     */
    @Query("SELECT t FROM Task t WHERE t.createdDate >= :sinceDate")
    List<Task> findRecentlyCreatedTasks(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find recently updated tasks (within last N days)
     * @param sinceDate number of days to look back
     * @return list of recently updated tasks
     */
    @Query("SELECT t FROM Task t WHERE t.updatedDate >= :sinceDate")
    List<Task> findRecentlyUpdatedTasks(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Get task completion statistics by project
     * @param projectId the project ID
     * @return array containing [total_tasks, completed_tasks, pending_tasks, in_progress_tasks]
     */
    @Query("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN t.status = 'TODO' THEN 1 ELSE 0 END) as pending, " +
            "SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress, " +
            "SUM(CASE WHEN t.status = 'BLOCKED' THEN 1 ELSE 0 END) as blocked " +
            "FROM Task t WHERE t.project.id = :projectId")
    List<Object[]> getTaskStatisticsByProject(@Param("projectId") UUID projectId);

    /**
     * Get task completion statistics by developer
     * @param developerId the developer ID
     * @return array containing [total_tasks, completed_tasks, pending_tasks, in_progress_tasks]
     */
    @Query("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN t.status = 'TODO' THEN 1 ELSE 0 END) as pending, " +
            "SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress, " +
            "SUM(CASE WHEN t.status = 'BLOCKED' THEN 1 ELSE 0 END) as blocked " +
            "FROM Task t WHERE t.developer.id = :developerId")
    List<Object[]> getTaskStatisticsByDeveloper(@Param("developerId") UUID developerId);
}
