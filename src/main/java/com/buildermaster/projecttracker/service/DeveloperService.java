package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.request.CreateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperResponseDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for developer business operations
 * Provides methods for managing developers and their associated data
 */
public interface DeveloperService {

    // ===== CRUD OPERATIONS =====

    /**
     * Create a new developer
     * @param createRequest the developer creation request DTO
     * @return the created developer response DTO
     */
    DeveloperResponseDTO createDeveloper(CreateDeveloperRequestDTO createRequest);

    /**
     * Get developer by ID
     * @param developerId the unique identifier of the developer
     * @return the developer response DTO
     */
    DeveloperResponseDTO getDeveloperById(UUID developerId);

    /**
     * Get all developers (cached)
     * @return list of all developer response DTOs
     */
    List<DeveloperResponseDTO> getAllDevelopers();

    /**
     * Get all developers with pagination
     * @param pageable pagination information
     * @return page of developer response DTOs
     */
    Page<DeveloperResponseDTO> getAllDevelopers(Pageable pageable);

    /**
     * Update an existing developer
     * @param developerId the unique identifier of the developer
     * @param updateRequest the developer update request DTO
     * @return the updated developer response DTO
     */
    DeveloperResponseDTO updateDeveloper(UUID developerId, UpdateDeveloperRequestDTO updateRequest);

    /**
     * Delete a developer by ID
     * @param developerId the unique identifier of the developer
     */
    void deleteDeveloper(UUID developerId);

    // ===== BUSINESS OPERATIONS =====

    /**
     * Get developer by email address (unique lookup)
     * @param email the email address to search for
     * @return the developer response DTO
     */
    DeveloperResponseDTO getDeveloperByEmail(String email);

    /**
     * Get top 5 developers by task count (performance metrics)
     * @return list of top 5 developers ordered by task count
     */
    List<DeveloperSummaryDTO> getTop5DevelopersByTaskCount();

    /**
     * Get developers without tasks (resource allocation insights)
     * @return list of developers who have no assigned tasks
     */
    List<DeveloperSummaryDTO> getDevelopersWithoutTasks();

    /**
     * Get developers by task status (workload analysis)
     * @param taskStatus the task status to filter by
     * @return list of developers who have tasks with the specified status
     */
    List<DeveloperSummaryDTO> getDevelopersByTaskStatus(ETaskStatus taskStatus);

    /**
     * Search developers by name with pagination
     * @param name the search term for developer name
     * @param pageable pagination information
     * @return page of developers matching the search criteria
     */
    Page<DeveloperResponseDTO> searchDevelopersByName(String name, Pageable pageable);

    /**
     * Search developers by skill with pagination
     * @param skill the search term for developer skills
     * @param pageable pagination information
     * @return page of developers with matching skills
     */
    Page<DeveloperResponseDTO> searchDevelopersBySkill(String skill, Pageable pageable);

    // ===== UTILITY METHODS =====

    /**
     * Check if a developer exists with the given email
     * @param email the email address to check
     * @return true if developer exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Get total count of developers
     * @return total number of developers in the system
     */
    Long getTotalDeveloperCount();

    /**
     * Get count of developers with tasks
     * @return number of developers that have at least one task
     */
    Long getDeveloperCountWithTasks();
}