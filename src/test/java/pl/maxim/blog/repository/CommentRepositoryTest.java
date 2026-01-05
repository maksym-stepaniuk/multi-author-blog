package pl.maxim.blog.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.maxim.blog.comment.Comment;
import pl.maxim.blog.comment.CommentRepository;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest extends JpaTcTestBase {

    @Autowired
    CommentRepository commentRepo;

    @Autowired
    PostRepository postRepo;

    @Autowired
    AppUserRepository userRepo;

    @Test
    void findByPostId_pageable() {
        AppUser a = new AppUser();
        a.setUsername("c1");
        a.setEmail("c1@ex.com");
        a.setPassword("x");
        a.setRole(Role.USER);
        userRepo.save(a);

        Post p = new Post();
        p.setTitle("pt");
        p.setContent("pc");
        p.getAuthors().add(a);
        postRepo.save(p);

        for (int i = 0; i < 3; i++) {
            Comment c = new Comment();
            c.setPost(p);
            c.setAuthor(a);
            c.setContent("m" + i);
            commentRepo.save(c);
        }

        var page = commentRepo.findByPostId(p.getId(), org.springframework.data.domain.PageRequest.of(0, 2));
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().size()).isEqualTo(2);
    }

    @Test
    void delete_comment() {
        AppUser a = new AppUser();
        a.setUsername("c2");
        a.setEmail("c2@ex.com");
        a.setPassword("x");
        a.setRole(Role.USER);
        userRepo.save(a);

        Post p = new Post();
        p.setTitle("pt2");
        p.setContent("pc2");
        p.getAuthors().add(a);
        postRepo.save(p);

        Comment c = new Comment();
        c.setPost(p);
        c.setAuthor(a);
        c.setContent("x");
        commentRepo.save(c);

        commentRepo.delete(c);
        assertThat(commentRepo.findAll()).isEmpty();
    }
}
