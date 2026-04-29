package ru.practicum.ewm.main.comment;

import java.util.List;

public interface CommentService {
    List<PublicCommentDto> findPublishedByEvent(Long eventId, int from, int size);

    PublicCommentDto findPublishedById(Long commentId);

    CommentDto create(Long authorId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateByAuthor(NewCommentDto newCommentDto, Long authorId, Long commentId);

    void deleteByAuthor(Long authorId, Long commentId);

    CommentDto findByIdForAuthor(Long authorId, Long commentId);

    List<CommentDto> findAllByFiltersForAdmin(List<Long> eventIds, List<Long> authorIds,
                                              List<CommentsStatus> statuses,
                                              int from, int size);

    CommentDto findByIdForAdmin(Long commentId);

    CommentDto updateStatusByAdmin(Long commentId, String status);

    void deleteByAdmin(Long commentId);

}
