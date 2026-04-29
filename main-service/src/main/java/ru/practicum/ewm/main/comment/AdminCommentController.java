package ru.practicum.ewm.main.comment;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> findAllByFiltersForAdmin(@RequestParam(required = false) List<Long> eventIds,
                                                     @RequestParam(required = false) List<Long> authorIds,
                                                     @RequestParam(required = false) List<CommentsStatus> statuses,
                                                     @RequestParam(defaultValue = "0")
                                                     @PositiveOrZero int from,
                                                     @RequestParam(defaultValue = "10")
                                                     @Positive int size) {
        return commentService.findAllByFiltersForAdmin(eventIds, authorIds, statuses, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto findByIdForAdmin(@PathVariable Long commentId) {
        return commentService.findByIdForAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateStatusByAdmin(@PathVariable Long commentId,
                                          @RequestParam String status) {
        return commentService.updateStatusByAdmin(commentId, status);
    }

    @DeleteMapping("/{commentId}")
    public void deleteByAdmin(@PathVariable Long commentId) {
        commentService.deleteByAdmin(commentId);
    }
}