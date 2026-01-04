package pl.maxim.blog.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.maxim.blog.post.PostService;
import pl.maxim.blog.post.dto.PostResponse;
import pl.maxim.blog.post.dto.UserSummary;
import pl.maxim.blog.security.CurrentUserService;
import pl.maxim.blog.user.UserService;
import pl.maxim.blog.user.dto.UserResponse;
import pl.maxim.blog.web.forms.PostForm;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final PostService postService;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public DashboardController(PostService postService, UserService userService, CurrentUserService currentUserService) {
        this.postService = postService;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String dashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var posts = postService.myPosts(pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("size", size);
        return "dashboard";
    }

    @GetMapping("/posts/new")
    public String newPost(Model model) {
        PostForm form = new PostForm();
        var current = currentUserService.requireUser();
        form.getAuthorIds().add(current.getId());

        model.addAttribute("postForm", form);
        model.addAttribute("users", allUsersForSelect());
        model.addAttribute("mode", "create");
        return "post_form";
    }

    @PostMapping("/posts")
    public String create(
            @Valid @ModelAttribute("postForm") PostForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("users", allUsersForSelect());
            model.addAttribute("mode", "create");
            return "post_form";
        }

        var current = currentUserService.requireUser();
        if (!currentUserService.isAdmin() && !form.getAuthorIds().contains(current.getId())) {
            throw new AccessDeniedException("You must be an author of your post");
        }

        PostResponse created = postService.create(new pl.maxim.blog.post.dto.PostCreateRequest(
                form.getTitle(),
                form.getContent(),
                form.getAuthorIds()
        ));

        return "redirect:/dashboard";
    }

    @GetMapping("/posts/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        PostResponse post = postService.get(id);

        PostForm form = new PostForm();
        form.setTitle(post.title());
        form.setContent(post.content());
        form.setAuthorIds(post.authors().stream().map(UserSummary::id).toList());

        model.addAttribute("postId", id);
        model.addAttribute("postForm", form);
        model.addAttribute("users", allUsersForSelect());
        model.addAttribute("mode", "edit");
        return "post_form";
    }

    @PostMapping("/posts/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("postForm") PostForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("users", allUsersForSelect());
            model.addAttribute("mode", "edit");
            return "post_form";
        }

        PostResponse updated = postService.update(id, new pl.maxim.blog.post.dto.PostUpdateRequest(
                form.getTitle(),
                form.getContent(),
                form.getAuthorIds()
        ));

        return "redirect:/dashboard";
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.delete(id);
        return "redirect:/dashboard";
    }

    private List<UserResponse> allUsersForSelect() {
        Pageable pageable = PageRequest.of(0, 200, Sort.by(Sort.Direction.ASC, "username"));
        return userService.search(null, pageable).getContent();
    }
}
