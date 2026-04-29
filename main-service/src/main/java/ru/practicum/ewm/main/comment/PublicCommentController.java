package ru.practicum.ewm.main.comment;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    public List<PublicCommentDto> findPublishedByEvent(@PathVariable Long eventId,
                                                       @RequestParam(defaultValue = "0")
                                                       @PositiveOrZero int from,
                                                       @RequestParam(defaultValue = "10")
                                                       @Positive int size) {
        return commentService.findPublishedByEvent(eventId, from, size);
    }

    @GetMapping("/comments/{commentId}")
    public PublicCommentDto findPublishedById(@PathVariable Long commentId) {
        return commentService.findPublishedById(commentId);
    }
}