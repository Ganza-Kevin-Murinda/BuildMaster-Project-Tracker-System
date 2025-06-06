package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.Developer;
import com.buildermaster.projecttracker.model.ETaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Developer entity
 * Provides CRUD operations and custom query methods for Developer management
 */
@Repository
public interface DeveloperRepository extends JpaRepository<Developer, UUID> {

    // ===== DERIVED QUERY METHODS =====

    /**
     * Find developer by email address
     * @param email the email to search for
     * @return Optional containing the developer if found
     */
    Optional<Developer> findByEmail(String email);

    /**
     * Find developers by name containing search term (case-insensitive)
     * @param name the search term for developer name
     * @return list of developers matching the search criteria
     */
    List<Developer> findByNameContainingIgnoreCase(String name);

    /**
     * Find developers by skills containing search term (case-insensitive)
     * @param skill the search term for developer skills
     * @return list of developers with matching skills
     */
    List<Developer> findBySkillsContainingIgnoreCase(String skill);

    /**
     * Check if a developer exists with the given email
     * @param email the email to check
     * @return true if developer exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find developers by exact name match (case-insensitive)
     * @param name the exact name to search for
     * @return list of developers with the exact name
     */
    List<Developer> findByNameIgnoreCase(String name);

    // ===== CUSTOM @QUERY METHODS =====

    /**
     * Find top 5 developers by task count (most productive developers)
     * @param pageable pagination information (should be limited to 5)
     * @return page of developers ordered by task count descending
     */
    @Query("SELECT d FROM Developer d LEFT JOIN d.tasks t GROUP BY d ORDER BY COUNT(t) DESC")
    Page<Developer> findTop5DevelopersByTaskCount(Pageable pageable);

    /**
     * Find developers who have no assigned tasks
     * @return list of developers without any tasks
     */
    @Query("SELECT d FROM Developer d WHERE d.tasks IS EMPTY")
    List<Developer> findDevelopersWithoutTasks();

    /**
     * Find developers who have tasks with specific status
     * @param status the task status to filter by
     * @return list of developers who have tasks with the specified status
     */
    @Query("SELECT DISTINCT d FROM Developer d JOIN d.tasks t WHERE t.status = :status")
    List<Developer> findDevelopersByTaskStatus(@Param("status") ETaskStatus status);

    /**
     * Get developer count by task count range
     * @param minTasks minimum number of tasks
     * @param maxTasks maximum number of tasks
     * @return count of developers within the task count range
     */
    @Query("SELECT COUNT(DISTINCT d) FROM Developer d LEFT JOIN d.tasks t GROUP BY d HAVING COUNT(t) BETWEEN :minTasks AND :maxTasks")
    Long countDevelopersByTaskCountRange(@Param("minTasks") int minTasks, @Param("maxTasks") int maxTasks);

    /**
     * Find developers created within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of developers created within the specified range
     */
    @Query("SELECT d FROM Developer d WHERE d.createdDate BETWEEN :startDate AND :endDate")
    List<Developer> findDevelopersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Count total developers with tasks
     * @return count of developers that have at least one task
     */
    @Query("SELECT COUNT(DISTINCT d) FROM Developer d JOIN d.tasks t")
    Long countDevelopersWithTasks();


    // ===== PAGINATION SUPPORT METHODS =====

    /**
     * Find developers by name containing search term with pagination (case-insensitive)
     * @param name the search term for developer name
     * @param pageable pagination information
     * @return page of developers matching the search criteria
     */
    Page<Developer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find developers by skills containing search term with pagination (case-insensitive)
     * @param skill the search term for developer skills
     * @param pageable pagination information
     * @return page of developers with matching skills
     */
    Page<Developer> findBySkillsContainingIgnoreCase(String skill, Pageable pageable);

    /**
     * Find all developers ordered by creation date with pagination (newest first)
     * @param pageable pagination information
     * @return page of developers ordered by creation date descending
     */
    Page<Developer> findAllByOrderByCreatedDateDesc(Pageable pageable);

    /**
     * Find all developers ordered by update date with pagination (newest first)
     * @param pageable pagination information
     * @return page of developers ordered by update date descending
     */
    Page<Developer> findAllByOrderByUpdatedDateDesc(Pageable pageable);

    /**
     * Find all developers ordered by name with pagination (alphabetical)
     * @param pageable pagination information
     * @return page of developers ordered by name ascending
     */
    Page<Developer> findAllByOrderByNameAsc(Pageable pageable);

}