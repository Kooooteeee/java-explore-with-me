package ru.practicum.ewm.main.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", length = 5000, nullable = false)
    private String text;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentsStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
}
