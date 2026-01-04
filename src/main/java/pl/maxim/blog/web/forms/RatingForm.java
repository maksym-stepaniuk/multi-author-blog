package pl.maxim.blog.web.forms;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RatingForm {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer value;

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
