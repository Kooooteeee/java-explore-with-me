package ru.practicum.ewm.main.comment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDto {

    private Long id;

    private String text;

    private LocalDateTime created;

    private LocalDateTime updated;

    private CommentsStatus status;

    private String eventTitle;

    private Long eventId;

    private String authorName;

    private Long authorId;
}
