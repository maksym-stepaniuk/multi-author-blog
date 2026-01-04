package pl.maxim.blog.web.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentForm {

    @NotBlank
    @Size(max = 5000)
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
