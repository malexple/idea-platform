package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByIdeaIdOrderByCreatedAtAsc(Long ideaId);
}
