package pl.maxim.blog.web.admin;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.maxim.blog.admin.AdminUserService;
import pl.maxim.blog.admin.dto.AdminUserUpdateRequest;
import pl.maxim.blog.post.PostService;
import pl.maxim.blog.post.dto.PostResponse;
import pl.maxim.blog.post.dto.PostUpdateRequest;
import pl.maxim.blog.post.dto.UserSummary;
import pl.maxim.blog.user.Role;
import pl.maxim.blog.user.UserService;
import pl.maxim.blog.web.forms.AdminUserForm;
import pl.maxim.blog.web.forms.PostForm;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/moderation")
public class AdminModerationController {

    private final AdminUserService adminUserService;
    private final PostService postService;
    private final UserService userService;

    public AdminModerationController(AdminUserService adminUserService, PostService postService, UserService userService) {
        this.adminUserService = adminUserService;
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "username"));
        model.addAttribute("users", adminUserService.list(q, pageable));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("size", size);
        return "admin_users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        var u = adminUserService.get(id);
        AdminUserForm form = new AdminUserForm();
        form.setEmail(u.email());
        form.setRole(u.role());
        form.setEnabled(u.enabled());

        model.addAttribute("userId", id);
        model.addAttribute("username", u.username());
        model.addAttribute("adminUserForm", form);
        model.addAttribute("roles", Arrays.asList(Role.values()));
        return "admin_user_form";
    }

    @PostMapping("/users/{id}")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("adminUserForm") AdminUserForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("roles", Arrays.asList(Role.values()));
            return "admin_user_form";
        }

        adminUserService.update(id, new AdminUserUpdateRequest(
                form.getEmail(),
                form.getRole(),
                form.getEnabled(),
                form.getPassword()
        ));

        return "redirect:/admin/moderation/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        adminUserService.delete(id);
        return "redirect:/admin/moderation/users";
    }

    @GetMapping("/posts")
    public String posts(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostResponse> result = (text == null || text.isBlank()) && (author == null || author.isBlank())
                ? postService.list(pageable)
                : postService.search(text, author, pageable);

        model.addAttribute("posts", result);
        model.addAttribute("text", text == null ? "" : text);
        model.addAttribute("author", author == null ? "" : author);
        model.addAttribute("size", size);
        return "admin_posts";
    }

    @GetMapping("/posts/{id}/edit")
    public String editPost(@PathVariable Long id, Model model) {
        PostResponse post = postService.get(id);

        PostForm form = new PostForm();
        form.setTitle(post.title());
        form.setContent(post.content());
        form.setAuthorIds(post.authors().stream().map(UserSummary::id).toList());

        model.addAttribute("postId", id);
        model.addAttribute("postForm", form);
        model.addAttribute("users", allUsersForSelect());
        return "admin_post_form";
    }

    @PostMapping("/posts/{id}")
    public String updatePost(
            @PathVariable Long id,
            @Valid @ModelAttribute("postForm") PostForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("users", allUsersForSelect());
            return "admin_post_form";
        }

        postService.update(id, new PostUpdateRequest(form.getTitle(), form.getContent(), form.getAuthorIds()));
        return "redirect:/admin/moderation/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        postService.delete(id);
        return "redirect:/admin/moderation/posts";
    }

    private List<pl.maxim.blog.user.dto.UserResponse> allUsersForSelect() {
        Pageable pageable = PageRequest.of(0, 500, Sort.by(Sort.Direction.ASC, "username"));
        return userService.search(null, pageable).getContent();
    }
}
