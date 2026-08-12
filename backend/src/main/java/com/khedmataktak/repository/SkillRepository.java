package com.khedmataktak.repository;

import com.khedmataktak.entity.Skill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByProfileIdOrderBySortOrderAsc(Long profileId);

    Optional<Skill> findByIdAndProfileId(Long id, Long profileId);
}
