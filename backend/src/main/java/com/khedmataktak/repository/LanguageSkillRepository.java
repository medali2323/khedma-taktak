package com.khedmataktak.repository;

import com.khedmataktak.entity.LanguageSkill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageSkillRepository extends JpaRepository<LanguageSkill, Long> {

    List<LanguageSkill> findByProfileIdOrderBySortOrderAsc(Long profileId);

    Optional<LanguageSkill> findByIdAndProfileId(Long id, Long profileId);
}
