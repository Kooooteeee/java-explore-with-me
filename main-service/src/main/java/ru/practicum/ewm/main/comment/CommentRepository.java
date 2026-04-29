package ru.practicum.ewm.main.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEvent_IdAndStatusOrderByUpdatedDesc(Long eventId,
                                                               CommentsStatus status,
                                                               Pageable pageable);

    Optional<Comment> findByIdAndAuthor_Id(Long commentId, Long authorId);

    @Query("""
        select c
        from Comment c
        where (:eventIds is null or c.event.id in :eventIds)
          and (:authorIds is null or c.author.id in :authorIds)
          and (:statuses is null or c.status in :statuses)
        order by c.created desc
        """)
    List<Comment> findAllByAdminFilters(List<Long> eventIds,
                                        List<Long> authorIds,
                                        List<CommentsStatus> statuses,
                                        Pageable pageable);
}