package com.khedmataktak.repository;

import com.khedmataktak.entity.Certification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

    List<Certification> findByProfileIdOrderBySortOrderAscIssueDateDesc(Long profileId);

    Optional<Certification> findByIdAndProfileId(Long id, Long profileId);
}
