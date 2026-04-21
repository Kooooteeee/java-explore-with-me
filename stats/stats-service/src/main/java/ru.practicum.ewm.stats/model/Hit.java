package ru.practicum.ewm.stats.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@Getter
@Setter
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app", nullable = false, length = 255)
    private String app;

    @Column(name = "uri", nullable = false, length = 512)
    private String uri;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}
