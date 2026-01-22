package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.ReviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {
    List<ReviewAssignment> findByIdeaId(Long ideaId);
    List<ReviewAssignment> findByReviewerIdAndReviewedAtIsNull(Long reviewerId);
    boolean existsByIdeaIdAndReviewerId(Long ideaId, Long reviewerId);
}
