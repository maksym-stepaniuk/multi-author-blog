package pl.maxim.blog.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    @Query("select avg(r.value) from PostRating r where r.post.id = :postId")
    Double findAverageRating(@Param("postId") Long postId);

    Page<Post> findDistinctByAuthorsId(Long authorId, Pageable pageable);

    @Query("select p from Post p join p.authors a where a.username = :username")
    Page<Post> findByAuthorUsername(@Param("username") String username, Pageable pageable);
}
