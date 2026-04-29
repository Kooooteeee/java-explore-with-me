package ru.practicum.ewm.main.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.EventState;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.request.EventRequestRepository;
import ru.practicum.ewm.main.request.RequestStatus;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    private final CommentRepository commentRepository;

    private final EventRequestRepository requestRepository;

    @Override
    public List<PublicCommentDto> findPublishedByEvent(Long eventId, int from, int size) {
        Event event = findEventByIdOrThrow(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Такого события нет!");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Comment> comments = commentRepository.findAllByEvent_IdAndStatusOrderByUpdatedDesc(eventId,
                CommentsStatus.PUBLISHED, pageable);

        return comments.stream()
                .map(CommentMapper::toPublicCommentDto)
                .toList();
    }

    @Override
    public PublicCommentDto findPublishedById(Long commentId) {
        Comment comment = findCommentByIdOrThrow(commentId);
        if (!comment.getStatus().equals(CommentsStatus.PUBLISHED)) {
            throw new NotFoundException("Такого комментария нет!");
        }
        return CommentMapper.toPublicCommentDto(comment);
    }

    @Override
    public CommentDto create(Long authorId, Long eventId, NewCommentDto newCommentDto) {

        Event event = findEventByIdOrThrow(eventId);
        User author = findUserByIdOrThrow(authorId);

        checkUserParticipation(authorId, eventId);

        Comment comment = CommentMapper.toComment(newCommentDto, event, author);
        LocalDateTime now = LocalDateTime.now();
        comment.setCreated(now);
        comment.setUpdated(now);
        comment.setStatus(CommentsStatus.PENDING);

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    public CommentDto updateByAuthor(NewCommentDto newCommentDto, Long authorId, Long commentId) {
        findUserByIdOrThrow(authorId);

        Optional<Comment> comment = commentRepository.findByIdAndAuthor_Id(commentId, authorId);

        if (comment.isEmpty()) {
            throw new ConflictException("Пользователь не является автором комментария!");
        }

        comment.get().setText(newCommentDto.getText());
        comment.get().setStatus(CommentsStatus.PENDING);
        comment.get().setUpdated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment.get());
        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    public void deleteByAuthor(Long authorId, Long commentId) {

        findUserByIdOrThrow(authorId);

        Optional<Comment> comment = commentRepository.findByIdAndAuthor_Id(commentId, authorId);

        if (comment.isEmpty()) {
            throw new ConflictException("Пользователь не является автором комментария!");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto findByIdForAuthor(Long authorId, Long commentId) {
        findUserByIdOrThrow(authorId);

        Optional<Comment> comment = commentRepository.findByIdAndAuthor_Id(commentId, authorId);

        if (comment.isEmpty()) {
            throw new ConflictException("Пользователь не является автором комментария!");
        }

        return CommentMapper.toCommentDto(comment.get());
    }

    @Override
    public List<CommentDto> findAllByFiltersForAdmin(List<Long> eventIds, List<Long> authorIds,
                                                     List<CommentsStatus> statuses, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        if (eventIds != null && eventIds.isEmpty()) {
            eventIds = null;
        }

        if (authorIds != null && authorIds.isEmpty()) {
            authorIds = null;
        }

        if (statuses != null && statuses.isEmpty()) {
            statuses = null;
        }

        List<Comment> comments = commentRepository.findAllByAdminFilters(eventIds,authorIds,
                statuses, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public CommentDto findByIdForAdmin(Long commentId) {
        Comment comment =  findCommentByIdOrThrow(commentId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public CommentDto updateStatusByAdmin(Long commentId, String status) {

        if (status == null) {
            throw new ConflictException("Некорректный статус!");
        }

        Comment comment = findCommentByIdOrThrow(commentId);

        CommentsStatus commentStatus;
        try {
            commentStatus = CommentsStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConflictException("Некорректный статус!");
        }

        if (commentStatus != CommentsStatus.PUBLISHED && commentStatus != CommentsStatus.REJECTED) {
            throw new ConflictException("Некорректный статус!");
        }

        comment.setStatus(commentStatus);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteByAdmin(Long commentId) {
        findCommentByIdOrThrow(commentId);
        commentRepository.deleteById(commentId);
    }

    private Event findEventByIdOrThrow(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        return event.orElseThrow(() -> new NotFoundException("Такого события нет!"));
    }

    private User findUserByIdOrThrow(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new NotFoundException("Такого пользователя нет!"));
    }

    private void checkUserParticipation(Long userId, Long eventId) {
        if (!requestRepository.existsByRequesterIdAndEventIdAndStatus(userId, eventId,
                RequestStatus.CONFIRMED)) {
            throw  new NotFoundException("Пользователь не является участником события!");
        }
    }

    private Comment findCommentByIdOrThrow(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        return comment.orElseThrow(() -> new NotFoundException("Такого комментария нет!"));
    }


}
