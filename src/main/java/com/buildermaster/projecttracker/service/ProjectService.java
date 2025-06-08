package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.request.CreateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.response.ProjectResponseDTO;
import com.buildermaster.projecttracker.dto.response.ProjectSummaryDTO;
import com.buildermaster.projecttracker.model.EProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Project operations
 * Provides business logic methods for project management
 */
public interface ProjectService {

    // ===== CRUD OPERATIONS =====

    /**
     * Create a new project
     * @param createRequest the project creation request
     * @return the created project response DTO
     */
    ProjectResponseDTO createProject(CreateProjectRequestDTO createRequest);

    /**
     * Get project by ID
     * @param id the project ID
     * @return the project response DTO
     */
    ProjectResponseDTO getProjectById(UUID id);

    /**
     * Get all projects
     * @return list of all projects
     */
    List<ProjectResponseDTO> getAllProjects();

    /**
     * Get all projects with pagination
     * @param pageable pagination information
     * @return page of project summary DTOs
     */
    Page<ProjectSummaryDTO> getAllProjects(Pageable pageable);

    /**
     * Update an existing project
     * @param id the project ID
     * @param updateRequest the project update request
     * @return the updated project response DTO
     */
    ProjectResponseDTO updateProject(UUID id, UpdateProjectRequestDTO updateRequest);

    /**
     * Delete a project by ID
     * @param id the project ID
     */
    void deleteProject(UUID id);

    // ===== BUSINESS OPERATIONS =====

    /**
     * Get projects by status
     * @param status the project status
     * @return list of projects with the specified status
     */
    List<ProjectResponseDTO> getProjectsByStatus(EProjectStatus status);

    /**
     * Get projects by status with pagination
     * @param status the project status
     * @param pageable pagination information
     * @return page of projects with the specified status
     */
    Page<ProjectSummaryDTO> getProjectsByStatus(EProjectStatus status, Pageable pageable);

    /**
     * Get overdue projects
     * @return list of overdue projects
     */
    List<ProjectResponseDTO> getOverdueProjects();

    /**
     * Get projects without tasks
     * @return list of projects that have no tasks
     */
    List<ProjectResponseDTO> getProjectsWithoutTasks();

    /**
     * Get projects ordered by task count with pagination
     * @param pageable pagination information
     * @return page of projects ordered by task count (highest first)
     */
    Page<ProjectSummaryDTO> getProjectsOrderedByTaskCount(Pageable pageable);

    /**
     * Get projects by deadline range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of projects with deadlines in the specified range
     */
    List<ProjectResponseDTO> getProjectsByDeadlineRange(LocalDate startDate, LocalDate endDate);

    /**
     * Search projects by name with pagination
     * @param name the search term for project name
     * @param pageable pagination information
     * @return page of projects matching the search criteria
     */
    Page<ProjectSummaryDTO> searchProjectsByName(String name, Pageable pageable);

    /**
     * Search projects by description with pagination
     * @param description the search term for project description
     * @param pageable pagination information
     * @return page of projects matching the search criteria
     */
    Page<ProjectSummaryDTO> searchProjectsByDescription(String description, Pageable pageable);

    /**
     * Get projects with deadlines before a specific date with pagination
     * @param date the cutoff date
     * @param pageable pagination information
     * @return page of projects with deadlines before the given date
     */
    Page<ProjectSummaryDTO> getProjectsWithDeadlineBefore(LocalDate date, Pageable pageable);

    /**
     * Get projects with deadlines after a specific date with pagination
     * @param date the cutoff date
     * @param pageable pagination information
     * @return page of projects with deadlines after the given date
     */
    Page<ProjectSummaryDTO> getProjectsWithDeadlineAfter(LocalDate date, Pageable pageable);

    /**
     * Get most recently created projects with pagination
     * @param pageable pagination information
     * @return page of projects ordered by creation date (newest first)
     */
    Page<ProjectSummaryDTO> getRecentlyCreatedProjects(Pageable pageable);

    /**
     * Get most recently updated projects with pagination
     * @param pageable pagination information
     * @return page of projects ordered by update date (newest first)
     */
    Page<ProjectSummaryDTO> getRecentlyUpdatedProjects(Pageable pageable);

    // ===== UTILITY METHODS =====

    /**
     * Check if a project exists by name
     * @param name the project name
     * @return true if project exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Get project count by status
     * @param status the project status
     * @return count of projects with the specified status
     */
    Long getProjectCountByStatus(EProjectStatus status);

    /**
     * Get count of projects with tasks
     * @return count of projects that have at least one task
     */
    Long getProjectsWithTasksCount();
}
