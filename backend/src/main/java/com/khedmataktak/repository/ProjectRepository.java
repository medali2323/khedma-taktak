package com.khedmataktak.repository;

import com.khedmataktak.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByProfileIdOrderBySortOrderAsc(Long profileId);

    Optional<Project> findByIdAndProfileId(Long id, Long profileId);
}
