package pl.maxim.blog.rating;

import jakarta.persistence.*;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.user.AppUser;

import java.time.Instant;

@Entity
@Table(
        name = "post_rating",
        uniqueConstraints = @UniqueConstraint(name = "uq_post_rating_post_user", columnNames = {"post_id", "user_id"})
)
public class PostRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private short value;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public short getValue() { return value; }
    public void setValue(short value) { this.value = value; }
    public Instant getCreatedAt() { return createdAt; }
}
