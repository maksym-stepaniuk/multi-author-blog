package pl.maxim.blog.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.maxim.blog.comment.dto.CommentCreateRequest;
import pl.maxim.blog.comment.dto.CommentResponse;
import pl.maxim.blog.common.ResourceNotFoundException;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.post.dto.UserSummary;
import pl.maxim.blog.security.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CurrentUserService currentUserService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, CurrentUserService currentUserService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> list(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable).map(c ->
                new CommentResponse(
                        c.getId(),
                        c.getPost().getId(),
                        new UserSummary(c.getAuthor().getId(), c.getAuthor().getUsername()),
                        c.getContent(),
                        c.getCreatedAt()
                )
        );
    }

    @Transactional
    public CommentResponse add(Long postId, CommentCreateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        var user = currentUserService.requireUser();

        Comment c = new Comment();
        c.setPost(post);
        c.setAuthor(user);
        c.setContent(req.content());

        Comment saved = commentRepository.save(c);
        log.info("User {} added comment {} to post {}", user.getId(), saved.getId(), postId);
        return new CommentResponse(
                saved.getId(),
                postId,
                new UserSummary(user.getId(), user.getUsername()),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public void delete(Long postId, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        if (!c.getPost().getId().equals(postId)) {
            throw new ResourceNotFoundException("Comment not found: " + commentId);
        }

        var user = currentUserService.requireUser();
        boolean owner = c.getAuthor().getId().equals(user.getId());

        if (!owner && !currentUserService.isAdmin()) {
            throw new AccessDeniedException("Forbidden");
        }

        commentRepository.delete(c);
        log.info("Comment {} deleted by user {}", commentId, user.getId());
    }
}
