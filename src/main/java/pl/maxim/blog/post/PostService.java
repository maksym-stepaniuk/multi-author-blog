package pl.maxim.blog.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.maxim.blog.common.ResourceNotFoundException;
import pl.maxim.blog.post.dto.PostCreateRequest;
import pl.maxim.blog.post.dto.PostResponse;
import pl.maxim.blog.post.dto.PostUpdateRequest;
import pl.maxim.blog.post.dto.UserSummary;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final AppUserRepository userRepository;

    public PostService(PostRepository postRepository, AppUserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> list(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        return toResponse(post);
    }

    @Transactional
    public PostResponse create(PostCreateRequest req) {
        Post post = new Post();
        post.setTitle(req.title());
        post.setContent(req.content());
        post.setAuthors(fetchAuthors(req.authorIds()));
        return toResponse(postRepository.save(post));
    }

    @Transactional
    public PostResponse update(Long id, PostUpdateRequest req) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        post.setTitle(req.title());
        post.setContent(req.content());
        post.setAuthors(fetchAuthors(req.authorIds()));
        return toResponse(postRepository.save(post));
    }

    @Transactional
    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found: " + id);
        }
        postRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> search(String text, String authorUsername, Pageable pageable) {
        Specification<Post> spec = Specification.where(PostSpecifications.textInTitleOrContent(text))
                .and(PostSpecifications.hasAuthorUsername(authorUsername));
        return postRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private Set<AppUser> fetchAuthors(List<Long> authorIds) {
        List<AppUser> users = userRepository.findAllById(authorIds);
        if (users.size() != new HashSet<>(authorIds).size()) {
            throw new ResourceNotFoundException("One or more authors not found");
        }
        return new HashSet<>(users);
    }

    private PostResponse toResponse(Post post) {
        Double avg = postRepository.findAverageRating(post.getId());
        var authors = post.getAuthors().stream()
                .map(u -> new UserSummary(u.getId(), u.getUsername()))
                .sorted(Comparator.comparing(UserSummary::username))
                .collect(Collectors.toList());
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                authors,
                avg,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
