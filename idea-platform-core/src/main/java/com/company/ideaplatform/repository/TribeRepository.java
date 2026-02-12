package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.Tribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TribeRepository extends JpaRepository<Tribe, Long> {
    List<Tribe> findByDivisionIdAndActiveTrue(Long divisionId);
    List<Tribe> findByActiveTrue();
}
