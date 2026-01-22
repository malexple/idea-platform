package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {
    List<Division> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}
