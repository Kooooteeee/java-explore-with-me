package ru.practicum.ewm.main.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.event.Event;

import ru.practicum.ewm.main.user.User;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

    public static Comment toComment(NewCommentDto newCommentDto, Event event, User author) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setEvent(event);
        comment.setAuthor(author);
        return comment;
    }

    public static void updateComment(Comment comment, NewCommentDto newCommentDto) {
        comment.setText(newCommentDto.getText());
    }

    public static PublicCommentDto toPublicCommentDto(Comment comment) {
        PublicCommentDto publicCommentDto = new PublicCommentDto();
        publicCommentDto.setText(comment.getText());
        publicCommentDto.setEventId(comment.getEvent().getId());
        publicCommentDto.setEventTitle(comment.getEvent().getTitle());
        publicCommentDto.setAuthorId(comment.getAuthor().getId());
        publicCommentDto.setAuthorName(comment.getAuthor().getName());
        publicCommentDto.setUpdated(comment.getUpdated());
        return publicCommentDto;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getCreated());
        commentDto.setUpdated(comment.getUpdated());
        commentDto.setStatus(comment.getStatus());
        commentDto.setEventId(comment.getEvent().getId());
        commentDto.setEventTitle(comment.getEvent().getTitle());
        commentDto.setAuthorId(comment.getAuthor().getId());
        commentDto.setAuthorName(comment.getAuthor().getName());
        return commentDto;
    }
}