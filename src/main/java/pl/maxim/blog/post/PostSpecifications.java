package pl.maxim.blog.post;

import org.springframework.data.jpa.domain.Specification;

public class PostSpecifications {

    public static Specification<Post> textInTitleOrContent(String text) {
        if (text == null || text.isBlank()) return null;
        String like = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("content").as(String.class)), like)
        );
    }

    public static Specification<Post> hasAuthorUsername(String username) {
        if (username == null || username.isBlank()) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            var authors = root.join("authors");
            return cb.equal(cb.lower(authors.get("username")), username.toLowerCase());
        };
    }
}
