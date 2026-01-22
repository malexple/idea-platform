package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.Vote;
import com.company.ideaplatform.entity.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByIdeaIdAndUserId(Long ideaId, Long userId);

    List<Vote> findByIdeaId(Long ideaId);

    boolean existsByIdeaIdAndUserId(Long ideaId, Long userId);

    long countByIdeaId(Long ideaId);

    long countByUserId(Long userId);

    @Query("SELECT v.voteType, COUNT(v) FROM Vote v WHERE v.idea.id = :ideaId GROUP BY v.voteType")
    List<Object[]> countByIdeaIdGroupByVoteType(@Param("ideaId") Long ideaId);

    @Query("SELECT SUM(CASE WHEN v.voteType IN ('MUST_HAVE', 'CONVENIENT', 'USEFUL', 'FULLY_SUPPORT', 'INTERESTING') " +
            "THEN 1 ELSE 0 END) FROM Vote v WHERE v.idea.id = :ideaId")
    Long countPositiveVotes(@Param("ideaId") Long ideaId);
}
