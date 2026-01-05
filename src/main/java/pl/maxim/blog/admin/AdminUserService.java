package pl.maxim.blog.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.maxim.blog.admin.dto.AdminUserUpdateRequest;
import pl.maxim.blog.common.ResourceNotFoundException;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.dto.UserResponse;

import java.util.List;

@Service
public class AdminUserService {

    private final AppUserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AppUserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(String q, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(q == null ? "" : q, pageable)
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.isEnabled()));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        AppUser u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.isEnabled());
    }

    @Transactional
    public UserResponse update(Long id, AdminUserUpdateRequest req) {
        AppUser u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        u.setEmail(req.email());
        u.setRole(req.role());
        u.setEnabled(req.enabled());

        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.password()));
        }

        AppUser saved = userRepository.save(u);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole(), saved.isEnabled());
    }

    @Transactional
    public void delete(Long id) {
        AppUser u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        List<Post> posts = postRepository.findDistinctByAuthorsId(id);
        for (Post p : posts) {
            var authors = new java.util.HashSet<>(p.getAuthors());
            authors.removeIf(a -> a.getId().equals(id));
            p.setAuthors(authors);
            if (authors.isEmpty()) {
                postRepository.delete(p);
            } else {
                postRepository.save(p);
            }
        }

        userRepository.delete(u);
    }
}
