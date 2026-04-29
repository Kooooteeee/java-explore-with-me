package ru.practicum.ewm.main.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    public CommentDto create(@PathVariable Long userId,
                             @PathVariable Long eventId,
                             @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.create(userId, eventId, newCommentDto);
    }

    @GetMapping("/comments/{commentId}")
    public CommentDto findByIdForAuthor(@PathVariable Long userId,
                                        @PathVariable Long commentId) {
        return commentService.findByIdForAuthor(userId, commentId);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateByAuthor(@PathVariable Long userId,
                                     @PathVariable Long commentId,
                                     @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.updateByAuthor(newCommentDto, userId, commentId);
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteByAuthor(@PathVariable Long userId,
                               @PathVariable Long commentId) {
        commentService.deleteByAuthor(userId, commentId);
    }
}