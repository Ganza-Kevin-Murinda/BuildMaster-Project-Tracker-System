package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.Project;
import com.buildermaster.projecttracker.model.EProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Project entity
 * Provides CRUD operations and custom query methods for Project management
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // ===== DERIVED QUERY METHODS =====

    /**
     * Find projects by status
     * @param status the project status to filter by
     * @return list of projects with the specified status
     */
    List<Project> findByStatus(EProjectStatus status);

    /**
     * Find projects with deadlines before the specified date
     * @param date the cutoff date
     * @return list of projects with deadlines before the given date
     */
    List<Project> findByDeadlineBefore(LocalDate date);

    /**
     * Find projects with deadlines between two dates (inclusive)
     * @param start the start date
     * @param end the end date
     * @return list of projects with deadlines in the specified range
     */
    List<Project> findByDeadlineBetween(LocalDate start, LocalDate end);

    /**
     * Check if a project exists by name (case-insensitive)
     * @param name the project name to check
     * @return true if project exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find projects by name and status
     * @param name the project name
     * @param status the project status
     * @return list of projects matching both criteria
     */
    List<Project> findByNameAndStatus(String name, EProjectStatus status);

    // ===== CUSTOM @QUERY METHODS =====

    /**
     * Find overdue projects that are not completed
     * Projects are considered overdue if their deadline has passed and status is not COMPLETED
     * @return list of overdue projects
     */
    @Query("SELECT p FROM Project p WHERE p.deadline < CURRENT_DATE AND p.status != 'COMPLETED'")
    List<Project> findOverdueProjects();

    /**
     * Find projects that have no tasks assigned
     * @return list of projects without any tasks
     */
    @Query("SELECT p FROM Project p WHERE p.tasks IS EMPTY")
    List<Project> findProjectsWithoutTasks();

    /**
     * Find projects ordered by task count in descending order
     * @param pageable pagination information
     * @return page of projects ordered by task count (highest first)
     */
    @Query("SELECT p FROM Project p LEFT JOIN p.tasks t GROUP BY p ORDER BY COUNT(t) DESC")
    Page<Project> findProjectsOrderedByTaskCount(Pageable pageable);

    /**
     * Get project count by status
     * @param status the project status
     * @return count of projects with the specified status
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    Long countByStatus(@Param("status") EProjectStatus status);

    /**
     * Find projects created within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of projects created within the specified range
     */
    @Query("SELECT p FROM Project p WHERE DATE(p.createdDate) BETWEEN :startDate AND :endDate")
    List<Project> findProjectsCreatedBetween(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * Count total projects with tasks
     * @return count of projects that have at least one task
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Project p JOIN p.tasks t")
    Long countProjectsWithTasks();

    // ===== PAGINATION SUPPORT METHODS =====

    /**
     * Find projects by status with pagination
     * @param status the project status to filter by
     * @param pageable pagination information
     * @return page of projects with the specified status
     */
    Page<Project> findByStatus(EProjectStatus status, Pageable pageable);

    /**
     * Find projects by name containing the search term (case-insensitive) with pagination
     * @param name the search term for project name
     * @param pageable pagination information
     * @return page of projects matching the search criteria
     */
    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find projects by description containing the search term (case-insensitive) with pagination
     * @param description the search term for project description
     * @param pageable pagination information
     * @return page of projects matching the search criteria
     */
    Page<Project> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * Find projects with deadlines before a specific date with pagination
     * @param date the cutoff date
     * @param pageable pagination information
     * @return page of projects with deadlines before the given date
     */
    Page<Project> findByDeadlineBefore(LocalDate date, Pageable pageable);

    /**
     * Find projects with deadlines after a specific date with pagination
     * @param date the cutoff date
     * @param pageable pagination information
     * @return page of projects with deadlines after the given date
     */
    Page<Project> findByDeadlineAfter(LocalDate date, Pageable pageable);

    /**
     * Find the most recently created projects
     * @param pageable pagination information
     * @return page of projects ordered by creation date (newest first)
     */
    Page<Project> findAllByOrderByCreatedDateDesc(Pageable pageable);

    /**
     * Find the most recently updated projects
     * @param pageable pagination information
     * @return page of projects ordered by update date (newest first)
     */
    Page<Project> findAllByOrderByUpdatedDateDesc(Pageable pageable);

}