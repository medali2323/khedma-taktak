package com.khedmataktak.repository;

import com.khedmataktak.entity.Experience;
import com.khedmataktak.entity.Profile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByProfileIdOrderBySortOrderAscStartDateDesc(Long profileId);

    Optional<Experience> findByIdAndProfileId(Long id, Long profileId);
}
