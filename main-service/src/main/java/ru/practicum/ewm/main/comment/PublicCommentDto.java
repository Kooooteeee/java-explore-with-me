package ru.practicum.ewm.main.comment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PublicCommentDto {

    private String text;

    private String eventTitle;

    private Long eventId;

    private String authorName;

    private Long authorId;

    private LocalDateTime updated;
}
