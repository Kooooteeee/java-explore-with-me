package ru.practicum.ewm.main.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCommentDto {
    @NotBlank
    @Size(max = 5000)
    private String text;
}
