package pl.maxim.blog.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.maxim.blog.comment.CommentService;
import pl.maxim.blog.comment.dto.CommentCreateRequest;
import pl.maxim.blog.message.MessageService;
import pl.maxim.blog.message.dto.SendMessageRequest;
import pl.maxim.blog.post.PostService;
import pl.maxim.blog.post.dto.PostResponse;
import pl.maxim.blog.rating.RatingService;
import pl.maxim.blog.rating.dto.RatePostRequest;
import pl.maxim.blog.web.forms.CommentForm;
import pl.maxim.blog.web.forms.MessageForm;
import pl.maxim.blog.web.forms.RatingForm;

@Controller
public class PublicPostController {

    private final PostService postService;
    private final CommentService commentService;
    private final RatingService ratingService;
    private final MessageService messageService;

    public PublicPostController(PostService postService, CommentService commentService, RatingService ratingService, MessageService messageService) {
        this.postService = postService;
        this.commentService = commentService;
        this.ratingService = ratingService;
        this.messageService = messageService;
    }

    @GetMapping("/")
    public String wall(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<PostResponse> posts = (isBlank(text) && isBlank(author))
                ? postService.list(pageable)
                : postService.search(text, author, pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("text", text == null ? "" : text);
        model.addAttribute("author", author == null ? "" : author);
        model.addAttribute("size", size);

        return "wall";
    }

    @GetMapping("/posts/{id}")
    public String post(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int cpage,
            @RequestParam(defaultValue = "10") int csize,
            Model model
    ) {
        return fillPostPage(id, cpage, csize, model, new CommentForm(), new RatingForm(), new MessageForm());
    }

    @PostMapping("/posts/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int cpage,
            @RequestParam(defaultValue = "10") int csize,
            @Valid @ModelAttribute("commentForm") CommentForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            return fillPostPage(id, cpage, csize, model, form, new RatingForm(), new MessageForm());
        }
        commentService.add(id, new CommentCreateRequest(form.getContent()));
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/rating")
    public String rate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int cpage,
            @RequestParam(defaultValue = "10") int csize,
            @Valid @ModelAttribute("ratingForm") RatingForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            return fillPostPage(id, cpage, csize, model, new CommentForm(), form, new MessageForm());
        }
        ratingService.rate(id, new RatePostRequest(form.getValue()));
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/message")
    public String message(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int cpage,
            @RequestParam(defaultValue = "10") int csize,
            @Valid @ModelAttribute("messageForm") MessageForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            return fillPostPage(id, cpage, csize, model, new CommentForm(), new RatingForm(), form);
        }
        messageService.send(new SendMessageRequest(form.getRecipientId(), form.getContent()));
        return "redirect:/posts/" + id;
    }

    private String fillPostPage(Long id, int cpage, int csize, Model model, CommentForm commentForm, RatingForm ratingForm, MessageForm messageForm) {
        PostResponse post = postService.get(id);

        Pageable pageable = PageRequest.of(cpage, csize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var comments = commentService.list(id, pageable);

        if (messageForm.getRecipientId() == null && post.authors() != null && !post.authors().isEmpty()) {
            messageForm.setRecipientId(post.authors().get(0).id());
        }

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);

        model.addAttribute("commentForm", commentForm);
        model.addAttribute("ratingForm", ratingForm);
        model.addAttribute("messageForm", messageForm);

        model.addAttribute("cpage", cpage);
        model.addAttribute("csize", csize);

        return "post";
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
