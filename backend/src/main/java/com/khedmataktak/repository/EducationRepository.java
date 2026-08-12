package com.khedmataktak.repository;

import com.khedmataktak.entity.Education;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByProfileIdOrderBySortOrderAscStartDateDesc(Long profileId);

    Optional<Education> findByIdAndProfileId(Long id, Long profileId);
}
