package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.Idea;
import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.entity.enums.IdeaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IdeaRepository extends JpaRepository<Idea, Long> {

    Optional<Idea> findByNumber(String number);

    Page<Idea> findByStatus(IdeaStatus status, Pageable pageable);

    Page<Idea> findByType(IdeaType type, Pageable pageable);

    Page<Idea> findByAuthorId(Long authorId, Pageable pageable);

    List<Idea> findByAuthorId(Long authorId);

    @Query("SELECT i FROM Idea i WHERE i.status = :status AND i.reviewDeadline < :deadline")
    List<Idea> findOverdueForReview(@Param("status") IdeaStatus status,
                                    @Param("deadline") LocalDateTime deadline);

    @Query("SELECT i FROM Idea i WHERE i.status IN :statuses ORDER BY i.createdAt DESC")
    Page<Idea> findByStatusIn(@Param("statuses") List<IdeaStatus> statuses, Pageable pageable);

    @Query("SELECT i FROM Idea i LEFT JOIN i.votes v " +
            "WHERE i.status = 'VOTING' " +
            "GROUP BY i.id ORDER BY COUNT(v) DESC")
    Page<Idea> findTopVotedIdeas(Pageable pageable);

    @Query("SELECT COUNT(i) FROM Idea i WHERE i.author.id = :authorId AND i.type = :type")
    long countByAuthorIdAndType(@Param("authorId") Long authorId, @Param("type") IdeaType type);

    @Query("SELECT COUNT(i) FROM Idea i WHERE i.author.id = :authorId AND i.status = :status")
    long countByAuthorIdAndStatus(@Param("authorId") Long authorId, @Param("status") IdeaStatus status);

    @Query("SELECT COUNT(i) FROM Idea i WHERE i.type = :type")
    long countByType(@Param("type") IdeaType type);

    @Query("SELECT COUNT(i) FROM Idea i WHERE i.status = :status")
    long countByStatus(@Param("status") IdeaStatus status);

    @Query("SELECT COUNT(i) FROM Idea i WHERE i.team.id = :teamId")
    long countByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT i.number FROM Idea i ORDER BY i.id DESC LIMIT 1")
    Optional<String> findLastNumber();
}
